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
import jakarta.persistence.EntityNotFoundException
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [CourseService::class, ConverterService::class])
class CourseServiceTests(
    @MockBean val courseRepository: CourseRepository,
    @MockBean val courseUserRepository: CourseUserRepository,
    @MockBean val roleRepository: RoleRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val weekRepository: WeekRepository,
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val subjectRepository: SubjectRepository,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val smRepository: StudentModuleRepository,
    @MockBean val smrqRepository: StudentModuleRequestRepository,
    @MockBean val semesterRepository: SemesterRepository,
    @MockBean val sandboxUserRepository: SandboxUserRepository,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val quizroomRepository: QuizroomRepo,
    @MockBean val questionRepo: QuestionRepo,
    @Autowired val converterService: ConverterService,
    @MockBean val fileService: FileService,
    @MockBean val subjectGuarantorRepository: SubjectGuarantorRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val logRepository: LogRepository,
    service: CourseService
) : StringSpec({
    val userDto = UserFindDTO(1, "testuser", "Test user")
    val userAuthDto = UserAuthenticateDto(userDto.id, userDto.username, "", "")
    val adminDto = UserFindDTO(1, "testadmin", "Test admin")
    val adminAuthDto = UserAuthenticateDto(adminDto.id, adminDto.username, "", "")
    val userDummy = User(
        "testsecret", userDto.username, userDto.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),  emptyList(),1
    )
    val adminDummy = User(
        "testsecret", userDto.username, userDto.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),1, true
    )

    val subject1 = Subject("Programming and Algorithms 1", "BI-PA1.21", emptyList(), emptyList(), emptyList())
    val subject2 = Subject("Programming and Algorithms 2", "BI-PA2.21", emptyList(), emptyList(), emptyList())
    val semester = Semester("B232", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()), emptyList(), 1)

    val courseDummy1 = Course("PA1", "PA1", true, "secret1", subject1, semester, emptyList(), emptyList(), 1)
    val courseDummy2 = Course("PA2", "PA1", false, "secret2", subject2, semester, emptyList(), emptyList(), 2)
    val courseDummy3 = Course("PA3", "PA1", false, null, null, null, emptyList(), emptyList(), 3)

    val cu1 = CourseUser(courseDummy1, userDummy, Role(RoleLevel.STUDENT))
    val cu2 = CourseUser(courseDummy2, userDummy, Role(RoleLevel.TEACHER))

    val user = userDummy.copy(courses = listOf(cu1, cu2))
    given(userRepository.getByUsername(userDto.username)).willReturn(user)

    val admin = adminDummy.copy()
    given(userRepository.getByUsername(adminDto.username)).willReturn(admin)

    val course1 = courseDummy1.copy(users = listOf(cu1))
    val course2 = courseDummy2.copy(users = listOf(cu2))
    val course3 = courseDummy3.copy(users = emptyList())

    given(authorizationService.isTrusted(user, course1)).willReturn(false)
    given(authorizationService.isTrusted(user, course2)).willReturn(true)
    given(authorizationService.isTrusted(user)).willReturn(true)

    val sdto1 = SubjectFindDTO(subject1.id, subject1.name, subject1.code)
    val sdto2 = SubjectFindDTO(subject2.id, subject2.name, subject2.code)
    val smdto1 = SemesterFindDTO(semester.id, semester.code, semester.from, semester.until)
    val dto1 = CourseFindDTO(course1.id, course1.name, course1.shortName, sdto1, smdto1, 0, 0, RoleLevel.STUDENT)
    val gdto1 = CourseGetDTO(course1.id, course1.name, course1.shortName, sdto1, smdto1, RoleLevel.STUDENT, course1.public,null, emptyList())
    val dto2 = CourseFindDTO(course2.id, course2.name, course2.shortName, sdto2, smdto1, 0, 0, RoleLevel.TEACHER)
    val gdto2 = CourseGetDTO(course2.id, course2.name, course2.shortName, sdto2, smdto1, RoleLevel.TEACHER, course2.public,course2.secret, emptyList())

    beforeTest { // Reset counters
        reset(courseRepository)
        reset(courseUserRepository)
        reset(weekRepository)
    }

    "findAll" {
        given(courseRepository.findAll(any<Sort>())).willReturn(listOf(course1, course2))

        service.findAll(userAuthDto) shouldBe listOf(dto1, dto2)

        verify(courseRepository).findAll(any<Sort>())
    }

    "findAll_empty" {
        given(courseRepository.findAll(any<Sort>())).willReturn(emptyList())

        service.findAll(userAuthDto) shouldBe emptyList()

        verify(courseRepository).findAll(any<Sort>())
    }

    "findAll_throw" {
        val e = shouldThrow<ResponseStatusException> {
            service.findAll(null)
        }
        e.statusCode shouldBe HttpStatus.UNAUTHORIZED
    }

    "getOne" {
        given(courseRepository.getReferenceById(course1.id)).willReturn(course1)
        given(courseRepository.getReferenceById(course2.id)).willReturn(course2)

        service.getByID(course1.id, userAuthDto) shouldBe gdto1
        service.getByID(course2.id, userAuthDto) shouldBe gdto2

        verify(courseRepository).getReferenceById(course1.id)
        verify(courseRepository).getReferenceById(course2.id)
    }

    "getOne_404" {
        given(courseRepository.getReferenceById(anyInt())).willThrow(
            JpaObjectRetrievalFailureException(EntityNotFoundException())
        )

        val e = shouldThrow<ResponseStatusException> {
            service.getByID(666, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    "getOne_403" {
        given(courseRepository.getReferenceById(course2.id)).willReturn(course2.copy(users = emptyList()))

        val e = shouldThrow<ResponseStatusException> {
            service.getByID(course2.id, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(courseRepository).getReferenceById(course2.id)
    }

    val week = Week(
        "Test week", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()),
        course2, emptyList(), 1
    )
    val weekDto = WeekGetDTO(week.id, week.name, week.from, week.until, dto2.copy(role = null), emptyList())

    "getWeek" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)

        service.getWeek(course2.id, week.id, userAuthDto) shouldBe weekDto

        verify(weekRepository).getReferenceById(week.id)
    }

    "getWeek_course_not_matching" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)

        val e = shouldThrow<ResponseStatusException> {
            service.getWeek(666, week.id, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(weekRepository).getReferenceById(week.id)
    }

    "getWeek_404" {
        given(weekRepository.getReferenceById(anyInt())).willThrow(
            JpaObjectRetrievalFailureException(EntityNotFoundException())
        )

        val e = shouldThrow<ResponseStatusException> {
            service.getWeek(1, 666, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    "getWeek_403" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week.copy(course = course1))

        val e = shouldThrow<ResponseStatusException> {
            service.getWeek(course1.id, week.id, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(weekRepository).getReferenceById(week.id)
    }

    val cdto = CourseCreateDTO(courseDummy3.name, courseDummy3.shortName, courseDummy3.public, null, null)
    val gdto3 = CourseGetDTO(courseDummy3.id, courseDummy3.name, courseDummy3.shortName, null, null, null, courseDummy3.public, null, emptyList())

    "create" {
        given(courseRepository.saveAndFlush(any<Course>())).willReturn(courseDummy3)

        service.create(cdto, adminAuthDto) shouldBe gdto3

        verify(courseRepository).saveAndFlush(any())
    }

    "create_403" {
        val e = shouldThrow<ResponseStatusException> {
            service.create(cdto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    val updateDto = CourseUpdateDTO("PA3", "PA3", true, emptyList(), 1, 1)

    "update" {
        given(courseRepository.getReferenceById(course1.id)).willReturn(course1)
        given(courseRepository.save(any<Course>())).willReturn(course1.copy(name = "PA3", shortName = "PA3"))

        service.update(course1.id, updateDto, adminAuthDto) shouldBe gdto1.copy(name = "PA3", shortName = "PA3", role = null)

        verify(courseRepository).getReferenceById(course1.id)
        verify(courseRepository).save(any())
    }

    "update_403" {
        given(courseRepository.getReferenceById(course1.id)).willReturn(course1)

        val e = shouldThrow<ResponseStatusException> {
            service.update(course1.id, updateDto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(courseRepository).getReferenceById(course1.id)
    }

    "delete" {
        given(courseRepository.getReferenceById(course3.id)).willReturn(course3)
       doNothing().`when`(courseRepository).delete(any())

        service.delete(course3.id, adminAuthDto)

        verify(courseRepository).getReferenceById(course3.id)
        verify(courseRepository).delete(course3)
    }

    "delete_403" {
        given(courseRepository.getReferenceById(course1.id)).willReturn(course1)

        val e = shouldThrow<ResponseStatusException> {
            service.delete(course1.id, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(courseRepository).getReferenceById(course1.id)
    }

    "delete_409" {
        given(courseRepository.getReferenceById(course1.id)).willReturn(course1.copy(weeks = listOf(week)))

        val e = shouldThrow<ResponseStatusException> {
            service.delete(course1.id, adminAuthDto)
        }
        e.statusCode shouldBe HttpStatus.CONFLICT

        verify(courseRepository).getReferenceById(course1.id)
    }

    val semesterCourseDto = SemesterCourseReadDTO(course1.id, course1.name, course1.shortName, sdto1, smdto1)

    "getBySemesterAndSubject" {
        given(subjectRepository.getReferenceById(subject1.id)).willReturn(subject1)
        given(courseRepository.findAll(Sort.unsorted())).willReturn(listOf(course1.copy(subject = subject1, semester = semester)))

        service.getBySemesterAndSubject(semester.id, subject1.id, adminAuthDto) shouldBe listOf(semesterCourseDto)

        verify(subjectRepository, times(1)).getReferenceById(subject1.id)
        verify(courseRepository).findAll(Sort.unsorted())
    }

    val secretDto = CourseSecretDTO("storm")
    val dummyCU = CourseUser(course1, user, Role(RoleLevel.STUDENT))

    "joinCourse" {
        given(courseRepository.getBySecret(secretDto.secret ?: "")).willReturn(course1)
        given(roleRepository.getByLevel(RoleLevel.STUDENT)).willReturn(Role(RoleLevel.STUDENT))
        given(courseUserRepository.saveAndFlush(dummyCU)).willReturn(cu1)

        service.joinCourse(secretDto, userAuthDto) shouldBe gdto1.copy(role = null)

        verify(courseRepository).getBySecret(secretDto.secret ?: "")
        verify(courseUserRepository).saveAndFlush(dummyCU)
    }

    "joinCourse_400" {
        given(courseRepository.getBySecret(secretDto.secret ?: "")).willReturn(course1)
        given(roleRepository.getByLevel(RoleLevel.STUDENT)).willReturn(Role(RoleLevel.STUDENT))
        given(courseUserRepository.saveAndFlush(dummyCU)).willThrow(DataIntegrityViolationException(""))

        val e = shouldThrow<ResponseStatusException> {
            service.joinCourse(secretDto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.BAD_REQUEST

        verify(courseRepository).getBySecret(secretDto.secret ?: "")
        verify(courseUserRepository).saveAndFlush(dummyCU)
    }

    "joinCourse_404" {
        given(courseRepository.getBySecret(secretDto.secret ?: "")).willThrow(
            JpaObjectRetrievalFailureException(EntityNotFoundException())
        )

        val e = shouldThrow<ResponseStatusException> {
            service.joinCourse(secretDto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND

        verify(courseRepository).getBySecret(secretDto.secret ?: "")
    }

    "setSecret" {
        given(courseRepository.getReferenceById(course2.id)).willReturn(course2)
        given(courseRepository.saveAndFlush(course2.copy(secret = secretDto.secret)))
            .willReturn(course2.copy(secret = secretDto.secret))

        service.setCourseSecret(course2.id, secretDto, userAuthDto) shouldBe gdto2.copy(
            secret = null, role = null
        )

        verify(courseRepository).getReferenceById(course2.id)
        verify(courseRepository).saveAndFlush(course2.copy(secret = secretDto.secret))
    }

    "setSecret_403" {
        given(courseRepository.getReferenceById(course1.id)).willReturn(course1)

        val e = shouldThrow<ResponseStatusException> {
            service.setCourseSecret(course1.id, secretDto, userAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(courseRepository).getReferenceById(course1.id)
    }
})
