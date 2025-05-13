package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Log
import cz.fit.cvut.wrzecond.trainer.repository.LogRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.springframework.beans.factory.annotation.Value
import cz.fit.cvut.wrzecond.trainer.entity.IEntity

/**
 * Service class for managing log operations.
 *
 * @property repository The repository interface for handling Log entities.
 * @property userRepository The repository interface for handling User entities.
 */
@Service
class LogService (  override val repository: LogRepository,
                    userRepository: UserRepository,
                    @Value("\${logging.enabled:false}") private val loggingEnabled: Boolean)
    : IServiceImpl<Log, LogFindDTO, IGetDTO, ICreateDTO, IUpdateDTO>(repository, userRepository){

    /**
     * Retrieves all log entries.
     *
     * @param userDto Information used to authenticate and retrieve the user.
     * @return A list of LogFindDTO objects representing all log entries.
     * @throws ResponseStatusException if the user is not an admin.
     */
    override fun findAll(userDto: UserAuthenticateDto?): List<LogFindDTO> {
        val user = getUser(userDto)
        if (user.isAdmin)
            return repository.findAll().map {converter.toFindDTO(it)}
        throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    /**
     * Deletes all records from the repository where the timestamp is less than the specified date.
     *
     * @param userDto the data transfer object containing user authentication details.
     * @param date the threshold date in the format 'yyyy-MM-dd'. All records with a timestamp less than this date will be deleted.
     *
     * @throws ResponseStatusException if the user is not an admin or if the date format is invalid.
     */
    @Transactional
    fun delete(userDto: UserAuthenticateDto?, date: String) {
        val user = getUser(userDto)
        if (user.isAdmin) {
            val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            val timestamp: Timestamp
            try {
                val localDate = LocalDate.from(formatter.parse(date))
                timestamp = Timestamp.valueOf(localDate.atTime(0, 0))
            }
            catch (e: Exception) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
            return repository.deleteAllByTimestampIsLessThan(timestamp)
        }
        throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    /**
     * Records a log entry only if loggingEnabled == true.
     * @param userDto logged-in user
     * @param target any entity implementing IEntity
     * @param action action, e.g., "create", "update", ...
     */
    fun log(userDto: UserAuthenticateDto?, target: IEntity, action: String) {
        if (!loggingEnabled) return

        val logEntry = createLogEntry(userDto, target, action)
        repository.saveAndFlush(logEntry)
    }
}