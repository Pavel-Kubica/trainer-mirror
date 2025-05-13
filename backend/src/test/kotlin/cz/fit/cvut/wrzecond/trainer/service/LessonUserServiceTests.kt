package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.mockito.Answers
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [LessonUserService::class])
class LessonUserServiceTests(
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val studentModuleRepository: StudentModuleRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    val service: LessonUserService
): StringSpec({

    val subject1 = Subject("Programming and Algorithms 1", "BI-PA1.21", emptyList(), emptyList(), emptyList())
    val semester = Semester("B232", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()), emptyList(), 1)
    val courseDummy = Course("PA1", "PA1", true, "secret1", subject1, semester, emptyList(), emptyList(), 1)

    val userDto = UserFindDTO(1, "testuser", "Test user")
    val userAuthDto = UserAuthenticateDto(userDto.id, userDto.username, "", "")
    val userStudentDto = UserFindDTO(2, "teststudent", "Test student")
    val userStudentAuthDto = UserAuthenticateDto(userStudentDto.id, userStudentDto.username, "", "")
    val userDummy = User("testsecret", userDto.username, userDto.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),1)
    val userStudentDummy = User("another", userStudentDto.username, userStudentDto.name, Timestamp.from(Instant.now()), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),2)
    val courseUser = CourseUser(courseDummy, userDummy, Role(RoleLevel.TEACHER))
    val courseUserStudent = CourseUser(courseDummy, userDummy, Role(RoleLevel.STUDENT))

    val course = courseDummy.copy(users = listOf(courseUser, courseUserStudent))
    val user = userDummy.copy(courses = listOf(courseUser))
    val userStudent = userStudentDummy.copy(courses = listOf(courseUserStudent))
    given(userRepository.getByUsername(userDto.username)).willReturn(user)
    given(userRepository.getByUsername(userStudentDto.username)).willReturn(userStudent)

    val weekDummy = Week("First week", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()),
        course, emptyList(), 1)
    val lesson1Dummy = Lesson("Compilation", false, 1, null, null, "Assignment",
        LessonType.TUTORIAL_PREPARATION, null, weekDummy, emptyList(), emptyList(),emptyList(),1)
    val lesson2Dummy = Lesson("First program", true, 2, Timestamp.from(Instant.now()),
        Timestamp.from(Instant.now().plusMillis(24 * 3600 * 1000)), "Another description",
        LessonType.TUTORIAL, "secret", weekDummy, emptyList(), emptyList(),emptyList(),2)
    val week = weekDummy.copy(lessons = listOf(lesson1Dummy, lesson2Dummy))
    val lesson1 = lesson1Dummy.copy(week = week)
    val lesson2 = lesson2Dummy.copy(week = week)

    val sdto1 = SubjectFindDTO(subject1.id, subject1.name, subject1.code)
    val smdto1 = SemesterFindDTO(semester.id, semester.code, semester.from, semester.until)
    val courseDto = CourseFindDTO(course.id, course.name, course.shortName, sdto1, smdto1, 0, 0, null)
    val weekDto = WeekFindDTO(week.id, week.name, week.from, week.until, courseDto)

    val gdto1 = LessonGetDTO(lesson1.id, weekDto, lesson1.name, lesson1.hidden, lesson1.type, lesson1.lockCode,
        lesson1.timeStart, lesson1.timeEnd, lesson1.description, emptyList(),emptyList())
    val gdto2 = LessonGetDTO(lesson2.id, weekDto, lesson2.name, lesson2.hidden, lesson2.type, lesson2.lockCode,
        lesson2.timeStart, lesson2.timeEnd, lesson2.description, emptyList(),emptyList())

    beforeTest { // Reset counters
        reset(lessonRepository)
    }

    "getLessonUsers" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(userRepository.findByLesson(lesson1)).willReturn(listOf(user, userStudent))

        service.getLessonUsers(lesson1.id, userAuthDto) shouldBe LessonUserListDTO(lesson1.id,
            courseDto.copy(role = RoleLevel.TEACHER), lesson1.name, emptyList(), listOf(
                ModuleUserListDTO(user.id, user.username, user.name, emptyList()),
                ModuleUserListDTO(userStudent.id, userStudent.username, userStudent.name, emptyList()),
            ))

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(userRepository, atLeastOnce()).findByLesson(lesson1)
    }

    "getLessonUsers_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.getLessonUsers(lesson1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

    "getLessonUser" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(userRepository.getReferenceById(userStudent.id)).willReturn(userStudent)

        service.getLessonUser(lesson1.id, userStudent.id, userAuthDto) shouldBe LessonUserReadDTO(
            gdto1.copy(week = weekDto.copy(course = courseDto.copy(role = RoleLevel.STUDENT))), userStudentDto
        )

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(userRepository, atLeastOnce()).getReferenceById(userStudent.id)
    }

    "getLessonUser_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.getLessonUser(lesson1.id, user.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

    val weekDetailDto = WeekDetailDTO(week.id, week.name, week.from, week.until, courseDto, listOf(
        gdto1.copy(week = weekDto.copy(course = courseDto.copy(role = RoleLevel.TEACHER))),
        gdto2.copy(week = weekDto.copy(course = courseDto.copy(role = RoleLevel.TEACHER))),
    ))

    "lessonUserWeek" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(lessonRepository.getReferenceById(lesson2.id)).willReturn(lesson2)
        given(userRepository.getReferenceById(user.id)).willReturn(user)
        given(userRepository.getReferenceById(userStudent.id)).willReturn(userStudent)

        service.getLessonUserWeek(lesson1.id, user.id, userAuthDto) shouldBe weekDetailDto
        service.getLessonUserWeek(lesson1.id, userStudent.id, userAuthDto) shouldBe weekDetailDto.copy(lessons = listOf(
            gdto1.copy(week = weekDto.copy(course = courseDto.copy(role = RoleLevel.STUDENT))),
        ))
        service.getLessonUserWeek(lesson2.id, user.id, userAuthDto) shouldBe weekDetailDto

        val e = shouldThrow<ResponseStatusException> {
            service.getLessonUserWeek(lesson1.id, user.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository, times(3)).getReferenceById(lesson1.id)
        verify(lessonRepository).getReferenceById(lesson2.id)
        verify(userRepository, atLeast(2)).getReferenceById(user.id)
        verify(userRepository, atLeastOnce()).getReferenceById(userStudent.id)
    }

    "resetLessonUser" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(userRepository.getReferenceById(userStudent.id)).willReturn(userStudent)
        given(studentModuleRepository.findByStudentLesson(userStudent, lesson1)).willReturn(emptyList())
        doNothing().`when`(studentModuleRepository).deleteAll(emptyList())

        service.resetLessonUser(lesson1.id, userStudent.id, userAuthDto)

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(userRepository, atLeastOnce()).getReferenceById(userStudent.id)
        verify(studentModuleRepository).findByStudentLesson(userStudent, lesson1)
        verify(studentModuleRepository).deleteAll(emptyList())
    }

    "resetLessonUser_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.resetLessonUser(lesson1.id, user.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

})
