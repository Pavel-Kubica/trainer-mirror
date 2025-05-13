package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Subject
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Subjects entities.
 */
@Repository
interface SubjectRepository : IRepository<Subject> {

    /**
     * Retrieves a Subject entity based on the provided name.
     *
     * @param name The name of the subject to be retrieved.
     * @return The subject matching the given name, or null if none is found.
     */
    fun getSubjectByName(name: String): Subject?
}