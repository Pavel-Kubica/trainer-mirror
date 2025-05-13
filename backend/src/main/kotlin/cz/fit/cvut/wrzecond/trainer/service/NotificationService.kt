package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Lesson
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.StudentModuleRequestRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

/**
 * Service class for handling notifications related to User entities.
 *
 * @property repository The User repository for database interactions.
 * @property environment The environment configuration used to retrieve properties.
 * @property studentModuleRequestRepository The repository for handling StudentModuleRequest entities.
 */
@Service
class NotificationService(override val repository: UserRepository, environment: Environment,
                          private val studentModuleRequestRepository: StudentModuleRequestRepository)
    : IServiceBase<User>(repository, repository) {

    /** Environment constants for max student/teacher notifications */
    private final val studentNotificationMax = environment.getProperty("notification.student", Int::class.java, 30)
    private final val teacherNotificationMax = environment.getProperty("notification.teacher", Int::class.java, 100)

    /**
     * Get notifications for given user
     * equals to all StudentModuleRequests in which user is:
     * student (satisfied) – in this case, we want to display information about teacher;
     * teacher (unsatisfied) – in this case, we want to display information about student
     */
    fun getNotifications(userDto: UserAuthenticateDto?, all: Boolean) = tryCatch {
        val user = getUser(userDto)
        val now = Timestamp.from(Instant.now())
        val studentNotifications = studentModuleRequestRepository.findNotificationsStudent(user, all, studentNotificationMax).map { smr ->
            NotificationDTO(smr.id, smr.studentModule.lesson.toNotificationDTO(), smr.studentModule.module.toNotificationDTO(),
                    smr.teacher.toNotificationDTO(), smr.teacherResponse ?: "",
                    true, smr.requestType, smr.satisfied, smr.satisfiedOn ?: now)
        }
        val teacherNotifications = studentModuleRequestRepository.findNotificationsTeacher(user, all, teacherNotificationMax).map { smr ->
            NotificationDTO(smr.id, smr.studentModule.lesson.toNotificationDTO(), smr.studentModule.module.toNotificationDTO(),
                    smr.studentModule.student.toNotificationDTO(), smr.requestText ?: "",
                    false, smr.requestType, smr.satisfied, smr.requestedOn)
        }
        (studentNotifications + teacherNotifications).sortedByDescending { it.datetime }
    }

    /**
     * Mark all notifications read for current user
     * sets user lastNotificationTime to current datetime
     */
    fun deleteNotifications(userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        val now = Timestamp.from(Instant.now())
        saveAndFlush(user.copy(lastNotificationTime = now))
    }

    /**
     * Extension function to convert a `Module` entity to a `ModuleNotificationDTO`.
     *
     * @receiver The Module instance to be converted.
     * @return A ModuleNotificationDTO containing the id and name of the Module.
     */
    private fun Module.toNotificationDTO() = ModuleNotificationDTO(id, name)

    /**
     * Extension function to convert a `Lesson` entity to a `LessonNotificationDTO`.
     *
     * @receiver The Lesson instance to be converted.
     * @return A LessonNotificationDTO containing the id and name of the Lesson.
     */
    private fun Lesson.toNotificationDTO() = LessonNotificationDTO(id, name)

    /**
     * Extension function to convert a nullable `User` entity to a `UserNotificationDTO`.
     *
     * The function creates a new `UserNotificationDTO` using the `id` and `name` properties
     * of the optional `User` instance. If the `User` instance is null, the `id` defaults
     * to `-1` and the `name` defaults to `"Teacher"`.
     *
     * @receiver The nullable `User` instance to be converted.
     * @return A `UserNotificationDTO` containing the `id` and `name` of the `User`, or default values if the `User` is null.
     */
    private fun User?.toNotificationDTO() = UserNotificationDTO(this?.id ?: -1, this?.name ?: "Teacher")

}
