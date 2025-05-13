package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Subject
import cz.fit.cvut.wrzecond.trainer.entity.SubjectGuarantor
import cz.fit.cvut.wrzecond.trainer.repository.LogRepository
import cz.fit.cvut.wrzecond.trainer.repository.SubjectGuarantorRepository
import cz.fit.cvut.wrzecond.trainer.repository.SubjectRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class for handling operations related to Subjects.
 *
 * @param repository The repository for handling subject data operations.
 * @param userRepository The repository for handling user data operations.
 * @param subjectGuarantorRepository The repository for handling subject guarantor data operations.
 * @param logRepository The repository for handling log data operations.
 * @param logService The service for handling log data operations.
 */
@Service
class SubjectService (override val repository: SubjectRepository, private val userRepository: UserRepository,
                      private val subjectGuarantorRepository: SubjectGuarantorRepository,
                      private val logRepository: LogRepository,
                      private val logService: LogService)
    : IServiceImpl<Subject, SubjectFindDTO, SubjectGetDTO, SubjectCreateDTO, SubjectUpdateDTO>(repository, userRepository) {

    /**
     * Finds all the subjects that the authenticated user is permitted to view, sorted by their code.
     *
     * @param userDto Data transfer object containing user authentication information. It can be `null`.
     * @return A list of subjects converted to the required FindDTO format.
     */
    override fun findAll(userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        repository.findAll(sort)
            .filter { it.canView(user) }
            .sortedBy { it.code }
            .map { converter.toFindDTO(it) }
    }

    /**
     * Retrieves a subject by its unique identifier.
     *
     * @param id The unique identifier of the entity to be retrieved.
     * @param userDto The authenticated user data transfer object containing user information.
     * @retyrn Fetched subject as DTO.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(id, userDto) { subject, user ->
            converter.toGetDTO(subject, user)
        }

    /**
     * Find all subjects, where user is guarantor
     * @param userId user identifier
     * @return list of subjects, where user is guarantor
     */
    fun findByGuarantor(userId: Int) = tryCatch {
        val user = userRepository.findById(userId)
        if (user.isEmpty)
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        repository.findAll()
            .filter { it.guarantors.any { sg -> sg.guarantor.id == userId } }
            .sortedBy { it.code }
    }

    /**
     * Creates a new subject.
     *
     * @param dto the data transfer object containing the subject information to create.
     * @param userDto the authenticated user data transfer object.
     * @return A created subject as DTO.
     * @throws ResponseStatusException with status FORBIDDEN if the user is not an admin.
     */
    override fun create(dto: SubjectCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!user.isAdmin)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val subject = repository.saveAndFlush(converter.toEntity(dto))
        logService.log(userDto, subject, "create")
        converter.toGetDTO(subject)
    }

    /**
     * Updates the subject and its guarantors based on the given DTO.
     *
     * @param id The ID of the subject to be updated.
     * @param dto The data transfer object containing updated information for the subject.
     * @param userDto Optional parameter representing the authenticated user requesting the update.
     * @return An updated subject as DTO.
     */
    override fun update(id: Int, dto: SubjectUpdateDTO, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { subject, _ ->
        val subjectToUpdate = repository.save(converter.merge(subject, dto))
        val result = converter.toGetDTO(subjectToUpdate)
        logService.log(userDto, subjectToUpdate, "update")
        if (dto.guarantors != null) {
            if (subjectGuarantorRepository.findBySubject(subject).isNotEmpty()) {
                val guarantorsToDelete = subjectGuarantorRepository.findBySubject(subject)
                guarantorsToDelete.forEach { logService.log(userDto, it, "delete") }
                subjectGuarantorRepository.deleteAll(guarantorsToDelete)
                subjectGuarantorRepository.flush()
            }
            val savedGuarantors = subjectGuarantorRepository.saveAllAndFlush(dto.guarantors.map { user ->
                SubjectGuarantor(subject, userRepository.getReferenceById(user.id))
            })
            savedGuarantors.forEach { logService.log(userDto, it, "create") }
        }
        result
        }

    /**
     * Deletes a subject identified by the given id.
     *
     * @param id the unique identifier of the subject to delete
     * @param userDto the user authentication details required for edit permissions
     * @throws ResponseStatusException if the subject contains courses or access is denied
     */
    override fun delete(id: Int, userDto: UserAuthenticateDto?):Unit
        = checkEditAccess(id, userDto) { subject, _ ->
            // if contains courses, cannot delete
            if (subject.courses.isNotEmpty())
                throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY)
            repository.delete(subject)
            logService.log(userDto, subject, "delete")
        }

    /**
     * Retrieves the list of guarantors for a specified subject.
     *
     * @param subjectId The unique identifier of the subject for which guarantors are being retrieved.
     * @param userDto The authenticated user data transfer object, which can be `null`.
     * @return A list of UserFindDTOs representing the guarantors of the specified subject.
     */
    fun getGuarantors(subjectId : Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(subjectId, userDto) { subject, _ ->
            subject.guarantors.map { converter.toFindDTO(it.guarantor) }
    }

    /**
     * Adds a guarantor to a specified subject.
     *
     * @param subjectId The unique identifier of the subject to which the guarantor is to be added.
     * @param userId The unique identifier of the user to be added as a guarantor.
     * @param userDto An optional data transfer object containing the authenticated user information.
     * @throws ResponseStatusException with status NOT_FOUND if the specified user is not found.
     */
    fun addGuarantor(subjectId: Int, userId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(subjectId, userDto) {subject, _ ->
            val guarantor = userRepository.findById(userId)
            if (guarantor.isEmpty)
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val sg = SubjectGuarantor(subject, guarantor.get())
        val createdSG = subjectGuarantorRepository.saveAndFlush(sg)
        logService.log(userDto, createdSG, "create")
        val updatedSubject = repository.saveAndFlush(Subject(subject.name, subject.code, subject.courses,
                    subject.guarantors + sg, subject.questions, subject.id))
        logService.log(userDto, updatedSubject, "update")
        null
    }

    /**
     * Deletes a guarantor from the specified subject.
     *
     * @param subjectId The unique identifier of the subject.
     * @param userId The unique identifier of the user to be deleted as a guarantor.
     * @param userDto Data Transfer Object containing user authentication details. It can be `null`.
     * @throws ResponseStatusException with status NOT_FOUND if the guarantor is not found.
     * @throws ResponseStatusException with status BAD_REQUEST if the guarantor is not associated with the subject.
     */
    fun deleteGuarantor(subjectId: Int, userId: Int, userDto: UserAuthenticateDto?)
            = checkEditAccess(subjectId, userDto) {subject, _ ->
        val guarantor = userRepository.findById(userId)
        if (guarantor.isEmpty)
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val sg = subjectGuarantorRepository.getSubjectGuarantorsBySubjectAndGuarantor(subject, guarantor.get())//SubjectGuarantor(subject, guarantor.get())
        if (sg.isEmpty)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        subjectGuarantorRepository.delete(sg.get())
        logService.log(userDto, sg.get(), "delete")
        val updatedSubject = repository.saveAndFlush(Subject(subject.name, subject.code, subject.courses,
                subject.guarantors - sg.get(), subject.questions, subject.id))
        logService.log(userDto, updatedSubject, "update")
        subjectGuarantorRepository.flush()
    }
}