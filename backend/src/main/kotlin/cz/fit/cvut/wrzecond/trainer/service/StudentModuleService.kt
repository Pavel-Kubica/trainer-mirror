package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.lang.Integer.max
import java.sql.Timestamp
import java.time.Instant

/**
 * Service for managing student modules and related requests.
 *
 * @property repository Repository for managing Lesson entities.
 * @property smRepository Repository for managing StudentModule entities.
 * @property moduleRepository Repository for managing Module entities.
 * @property smrqRepository Repository for managing StudentModuleRequest entities.
 * @property codeCommentRepository Repository for managing CodeComment entities.
 * @property userRepository Repository for managing User entities.
 */
@Service
class StudentModuleService(override val repository: LessonRepository,
                           private val smRepository: StudentModuleRepository,
                           private val moduleRepository: ModuleRepository,
                           private val smrqRepository: StudentModuleRequestRepository,
                           private val codeCommentRepository: CodeCommentRepository,
                           private val userRepository: UserRepository)
: IServiceBase<Lesson>(repository, userRepository) {

    /**
     * Set module data for student
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param dto desired data
     * @param userDto user performing the request
     * @return StudentModuleReadDTO in case of success
     */
    fun putStudentModule(lessonId: Int, moduleId: Int, dto: StudentModuleEditDTO, userDto: UserAuthenticateDto?)
        = checkViewAccess(lessonId, moduleId, userDto) { lesson, module, user ->
            val sm = smRepository.getByStudentModule(user, module, lesson)
            val now = Timestamp.from(Instant.now())

        // check unlocked
        if (module.lockable && lesson.lockCode != null && sm?.unlocked != true)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        // set resulting percent
        val resultPercent = dto.percent?.let { max(it, sm?.percent ?: 0) } ?: sm?.percent

        // completed early = completed, has time limit, lesson has time limit and completed in time limit
        val completedEarly = (resultPercent ?: 0) >= module.minPercent && module.timeLimit &&
                lesson.timeEnd != null && now < lesson.timeEnd

        converter.toReadDTO(smRepository.saveAndFlush(StudentModule(
            lesson, module, user,
            resultPercent, sm?.openedOn ?: now,
            sm?.completedOn ?: if (!module.manualEval && (resultPercent ?: 0) >= module.minPercent) now else null,
            dto.allowedShow ?: (sm?.allowedShow ?: false), sm?.completedEarly == true || completedEarly, true,
            sm?.file,sm?.requests ?: emptyList(), sm?.id ?: 0
        )))
    }

    /**
     * Get student module request
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param userId identifier of the user
     * @param userDto user performing the request
     * @return List<StudentRequestDTO>
     */
    fun getStudentModuleRequests(lessonId: Int, moduleId: Int, userId: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(lessonId, moduleId, userDto) { lesson, module, _ ->
            val student = userRepository.getReferenceById(userId)
            val sm = smRepository.getByStudentModule(student, module, lesson)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
            smrqRepository.getAllByStudentModule(sm, false).map {
                converter.toStudentRequestDTO(it)
            }
        }

    /**
     * Get student module request for current user
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param userDto user performing the request
     * @return StudentRequestDTO in case of success
     */
    fun getStudentModuleRequests(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(lessonId, moduleId, userDto) { _, _, user ->
            getStudentModuleRequests(lessonId, moduleId, user.id, userDto)
        }

    /**
     * Create (or update) existing StudentModuleRequest
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param dto desired data
     * @param userDto user performing the request
     */
    fun putStudentModuleRequest(lessonId: Int, moduleId: Int, dto: StudentModuleRequestEditDTO, userDto: UserAuthenticateDto?)
        = checkViewAccess(lessonId, moduleId, userDto) { lesson, module, user ->
            val sm = smRepository.getByStudentModule(user, module, lesson) ?:
                smRepository.saveAndFlush(emptySM(lesson, module, user))
            val now = Timestamp.from(Instant.now())
            val smrq = smrqRepository.getByStudentModule(sm, true)

        smrqRepository.saveAndFlush(StudentModuleRequest(
            sm, smrq?.teacher, dto.requestType, dto.requestText, now,
            null, false, null, smrq?.id ?: 0
        ))
        converter.toReadDTO(sm)
    }

    /**
     * Delete existing StudentModuleRequest
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param userDto user performing the request
     */
    fun deleteStudentModuleRequest(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(lessonId, moduleId, userDto) { lesson, module, user ->
            val sm = smRepository.getByStudentModule(user, module, lesson)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
            val smrq = smrqRepository.getByStudentModule(sm, true)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
            smrqRepository.delete(smrq)
        }

    /**
     * Set module data for student (teacher side)
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param userId identifier of student to be updated
     * @param dto desired data
     * @param userDto user performing the request
     * @return StudentModuleReadDTO in case of success
     */
    fun putStudentModuleRequestTeacher(lessonId: Int, moduleId: Int, userId: Int, dto: StudentModuleRequestTeacherDTO,
                             userDto: UserAuthenticateDto?) = checkEditAccess(lessonId, userDto) { lesson, user ->
        val module = moduleRepository.getReferenceById(moduleId)
        val student = userRepository.getReferenceById(userId)
        val sm = smRepository.getByStudentModule(student, module, lesson)
            ?: smRepository.saveAndFlush(emptySM(lesson, module, student))

        val now = Timestamp.from(Instant.now())
        val smr = smrqRepository.getByStudentModule(sm, true) ?: StudentModuleRequest(
            sm, null, StudentModuleRequestType.COMMENT, null, now, null, false, null
        )

        // Comment and satisfy
        smrqRepository.saveAndFlush(smr.copy(
            teacher = user, teacherResponse = dto.responseText, satisfied = true, satisfiedOn = now
        ))

        // Evaluate / comment = update completedOn and percent if evaluated
        if (smr.requestType != StudentModuleRequestType.HELP) {
            return@checkEditAccess converter.toReadDTO(smRepository.saveAndFlush(sm.copy(
                completedOn = if (dto.evaluation == true) now else sm.completedOn,
                percent = dto.percent ?: sm.percent
            )))
        }

        return@checkEditAccess converter.toReadDTO(sm)
    }


    /**
     * Delete the answer to the student request
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param userId identifier of student to be updated
     * @param userDto user performing the request
     */
    @Transactional
    fun deleteStudentRequestAnswerTeacher(lessonId: Int, moduleId: Int, userId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(lessonId, userDto) { lesson, _ ->
        val module = moduleRepository.getReferenceById(moduleId)
        val student = userRepository.getReferenceById(userId)
        val sm = smRepository.getByStudentModule(student, module, lesson)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val smr = smrqRepository.getByStudentModule(sm)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        // if it is a comment, delete the request
        if (smr.requestType == StudentModuleRequestType.COMMENT) {
            smrqRepository.delete(smr)
            return@checkEditAccess
        }

        smrqRepository.saveAndFlush(smr.copy(
            teacher = null, teacherResponse = null, satisfied = false, satisfiedOn = null
        ))

        // delete code comments if any
        codeCommentRepository.deleteByModuleRequest(smr)
    }

    /**
     * Set module file for student
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param file the file for module
     * @param userDto user performing the request
     */
    fun putStudentModuleFile(lessonId: Int, moduleId: Int, file: MultipartFile, userDto: UserAuthenticateDto?)
        = checkViewAccess(lessonId, moduleId, userDto) { lesson, module, user ->
            val sm = smRepository.getByStudentModule(user, module, lesson) ?: emptySM(lesson, module, user)

        // check unlocked
        if (module.lockable && lesson.lockCode != null && !sm.unlocked)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val fileName = sm.file ?: "${FileService.generateFileName()}.tar"
        try { fileService.saveFile(file, fileName, FileService.UPLOADS_PATH_STUDENTS) }
        catch (_: FileAlreadyExistsException) { throw ResponseStatusException(HttpStatus.CONFLICT) }
        converter.toReadDTO(smRepository.saveAndFlush(sm.copy(file = fileName)))
    }

    /**
     * Get module file of student
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param userDto user performing the request
     */
    fun getStudentModuleFile(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(lessonId, moduleId, userDto) { lesson, module, user ->
            val filePath = smRepository.getByStudentModule(user, module, lesson)?.file
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
            fileService.readFile(filePath, FileService.UPLOADS_PATH_STUDENTS)
        }

    /**
     * Get module file of student (teacher side)
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param userId identifier of the user
     * @param userDto user performing the request
     */
    fun getStudentModuleFile(lessonId: Int, moduleId: Int, userId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(lessonId, userDto) { lesson, _ ->
            val module = moduleRepository.getReferenceById(moduleId)
            val student = userRepository.getReferenceById(userId)

        val filePath = smRepository.getByStudentModule(student, module, lesson)?.file
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        fileService.readFile(filePath, FileService.UPLOADS_PATH_STUDENTS)
    }

    /**
     * Delete own module (teacher only)
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param userDto performing the request
     */
    fun deleteUserModule(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?)
            = checkEditAccess(lessonId, userDto) { lesson, user ->
        val module = moduleRepository.getReferenceById(moduleId)
        val sm = smRepository.getByStudentModule(user, module, lesson)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        smRepository.delete(sm)
    }


    /**
     * Retrieves a StudentModule based on the provided lesson, module, and user details.
     *
     * @param lessonId Identifier of the lesson.
     * @param moduleId Identifier of the module.
     * @param userDto User performing the request.
     * @return A found StudentModule as DTO.
     */

    fun getByUserModuleLesson(lessonId: Int, moduleId: Int, userId: Int, userDto: UserAuthenticateDto?) = checkViewAccess(lessonId, userDto){
            _, _ ->
        val user = userRepository.getReferenceById(userId)
        val module = moduleRepository.getReferenceById(moduleId)
        val lesson = repository.getReferenceById(lessonId)
        smRepository.findByUserModuleLesson(user,module,lesson)?.let { converter.toGetDTO(it) }
    }


    /**
     * Helper function to check edit access for a lesson and read access for a module.
     *
     * @param X The return type of the block function.
     * @param lessonId Identifier of the lesson.
     * @param moduleId Identifier of the module.
     * @param userDto User performing the request.
     * @param block Function to be executed if permissions are granted. It receives a Lesson, Module, and User.
     */
    private fun <X> checkViewAccess(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?,
                                    block: (Lesson, Module, User) -> X)
            = checkViewAccess(lessonId, userDto) { lesson, user ->
        val module = moduleRepository.getReferenceById(moduleId)
        if (!module.canView(user)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        block(lesson, module, user)
    }

    /**
     * Helper function to generate an empty student module based on provided lesson, module, and user.
     *
     * @param lesson the lesson associated with the student module
     * @param module the module associated with the student module
     * @param user the user associated with the student module
     */
    private fun emptySM(lesson: Lesson, module: Module, user: User) = StudentModule(lesson, module, user,
        null, Timestamp.from(Instant.now()), null, false,
        false, false, null,  emptyList()
    )

}
