package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Course
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Courses entities.
 */
@Repository
interface CourseRepository: IRepository<Course> {

    /**
     * Retrieves a Course entity based on a given secret.
     *
     * @param secret The secret associated with the Course.
     * @return The Course entity that matches the given secret.
     */
    @Query("SELECT c FROM Course c WHERE c.secret = :secret")
    fun getBySecret(secret: String) : Course

}
