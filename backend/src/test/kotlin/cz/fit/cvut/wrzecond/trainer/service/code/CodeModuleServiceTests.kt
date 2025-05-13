package cz.fit.cvut.wrzecond.trainer.service.code

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.dto.code.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.code.*
import cz.fit.cvut.wrzecond.trainer.repository.LessonModuleRepository
import cz.fit.cvut.wrzecond.trainer.repository.ModuleRepository
import cz.fit.cvut.wrzecond.trainer.repository.StudentModuleRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.repository.code.CodeModuleFileRepository
import cz.fit.cvut.wrzecond.trainer.repository.code.CodeModuleRepository
import cz.fit.cvut.wrzecond.trainer.repository.code.CodeModuleTestRepository
import cz.fit.cvut.wrzecond.trainer.service.UserService
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.ConverterService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.mockito.Answers
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [CodeModuleService::class])
class CodeModuleServiceTests(
    @MockBean val repository: CodeModuleRepository,
    @MockBean val cmtRepository: CodeModuleTestRepository,
    @MockBean val cmfRepository: CodeModuleFileRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    @MockBean val moduleRepository: ModuleRepository,
    @MockBean val lessonModuleRepository: LessonModuleRepository,
    @MockBean val studentModuleRepository: StudentModuleRepository,
    @MockBean val userService: UserService,
    service: CodeModuleService
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
        LessonType.TUTORIAL_PREPARATION, null, null, weekDummy, emptyList(), emptyList(), emptyList(),1)
    val module1Dummy = Module("Dummy", ModuleType.TEXT, "Text of module", ModuleDifficulty.EASY,
        false, false, false, false, 100, null, Timestamp.from(Instant.now()),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),user, 1)
    val lm1 = LessonModule(lesson1Dummy, module1Dummy, 1, null, 1)
    val module1 = module1Dummy.copy(lessons = listOf(lm1))
    val cm1Dummy = CodeModule(CodeType.SHOWCASE, InteractionType.EDITOR, LibraryType.LIB_C, EnvelopeType.ENV_C_IO,
        null, "tester", 2048,false,false, emptyList(), emptyList(), module1, 12)

    val cmt1 = CodeModuleTest("Test 1", 1, "Basic test", null, false, false,
        false, null, cm1Dummy, 100)
    val cmf1 = CodeModuleFile("File 1", 1024, "Default code", "Ref code", false,
        cm1Dummy, 1000)
    val cm1 = cm1Dummy.copy(tests = listOf(cmt1), files = listOf(cmf1))

    val cmt1Dto = CodeModuleTestFindDTO(cmt1.id, cmt1.id, cmt1.name, cmt1.parameter, cmt1.description,
        cmt1.timeLimit, cmt1.checkMemory, cmt1.shouldFail, cmt1.hidden)
    val cmf1Dto = CodeModuleFileFindDTO(cmf1.id, cmf1.id, cmf1.name, cmf1.codeLimit, cmf1.content, cmf1.reference, cmf1.headerFile)
    val dto = CodeModuleGetDTO(cm1.id, cm1.codeType, cm1.interactionType, cm1.codeHidden, cm1.referencePublic, cm1.fileLimit,
        cm1.hideCompilerOutput, cm1.libraryType, cm1.envelopeType, cm1.customEnvelope, listOf(cmt1Dto), listOf(cmf1Dto))

    beforeTest {
        reset(repository)
        reset(cmfRepository)
        reset(cmtRepository)
    }

    "getByID" {
        given(repository.getByModuleId(module1.id)).willReturn(cm1)

        service.getByID(module1.id, userStudentAuthDto) shouldBe dto

        verify(repository).getByModuleId(module1.id)
    }

    val cmtcdto = CodeModuleTestEditDTO(cmt1.id, cmt1.id, "EditedName", 101, cmt1.description,
        cmt1.timeLimit, cmt1.checkMemory, true, true)
    val cmfcdto = CodeModuleFileEditDTO(cmf1.id, cmf1.id, cmf1.name, 666, "EditedContent",
        cmf1.reference, cmf1.headerFile)
    val udto = CodeModuleUpdateDTO(CodeType.WRITE_ASSERT, cm1.interactionType, cm1.codeHidden,
        cm1.referencePublic, cm1.fileLimit, cm1.hideCompilerOutput, cm1.libraryType, cm1.envelopeType, cm1.customEnvelope,
        listOf(cmtcdto), listOf(cmfcdto))

    "update" {
        given(repository.getByModuleId(module1.id)).willReturn(cm1)
        given(repository.saveAndFlush(any<CodeModule>())).willReturn(cm1)
        given(cmtRepository.saveAllAndFlush(any<List<CodeModuleTest>>())).willReturn(listOf(cmt1))
        given(cmfRepository.saveAllAndFlush(any<List<CodeModuleFile>>())).willReturn(listOf(cmf1))
        given(repository.getReferenceById(cm1.id)).willReturn(cm1)

        service.update(module1.id, udto, userAuthDto) shouldBe dto

        verify(repository).getByModuleId(module1.id)
        verify(repository).saveAndFlush(any<CodeModule>())
        verify(cmtRepository).saveAllAndFlush(any<List<CodeModuleTest>>())
        verify(cmfRepository).saveAllAndFlush(any<List<CodeModuleFile>>())
        verify(repository).getReferenceById(cm1.id)
    }

    "update_403" {
        given(repository.getByModuleId(module1.id)).willReturn(cm1)

        val e = shouldThrow<ResponseStatusException> {
            service.update(module1.id, udto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(repository).getByModuleId(module1.id)
    }

    "delete" {
        given(repository.getByModuleId(module1.id)).willReturn(cm1)
        doNothing().`when`(repository).delete(cm1)

        service.delete(module1.id, userAuthDto)

        verify(repository).getByModuleId(module1.id)
        verify(repository).delete(cm1)
    }

    "delete_403" {
        given(repository.getByModuleId(module1.id)).willReturn(cm1)

        val e = shouldThrow<ResponseStatusException> {
            service.delete(module1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(repository).getByModuleId(module1.id)
    }

    "deleteTest" {
        given(cmtRepository.getReferenceById(cmt1.id)).willReturn(cmt1)
        doNothing().`when`(cmtRepository).delete(cmt1)

        service.deleteTest(cmt1.id, userAuthDto)

        verify(cmtRepository).getReferenceById(cmt1.id)
        verify(cmtRepository).delete(cmt1)
    }

    "deleteTest_403" {
        given(cmtRepository.getReferenceById(cmt1.id)).willReturn(cmt1)

        val e = shouldThrow<ResponseStatusException> {
            service.deleteTest(cmt1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(cmtRepository).getReferenceById(cmt1.id)
    }

    "deleteFile" {
        given(cmfRepository.getReferenceById(cmf1.id)).willReturn(cmf1)
        doNothing().`when`(cmfRepository).delete(cmf1)

        service.deleteFile(cmf1.id, userAuthDto)

        verify(cmfRepository).getReferenceById(cmf1.id)
        verify(cmfRepository).delete(cmf1)
    }

    "deleteFile_403" {
        given(cmfRepository.getReferenceById(cmf1.id)).willReturn(cmf1)

        val e = shouldThrow<ResponseStatusException> {
            service.deleteFile(cmf1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(cmfRepository).getReferenceById(cmf1.id)
    }


})