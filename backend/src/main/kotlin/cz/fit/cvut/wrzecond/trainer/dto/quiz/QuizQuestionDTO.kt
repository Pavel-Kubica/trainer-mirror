package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.entity.ModuleType
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionType

/**
 * Data transfer object for quiz question list
 * @property id quiz question identifier
 * @property text question text
 * @property type question type
 */
data class QuizQuestionReadDTO(val id: Int, val text: String, val type: QuestionType, val orderNum: Int)

/**
 * Data transfer object for quiz question creating
 * @property orderNum question order in quiz
 */
data class QuizQuestionEditDTO(val orderNum: Int?)

/**
 * Data transfer object for quiz question copying
 * @param copiedId original question id
 * @property orderNum question order in quiz
 */
data class QuizQuestionCopyDTO(val copiedId: Int, val orderNum: Int)