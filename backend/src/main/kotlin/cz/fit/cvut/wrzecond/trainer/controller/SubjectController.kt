package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Subject
import cz.fit.cvut.wrzecond.trainer.service.SubjectService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * SubjectController is responsible for handling HTTP requests related to subjects.
 *
 * @param service Service for handling subject-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/subjects")
class SubjectController(override val service: SubjectService, userService: UserService) :
    IControllerImpl<Subject, SubjectFindDTO, SubjectGetDTO, SubjectCreateDTO, SubjectUpdateDTO>(
        service,
        userService
    ) {

    /**
     * Retrieves guarantors for a specific subject.
     *
     * @param subjectId The ID of the subject which guarantors will be retrieved.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication
     * @return A list of UserFindDTO objects which represent guarantors.
     */
    @GetMapping("/{subjectId}/guarantors")
    fun getGuarantors(@PathVariable subjectId: Int,
                      request: HttpServletRequest, response: HttpServletResponse,
                      @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getGuarantors(subjectId, user) }

    /**
     * Adds a guarantor to a specific subject.
     *
     * @param subjectId The ID of the subject to which the guarantor will be added.
     * @param userId The ID of the user to be added as a guarantor.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @PostMapping("/{subjectId}/guarantors/{userId}")
    fun addGuarantor(@PathVariable subjectId: Int, @PathVariable userId: Int,
                     request: HttpServletRequest, response: HttpServletResponse,
                     @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.addGuarantor(subjectId, userId, user) }

    /**
     * Removes a guarantor from a specific subject.
     *
     * @param subjectId The ID of the subject from which the guarantor will be removed.
     * @param userId The ID of the user to be removed as a guarantor.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{subjectId}/guarantors/{userId}")
    fun deleteGuarantor(@PathVariable subjectId: Int, @PathVariable userId: Int,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.deleteGuarantor(subjectId, userId, user) }

    /**
     * Retrieves IDs of the subjects for which the authenticated user is a guarantor.
     *
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of Int which represent IDs of the subjects.
     */
    @GetMapping("/me")
    fun getGuarantorSubjects(request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findByGuarantor(user!!.id).map { it.id } }
}