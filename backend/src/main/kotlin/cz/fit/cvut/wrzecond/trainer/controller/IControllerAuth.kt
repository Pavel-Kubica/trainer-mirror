package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.dto.UserFindDTO
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.server.ResponseStatusException
import kotlin.reflect.full.findAnnotation

/**
 * Abstract class IControllerAuth responsible for handling authentication using UserService.
 *
 * @property userService Service for handling user-related operations.
 */
@CrossOrigin(origins = ["http://localhost:8080","https://trainer.ksi.fit.cvut.cz","https://dev.trainer.ksi.fit.cvut.cz"], allowCredentials = "true")
abstract class IControllerAuth (private val userService: UserService) {

    // === AUTHENTICATION ===

    /**
     * Authenticates the user based on the provided HTTP request and login secret.
     *
     * @param A The type of the result returned by the action.
     * @param request The HTTP request object that contains request details.
     * @param function A function that takes a `Visibility` annotation and returns `VisibilitySettings`.
     * @param loginSecret The secret key used for user authentication.
     * @param action The action to be executed with the authenticated user details.
     * @return The result returned by the action.
     */
    protected fun <A> authenticate (request: HttpServletRequest, function: (Visibility) -> VisibilitySettings, loginSecret: String, action: (UserAuthenticateDto?) -> A) : A {
        val annotation = this::class.findAnnotation<Visibility>()
        return authenticate(request, function(annotation!!), loginSecret, action)
    }


    /**
     * Authenticates the user based on the provided HTTP request and login secret.
     *
     * @param A The type of the result returned by the action.
     * @param request The HTTP request object that contains request details.
     * @param settings The visibility settings that determine the accessibility of the resource.
     * @param loginSecret The secret key used for user authentication.
     * @param action The action to be executed with the authenticated user details.
     * @return The result returned by the action.
     */
    protected fun <A> authenticate (request: HttpServletRequest, settings: VisibilitySettings, loginSecret: String, action: (UserAuthenticateDto?) -> A) : A {
        // Try to load user
        val dto = userService.getByLoginSecret(loginSecret)

        when (settings) {
            VisibilitySettings.ALL    -> {}
            VisibilitySettings.LOGGED -> if (dto == null) throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
            VisibilitySettings.NONE   -> throw ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED)
        }

        if (dto != null && userService.getTestMode(dto.id)) {
            val testUser = userService.repository.getByUsername("test01")
            if (testUser != null)
                return action(UserAuthenticateDto(testUser.id, testUser.username, request.getHeader("user-agent"), request.remoteAddr))
        }
        // Perform requested action
        return action(dto?.let { UserAuthenticateDto(it.id, it.username, request.getHeader("user-agent"), request.remoteAddr) })
    }

    // === AUTHENTICATION ===

}

// Helper auth annotation class
annotation class Visibility (
        val findAll: VisibilitySettings = VisibilitySettings.LOGGED,
        val getByID: VisibilitySettings = VisibilitySettings.LOGGED,
        val create : VisibilitySettings = VisibilitySettings.LOGGED,
        val delete : VisibilitySettings = VisibilitySettings.LOGGED,
        val update : VisibilitySettings = VisibilitySettings.LOGGED
)
enum class VisibilitySettings { NONE, LOGGED, ALL }
