package cz.fit.cvut.wrzecond.trainer.controller.quiz

import cz.fit.cvut.wrzecond.trainer.controller.IControllerAuth
import cz.fit.cvut.wrzecond.trainer.controller.Visibility
import cz.fit.cvut.wrzecond.trainer.controller.VisibilitySettings
import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.quiz.*
import cz.fit.cvut.wrzecond.trainer.service.quiz.QuizroomService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * QuizroomController is responsible for handling HTTP requests related to quizzroom.
 *
 * @param service Service for handling quizroom-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/rooms")
class QuizroomController(private val service : QuizroomService, userService: UserService)
    : IControllerAuth(userService) {

    /**
     * Retrieves a list of quiz rooms either by a specified password or all accessible quiz rooms for the logged-in user.
     *
     * @param roomPassword Optional password to filter quiz rooms.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication
     * @return A list of QuizroomFindDTO objects.
     */
    @GetMapping
    fun getAllOrByPassword(@RequestParam(value = "passwd", required = false) roomPassword: String?,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) : List<QuizroomFindDTO> {
        setCookie(loginSecret, response)
        if(roomPassword == null)
            return authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user -> service.findAll(user) }
        else{
            return authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user -> service.getByPassword(roomPassword, user)}
        }
    }

    /**
     * Retrieves all quiz rooms associated with a given user and quiz.
     *
     * @param studentId The ID of the student for whom to retrieve the quiz rooms.
     * @param quizId The ID of the quiz for which to retrieve the quiz rooms.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of QuizroomListDTO objects.
     */
    @GetMapping("/all/{studentId}/quizzes/{quizId}")
    fun getAllByUserAndQuiz(@PathVariable studentId: Int, @PathVariable quizId: Int,
                            request: HttpServletRequest, response: HttpServletResponse,
                            @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getAllByUserAndQuiz(studentId, quizId, user)}

    /**
     * Retrieves the last quiz room associated with a given student and module.
     *
     * @param studentId The ID of the student for whom to retrieve the last quiz room.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param moduleId The ID of the module for which to retrieve the last quiz room.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuizroomGetDTO object.
     */
    @GetMapping("/last/{studentId}/modules/{moduleId}")
    fun getAllByUser(@PathVariable studentId: Int,
                     request: HttpServletRequest, response: HttpServletResponse,
                     @PathVariable moduleId: Int, @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.getLastRoomByUser(studentId, moduleId, user)}

    /**
     * Retrieves a quiz room by its ID.
     *
     * @param id The ID of the quiz room to be retrieved,
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuizroomGetDTO object.
     */
    @GetMapping("/{id}")
    fun getQuizroomByID(@PathVariable id: Int,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.getByID }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.getByID(id, user) }

    /**
     * Retrieves a self test by its ID.
     *
     * @param id The ID of the self test to be retrieved,
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A SelftestGetDTO object.
     */
    @GetMapping("/{id}/selftest")
    fun getSelftestByID(@PathVariable id: Int,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.getByID }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.getSelftestByID(id, user) }

    /**
     * Creates a new quiz room.
     *
     * @param dto DTO containing information needed to create the quiz room.
     * @param request The HTTP request object that contains request details
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuizroomGetDTO object.
     */
    @PostMapping
    fun createQuizroom(@RequestBody dto: QuizroomCreateDTO,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.create }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.create(dto, user) }

    /**
     * Creates a new self test quiz room.
     *
     * @param dto DTO containing information needed to create the self test quiz room.
     * @param request The HTTP request object that contains request details
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A SelftestGetDTO object.
     */
    @PostMapping("/selftest")
    fun createSelftestQuizroom(@RequestBody dto: SelftestCreateDTO,
                               request: HttpServletRequest, response: HttpServletResponse,
                               @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) : SelftestGetDTO
            = authenticate(request, { it.create }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.createSelftest(dto, user) }

    /**
     * Updates an existing quiz room.
     *
     * @param id The ID of the quiz room to be updated.
     * @param dto DTO containing new quiz rooms information.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A QuizroomGetDTO object.
     */
    @PutMapping("/{id}")
    fun updateQuizroom(@PathVariable id: Int, @RequestBody dto: QuizroomUpdateDTO,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.update }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.update(id, dto, user) }

    /**
     * Deletes an existing quiz room.
     *
     * @param id The ID of the quiz room to be deleted.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{id}")
    fun deleteQuizroom(@PathVariable id: Int,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, { it.delete }, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.delete(id, user) }

}