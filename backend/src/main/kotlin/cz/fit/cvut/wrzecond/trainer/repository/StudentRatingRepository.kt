package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.*
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing StudentRatings entities.
 */
@Repository
interface StudentRatingRepository: IRepository<StudentRating>{

    /**
     * Fetches a StudentRating entity based on the provided module ID and student ID.
     *
     * @param module The ID of the module.
     * @param student The ID of the student.
     * @return The StudentRating entity if found, or null if no matching entity exists.
     */
    @Query("SELECT sr FROM StudentRating sr WHERE sr.module.id =:module AND sr.student.id =:student")
    fun getByUserAndModule (module: Int, student: Int) : StudentRating?

    /**
     * Retrieves a list of StudentRating entities for a given module ID.
     *
     * @param module The ID of the module.
     * @return A list of StudentRating entities associated with the specified module.
     */
    @Query("SELECT sr FROM StudentRating sr WHERE sr.module.id = :module")
    fun getByModule (module: Int) : List<StudentRating>
}
