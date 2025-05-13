package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.*
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing LessonModules entities.
 */
@Repository
interface LessonModuleRepository: IRepository<LessonModule> {

    /**
     * Retrieves a LessonModule entity based on the provided lesson and module.
     *
     * @param lesson the Lesson entity which the LessonModule is associated with.
     * @param module the Module entity which the LessonModule is associated with.
     * @return a LessonModule entity if a match is found, otherwise null.
     */
    @Query("SELECT lm FROM LessonModule lm WHERE lm.lesson = :lesson AND lm.module = :module")
    fun getByLessonModule (lesson: Lesson, module: Module) : LessonModule?

    @Query("SELECT lm FROM LessonModule lm WHERE lm.lesson = :lesson")
    fun getModulesByLesson (lesson: Lesson) : List<LessonModule>

}
