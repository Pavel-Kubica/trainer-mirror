package cz.fit.cvut.wrzecond.trainer.dto.quiz

import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO

/**
 * Data Transfer Object representing a quiz with details.
 *
 * @property id The unique identifier of the quiz.
 * @property name The name of the quiz.
 * @property numOfQuestions The number of questions present in the quiz.
 * @property numOfAttempts The number of attempts made for the quiz.
 *
 * Inherits from IFindDTO to enable searching for specific quizzes by their ID.
 */
//-> out
data class QuizFindDTO(override val id: Int, val name : String, val numOfQuestions: Int,
                       val numOfAttempts: Int) : IFindDTO
/**
 * Data Transfer Object for retrieving quiz information.
 *
 * @property id The unique identifier of the quiz.
 * @property name The name of the quiz.
 * @property numOfQuestions The number of questions in the quiz.
 * @property numOfAttempts The number of attempts made on the quiz.
 * @property questions The list of question identifiers associated with the quiz.
 */
//-> out
data class QuizGetDTO(override val id: Int, val name : String, val numOfQuestions: Int,
                      val numOfAttempts: Int, val questions: List<Int>) : IGetDTO

/**
 * Data transfer object for updating quiz details.
 *
 * This class holds the necessary information to update an existing quiz, including its
 * name, number of questions, number of attempts, list of question IDs,
 * and associated module ID.
 *
 * @property name The name of the quiz; null if not being updated.
 * @property numOfQuestions The total number of questions in the quiz; null if not being updated.
 * @property numOfAttempts The number of attempts allowed for the quiz; null if not being updated.
 * @property questions A list of question IDs associated with the quiz; null if not being updated.
 * @property moduleId The ID of the module to which the quiz belongs; null if not being updated.
 */
//<- in
data class QuizUpdateDTO (val name : String?, val numOfQuestions: Int?,
                          val numOfAttempts: Int?, val questions: List<Int>?,
                          val moduleId : Int?) : IUpdateDTO
/**
 * Data transfer object for creating a quiz.
 *
 * @property name The name of the quiz.
 * @property numOfQuestions The number of questions in the quiz.
 * @property numOfAttempts The number of attempts allowed for the quiz.
 * @property questions A list of IDs representing the questions included in the quiz.
 * @property moduleId The ID of the module to which the quiz belongs.
 */
//<- in
data class QuizCreateDTO (val name : String, val numOfQuestions: Int,
                          val numOfAttempts: Int, val questions: List<Int>,
                          val moduleId : Int) : ICreateDTO
