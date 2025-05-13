package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.dto.UserFindDTO
import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.Log
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.sentry.Sentry
import org.apache.catalina.connector.ClientAbortException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp

/**
 * Abstract class containing base helper methods
 */
abstract class IServiceBase<T: IEntity> (open val repository: IRepository<T>, private val userRepository: UserRepository) {

    @Autowired
    protected lateinit var converter: ConverterService
    @Autowired
    protected lateinit var fileService: FileService

    /** Default sort */
    protected val sort: Sort
        get () = Sort.unsorted()

    /**
     * Helper method for getting User object from UserReadDTO
     * @throws ResponseStatusException with code 401 when unauthenticated
     * @throws ResponseStatusException with code 403 when unauthorized
     */
    protected fun getUser (dto: UserFindDTO?) =
        try { dto?.let { userRepository.getByUsername(it.username) } ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED) }
        catch (_: JpaObjectRetrievalFailureException) { throw ResponseStatusException(HttpStatus.UNAUTHORIZED) }
        catch (_: EmptyResultDataAccessException)     { throw ResponseStatusException(HttpStatus.UNAUTHORIZED) }

    /**
     * Helper method for getting User object from UserAuthenticateDto
     * @throws ResponseStatusException with code 401 when unauthenticated
     * @throws ResponseStatusException with code 403 when unauthorized
     */
    protected fun getUser (dto: UserAuthenticateDto?) =
            try { dto?.let { userRepository.getByUsername(it.username) } ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED) }
            catch (_: JpaObjectRetrievalFailureException) { throw ResponseStatusException(HttpStatus.UNAUTHORIZED) }
            catch (_: EmptyResultDataAccessException)     { throw ResponseStatusException(HttpStatus.UNAUTHORIZED) }

    /**
     * Helper template function allowing to perform repository action,
     * catch any exception and change it to ResponseStatusException
     * @param block action to perform
     * @return block return value
     */
    protected fun <X> tryCatch (block: IRepository<T>.() -> X) =
        try { repository.block() }
        catch (_: JpaObjectRetrievalFailureException) { throw ResponseStatusException(HttpStatus.NOT_FOUND) }
        catch (_: EmptyResultDataAccessException)     { throw ResponseStatusException(HttpStatus.NOT_FOUND) }
        catch (_: NoSuchFileException)                { throw ResponseStatusException(HttpStatus.NOT_FOUND) }
        catch (_: DataIntegrityViolationException)    { throw ResponseStatusException(HttpStatus.BAD_REQUEST) }
        catch (_: FileAlreadyExistsException)         { throw ResponseStatusException(HttpStatus.CONFLICT) }
        catch (_: ClientAbortException)               { throw ResponseStatusException(HttpStatus.BAD_REQUEST) } // ignore, cannot do anything about this
        catch (e: ResponseStatusException)            { throw e } // rethrow ResponseStatusException
        catch (xcc: Exception)                        { println(xcc); Sentry.captureException(xcc); throw ResponseStatusException(HttpStatus.BAD_REQUEST) }

    /**
     *  Helper method to get entity with given ID
     *  @param id searched entity ID
     *  @throws ResponseStatusException with corresponding code in case of failure
     *  @return searched Entity (T)
     */
    protected fun getEntityByID (id: Int) = tryCatch { repository.getReferenceById(id) }

    /**
     * Helper method throwing unauthorized/forbidden response for view
     * @param entity to check the access to
     * @param dto user to check the access with
     * @param block action to execute when check is successful
     * @return result of the called block
     */
    protected fun <X> checkViewAccess (entity: T, dto: UserAuthenticateDto?, block: (T, User) -> X) = tryCatch {
        val user = getUser(dto)
        if (!entity.canView(user)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        block(entity, user)
    }
    /**
     * Helper method throwing unauthorized/forbidden response for view
     * @param id ID of user to check the access to
     * @param dto user to check the access with
     * @param block action to execute when check is successful
     * @return result of the called block
     */
    protected fun <X> checkViewAccess (id: Int, dto: UserAuthenticateDto?, block: (T, User) -> X)
        = checkViewAccess(getEntityByID(id), dto, block)

    /**
     * Helper method throwing unauthorized/forbidden response for edit
     * @param entity to check the access to
     * @param dto user to check the access with
     * @param block action to execute when check is successful
     * @return result of the called block
     */
    protected fun <X> checkEditAccess (entity: T, dto: UserAuthenticateDto?, block: (T, User) -> X) = tryCatch {
        val user = getUser(dto)
        if (!entity.canEdit(user)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        block(entity, user)
    }
    /**
     * Helper method throwing unauthorized/forbidden response for edit
     * @param id ID of user to check the access to
     * @param dto user to check the access with
     * @param block action to execute when check is successful
     * @return result of the called block
     */
    protected fun <X> checkEditAccess (id: Int, dto: UserAuthenticateDto?, block: (T, User) -> X)
        = checkEditAccess(getEntityByID(id), dto, block)

    /**
     * Creates a log entry for an operation performed by a user on an entity.
     *
     * @param userDto Data Transfer Object containing user authentication details. Can be null.
     * @param entity The entity on which the operation is performed.
     * @param operation A string representing the operation performed on the entity.
     * @return A created log entry.
     */
    protected fun createLogEntry(userDto: UserAuthenticateDto?, entity: IEntity, operation: String)
            = Log(userDto?.username ?: "",
            userDto?.ipAddress ?: "",
            userDto?.client ?: "",
            Timestamp(System.currentTimeMillis()),
            entity.javaClass.kotlin.simpleName ?: "",
            entity.id,
            operation)
}
