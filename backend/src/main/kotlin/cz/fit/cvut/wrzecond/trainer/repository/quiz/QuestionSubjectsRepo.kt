package cz.fit.cvut.wrzecond.trainer.repository.quiz

import cz.fit.cvut.wrzecond.trainer.entity.Subject
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Question
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionSubject
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing QuestionSubjects entities.
 */
@Repository
interface QuestionSubjectsRepo : IRepository<QuestionSubject> {
    /**
     * Deletes an entry from the database based on the provided question and subject.
     *
     * @param question The question that identifies the entry to be deleted.
     * @param subject The subject that identifies the entry to be deleted.
     */
    fun deleteByQuestionAndSubject(question: Question, subject: Subject)
}