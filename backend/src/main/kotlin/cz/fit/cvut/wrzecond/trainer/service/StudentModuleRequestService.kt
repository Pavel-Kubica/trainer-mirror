package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.CodeCommentEditDTO
import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.entity.StudentModuleRequest
import cz.fit.cvut.wrzecond.trainer.repository.CodeCommentRepository
import cz.fit.cvut.wrzecond.trainer.repository.LogRepository
import cz.fit.cvut.wrzecond.trainer.repository.StudentModuleRequestRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException


/**
 * Service class responsible for handling student module requests.
 *
 * @property repository Repository for managing StudentModuleRequests entities.
 * @property codeCommentRepository Repository for managing CodeComments entities.
 * @property logRepository Repository for managing Logs entities.
 * @property userRepository Repository for managing User entities.
 */
@Service
class StudentModuleRequestService (override val repository: StudentModuleRequestRepository,
                                   private val codeCommentRepository: CodeCommentRepository,
                                   private val logRepository: LogRepository,
                                   userRepository: UserRepository)
: IServiceBase<StudentModuleRequest>(repository, userRepository) {

    /**
     * Get code comments for a given student module request
     *
     * @param smrId identifier of the student module request
     * @param userDto user performing the request
     * @return list of code comments
     */
    fun getCodeComments(smrId: Int, userDto: UserAuthenticateDto?)
            = checkViewAccess(smrId, userDto) { smr, _ ->
        codeCommentRepository.findByStudentModuleRequestId(smr.id).map { converter.toFindDTO(it) }
    }

    /**
     * Creates a new code comment for a specified student module request.
     *
     * @param smrId The ID of the student module request.
     * @param dto The Data Transfer Object containing information about the new comment.
     * @param userDto The Data Transfer Object representing the authenticated user, or null if the request is unauthenticated.
     * @throws ResponseStatusException if a comment already exists on the specified line.
     * @return The created code comment as a Data Transfer Object.
     */
    fun createCodeComment(smrId: Int, dto: CodeCommentEditDTO, userDto: UserAuthenticateDto?)
            = checkEditAccess(smrId, userDto) { smr, _ ->
        if (codeCommentRepository.commentExists(smr.id, dto.fileName, dto.rowNumber))
            throw ResponseStatusException(HttpStatus.CONFLICT, "Comment on this line already exists")
        val codeComment = codeCommentRepository.saveAndFlush(converter.toEntity(dto, smr))
        logRepository.saveAndFlush(createLogEntry(userDto, codeComment, "creatw"))
        converter.toFindDTO(codeComment)
    }

}