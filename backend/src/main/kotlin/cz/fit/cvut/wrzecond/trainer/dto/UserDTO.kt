package cz.fit.cvut.wrzecond.trainer.dto

/**
 * Data transfer object for user log in
 * @property id user identifier
 * @property loginSecret user login secret
 * @property username user username
 * @property name user full name
 * @property isTeacher ids of all courses, in which user is teacher
 * @property isGuarantor ids of all subjects, in which user is guarantor
 * @property isAdmin is user admin
 */
data class UserLoginDTO (val id: Int, val username: String,
                         val name: String, val isTeacher: List<Int>, val isGuarantor: List<Int>, val isAdmin: Boolean, val gitlabToken: String = "", val isBlocked: Boolean = false)

/**
 * Data transfer object for user in band members list
 * @property id unique identifier of user
 * @property username username
 * @property name name of user
 */
data class UserFindDTO (override val id: Int, val username: String, val name: String) : IFindDTO

/**
 * Data transfer object for notifications
 * @property id user id
 * @property name user name
 */
data class UserNotificationDTO (val id: Int, val name: String)

/**
 * Data Transfer Object for User entity.
 *
 * @property id Unique identifier of the user.
 * @property username Username of the user.
 * @property name Full name of the user.
 * @property isAdmin Indicates if the user has administrative privileges.
 */
data class UserGetDto(override val id: Int, val username: String, val name: String, val isAdmin: Boolean) : IGetDTO

/**
 * Data Transfer Object for authenticating user.
 *
 * @property id Unique identifier of the entity.
 * @property username The username of the user.
 * @property client The client application making the request.
 * @property ipAddress The IP address from which the request is made.
 */
data class UserAuthenticateDto(override val id: Int, val username: String, val client: String, val ipAddress: String) : IFindDTO

data class UserGitlabUpdateDto(val gitlabToken: String)