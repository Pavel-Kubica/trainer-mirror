package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.TeacherNote
import cz.fit.cvut.wrzecond.trainer.repository.ModuleRepository
import cz.fit.cvut.wrzecond.trainer.repository.TeacherNoteRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class for managing teacher notes.
 *
 * @property repository Repository for managing TeacherNote entities.
 * @property authorizationService Service for handling authorization logic.
 * @property userRepository Repository for managing User entities.
 * @property moduleRepository Repository for managing Module entities.
 */
@Service
class TeacherNotesService(
    override val repository: TeacherNoteRepository,
    private val authorizationService: AuthorizationService,
    private val userRepository: UserRepository,
    private val moduleRepository: ModuleRepository,
) : IServiceImpl<TeacherNote, TeacherNoteFindDTO, TeacherNoteFindDTO, TeacherNoteCreateDTO, TeacherNoteUpdateDTO>(
    repository,
    userRepository
) {

    /**
     * Deletes a note.
     *
     * @param id The ID of the note to be deleted.
     * @param userDto The DTO containing authentication information of the user attempting to delete the note.
     * @throws ResponseStatusException If the user is not authorized to delete the note.
     */
    @Transactional
    override fun delete(id: Int, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { note, _ ->
        val user = getUser(userDto)
        if (note.author.id == user.id)
            repository.deleteById(id)
        else
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }

    /**
     * Creates a teacher note associated with a specified module.
     *
     * @param moduleId ID of the module to associate the teacher note with.
     * @param dto Data Transfer Object containing the details of the teacher note to be created.
     * @param userDto Data Transfer Object containing the details of the authenticated user.
     * @return A created note as DTO.
     */
    fun create(moduleId: Int, dto: TeacherNoteCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val module = moduleRepository.getReferenceById(moduleId)
        val teacherNote = repository.save(converter.toEntity(dto, module, user))
        converter.toFindDTO(teacherNote)
    }

    /**
     * Updates an existing teacher note with the given update data.
     * If the user is the author of the note, merges the given update data into the note and saves it.
     * Throws a forbidden status exception if the user is not the author.
     *
     * @param id the ID of the teacher note to be updated
     * @param dto the data transfer object containing the update information for the note
     * @param userDto the data transfer object containing the authentication information for the current user
     * @return An updated note as DTO.
     * @throws ResponseStatusException if the user is not the author of the note
     */
    @Transactional
    override fun update(id: Int, dto: TeacherNoteUpdateDTO, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { note, _ ->
        val user = getUser(userDto)
        if (note.author.id == user.id)
            converter.toFindDTO(repository.saveAndFlush(converter.merge(note, dto)), user)
        else
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
    }
}