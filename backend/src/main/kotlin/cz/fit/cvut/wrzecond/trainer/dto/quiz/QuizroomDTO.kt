package cz.fit.cvut.wrzecond.trainer.dto.quiz

import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO

/**
 * Data Transfer Object representing a quiz room with detailed information.
 *
 * @property id Unique identifier of the quiz room entity.
 * @property createdBy The identifier of the user who created the quiz room.
 * @property roomPassword The password required to join the quiz room.
 * @property quiz The identifier of the associated quiz.
 * @property students A list of identifiers for students participating in the quiz room.
 * @property currQuestion The identifier of the current question being presented in the quiz room.
 * @property roomState A boolean indicating whether the room is active (true) or not (false).
 * @property timeLeft The time remaining for the quiz in seconds.
 * @property quizState The current state of the quiz as defined in the system.
 */
//-> out
data class QuizroomFindDTO(override val id: Int, val createdBy : String,
                           val roomPassword : String, val quiz : Int,
                           val students: List<Int>, val currQuestion : Int,
                           val roomState: Boolean, val timeLeft: Int,
                           val quizState : String) : IFindDTO
/**
 * Data Transfer Object for retrieving quiz room details.
 *
 * @property id Unique identifier for the quiz room.
 * @property createdBy Username or identifier of the person who created the quiz room.
 * @property roomPassword Password required to enter the quiz room.
 * @property quiz Identifier for the quiz associated with the room.
 * @property students List of students participating in the quiz room.
 * @property currQuestion Current question number being presented to the students.
 * @property roomState Indicates if the room is currently active or not.
 * @property timeLeft Time left for the current question or session.
 * @property quizState Current state of the quiz.
 */
//-> out
data class QuizroomGetDTO(override val id: Int, val createdBy : String,
                          val roomPassword : String, val quiz : Int,
                          val students: List<String>, val currQuestion : Int,
                          val roomState: Boolean , val timeLeft: Int,
                          val quizState : String) : IGetDTO

/**
 * Data Transfer Object for updating information of a quiz room.
 *
 * @property createdBy The username or identifier of the creator of the quiz room.
 * @property roomPassword The password required to access the quiz room.
 * @property quiz The ID of the associated quiz.
 * @property students A list of student IDs associated with the quiz room.
 * @property currQuestion The index or ID of the current question in the quiz.
 * @property roomState The availability status of the quiz room.
 * @property timeLeft The remaining time in seconds for the quiz room session.
 * @property quizState The current state or progress of the quiz.
 * @property lesson The associated lesson or module ID.
 */
//<- in
data class QuizroomUpdateDTO (val createdBy : String?, val roomPassword : String?,
                              val quiz : Int?, val students : List<Int>?,
                              val currQuestion : Int?, val roomState : Boolean?,
                              val timeLeft: Int?, val quizState : String?,
                              val lesson : Int ) : IUpdateDTO
/**
 * Data Transfer Object for creating a new quiz room.
 *
 * @property createdBy The username or identifier of the user who is creating the quiz room.
 * @property quiz The ID of the quiz associated with the quiz room.
 * @property lesson The ID of the lesson associated with the quiz room.
 */
//<- in
data class QuizroomCreateDTO (val createdBy : String, val quiz : Int,
                              val lesson: Int) : ICreateDTO


//=======

/**
 * Data Transfer Object representing a quiz room.
 *
 * @property id Unique identifier of the quiz room.
 * @property createdBy The creator of the quiz room.
 * @property roomPassword Password required to enter the quiz room.
 * @property quiz Identifier for the quiz associated with the room.
 * @property students List of student identifiers currently in the room.
 * @property currQuestion Identifier of the current question being handled in the quiz room.
 * @property roomState Boolean indicating if the room is currently active or not.
 * @property timeLeft Amount of time left for the current quiz session in the room.
 * @property quizState The current state description of the quiz.
 * @property points Total points accumulated in the quiz room.
 */
data class QuizroomListDTO(override val id: Int, val createdBy : String,
                           val roomPassword : String, val quiz : Int,
                           val students: List<Int>, val currQuestion : Int,
                           val roomState: Boolean, val timeLeft: Int,
                           val quizState : String, val points: Int) : IFindDTO

/**
 * Data Transfer Object for retrieving details about a self-test session.
 *
 * @property id Unique identifier of the self-test session.
 * @property createdBy The username of the creator of the self-test.
 * @property roomPassword The password of the room where the self-test is conducted.
 * @property quiz The unique identifier of the quiz associated with this self-test.
 * @property students A list of usernames of the students participating in the self-test.
 * @property currQuestion The current question index that is being presented in the self-test.
 * @property roomState The current state of the room.
 * @property timeLeft The remaining time for the self-test in seconds.
 * @property quizState The current state of the quiz.
 * @property selftestQuestions A list of question identifiers associated with the self-test.
 */
data class SelftestGetDTO(override val id: Int, val createdBy : String,
                          val roomPassword : String, val quiz : Int,
                          val students: List<String>, val currQuestion : Int,
                          val roomState: Boolean, val timeLeft: Int,
                          val quizState : String, val selftestQuestions : List<Int>) : IGetDTO

/**
 * Data Transfer Object for creating a self-test.
 *
 * @property createdBy The user who created the self-test.
 * @property quiz The ID of the quiz associated with the self-test.
 * @property lesson The ID of the lesson associated with the self-test.
 * @property selftestLength The length of the self-test, typically measured in number of questions.
 * @property timeControl The time control level for the self-test.
 */
data class SelftestCreateDTO (val createdBy : String, val quiz : Int,
                              val lesson: Int, val selftestLength : Int,
                              val timeControl : String) : ICreateDTO