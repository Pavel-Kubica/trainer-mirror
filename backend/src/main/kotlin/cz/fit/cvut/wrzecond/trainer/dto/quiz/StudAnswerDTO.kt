package cz.fit.cvut.wrzecond.trainer.dto.quiz

import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO

/**
 * Data Transfer Object for finding student answers.
 *
 * @property id Identifier of the student answer.
 * @property quizroom Identifier for the quiz room where the answer was given. Nullable.
 * @property student Identifier of the student who provided the answer.
 * @property question Identifier of the question that was answered.
 * @property data The answer data provided by the student.
 */
//-> out
data class StudAnswerFindDTO(override val id: Int, val quizroom : Int?, val student: Int,
                             val question: Int, val data : String) : IFindDTO

/**
 * Data Transfer Object for Student Answer.
 *
 * @property id The unique identifier of the student answer.
 * @property quizroom The identifier of the quiz room. Nullable.
 * @property student The identifier of the student.
 * @property question The identifier of the question.
 * @property data The answer data provided by the student.
 */
//-> out
data class StudAnswerGetDTO(override val id: Int, val quizroom : Int?, val student: Int,
                            val question: Int, val data : String) : IGetDTO

/**
 * Data Transfer Object for updating a student's answer.
 *
 * @constructor Initializes an instance of the StudAnswerUpdateDTO class.
 * @property quizroom The ID of the quiz room associated with the answer, nullable.
 * @property student The ID of the student providing the answer, nullable.
 * @property question The ID of the question being answered, nullable.
 * @property data The answer data provided by the student.
 * @property lesson The ID of the lesson associated with the answer.
 */
//<- in
data class StudAnswerUpdateDTO (val quizroom : Int?, val student: Int?,
                                val question: Int?, val data : String,
                                val lesson: Int) : IUpdateDTO

/**
 * Data Transfer Object for creating a student's answer.
 *
 * @property lesson Identifier for the related lesson.
 * @property quizroom Optional identifier for the quiz room.
 * @property student Identifier for the student providing the answer.
 * @property question Identifier for the question being answered.
 * @property data The answer data/response provided by the student.
 */
//<- in
data class StudAnswerCreateDTO (val lesson: Int,val quizroom : Int?,
                                val student: Int, val question: Int,
                                val data : String) : ICreateDTO
