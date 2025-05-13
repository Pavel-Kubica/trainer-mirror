package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.*
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository interface for managing StudentModules entities.
 */
@Repository
interface StudentModuleRepository: IRepository<StudentModule> {

    /**
     * Retrieves a StudentModule entity based on the provided student, module, and lesson.
     *
     * @param student The student for whom the StudentModules are being retrieved.
     * @param module The module for which the StudentModules are being retrieved.
     * @param lesson The lesson for which the StudentModules are being retrieved.
     * @return The matched StudentModule entity or null if not found.
     */
    @Query("SELECT sm FROM StudentModule sm WHERE sm.student = :student AND sm.module = :module AND sm.lesson = :lesson")
    fun getByStudentModule (student: User, module: Module, lesson: Lesson) : StudentModule?

    /**
     * Finds and returns a list of StudentModule entries for a specific student and lesson.
     *
     * @param student The student for whom the StudentModules are being retrieved.
     * @param lesson The lesson for which the StudentModules are being retrieved.
     * @return a list of StudentModule entries that match the specified student and lesson.
     */
    @Query("SELECT sm FROM StudentModule sm WHERE sm.student = :student AND sm.lesson = :lesson")
    fun findByStudentLesson (student: User, lesson: Lesson) : List<StudentModule>

    /**
     * Retrieves a StudentModule entity based on the provided student, module, and lesson.
     *
     * @param student The student for whom the StudentModules are being retrieved.
     * @param module The module for which the StudentModules are being retrieved.
     * @param lesson The lesson for which the StudentModules are being retrieved.
     * @return The matched StudentModule entity or null if not found.
     */
    @Query("SELECT sm FROM StudentModule sm WHERE sm.module =:module AND sm.student =:student AND sm.lesson =:lesson")
    fun findByUserModuleLesson (student: User, module: Module, lesson: Lesson) : StudentModule?

    /**
     * Retrieves a list of StudentModule entities for a given lesson and module, ordered by their completion date.
     *
     * @param lesson The lesson for which the StudentModules are being retrieved.
     * @param module The module for which the StudentModules are being retrieved.
     * @return A list of StudentModule entities that match the specified lesson and module.
     */
    @Query("SELECT sm FROM StudentModule sm WHERE sm.lesson.id = :lesson AND sm.module.id = :module ORDER BY sm.completedOn")
    fun findByLessonModule (lesson: Int, module: Int) : List<StudentModule>


    /**
     * Finds and returns a list of StudentModule entries associated with a specific module.
     *
     * @param module The module for which the StudentModules are being retrieved.
     * @return A list of StudentModule entries that match the specified module.
     */
    @Query("SELECT sm FROM StudentModule sm WHERE sm.module = :module")
    fun findByModule (module: Module) : List<StudentModule>

}
