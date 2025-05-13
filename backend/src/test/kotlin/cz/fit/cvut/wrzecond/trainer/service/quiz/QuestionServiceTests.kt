package cz.fit.cvut.wrzecond.trainer.service.quiz

import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuestionFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuestionGetDTO
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Question
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionType
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.*
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityNotFoundException
import org.mockito.Answers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
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

@SpringBootTest(classes = [QuestionService::class])
class QuestionServiceTests(
    @MockBean val questionRepo : QuestionRepo,
    @MockBean val quizroomRepo: QuizroomRepo,
    @MockBean val userRepo: UserRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val topicRepository: TopicRepository,
    @MockBean val qtRepository: QuestionTopicRepository,
    @MockBean val qsRepository: QuestionSubjectsRepo,
    @MockBean val subjectsRepo: SubjectRepository,
    @MockBean val quizQuestionRepo: QuizQuestionRepo,
    @MockBean val quizRepo: QuizRepo,
    @MockBean val saRepo : StudAnswerRepo,
    @MockBean val quizroomStudentRepo : QuizroomStudentRepo,
    @MockBean val studentModuleRepo : StudentModuleRepository,
    @MockBean val logRepository: LogRepository,
    @MockBean val lessonRepo: LessonRepository,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    service: QuestionService
): StringSpec({


    val userDto = UserAuthenticateDto(1, "testuser", "client","ipAddress")
    val unknownDto = UserAuthenticateDto(2, "unknownUser", "clientUnknown", "ipUnknown")
    val userDummy = User("testsecret", userDto.username, "name", Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),1)

    given(userRepo.getByUsername(userDto.username)).willReturn(userDummy)

    val questionLegacy = Question("Legacy", "[a,b,c,d]","[a]", "",true,
        userDummy, 10, QuestionType.LEGACY, emptyList(), emptyList(), emptyList(), emptyList(), 1)
    val questionTF = Question("TF", "[True, False]","[True]", "",true,
        userDummy, 10, QuestionType.TRUEFALSE, emptyList(), emptyList(), emptyList(), emptyList(), 2)
    val questionMultichoice =  Question("TF", "[Alica, Bob, Cyril]","[Alica, Bob]", "",false,
        userDummy.copy(username = "pasekmat"), 10, QuestionType.TRUEFALSE, emptyList(), emptyList(), emptyList(), emptyList(), 3)

    val legDto = QuestionFindDTO(1,questionLegacy.questionData, questionLegacy.possibleAnswersData, questionLegacy.timeLimit,
        questionLegacy.author.username, questionLegacy.singleAnswer, questionLegacy.questionType, emptyList(), emptyList()
    )
    val legDto2 = QuestionGetDTO(1,questionLegacy.questionData, questionLegacy.possibleAnswersData, questionLegacy.timeLimit,
        questionLegacy.author.username, questionLegacy.singleAnswer, questionLegacy.questionType)

    val tfDto = QuestionFindDTO(2,questionTF.questionData, questionTF.possibleAnswersData, questionTF.timeLimit,
        questionTF.author.username, questionTF.singleAnswer, questionTF.questionType,emptyList(), emptyList())
    val multDto = QuestionFindDTO(3,questionMultichoice.questionData, questionMultichoice.possibleAnswersData, questionMultichoice.timeLimit,
        questionMultichoice.author.username, questionMultichoice.singleAnswer, questionMultichoice.questionType,emptyList(), emptyList())


    beforeTest {
        reset(questionRepo)
    }


    "getAll" {
        given(questionRepo.findAll(any<Sort>())).willReturn(listOf(questionLegacy, questionTF, questionMultichoice))

        service.findAll(userDto) shouldBe listOf(legDto,tfDto)

        verify(questionRepo).findAll(any<Sort>())
    }

    "getAllEmpty" {
        given(questionRepo.findAll(any<Sort>())).willReturn(emptyList())

        service.findAll(userDto) shouldBe emptyList()

        verify(questionRepo).findAll(any<Sort>())
    }

    "getAllByUser" {
        given(questionRepo.getAllByAuthorUsername("testuser")).willReturn(listOf(questionLegacy, questionTF))

        service.getByUsername("testuser", userDto) shouldBe listOf(legDto,tfDto)

        verify(questionRepo).getAllByAuthorUsername("testuser")
    }

    "getAllByUserEmpty" {
        given(questionRepo.getAllByAuthorUsername("nonexistent")).willReturn(listOf(questionLegacy, questionTF))

        service.getByUsername("nonexistent",userDto) shouldBe listOf(legDto,tfDto)

        verify(questionRepo).getAllByAuthorUsername("nonexistent")
    }

    "getAllByUserUnauthorized" {

        given(questionRepo.getAllByAuthorUsername("nonexistent")).willReturn(listOf(questionLegacy, questionTF))

        val e = shouldThrow<ResponseStatusException> {
            service.getByUsername("nonexistent", unknownDto)
        }
        e.statusCode shouldBe HttpStatus.UNAUTHORIZED
    }

    "getById" {
        given(questionRepo.getReferenceById(1)).willReturn(questionLegacy)

        service.getByID(1,userDto) shouldBe legDto2

        verify(questionRepo).getReferenceById(1)
    }

    "getByIdNotFound" {
        given(questionRepo.getReferenceById(anyInt())).willThrow(
            JpaObjectRetrievalFailureException(EntityNotFoundException())
        )

        val e = shouldThrow<ResponseStatusException> {
            service.getByID(666, userDto)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND
    }

})