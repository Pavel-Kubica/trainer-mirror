package cz.fit.cvut.wrzecond.trainer.service.quiz

import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.dto.UserFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizroomFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizroomGetDTO
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.ModuleDifficulty
import cz.fit.cvut.wrzecond.trainer.entity.ModuleType
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quiz
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Quizroom
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionInstanceRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomStudentRepo
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
import cz.fit.cvut.wrzecond.trainer.service.LogService


@SpringBootTest(classes = [QuizroomService::class])
class QuizroomServiceTests (
    @MockBean val repository: QuizroomRepo,
    @MockBean val quizRepo: QuizRepo,
    @MockBean val quizroomStudentRepo: QuizroomStudentRepo,
    @MockBean val userRepo: UserRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val logRepo: LogRepository,
    @MockBean val logService: LogService,
    @MockBean val moduleRepo : ModuleRepository,
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val weekRepository: WeekRepository,
    @MockBean val cuRepository: CourseUserRepository,
    @MockBean val qiRepo : QuestionInstanceRepo,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    service: QuizroomService
): StringSpec({


    val userDto = UserAuthenticateDto(1, "testuser", "client","ipAddress")
    //val unknownDto = UserAuthenticateDto(2, "unknownUser", "clientUnknown", "ipUnknown")
    val userDummy = User("testsecret", userDto.username, "name", Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),1)

    given(userRepo.getByUsername(userDto.username)).willReturn(userDummy)

    val module = Module("quiz",ModuleType.QUIZ, "",ModuleDifficulty.EASY, true, false,
        false, false, 0, "", Timestamp.from(Instant.now()),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),userDummy, 1)
    val quiz = Quiz(1,"quiz", 1, emptyList(), emptyList(), module, 1)
    val room1 = Quizroom("testuser","123456", 0, 0, true, "waitingRoom", quiz, emptyList(), 1)
    val room2 = Quizroom("testuser2","000000", 0, 0, true, "waitingRoom", quiz, emptyList(), 2)

    val room1Dto = QuizroomFindDTO(room1.id, room1.createdBy, room1.roomPassword, room1.quiz.id, emptyList(), 0, room1.roomState, room1.timeLeft, room1.quizState)
    val room2Dto = QuizroomFindDTO(room2.id, room2.createdBy, room2.roomPassword, room2.quiz.id, emptyList(), 0, room2.roomState, room2.timeLeft, room2.quizState)

    val room1DtoGet = QuizroomGetDTO(room1.id, room1.createdBy, room1.roomPassword, room1.quiz.id, emptyList(), 0, room1.roomState, room1.timeLeft, room1.quizState)

    beforeTest { // Reset counters
        reset(repository)
    }


    "getAll" {
        given(repository.findAll(ArgumentMatchers.any<Sort>())).willReturn(listOf(room1, room2))

        service.findAll(userDto) shouldBe listOf(room1Dto, room2Dto)

        verify(repository).findAll(ArgumentMatchers.any<Sort>())
    }

    "getAllEmpty" {
        given(repository.findAll(ArgumentMatchers.any<Sort>())).willReturn(emptyList())

        service.findAll(userDto) shouldBe emptyList()

        verify(repository).findAll(ArgumentMatchers.any<Sort>())
    }

    "getAllByPassword" {
        given(repository.existsByRoomPassword("123456")).willReturn(true)
        given(repository.getAllByRoomPasswordAndRoomState("123456", true)).willReturn(listOf(room1))

        service.getByPassword("123456", userDto) shouldBe listOf(room1Dto)

        verify(repository).getAllByRoomPasswordAndRoomState("123456", true)
    }

    "getAllByUserEmpty" {
        given(repository.existsByRoomPassword("666666")).willReturn(true)
        given(repository.getAllByRoomPasswordAndRoomState("666666", true)).willReturn(emptyList())

        service.getByPassword("666666",userDto) shouldBe emptyList()

        verify(repository).getAllByRoomPasswordAndRoomState("666666", true)
    }

    "getById" {
        given(repository.getReferenceById(1)).willReturn(room1)

        service.getByID(1,userDto) shouldBe room1DtoGet

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

})