package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Course
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.entity.Week
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Weeks entities.
 */
@Repository
interface WeekRepository: IRepository<Week> {
    /**
     * Retrieves all Weeks that are contained in courses that this user teaches
     *
     * @param user The user that would be teaching the courses containing the weeks
     * @return List of Week entities contained in courses taught by this user
     */
    @Query("""
            SELECT w FROM Week w 
            JOIN w.course c
            JOIN CourseUser cu ON cu.course = c
            WHERE cu.user = :user AND cu.role.level = cz.fit.cvut.wrzecond.trainer.entity.RoleLevel.TEACHER
            ORDER BY w.from DESC 
            """)
    fun findAllTaught(user: User) : List<Week>
}
