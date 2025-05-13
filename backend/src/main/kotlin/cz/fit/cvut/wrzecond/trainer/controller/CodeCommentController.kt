package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.CodeCommentEditDTO
import cz.fit.cvut.wrzecond.trainer.service.StudentModuleRequestService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * CodeCommentController is responsible for handling HTTP requests related to semesters.
 *
 * @param service Service for handling studentModuleRequest-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@RequestMapping("/student-help-requests")
class CodeCommentController(private val service: StudentModuleRequestService, userService: UserService)
: IControllerAuth(userService) {

    /**
     * Retrieves code comments for a specific student module request.
     *
     * @param requestId The ID of the student module request for which comments are to be retrieved.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of CodeCommentFindDTO objects.
     */
    @GetMapping("/{requestId}/code-comments")
    fun getComments(@PathVariable requestId: Int,
                    request: HttpServletRequest, response: HttpServletResponse,
                    @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getCodeComments(requestId, user)
        }

    /**
     * Adds a code comment for a specific student module request.
     *
     * @param requestId The ID of the student module request for which a comment will be added.
     * @param dto DTO containing new comment information.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A CodeCommentFindDTO for new comment.
     */
    @PostMapping("/{requestId}/code-comments")
    fun postComment(@PathVariable requestId: Int, @RequestBody dto: CodeCommentEditDTO,
                    request: HttpServletRequest, response: HttpServletResponse,
                    @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.createCodeComment(requestId, dto, user)
        }
}