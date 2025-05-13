package cz.fit.cvut.wrzecond.trainer.repository.quiz

import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quiz
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Quizzes entities.
 */
@Repository
interface QuizRepo : IRepository<Quiz> {

    /**
     * Checks if quiz with a given module exists.
     *
     * @param module The module to check.
     * @return True if such quiz exists, false otherwise.
     */
    fun existsByModule(module: Module) : Boolean
    /**
     * Retrieves a quiz associated with the specified module.
     *
     * @param module The module for which the associated quiz is to be retrieved.
     * @return The quiz associated with the provided module.
     */
    fun getByModule(module: Module) : Quiz
}