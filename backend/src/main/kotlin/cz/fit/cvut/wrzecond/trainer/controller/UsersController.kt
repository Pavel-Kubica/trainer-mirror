package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * WeekController is responsible for handling HTTP requests related to users.
 *
 * @param service Service for handling user-related operations
 */
@RestController
@Visibility(getByID = VisibilitySettings.LOGGED)
@RequestMapping("/users")
class UsersController(override val service: UserService)
    : IControllerImpl<User, UserFindDTO, UserGetDto, ICreateDTO, IUpdateDTO>(
            service,
            service
    ) {

    /**
     * Retrieves all users. Supports filtering and pagination.
     *
     * @param name The optional name to filter users. If null, no filtering is applied based on name.
     * @param limit The maximum number of items on page. Must be provided.
     * @param offset The desired page's number. Must be provided.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication
     * @return A page of UserFindDTO objects.
     */
    @GetMapping(params = ["limit", "offset"])
    fun findAll(@RequestParam(required = false) name: String?, @RequestParam(required = true) limit: Int,
                @RequestParam(required = true) offset: Int,
                request: HttpServletRequest, response: HttpServletResponse,
                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
     = authenticate(request, VisibilitySettings.LOGGED, loginSecret) {user ->
         setCookie(loginSecret, response)
         service.findAll(name, limit, offset, user)}

    /**
     * Swaps current user to predefined test user.
     *
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication
     */
    @GetMapping("/swap")
    fun swapToTestUser(request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
            authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
                setCookie(loginSecret, response)
                if (user != null && user.username != "test01")
                    service.swapToTestUser(user)
                else {
                    val realUser = service.getByLoginSecret(loginSecret)
                    if (user != null && realUser != null)
                        service.swapToTestUser(UserAuthenticateDto(realUser.id, realUser.username, user.client, user.ipAddress))
                }
            }

    /**
     * Retrieves a sandbox for a specific user.
     *
     * @param id The ID of the user for whom a sandbox will be retrieved.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication
     * @return An optional CourseGetDTO objeect.
     */
    @GetMapping("/{id}/sandbox")
    fun getSandbox(@PathVariable id: Int, request: HttpServletRequest, response: HttpServletResponse,
                   @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
            authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.getSandbox(id, user)
            }

    @GetMapping("/teacher")
    fun isTeacher(request: HttpServletRequest, response: HttpServletResponse,
                  @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
            authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.taughtCourses(user)
            }

    @PatchMapping("/{id}/gitlabToken")
    fun updateGitlabToken(@PathVariable id: Int, request: HttpServletRequest,  @RequestBody dto: UserGitlabUpdateDto,
                          response: HttpServletResponse,
                          @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.updateToken(id, user, dto)
        }
}