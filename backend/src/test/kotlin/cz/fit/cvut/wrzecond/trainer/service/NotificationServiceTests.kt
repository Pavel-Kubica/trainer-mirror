package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.mockito.Answers
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.env.MockEnvironment
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [NotificationService::class])
class NotificationServiceTests(
    @MockBean val userRepository: UserRepository,
    @MockBean val environment: MockEnvironment,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val studentModuleRequestRepository: StudentModuleRequestRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val lessonModuleRepository: LessonModuleRepository,
    @MockBean val userService: UserService,

    val service: NotificationService
): StringSpec({

    val time = Instant.now()

    val subject1 = Subject("Programming and Algorithms 1", "BI-PA1.21", emptyList(), emptyList(), emptyList())
    val semester = Semester("B232", Timestamp.from(time), Timestamp.from(time), emptyList(), 1)
    val courseDummy = Course("PA1", "PA1", true, "secret1", subject1, semester, emptyList(), emptyList(), 1)

    val userDto = UserFindDTO(1, "testuser", "Test user")
    val userAuthDto = UserAuthenticateDto(userDto.id, userDto.username, "", "")
    val userStudentDto = UserFindDTO(2, "teststudent", "Test student")
    val userStudentAuthDto = UserAuthenticateDto(userStudentDto.id, userStudentDto.username, "", "")
    val userDummy = User("testsecret", userDto.username, userDto.name, Timestamp.from(time), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),1)
    val userStudentDummy = User("another", userStudentDto.username, userStudentDto.name, Timestamp.from(time), false,
        emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),2)
    val courseUser = CourseUser(courseDummy, userDummy, Role(RoleLevel.TEACHER))
    val courseUserStudent = CourseUser(courseDummy, userDummy, Role(RoleLevel.STUDENT))

    val course = courseDummy.copy(users = listOf(courseUser, courseUserStudent))
    val user = userDummy.copy(courses = listOf(courseUser))
    val userStudent = userStudentDummy.copy(courses = listOf(courseUserStudent))
    given(userRepository.getByUsername(userDto.username)).willReturn(user)
    given(userRepository.getByUsername(userStudentDto.username)).willReturn(userStudent)

    val weekDummy = Week("First week", Timestamp.from(time), Timestamp.from(time),
        course, emptyList(), 1)
    val lesson1Dummy = Lesson("Compilation", false, 1, null, null, "Assignment",
        LessonType.TUTORIAL_PREPARATION, null, null, weekDummy, emptyList(), emptyList(),emptyList(),1)
    val lesson2Dummy = Lesson("First program", true, 2, Timestamp.from(time),
        Timestamp.from(time.plusMillis(24 * 3600 * 1000)), "Another description",
        LessonType.TUTORIAL, "secret", null, weekDummy, emptyList(), emptyList(),emptyList(),2)
    val week = weekDummy.copy(lessons = listOf(lesson1Dummy, lesson2Dummy))

    val module1Dummy = Module("Dummy", ModuleType.TEXT, "Text of module", ModuleDifficulty.EASY,
        false, false, false, true, 100, null, Timestamp.from(time),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),user, 1)
    val module2Dummy = Module("Another", ModuleType.QUIZ, "Some quiz module", null,
        true, false, false, true, 50, null, Timestamp.from(time),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),user, 2)
    val lm1 = LessonModule(lesson1Dummy, module1Dummy, 1, null,1)
    val lm2 = LessonModule(lesson2Dummy, module2Dummy, 1, null,2)
    val module1 = module1Dummy.copy(lessons = listOf(lm1))
    val module2 = module2Dummy.copy(lessons = listOf(lm2))

    val lesson1 = lesson1Dummy.copy(week = week, modules = listOf(lm1))
    val lesson2 = lesson2Dummy.copy(week = week, modules = listOf(lm2))

    val lnotdto1 = LessonNotificationDTO(lesson1.id, lesson1.name)
    val lnotdto2 = LessonNotificationDTO(lesson2.id, lesson2.name)
    val mnotdto1 = ModuleNotificationDTO(module1.id, module1.name)
    val mnotdto2 = ModuleNotificationDTO(module2.id, module2.name)
    val unotdto = UserNotificationDTO(user.id, user.name)
    val ustudentnotdto = UserNotificationDTO(userStudent.id, userStudent.name)

    val sm1 = StudentModule(lesson1, module1, userStudent, null, null, null, false,
        false, false, null,  emptyList(),1)
    val sm2 = StudentModule(lesson2, module2, userStudent, null, null, null, false,
        false, false, null,  emptyList(),2)
    val smrq1 = StudentModuleRequest(sm1, user, StudentModuleRequestType.EVALUATE, "Please evaluate",
        Timestamp.from(time), "Congratulations",  true, Timestamp.from(time), 1)
    val smrq2 = StudentModuleRequest(sm2, null, StudentModuleRequestType.HELP, "Please help",
        Timestamp.from(time.plusSeconds(1).plusSeconds(1)), null,  false, null, 2)

    val notdto1 = NotificationDTO(1, lnotdto1, mnotdto1, unotdto, smrq1.teacherResponse ?: "",
        true, smrq1.requestType, smrq1.satisfied, smrq1.satisfiedOn ?: Timestamp.from(time))
    val notdto2 = NotificationDTO(1, lnotdto1, mnotdto1, ustudentnotdto, smrq1.requestText ?: "",
        false, smrq1.requestType, smrq1.satisfied, smrq1.requestedOn)
    val notdto3 = NotificationDTO(2, lnotdto2, mnotdto2, ustudentnotdto, smrq2.requestText ?: "",
            false, smrq2.requestType, smrq2.satisfied, smrq2.requestedOn)

    beforeTest {
        reset(studentModuleRequestRepository)
    }

    "getNotifications" {
        given(studentModuleRequestRepository.findNotificationsStudent(eq(userStudent), eq(true), anyOrNull()))
            .willReturn(listOf(smrq1))
        given(studentModuleRequestRepository.findNotificationsTeacher(eq(user), eq(false), anyOrNull(), eq(RoleLevel.TEACHER)))
            .willReturn(listOf(smrq2))
        given(studentModuleRequestRepository.findNotificationsTeacher(eq(user), eq(true), anyOrNull(), eq(RoleLevel.TEACHER)))
            .willReturn(listOf(smrq1, smrq2))

        service.getNotifications(userAuthDto, false) shouldBe listOf(notdto3)
        service.getNotifications(userStudentAuthDto, true) shouldBe listOf(notdto1)
        service.getNotifications(userAuthDto, true) shouldBe listOf(notdto3, notdto2)

        verify(studentModuleRequestRepository).findNotificationsStudent(eq(userStudent), eq(true), anyOrNull())
        verify(studentModuleRequestRepository).findNotificationsTeacher(eq(user), eq(false), anyOrNull(), eq(RoleLevel.TEACHER))
        verify(studentModuleRequestRepository).findNotificationsTeacher(eq(user), eq(true), anyOrNull(), eq(RoleLevel.TEACHER))
    }

    "markAllNotificationsRead" {
        given(userRepository.saveAndFlush(any<User>())).willReturn(userStudent)

        service.markAllNotificationsRead(userStudentAuthDto)

        verify(userRepository).saveAndFlush(any<User>())
    }

})