package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.entity.StudentModuleRequestType
import java.sql.Timestamp

/**
 * Data transfer object for notification list
 * @property id notification identifier
 * @property lesson lesson connected with notification
 * @property module module connected with notification
 * @property user user connected with notification
 * @property isStudent is notification student/teacher side?
 * @property type type of notification
 * @property satisfied was notification satisfied?
 * @property datetime time when notification was received
 */
data class NotificationDTO(val id: Int, val lesson: LessonNotificationDTO, val module: ModuleNotificationDTO, val user: UserNotificationDTO, val comment: String,
                      val isStudent: Boolean, val type: StudentModuleRequestType, val satisfied: Boolean, val datetime: Timestamp)