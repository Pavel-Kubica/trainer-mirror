package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.TemplateCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.dto.TemplateFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.TemplateUpdateDTO
import cz.fit.cvut.wrzecond.trainer.entity.Template
import cz.fit.cvut.wrzecond.trainer.repository.LogRepository
import cz.fit.cvut.wrzecond.trainer.repository.TemplateRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.repository.code.CodeModuleTestRepository
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class for managing templates.
 *
 * @property repository Repository for managing Template entities.
 * @property logRepository Repository for managing Log entities.
 * @property logService Service for managing Log entities.
 * @property cmtRepository Repository for managing CodeModuleTest entities.
 * @property authorizationService Service for handling authorization logic.
 * @property userRepository Repository for managing User entities.
 */
@Service
class TemplateService(
    override val repository: TemplateRepository,
    private val logRepository: LogRepository,
    private val logService: LogService,
    private val cmtRepository: CodeModuleTestRepository,
    private val authorizationService: AuthorizationService,
    userRepository: UserRepository
) : IServiceImpl<Template, TemplateFindDTO, TemplateFindDTO, TemplateCreateDTO, TemplateUpdateDTO>(
    repository,
    userRepository
) {

    /**
     * Retrieves all Teplates from the repository..
     *
     * @param userDto Details of the user who is making the request. If the user is not trusted,
     *                a `ResponseStatusException` with `HttpStatus.FORBIDDEN` is thrown.
     * @return A list of DTOs for all entities in the repository.
     * @throws ResponseStatusException if the user is not trusted.
     */
    override fun findAll(userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        repository.findAll().map { converter.toFindDTO(it) }
    }

    /**
     * Retrieves a template by its unique identifier.
     *
     * @param id The unique identifier of the entity to retrieve.
     * @param userDto The user authentication details used to check access permissions.
     * @return The entity, converted to a DTO format, if the user has the necessary access rights.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?) =
        checkViewAccess(id, userDto) { template, _ -> converter.toFindDTO(template) }

    /**
     * Creates a new template based on the provided TemplateCreateDTO and associated user details.
     *
     * @param dto The data transfer object containing the template details.
     * @param userDto The data transfer object containing the authenticated user details.
     * @return A created template as a DTO.
     */
    override fun create(dto: TemplateCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val template = repository.saveAndFlush(converter.toEntity(dto, user))
        cmtRepository.saveAllAndFlush(dto.tests.map { converter.toEntity(it, template) })
        logService.log(userDto, template, "create")
        converter.toFindDTO(template)
    }

    /**
     * Updates an existing template with provided data and user authentication details.
     *
     * @param id Identifier of the template to be updated.
     * @param dto Data transfer object containing updated template details.
     * @param userDto Data transfer object of the authenticated user performing the update.
     * @return An updated template as a DTO.
     */
    override fun update(id: Int, dto: TemplateUpdateDTO, userDto: UserAuthenticateDto?) =
        checkEditAccess(id, userDto) { template, _ ->
            checkEditAccess(converter.merge(template, dto), userDto) { templateUpdate, _ ->
                val editedTemplate = repository.saveAndFlush(templateUpdate)
                dto.tests?.let { tests -> cmtRepository.saveAllAndFlush(tests.map { converter.toEntity(it, editedTemplate) })}
                logService.log(userDto, templateUpdate, "update")
                converter.toFindDTO(templateUpdate)
            }
        }

    /**
     * Deletes a template by the given ID.
     *
     * @param id The ID of the entity to be deleted.
     * @param userDto The user authentication details used for access control.
     */
    override fun delete(id: Int, userDto: UserAuthenticateDto?) =
        checkEditAccess(repository.getReferenceById(id), userDto) { entity, _ -> repository.delete(entity) }

    fun deleteTest(cmtId: Int, userDto: UserAuthenticateDto?) = tryCatch {
        cmtRepository.getReferenceById(cmtId).run {
            if (template != null) {
                checkEditAccess(template, userDto) { _, _ -> cmtRepository.delete(this) }
            }else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
        }
    }
}