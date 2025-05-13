package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.TopicCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.TopicFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.entity.Topic
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class for managing topics.
 *
 * @property repository The repository instance for performing CRUD operations on topics.
 * @property authorizationService The service for handling authorization logic.
 * @property logRepository The repository for logging actions performed on topics.
 * @property userRepository The repository for managing user entities.
 */
@Service
class TopicService(
    override val repository: TopicRepository,
    private val authorizationService: AuthorizationService,
    private val logRepository: LogRepository,
    userRepository: UserRepository
) : IServiceImpl<Topic, TopicFindDTO, TopicFindDTO, TopicCreateDTO, TopicCreateDTO>(repository, userRepository) {

    /**
     * Retrieves all Topics from the repository, mapping them to DTOs.
     *
     * @param userDto The data transfer object containing user authentication details. Can be null.
     * @throws ResponseStatusException if the user is not trusted.
     * @return A list of mapped DTOs representing the records from the repository.
     */
    override fun findAll(userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        repository.findAll().map { converter.toFindDTO(it) }
    }

    /**
     * Retrieves a Topic by its unique identifier and checks if the user has view access to it.
     *
     * @param id The unique identifier of the entity to retrieve.
     * @param userDto An optional object containing user authentication details.
     * @return Fetched topic as a DTO.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { topic, _ ->
        converter.toGetDTO(topic)
    }

    /**
     * Creates a new topic based on the provided DTO.
     *
     * @param dto the data transfer object containing the information for the topic to be created
     * @param userDto the data transfer object containing the authentication information of the user performing the operation
     * @return Created topic as a DTO.
     */
    override fun create(dto: TopicCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val topic = repository.saveAndFlush(converter.toEntity(dto))
        logRepository.saveAndFlush(createLogEntry(userDto, topic, "create"))
        converter.toGetDTO(topic)
    }

    /**
     * Updates an existing topic with the provided data.
     *
     * @param id The ID of the topic to be updated.
     * @param dto The data transfer object containing the new topic information.
     * @param userDto The data transfer object containing user authentication information.
     * @return Updated topic as a DTO.
     */
    override fun update(id: Int, dto: TopicCreateDTO, userDto: UserAuthenticateDto?) =
        checkEditAccess(id, userDto) { topic, _ ->
            val topicUpdate = repository.saveAndFlush(converter.merge(topic, dto))
            logRepository.saveAndFlush(createLogEntry(userDto, topicUpdate, "update"))
            converter.toGetDTO(topicUpdate)
        }

    /**
     * Deletes a topic identified by the given ID.
     *
     * @param id the unique identifier of the topic to be deleted.
     * @param userDto an optional parameter representing the authenticated user requesting the deletion.
     */
    override fun delete(id: Int, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { topic, _ ->
        logRepository.saveAndFlush(createLogEntry(userDto, topic, "delete"))
        repository.delete(topic)
    }

}