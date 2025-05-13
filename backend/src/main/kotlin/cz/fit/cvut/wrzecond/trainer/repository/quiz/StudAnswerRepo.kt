package cz.fit.cvut.wrzecond.trainer.repository.quiz


import cz.fit.cvut.wrzecond.trainer.entity.quiz.Question
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quizroom
import cz.fit.cvut.wrzecond.trainer.entity.quiz.StudentAnswer
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing StudAnswers entities.
 */
@Repository
interface StudAnswerRepo : IRepository<StudentAnswer> {

    /**
     * Retrieves a list of StudentAnswer entities associated with the given Question.
     *
     * @param question The Question entity for which to retrieve associated StudentAnswer entities.
     * @return A list of StudentAnswer entities associated with the given Question,
     *         or null if no answers are found.
     */
    fun getAllByQuestion(question: Question) : List<StudentAnswer>?

    //fun getAllByQuizroom(quizroom: Quizroom) : List<StudentAnswer>?

    /**
     * Retrieves a list of StudentAnswer objects that are associated with the specified quiz room and question.
     *
     * @param quizroom The Quizroom object for which the answers are to be retrieved.
     * @param question The Question object for which the answers are to be retrieved.
     * @return A list of StudentAnswer objects that match the specified quiz room and question.
     */
    fun getAllByQuizroomAndQuestion(quizroom: Quizroom, question: Question) :List<StudentAnswer>

    /**
     * Checks if a StudAnswer exists for the given quiz room, student, and question.
     *
     * @param quizroom The quiz room to check.
     * @param student The student to check.
     * @param question The question to check.
     * @return `true` if the StudAnswer exists, `false` otherwise.
     */
    fun existsByQuizroomAndStudentAndQuestion(quizroom: Quizroom, student: User, question: Question) : Boolean

    /**
     * Retrieves all student answers for a given quiz room, question, and student.
     *
     * @param quizroom The quiz room to check.
     * @param question The question to check.
     * @param student The student to check.
     * @return A list of StudAnswers associated with the specified quiz room, question, and student.
     */
    fun getAllByQuizroomAndQuestionAndStudent(quizroom: Quizroom, question: Question, student: User) : List<StudentAnswer>

    /**
     * Retrieves all student answers for a given quiz room and student.
     *
     * @param quizroom The quiz room to check.
     * @param student The student to check.
     * @return a list of StudAnswers for the specified quiz room and student.
     */
    fun getAllByQuizroomAndStudent(quizroom: Quizroom, student: User): List<StudentAnswer>

}