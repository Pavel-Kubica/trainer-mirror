package cz.fit.cvut.wrzecond.trainer.controller.quiz

import cz.fit.cvut.wrzecond.trainer.controller.IControllerAuth
import cz.fit.cvut.wrzecond.trainer.controller.Visibility
import cz.fit.cvut.wrzecond.trainer.controller.VisibilitySettings
import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.quiz.*
import cz.fit.cvut.wrzecond.trainer.service.quiz.QuestionService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.web.bind.annotation.*

/**
 * QuestionController is responsible for handling HTTP requests related to questions in quiz.
 *
 * @param service Service for handling question-related operations.
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/questions")
class QuestionController(private val service : QuestionService, userService: UserService)
    : IControllerAuth(userService) {

    /**
     * Retrieves a list of all questions or questions by a specific user.
     *
     * @param username Optional query parameter to filter questions by a specific user.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of QuestionFindDTO objects, either all questions or filtered by the specified user.
     */
    @GetMapping
    fun getAllOrByUser(@RequestParam(value = "user", required = false) username: String?,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) : List<QuestionFindDTO> {
        setCookie(loginSecret, response)
        if(username == null)
            return authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user -> service.findAll(user) }
        else{
            return authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user -> service.getByUsername(username, user)}
        }
    }

    /**
     * Retrieves a question by its ID.
     *
     * @param id The ID of the question to be retrieved.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuestionGetDTO object.
     */
    @GetMapping("/{id}")
    fun getQuestionByID(@PathVariable id: Int,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.getByID }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.getByID(id, user) }

    /**
     * Creates a new question.
     *
     * @param dto The data transfer object containing question details.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuestionGetDTO object.
     */
    @PostMapping
    fun createQuestion(@RequestBody dto: QuestionCreateDTO,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.create }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.create(dto, user) }

    /**
     * Updates a question.
     *
     * @param id The ID of the question to be updated.
     * @param dto The data transfer object containing new question details.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuestionGetDTO object.
     */
    @PutMapping("/{id}")
    fun updateQuestion(@PathVariable id: Int, @RequestBody dto: QuestionUpdateDTO,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.update }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.update(id, dto, user) }

    /**
     * Updates a question and associated student points in a quiz room.
     *
     * @param id The ID of the question to be updated.
     * @param roomId The ID of the quiz room where the question is located.
     * @param dto The data transfer object containing new question details.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuestionWithPointReasignDTO object.
     */
    @PutMapping("/{id}/quizroom/{roomId}")
    fun updateQuestionAndStudentPoints(@PathVariable id: Int, @PathVariable roomId: Int,
                                       @RequestBody dto: QuestionUpdateDTO,
                                       request: HttpServletRequest, response: HttpServletResponse,
                                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.update }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.updateWithStudentPoints(id, roomId, dto, user) }

    /**
     * Deletes a question.
     *
     * @param id The ID of the question to be deleted.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{id}")
    fun deleteQuestion(@PathVariable id: Int,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.delete }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.delete(id, user) }

    /**
     * Retrieves questions by topics associated with them.
     *
     * @param topicIds The IDs of the topics.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of QuestionGetDTO objects, questions associated with certain topics.
     */
    @GetMapping("/topics")
    fun getQuestionsByTopics(@RequestParam topicIds: List<Int>, request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, {it.getByID}, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findByTopics(topicIds, user) }

    /**
     * Retrieves topics associated with a question.
     *
     * @param id The ID of the question whose topics are to be retrieved.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of TopicFindDT objects, topics associated with certain question.
     */
    @GetMapping("/{id}/topics")
    fun getQuestionTopics(@PathVariable id: Int, request: HttpServletRequest,
                          response: HttpServletResponse, @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, {it.getByID}, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getQuestionTopics(id, user) }

    /**
     * Adds new topioc to a question.
     *
     * @param id The ID of the question to which a topic will be added.
     * @param topicId The ID of the topic which will be added to a question.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuestionGetDTO object.
     */
    @PostMapping("/{id}/topics/{topicId}")
    fun addQuestionTopic(@PathVariable id: Int, @PathVariable topicId: Int, request: HttpServletRequest,
    response: HttpServletResponse, @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, {it.create}, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.addTopic(id, topicId, user) }

    /**
     * Removes existing topic from a question.
     *
     * @param id The ID of the question from which a topic will be removed.
     * @param topicId The ID of the topic which will be removed from a question.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{id}/topics/{topicId}")
    fun removeQuestionTopic(@PathVariable id: Int, @PathVariable topicId: Int, request: HttpServletRequest,
                            response: HttpServletResponse, @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, {it.delete}, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.removeTopic(id, topicId, user) }

    /**
     * Retrieves questions by subjects associated with them.
     *
     * @param subjectIds The IDs of the subjects.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of QuestionGetDTO objects, questions associated with certain subjects.
     */
    @GetMapping("/subjects")
    fun getQuestionBySubjects(@RequestParam subjectIds: List<Int>, request: HttpServletRequest,
                              response: HttpServletResponse, @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, {it.getByID}, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findBySubjects(subjectIds, user) }

    /**
     * Retrieves subjects associated with a question.
     *
     * @param id The ID of the question whose subjects are to be retrieved.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of SubjectGetDTO objects, subjects associated with certain question.
     */
    @GetMapping("/{id}/subjects")
    fun getQuestionSubject(@PathVariable id: Int, request: HttpServletRequest,
                           response: HttpServletResponse, @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, {it.getByID}, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getQuestionSubjects(id, user) }


    /**
     * Adds new subject to a question.
     *
     * @param id The ID of the question to which a subject will be added.
     * @param subjectId The ID of the subject which will be added to a question.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuestionGetDTO object.
     */
    @PostMapping("/{id}/subjects/{subjectId}")
    fun addQuestionSubject(@PathVariable id: Int, @PathVariable subjectId: Int, request: HttpServletRequest,
                           response: HttpServletResponse, @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
                           authenticate(request, {it.create}, loginSecret) { user ->
                               setCookie(loginSecret, response)
                               service.addSubject(id, subjectId, user) }


    /**
     * Removes existing subject from a question.
     *
     * @param id The ID of the question from which a subject will be removed.
     * @param subjectId The ID of the subject which will be removed from a question.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{id}/subjects/{subjectId}")
    fun removeQuestionSubject(@PathVariable id: Int, @PathVariable subjectId: Int, request: HttpServletRequest,
                              response: HttpServletResponse, @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, {it.delete}, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.deleteSubject(id, subjectId, user) }
    //---------

    /**
     * Handles incoming WebSocket messages for new questions and processes them.
     *
     * @param questionWS the WebSocket message containing the question details.
     * @param headerAccessor provides access to the message headers.
     * @return A QuestionWebsocketGetDTO object.
     */
    @MessageMapping("/questions")
    @SendTo("/messages/questions")
    fun newQuestionCalled(questionWS: QuestionWS, headerAccessor: SimpMessageHeaderAccessor) = run {
        val authCookie = headerAccessor.sessionAttributes?.get("auth")
        service.websocketGetByID(questionWS.id, authCookie.toString(), questionWS.room)
    }

    /**
     * Rretrieve the correct answer for a given question.
     *
     * @param id The ID of the question for which the correct answer is being requested.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuestionGetCorrectDTO object.
     */
    @GetMapping("/{id}/correct")
    fun getCorrectAnswer(@PathVariable id : Int,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.getCorrectById(id,user)}

}