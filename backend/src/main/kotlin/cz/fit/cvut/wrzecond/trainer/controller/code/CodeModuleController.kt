package cz.fit.cvut.wrzecond.trainer.controller.code

import cz.fit.cvut.wrzecond.trainer.controller.IControllerImpl
import cz.fit.cvut.wrzecond.trainer.controller.Visibility
import cz.fit.cvut.wrzecond.trainer.controller.VisibilitySettings
import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleUpdateDTO
import cz.fit.cvut.wrzecond.trainer.entity.code.CodeModule
import cz.fit.cvut.wrzecond.trainer.service.code.CodeModuleService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


/**
 * CodeModuleController is responsible for handling HTTP requests related to code modules.
 *
 * @param service Service for handling codeModule-related operations..
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility(findAll = VisibilitySettings.NONE)
@RequestMapping("/code")
class CodeModuleController(override val service : CodeModuleService, userService: UserService)
    : IControllerImpl<CodeModule, CodeModuleFindDTO, CodeModuleGetDTO, CodeModuleCreateDTO, CodeModuleUpdateDTO>(service, userService) {

    /**
     * Deletes a code module test by its id.
     *
     * @param id The unique identifier of the code module test to be deleted.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/tests/{id}")
    fun deleteCodeModuleTest(@PathVariable id: Int,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.deleteTest(id, user) }

    /**
     * Deletes a code module file by its id.
     *
     * @param id The unique identifier of the code module file to be deleted.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/files/{id}")
    fun deleteCodeModuleFile(@PathVariable id: Int,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.deleteFile(id, user) }

}
