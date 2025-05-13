package cz.fit.cvut.wrzecond.trainer.dto.quiz

/**
 * Represents a question within a specific room.
 *
 * @property id The unique identifier of the question.
 * @property room The identifier of the room where the question is located.
 */
data class QuestionWS(val id: Int, val room : Int)
