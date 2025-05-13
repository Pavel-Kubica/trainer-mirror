package cz.fit.cvut.wrzecond.trainer.service.quiz

import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.dto.UserFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.StudAnswerFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.StudAnswerGetDTO
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.entity.quiz.*
import cz.fit.cvut.wrzecond.trainer.repository.LessonRepository
import cz.fit.cvut.wrzecond.trainer.repository.StudentModuleRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.repository.quiz.*
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityNotFoundException
import org.mockito.Answers
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.given
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant
import java.util.*


@SpringBootTest(classes = [StudAnswerService::class])
class StudAnswerServiceTests (
    @MockBean val repository: StudAnswerRepo,
    @MockBean val quizroomRepo: QuizroomRepo,
    @MockBean val questionRepo: QuestionRepo,
    @MockBean val quizroomStudentRepo: QuizroomStudentRepo,
    @MockBean val studentModuleRepo: StudentModuleRepository,
    @MockBean val lessonRepo: LessonRepository,
    @MockBean val quizQuestionRepo: QuizQuestionRepo,
    @MockBean val userRepo: UserRepository,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    service: StudAnswerService
): StringSpec({

    val userDto = UserAuthenticateDto(1, "testuser", "client","ipAddress")
    //val unknownDto = UserAuthenticateDto(2, "unknownUser", "clientUnknown", "ipUnknown")
    val userDummy = User("testsecret", userDto.username, "name", Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),1)

    given(userRepo.getByUsername(userDto.username)).willReturn(userDummy)

    var course = Course("quizCourse", "qc",true, "", null, null, emptyList(), emptyList(), 1)
    var week = Week("week", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()), course, emptyList(), 1)
    val lesson = Lesson("lesson", false, 1, Timestamp.from(Instant.now()), Timestamp.from(Instant.now()), "quizLesson", LessonType.INDIVIDUAL_TASK, "", week, emptyList(), emptyList(),emptyList(),1)
    course = course.copy(weeks = listOf(week))
    week = week.copy(lessons = listOf(lesson))

    val module = Module("quiz", ModuleType.QUIZ, "", ModuleDifficulty.EASY, true, false,
        false, false, 0, "", Timestamp.from(Instant.now()),
        emptyList(), emptyList(), emptyList(),emptyList(), emptyList(), emptyList(), emptyList(), userDummy, 1)
    val quiz = Quiz(1,"quiz", 1, emptyList(), emptyList(), module, 1)
    val room1 = Quizroom("testuser","123456", 0, 0, true, "waitingRoom", quiz, emptyList(), 1)
    //val room2 = Quizroom("testuser2","000000", 0, 0, true, "waitingRoom", quiz, emptyList(), 2)

    val quizroomStudent = QuizroomStudent(userDummy, room1, 1)
    val quizroomStudentSubtract = QuizroomStudent(quizroomStudent.student, quizroomStudent.quizroom, quizroomStudent.points-1)
    val studentModule = StudentModule(lesson, module, userDummy, null, null, null, true, true, true, null, emptyList(),1)

    val questionLegacy = Question("Legacy", "[a,b,c,d]","[a]", "",true,
        userDummy, 10, QuestionType.LEGACY, emptyList(), emptyList(), emptyList(), emptyList())
    val questionTF = Question("TF", "[True, False]","[True]", "",true,
        userDummy, 10, QuestionType.TRUEFALSE, emptyList(), emptyList(), emptyList(), emptyList())
    val questionMultichoice =  Question("TF", "[Alica, Bob, Cyril]","[Alica, Bob]", "",false,
        userDummy, 10, QuestionType.TRUEFALSE, emptyList(), emptyList(), emptyList(),emptyList())

    val answer1 = StudentAnswer("[]",AnswerStatusType.CORRECT, room1, userDummy, questionLegacy, 1)
    val answer2 = StudentAnswer("[]",AnswerStatusType.CORRECT, room1, userDummy, questionTF, 2)
    val answer3 = StudentAnswer("[]",AnswerStatusType.CORRECT, room1, userDummy, questionMultichoice, 3)

    val answer1DtoF = StudAnswerFindDTO(answer1.id, answer1.quizroom?.id, answer1.student.id, answer1.question.id, answer1.answerData)
    val answer2DtoF = StudAnswerFindDTO(answer2.id, answer2.quizroom?.id, answer2.student.id, answer2.question.id, answer2.answerData)
    val answer3DtoF = StudAnswerFindDTO(answer3.id, answer3.quizroom?.id, answer3.student.id, answer3.question.id, answer3.answerData)

    val answer1DtoG = StudAnswerGetDTO(answer1.id, answer1.quizroom?.id, answer1.student.id, answer1.question.id, answer1.answerData)
   // val answer2DtoG = StudAnswerGetDTO(answer2.id, answer2.quizroom?.id, answer2.student.id, answer2.question.id, answer2.answerData)
    //val answer3DtoG = StudAnswerGetDTO(answer3.id, answer3.quizroom?.id, answer3.student.id, answer3.question.id, answer3.answerData)


    beforeTest {
        reset(repository)
    }

    "getAll" {
        given(repository.findAll(ArgumentMatchers.any<Sort>())).willReturn(listOf(answer1, answer2, answer3))

        service.findAll(userDto) shouldBe listOf(answer1DtoF, answer2DtoF, answer3DtoF)

        verify(repository).findAll(ArgumentMatchers.any<Sort>())
    }

    "getAllEmpty" {
        given(repository.findAll(ArgumentMatchers.any<Sort>())).willReturn(emptyList())

        service.findAll(userDto) shouldBe emptyList()

        verify(repository).findAll(ArgumentMatchers.any<Sort>())
    }


    "getById" {
        given(repository.getReferenceById(1)).willReturn(answer1)

        service.getByID(1,userDto) shouldBe answer1DtoG

        verify(repository).getReferenceById(1)
    }

    "getByIdNotFound" {
        given(repository.getReferenceById(ArgumentMatchers.anyInt())).willThrow(
            JpaObjectRetrievalFailureException(EntityNotFoundException())
        )

        val e = shouldThrow<ResponseStatusException> {
            service.getByID(666, userDto)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    "findAllByRoomAndQuestion" {
        given(questionRepo.existsById(1)).willReturn(true)
        given(quizroomRepo.existsById(1)).willReturn(true)

        given(questionRepo.findById(1)).willReturn(Optional.of(questionLegacy))
        given(quizroomRepo.findById(1)).willReturn(Optional.of(room1))

        given(repository.getAllByQuizroomAndQuestion(room1, questionLegacy)).willReturn(listOf(answer1))

        service.findAllByRoomAndQuestion(1, 1, userDto) shouldBe listOf(answer1DtoF)

        verify(repository).getAllByQuizroomAndQuestion(room1, questionLegacy)
    }

    "getByModuleNotFound" {
        given(questionRepo.findById(1)).willReturn(Optional.empty())
        given(quizroomRepo.findById(1)).willReturn(Optional.empty())

        val e = shouldThrow<ResponseStatusException> {
            service.findAllByRoomAndQuestion(1,1,userDto)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND

    }

})