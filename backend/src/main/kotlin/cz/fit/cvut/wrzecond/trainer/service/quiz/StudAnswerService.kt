package cz.fit.cvut.wrzecond.trainer.service.quiz

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.dto.quiz.StudAnswerCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.StudAnswerFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.StudAnswerGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.StudAnswerUpdateDTO
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.entity.quiz.AnswerStatusType
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionType
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuizroomStudent
import cz.fit.cvut.wrzecond.trainer.entity.quiz.StudentAnswer
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.*
import cz.fit.cvut.wrzecond.trainer.service.IServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class responsible for handling Student Answer operations.
 *
 * @property repository Reference to the repository managing StudentAnswer entities.
 * @property quizroomRepo Reference to the repository managing Quizroom entities.
 * @property questionRepo Reference to the repository managing Question entities.
 * @property quizroomStudentRepo Reference to the repository managing QuizroomStudent entities.
 * @property studentModuleRepo Reference to the repository managing StudentModule entities.
 * @property lessonRepo Reference to the repository managing Lesson entities.
 * @property quizQuestionRepo Reference to the repository managing QuizQuestion entities.
 * @property userRepo Reference to the repository managing User entities.
 */
@Service
class StudAnswerService(
    override val repository: StudAnswerRepo, private val quizroomRepo: QuizroomRepo,
    private val questionRepo: QuestionRepo, private val quizroomStudentRepo: QuizroomStudentRepo,
    private val studentModuleRepo: StudentModuleRepository, private val lessonRepo: LessonRepository,
    private val quizQuestionRepo: QuizQuestionRepo, private val userRepo: UserRepository
) : IServiceImpl<StudentAnswer, StudAnswerFindDTO, StudAnswerGetDTO, StudAnswerCreateDTO, StudAnswerUpdateDTO>(
    repository, userRepo
) {
    override fun create(dto: StudAnswerCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
            createEntityChecks(dto)
            var isCorrect = AnswerStatusType.INCORRECT
            val q = questionRepo.findById(dto.question).get()
            if (q.questionType == QuestionType.LEGACY) {
                val correctAnswers: Set<String>
                val studAnswers: Set<String>

                if (dto.quizroom != null && quizroomRepo.existsById(dto.quizroom) &&
                    quizroomRepo.findById(dto.quizroom).get().quiz.module.type == ModuleType.SELFTEST) {
                    studAnswers = parseAnswers(dto.data)
                    correctAnswers = parseAnswers(q.correctAnswerData)
                } else {
                    studAnswers =
                        dto.data.split(Regex("\"answer[1-4]\": \"(.*?)\"")).filter { it.isNotEmpty() }.toSortedSet()
                    correctAnswers = q.correctAnswerData.split("\",\"", "[\"", "\"]").toSortedSet()
                    correctAnswers.remove("")
                }
                // comparing student answers with correct answers todo VYEXTRAHOVAT
                if (correctAnswers == studAnswers) {
                    isCorrect = AnswerStatusType.CORRECT
                    saveStudentModuleAndQuizroom(dto.quizroom, dto.student, dto.lesson)
                }
            } else if (q.questionType == QuestionType.TRUEFALSE) {
                if (dto.data == q.correctAnswerData) {
                    isCorrect = AnswerStatusType.CORRECT
                    saveStudentModuleAndQuizroom(dto.quizroom, dto.student, dto.lesson)
                }
            } else if (q.questionType == QuestionType.MULTICHOICE) {

                val options = parseAnswers(q.possibleAnswersData)
                val studentAns = parseAnswers(dto.data)
                val refAns = parseAnswers(q.correctAnswerData).filter { options.contains(it) }.toSortedSet()

                if (studentAns == refAns) {
                    isCorrect = AnswerStatusType.CORRECT
                    saveStudentModuleAndQuizroom(dto.quizroom, dto.student, dto.lesson)
                }
            } else {
                if (dto.quizroom == null)
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                saveStudentModuleAndQuizroom(dto.quizroom, dto.student, dto.lesson)
            }
        val studentAnswer = repository.saveAndFlush(converter.toEntity(dto, isCorrect))
        converter.toGetDTO(studentAnswer)
    }

    //should not be possible to edit an old answer unless its from selftest -> no matter what dto is sent old data is preserved
    override fun update(id: Int, dto: StudAnswerUpdateDTO, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { studentAnswer, _ ->
        var studentAnswerFinal = studentAnswer
        if (dto.quizroom != null && studentAnswer.quizroom?.quiz?.module?.type == ModuleType.SELFTEST) {

            val studAnswers = parseAnswers(dto.data)
            val correctAnswers = parseAnswers(studentAnswer.question.correctAnswerData)
            val oldAnswers = parseAnswers(studentAnswer.answerData)

            if (studAnswers == correctAnswers && correctAnswers != oldAnswers) {
                asignPoints(studentAnswer.quizroom.id, studentAnswer.student.id, dto.lesson)
                studentAnswerFinal = converter.merge(studentAnswer, dto, AnswerStatusType.CORRECT)
            } else if (studAnswers != correctAnswers && correctAnswers == oldAnswers) {
                subtractPoints(studentAnswer.quizroom.id, studentAnswer.student.id, dto.lesson)
                studentAnswerFinal = converter.merge(studentAnswer, dto, AnswerStatusType.INCORRECT)
            } else
                studentAnswerFinal = converter.merge(studentAnswer, dto, studentAnswer.isCorrect)
        }
            converter.toGetDTO(studentAnswerFinal)
    }

    override fun delete(id: Int, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { studentAnswer, _ ->
        repository.delete(studentAnswer)
    }

    override fun findAll(userDto: UserAuthenticateDto?): List<StudAnswerFindDTO> = tryCatch{
        val userEntity = try { getUser(userDto) } catch (_: Exception) { null }
        repository.findAll(sort).filter { it.canView(userEntity) }.map { converter.toFindDTO(it) }
    }

    override fun getByID(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { entity, _ -> converter.toGetDTO(entity) }

    fun parseAnswers(data: String): Set<String> =
        data.removeSurrounding("[", "]")
            .takeIf(String::isNotEmpty)
            ?.split(",")
            ?.toSortedSet()
            ?: emptySet()

    /**
     * Retrieves all student answers for a specific quiz room and question.
     *
     * @param question The ID of the question for which the answers are to be retrieved.
     * @param quizroom The ID of the quiz room for which the answers are to be retrieved.
     * @param user The authenticated user DTO for authorization purposes. Can be nullable.
     * @throws ResponseStatusException if the question or quiz room does not exist.
     * @return A list of student answer DTOs that match the specified quiz room and question.
     */
    fun findAllByRoomAndQuestion(question: Int, quizroom: Int, user: UserAuthenticateDto?) = tryCatch {
        val userEntity = try {
            getUser(user)
        } catch (_: Exception) {
            null
        }
        val questionObject = questionRepo.findById(question).orElseThrow{ResponseStatusException(HttpStatus.NOT_FOUND)}
        val quizroomObject = quizroomRepo.findById(quizroom).orElseThrow{ResponseStatusException(HttpStatus.NOT_FOUND)}
        repository.getAllByQuizroomAndQuestion(quizroomObject,questionObject).filter { it.canView(userEntity) }.map { converter.toFindDTO(it) }
    }

    /**
     * Retrieves all student answers for a specific quiz room, question, and student.
     *
     * @param quizroom The ID of the quiz room for which the answers are to be retrieved.
     * @param question The ID of the question for which the answers are to be retrieved.
     * @param student The ID of the student for which the answers are to be retrieved.
     * @param user The authenticated user DTO for authorization purposes. Can be nullable.
     * @throws ResponseStatusException if the quiz room, question, or student does not exist.
     * @return A list of student answer DTOs that match the specified quiz room, question, and student.
     */
    fun findAllByRoomAndQuestionAndStudent(quizroom: Int, question: Int, student: Int, user: UserAuthenticateDto?) =
        tryCatch {
            val userEntity = try {
                getUser(user)
            } catch (_: Exception) {
                null
            }
            val questionObject = questionRepo.findById(question).orElseThrow{ResponseStatusException(HttpStatus.NOT_FOUND)}
            val quizroomObject = quizroomRepo.findById(quizroom).orElseThrow{ResponseStatusException(HttpStatus.NOT_FOUND)}
            val studentObject = userRepo.findById(student).orElseThrow{ResponseStatusException(HttpStatus.NOT_FOUND)}
            repository.getAllByQuizroomAndQuestionAndStudent(quizroomObject,questionObject,studentObject)
                .filter { it.canView(userEntity) }.map { converter.toFindDTO(it) }
        }

    /**
     * Retrieves all student answers for a specific quiz room and student.
     *
     * @param quizroom The ID of the quiz room for which the answers are to be retrieved.
     * @param student The ID of the student for which the answers are to be retrieved.
     * @param user The authenticated user DTO for authorization purposes. Can be nullable.
     * @throws ResponseStatusException If the quiz room or student does not exist.
     * @return A list of student answer DTOs that match the specified quiz room and student.
     */
    fun findAllByRoomAndStudent(quizroom: Int, student: Int, user: UserAuthenticateDto?) = tryCatch {
        val quizroomObject = quizroomRepo.findById(quizroom).orElseThrow{ResponseStatusException(HttpStatus.NOT_FOUND)}
        val studentObject = userRepo.findById(student).orElseThrow{ResponseStatusException(HttpStatus.NOT_FOUND)}
        repository.getAllByQuizroomAndStudent(quizroomObject,studentObject).map { converter.toFindDTO(it) }
    }

    fun createEntityChecks(dto: StudAnswerCreateDTO) {
        if (!lessonRepo.existsById(dto.lesson) ||
            !questionRepo.existsById(dto.question))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val student = userRepo.findById(dto.student).orElseThrow{ResponseStatusException(HttpStatus.NOT_FOUND)}

        if (dto.quizroom != null) {
            val quizroom = quizroomRepo.findById(dto.quizroom).orElseThrow{ResponseStatusException(HttpStatus.NOT_FOUND)}
            val question = questionRepo.findById(dto.question).get()

            if (!quizroomStudentRepo.existsByQuizroomAndStudent(quizroom,student) ||
                repository.existsByQuizroomAndStudentAndQuestion(quizroom,student,question)||
                !quizQuestionRepo.existsByQuestionAndQuiz(question,quizroom.quiz) ||
                (quizroom.quizState != "question" && quizroom.quizState != "ANSWERING" && quizroom.quizState != "CHECKING")
                )
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    fun asignPoints(quizroom: Int?, student: Int, lesson: Int) {
        saveStudentModuleAndQuizroom(quizroom, student, lesson)
    }

    fun subtractPoints(quizroom: Int?, student: Int, lesson: Int) {
        saveStudentModuleAndQuizroom(quizroom, student, lesson, pointsAdjustment = -1)
    }

    fun saveStudentModuleAndQuizroom(quizroom: Int?, student: Int, lesson: Int, pointsAdjustment: Int = 1) {
        if (quizroom != null) {
            val stud = quizroomStudentRepo.findByQuizroomAndStudent(quizroomRepo.findById(quizroom).get(),userRepo.findById(student).get())

            quizroomStudentRepo.saveAndFlush(
                QuizroomStudent(stud.student, stud.quizroom, stud.points + pointsAdjustment, stud.id)
            )
            val lessonInstance = lessonRepo.findById(lesson).get()
            studentModuleRepo.saveAndFlush(
                StudentModule(
                    lessonInstance, stud.quizroom.quiz.module, stud.student,
                    (stud.points * 100) / stud.quizroom.quiz.numOfQuestions,
                    null, null,false,false,false,"",emptyList(),
                    studentModuleRepo.getByStudentModule(
                        stud.student, stud.quizroom.quiz.module, lessonInstance
                    )?.id ?: 0
                )
            )
        }
    }
}