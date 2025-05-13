package cz.fit.cvut.wrzecond.trainer.repository.quiz

import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quiz
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quizroom
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Quizrooms entities.
 */
@Repository
interface QuizroomRepo : IRepository<Quizroom> {

    /**
     * Retrieves all Quizroom entities associated with the given Quiz.
     *
     * @param quiz The quiz entity to retrieve associated Quizrooms for
     * @return A list of Quizroom entities associated with the given quiz, or null if no Quizrooms are found
     */
    fun getAllByQuiz(quiz: Quiz) : List<Quizroom>?

    /**
     * Retrieves a list of Quizroom entities that match the provided room password.
     *
     * @param roomPassword The password of the room for which the Quizroom entities are to be retrieved.
     * @return A list of Quizroom entities that match the provided room*/
    fun getAllByRoomPassword(roomPassword: String) : List<Quizroom>

    /**
     * Retrieves all quiz rooms that match the specified password and state.
     *
     * @param roomPassword The password of the quiz rooms to be retrieved.
     * @param roomState The state of the quiz rooms to be retrieved.
     * @return A list of quiz rooms that match the given password and state.
     */
    fun getAllByRoomPasswordAndRoomState(roomPassword: String, roomState: Boolean) : List<Quizroom>

    /**
     * Checks if a room exists by its password.
     *
     * @param roomPassword The password of the room to check for existence.
     * @return `true` if a room with the specified password exists, `false` otherwise.
     */
    fun existsByRoomPassword(roomPassword: String) : Boolean

    @Query("SELECT qr FROM Quizroom qr WHERE qr.roomState = true")
    fun getAllActive() : List<Quizroom>
}