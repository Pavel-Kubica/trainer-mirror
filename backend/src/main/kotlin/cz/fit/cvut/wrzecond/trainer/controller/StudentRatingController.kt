package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.StudentRating
import cz.fit.cvut.wrzecond.trainer.service.StudentRatingService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * StudentRatingController is responsible for handling HTTP requests related to student ratings of StudentModules.
 *
 * @param service Service for handling studentRating-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/studentRatings")
class StudentRatingController(override val service: StudentRatingService, userService: UserService)
:  IControllerImpl<StudentRating, StudentRatingFindDTO, StudentRatingGetDTO, StudentRatingCreateDTO, StudentRatingUpdateDTO>(
    service,
    userService
) {
    /**
     * Retrieve student ratings for module by user and module.
     *
     * @param moduleId The ID of the module for which to retrieve ratings.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return An optional StudentRatingGetDTO object.
     */
    @GetMapping("/{moduleId}")
    fun getByUserAndModule(@PathVariable moduleId: Int, request: HttpServletRequest,
                           response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request,VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
            service.getByUserAndModule(moduleId, user)
        }

}
