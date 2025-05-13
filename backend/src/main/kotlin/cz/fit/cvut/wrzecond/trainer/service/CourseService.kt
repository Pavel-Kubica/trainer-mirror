package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp

/**
 * Service class for managing courses and related operations.
 * @param repository the course repository.
 * @param courseUserRepository the course user repository.
 * @param subjectRepository the subject repository.
 * @param roleRepository the role repository.
 * @param weekRepository the week repository.
 * @param userRepository the user repository.
 * @param lessonRepository the lesson repository.
 * @param logRepository the log repository.
 * @param studentModuleRepository the student module repository.
 * @param sandboxUserRepository the sandbox user repository.
 */
@Service
class CourseService(
    override val repository: CourseRepository,
    private val courseUserRepository: CourseUserRepository,
    private val subjectRepository: SubjectRepository,
    private val roleRepository: RoleRepository,
    private val weekRepository: WeekRepository,
    private val userRepository: UserRepository,
    private val lessonRepository: LessonRepository,
    private val logRepository: LogRepository,
    private val logService: LogService,
    private val studentModuleRepository: StudentModuleRepository,
    private val sandboxUserRepository: SandboxUserRepository,
) : IServiceImpl<Course, CourseFindDTO, CourseGetDTO, CourseCreateDTO, CourseUpdateDTO>(repository, userRepository) {

    /**
     * Creates a new course.
     *
     * @param dto The data transfer object containing the details of the course to be created.
     * @param userDto The data transfer object containing the authenticated user's details. Can be null.
     * @return New course as CourseGetDTO.
     * @throws ResponseStatusException if the authenticated user is not authorized to create the course.
     */
    override fun create(dto: CourseCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (dto.subject != null) {
            val subject = subjectRepository.getReferenceById(dto.subject)
            if (!subject.guarantors.any { it.guarantor.id == user.id } && !user.isAdmin)
                throw ResponseStatusException(HttpStatus.FORBIDDEN)
        } else {
            if (!user.isAdmin)
                throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        var course = converter.toEntity(dto)
        val testUser = userRepository.getByUsername("test01")
        logService.log(userDto, course, "create")
        course = repository.saveAndFlush(course)
        if (testUser != null)
            courseUserRepository.saveAndFlush(CourseUser(course, testUser, roleRepository.getByLevel(RoleLevel.STUDENT)))
        converter.toGetDTO(course)
    }

    /**
     * Updates an existing course with the provided data transfer object (DTO) and performs necessary
     * logging and access checks. The update operation includes updating associated teachers.
     *
     * @param id the ID of the course to be updated.
     * @param dto the data transfer object containing updated course information.
     * @param userDto the user authentication data transfer object of the user performing the update.
     * @return An updated course as CourseGetDTO.
     */
    override fun update(id: Int, dto: CourseUpdateDTO, userDto: UserAuthenticateDto?) =
        checkEditAccess(id, userDto) { course, _ ->
            val courseUpdated = repository.save(converter.merge(course, dto))
            val result = converter.toGetDTO(courseUpdated)
            logService.log(userDto, courseUpdated, "update")
            if (dto.teachers != null) {
                if (courseUserRepository.findRecordsOfTeacherInCourse(course).isNotEmpty()) {
                    courseUserRepository.findRecordsOfTeacherInCourse(course).forEach {
                        logService.log(userDto, it, "delete")
                    }
                    courseUserRepository.deleteAll(courseUserRepository.findRecordsOfTeacherInCourse(course))
                    courseUserRepository.flush()
                }
                dto.teachers.forEach { userFindDto ->
                    val teacher = userRepository.getByUsername(userFindDto.username)
                    if (teacher != null) {
                        createSandbox(
                            repository, courseUserRepository, weekRepository, lessonRepository,
                            roleRepository, subjectRepository, userRepository, logRepository, logService,
                            studentModuleRepository, sandboxUserRepository,teacher
                        )
                        val courseUser = courseUserRepository.getByCourseUser(course, teacher)
                        if (courseUser != null) {
                            logService.log(userDto, courseUser, "delete")
                            courseUserRepository.delete(courseUser)
                        }
                    }
                }
                val courseUsers = courseUserRepository.saveAllAndFlush(dto.teachers.map { user ->
                    CourseUser(
                        course,
                        userRepository.getReferenceById(user.id),
                        roleRepository.getByLevel(RoleLevel.TEACHER)
                    )
                })
                courseUsers.forEach {
                    logService.log(userDto, it, "create")
                }
            }
            result
        }

    /**
     * Deletes a course corresponding to the provided course ID if the user has edit access.
     *
     * This method first checks if the user has permission to edit the course. If the course contains
     * any weeks or users, a conflict exception is thrown. If not, the course is deleted from the repository
     * and a deletion log entry is created.
     *
     * @param id The ID of the course to be deleted.
     * @param userDto The user authentication details to check for edit access.
     */
    @Transactional
    override fun delete(id: Int, userDto: UserAuthenticateDto?): Unit = checkEditAccess(id, userDto) { course, _ ->
        if (course.weeks.isNotEmpty() || course.users.isNotEmpty())
            throw ResponseStatusException(HttpStatus.CONFLICT)
        repository.delete(course)
        logService.log(userDto, course, "delete")
    }

    /**
     * Retrieves all courses that the specified user can view.
     * The courses are first filtered by visibility criteria,
     * then converted to DTOs and finally sorted by semester code in descending order
     * and by name in ascending order.
     *
     * @param userDto the DTO containing authentication information of the user for whom the courses are fetched.
     * @return A list of CourseFindDTOs.
     */
    @Transactional
    override fun findAll(userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        repository.findAll(sort)
            .filter { it.canView(user) }
            .map { converter.toFindDTO(it, user) }
            .sortedWith(compareByDescending<CourseFindDTO> { it.semester?.code }.thenBy { it.name })
    }

    /**
     * Retrieves an entity's data based on the given ID.
     *
     * @param id The unique identifier of the entity to be retrieved.
     * @param userDto An optional parameter containing user authentication details
     *                to check access permissions.
     * @return A CourseGetDTO object.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { course, user ->
        converter.toGetDTO(course, user)
    }

    /**
     * Get week detail
     * @param id course id
     * @param weekId week id
     * @param userDto user performing the request
     */
    fun getWeek(id: Int, weekId: Int, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        val week = weekRepository.getReferenceById(weekId)
        if (!week.canEdit(user) || week.course.id != id)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        converter.toGetDTO(week)
    }

    /**
     * Join course with given secret
     * @param dto join dto with secret
     * @param userDto user performing the request
     */
    @Transactional
    fun joinCourse(dto: CourseSecretDTO, userDto: UserAuthenticateDto?) = tryCatch {
        // No auth, it's course JOINing
        val course = repository.getBySecret(dto.secret ?: "")
        val user = getUser(userDto)
        val role = roleRepository.getByLevel(RoleLevel.STUDENT)
        val courseUser = courseUserRepository.saveAndFlush(CourseUser(course, user, role))
        logService.log(userDto, courseUser, "create")
        converter.toGetDTO(courseUser.course)
    }

    /**
     * Get all courses based on semester and subject
     * @param semesterId id of the semester
     * @param subjectId id of the subject
     * @param userDto user performing the request
     */
    fun getBySemesterAndSubject(semesterId: Int, subjectId: Int, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        val subject = subjectRepository.getReferenceById(subjectId)

        if (!user.isAdmin && !subject.guarantors.any { it.guarantor.id == user.id })
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        repository.findAll(sort)
            .filter { it.semester?.id == semesterId && it.subject?.id == subjectId }
            .map { it.semester?.let { it1 -> converter.toFindDTO(it, it1, user) } }
    }

    /**
     * Set course secret
     * @param id course identifier
     * @param dto dto with secret
     * @param userDto user performing the request
     */
    @Transactional
    fun setCourseSecret(id: Int, dto: CourseSecretDTO, userDto: UserAuthenticateDto?) =
        checkEditAccess(id, userDto) { course, _ ->
            val updatedCourse = repository.saveAndFlush(course.copy(secret = dto.secret))
            logService.log(userDto, updatedCourse, "update")
            converter.toGetDTO(updatedCourse)
        }

    /**
     * Create sandbox course for testing modules
     * @param potentialTeacher id of the teacher to create sandbox for
     * @param userDto user performing the request
     **/
    fun createSandbox(potentialTeacher: Int, userDto: UserAuthenticateDto?): CourseGetDTO {
        val user = getUser(userDto)
        if (!(user.isAdmin ||
                    subjectRepository.findAll()
                        .any { subject -> subject.guarantors.any { it.guarantor.id == user.id } } ||
                    user.courses.any { it.user.id == user.id && it.role.level == RoleLevel.TEACHER })
        )
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val teacher = userRepository.getReferenceById(potentialTeacher)
        val sandbox = Companion.createSandbox(
            repository,
            courseUserRepository,
            weekRepository,
            lessonRepository,
            roleRepository,
            subjectRepository,
            userRepository,
            logRepository,
            logService,
            studentModuleRepository,
            sandboxUserRepository,
            teacher
        )
            ?: throw ResponseStatusException(HttpStatus.CONFLICT)
        return converter.toGetDTO(sandbox, user)
    }

    /**
     * Creates and saves a log entry for a specific operation performed on an entity.
     *
     * @param entity The entity on which the operation is performed.
     * @param operation The type of operation performed on the entity (e.g., create, update, delete).
     * @return The created Log entry that was saved to the repository.
     */
    private fun callCreateLog(entity: IEntity, operation: String): Log {
        return logRepository.saveAndFlush(createLogEntry(null, entity, operation))
    }

    /**
     * Companion object for the CourseService class, providing utility methods.
     */
    companion object {
        /**
         * Creates a sandbox course for a specified teacher if not already existing.
         *
         * @param repository The repository for managing Course entities.
         * @param cuRepository The repository for managing CourseUser entities.
         * @param weekRepository The repository for managing Week entities.
         * @param lessonRepository The repository for managing Lesson entities.
         * @param roleRepository The repository for managing Role entities.
         * @param subjectRepository The repository for managing Subject entities.
         * @param userRepository The repository for managing User entities.
         * @param logRepository The repository for managing Log entities.
         * @param studentModuleRepository The repository for managing StudentModule entities.
         * @param sandboxUserRepository The repository for managing SandboxUser entities.
         * @param teacher The teacher for whom the sandbox course should be created.
         * @return The created sandbox Course, or null if a sandbox for the teacher already exists.
         */
        fun createSandbox(
            repository: CourseRepository,
            cuRepository: CourseUserRepository,
            weekRepository: WeekRepository,
            lessonRepository: LessonRepository,
            roleRepository: RoleRepository,
            subjectRepository: SubjectRepository,
            userRepository: UserRepository,
            logRepository: LogRepository,
            logService: LogService,
            studentModuleRepository: StudentModuleRepository,
            sandboxUserRepository: SandboxUserRepository,
            teacher: User
        ): Course? {
            if (sandboxUserRepository.getSandboxUserByUserId(teacher.id) == null) {
                val sandbox = Course(
                    "Sandbox "+ teacher.username, teacher.username, false, null, null,
                    null, emptyList(), emptyList(), -1
                )
                val instance = CourseService(
                    repository,
                    cuRepository,
                    subjectRepository,
                    roleRepository,
                    weekRepository,
                    userRepository,
                    lessonRepository,
                    logRepository,
                    logService,
                    studentModuleRepository,
                    sandboxUserRepository
                )
                val sandboxCourse = repository.saveAndFlush(sandbox)
                sandboxUserRepository.saveAndFlush(
                    SandboxUser(
                        teacher,
                        sandboxCourse
                    )
                )
                instance.callCreateLog(sandboxCourse, "create")
                val role = roleRepository.getByLevel(RoleLevel.TEACHER)
                val courseUser = cuRepository.saveAndFlush(
                    CourseUser(sandboxCourse, teacher, role, 0)
                )
                instance.callCreateLog(courseUser, "create")
                val weekSandbox = weekRepository.saveAndFlush(
                    Week(
                        "Sandbox", Timestamp(682984800000), Timestamp(System.currentTimeMillis()),
                        sandboxCourse, emptyList(), 0
                    )
                )
                instance.callCreateLog(weekSandbox, "create")
                val lesson = lessonRepository.saveAndFlush(
                    Lesson(
                        "Sandbox", false, 0, null, null, "Sandbox",
                        LessonType.INFORMATION, null,null, weekSandbox, emptyList(), emptyList(), emptyList(),0
                    )
                )
                instance.callCreateLog(lesson, "create")
                return sandboxCourse
            } else {
                return null
            }
        }
    }
}
