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

@SpringBootTest(classes = [WeekService::class])
class WeekServiceTests(
    @MockBean val weekRepository: WeekRepository,
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val courseRepository: CourseRepository,
    @MockBean val lessonModuleRepository: LessonModuleRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    @MockBean val logRepository: LogRepository,
    val service: WeekService
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
        emptyList(), emptyList(), emptyList(),emptyList(),emptyList(), 2)
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

    beforeTest { // Reset counters
        reset(courseRepository)
        reset(lessonRepository)
        reset(weekRepository)
    }

    val fdto1 = LessonFindDTO(lesson1.id, lesson1.name, lesson1.hidden, lesson1.order, lesson1.type,
        lesson1.lockCode, lesson1.timeEnd, 0, 0)
    val fdto2 = LessonFindDTO(lesson2.id, lesson2.name, lesson2.hidden, lesson2.order, lesson2.type,
        lesson2.lockCode, lesson2.timeEnd, 0,0)
    
    val weekgdto = WeekGetDTO(week.id, week.name, week.from, week.until, courseDto, listOf(fdto1))
    val cdto = WeekCreateDTO(week.name ?: "", week.from, week.until, week.course.id)

    "create" {
        given(courseRepository.getReferenceById(cdto.courseId)).willReturn(course)
        given(weekRepository.saveAndFlush(any<Week>())).willReturn(week)

        service.create(cdto, userAuthDto) shouldBe weekgdto

        verify(courseRepository).getReferenceById(cdto.courseId)
        verify(weekRepository).saveAndFlush(any())
    }

    "create_403" {
        given(courseRepository.getReferenceById(cdto.courseId)).willReturn(course)

        val e = shouldThrow<ResponseStatusException> {
            service.create(cdto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(courseRepository).getReferenceById(cdto.courseId)
    }

    val updateDto = WeekUpdateDTO("UpdatedName", null, Timestamp.from(Instant.now()))

    "update" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)
        given(weekRepository.saveAndFlush(any<Week>())).willReturn(week.copy(name = updateDto.name ?: "",
            until = weekDto.until))

        service.update(week.id, updateDto, userAuthDto) shouldBe weekgdto.copy(name = updateDto.name ?: "",
            until = weekDto.until)

        verify(weekRepository).getReferenceById(week.id)
        verify(weekRepository).saveAndFlush(any())
    }

    "update_403" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)

        val e = shouldThrow<ResponseStatusException> {
            service.update(week.id, updateDto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(weekRepository).getReferenceById(week.id)
    }

    "delete" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)
        doNothing().`when`(weekRepository).delete(any())

        service.delete(week.id, userAuthDto)

        verify(weekRepository).getReferenceById(week.id)
        verify(weekRepository).delete(any())
    }

    "delete_403" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)

        val e = shouldThrow<ResponseStatusException> {
            service.delete(week.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(weekRepository).getReferenceById(week.id)
    }

    val wlodto = WeekLessonOrderDTO(listOf(lesson2.id, lesson1.id))

    "editLessonOrder" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(lessonRepository.getReferenceById(lesson2.id)).willReturn(lesson2)
        given(lessonRepository.saveAllAndFlush(listOf(lesson2.copy(order = 0), lesson1.copy(order = 1))))
            .willReturn(listOf(lesson2, lesson1))

        service.editLessonOrder(week.id, wlodto, userAuthDto)

        verify(weekRepository).getReferenceById(week.id)
        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(lessonRepository).getReferenceById(lesson2.id)
        verify(lessonRepository).saveAllAndFlush(listOf(lesson2.copy(order = 0), lesson1.copy(order = 1)))
    }

    "editLessonOrder_403" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)

        val e = shouldThrow<ResponseStatusException> {
            service.editLessonOrder(week.id, wlodto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(weekRepository).getReferenceById(week.id)
    }

    "cloneWeek" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(weekRepository.getReferenceById(week.id)).willReturn(week)
        given(weekRepository.saveAndFlush(week.copy(lessons = emptyList(), id = 0))).willReturn(week)

        service.cloneWeek(lesson1.id, course.id, userAuthDto) shouldBe weekgdto.copy(lessons = listOf(fdto1, fdto2))

        verify(courseRepository).getReferenceById(course.id)
        verify(weekRepository).getReferenceById(week.id)
        verify(weekRepository).saveAndFlush(week.copy(lessons = emptyList(), id = 0))
    }

    "cloneWeek_403" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)

        val e = shouldThrow<ResponseStatusException> {
            service.cloneWeek(week.id, course.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(weekRepository).getReferenceById(week.id)
    }

})