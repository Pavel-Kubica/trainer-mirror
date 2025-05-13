package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.service.NotificationService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * NotificationController is responsible for handling HTTP requests related to semesters.
 *
 * @param service Service for handling user-related operations
 * @param notificationService Service for handling notification-related operations.
 */
@RestController
@RequestMapping("/notifications")
class NotificationController(service: UserService, private val notificationService: NotificationService)
    : IControllerAuth(service) {

    /**
     * Retrieves notifications for current user.
     * Retrieves a predefined max number of notifications.
     *
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of NotificationDTO which represents a list of notifications for a current userr.
     */
    @GetMapping
    fun getNotifications(request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        notificationService.getNotifications(user, false)
    }

    /**
     * Retrieves all notifications for current user.
     *
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of NotificationDTO which represents a list of notifications for a current userr.
     */
    @GetMapping("/all")
    fun getAllNotifications(request: HttpServletRequest, response: HttpServletResponse,
                            @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        notificationService.getNotifications(user, true)
    }

    /**
     * Mark all notifications as read for current user.
     *
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @PatchMapping()
    @ResponseStatus(HttpStatus.OK)
    fun markAllNotificationsRead(request: HttpServletRequest, response: HttpServletResponse,
                            @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) : Unit
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        notificationService.markAllNotificationsRead(user)
    }

}