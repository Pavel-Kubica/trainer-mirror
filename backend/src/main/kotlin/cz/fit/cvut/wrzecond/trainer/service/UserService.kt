package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.CourseUser
import cz.fit.cvut.wrzecond.trainer.entity.Role
import cz.fit.cvut.wrzecond.trainer.entity.RoleLevel
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.CourseRepository
import cz.fit.cvut.wrzecond.trainer.repository.SandboxUserRepository
import cz.fit.cvut.wrzecond.trainer.repository.SubjectGuarantorRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.service.specification.UserSpecification.Companion.userNameLike
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.random.Random

/**
 * Service class for managing User-related operations.
 *
 * @property repository The repository interface for User entity.
 * @property subjectGuarantorRepository Repository for handling SubjectGuarantor-related operations.
 * @property sandboxUserRepository Repository for handling SandboxUser-related operations.
 */
@Service
class UserService (override val repository: UserRepository, val subjectGuarantorRepository: SubjectGuarantorRepository,
                   val sandboxUserRepository: SandboxUserRepository)
    : IServiceImpl<User, UserFindDTO, UserGetDto, ICreateDTO, IUpdateDTO>(repository, repository) {

    /**
     * Function to get user by login secret
     * @property loginSecret user's login secret
     * @return UserFindDTO in case of success, null otherwise
     */
    fun getByLoginSecret (loginSecret: String) = repository.getByLoginSecret(loginSecret)?.let {
        converter.toFindDTO(it)
    }


    /**
     * Function to find all users.
     * Supports filtering by user's name and pagination.
     * @property name user's name, used in filter
     * @property limit maximal number of users in one page
     * @property offset page's number
     * @return page of users
     */
    fun findAll (name: String?, limit: Int, offset: Int, userDto: UserAuthenticateDto?) : Page<UserFindDTO> {
        val user = getUser(userDto)
        if (!user.isAdmin &&
                !subjectGuarantorRepository.findAll().any { it.guarantor.id == user.id })
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val pageable: Pageable = PageRequest.of(offset, limit)
        if (name != null)
            return repository.findAll(userNameLike(name), pageable).map { converter.toFindDTO(it) }
        return repository.findAll(pageable).map { converter.toFindDTO(it) }
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id The unique identifier of the user to be retrieved.
     * @param userDto An optional data transfer object for authenticating the user.
     * @return A data transfer object containing user information.
     * @throws ResponseStatusException if the requesting user does not have the necessary permissions.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?) : UserGetDto {
        val user: User = getUser(userDto)
        if (user.id == id)
            return converter.toGetDTO(user)
        if (!user.isAdmin && !subjectGuarantorRepository.findAll().any { it.guarantor.id == user.id })
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        return converter.toGetDTO(getEntityByID(id))
    }

    /**
     * Swaps the current user's mode to test mode or vice versa.
     *
     * @param userDto The data transfer object representing the user's authentication details.
     */
    fun swapToTestUser(userDto: UserAuthenticateDto?)  {
        val user: User = getUser(userDto)
        user.testMode = !user.testMode
        repository.saveAndFlush(user)
    }

    /**
     * Retrieves the test mode status for a user.
     *
     * @param id The unique identifier of the user.
     * @return True if the user is in test mode, false otherwise.
     */
    fun getTestMode(id: Int) : Boolean {
        val user = repository.findById(id)
        if (user.isPresent)
            return user.get().testMode
        return false
    }

    fun isTeacher(userDto: UserAuthenticateDto?) : Boolean {
        val user = getUser(userDto)
        return user.isAdmin || subjectGuarantorRepository.findByGuarantor(user).isNotEmpty() || user.courses.any { it.role.level ==  RoleLevel.TEACHER}
    }

    /**
     * Retrieves a sandbox for a specific user.
     *
     * @param id The unique identifier of the user for whom the sandbox is being retrieved.
     * @param userDto An optional data transfer object for authenticating the user.
     * @return A CourseGetDTO object containing sandbox details if found and accessible, null otherwise.
     * @throws ResponseStatusException with a 403 status if the requesting user does not have the necessary permissions.
     */
    fun getSandbox(id:Int, userDto: UserAuthenticateDto?) : CourseGetDTO? {
        val user = getUser(userDto)
        val sandboxUser = sandboxUserRepository.getSandboxUserByUserId(id) ?: return null
        if (user.id != id || !sandboxUser.sandbox.canView(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        return converter.toGetDTO(sandboxUser.sandbox, user)
    }

    fun updateToken(id: Int, userDto: UserAuthenticateDto?, gitlabTokenDto: UserGitlabUpdateDto) : Unit {
        val user = getUser(userDto)
        if (user.id != id)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        user.gitlabToken = gitlabTokenDto.gitlabToken
        repository.saveAndFlush(user)
    }

    /**
     * Companion object for the UserService class.
     *
     * Provides utility functions and constants related to user authentication.
     */
    companion object {
        private const val LOGIN_SECRET_LENGTH = 16
        private val CHAR_POOL = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        /**
         * Generates a random secret string.
         *
         * The secret is generated by constructing a sequence of random characters
         * from a predefined character pool. The length of the secret is determined
         * by the constant `LOGIN_SECRET_LENGTH`.
         *
         * @return A randomly generated secret string.
         */
        fun generateSecret () = (1..LOGIN_SECRET_LENGTH)
            .map { Random.nextInt(0, CHAR_POOL.size) }
            .map(CHAR_POOL::get)
            .joinToString("")
    }
}
