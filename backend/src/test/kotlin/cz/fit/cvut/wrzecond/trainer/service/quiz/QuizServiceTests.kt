package cz.fit.cvut.wrzecond.trainer.service.quiz

import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.dto.UserFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizGetDTO
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.ModuleDifficulty
import cz.fit.cvut.wrzecond.trainer.entity.ModuleType
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quiz
import cz.fit.cvut.wrzecond.trainer.repository.LogRepository
import cz.fit.cvut.wrzecond.trainer.repository.ModuleRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizQuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizRepo
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
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
import cz.fit.cvut.wrzecond.trainer.service.LogService


@SpringBootTest(classes = [QuizService::class])
class QuizServiceTests (
    @MockBean val repository: QuizRepo,
    @MockBean val questionRepo: QuestionRepo,
    @MockBean val moduleRepo: ModuleRepository,
    @MockBean val quizQuestionRepo: QuizQuestionRepo,
    @MockBean val logRepo: LogRepository,
    @MockBean val logService: LogService,
    @MockBean val userRepo: UserRepository,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    service: QuizService
): StringSpec({



    val userDto = UserAuthenticateDto(1, "testuser", "client","ipAddress")
    //val unknownDto = UserAuthenticateDto(2, "unknownUser", "clientUnknown", "ipUnknown")
    val userDummy = User("testsecret", userDto.username, "name", Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),1)

    given(userRepo.getByUsername(userDto.username)).willReturn(userDummy)

    val module1 = Module("quiz", ModuleType.QUIZ, "", ModuleDifficulty.EASY, true, false,
        false, false, 0, "", Timestamp.from(Instant.now()),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),userDummy, 1)
    val module2 = Module("quiz", ModuleType.QUIZ, "", ModuleDifficulty.EASY, true, false,
        false, false, 0, "", Timestamp.from(Instant.now()),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),userDummy, 2)
    val quiz1 = Quiz(1,"quiz", 1, emptyList(), emptyList(), module1, 1)
    val quiz2 = Quiz(1,"quiz", 1, emptyList(), emptyList(), module2, 2)


    val quiz1DtoF = QuizFindDTO(quiz1.id, quiz1.name, quiz1.numOfQuestions, quiz1.numOfAttempts)
    val quiz2DtoF = QuizFindDTO(quiz2.id, quiz2.name, quiz2.numOfQuestions, quiz2.numOfAttempts)

    val quiz1DtoG = QuizGetDTO(quiz1.id, quiz1.name, quiz1.numOfQuestions, quiz1.numOfAttempts, emptyList())
    //val quiz2DtoG = QuizGetDTO(quiz2.id, quiz2.name, quiz2.numOfQuestions, quiz2.numOfAttempts, emptyList())

    beforeTest { // Reset counters
        reset(repository)
    }

    "getAll" {
        given(repository.findAll(ArgumentMatchers.any<Sort>())).willReturn(listOf(quiz1, quiz2))

        service.findAll(userDto) shouldBe listOf(quiz1DtoF, quiz2DtoF)

        verify(repository).findAll(ArgumentMatchers.any<Sort>())
    }

    "getAllEmpty" {
        given(repository.findAll(ArgumentMatchers.any<Sort>())).willReturn(emptyList())

        service.findAll(userDto) shouldBe emptyList()

        verify(repository).findAll(ArgumentMatchers.any<Sort>())
    }


    "getById" {
        given(repository.getReferenceById(1)).willReturn(quiz1)

        service.getByID(1,userDto) shouldBe quiz1DtoG

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

    "getByModule" {
        given(moduleRepo.existsById(1)).willReturn(true)
        given(moduleRepo.findById(1)).willReturn(Optional.of(module1))
        given(repository.existsByModule(module1)).willReturn(true)
        given(repository.getByModule(module1)).willReturn(quiz1)

        service.getQuizByModuleId(1, userDto) shouldBe quiz1DtoG

        verify(repository).getByModule(module1)
    }

    "getByModuleNotFound" {
        given(repository.existsByModule(module2)).willReturn(false)
        given(repository.getByModule(module2)).willReturn(null)

        val e = shouldThrow<ResponseStatusException> {
            service.getQuizByModuleId(1,userDto)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND

    }


})