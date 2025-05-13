package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleTestEditDTO
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.entity.code.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.code.CodeModuleTestRepository
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityNotFoundException
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant


@SpringBootTest(classes = [TemplateService::class, ConverterService::class])
class TemplateServiceTests(
    @MockBean val repository: TemplateRepository,
    @MockBean val logRepository: LogRepository,
    @MockBean val cmtRepository: CodeModuleTestRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val userRepository: UserRepository,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val smRepository: StudentModuleRepository,
    @MockBean val smrqRepository: StudentModuleRequestRepository,
    @MockBean val semesterRepository: SemesterRepository,
    @MockBean val quizroomRepo: QuizroomRepo,
    @MockBean val questionRepo: QuestionRepo,
    @MockBean val subjectRepository: SubjectRepository,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val fileService: FileService,
    @MockBean val lessonRepository: LessonRepository,
    @Autowired val converterService: ConverterService,
    service: TemplateService
): StringSpec({

    val userDto = UserFindDTO(1, "testuser", "Test user")
    val userDummy = User(
        "testsecret", userDto.username, userDto.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),1
    )
    val adminDto = UserFindDTO(2, "testadmin", "Test admin")
    val adminDummy = User("testsecret", adminDto.username, adminDto.name, Timestamp.from(Instant.now()), false, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 1, true)
    val time = Timestamp.from(Instant.now())
    val userAuthDto = UserAuthenticateDto(userDto.id, userDto.username, "", "")
    val adminAuthDto = UserAuthenticateDto(adminDto.id, adminDto.username, "", "")
    val cmtedto = CodeModuleTestEditDTO(1, 1, "testTest", 1, "testInput", 100, true, false, false)
    val templateDummy = Template("testtemplate", CodeType.TEST_IO, InteractionType.EDITOR, LibraryType.LIB_CPP, EnvelopeType.ENV_CUSTOM,"TestCustomEnvelope", "testAnswer", 10240, time, emptyList(), userDummy, 1)
    val cmtDummy = CodeModuleTest("testTest", 1, "testInput", 1, true, false, false, templateDummy, null,1)
    val tcdto = TemplateCreateDTO( "testtemplate", CodeType.TEST_IO, LibraryType.LIB_CPP, InteractionType.EDITOR, EnvelopeType.ENV_CUSTOM,"TestCustomEnvelope", "testAnswer", 10240, listOf(cmtedto), 1)
    val tfdto = TemplateFindDTO(1, "testtemplate", CodeType.TEST_IO, LibraryType.LIB_CPP, InteractionType.EDITOR, EnvelopeType.ENV_CUSTOM,"TestCustomEnvelope", "testAnswer", 10240, emptyList(), time,userDto)

    val user = userDummy.copy()
    val admin = adminDummy.copy()

    given(authorizationService.isTrusted(user)).willReturn(false)
    given(authorizationService.isTrusted(admin)).willReturn(true)

    given(userRepository.getByUsername(userDto.username)).willReturn(user)
    given(userRepository.getByUsername(adminDto.username)).willReturn(admin)

    given(userRepository.getReferenceById(userDto.id)).willReturn(user)
    given(userRepository.getReferenceById(adminDto.id)).willReturn(admin)


    "getByID" {
        given(repository.getReferenceById(1)).willReturn(templateDummy)

        service.getByID(1, adminAuthDto) shouldBe tfdto

        verify(repository).getReferenceById(1)
    }

    "getByID_403" {
        given(userRepository.getByUsername(userDto.username)).willReturn(user.copy(id=3))
        val e = shouldThrow<ResponseStatusException> {
            service.getByID(1, userAuthDto.copy(id=3))
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
        verify(userRepository).getByUsername(userDto.username)
    }

    "create" {
        given(repository.saveAndFlush(any<Template>())).willReturn(templateDummy)

        service.create(tcdto, adminAuthDto) shouldBe tfdto

        verify(repository).saveAndFlush(any<Template>())
    }

    "create_403" {
        val e = shouldThrow<ResponseStatusException> {
            service.create(tcdto, userAuthDto) shouldBe tfdto
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    "findAll" {
        given(repository.findAll()).willReturn(listOf(templateDummy))

        service.findAll(adminAuthDto) shouldBe listOf(tfdto)

        verify(repository).findAll()
    }

    "findAll_403" {
        val e = shouldThrow<ResponseStatusException> {
            service.findAll(userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    "delete" {
        given(repository.getReferenceById(1)).willReturn(templateDummy)
        doNothing().`when`(repository).delete(org.mockito.kotlin.any())

        service.delete(1, adminAuthDto)

        verify(repository).delete(templateDummy)
    }

})


