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
import org.springframework.http.HttpStatus
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [CourseUserService::class, ConverterService::class])
class CourseUserServiceTests(
    @MockBean val courseRepository: CourseRepository,
    @MockBean val courseUserRepository: CourseUserRepository,
    @MockBean val roleRepository: RoleRepository,
    @MockBean val logRepository: LogRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val quizroomRepo: QuizroomRepo,
    @MockBean val questionRepo: QuestionRepo,
    @MockBean val weekRepository: WeekRepository,
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val smRepository: StudentModuleRepository,
    @MockBean val suRepository: SandboxUserRepository,
    @MockBean val smrqRepository: StudentModuleRequestRepository,
    @MockBean val semesterRepository: SemesterRepository,
    @MockBean val subjectRepository: SubjectRepository,
    @Autowired val converterService: ConverterService,
    @MockBean val fileService: FileService,
    service: CourseUserService
): StringSpec({
    val userDto1 = UserFindDTO(1, "testuser", "Test user")
    val userAuthDto1 = UserAuthenticateDto(userDto1.id, userDto1.username, "", "")
    val userDto2 = UserFindDTO(2, "wrzecond", "Test teacher")
    val userAuthDto2 = UserAuthenticateDto(userDto2.id, userDto2.username, "", "")
    val userDummy1 = User("testsecret", userDto1.username, userDto1.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), userDto1.id)
    val userDummy2 = User("another", userDto2.username, userDto2.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),userDto2.id, true)

    val subject = Subject("Programming and Algorithms 1", "BI-PA1.21", emptyList(), emptyList(), emptyList())
    val semester = Semester("B232", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()), emptyList(), 1)

    val courseDummy = Course("PA1", "PA1", true, "secret1", subject, semester, emptyList(), emptyList(), 1)

    val cu1 = CourseUser(courseDummy, userDummy1, Role(RoleLevel.STUDENT))
    val cu2 = CourseUser(courseDummy, userDummy2, Role(RoleLevel.TEACHER))
    val course = courseDummy.copy(users = listOf(cu1, cu2))

    val user1 = userDummy1.copy(courses = listOf(cu1))
    val user2 = userDummy2.copy(courses = listOf(cu2))

    given(authorizationService.isTrusted(user1, course)).willReturn(false)
    given(authorizationService.isTrusted(user2, course)).willReturn(true)
    given(authorizationService.isTrusted(user1)).willReturn(false)
    given(authorizationService.isTrusted(user2)).willReturn(true)

    val sdto1 = SubjectFindDTO(subject.id, subject.name, subject.code)
    val smdto1 = SemesterFindDTO(semester.id, semester.code, semester.from, semester.until)
    val dto1 = CourseFindDTO(course.id, course.name, course.shortName, sdto1, smdto1, 0, 0, null)
    val gdto1 = CourseGetDTO(course.id, course.name, course.shortName, sdto1, smdto1, RoleLevel.STUDENT, course.public,null, emptyList())

    val cudto1 = CourseUserReadDTO(user1.id, user1.username, user1.name, cu1.role.level, 0)
    val cudto2 = CourseUserReadDTO(user2.id, user2.username, user2.name, cu2.role.level, 0)

    beforeTest { // Reset counters
        reset(courseRepository)
        reset(courseUserRepository)
        reset(roleRepository)

        reset(userRepository)
        given(userRepository.getByUsername(userDto1.username)).willReturn(user1)
        given(userRepository.getReferenceById(userDto1.id)).willReturn(user1)
        given(userRepository.getByUsername(userDto2.username)).willReturn(user2)
        given(userRepository.getReferenceById(userDto2.id)).willReturn(user2)
    }

    "findCourseUsers" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(courseUserRepository.findByCourse(course)).willReturn(listOf(cu1, cu2))

        service.findCourseUsers(course.id, userAuthDto2) shouldBe CourseUserList(dto1, listOf(cudto1, cudto2))

        verify(courseRepository).getReferenceById(course.id)
        verify(courseUserRepository).findByCourse(course)
    }

    "findCourseUsers_empty" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(courseUserRepository.findByCourse(course)).willReturn(emptyList())

        service.findCourseUsers(course.id, userAuthDto2) shouldBe CourseUserList(dto1, emptyList())

        verify(courseRepository).getReferenceById(course.id)
        verify(courseUserRepository).findByCourse(course)
    }

    "findCourseUsers_404" {
        given(courseRepository.getReferenceById(course.id)).willThrow(
            JpaObjectRetrievalFailureException(EntityNotFoundException())
        )

        val e = shouldThrow<ResponseStatusException> {
            service.findCourseUsers(course.id, userAuthDto2)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND

        verify(courseRepository).getReferenceById(course.id)
    }

    "findCourseUsers_403" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)

        val e = shouldThrow<ResponseStatusException> {
            service.findCourseUsers(course.id, userAuthDto1)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(courseRepository).getReferenceById(course.id)
    }

    "findCourseTeachers" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(courseUserRepository.findTeachersByCourse(course, user1)).willReturn(listOf(user2))

        service.findCourseTeachers(course.id, userAuthDto1) shouldBe listOf(cudto2.copy(progress = null))

        verify(courseRepository).getReferenceById(course.id)
        verify(courseUserRepository).findTeachersByCourse(course, user1)
    }

    "findCourseTeachers_empty" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(courseUserRepository.findTeachersByCourse(course, user1)).willReturn(emptyList())

        service.findCourseTeachers(course.id, userAuthDto1) shouldBe emptyList()

        verify(courseRepository).getReferenceById(course.id)
        verify(courseUserRepository).findTeachersByCourse(course, user1)
    }

    "findCourseTeachers_404" {
        given(courseRepository.getReferenceById(course.id)).willThrow(
            JpaObjectRetrievalFailureException(EntityNotFoundException())
        )

        val e = shouldThrow<ResponseStatusException> {
            service.findCourseUsers(course.id, userAuthDto1)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND

        verify(courseRepository).getReferenceById(course.id)
    }

    "getCourseUser_student" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)

        service.getCourseUser(course.id, user1.id, userAuthDto2) shouldBe CourseUserDetail(gdto1, userDto1)

        verify(courseRepository).getReferenceById(course.id)
    }

    "getCourseUser_teacher" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)

        service.getCourseUser(course.id, user2.id, userAuthDto2) shouldBe CourseUserDetail(
            gdto1.copy(secret = course.secret, role = cu2.role.level), userDto2
        )

        verify(courseRepository).getReferenceById(course.id)
    }

    "getCourseUser_404" {
        given(courseRepository.getReferenceById(anyInt())).willThrow(
            JpaObjectRetrievalFailureException(EntityNotFoundException())
        )

        val e = shouldThrow<ResponseStatusException> {
            service.getCourseUser(666, user1.id, userAuthDto2)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    "getCourseUser_403" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)

        val e = shouldThrow<ResponseStatusException> {
            service.getCourseUser(course.id, user1.id, userAuthDto1)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(courseRepository).getReferenceById(course.id)
    }

    val editDTO = CourseUserEditDTO("karlos52", "Karlos", RoleLevel.STUDENT)
    val editUser = User("generated", editDTO.username, editDTO.name ?: "", Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(),emptyList(),emptyList())
    val editUserDto = CourseUserReadDTO(editUser.id, editUser.username, editUser.name, editDTO.role, 0)
    val role = Role(editDTO.role)
    val editCu = CourseUser(course, editUser, role)

    "addCourseUsers" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(userRepository.getByUsername(editDTO.username)).willReturn(null)
        given(userRepository.saveAndFlush(any())).willReturn(editUser)
        given(roleRepository.getByLevel(editDTO.role)).willReturn(role)
        given(courseUserRepository.saveAllAndFlush(listOf(editCu)))
            .willReturn(listOf(editCu))

        service.addCourseUsers(course.id, listOf(editDTO), userAuthDto2) shouldBe listOf(editUserDto)

        verify(courseRepository).getReferenceById(course.id)
        verify(userRepository).getByUsername(editDTO.username)
        verify(userRepository).saveAndFlush(any())
        verify(roleRepository).getByLevel(editDTO.role)
        verify(courseUserRepository).saveAllAndFlush(listOf(editCu))
    }

    "addCourseUsers_403" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)

        val e = shouldThrow<ResponseStatusException> {
            service.addCourseUsers(course.id, listOf(editDTO), userAuthDto1)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(courseRepository).getReferenceById(course.id)
    }

    "addCourseUsers_400" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(userRepository.getByUsername(editDTO.username)).willReturn(user1)
        given(roleRepository.getByLevel(editDTO.role)).willReturn(role)
        given(courseUserRepository.saveAllAndFlush(listOf(CourseUser(course, user1, role))))
            .willThrow(DataIntegrityViolationException(""))

        val e = shouldThrow<ResponseStatusException> {
            service.addCourseUsers(course.id, listOf(editDTO), userAuthDto2)
        }
        e.statusCode shouldBe HttpStatus.BAD_REQUEST

        verify(courseRepository).getReferenceById(course.id)
        verify(userRepository).getByUsername(editDTO.username)
        verify(roleRepository).getByLevel(editDTO.role)
        verify(courseUserRepository).saveAllAndFlush(listOf(CourseUser(course, user1, role)))
    }

    val delUser = User("goodbye", "deleted", "To be deleted", Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),42)
    val delCU = CourseUser(course, delUser, role)

    "delCourseUser" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(userRepository.getReferenceById(delUser.id)).willReturn(delUser)
        given(courseUserRepository.getByCourseUser(course, delUser)).willReturn(delCU)
        doNothing().`when`(courseUserRepository).delete(delCU)

        service.delCourseUser(course.id, delUser.id, userAuthDto2)

        verify(courseRepository).getReferenceById(course.id)
        verify(userRepository).getReferenceById(delUser.id)
        verify(courseUserRepository).getByCourseUser(course, delUser)
        verify(courseUserRepository).delete(delCU)
    }

    "delCourseUser_404" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(userRepository.getReferenceById(delUser.id)).willReturn(delUser)
        given(courseUserRepository.getByCourseUser(course, delUser)).willThrow(
            JpaObjectRetrievalFailureException(EntityNotFoundException())
        )

        val e = shouldThrow<ResponseStatusException> {
            service.delCourseUser(course.id, delUser.id, userAuthDto2)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND

        verify(courseRepository).getReferenceById(course.id)
        verify(userRepository).getReferenceById(delUser.id)
        verify(courseUserRepository).getByCourseUser(course, delUser)
    }

    "delCourseUser_403" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)

        val e = shouldThrow<ResponseStatusException> {
            service.delCourseUser(course.id, delUser.id, userAuthDto1)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(courseRepository).getReferenceById(course.id)
    }

})
