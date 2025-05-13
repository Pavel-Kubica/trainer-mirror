package cz.fit.cvut.wrzecond.trainer.controller.quiz

import cz.fit.cvut.wrzecond.trainer.controller.IControllerAuth
import cz.fit.cvut.wrzecond.trainer.controller.Visibility
import cz.fit.cvut.wrzecond.trainer.controller.VisibilitySettings
import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.quiz.StudAnswerCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.StudAnswerFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.StudAnswerUpdateDTO
import cz.fit.cvut.wrzecond.trainer.service.quiz.StudAnswerService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * StudAnswerController is responsible for handling HTTP requests related to studentAnswer.
 *
 * @param service Service for handling studentAnswer-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/answers")
class StudAnswerController (private val service: StudAnswerService, userService: UserService)
    : IControllerAuth(userService) {

    /**
     * Retrieves student answers based on provided query parameters: quiz room ID, question ID, and student ID.
     *
     * @param quizroom Optional quiz room ID to filter the student answers.
     * @param question Optional question ID to filter the student answers.
     * @param student Optional student ID to filter the student answers.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of StudentAnswerFindDTOs.
     * @throws ResponseStatusException if the provided parameters are invalid or if authentication fails.
     */
    @GetMapping
    fun findStudentAnswers(@RequestParam(value = "quizroom", required = false) quizroom : Int?,
                           @RequestParam(value = "question", required = false) question : Int?,
                           @RequestParam(value = "student", required = false) student : Int?,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) : List<StudAnswerFindDTO> {

        setCookie(loginSecret, response)
        if(quizroom == null && question == null){
            return authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user -> service.findAll(user)}
        }
        else if(quizroom != null && question != null && student != null){
            return authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user -> service.findAllByRoomAndQuestionAndStudent(quizroom, question, student, user)}
        }
        else if(quizroom != null && question != null){
            return authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user -> service.findAllByRoomAndQuestion(question,quizroom, user)}
        }
        else if(quizroom != null && student != null){
            return authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user -> service.findAllByRoomAndStudent(quizroom,student,user)}
        }
        throw ResponseStatusException(HttpStatus.BAD_REQUEST)

    }

    /**
     * Retrieves a student answer by its ID.
     *
     * @param id The ID of the student.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A StudAnswerGetDTO object.
     */
    @GetMapping("/{id}")
    fun getStudentAnswerById(@PathVariable id: Int,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.getByID }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.getByID(id, user) }


    /**
     * Creates new student answer.
     *
     * @param dto DTO containing the details of the student answer.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A StudAnswerGetDTO object.
     */
    @PostMapping
    fun createStudentAnswer(@RequestBody dto: StudAnswerCreateDTO,
                            request: HttpServletRequest, response: HttpServletResponse,
                            @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.create }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.create(dto, user) }

    /**
     * Updates the details of a quizroom.
     *
     * @param id The ID of the quizroom.
     * @param dto DTO containing the updated details of the quizroom.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A StudAnswerGetDTO object.
     */
    @PutMapping("/{id}")
    fun updateQuizroom(@PathVariable id: Int, @RequestBody dto: StudAnswerUpdateDTO,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.update }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.update(id, dto, user) }

    /**
     * Deletes student answer.
     *
     * @param id The ID of the student answer.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{id}")
    fun deleteStudentAnswer(@PathVariable id: Int,
                            request: HttpServletRequest, response: HttpServletResponse,
                            @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.delete }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.delete(id, user) }
}