package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Semester
import cz.fit.cvut.wrzecond.trainer.service.SemesterService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * SemesterController is responsible for handling HTTP requests related to semesters.
 *
 * @param service Service for handling semester-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/semesters")
class SemesterController(override val service: SemesterService, userService: UserService) :
    IControllerImpl<Semester, SemesterFindDTO, SemesterGetDTO, SemesterCreateDTO, SemesterUpdateDTO>(
        service,
        userService
    ) {

    /**
     * Retrieves courses for a specific semester.
     *
     * @param id The ID of the semester.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of SemesterCourseReadDTO which represents a list of courses for a specific semester.
     */
    @GetMapping("/{id}/courses")
    fun getCourses(@PathVariable id: Int,
                   request: HttpServletRequest, response: HttpServletResponse,
                   @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findSemesterCourses(id, user) }

    /**
     * Retrieves subjects for a specific semester.
     *
     * @param id The ID of the semester.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of SubjectFindDTO which represents a list of subjects for a specific semester.
     */
    @GetMapping("/{id}/subjects")
    fun getSubjects(@PathVariable id: Int,
                    request: HttpServletRequest, response: HttpServletResponse,
                    @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findSemesterSubjects(id, user) }
}