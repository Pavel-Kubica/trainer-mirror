package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import cz.fit.cvut.wrzecond.trainer.service.code.CodeModuleService
import cz.fit.cvut.wrzecond.trainer.service.quiz.QuizService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

/**
 * Service class for managing LessonModules.
 *
 * @property repository the LessonModule repository.
 * @property lessonRepository the repository for managing lessons.
 * @property codeModuleService the service for handling code modules.
 * @property quizService the service for handling quiz modules.
 * @property studentModuleRepository the repository for managing student modules.
 * @property moduleTopicRepository the repository for managing module topics.
 * @property moduleSubjectRepository the repository for managing module subjects.
 * @property moduleRepository the repository for managing modules.
 * @property logRepository the repository for managing logs.
 * @property userRepository the repository for managing users.
 */
@Service
class LessonModuleService(override val repository: LessonModuleRepository, private val lessonRepository: LessonRepository,
                          private val codeModuleService: CodeModuleService, private val quizService: QuizService,
                          private val quizroomRepository: QuizroomRepo,
                          private val studentModuleRepository: StudentModuleRepository,
                          private val moduleTopicRepository: ModuleTopicRepository,
                          private val moduleSubjectRepository: ModuleSubjectRepository,
                          private val moduleRepository: ModuleRepository,
                          private val logRepository: LogRepository, userRepository: UserRepository)
: IServiceBase<LessonModule>(repository, userRepository) {

    /**
     * Get the module within this lesson that currently has an active quizroom
     */
    fun getModuleWithActiveQuizrooms(lessonId: Int, userDto: UserAuthenticateDto?) = tryCatch {
        val modules = lessonRepository.getReferenceById(lessonId).modules.map { it.module }

        // We need to only return one module with an active quizroom
        // Select the one whose quizroom has the highest ID (latest created)
        val activeQuizrooms = quizroomRepository.getAllActive().sortedByDescending { it.id }
        for (room in activeQuizrooms)
        {
            val moduleWithThisRoom = modules.find { it.id == room.quiz.module.id }
            if (moduleWithThisRoom != null) {
                return@tryCatch converter.toGetDTO(moduleWithThisRoom)
            }
        }
    }

    /**
     * Get preview of all users in given lesson and module (for teacher)
     * @property lessonId lesson id to display the detail for
     * @property moduleId module id to display the detail for
     * @property userDto teacher's dto
     * @return LessonModuleUserList
     */
    fun getLessonModuleUsers(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?)
        = checkAccess(lessonId, moduleId, userDto) { lesson, module, _ ->
            val lm = repository.getByLessonModule(lesson, module)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
            LessonModuleUserListDTO(
                converter.toGetDTO(lesson), converter.toFindDTO(lm.module),
                studentModuleRepository.findByLessonModule(lesson.id, lm.module.id).map { sm ->
                    ModuleUserSingleListDTO(sm.student.id, sm.student.username, sm.student.name,
                        converter.toModuleUserDTO(sm))
                }
            )
        }


    /**
     * Get preview of all users in given lesson and module (for teacher)
     * @property lessonId lesson id to display the detail for
     * @property moduleId module id to display the detail for
     * @property userDto teacher's dto
     * @return LessonModuleReadDTO
     */
    fun getLessonModule(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?)
            = checkAccess(lessonId, moduleId, userDto) { lesson, module, _ ->
        val lm = repository.getByLessonModule(lesson, module)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        LessonModuleReadDTO(lm.id,lm.module.name,lm.module.type,1)
    }

    /**
     * Get preview of all users in given lesson and module (for teacher)
     * @property lessonId lesson id to display the detail for
     * @property moduleId module id to display the detail for
     * @property userDto teacher's dto
     * @return LessonModuleReadDTO
     */
    fun getModulesByLesson(lessonId: Int, userDto: UserAuthenticateDto?)
            = checkAccess(lessonId, userDto) { lesson, _ ->
        repository.getModulesByLesson(lesson).map{converter.toGetDTO(it)}
    }

    /**
     * Add new module to a given lesson
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param dto desired order
     * @param userDto user performing the request
     * @return ModuleReadDTO in case of success
     */
    @Transactional
    fun putLessonModule(lessonId: Int, moduleId: Int, dto: LessonModuleEditDTO, userDto: UserAuthenticateDto?)
        = checkAccess(lessonId, moduleId, userDto) { lesson, module, user ->
            val dependsOn = if (dto.depends == -1) null else dto.depends?.let { moduleRepository.getReferenceById(it) }
            val lm = repository.getByLessonModule(lesson, module)
            val id = lm?.id ?: 0
            if (id == 0 && !module.canView(user))
                throw ResponseStatusException(HttpStatus.FORBIDDEN)
            val newLm = LessonModule(lesson, module, dto.order ?: lm?.order ?: (lesson.modules.size + 1), dependsOn, id)
            val savedNewLm = repository.saveAndFlush(newLm)

        val allCourseUsers = lesson.week.course.users

        for (courseUser in allCourseUsers) {
            val existingStudentModule = studentModuleRepository.findByUserModuleLesson(courseUser.user, module, lesson)
            if (existingStudentModule == null) {
                val newStudentModule = StudentModule(lesson = lesson,module = module,student = courseUser.user,null,
                    null,null,true,false,false,null,
                     emptyList())
                studentModuleRepository.save(newStudentModule)
            }
        }
            logRepository.saveAndFlush(createLogEntry(userDto, savedNewLm, "create"))
            converter.toReadDTO(savedNewLm)
        }

    /**
     * Copy new module to a given lesson
     * @param lessonId identifier of the lesson
     * @param dto desired order
     * @param userDto user performing the request
     * @return ModuleReadDTO in case of success
     */
    @Transactional
    fun copyLessonModule(lessonId: Int, dto: LessonModuleCopyDTO, userDto: UserAuthenticateDto?)
        = checkAccess(lessonId, dto.copiedId, userDto) { lesson, module, user ->
            // Step 1: clone module (reset properties, set copier as author)
            val newModule = module.copy(lockable = false, timeLimit = false, manualEval = false,
                lastModificationTime = Timestamp.from(Instant.now()),
                lessons = emptyList(), students = emptyList(), editors = emptyList(),
                author = user, topics = emptyList(), subjects = emptyList(), notes = emptyList(), ratings = emptyList(),id = 0)
            val newModuleWithId = moduleRepository.saveAndFlush(newModule)
            module.topics.forEach() { moduleTopic ->
                moduleTopicRepository.saveAndFlush(ModuleTopic(newModule, moduleTopic.topic))
            }
            module.subjects.forEach() { moduleSubject ->
                moduleSubjectRepository.saveAndFlush(ModuleSubject(newModule, moduleSubject.subject))
            }
            // Step 2: copy extra data (code module)
            if (module.type == ModuleType.CODE)
                codeModuleService.copyModule(module, newModuleWithId)
            if (module.type == ModuleType.QUIZ)
                quizService.copyQuiz(module, newModuleWithId)

            // Step 3: put new lesson module
            val lm = LessonModule(lesson, newModuleWithId, dto.order, dependsOn = null)
            val newLm = repository.saveAndFlush(lm)
            logRepository.saveAndFlush(createLogEntry(userDto, newLm, "create"))
            converter.toReadDTO(newLm)
        }

    /**
     * Remove lesson from a given module
     * @param lessonId identifier of the lesson
     * @param moduleId identifier of the module
     * @param userDto user performing the request
     */
    fun delLessonModule(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?): Unit
        = checkAccess(lessonId, moduleId, userDto) { lesson, module, _ ->
            val lm = repository.getByLessonModule(lesson, module) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
            repository.delete(lm)
            logRepository.saveAndFlush(createLogEntry(userDto, lm, "delete"))
        }

    private fun <X> checkAccess(lessonId: Int, userDto: UserAuthenticateDto?,
                                block: (Lesson, User) -> X) = tryCatch {
        val user = getUser(userDto)
        val lesson = lessonRepository.getReferenceById(lessonId)
        if (!lesson.canEdit(user)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        block(lesson, user)
    }

    // Helper function to check edit access for lesson + read access for module
    /**
     * Helper function to check edit access for a lesson and read access for a module.
     *
     * @param lessonId Identifier of the lesson.
     * @param moduleId Identifier of the module.
     * @param userDto User authentication details.
     * @param block Lambda function to execute if the checks pass, it receives the lesson, module, and user as parameters.
     * @return The result of the lambda function execution.
     * @throws ResponseStatusException with status FORBIDDEN if the user does not have the required permissions.
     */
    private fun <X> checkAccess(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?,
                                block: (Lesson, Module, User) -> X) = tryCatch {
        val user = getUser(userDto)
        val lesson = lessonRepository.getReferenceById(lessonId)
        val module = moduleRepository.getReferenceById(moduleId)
        if (!lesson.canEdit(user) || !module.canView(user)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        block(lesson, module, user)
    }

}
