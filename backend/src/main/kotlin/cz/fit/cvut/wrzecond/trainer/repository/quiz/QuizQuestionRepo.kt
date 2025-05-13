package cz.fit.cvut.wrzecond.trainer.repository.quiz

import cz.fit.cvut.wrzecond.trainer.entity.quiz.Question
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quiz
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuizQuestion
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing QuizQuestions entities.
 */
@Repository
interface QuizQuestionRepo : IRepository<QuizQuestion> {

    /**
     * Checks if a QuizQuestion exists within a given Question and Quiz.
     *
     * @param question The Question to check.
     * @param quiz The Quiz to check.
     * @return True if the such QuizQuestion exists, otherwise false.
     */
    fun existsByQuestionAndQuiz(question: Question, quiz: Quiz) : Boolean

    /**
     * Finds all quiz questions associated with a specific quiz.
     *
     * @param quiz The quiz for which quiz questions are to be retrieved.
     * @return A list of QuizQuestion objects associated with the given quiz.
     */
    fun findAllByQuiz(quiz: Quiz) : List<QuizQuestion>

    /**
     * Fetches all QuizQuestion entities that are associated with the given question.
     *
     * @param question The question entity for which related QuizQuestion entities are to be retrieved.
     * @return A list of QuizQuestion entities related to the specified question.
     */
    fun findAllByQuestion(question: Question) : List<QuizQuestion>

    /**
     * Retrieves a QuizQuestion entity by its associated Quiz and Question.
     *
     * @param quiz The Quiz entity.
     * @param question The Question entity.
     * @return The QuizQuestion entity that matches the given Quiz and Question, or null if no match is found.
     */
    @Query("SELECT qq FROM QuizQuestion qq WHERE qq.quiz = :quiz AND qq.question = :question")
    fun getByQuizQuestion (quiz: Quiz, question: Question) : QuizQuestion?
}