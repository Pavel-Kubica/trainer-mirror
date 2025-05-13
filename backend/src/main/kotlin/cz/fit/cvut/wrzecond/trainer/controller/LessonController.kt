package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Lesson
import cz.fit.cvut.wrzecond.trainer.service.LessonService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

/**
 * LessonController is responsible for handling HTTP requests related to lessons.
 *
 * @param service Service for handling lesson-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility(findAll = VisibilitySettings.NONE)
@RequestMapping("/lessons")
class LessonController(override val service: LessonService, userService: UserService)
: IControllerImpl<Lesson, LessonFindDTO, LessonGetDTO, LessonCreateDTO, LessonUpdateDTO>(service, userService) {

    /**
     * Retrieves current lessons for current user
     *
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of LessonGetDTOs.
     */
    @GetMapping("/current")
    fun currentLessons(request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) {user ->
        setCookie(loginSecret, response)
        service.currentLessons(user)
    }

    /**
     * Retrieves the details for the week in which the specified lesson takes place.
     *
     * @param id The ID of the lesson for which the week's details are to be fetched.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A WeekDetailDTO object.
     */
    @GetMapping("/{id}/week")
    fun lessonWeekDetail(@PathVariable id: Int,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.lessonWeekDetail(id, user) }

    /**
     * Retrieves teacher-specific lesson data by lesson ID.
     *
     * @param id The ID of the lesson to retrieve.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A lessonGetDTO object.
     */
    @GetMapping("/{id}/editData")
    fun teacherGetByID(@PathVariable id: Int,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.teacherGetByID(id, user) }

    /**
     * Unlocks lesson modules.
     *
     * @param id The ID of the lesson whose modules are to be unlocked.
     * @param unlockDto Data transfer object containing the unlock code.
     * @param request The HttpServletRequest object containing request details.
     * @param response The HttpServletResponse object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @PostMapping("/{id}/code")
    fun unlockLessonModules(@PathVariable id: Int, @RequestBody unlockDto: LessonUnlockDTO,
                            request: HttpServletRequest, response: HttpServletResponse,
                            @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.unlockLessonModules(id, unlockDto, user) }

    /**
     * Clones a lesson from one course to another.
     *
     * @param id The ID of the lesson to be cloned.
     * @param courseId The ID of the course into which the lesson will be cloned.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A LessonGetDTO object.
     */
    @PostMapping("/{id}/courses/{courseId}")
    fun cloneLesson(@PathVariable id: Int, @PathVariable courseId: Int,
                    request: HttpServletRequest, response: HttpServletResponse,
                    @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.cloneLesson(id, courseId, user) }

}
