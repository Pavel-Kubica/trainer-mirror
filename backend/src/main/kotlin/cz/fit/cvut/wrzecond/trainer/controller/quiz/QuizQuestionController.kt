package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.QuizQuestionEditDTO
import cz.fit.cvut.wrzecond.trainer.service.QuizQuestionService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * QuizQuestionController is responsible for handling HTTP requests for adding and removing question from quizzes.
 *
 * @param service Service for handling quizQuestion-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@RequestMapping("/quizzes/{id}/questions")
class QuizQuestionController(private val service: QuizQuestionService, userService: UserService)
    : IControllerAuth(userService) {

    /**
     * Adds question to a specified quiz.
     *
     * @param id The ID of the quiz.
     * @param questionId The ID of the question to be added.
     * @param dto The DTO containing the order number for the question.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication
     * @return A  QuizQuestionReadDTO object.
     */
    @PutMapping("/{questionId}")
    fun createQuizQuestion(@PathVariable id: Int, @PathVariable questionId: Int,
                           @RequestBody dto: QuizQuestionEditDTO, request: HttpServletRequest,
                           response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, {it.create}, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.putQuizQuestion(id, questionId, dto, user) }

    /**
     * Removes question from a specified quiz.
     *
     * @param id The ID of the quiz.
     * @param questionId The ID of the question to be removed.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication
     * @return A QuizQuestionReadDTO object.
     */
    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteQuizQuestion(@PathVariable id: Int, @PathVariable questionId: Int, request: HttpServletRequest,
                            response: HttpServletResponse,
                            @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, {it.delete}, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.delQuizQuestion(id, questionId, user) }

}
