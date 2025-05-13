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

@SpringBootTest(classes = [LessonService::class])
class LessonServiceTests(
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val courseRepository: CourseRepository,
    @MockBean val lessonModuleRepository: LessonModuleRepository,
    @MockBean val studentModuleRepository: StudentModuleRepository,
    @MockBean val weekRepository: WeekRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    @MockBean val logRepository: LogRepository,
    val service: LessonService
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
        reset(courseRepository)
        reset(lessonRepository)
        reset(weekRepository)
    }

    "getByID" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(lessonRepository.getReferenceById(lesson2.id)).willReturn(lesson2)

        service.getByID(lesson1.id, userAuthDto) shouldBe gdto1.copy(
            week = weekDto.copy(course = courseDto.copy(role = RoleLevel.TEACHER))
        )
        service.getByID(lesson2.id, userAuthDto) shouldBe gdto2.copy(
            week = weekDto.copy(course = courseDto.copy(role = RoleLevel.TEACHER))
        )

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(lessonRepository).getReferenceById(lesson2.id)
    }

    "getByID_student" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(lessonRepository.getReferenceById(lesson2.id)).willReturn(lesson2)

        service.getByID(lesson1.id, userStudentAuthDto) shouldBe gdto1.copy(
            week = weekDto.copy(course = courseDto.copy(role = RoleLevel.STUDENT))
        ) // no problem

        val e = shouldThrow<ResponseStatusException> {
            service.getByID(lesson2.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN // hidden lesson

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(lessonRepository).getReferenceById(lesson2.id)
    }

    "getByID_401" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.getByID(lesson1.id, null)
        }
        e.statusCode shouldBe HttpStatus.UNAUTHORIZED

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

    "teacherGetByID" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(lessonRepository.getReferenceById(lesson2.id)).willReturn(lesson2)

        service.teacherGetByID(lesson1.id, userAuthDto) shouldBe gdto1
        service.teacherGetByID(lesson2.id, userAuthDto) shouldBe gdto2

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(lessonRepository).getReferenceById(lesson2.id)
    }

    "teacherGetByID_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.teacherGetByID(lesson1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

    val cdto = LessonCreateDTO(week.id, lesson1.name, lesson1.hidden, lesson1.order, lesson1.type,
        lesson1.lockCode, lesson1.timeStart, lesson1.timeEnd, lesson1.description)

    "create" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)
        given(lessonRepository.saveAndFlush(any<Lesson>())).willReturn(lesson1)

        service.create(cdto, userAuthDto) shouldBe gdto1

        verify(weekRepository).getReferenceById(week.id)
        verify(lessonRepository).saveAndFlush(any())
    }

    "create_403" {
        given(weekRepository.getReferenceById(week.id)).willReturn(week)

        val e = shouldThrow<ResponseStatusException> {
            service.create(cdto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(weekRepository).getReferenceById(week.id)
    }

    val updateDto = LessonUpdateDTO("UpdatedName", null, null, null,
        "code", null, null, "Updated description")

    "update" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(lessonRepository.saveAndFlush(any<Lesson>())).willReturn(lesson1.copy(name = updateDto.name ?: "",
            lockCode = updateDto.lockCode, description = updateDto.description ?: ""))

        service.update(lesson1.id, updateDto, userAuthDto) shouldBe gdto1.copy(name = updateDto.name ?: "",
            lockCode = updateDto.lockCode, description = updateDto.description ?: "")

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(lessonRepository).saveAndFlush(any())
    }

    "update_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.update(lesson1.id, updateDto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

    "delete" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        doNothing().`when`(lessonRepository).delete(any())

        service.delete(lesson1.id, userAuthDto)

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(lessonRepository).delete(any())
    }

    "delete_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.delete(lesson1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

    "currentLessons" {
        given(lessonRepository.findUserLessonsAtTime(eq(user), anyOrNull())).willReturn(listOf(lesson2))
        given(lessonRepository.findUserLessonsAtTime(eq(userStudent), anyOrNull())).willReturn(emptyList())

        service.currentLessons(userAuthDto) shouldBe listOf(gdto2.copy(
            week = weekDto.copy(course = courseDto.copy(role = RoleLevel.TEACHER))
        ))
        service.currentLessons(userStudentAuthDto) shouldBe emptyList()

        verify(lessonRepository).findUserLessonsAtTime(eq(user), anyOrNull())
        verify(lessonRepository).findUserLessonsAtTime(eq(userStudent), anyOrNull())
    }

    val weekDetailDto = WeekDetailDTO(week.id, week.name, week.from, week.until, courseDto, listOf(
        gdto1.copy(week = weekDto.copy(course = courseDto.copy(role = RoleLevel.TEACHER))),
        gdto2.copy(week = weekDto.copy(course = courseDto.copy(role = RoleLevel.TEACHER))),
    ))

    "lessonWeekDetail" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(lessonRepository.getReferenceById(lesson2.id)).willReturn(lesson2)

        service.lessonWeekDetail(lesson1.id, userAuthDto) shouldBe weekDetailDto
        service.lessonWeekDetail(lesson1.id, userStudentAuthDto) shouldBe weekDetailDto.copy(lessons = listOf(
            gdto1.copy(week = weekDto.copy(course = courseDto.copy(role = RoleLevel.STUDENT))),
        ))
        service.lessonWeekDetail(lesson2.id, userAuthDto) shouldBe weekDetailDto

        verify(lessonRepository, times(2)).getReferenceById(lesson1.id)
        verify(lessonRepository).getReferenceById(lesson2.id)
    }

    val unlockDto = LessonUnlockDTO(lesson2.lockCode ?: "")

    "unlockLessonModules" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(lessonRepository.getReferenceById(lesson2.id)).willReturn(lesson2.copy(hidden = false))

        // wrong code
        val e = shouldThrow<ResponseStatusException> {
            service.unlockLessonModules(lesson1.id, unlockDto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.BAD_REQUEST

        // ok
        service.unlockLessonModules(lesson2.id, unlockDto, userStudentAuthDto)

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(lessonRepository).getReferenceById(lesson2.id)
    }

    "cloneLesson" {
        given(courseRepository.getReferenceById(course.id)).willReturn(course)
        given(weekRepository.save(anyOrNull<Week>())).willReturn(week)
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(lessonRepository.saveAndFlush(lesson1.copy(hidden = true, id = 0))).willReturn(lesson1)

        service.cloneLesson(lesson1.id, course.id, userAuthDto) shouldBe gdto1.copy(
            week = weekDto.copy(course = courseDto.copy(role = RoleLevel.TEACHER))
        )

        verify(courseRepository).getReferenceById(course.id)
        verify(weekRepository).save(anyOrNull<Week>())
        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(lessonRepository).saveAndFlush(lesson1.copy(hidden = true, id = 0))
    }

    "cloneLesson_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.cloneLesson(lesson1.id, course.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

})