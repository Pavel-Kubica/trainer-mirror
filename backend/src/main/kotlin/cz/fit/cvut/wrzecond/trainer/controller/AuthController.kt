package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.AuthDTO
import cz.fit.cvut.wrzecond.trainer.dto.AuthSettingsDTO
import cz.fit.cvut.wrzecond.trainer.dto.UserLoginDTO
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * AuthController is responsible for handling HTTP requests related to authentication.
 *
 * @param authService Service for handling authentication-related operations.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = ["http://localhost:8080","https://trainer.ksi.fit.cvut.cz","https://dev.trainer.ksi.fit.cvut.cz"], allowCredentials = "true")
class AuthController(
    private val authService: AuthService
) {

    /**
     * Retrieve authentication settings.
     *
     * @return An instance of AuthSettingsDTO containing the client ID and redirect URI for FIT OAuth.
     */
    @GetMapping("/settings")
    fun getSettings() = AuthSettingsDTO(authService.clientId, authService.redirectUri)

    /**
     * Authenticates a user using the provided authentication data (AuthDTO) and sets a
     * login cookie before returning the authenticated user information.
     *
     * @param dto DTO containing the authentication code received from FIT OAuth.
     * @param response The HttpServletResponse object used to set the login cookie.
     * @return An instance of UserLoginDTO containing information about the authenticated user.
     */
    @PostMapping
    fun authenticate (@RequestBody dto: AuthDTO, response: HttpServletResponse) : UserLoginDTO {
        val userLoginDTO = authService.authenticate(dto)
        authService.repository.getByUsername(userLoginDTO.username)?.let {
            setCookie(it.loginSecret, response)
            it.testMode = false
            authService.repository.saveAndFlush(it)
        }
        return userLoginDTO
    }
}