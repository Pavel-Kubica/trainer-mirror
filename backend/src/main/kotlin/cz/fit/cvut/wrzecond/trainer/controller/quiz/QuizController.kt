package cz.fit.cvut.wrzecond.trainer.controller.quiz

import cz.fit.cvut.wrzecond.trainer.controller.IControllerImpl
import cz.fit.cvut.wrzecond.trainer.controller.Visibility
import cz.fit.cvut.wrzecond.trainer.controller.VisibilitySettings
import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizUpdateDTO
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quiz
import cz.fit.cvut.wrzecond.trainer.service.quiz.QuizService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * QuizController is responsible for handling HTTP requests related to quiz.
 *
 * @param service Service for handling quiz-related operations.
 * @param userService Service for handling user-related operations.
 * @param quizService Service quiz-related operations.
 */
@RestController
@Visibility
@RequestMapping("/quizzes")
class QuizController(service : QuizService, userService: UserService, private val quizService: QuizService)
    : IControllerImpl<Quiz, QuizFindDTO, QuizGetDTO, QuizCreateDTO, QuizUpdateDTO>(service, userService){

    /**
     * Retrieves a module by its ID.
     *
     * @param moduleId The ID of the module to be retrieved.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuizGetDTO object.
     */
    @GetMapping("/module/{moduleId}")
    fun getByModuleId(@PathVariable moduleId : Int,
                      request: HttpServletRequest, response: HttpServletResponse,
                      @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request,VisibilitySettings.LOGGED, loginSecret) { user ->
                setCookie(loginSecret, response)
                quizService.getQuizByModuleId(moduleId, user)}
}