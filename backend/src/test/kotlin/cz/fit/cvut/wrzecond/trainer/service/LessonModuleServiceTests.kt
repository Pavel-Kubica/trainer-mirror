package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import cz.fit.cvut.wrzecond.trainer.service.code.CodeModuleService
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import cz.fit.cvut.wrzecond.trainer.service.quiz.QuizService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [LessonModuleService::class, ConverterService::class])
class LessonModuleServiceTests(
    @MockBean val repository: LessonModuleRepository,
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val codeModuleService: CodeModuleService,
    @MockBean val quizService: QuizService,
    @MockBean val studentModuleRepository: StudentModuleRepository,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val mtRepository: ModuleTopicRepository,
    @MockBean val msRepository: ModuleSubjectRepository,
    @MockBean val questionRepo: QuestionRepo,
    @MockBean val quizroomRepo: QuizroomRepo,
    @MockBean val studentModuleRequestRepository: StudentModuleRequestRepository,
    @MockBean val fileService: FileService,
    @MockBean val semesterRepository: SemesterRepository,
    @MockBean val subjectRepository: SubjectRepository,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val logRepository: LogRepository,
    @MockBean val logService: LogService,
    @MockBean val userService: UserService,
    val converterService: ConverterService,
    val service: LessonModuleService
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
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),2)
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
        LessonType.TUTORIAL_PREPARATION, null, null, weekDummy, emptyList(), emptyList(),emptyList(),1)
    val lesson2Dummy = Lesson("First program", true, 2, Timestamp.from(Instant.now()),
        Timestamp.from(Instant.now().plusMillis(24 * 3600 * 1000)), "Another description",
        LessonType.TUTORIAL, "secret", null, weekDummy, emptyList(), emptyList(),emptyList(),2)
    val week = weekDummy.copy(lessons = listOf(lesson1Dummy, lesson2Dummy))

    val module1Dummy = Module("Dummy", ModuleType.TEXT, "Text of module", ModuleDifficulty.EASY,
        false, false, false, false, 100, null, Timestamp.from(Instant.now()),
        emptyList(), emptyList(),emptyList(),emptyList(), emptyList(), emptyList(),emptyList(),user,1)
    val lm1 = LessonModule(lesson1Dummy, module1Dummy, 1, null, 1)
    val module1 = module1Dummy.copy(lessons = listOf(lm1))

    val lesson1 = lesson1Dummy.copy(week = week, modules = listOf(lm1))
    val lesson2 = lesson2Dummy.copy(week = week)

    val sdto1 = SubjectFindDTO(subject1.id, subject1.name, subject1.code)
    val smdto1 = SemesterFindDTO(semester.id, semester.code, semester.from, semester.until)
    val courseDto = CourseFindDTO(course.id, course.name, course.shortName, sdto1, smdto1, null)
    val weekDto = WeekFindDTO(week.id, week.name, week.from, week.until, courseDto)

    val mfdto1 = ModuleFindDTO(module1.id, module1.name, emptyList(), emptyList(),
        module1.type, module1.assignment, module1.difficulty,
        module1.lockable, module1.lockable, module1.timeLimit, module1.manualEval, module1.hidden,
        module1.minPercent, module1.file, lm1.dependsOn?.id, module1.author.username, emptyList(), null, null, null,
        null, lm1.order, null, false, emptyList(), emptyList())

    val gdto1 = LessonGetDTO(lesson1.id, weekDto, lesson1.name, lesson1.hidden, lesson1.type, lesson1.lockCode, lesson1.referenceSolutionAccessibleFrom,
        lesson1.timeStart, lesson1.timeEnd, lesson1.description, listOf(mfdto1),emptyList(),)

    beforeTest { // Reset counters
        reset(lessonRepository)
        reset(repository)
        reset(moduleRepository)
    }

    "getLessonModuleUsers" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(repository.getByLessonModule(lesson1, module1)).willReturn(lm1)
        given(studentModuleRequestRepository.getLastTeacherComment(null)).willReturn(null)

        service.getLessonModuleUsers(lesson1.id, module1.id, userAuthDto) shouldBe LessonModuleUserListDTO(
            gdto1, mfdto1.copy(order = null, hasStudentFile = null), emptyList()
        )

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(repository).getByLessonModule(lesson1, module1)
    }

    "getLessonModuleUsers_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.getLessonModuleUsers(lesson1.id, module1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
    }

    val lmedto = LessonModuleEditDTO(2, null)
    val lmedited = LessonModule(lesson1, module1, lmedto.order ?: 0, null,0)

    "putLessonModule" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(repository.getByLessonModule(lesson1, module1)).willReturn(null)
        given(repository.saveAndFlush(lmedited)).willReturn(lmedited.copy(id = 41))

        service.putLessonModule(lesson1.id, module1.id, lmedto, userAuthDto) shouldBe LessonModuleReadDTO(
            41, module1.name, module1.type, lmedto.order ?: 0)

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(repository).getByLessonModule(lesson1, module1)
        verify(repository).saveAndFlush(lmedited)
    }

    "putLessonModule_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.putLessonModule(lesson1.id, module1.id, lmedto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
    }

    val copyDto = LessonModuleCopyDTO(module1.id, 5)
    val mcopied = module1.copy(lockable = false, timeLimit = false, manualEval = false,
        lessons = emptyList(), id = 12)
    val newLm = LessonModule(lesson2, mcopied, copyDto.order, null, 0)

    "copyLessonModule" {
        given(lessonRepository.getReferenceById(lesson2.id)).willReturn(lesson2)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1.copy(type = ModuleType.CODE))
        given(moduleRepository.saveAndFlush(any<Module>())).willReturn(mcopied)
        doNothing().`when`(codeModuleService).copyModule(any<Module>(), any<Module>())
        given(repository.saveAndFlush(newLm)).willReturn(newLm.copy(id = 2))

        service.copyLessonModule(lesson2.id, copyDto, userAuthDto) shouldBe LessonModuleReadDTO(
            2, mcopied.name, mcopied.type, copyDto.order
        )

        verify(lessonRepository).getReferenceById(lesson2.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(moduleRepository).saveAndFlush(any<Module>())
        verify(codeModuleService).copyModule(any<Module>(), any<Module>())
        verify(repository).saveAndFlush(newLm)
    }

    "copyLessonModule_403" {
        given(lessonRepository.getReferenceById(lesson2.id)).willReturn(lesson2)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1.copy(type = ModuleType.CODE))

        val e = shouldThrow<ResponseStatusException> {
            service.copyLessonModule(lesson2.id, copyDto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson2.id)
        verify(moduleRepository).getReferenceById(module1.id)
    }

    "deleteLessonModule" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(repository.getByLessonModule(lesson1, module1)).willReturn(lm1)
        doNothing().`when`(repository).delete(lm1)

        service.delLessonModule(lesson1.id, module1.id, userAuthDto)

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(repository).getByLessonModule(lesson1, module1)
        verify(repository).delete(lm1)
    }

    "deleteLessonModule_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.delLessonModule(lesson1.id, module1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
    }

})