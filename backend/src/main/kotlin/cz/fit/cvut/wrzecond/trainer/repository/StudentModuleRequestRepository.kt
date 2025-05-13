package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.*
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing StudentModuleRequests entities.
 */
@Repository
interface StudentModuleRequestRepository: IRepository<StudentModuleRequest> {

    /**
     * Retrieves the most recent unsatisfied or any StudentModuleRequest, based on the specified StudentModule.
     *
     * @param sm The StudentModule associated with the request to be fetched.
     * @param onlyUnsatisfied Boolean indicating whether to fetch only unsatisfied requests.
     * @return the most recent StudentModuleRequest that matches the criteria, or null if no such request exists.
     */
    @Query("SELECT smr FROM StudentModuleRequest smr WHERE smr.studentModule = :sm AND (smr.satisfied = false OR :onlyUnsatisfied = false) ORDER BY smr.requestedOn DESC LIMIT 1")
    fun getByStudentModule(sm: StudentModule, onlyUnsatisfied: Boolean = false) : StudentModuleRequest?

    /**
     * Retrieves the most recent satisfied StudentModuleRequest corresponding to the given StudentModule.
     *
     * @param sm The StudentModule object associated with the request to fetch.
     * @return The most recent satisfied StudentModuleRequest, or null if no such request exists.
     */
    @Query("SELECT smr FROM StudentModuleRequest smr WHERE smr.studentModule = :sm AND smr.satisfied = true ORDER BY smr.satisfiedOn DESC LIMIT 1")
    fun getLastTeacherComment(sm: StudentModule?) : StudentModuleRequest?

    /**
     * Retrieves the oldest unsatisfied or any StudentModuleRequest, based on the specified StudentModule.
     *
     * @param sm The StudentModule associated with the request to be fetched.
     * @param onlyUnsatisfied Boolean indicating whether to fetch only unsatisfied requests.
     * @return The oldest StudentModuleRequest that matches the criteria, or null if no such request exists.
     */
    @Query("SELECT smr FROM StudentModuleRequest smr WHERE smr.studentModule = :sm AND (smr.satisfied = false OR :onlyUnsatisfied = false) ORDER BY smr.requestedOn ASC LIMIT 1")
    fun getStudentRequestText(sm: StudentModule?, onlyUnsatisfied: Boolean = false) : StudentModuleRequest?

    /**
     * Retrieves a list of notifications for a given student. The notifications are based on
     * the student's module requests that have been satisfied. The retrieval can be filtered
     * to fetch all notifications or only those that are more recent than the student's last
     * notification time.
     *
     * @param user The student for whom the notifications are being fetched.
     * @param all A boolean flag indicating whether to fetch all notifications or only those
     *        that are newer than the student's last notification time.
     * @param maxNotifications The maximum number of notifications to retrieve.
     * @return A list of StudentModuleRequest objects representing the notifications.
     */
    @Query("SELECT smr FROM StudentModuleRequest smr WHERE smr.studentModule.student = :user" +
            " AND smr.satisfiedOn IS NOT NULL" +
            " AND (:all = true OR smr.satisfiedOn > smr.studentModule.student.lastNotificationTime)" +
            " ORDER BY smr.satisfiedOn DESC LIMIT :maxNotifications")
    fun findNotificationsStudent(user: User, all: Boolean, maxNotifications: Int) : List<StudentModuleRequest>

    /**
     * Finds notifications for a teacher based on specific criteria.
     *
     * @param user The user whose notifications are being queried.
     * @param all If true, retrieves all notifications; if false, retrieves notifications requested after the user's last notification time.
     * @param maxNotifications The maximum number of notifications to retrieve.
     * @param roleLevel The role level required to access the notifications, defaulting to TEACHER.
     * @return A list of StudentModuleRequest matching the query criteria.
     */
    @Query("SELECT smr FROM StudentModuleRequest smr" +
            " JOIN CourseUser cu ON cu.course = smr.studentModule.lesson.week.course" +
            " WHERE :user = cu.user AND cu.role.level = :roleLevel" +
            " AND (smr.teacher = :user OR smr.teacher IS NULL)" +
            " AND (:all = true OR (smr.requestedOn > cu.user.lastNotificationTime AND smr.satisfiedOn IS NULL))" +
            " ORDER BY smr.requestedOn DESC LIMIT :maxNotifications")
    fun findNotificationsTeacher(user: User, all: Boolean, maxNotifications: Int, roleLevel: RoleLevel = RoleLevel.TEACHER) : List<StudentModuleRequest>

}
