package cz.fit.cvut.wrzecond.trainer.service.helper

import com.google.gson.Gson
import cz.fit.cvut.wrzecond.trainer.dto.AuthDTO
import cz.fit.cvut.wrzecond.trainer.dto.UserLoginDTO
import cz.fit.cvut.wrzecond.trainer.entity.RoleLevel
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.service.IServiceBase
import cz.fit.cvut.wrzecond.trainer.service.SubjectService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

/**
 * Service class responsible for authenticating users using FIT OAuth.
 *
 * @property repository A repository for accessing user data.
 * @property networkService A service for handling network requests related to FIT OAuth.
 * @property subjectService A service for accessing subject data.
 * @property environment A Spring Environment object for accessing environment properties.
 */
@Service
class AuthService(
    override val repository: UserRepository,
    private val networkService: NetworkService,
    private val subjectService: SubjectService,
    private val environment: Environment
) : IServiceBase<User>(repository, repository) {

    /** Environment constant with FIT OAuth client ID */
    final val clientId = environment.getProperty("auth.clientId", "")

    /** Environment constant with FIT OAuth redirect URI */
    final val redirectUri = environment.getProperty("auth.redirectUri", "")

    /** Environment constant with FIT OAuth client secret */
    private final val clientSecret = environment.getProperty("auth.clientSecret", "")

    /** Environment constant with default test user id */
    private final val testUsername = environment.getProperty("auth.testUsername", "test01")

    private final val activeProfile = environment.getProperty("spring.profiles.active", "")

    /**
     * Environment constant which indicates if application is in dev version. If value of property is true new
     * users won't be created
     */
    private final val isDevVersion = environment.getProperty("auth.isDevVersion", "false").toBoolean()

    /**
     * This method will try to authenticate given user
     * @param dto dto from FIT OAuth server
     * @return UserLoginDTO containing logged-in user secret on success
     * @throws ResponseStatusException with code 400 BAD_REQUEST on failure
     */
    fun authenticate(dto: AuthDTO) = tryCatch {
        val username = fitAuth(dto.code)
        val user = repository.getByUsername(username) ?: createUser(username)
        if (user.blocked) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "This account is blocked")
        }
        user.loginSecret = UserService.generateSecret()
        repository.saveAndFlush(user)
        UserLoginDTO(user.id, user.username, user.name,
                user.courses.filter { it.role.level == RoleLevel.TEACHER }.map { it.course.id },
                subjectService.findByGuarantor(user.id).map { it.id },
                user.isAdmin, user.gitlabToken ?: "", user.blocked)
    }

    // === PRIVATE HELPERS ===

    /**
     * Authenticates the user using a given authorization code from the FIT OAuth server.
     *
     * @param code The authorization code used for obtaining access and refresh tokens from the FIT OAuth server.
     * @return The username of the authenticated user obtained from the FIT OAuth token information.
     */
    private fun fitAuth (code: String) : String = tryCatch {
        val body  = networkService.fitAuthRequest(code, clientId, clientSecret, redirectUri)
        val token = Gson().fromJson(body, FitToken::class.java)

        val body2 = networkService.fitTokenInfoRequest(token.access_token)
        val decoded = Gson().fromJson(body2, FitTokenInfo::class.java)
        decoded.user_name
    }

    private fun createUser (username: String): User {
        if (isDevVersion) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "New users cannot be created automatically on dev version")
        }
        return repository.saveAndFlush(
            User(
                UserService.generateSecret(), username, username, Timestamp.from(Instant.now()), false,
                emptyList(), emptyList(), emptyList(), emptyList(), emptyList()
            )
        )
    }

    /**
     * Creates a `UserLoginDTO` for a test user defined by the `testUsername` field.
     *
     * This method retrieves the user object associated with the `testUsername` from the `repository`.
     * If the user is found, a `UserLoginDTO` is created and returned. If the user is not found,
     * the method returns null.
     *
     * @return `UserLoginDTO` for the test user or null if the user is not found.
     */
    private fun testUserDto () = repository.getByUsername(testUsername)?.let { user ->
        UserLoginDTO(user.id, user.username, user.name, emptyList(), emptyList(),user.isAdmin, "")
    }

    // === PRIVATE CLASSES ===

    /**
     * Data class representing the FIT OAuth token information.
     *
     * @property access_token The access token issued by the FIT OAuth server.
     * @property token_type The type of the token issued.
     * @property refresh_token The refresh token issued by the FIT OAuth server for obtaining new access tokens.
     * @property expires_in The duration in seconds until the access token expires.
     * @property scope The scope of the access token.
     */
    private data class FitToken (val access_token: String, val token_type: String,
                                 val refresh_token: String, val expires_in: Int,
                                 val scope: String)
    /**
     * Data class representing the information contained in a token returned by the FIT OAuth server.
     *
     * @property aud List of intended audiences for the token.
     * @property exp Expiration time of the token in UNIX timestamp format.
     * @property user_name Username of the authenticated user.
     * @property authorities List of authorities (roles) assigned to the user.
     * @property client_id Client ID associated with the token.
     * @property scope List of scopes assigned to the token.
     */
    private data class FitTokenInfo (val aud: List<String>, val exp: Int, val user_name: String,
                                     val authorities: List<String>, val client_id: String,
                                     val scope: List<String>)

    // === STATIC PROPERTIES ===

    /**
     * Companion object for the AuthService class providing constants for endpoint URLs.
     *
     * This companion object includes endpoint URLs and paths required for interacting with
     * the FIT OAuth server for authentication and token validation purposes.
     */
    companion object {
        const val FIT_ENDPOINT = "https://auth.fit.cvut.cz"
        const val TOKEN_PATH = "/oauth/oauth/token"
        const val TOKEN_INFO_PATH = "/oauth/check_token"
    }
}