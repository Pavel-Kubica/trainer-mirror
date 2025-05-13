package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.any
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.given
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.sql.Timestamp
import java.time.Instant
import org.mockito.Answers
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.kotlin.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

@SpringBootTest(classes = [TeacherNotesService::class, ConverterService::class])
class TeacherNotesServiceTests (
    @MockBean val repository: TeacherNoteRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val userRepository: UserRepository,
    @Autowired val converterService: ConverterService,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val smRepository: StudentModuleRepository,
    @MockBean val smrqRepository: StudentModuleRequestRepository,
    @MockBean val semesterRepository: SemesterRepository,
    @MockBean val subjectRepository: SubjectRepository,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val questionRepository: QuestionRepo,
    @MockBean val quizroomRepository: QuizroomRepo,
    @MockBean val fileService: FileService,
    @MockBean val subjectGuarantorRepository: SubjectGuarantorRepository,
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val logRepository: LogRepository,
    @MockBean val logService: LogService,
    @MockBean val lessonModuleRepository: LessonModuleRepository,
    @MockBean val userService: UserService,

    service: TeacherNotesService
) : StringSpec({
    val userDto = UserFindDTO(1, "testuser", "Test user")
    val userAuthDto = UserAuthenticateDto(userDto.id, userDto.username, "", "")
    val adminDto = UserFindDTO(2, "testadmin", "Test admin")
    val adminAuthDto = UserAuthenticateDto(adminDto.id, adminDto.username, "", "")
    val userDummy = User(
        "testsecret", userDto.username, userDto.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),  emptyList(),1
    )
    val adminDummy = User(
        "testsecret", adminDto.username, adminDto.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),2, true
    )

    val module1Dummy = Module("Dummy", ModuleType.CODE, "Text of module", ModuleDifficulty.EASY,
        false, false, false, false, 100, null, Timestamp.from(Instant.now()),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),userDummy, 1)

    val note = TeacherNote("Test note", Timestamp.from(Instant.now()), false, module1Dummy, adminDummy, 1)
    val notecdto = TeacherNoteCreateDTO(note.content, note.created)
    val notefdto = TeacherNoteFindDTO(note.id, note.content, note.created, adminDto, note.redacted)
    val noteudto = TeacherNoteUpdateDTO(note.content, note.created)

    beforeTest { // Reset counters
        reset(repository)
    }

    "delete" {
        given(repository.findById(note.id)).willReturn(Optional.of(note))
        given(repository.getReferenceById(note.id)).willReturn(note)
        given(userRepository.getReferenceById(adminDummy.id)).willReturn(adminDummy)
        given(userRepository.getByUsername(adminAuthDto.username)).willReturn(adminDummy)

        service.delete(note.id, adminAuthDto)

        verify(repository).deleteById(note.id)
    }

    "delete_403" {
        given(repository.findById(note.id)).willReturn(Optional.of(note))
        given(repository.getReferenceById(note.id)).willReturn(note)
        given(userRepository.getReferenceById(userDummy.id)).willReturn(userDummy)
        given(userRepository.getByUsername(userAuthDto.username)).willReturn(userDummy)

        val e = shouldThrow<ResponseStatusException> {
            service.delete(note.id, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    "create" {
        given(userRepository.getReferenceById(adminDummy.id)).willReturn(adminDummy)
        given(userRepository.getByUsername(adminAuthDto.username)).willReturn(adminDummy)
        given(moduleRepository.getReferenceById(module1Dummy.id)).willReturn(module1Dummy)
        given(authorizationService.isTrusted(adminDummy)).willReturn(true)
        given(repository.save(any<TeacherNote>())).willReturn(note)

        service.create(module1Dummy.id, notecdto, adminAuthDto) shouldBe notefdto

        verify(repository).save(any())
    }

    "create_403" {
        given(userRepository.getReferenceById(userDummy.id)).willReturn(userDummy)
        given(userRepository.getByUsername(userAuthDto.username)).willReturn(userDummy)
        given(moduleRepository.getReferenceById(module1Dummy.id)).willReturn(module1Dummy)

        val e = shouldThrow<ResponseStatusException> {
            service.create(module1Dummy.id, notecdto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    "update" {
        given(repository.findById(note.id)).willReturn(Optional.of(note))
        given(repository.getReferenceById(note.id)).willReturn(note)
        given(userRepository.getReferenceById(adminDummy.id)).willReturn(adminDummy)
        given(userRepository.getByUsername(adminAuthDto.username)).willReturn(adminDummy
        )
        given(authorizationService.isTrusted(adminDummy)).willReturn(true)
        given(repository.saveAndFlush(any<TeacherNote>())).willReturn(note)

        service.update(note.id, noteudto, adminAuthDto) shouldBe notefdto

        verify(repository).saveAndFlush(any())
    }

    "update_403" {
        given(repository.findById(note.id)).willReturn(Optional.of(note))
        given(repository.getReferenceById(note.id)).willReturn(note)
        given(userRepository.getReferenceById(userDummy.id)).willReturn(userDummy)
        given(userRepository.getByUsername(userAuthDto.username)).willReturn(userDummy)

        val e = shouldThrow<ResponseStatusException> {
            service.update(note.id, noteudto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }
})