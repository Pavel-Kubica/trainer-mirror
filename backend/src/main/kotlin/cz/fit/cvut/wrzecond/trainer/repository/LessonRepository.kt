package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Lesson
import cz.fit.cvut.wrzecond.trainer.entity.User
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.sql.Timestamp

/**
 * Repository interface for managing Lessons entities.
 */
@Repository
interface LessonRepository: IRepository<Lesson> {

    /**
     * Finds the lessons of a given user that are happening at a specific time.
     *
     * @param user The user whose lessons need to be found.
     * @param time The time at which the lessons need to be checked.
     * @return A list of lessons that the user is attending at the specified time.
     */
    @Query("SELECT l FROM Lesson l " +
            "JOIN CourseUser cu ON cu.course = l.week.course " +
            "WHERE (:time BETWEEN l.timeStart AND l.timeEnd) AND cu.user = :user")
    fun findUserLessonsAtTime(user: User, time: Timestamp) : List<Lesson>

}
