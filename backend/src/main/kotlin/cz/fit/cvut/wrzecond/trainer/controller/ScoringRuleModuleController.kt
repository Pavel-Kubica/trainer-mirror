package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.service.*
import cz.fit.cvut.wrzecond.trainer.dto.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/scoringRules/{id}/modules")
class ScoringRuleModuleController(private val service: ScoringRuleModuleService, userService: UserService)
    : IControllerAuth(userService) {

    @PutMapping("/{lessonModuleId}")
    fun createScoringRuleModule(@PathVariable id: Int, @PathVariable lessonModuleId: Int,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.putScoringRuleModule(id, lessonModuleId, user) }


    @DeleteMapping("/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLessonModule(@PathVariable id: Int, @PathVariable moduleId: Int,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.delScoringRuleModule(id, moduleId, user)}


}