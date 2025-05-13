package cz.fit.cvut.wrzecond.trainer.service.quiz

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.dto.quiz.*
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionTopic
import cz.fit.cvut.wrzecond.trainer.entity.StudentModule
import cz.fit.cvut.wrzecond.trainer.entity.Topic
import cz.fit.cvut.wrzecond.trainer.entity.quiz.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.*
import cz.fit.cvut.wrzecond.trainer.service.IServiceImpl
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class responsible for managing questions.
 *
 * @property repository Manages questions and related operations.
 * @property authorizationService Provides authorization logic to verify user permissions.
 * @property quizroomRepo Manages quiz rooms and related operations.
 * @property userRepo Manages users and related operations.
 * @property quizQuestionRepo Manages the association between quizzes and questions.
 * @property quizRepo Manages quizzes and related operations.
 * @property topicRepository Manages topics and related operations.
 * @property questionTopicRepository Manages the association between questions and topics.
 * @property questionSubjectRepository Manages the association between questions and subjects.
 * @property saRepo Repository for handling student assignments related to questions.
 * @property logRepository Repository for handling logging
 * @property quizroomStudentRepo Manages the association between quiz rooms and students.
 * @property studentModuleRepo Manages studentModules and related operations.
 * @property lessonRepo Manages lessons and related operations.
 * @property subjectRepository Manages subjects and related operations.
 */
@Service
class QuestionService(
    override val repository: QuestionRepo,
    private val authorizationService: AuthorizationService,
    private val quizroomRepo: QuizroomRepo,
    private val userRepo: UserRepository,
    private val quizQuestionRepo: QuizQuestionRepo,
    private val quizRepo: QuizRepo,
    private val topicRepository: TopicRepository,
    private val questionTopicRepository: QuestionTopicRepository,
    private val questionSubjectRepository: QuestionSubjectsRepo,
    private val saRepo: StudAnswerRepo,
    private val logRepository: LogRepository,
    private val quizroomStudentRepo: QuizroomStudentRepo,
    private val studentModuleRepo: StudentModuleRepository,
    private val lessonRepo: LessonRepository,
    private val subjectRepository: SubjectRepository
) : IServiceImpl<Question, QuestionFindDTO, QuestionGetDTO, QuestionCreateDTO, QuestionUpdateDTO>(
    repository,
    userRepo
) {
    @Transactional
    override fun delete(id: Int, userDto: UserAuthenticateDto?) {
        val q = repository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

        val qRooms = quizQuestionRepo.findAllByQuestion(q)
        qRooms.forEach {
            val quiz = quizRepo.findById(it.quiz.id).get()
            quizRepo.saveAndFlush( Quiz(
                quiz.numOfQuestions - 1, quiz.name, quiz.numOfAttempts, quiz.questions,
                quiz.quizRooms, quiz.module, quiz.id
            ))
        }
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user) && q.author.username != user.username)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        repository.delete(q)
        logRepository.saveAndFlush(createLogEntry(userDto, q, "delete"))
    }

    override fun create(dto: QuestionCreateDTO, userDto: UserAuthenticateDto?)= tryCatch {
        checkEditAccess(converter.toEntity(dto), userDto) { entity, _ -> converter.toGetDTO(repository.saveAndFlush(entity)) }
    }

    override fun update(id: Int, dto: QuestionUpdateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val q = repository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user) && q.author.username != user.username)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val ret = repository.saveAndFlush(converter.merge(q, dto))
        logRepository.saveAndFlush(createLogEntry(userDto, ret, "update"))
        converter.toGetDTO(ret)
    }

    override fun getByID (id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(getEntityByID(id), userDto) { question, _ -> converter.toGetDTO(question) }

    @Transactional
    override fun findAll (userDto: UserAuthenticateDto?) = tryCatch {
        val userEntity = getUser(userDto)
        repository.findAll(sort).filter { it.canView(userEntity) }.map { converter.toFindDTO(it) }
    }

    /**
     * Retrieves a QuestionWebsocketGetDTO by its id, ensuring various authorization requirements are met.
     *
     * @param id The unique identifier of the question to retrieve.
     * @param loginSecret The login secret of the user making the request.
     * If null, a FORBIDDEN status is thrown.
     * @param room The identifier of the quiz room where the question belongs.
     * @return A QuestionWebsocketGetDTO representing the requested question if found and all checks pass.
     * @throws ResponseStatusException If any of the authorization checks fail or
     * if the question with the provided id does not exist.*
     */
    fun websocketGetByID(id: Int, loginSecret: String?, room: Int): QuestionWebsocketGetDTO {
        if (loginSecret == null)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val userEntity = userRepo.getByLoginSecret(loginSecret) ?: throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val roomEntity = quizroomRepo.findById(room).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST) }
        val roomAuthor = userRepo.getByUsername(roomEntity.createdBy) ?: throw ResponseStatusException(HttpStatus.FORBIDDEN)

        if (roomAuthor.id != userEntity.id)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val q = repository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        return QuestionWebsocketGetDTO(
            q.id, q.questionData, q.possibleAnswersData, q.timeLimit, q.author.username, q.singleAnswer, room, q.questionType
        )
    }

    /**
     * Retrieves the correct answer and its explanation for a given question by its ID.
     * Ensures the requesting user has the necessary permissions to view the question.
     *
     * @param id The ID of the question to retrieve.
     * @param user The user authentication information of the requester.
     * @return A DTO containing the correct answer and explanation if found and authorized.
     * @throws ResponseStatusException If the question with the provided ID does not exist or if the user lacks permission.
     */
    fun getCorrectById(id: Int, user: UserAuthenticateDto?) = tryCatch {
        val q = repository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        val userEntity = getUser(user)
        if (!q.canView(userEntity))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        QuestionGetCorrectDTO(q.correctAnswerData, q.explanation)
    }

    /**
     * Retrieves a list of questions authored by the specified username,
     * filtered by the user's view permissions.
     *
     * @param username The username of the author whose questions are to be retrieved.
     * @param user The authenticated user attempting to view the questions.
     *
     * @return A list of questions that the authenticated user is permitted to view.
     */
    fun getByUsername(username: String, user: UserAuthenticateDto?) = tryCatch {
        val userEntity = getUser(user)
        val questions = repository.getAllByAuthorUsername(username)
        questions.filter { it.canView(userEntity) }.map { converter.toFindDTO(it) }
    }

    /**
     * Updates the question with the given student points. This method handles updates to both the question and the student answers related to it.
     *
     * @param id The unique identifier of the question to be updated.
     * @param roomId The unique identifier of the quiz room containing the question.
     * @param dto The data transfer object containing the new details for the question.
     * @param userDto Optional user authentication data transfer object.
     * @retyrn A QuestionWithPointReasignDTO object.
     */
    fun updateWithStudentPoints(id: Int, roomId: Int, dto: QuestionUpdateDTO, userDto: UserAuthenticateDto?) =
        tryCatch {
            setUpAnswersStatus()

            val q = repository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
            val room = quizroomRepo.findById(roomId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
            val ret = repository.saveAndFlush(converter.merge(q, dto))

            var newCorrectAnswers: Set<String> = emptySet()
            var newOptions: Set<String> = emptySet()

            if (q.questionType == QuestionType.LEGACY) {
                newOptions = ret.possibleAnswersData.split(
                    "{\"option1\":\"", "\",\"option2\":\"", "\",\"option3\":\"", "\",\"option4\":\"", "\"}"
                ).toSortedSet()
                newOptions.remove("")

                newCorrectAnswers =
                    ret.correctAnswerData.split("\",\"", "[\"", "\"]").filter { newOptions.contains(it) }.toSortedSet()
            } else if (q.questionType == QuestionType.MULTICHOICE || q.questionType == QuestionType.TRUEFALSE) {

                newOptions = (ret.possibleAnswersData.removeSurrounding("[", "]")
                    .takeIf(String::isNotEmpty)
                    ?.split(",")?.toSortedSet()
                    ?: emptyList()).toSortedSet()

                newCorrectAnswers = (ret.correctAnswerData.removeSurrounding("[", "]")
                    .takeIf(String::isNotEmpty)
                    ?.split(",")?.filter { newOptions.contains(it) }?.toSortedSet()
                    ?: emptyList()).toSortedSet()
            }

            val studs = saRepo.getAllByQuizroomAndQuestion(room, q)
            val studentsAdd: ArrayList<String> = arrayListOf()
            val studentsSub: ArrayList<String> = arrayListOf()

            studs.forEach {
                var studentAns: Set<String> = emptySet()

                if (q.questionType == QuestionType.LEGACY) {
                    studentAns = it.answerData.split(
                        "{\"answer1\": \"", "\",\"answer2\": \"", "\",\"answer3\": \"", "\",\"answer4\": \"", "\"}"
                    ).toSortedSet()
                    studentAns.remove("")
                } else if (q.questionType == QuestionType.TRUEFALSE || q.questionType == QuestionType.MULTICHOICE) {
                    studentAns = (it.answerData.removeSurrounding("[", "]")
                        .takeIf(String::isNotEmpty)
                        ?.split(",")?.toSortedSet()
                        ?: emptyList()).toSet()
                }

                val filteredStudentAns = studentAns.filter { a -> newOptions.contains(a) }.toSortedSet()

                if (it.isCorrect == AnswerStatusType.CORRECT && filteredStudentAns != newCorrectAnswers) {
                    studentsSub.add(it.student.username)
                    val stud = quizroomStudentRepo.findByQuizroomAndStudent(room, it.student)

                    saRepo.saveAndFlush(
                        StudentAnswer(
                            it.answerData, AnswerStatusType.INCORRECT, it.quizroom,it.student, it.question, it.id
                        )
                    )
                    quizroomStudentRepo.saveAndFlush(QuizroomStudent(stud.student, stud.quizroom, stud.points - 1, stud.id))
                    if (dto.lesson != null) {
                        studentModuleRepo.saveAndFlush(
                            StudentModule(
                                lessonRepo.findById(dto.lesson).get(),
                                stud.quizroom.quiz.module,
                                stud.student,
                                (stud.points * 100) / stud.quizroom.quiz.numOfQuestions,
                                null,
                                null,
                                false,
                                false,
                                false,
                                "",
                                emptyList(),
                                studentModuleRepo.getByStudentModule(
                                    stud.student, stud.quizroom.quiz.module, lessonRepo.findById(dto.lesson).get()
                                )?.id ?: 0
                            )
                        )
                    }
                } else if (it.isCorrect == AnswerStatusType.INCORRECT && filteredStudentAns == newCorrectAnswers) {

                    val stud = quizroomStudentRepo.findByQuizroomAndStudent(room, it.student)
                    studentsAdd.add(it.student.username)

                    saRepo.saveAndFlush(
                        StudentAnswer(it.answerData, AnswerStatusType.CORRECT, it.quizroom, it.student, it.question, it.id)
                    )

                    quizroomStudentRepo.saveAndFlush(
                        QuizroomStudent(stud.student, stud.quizroom, stud.points + 1, stud.id)
                    )

                    if (dto.lesson != null) {
                        studentModuleRepo.saveAndFlush(
                            StudentModule(
                                lessonRepo.findById(dto.lesson).get(),
                                stud.quizroom.quiz.module,
                                stud.student,
                                (stud.points * 100) / stud.quizroom.quiz.numOfQuestions,
                                null,
                                null,
                                false,
                                false,
                                false,
                                "",
                                emptyList(),
                                studentModuleRepo.getByStudentModule(
                                    stud.student, stud.quizroom.quiz.module, lessonRepo.findById(dto.lesson).get()
                                )?.id ?: 0
                            )
                        )
                    }
                }
            }

            QuestionWithPointReasignDTO(
                ret.id, ret.questionData, ret.possibleAnswersData, ret.correctAnswerData, ret.timeLimit,
                ret.author.username, ret.singleAnswer, ret.questionType, studentsAdd, studentsSub
            )
        }


    /**
     * Get list of questions with at least one of the given topics
     * @property topicIds list of topic ids
     * @property userDto user retrieving the list
     * @return List of [QuestionGetDTO]
     */
    fun findByTopics(topicIds: List<Int>, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        repository.findByTopics(topicIds).map { converter.toGetDTO(it) }
    }

    /**
     * Add a topic to the question (teacher only)
     * @property id question id
     * @property topicId topic id to be added
     * @property userDto teacher adding the topic
     * @return ModuleGetDTO
     */
    fun addTopic(id: Int, topicId: Int, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { question, _ ->
        val topic = topicRepository.getReferenceById(topicId)
        questionTopicRepository.saveAndFlush(QuestionTopic(question, topic))
        converter.toGetDTO(question)
    }

    /**
     * Remove a topic from the question (teacher only)
     * @property id question id
     * @property topicId question id to be removed
     * @property userDto teacher removing the topic
     * @return QuestionGetDTO
     */
    @Transactional
    fun removeTopic(id: Int, topicId: Int, userDto: UserAuthenticateDto?) =
        checkEditAccess(id, userDto) { question, _ ->
            val topic = topicRepository.getReferenceById(topicId)
            questionTopicRepository.deleteByQuestionAndTopic(question, topic)
            null
        }


    /**
     * Retrieves the list of topics associated with a given question.
     *
     * @param id The unique identifier of the question for which to fetch topics.
     * @param userDto The user authentication information of the requester, can be nullable.
     * If null, the function ensures that only authorized users can access the question's topics.
     * @return A list of TopicFindDTOs.
     */
    fun getQuestionTopics(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { question, _ ->
        question.topics.map { converter.toGetDTO(it.topic) }
    }

    /**
     * Retrieves a list of questions associated with the given subject IDs.
     *
     * @param subjectsId A list of subject IDs to filter the questions by.
     * @param userDto The user authentication information of the requester, can be nullable.
     * If null, the function ensures that only authorized users can access the questions.
     * @return A list of questions that match the specified subject IDs, converted to DTO format.
     */
    fun findBySubjects(subjectsId: List<Int>, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        repository.findBySubjects(subjectsId).map { converter.toGetDTO(it) }
    }

    /**
     * Retrieves the subjects related to a specific question based on the question ID,
     * ensuring that the user has the necessary view access.
     *
     * @param id The unique identifier of the question whose subjects are to be retrieved.
     * @param userDto The user authentication information of the requester, can be nullable.
     *                If null, the function ensures that only authorized users can access the question's subjects.
     * @return A list of SubjectGetDTOs representing the subjects associated with the question.
     */
    fun getQuestionSubjects(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { question, _ ->
        question.subjects.map { converter.toGetDTO(it.subject) }
    }

    /**
     * Adds a subject to a question, ensuring the user has the necessary edit access.
     *
     * @param id The unique identifier of the question to which the subject is to be added.
     * @param subjectId The unique identifier of the subject to add to the question.
     * @param userDto The user authentication data transfer object, needed to verify edit permissions.
     * @return A QuestionGetDTO object.
     */
    fun addSubject(id: Int, subjectId: Int, userDto: UserAuthenticateDto?) =
        checkEditAccess(id, userDto) { question, _ ->
            val subject = subjectRepository.getReferenceById(subjectId)
            questionSubjectRepository.saveAndFlush(QuestionSubject(question, subject))
            converter.toGetDTO(question)
        }

    /**
     * Deletes a specified subject from a given question.
     *
     * @param id The unique identifier of the question whose subject is to be deleted.
     * @param subjectId The unique identifier of the subject to be deleted.
     * @param userDto The user authentication information of the requester, can be nullable.
     *                If null, the function ensures that only authorized users can perform the delete action.
     */
    @Transactional
    fun deleteSubject(id: Int, subjectId: Int, userDto: UserAuthenticateDto?) =
        checkEditAccess(id, userDto) { question, _ ->
            val subject = subjectRepository.getReferenceById(subjectId)
            questionSubjectRepository.deleteByQuestionAndSubject(question, subject)
            null
        }

    /**
     * Updates the status of student answers from UNKNOWN to CORRECT or INCORRECT
     * based on a comparison between the student's answers and the correct answers
     * defined for each question. The status update is performed on all answers found in
     * the repository that currently have a status of UNKNOWN.
     *
     * After determining the correctness of each student's answer, the method updates
     * the answer's status in the repository and persists the changes.
     */
    private fun setUpAnswersStatus() {
        val allAnswers = saRepo.findAll().filter { it.isCorrect == AnswerStatusType.UNKNOWN }

        if (allAnswers.isNotEmpty()) {
            allAnswers.forEach {
                var correctAnswers: Set<String> = emptySet()
                val options: Set<String>
                var studAnswers: Set<String> = emptySet()

                if (it.question.questionType == QuestionType.LEGACY) {
                    options = it.question.possibleAnswersData.split(
                        "{\"option1\":\"", "\",\"option2\":\"", "\",\"option3\":\"", "\",\"option4\":\"", "\"}"
                    ).toSortedSet()
                    options.remove("")

                    correctAnswers =
                        it.question.correctAnswerData.split("\",\"", "[\"", "\"]").filter { a -> options.contains(a) }
                            .toSortedSet()
                    correctAnswers.remove("")

                    studAnswers =
                        it.answerData.split(
                            "{\"answer1\": \"", "\",\"answer2\": \"", "\",\"answer3\": \"", "\",\"answer4\": \"", "\"}"
                        ).toSortedSet()
                    studAnswers.remove("")
                } else if (it.question.questionType == QuestionType.MULTICHOICE || it.question.questionType == QuestionType.TRUEFALSE) {
                    options = (it.question.possibleAnswersData.removeSurrounding("[", "]")
                        .takeIf(String::isNotEmpty)
                        ?.split(",")?.toSortedSet()
                        ?: emptyList()).toSortedSet()

                    correctAnswers = (it.question.correctAnswerData.removeSurrounding("[", "]")
                        .takeIf(String::isNotEmpty)
                        ?.split(",")?.filter { a -> options.contains(a) }?.toSortedSet()
                        ?: emptyList()).toSortedSet()

                    studAnswers = (it.answerData.removeSurrounding("[", "]")
                        .takeIf(String::isNotEmpty)
                        ?.split(",")?.toSortedSet()
                        ?: emptyList()).toSet()
                }

                if (studAnswers == correctAnswers)
                    saRepo.saveAndFlush(
                        StudentAnswer(
                            it.answerData, AnswerStatusType.CORRECT, it.quizroom, it.student, it.question, it.id
                        )
                    )
                else
                    saRepo.saveAndFlush(
                        StudentAnswer(
                            it.answerData, AnswerStatusType.INCORRECT, it.quizroom, it.student, it.question, it.id
                        )
                    )
            }
        }

        return
    }

}