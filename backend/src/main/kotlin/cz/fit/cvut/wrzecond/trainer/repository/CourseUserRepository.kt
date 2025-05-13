package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Course
import cz.fit.cvut.wrzecond.trainer.entity.CourseUser
import cz.fit.cvut.wrzecond.trainer.entity.RoleLevel
import cz.fit.cvut.wrzecond.trainer.entity.User
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing CourseUsers entities.
 */
@Repository
interface CourseUserRepository: IRepository<CourseUser> {

    /**
     * Retrieves a list of CourseUser entities associated with the specified course.
     *
     * @param course The course entity for which associated CourseUser entities are to be retrieved.
     * @return A list of CourseUser entities associated with the given course.
     */
    @Query("SELECT cu FROM CourseUser cu WHERE cu.course = :course")
    fun findByCourse (course: Course) : List<CourseUser>

    /**
     * Finds and returns all teachers associated with a given course, excluding the author.
     * Only users with the specified role level (defaulting to RoleLevel.TEACHER) are considered.
     *
     * @param course the course for which to find associated teachers
     * @param author the user who is the author of the course and should be excluded from the results
     * @param teacher the role level to filter users by, defaulting to RoleLevel.TEACHER
     * @return a list of users who are teachers for the given course, excluding the author
     */
    @Query("SELECT u FROM CourseUser cu JOIN User u ON cu.user = u" +
            " WHERE cu.course = :course AND cu.role.level = :teacher AND cu.user != :author")
    fun findTeachersByCourse (course: Course, author: User, teacher: RoleLevel = RoleLevel.TEACHER) : List<User>

    /**
     * Retrieves a CourseUser entity based on the provided course and user.
     *
     * @param course the Course entity to filter the CourseUser by.
     * @param user the User entity to filter the CourseUser by.
     * @return the CourseUser entity that matches the provided course and user, or null if no match is found.
     */
    @Query("SELECT cu FROM CourseUser cu WHERE cu.course = :course AND cu.user = :user")
    fun getByCourseUser (course: Course, user: User) : CourseUser?

    /**
     * Retrieves the list of CourseUser records for a given course where the use is a teacher
     *
     * @param course The course for which the records are being retrieved.
     * @param role The role level to filter the records, defaults to TEACHER.
     * @return A list of CourseUser objects that match the specified course.
     */
    @Query("SELECT cu FROM CourseUser cu WHERE cu.course = :course AND cu.role.level = :role")
    fun findRecordsOfTeacherInCourse (course: Course, role: RoleLevel = RoleLevel.TEACHER) : List<CourseUser>
}
