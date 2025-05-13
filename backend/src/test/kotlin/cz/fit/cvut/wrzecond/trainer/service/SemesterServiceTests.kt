package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.mockito.BDDMockito.doNothing
import org.mockito.BDDMockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [SemesterService::class, ConverterService::class])
class SemesterServiceTests (
    @MockBean val repository: SemesterRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val userRepository: UserRepository,
    @MockBean val courseRepository: CourseRepository,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val smRepository: StudentModuleRepository,
    @MockBean val smrqRepository: StudentModuleRequestRepository,
    @MockBean val subjectRepository: SubjectRepository,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val questionRepo: QuestionRepo,
    @MockBean val quizroomRepo: QuizroomRepo,
    service: SemesterService,
    @MockBean val fileService: FileService,
    @MockBean val logRepository: LogRepository,
    @MockBean val logService: LogService,
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val lessonModuleRepository: LessonModuleRepository,
    @MockBean val userService: UserService,

    @Autowired val converterService: ConverterService
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

    val from = Timestamp(666)
    val until = Timestamp(666)
    val semester1 = Semester("Test semester", from, until, emptyList(), 1)
    val scdto = SemesterCreateDTO("Test semester", from, until)
    val sfdto = SemesterFindDTO(1, "Test semester", from, until)
    val sgdto = SemesterGetDTO(1, "Test semester", from, until)
    val sudto = SemesterUpdateDTO("Update semester", from, until)

    beforeTest { // Reset counters
        reset(repository)
    }

    "create" {
        given(repository.saveAndFlush(any<Semester>())).willReturn(semester1)

        service.create(scdto, adminAuthDto) shouldBe sgdto

        verify(repository).saveAndFlush(any())
    }

    "create_403"{
        given(repository.saveAndFlush(semester1)).willReturn(semester1)
        val e = shouldThrow<ResponseStatusException> {
            service.create(scdto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    "update" {
        given(repository.getReferenceById(semester1.id)).willReturn(semester1)
        given(repository.saveAndFlush(any<Semester>())).willReturn(sudto.code?.let { semester1.copy(code = it) })

        service.update(semester1.id, sudto, adminAuthDto) shouldBe sgdto.copy(code = sudto.code!!)

        verify(repository).saveAndFlush(any())
    }

    "update_403" {
        given(repository.getReferenceById(semester1.id)).willReturn(semester1)
        val e = shouldThrow<ResponseStatusException> {
            service.update(semester1.id, sudto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    "delete" {
        given(repository.getReferenceById(semester1.id)).willReturn(semester1)
        doNothing().`when`(repository).delete(any())

        service.delete(semester1.id, adminAuthDto)

        verify(repository).getReferenceById(semester1.id)
        verify(repository).delete(semester1)
    }

    "delete_403" {
        given(repository.getReferenceById(semester1.id)).willReturn(semester1)
        val e = shouldThrow<ResponseStatusException> {
            service.delete(semester1.id, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
        verify(repository).getReferenceById(semester1.id)
    }

    val course = Course("PA1", "PA1", true, "secret1", null, semester1, emptyList(), emptyList(), 1)
    val scrdto = SemesterCourseReadDTO(1, "PA1", "PA1", null, sfdto)
    val semesterCourse = semester1.copy(courses = listOf(course))

    "findSemesterCourses" {
        given(repository.getReferenceById(semester1.id)).willReturn(semesterCourse)
        given(courseRepository.findAll(Sort.unsorted())).willReturn(listOf(course))

        service.findSemesterCourses(semester1.id, adminAuthDto) shouldBe listOf(scrdto)

        verify(repository).getReferenceById(semester1.id)
        verify(courseRepository).findAll(Sort.unsorted())
    }

    "findSemesterCourses_403" {
        given(repository.getReferenceById(semester1.id)).willReturn(semesterCourse)
        val e = shouldThrow<ResponseStatusException> {
            service.findSemesterCourses(semester1.id, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
        verify(repository).getReferenceById(semester1.id)
    }

    val subject = Subject("PA1", "PA1", listOf(course), emptyList(), emptyList(), 1)
    val sfdto2 = SubjectFindDTO(1, "PA1", "PA1")
    val semesterSubject = semester1.copy(courses = listOf(course))

    "findSemesterSubjects" {
        given(repository.getReferenceById(semester1.id)).willReturn(semesterSubject)
        given(subjectRepository.findAll(Sort.unsorted())).willReturn(listOf(subject))

        service.findSemesterSubjects(semester1.id, adminAuthDto) shouldBe listOf(sfdto2)

        verify(repository).getReferenceById(semester1.id)
        verify(subjectRepository).findAll(Sort.unsorted())
    }

    "findSemesterSubjects_403" {
        given(repository.getReferenceById(semester1.id)).willReturn(semesterSubject)
        val e = shouldThrow<ResponseStatusException> {
            service.findSemesterSubjects(semester1.id, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
        verify(repository).getReferenceById(semester1.id)
    }
})