package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

/**
 * Service for managing CourseUsers.
 *
 * @property repository Repository for course entities.
 * @property cuRepository Repository for course user entities.
 * @property roleRepository Repository for role entities.
 * @property userRepository Repository for user entities.
 * @property weekRepository Repository for week entities.
 * @property lessonRepository Repository for lesson entities.
 * @property subjectRepository Repository for subject entities.
 * @property logRepository Repository for log entities.
 * @property studentModuleRepository Repository for student module entities.
 * @property sandboxUserRepository Repository for sandbox user entities.
 */
@Service
class CourseUserService(
    override val repository: CourseRepository,
    private val cuRepository: CourseUserRepository,
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val weekRepository: WeekRepository,
    private val lessonRepository: LessonRepository,
    private val subjectRepository: SubjectRepository,
    private val logRepository: LogRepository,
    private val studentModuleRepository: StudentModuleRepository,
    private val sandboxUserRepository: SandboxUserRepository
) : IServiceBase<Course>(repository, userRepository) {

    /**
     * Find all users in given course
     * @param courseId identifier of searched course
     * @return CourseUserList with user roles and progress
     */
    fun findCourseUsers(courseId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(courseId, userDto) { course, _ ->
            CourseUserList(
                converter.toFindDTO(course, null),
                cuRepository.findByCourse(course).map { cu -> converter.toReadDTO(cu, course) }
            )
        }

    /**
     * Find all teachers in given course
     * @param courseId identifier of searched course
     * @return [CourseUserReadDTO]
     */
    fun findCourseTeachers(courseId: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(courseId, userDto) { course, user ->
            cuRepository.findTeachersByCourse(course, user).map { teacher ->
                CourseUserReadDTO(teacher.id, teacher.username, teacher.name, RoleLevel.TEACHER, null)
            }
        }

    /**
     * Gets detail of one course user
     * @param courseId identifier of searched course
     * @param userId identifier of searched user
     * @return CourseUserDetail with lessons and progress
     */
    fun getCourseUser(courseId: Int, userId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(courseId, userDto) { course, _ ->
            val otherUser = userRepository.getReferenceById(userId)
            CourseUserDetail(
                converter.toGetDTO(course, otherUser),
                converter.toFindDTO(otherUser)
            )
        }

    /**
     * Add new user to a given course
     * @param courseId identifier of the course
     * @param dtos user datas and desired role
     * @param userDto user performing the request
     * @return [CourseUserReadDTO] in case of success
     */
    @Transactional
    fun addCourseUsers(courseId: Int, dtos: List<CourseUserEditDTO>, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        val course = repository.getReferenceById(courseId)
        if (!(user.isAdmin || course.subject?.guarantors?.any{it.guarantor.id == user.id} == true) )
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val courseUsers = dtos.map { dto ->
            // Create sandbox course for teacher if it does not exist
            if (dto.role == RoleLevel.TEACHER) {
                val potentialTeacher = userRepository.getByUsername(dto.username)
                if (potentialTeacher != null) {
                    CourseService.createSandbox(
                        repository, cuRepository, weekRepository, lessonRepository,
                        roleRepository, subjectRepository, userRepository, logRepository, studentModuleRepository, sandboxUserRepository, potentialTeacher
                    )
                }
            }

            // Find the user or create new if it does not exist
            val otherUser = userRepository.getByUsername(dto.username) ?: userRepository.saveAndFlush(
                User(
                    UserService.generateSecret(), dto.username,
                    dto.name ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST),
                    Timestamp.from(Instant.now()), false, emptyList(), emptyList(), emptyList(), emptyList(), emptyList()
                )
            )
            val role = roleRepository.getByLevel(dto.role)
            CourseUser(course, otherUser, role)
        }
        val result = cuRepository.saveAllAndFlush(courseUsers).map {
            logRepository.saveAndFlush(createLogEntry(userDto, it, "create"))
            converter.toReadDTO(it, course) }
        result
    }

    /**
     * Remove user from a given course
     * @param courseId identifier of the course
     * @param userId deleted user's id
     * @param userDto user performing the request
     */
    fun delCourseUser(courseId: Int, userId: Int, userDto: UserAuthenticateDto?): Unit = tryCatch {
        val user = getUser(userDto)
        val course = repository.getReferenceById(courseId)
        if (!(user.isAdmin || course.subject?.guarantors?.any{it.guarantor.id == user.id} == true) )
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val otherUser = userRepository.getReferenceById(userId)
        val cu = cuRepository.getByCourseUser(course, otherUser) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        cuRepository.delete(cu)
        logRepository.saveAndFlush(createLogEntry(userDto, cu, "delete"))
    }

}
