package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Question
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quiz
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuizQuestion
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizQuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizRepo
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import cz.fit.cvut.wrzecond.trainer.service.LogService

/**
 * Service class for managing quiz questions.
 *
 * @param repository The repository for quiz questions.
 * @param questionRepository The repository for questions.
 * @param quizRepository The repository for quizzes.
 * @param logRepository The repository for logs.
 * @param userRepository The repository for users.
 * @param logService The service for logs.
 */
@Service
class QuizQuestionService(override val repository: QuizQuestionRepo,
                          private val questionRepository: QuestionRepo,
                          private val quizRepository: QuizRepo,
                          private val logRepository: LogRepository, userRepository: UserRepository,
                          private val logService: LogService)
    : IServiceBase<QuizQuestion>(repository, userRepository) {


    /**
     * Add new question to a given quiz
     * @param quizId identifier of the quiz
     * @param questionId identifier of the question
     * @param dto desired order
     * @param userDto user performing the request
     * @return QuizQuestionReadDTO in case of success
     */
    fun putQuizQuestion(quizId: Int, questionId: Int, dto: QuizQuestionEditDTO, userDto: UserAuthenticateDto?)
            = checkAccess(quizId, questionId, userDto) { quiz, question, user ->
        val qq = repository.getByQuizQuestion(quiz, question)
        val id = qq?.id ?: 0
        if (id == 0 && !question.canView(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val newQq = QuizQuestion(quiz, question, dto.orderNum ?: qq?.orderNum ?: (quiz.questions.size + 1), id)
        val savedNewQq = repository.saveAndFlush(newQq)

        logService.log(userDto, savedNewQq, "create")
        converter.toReadDTO(savedNewQq)
    }

    /**
     * Remove question from a given quiz
     * @param quizId identifier of the quiz
     * @param questionId identifier of the question
     * @param userDto user performing the request
     */
    fun delQuizQuestion(quizId: Int, questionId: Int, userDto: UserAuthenticateDto?): Unit
            = checkAccess(quizId, questionId, userDto) { quiz, question, _ ->
        val qq = repository.getByQuizQuestion(quiz, question) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        repository.delete(qq)
        logService.log(userDto, qq, "delete")
    }



    /**
     * Checks if the user has access to the specified quiz and question, then performs the given block of code.
     *
     * @param quizId the identifier of the quiz
     * @param questionId the identifier of the question
     * @param userDto the user authentication data transfer object
     * @param block the code block to execute if access is granted, taking the quiz, question, and user as parameters
     */
    private fun <X> checkAccess(quizId: Int, questionId: Int, userDto: UserAuthenticateDto?,
                                block: (Quiz, Question, User) -> X) = tryCatch {
        val user = getUser(userDto)
        val quiz = quizRepository.getReferenceById(quizId)
        val question = questionRepository.getReferenceById(questionId)
        if (!quiz.canEdit(user) || !question.canView(user)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        block(quiz, question, user)
    }
}
