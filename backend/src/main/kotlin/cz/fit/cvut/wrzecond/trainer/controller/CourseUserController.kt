package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.CourseUserEditDTO
import cz.fit.cvut.wrzecond.trainer.service.CourseUserService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * CourseUserController is responsible for handling HTTP requests related to course user relations.
 *
 * @param service Service for handling courseUser-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@RequestMapping("/courses/{id}/users")
class CourseUserController(private val service: CourseUserService, userService: UserService)
: IControllerAuth(userService) {

    /**
     * Retrieves a course and users associated with it.
     *
     * @param id The ID of the course for which to find users.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A CourseUserList object.
     */
    @GetMapping
    fun findCourseUsers(@PathVariable id: Int,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findCourseUsers(id, user) }

    /**
     * Retrieves teachers for a specific course.
     *
     * @param id The ID of the course for which to find teachers.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of UserCourseReadDTO, which represents a list of teachers associated with given course.
     */
    @GetMapping("/teachers")
    fun findCourseTeachers(@PathVariable id: Int,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findCourseTeachers(id, user) }

    /**
     * Retrieves a specific user and course.
     *
     * @param id The ID of the course.
     * @param userId The ID of the user.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A CourseUserDetail object.
     */
    @GetMapping("/{userId}")
    fun getCourseUser(@PathVariable id: Int, @PathVariable userId: Int,
                      request: HttpServletRequest, response: HttpServletResponse,
                      @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getCourseUser(id, userId, user) }

    /**
     * Creates or adds users to a specified course.
     *
     * @param id The ID of the course to which users are to be added.
     * @param dtos A list of CourseUserEditDTO containing user details and roles.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of CourseUserReadDTO, which represent list of created orr added users to a course.
     */
    @PostMapping
    fun createCourseUsers(@PathVariable id: Int, @RequestBody dtos: List<CourseUserEditDTO>,
                          request: HttpServletRequest, response: HttpServletResponse,
                          @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.addCourseUsers(id, dtos, user) }

    /**
     * Deletes a specific user from a specified course.
     *
     * @param id The ID of the course from which the user is to be removed.
     * @param userId The ID of the user to be removed from the course.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCourseUser(@PathVariable id: Int, @PathVariable userId: Int,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.delCourseUser(id, userId, user) }

}
