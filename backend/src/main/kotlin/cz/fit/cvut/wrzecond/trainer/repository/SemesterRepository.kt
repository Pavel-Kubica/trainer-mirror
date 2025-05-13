package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Semester
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Semesters entities.
 */
@Repository
interface SemesterRepository: IRepository<Semester> {

    /**
     * Retrieves a semester entity based on the provided code.
     *
     * @param code The code identifier of the semester to retrieve.
     * @return The Semester entity associated with the given code, or null if no such semester exists.
     */
    fun getSemesterByCode(code: String): Semester?
}