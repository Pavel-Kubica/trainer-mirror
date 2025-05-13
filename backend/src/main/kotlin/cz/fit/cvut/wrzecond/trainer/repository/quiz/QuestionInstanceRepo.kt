package cz.fit.cvut.wrzecond.trainer.repository.quiz

import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionInstance
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quizroom
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing QuestionInstance entities.
 */
@Repository
interface QuestionInstanceRepo : IRepository<QuestionInstance> {

    /**
     * Finds all `QuestionInstance` entities associated with the given `Quizroom`.
     *
     * @param quizroom The `Quizroom` entity for which all associated `QuestionInstance` entities should be retrieved.
     * @return A list of `QuestionInstance` entities that belong to the specified `Quizroom`.
     */
    fun findAllByQuizroom(quizroom: Quizroom) : List<QuestionInstance>
}