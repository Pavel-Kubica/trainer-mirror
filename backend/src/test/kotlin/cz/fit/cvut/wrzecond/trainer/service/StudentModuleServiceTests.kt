package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityNotFoundException
import org.mockito.Answers
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [StudentModuleService::class])
class StudentModuleServiceTests(
    @MockBean val lessonRepository: LessonRepository,
    @MockBean val smRepository: StudentModuleRepository,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val smrqRepository: StudentModuleRequestRepository,
    @MockBean val codeCommentRepository: CodeCommentRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    @MockBean val lessonModuleRepository: LessonModuleRepository,
    @MockBean val userService: UserService,

    val service: StudentModuleService
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
    given(userRepository.getReferenceById(userStudentDto.id)).willReturn(userStudent)

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
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),user, 1)
    val lm1 = LessonModule(lesson1Dummy, module1Dummy, 1, null,1)
    val module1 = module1Dummy.copy(lessons = listOf(lm1))
    val lesson1 = lesson1Dummy.copy(week = week, modules = listOf(lm1))

    val sm1 = StudentModule(lesson1, module1, userStudent, 100, Timestamp.from(Instant.now()),
        Timestamp.from(Instant.now()), true, false, true, null,  emptyList(),1)
    val smrq1 = StudentModuleRequest(sm1, user, StudentModuleRequestType.EVALUATE, "Please evaluate",
        Timestamp.from(Instant.now()), "Congratulations",  true, Timestamp.from(Instant.now()), 1)

    beforeTest {
        reset(lessonRepository)
        reset(moduleRepository)
        reset(smRepository)
        reset(smrqRepository)
    }

    val smedto = StudentModuleEditDTO(true, 100)

    "putStudentModule" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(smRepository.getByStudentModule(userStudent, module1, lesson1)).willReturn(null)
        given(smRepository.saveAndFlush(any<StudentModule>())).willReturn(sm1)

        service.putStudentModule(lesson1.id, module1.id, smedto, userStudentAuthDto) shouldBe StudentModuleReadDTO(
            smedto.percent, sm1.completedEarly, sm1.completedOn
        )

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(smRepository).getByStudentModule(userStudent, module1, lesson1)
        verify(smRepository).saveAndFlush(any<StudentModule>())
    }

    val smrqedto = StudentModuleRequestEditDTO("Help me", StudentModuleRequestType.HELP)

    "putStudentModuleRequest" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(smRepository.getByStudentModule(userStudent, module1, lesson1)).willReturn(sm1)

        service.putStudentModuleRequest(lesson1.id, module1.id, smrqedto, userStudentAuthDto) shouldBe StudentModuleReadDTO(
            sm1.percent, sm1.completedEarly, sm1.completedOn
        )

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(smRepository).getByStudentModule(userStudent, module1, lesson1)
    }

    "deleteStudentModuleRequest" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(smRepository.getByStudentModule(userStudent, module1, lesson1)).willReturn(sm1)
        given(smrqRepository.getByStudentModule(sm1, true)).willReturn(smrq1)
        doNothing().`when`(smrqRepository).delete(smrq1)

        service.deleteStudentModuleRequest(lesson1.id, module1.id, userStudentAuthDto)

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(smRepository).getByStudentModule(userStudent, module1, lesson1)
        verify(smrqRepository).getByStudentModule(sm1, true)
        verify(smrqRepository).delete(smrq1)
    }

    "deleteStudentModuleRequest_404" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(smRepository.getByStudentModule(userStudent, module1, lesson1)).willReturn(sm1)
        given(smrqRepository.getByStudentModule(sm1, true)).willThrow(JpaObjectRetrievalFailureException(EntityNotFoundException()))

        val e = shouldThrow<ResponseStatusException> {
            service.deleteStudentModuleRequest(lesson1.id, module1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.NOT_FOUND

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(smRepository).getByStudentModule(userStudent, module1, lesson1)
        verify(smrqRepository).getByStudentModule(sm1, true)
    }

    val smrqtdto = StudentModuleRequestTeacherDTO("Congratulations", true, 100)

    "putStudentModuleRequestTeacher" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(smRepository.getByStudentModule(userStudent, module1, lesson1)).willReturn(sm1)
        given(smrqRepository.getByStudentModule(sm1, true)).willReturn(smrq1)
        given(smrqRepository.saveAndFlush(any<StudentModuleRequest>())).willReturn(smrq1)
        given(smRepository.saveAndFlush(any<StudentModule>())).willReturn(sm1)

        service.putStudentModuleRequestTeacher(lesson1.id, module1.id, userStudent.id, smrqtdto, userAuthDto) shouldBe StudentModuleReadDTO(
            sm1.percent, sm1.completedEarly, sm1.completedOn
        )

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(smRepository).getByStudentModule(userStudent, module1, lesson1)
        verify(smrqRepository).getByStudentModule(sm1, true)
        verify(smrqRepository).saveAndFlush(any<StudentModuleRequest>())
        verify(smRepository).saveAndFlush(any<StudentModule>())
    }

    "putStudentModuleRequestTeacher_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.putStudentModuleRequestTeacher(lesson1.id, module1.id, user.id, smrqtdto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

    val mockFile = MockMultipartFile("Dummy", null)

    "putStudentModuleFile" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(smRepository.getByStudentModule(userStudent, module1, lesson1)).willReturn(sm1)
        given(smRepository.saveAndFlush(any<StudentModule>())).willReturn(sm1)

        service.putStudentModuleFile(lesson1.id, module1.id, mockFile, userStudentAuthDto) shouldBe StudentModuleReadDTO(
            sm1.percent, sm1.completedEarly, sm1.completedOn
        )

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(smRepository).getByStudentModule(userStudent, module1, lesson1)
        verify(fileService).saveFile(eq(mockFile), any(), eq(FileService.UPLOADS_PATH_STUDENTS))
        verify(smRepository).saveAndFlush(any<StudentModule>())
    }

    val emptyByteArray = ByteArray(100) { 0 }

    "getStudentModuleFile" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(smRepository.getByStudentModule(userStudent, module1, lesson1)).willReturn(sm1.copy(file = "file.tar"))
        given(fileService.readFile("file.tar", FileService.UPLOADS_PATH_STUDENTS)).willReturn(emptyByteArray)

        service.getStudentModuleFile(lesson1.id, module1.id, userStudentAuthDto) shouldBe emptyByteArray

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(smRepository).getByStudentModule(userStudent, module1, lesson1)
    }

    "getStudentModuleFile_teacher" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(smRepository.getByStudentModule(userStudent, module1, lesson1)).willReturn(sm1.copy(file = "file.tar"))
        given(fileService.readFile("file.tar", FileService.UPLOADS_PATH_STUDENTS)).willReturn(emptyByteArray)

        service.getStudentModuleFile(lesson1.id, module1.id, userStudent.id, userAuthDto) shouldBe emptyByteArray

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(smRepository).getByStudentModule(userStudent, module1, lesson1)
    }

    "getStudentModuleFile_teacher_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.getStudentModuleFile(lesson1.id, module1.id, user.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

    "deleteStudentModule" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)
        given(moduleRepository.getReferenceById(module1.id)).willReturn(module1)
        given(smRepository.getByStudentModule(user, module1, lesson1)).willReturn(sm1)
        doNothing().`when`(smRepository).delete(sm1)

        service.deleteUserModule(lesson1.id, module1.id, userAuthDto)

        verify(lessonRepository).getReferenceById(lesson1.id)
        verify(moduleRepository).getReferenceById(module1.id)
        verify(smRepository).getByStudentModule(user, module1, lesson1)
        verify(smRepository).delete(sm1)
    }

    "deleteStudentModule_403" {
        given(lessonRepository.getReferenceById(lesson1.id)).willReturn(lesson1)

        val e = shouldThrow<ResponseStatusException> {
            service.deleteUserModule(lesson1.id, module1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(lessonRepository).getReferenceById(lesson1.id)
    }

})