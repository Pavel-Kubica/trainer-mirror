package cz.fit.cvut.wrzecond.trainer.controller.quiz

import cz.fit.cvut.wrzecond.trainer.controller.IControllerAuth
import cz.fit.cvut.wrzecond.trainer.controller.VisibilitySettings
import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizroomStudentCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizroomStudentUpdateDTO
import cz.fit.cvut.wrzecond.trainer.service.quiz.QuizroomStudentService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * QuizroomStudentController is responsible for handling HTTP requests related to quizroomStudent.
 *
 * @param service Service for handling quizroomStudent-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@RequestMapping("/rooms/{id}/students")
class QuizroomStudentController(private val service: QuizroomStudentService, userService: UserService)
    : IControllerAuth(userService){


    /**
     * Retrieves the list of students associated with a specific quiz room.
     *
     * @param id The ID of the quiz room.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of QuizroomStudentFindDTO, which represents a list of students for the specified quiz room.
     */
    @GetMapping
    fun findStudentsInQuizroom(@PathVariable id: Int,
                               request: HttpServletRequest, response: HttpServletResponse,
                               @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findAllQuizroomStudents(id,user)}

    /**
     * Retrieves a specific student associated with a specific quiz room.
     *
     * @param id The ID of the quiz room.
     * @param studentId The ID of the student.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuizroomStudentGetDTO object.
     */
    @GetMapping("/{studentId}")
    fun getStudentInQuizroom(@PathVariable id: Int, @PathVariable studentId: Int,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getQuizroomStudent(id, studentId, user)}

    /**
     * Adds a student into a specified quiz room.
     *
     * @param id The ID of the quiz room.
     * @param dto DTO containing details of the student and quiz room.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuizroomStudentGetDTO object.
     */
    @PostMapping
    fun addStudentIntoQuizroom(@PathVariable id: Int, @RequestBody dto: QuizroomStudentCreateDTO,
                               request: HttpServletRequest, response: HttpServletResponse,
                               @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.createQuizroomStudent(id, dto, user)}

    /**
     * Updates a student in a specific quiz room.
     *
     * @param id The ID of the quiz room.
     * @param studentId The ID of the student.
     * @param dto DTO containing an updated details of the student and quiz room.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuizroomStudentGetDTO object.
     */
    @PutMapping("/{studentId}")
    fun updateStudentInQuizroom(@PathVariable id: Int, @PathVariable studentId: Int,
                                @RequestBody dto: QuizroomStudentUpdateDTO,
                                request: HttpServletRequest, response: HttpServletResponse,
                                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.updateQuizroomStudent(id, studentId, dto, user)}


}