package cz.fit.cvut.wrzecond.trainer.service.quiz

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.dto.quiz.*
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Question
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quiz
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuizQuestion
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizQuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizRepo
import cz.fit.cvut.wrzecond.trainer.service.IServiceImplOld
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException


/**
 * Service class for managing quizzes, including creation, update, and retrieval operations.
 *
 * @param repository The repository that manages Quiz entities.
 * @param questionRepo The repository that manages Question entities.
 * @param moduleRepo The repository that manages Module entities.
 * @param quizQuestionRepo The repository that manages QuizQuestion entities.
 * @param logRepository The repository that logs operations on entities.
 * @param userRepo The repository that manages User entities.
 */
@Service
class QuizService(override val repository: QuizRepo, private val questionRepo: QuestionRepo,
                  private val moduleRepo: ModuleRepository, private val quizQuestionRepo: QuizQuestionRepo,
                  private val logRepository: LogRepository,
                  userRepo: UserRepository
)
    : IServiceImplOld<Quiz, QuizFindDTO, QuizGetDTO, QuizCreateDTO, QuizUpdateDTO>(repository, userRepo){


    /**
     * Retrieves a quiz by the specified module ID.
     *
     * @param moduleId The ID of the module whose quiz is to be retrieved.
     * @param user The authenticated user requesting the quiz.
     * @throws ResponseStatusException if the module or the quiz does not exist.
     * @return A QuizGetDTO object representing the quiz.
     */
    fun getQuizByModuleId(moduleId : Int, user: UserAuthenticateDto?) = tryCatch {
        if(!moduleRepo.existsById(moduleId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if(!repository.existsByModule(moduleRepo.findById(moduleId).get()))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        repository.getByModule(moduleRepo.findById(moduleId).get()).toGetDTO()
    }

    /**
     * Creates a new Quiz based on the provided QuizCreateDTO and the authenticated user information.
     *
     * @param dto the QuizCreateDTO object containing details for the new quiz.
     * @param userDto the UserAuthenticateDto object containing the authenticated user's details. This can be null.
     * @return the created Quiz in its GetDTO representation after saving it and its associated questions to the database.
     * @throws ResponseStatusException if any of the questions in dto do not exist in the question repository.
     */
    override fun create(dto: QuizCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {

        val newQuiz = repository.saveAndFlush(dto.toEntity())
        logRepository.saveAndFlush(createLogEntry(userDto, newQuiz, "create"))

        var i = 1
        dto.questions.forEach {
            if(!questionRepo.existsById(it))
                throw ResponseStatusException(HttpStatus.NOT_FOUND)

            val qq = quizQuestionRepo.saveAndFlush(QuizQuestion(newQuiz,questionRepo.findById(it).get(),i++))
            logRepository.saveAndFlush(createLogEntry(userDto, qq, "create"))
        }

        newQuiz.toGetDTO()
    }

    /**
     * Updates an existing quiz with the provided data transfer object.
     *
     * @param id The ID of the quiz to be updated.
     * @param dto The QuizUpdateDTO object containing the updated quiz data.
     * @param userDto The UserAuthenticateDto object containing user authentication details.
     *                It may be nullable.
     * @return An updated quiz as QuizGetDTO object.
     * @throws ResponseStatusException if the quiz with the specified ID does not exist or
     *                                 if any question ID in the provided questions list does not exist.
     */
    override fun update(id: Int, dto: QuizUpdateDTO, userDto: UserAuthenticateDto?) = tryCatch {

        if(!repository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val quiz = repository.findById(id).get()
        if(dto.questions != null){
            val toDelete = quizQuestionRepo.findAllByQuiz(quiz)
            toDelete.forEach {
                quizQuestionRepo.delete(it)
            }

            var i = 1
            dto.questions.forEach {
                if(!questionRepo.existsById(it))
                    throw ResponseStatusException(HttpStatus.NOT_FOUND)

                val qq = quizQuestionRepo.saveAndFlush(QuizQuestion(quiz,questionRepo.findById(it).get(),i++))
                logRepository.saveAndFlush(createLogEntry(userDto, qq, "create"))
            }

        }

        val updatedQuiz = repository.saveAndFlush(repository.findById(id).get().merge(dto))
        logRepository.saveAndFlush(createLogEntry(userDto, updatedQuiz, "update"))
        updatedQuiz.toGetDTO()

    }

    /**
     * Copies an existing quiz from `oldModule` to `newModule`.
     *
     * @param oldModule The module from which the quiz will be copied.
     * @param newModule The module to which the quiz will be copied.
     *
     * @throws ResponseStatusException if no quiz is found for the `oldModule`.
     */
    fun copyQuiz(oldModule : Module, newModule : Module){
        if(!repository.existsByModule(oldModule))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val oldQuiz = repository.getByModule(oldModule)
        val newQuiz = repository.saveAndFlush(Quiz(oldQuiz.numOfQuestions, oldQuiz.name, oldQuiz.numOfAttempts, emptyList(), emptyList(), newModule))
        logRepository.saveAndFlush(createLogEntry(null, newQuiz, "create"))

        val oldQuestions = oldQuiz.questions
        oldQuestions.forEach {
            val qq = quizQuestionRepo.saveAndFlush(QuizQuestion(newQuiz,it.question,it.orderNum))
            logRepository.saveAndFlush(createLogEntry(null, qq, "create"))
        }
    }

    /**
     * Converts a Quiz entity to a QuizGetDTO.
     *
     * @return A QuizGetDTO object containing the details of the quiz
     *         including its ID, name, number of questions, number of attempts, and
     *         the list of question IDs associated with the quiz.
     */
    override fun Quiz.toGetDTO() = QuizGetDTO(id, name, numOfQuestions, numOfAttempts, questions.map { it.question.id })

    /**
     * Converts a Quiz entity to a QuizFindDTO.
     *
     * @return A QuizFindDTO containing the quiz's ID, name, number of questions,
     *         and number of attempts.
     */
    override fun Quiz.toFindDTO() = QuizFindDTO(id,name, numOfQuestions, numOfAttempts)

    /**
     * Converts a QuizCreateDTO object to a Quiz entity.
     *
     * This method performs the following operations*/
    override fun QuizCreateDTO.toEntity() : Quiz {
        questions.forEach{
            if(!questionRepo.existsById(it))
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        if(!moduleRepo.existsById(moduleId)){
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }


        return Quiz(numOfQuestions, name, numOfAttempts, emptyList(), emptyList(), moduleRepo.findById(moduleId).get())
    }

    /**
     * Merges the current Quiz object with the properties from the provided QuizUpdateDTO.
     *
     * @param dto The QuizUpdateDTO containing the updated quiz information, such as questions, module ID, name, number of questions, and number of attempts.
     * @return The updated Quiz object with merged properties.
     * @throws ResponseStatusException if any of the question IDs does not exist in the question repository or if the module ID does not exist in the module repository.
     */
    override fun Quiz.merge(dto: QuizUpdateDTO): Quiz {
        dto.questions?.forEach{
            if(!questionRepo.existsById(it))
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        if(dto.moduleId != null){
            if(!moduleRepo.existsById(dto.moduleId))
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }


        if(dto.moduleId != null){
            return Quiz(dto.numOfQuestions?: numOfQuestions, dto.name ?: name,
                dto.numOfAttempts ?: numOfAttempts, questions, quizRooms, moduleRepo.findById(dto.moduleId).get(), id)
        }

        return Quiz(dto.numOfQuestions?: numOfQuestions, dto.name ?: name,
            dto.numOfAttempts ?: numOfAttempts, questions, quizRooms, module ,id)


    }

    /**
     * Converts a Question entity to a QuestionFindDTO.
     *
     * @return A QuestionFindDTO containing the question's ID, question data, possible answers data,
     *          time limit, author, whether it is single-answer, question type, associated topics,
     *          and associated subjects.
     */
    private fun Question.toFindDTO() = QuestionFindDTO(id, questionData, possibleAnswersData, timeLimit, author.username, singleAnswer, questionType,
        topics.map { converter.toFindDTO(it.topic) } ,subjects.map { converter.toFindDTO(it.subject) } )
}