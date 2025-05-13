package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Lesson
import cz.fit.cvut.wrzecond.trainer.entity.User
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Users entities.
 */
@Repository
interface UserRepository : IRepository<User>, JpaSpecificationExecutor<User> {

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve
     * @return The User object if found, or null if no user with the specified username exists
     */
    @Query("SELECT u FROM User u WHERE u.username = :username")
    fun getByUsername (username: String) : User?

    /**
     * Retrieves a user based on the provided login secret.
     *
     * @param loginSecret The loginSecret of the user to retrieve.
     * @return A User object if the login secret is found; null otherwise.
     */
    @Query("SELECT u FROM User u WHERE u.loginSecret = :loginSecret")
    fun getByLoginSecret (loginSecret: String) : User?

    /**
     * Finds and returns a list of users associated with a given lesson.
     *
     * @param lesson The lesson entity used to filter and find relevant users.
     * @return A list of users who are associated with the specified lesson.
     */
    @Query("SELECT u FROM User u JOIN CourseUser cu ON cu.user = u" +
            " JOIN Course c ON cu.course = c JOIN Week w ON w.course = c JOIN Lesson l ON l.week = w AND l = :lesson")
    fun findByLesson (lesson: Lesson) : List<User>

}
