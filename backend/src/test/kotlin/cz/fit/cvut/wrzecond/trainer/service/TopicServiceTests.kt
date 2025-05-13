package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.TopicCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.TopicFindDTO
import cz.fit.cvut.wrzecond.trainer.entity.Topic
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.TopicRepository
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.impl.recording.PermanentMockerFactory
import org.mockito.BDDMockito.doNothing
import org.mockito.BDDMockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [TopicService::class, ConverterService::class])
class TopicServiceTests (
    @MockBean val repository: TopicRepository,
    @MockBean val logRepository: LogRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val smRepository: StudentModuleRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val smrqRepository: StudentModuleRequestRepository,
    @MockBean val semesterRepository: SemesterRepository,
    @MockBean val quizroomRepo: QuizroomRepo,
    @MockBean val questionRepo: QuestionRepo,
    @MockBean val subjectRepository: SubjectRepository,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val fileService: FileService,
    @MockBean val lessonRepository: LessonRepository,
    @Autowired val converterService: ConverterService,
    service: TopicService
): StringSpec({
    val userDto = UserFindDTO(1, "testuser", "Test user")
    val userAuthDto = UserAuthenticateDto(userDto.id, userDto.username, "", "")
    val adminDto = UserFindDTO(1, "testadmin", "Test admin")
    val adminAuthDto = UserAuthenticateDto(adminDto.id, adminDto.username, "", "")
    val userDummy = User(
        "testsecret", userDto.username, userDto.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),1
    )
    val adminDummy = User(
        "testsecret", userDto.username, userDto.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),1, true
    )

    val user = userDummy.copy()
    given(userRepository.getByUsername(userDto.username)).willReturn(user)

    val admin = adminDummy.copy()
    given(userRepository.getByUsername(adminDto.username)).willReturn(admin)

    given(authorizationService.isTrusted(user)).willReturn(false)
    given(authorizationService.isTrusted(admin)).willReturn(true)

    val topic1 = Topic("Test topic", emptyList(), emptyList(), 1)

    val tcdto = TopicCreateDTO(topic1.name)
    val tfdto = TopicFindDTO(1, topic1.name)
    val tudto = tcdto.copy(name = "Updated topic")

    beforeTest { // Reset counters
        reset(repository)
    }

    "create" {
        given(repository.saveAndFlush(any<Topic>())).willReturn(topic1)

        service.create(tcdto, adminAuthDto) shouldBe tfdto

        verify(repository).saveAndFlush(any())
    }

    "create_403" {
        val e = shouldThrow<ResponseStatusException> {
            service.create(tcdto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    "update" {
        given(repository.getReferenceById(topic1.id)).willReturn(topic1)
        given(repository.saveAndFlush(any<Topic>())).willReturn(topic1.copy(name = tudto.name))

        service.update(topic1.id, tudto, adminAuthDto) shouldBe tfdto.copy(name = tudto.name)

        verify(repository).getReferenceById(topic1.id)
        verify(repository).saveAndFlush(any())
    }

    "update_403" {
        given(repository.getReferenceById(topic1.id)).willReturn(topic1)
        val e = shouldThrow<ResponseStatusException> {
            service.update(topic1.id, tudto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
        verify(repository).getReferenceById(topic1.id)
    }

    "delete" {
        given(repository.getReferenceById(topic1.id)).willReturn(topic1)
        doNothing().`when`(repository).delete(any())

        service.delete(topic1.id, adminAuthDto)

        verify(repository).getReferenceById(topic1.id)
        verify(repository).delete(topic1)
    }

    "delete_403" {
        given(repository.getReferenceById(topic1.id)).willReturn(topic1)
        val e = shouldThrow<ResponseStatusException> {
            service.delete(topic1.id, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
        verify(repository).getReferenceById(topic1.id)
    }
})