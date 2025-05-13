package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO
import cz.fit.cvut.wrzecond.trainer.dto.LogFindDTO
import cz.fit.cvut.wrzecond.trainer.entity.Log
import cz.fit.cvut.wrzecond.trainer.service.LogService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * LogController is responsible for handling HTTP requests related to logs.
 *
 * @param service Service for handling log-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@RequestMapping("/logs")
@Visibility
class LogController(override val service: LogService, userService: UserService)
    : IControllerImpl<Log, LogFindDTO, IGetDTO, ICreateDTO, IUpdateDTO>(service, userService) {

    /**
     * Retrieves all log entries.
     *
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of LogFindDTO objects.
     */
    @GetMapping
    override fun all(request: HttpServletRequest, response: HttpServletResponse,
                     @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) {
        user ->
        setCookie(loginSecret, response)
        service.findAll(user)
    }
}