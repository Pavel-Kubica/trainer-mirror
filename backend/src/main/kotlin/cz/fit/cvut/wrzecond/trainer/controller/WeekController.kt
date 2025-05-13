package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Week
import cz.fit.cvut.wrzecond.trainer.service.UserService
import cz.fit.cvut.wrzecond.trainer.service.WeekService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * WeekController is responsible for handling HTTP requests related to weeks.
 *
 * @param service Service for handling week-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/weeks")
class WeekController(override val service: WeekService, userService: UserService)
: IControllerImpl<Week, WeekFindDTO, WeekGetDTO, WeekCreateDTO, WeekUpdateDTO>(service, userService) {

    /**
     * Updates the order of lessons within a week.
     *
     * @param id The ID of the week to update the order of lessons in.
     * @param dto DTO containing the IDs of lessons in their new order.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{id}/lessons")
    fun editLessonOrder(@PathVariable id: Int, @RequestBody dto: WeekLessonOrderDTO,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.editLessonOrder(id, dto, user) }

    /**
     * Clones an existing week, including all its lessons and modules, from one course to another.
     *
     * @param id The ID of the week to be copied.
     * @param courseId The ID of the course to copy the week into.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A GetWeekDTO object.
     */
    @PostMapping("/{id}/courses/{courseId}")
    fun cloneWeek(@PathVariable id: Int, @PathVariable courseId: Int,
                  request: HttpServletRequest, response: HttpServletResponse,
                  @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
                setCookie(loginSecret, response)
                service.cloneWeek(id, courseId, user) }

}
