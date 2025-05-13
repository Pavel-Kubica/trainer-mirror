package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.service.*
import cz.fit.cvut.wrzecond.trainer.dto.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*


@RestController
@Visibility
@RequestMapping("/scoringRules")
class ScoringRuleController(override val service: ScoringRuleService, userService: UserService)
    : IControllerImpl<ScoringRule, ScoringRuleFindDTO, ScoringRuleGetDTO, ScoringRuleCreateDTO, ScoringRuleUpdateDTO>(service, userService) {


    @GetMapping("/lessons")
    fun getModulesBySubjects(@RequestParam lessonIds: List<Int>,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findByLessons(lessonIds, user) }

    @GetMapping("/{id}/users/{userId}")
    fun getScoringRuleUser(@PathVariable id: Int, @PathVariable userId: Int,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getScoringRuleUser(id, userId, user)
        }

}