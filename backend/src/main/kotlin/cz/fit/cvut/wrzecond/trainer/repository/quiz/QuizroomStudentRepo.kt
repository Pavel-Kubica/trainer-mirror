package cz.fit.cvut.wrzecond.trainer.repository.quiz

import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quizroom
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuizroomStudent
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing QuizroomStudents entities.
 */
@Repository
interface QuizroomStudentRepo: IRepository<QuizroomStudent> {

    /**
     * Checks if a QuizroomStudent with specified Quizroom and student.
     *
     * @param quizroom The quiz room to check.
     * @param student The student to check.
     * @return True if such QuizroomStudent entity exisits, otherwise false.
     */
    fun existsByQuizroomAndStudent(quizroom: Quizroom, student: User) : Boolean

    /**
     * Finds a QuizroomStudent entity based on the provided quiz room and student.
     *
     * @param quizroom The quiz room to check.
     * @param student The student to check.
     * @return The QuizroomStudent entity matching the provided quiz room and student.
     */
    fun findByQuizroomAndStudent(quizroom: Quizroom, student: User) : QuizroomStudent

    /**
     * Retrieves a list of QuizroomStudent entities associated with a given Quizroom.
     *
     * @param quizroom The Quizroom object used to find associated QuizroomStudent entities.
     * @return A list of such QuizroomStudent entities.
     */
    fun findAllByQuizroom(quizroom: Quizroom) : List<QuizroomStudent>

    /**
     * Retrieves a list of QuizroomStudent objects associated with the specified student.
     *
     * @param student the User object used to find associated QuizroomStudent entities.
     * @return a list of QuizroomStudent objects corresponding to the provided student.
     */
    fun findAllByStudent(student: User) : List<QuizroomStudent>
}