package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Course
import cz.fit.cvut.wrzecond.trainer.service.CourseService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * CourseController is responsible for handling HTTP requests related to courses.
 *
 * @param service Service for handling courses-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/courses")
class CourseController(override val service: CourseService, userService: UserService)
: IControllerImpl<Course, CourseFindDTO, CourseGetDTO, CourseCreateDTO, CourseUpdateDTO>(service, userService) {

    /**
     * Retrieves courses for a specific semester and subject.
     *
     * @param subjectId The ID of the subject.
     * @param semesterId The ID of the semester.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of optional SemesterCourseReadDTO which represents a list of courses for a specific semester and subject.
     */
    @GetMapping("/all")
    fun getBySemesterAndSubject( @RequestParam(name = "subject") subjectId: Int , @RequestParam(name = "semester") semesterId: Int,
                                 request: HttpServletRequest, response: HttpServletResponse,
                                 @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String): List<SemesterCourseReadDTO?> {
        return authenticate(request, VisibilitySettings.ALL, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getBySemesterAndSubject(semesterId,subjectId, user)
        }
    }

    /**
     * Retrieves a specific week for a specific course.
     *
     * @param id The ID of the course.
     * @param weekId The ID of the week.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A WeekGetDTO object.
     */
    @GetMapping("/{id}/weeks/{weekId}")
    fun getWeek(@PathVariable id: Int, @PathVariable weekId: Int,
                request: HttpServletRequest, response: HttpServletResponse,
                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getWeek(id, weekId, user) }

    /**
     * Joins a current user to a course with the provided course secret.
     *
     * @param dto DTO containing the course secret.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A CourseGetDTO object.
     */
    @PutMapping("/me")
    fun joinCourse(@RequestBody dto: CourseSecretDTO,
                   request: HttpServletRequest, response: HttpServletResponse,
                   @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.joinCourse(dto, user) }

    /**
     * Sets a secret for a specific course.
     *
     * @param id The ID of course.
     * @param dto DTO containing the secret.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A CourseGetDTO object.
     */
    @PutMapping("/{id}/secret")
    fun setCourseSecret(@PathVariable id: Int, @RequestBody dto: CourseSecretDTO,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.setCourseSecret(id, dto, user) }

    /**
     * Creates a sandbox for the specified teacher.
     *
     * @param id The ID of the teacher for whom to create the sandbox.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A CourseGetDTO object.
     */
    @PutMapping("/sandbox/{id}")
    fun createSandbox(@PathVariable id: Int,
                      request: HttpServletRequest, response: HttpServletResponse,
                      @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.createSandbox(id, user) }
}
