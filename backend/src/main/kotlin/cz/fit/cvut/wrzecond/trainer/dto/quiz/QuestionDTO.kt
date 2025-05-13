package cz.fit.cvut.wrzecond.trainer.dto.quiz

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionType


/**
 * Data transfer object for finding questions.
 *
 * @property id Unique identifier of the question.
 * @property questionData The content of the question.
 * @property possibleAnswersData The possible answers for the question, formatted as a string.
 * @property timeLimit Time limit for answering the question, in seconds.
 * @property author The author of the question.
 * @property singleAnswer Indicates if the question expects a single answer.
 * @property questionType The type of the question, represented by the [QuestionType] enum.
 * @property topics List of topics associated with the question, each represented by a [TopicFindDTO].
 * @property subjects List of subjects associated with the question, each represented by a [SubjectFindDTO].
 */
data class QuestionFindDTO(override val id: Int, val questionData: String,
                           val possibleAnswersData: String, val timeLimit : Int,
                           val author : String, val singleAnswer : Boolean,
                           val questionType : QuestionType, val topics: List<TopicFindDTO>, val subjects: List<SubjectFindDTO>
) : IFindDTO

/**
 * Data transfer object for retrieving question details.
 *
 * @property id The unique identifier for the question.
 * @property questionData The text of the question itself.
 * @property possibleAnswersData A string representation of possible answers.
 * @property timeLimit The time limit for answering the question, in seconds.
 * @property author The author of the question.
 * @property singleAnswer Indicates if the question expects a single answer.
 * @property questionType The type of the question.
 */
data class QuestionGetDTO(override val id: Int, val questionData: String,
                          val possibleAnswersData: String, val timeLimit : Int,
                          val author : String, val singleAnswer : Boolean,
                          val questionType : QuestionType
) : IGetDTO

/**
 * Data transfer object used to update a question.
 *
 * @property questionData The content of the question.
 * @property possibleAnswersData The possible answers for the question.
 * @property correctAnswerData The correct answer among the possible answers.
 * @property timeLimit The time limit for answering the question.
 * @property explanation An explanation or justification for the correct answer.
 * @property author The author of the question.
 * @property singleAnswer Indicates if the question expects a single answer (true) or multiple answers (false).
 * @property questionType The type of the question, as defined in the `QuestionType` enum.
 * @property lesson The ID of the lesson to which this question is related.
 */
data class QuestionUpdateDTO (val questionData: String?, val possibleAnswersData: String?,
                              val correctAnswerData: String?, val timeLimit : Int?,
                              val explanation : String?, val author : String?,
                              val singleAnswer : Boolean?, val questionType : QuestionType,
                              val lesson : Int?
) : IUpdateDTO

/**
 * Data transfer object for creating a question.
 *
 * @param questionData The text of the question.
 * @param possibleAnswersData Possible answers for the question.
 * @param correctAnswerData The correct answer for the question.
 * @param timeLimit The time limit to answer the question, in seconds.
 * @param explanation An optional explanation for the correct answer.
 * @param author The author of the question.
 * @param singleAnswer Indicates if the question allows a single answer (true) or multiple answers (false).
 * @param questionType The type of the question.
 * @param lesson An optional ID representing the lesson to which the question belongs.
 */
data class QuestionCreateDTO (val questionData: String, val possibleAnswersData: String,
                              val correctAnswerData: String, val timeLimit : Int,
                              val explanation : String?, val author : String,
                              val singleAnswer : Boolean, val questionType : QuestionType,
                              val lesson : Int?
) : ICreateDTO

/**
 * Data Transfer Object for representing a question in a websocket communication.
 *
 * @property id Unique identifier of the question.
 * @property questionData The content of the question.
 * @property possibleAnswersData The possible answers associated with the question.
 * @property timeLimit The time limit for answering the question, in seconds.
 * @property author The author or creator of the question.
 * @property singleAnswer Indicator if the question accepts a single answer or multiple answers.
 * @property quizroomId Identifier of the quiz room to which the question belongs.
 * @property questionType The type of the question, defined by the enum `QuestionType`.
 */
data class QuestionWebsocketGetDTO(val id: Int, val questionData: String,
                                   val possibleAnswersData: String, val timeLimit : Int,
                                   val author : String, val singleAnswer : Boolean,
                                   val quizroomId : Int, val questionType : QuestionType
)
/**
 * Data Transfer Object for retrieving the correct answer and its explanation for a question.
 *
 * @property correctAnswerData Represents the correct answer data of the question.
 * @property explanation Provides optional additional information or explanation about the correct answer.
 */
data class QuestionGetCorrectDTO(val correctAnswerData: String, val explanation : String?)

/**
 * A data transfer object representing a question with point reassignment details.
 *
 * @property id The unique identifier of the question.
 * @property questionData The textual content of the question.
 * @property possibleAnswersData The possible answers for the question, formatted as a string.
 * @property correctAnswerData The correct answer for the question, formatted as a string.
 * @property timeLimit The time limit for answering the question in seconds.
 * @property author The author of the question.
 * @property singleAnswer A flag indicating whether the question has a single correct answer.
 * @property questionType The type of the question, represented by the QuestionType enum.
 * @property studentsAdd A list of students who gained points upon answering this question.
 * @property studentsSub A list of students who lost points upon answering this question.
 */
data class QuestionWithPointReasignDTO(val id: Int, val questionData: String,
                                       val possibleAnswersData: String, val correctAnswerData: String,
                                       val timeLimit : Int, val author : String, val singleAnswer : Boolean,
                                       val questionType : QuestionType, val studentsAdd : List<String>,
                                       val studentsSub : List<String>
)