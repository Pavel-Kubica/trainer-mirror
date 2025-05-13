package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.service.LessonUserService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * LessonUserController is responsible for handling HTTP requests related to lessonUser.
 *
 * @param service Service for handling lessonUser-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@RequestMapping("/lessons/{id}/users")
class LessonUserController(private val service: LessonUserService, userService: UserService)
: IControllerAuth(userService) {

    /**
     * Retrieves users for a specific lesson.
     *
     * @param id The ID of the lesson.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A LessonUserListDTO object.
     */
    @GetMapping
    fun getLessonUsers(@PathVariable id: Int,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getLessonUsers(id, user) }

    /**
     * Retrieves a specific user for a specific lesson.
     *
     * @param id The ID of the lesson.
     * @param userId The ID of the user.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A LessonUserReadDTO object.
     */
    @GetMapping("/{userId}")
    fun getLessonUser(@PathVariable id: Int, @PathVariable userId: Int,
                      request: HttpServletRequest, response: HttpServletResponse,
                      @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getLessonUser(id, userId, user) }

    /**
     * Retrieves the week details for a specific lesson and user.
     *
     * @param id The ID of the lesson.
     * @param userId The ID of the user.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return WeekDetailDTO object.
     */
    @GetMapping("/{userId}/week")
    fun getLessonUserWeekDetail(@PathVariable id: Int, @PathVariable userId: Int,
                                request: HttpServletRequest, response: HttpServletResponse,
                                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.getLessonUserWeek(id, userId, user) }

    /**
     * Resets the progress of a student in a specific lesson.
     *
     * @param id The ID of the lesson.
     * @param userId The ID of the student whose progress will be reseted.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resetLessonUser(@PathVariable id: Int, @PathVariable userId: Int,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.resetLessonUser(id, userId, user) }

}
