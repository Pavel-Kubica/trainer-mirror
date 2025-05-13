package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.LessonModuleCopyDTO
import cz.fit.cvut.wrzecond.trainer.dto.LessonModuleEditDTO
import cz.fit.cvut.wrzecond.trainer.service.LessonModuleService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * LessonModuleController is responsible for handling HTTP requests related to lessonModule.
 *
 * @param service Service for handling lessonModule-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@RequestMapping("/lessons/{id}/modules")
class LessonModuleController(private val service: LessonModuleService, userService: UserService)
: IControllerAuth(userService) {

    @GetMapping("/with-active-quizroom")
    fun getModuleWithActiveQuizrooms(@PathVariable id: Int,
                                    request: HttpServletRequest, response: HttpServletResponse,
                                    @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.getModuleWithActiveQuizrooms(id, user) }

    /**
     * Retrieves lesson, module and their users for specific lesson and module
     *
     * @param id The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A LessonModuleUserListDTO object.
     */
    @GetMapping("/{moduleId}/users")
    fun getLessonModuleUsers(@PathVariable id: Int, @PathVariable moduleId: Int,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getLessonModuleUsers(id, moduleId, user) }

    @GetMapping("/{moduleId}")
    fun getLessonModule(@PathVariable id: Int, @PathVariable moduleId: Int,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.getLessonModule(id, moduleId, user) }

    /**
     * Copies a lesson module to a specified lesson.
     *
     * @param id The ID of the lesson to which the module should be copied.
     * @param dto The DTO containing information about the module to be copied and its order.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A LessonModuleReadDTO object.
     */
    @PostMapping
    fun copyLessonModule(@PathVariable id: Int, @RequestBody dto: LessonModuleCopyDTO,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.copyLessonModule(id, dto, user) }

    /**
     * Creates or updates a lesson module for a specific lesson and module.
     *
     * @param id The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param dto The DTO containing the new information of the lesson module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A LessonModuleReadDTO object.
     */
    @PutMapping("/{moduleId}")
    fun createLessonModule(@PathVariable id: Int, @PathVariable moduleId: Int,
                           @RequestBody dto: LessonModuleEditDTO,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.putLessonModule(id, moduleId, dto, user) }



    @GetMapping()
    fun getModulesByLesson(@PathVariable id: Int,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getModulesByLesson(id, user) }

    /**
     * Deletes a lesson module for a specific lesson and module.
     *
     * @param id The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLessonModule(@PathVariable id: Int, @PathVariable moduleId: Int,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.delLessonModule(id, moduleId, user) }

}
