package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException


/**
 * Service class responsible for handling operations related to StudentRating entities.
 *
 * @param repository The repository for StudentRating entities.
 * @param moduleRepository The repository for Module entities.
 * @param userRepository The repository for User entities.
 */
@Service
class StudentRatingService(
    override val repository: StudentRatingRepository,
    private val moduleRepository: ModuleRepository,
    userRepository: UserRepository,
) : IServiceImpl<StudentRating,StudentRatingFindDTO, StudentRatingGetDTO, StudentRatingCreateDTO, StudentRatingUpdateDTO>(repository, userRepository)
{

    /**
     * Creates a new student rating entity based on the provided DTO.
     *
     * @param dto a data transfer object containing the details of the student rating to create
     * @param userDto an optional data transfer object containing user authentication information
     * @return the created student rating entity converted back to a DTO
     */
    override fun create(dto: StudentRatingCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val result = converter.toEntity(dto)
        val result1 = repository.saveAndFlush(result)
        converter.toGetDTO(result1)
    }


    /**
     * Updates the student rating identified by the given id with the information
     * provided in the StudentRatingUpdateDTO object.
     *
     * @param id the unique identifier of the student rating to be updated.
     * @param dto the data transfer object containing the new values for the student rating.
     * @param userDto the data transfer object containing authentication information for the user making the request. Can be null.
     * @throws ResponseStatusException with HttpStatus.FORBIDDEN if the user does not have permission to edit the student rating.
     * @return the updated student rating as a data transfer object.
     */
    override fun update(id: Int, dto: StudentRatingUpdateDTO, userDto: UserAuthenticateDto?) =
        tryCatch {
            if (!repository.getReferenceById(id).canEdit(getUser(userDto))) throw ResponseStatusException(HttpStatus.FORBIDDEN)
            val result = converter.toGetDTO(repository.saveAndFlush(converter.merge(repository.getReferenceById(id),dto)))
            result
        }

    /**
     * Fetches and returns a student rating by the provided ID.
     *
     * @param id The unique identifier of the student rating to be retrieved.
     * @param userDto The user details for authentication and authorization checks, can be null.
     * @return Fetched StudentRating as DTO.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto){ studentRating, _ ->
        converter.toGetDTO(studentRating)
    }

    /**
     * Deletes a student rating identified by the provided ID.
     *
     * @param id The ID of the student rating to be deleted.
     * @param userDto The user authentication details necessary to verify edit access.
     */
    override fun delete(id: Int, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto){ studentRating, _ ->
        repository.delete(studentRating)
    }

    /**
     * Retrieves a student rating based on the user and the module ID.
     *
     * @param moduleId The ID of the module for which the student rating is requested.
     * @param userDto The authentication details of the user requesting the student rating.
     * @return An optional StudentRatingGetDTO object if a matching student rating is found.
     */
    fun getByUserAndModule(moduleId: Int, userDto: UserAuthenticateDto?) = checkViewAccess(moduleId, userDto){
            _, _ ->
        val user = getUser(userDto)
        repository.getByUserAndModule(user.id,moduleId)?.let { converter.toGetDTO(it) }
    }
}


