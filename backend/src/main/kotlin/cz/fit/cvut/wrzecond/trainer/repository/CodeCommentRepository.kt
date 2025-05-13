package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.CodeComment
import cz.fit.cvut.wrzecond.trainer.entity.StudentModuleRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing CodeComments entities.
 */
@Repository
interface CodeCommentRepository : IRepository<CodeComment> {

    /**
     * Finds all CodeComment entities associated with a given StudentModuleRequestId.
     *
     * @param moduleRequestId The ID of the student module request.
     * @return A list of CodeComment entities associated with the given StudentModuleRequestId.
     */
    @Query("SELECT cc FROM CodeComment cc WHERE cc.moduleRequest.id = :moduleRequestId")
    fun findByStudentModuleRequestId(moduleRequestId: Int) : List<CodeComment>

    /**
     * Checks if a comment exists for a specific ModuleRequestId, file, and row number.
     *
     * @param moduleRequestId The ID of the student module request.
     * @param fileName The name of the file where the comment is located.
     * @param rowNumber The row number in the file where the comment is located.
     * @return true if the comment exists, false otherwise.
     */
    @Query("SELECT CASE WHEN COUNT(cc) > 0 THEN true ELSE false END " +
            "FROM CodeComment cc " +
            "WHERE cc.moduleRequest.id = :moduleRequestId AND cc.fileName = :fileName AND cc.rowNumber = :rowNumber")
    fun commentExists(moduleRequestId: Int, fileName: String, rowNumber: Int) : Boolean

    /**
     * Deletes CodeComment entities associated with the given student module request.
     *
     * @param smrq The student module request whose associated comments should be deleted.
     */
    fun deleteByModuleRequest(smrq: StudentModuleRequest)
}