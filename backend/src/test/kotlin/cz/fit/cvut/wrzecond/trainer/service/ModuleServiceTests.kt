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
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@SpringBootTest(classes = [ModuleService::class])
class ModuleServiceTests(
    @MockBean val repository: ModuleRepository,
    @MockBean val moduleEditorRepository: ModuleEditorRepository,
    @MockBean val smRepository: StudentModuleRepository,
    @MockBean val userRepository: UserRepository,
    @MockBean val topicRepository: TopicRepository,
    @MockBean val moduleTopicRepository: ModuleTopicRepository,
    @MockBean val subjectRepository: SubjectRepository,
    @MockBean val teacherNoteRepository: TeacherNoteRepository,
    @MockBean val studentRatingRepository: StudentRatingRepository,
    @MockBean val subjectGuarantorRepository: SubjectGuarantorRepository,
    @MockBean val authorizationService: AuthorizationService,
    @MockBean val moduleSubjectRepository: ModuleSubjectRepository,
    @MockBean(answer = Answers.CALLS_REAL_METHODS) val converterService: ConverterService,
    @MockBean val fileService: FileService,
    @MockBean val logRepository: LogRepository,
    @MockBean val logService: LogService,
    @MockBean val lessonModuleRepository: LessonModuleRepository,
    @MockBean val userService: UserService,

    val service: ModuleService
): StringSpec({

    val subject1 = Subject("Programming and Algorithms 1", "BI-PA1.21", emptyList(), emptyList(),emptyList())
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

    given(authorizationService.isTrusted(eq(user), any())).willReturn(true)
    given(authorizationService.isTrusted(eq(user))).willReturn(true)
    given(authorizationService.isTrusted(eq(userStudent), any())).willReturn(false)
    given(authorizationService.isTrusted(eq(userStudent))).willReturn(false)

    val topicDummy = Topic("Programming", emptyList(), emptyList(), 1)
    val weekDummy = Week("First week", Timestamp.from(Instant.now()), Timestamp.from(Instant.now()),
        course, emptyList(), 1)
    val lesson1Dummy = Lesson("Compilation", false, 1, null, null, "Assignment",
        LessonType.TUTORIAL_PREPARATION, null, null, weekDummy, emptyList(), emptyList(),emptyList(),1)
    val lesson2Dummy = Lesson("First program", true, 2, Timestamp.from(Instant.now()),
        Timestamp.from(Instant.now().plusMillis(24 * 3600 * 1000)), "Another description",
        LessonType.TUTORIAL, "secret", null, weekDummy, emptyList(), emptyList(),emptyList(),2)

    val module1Dummy = Module("Dummy", ModuleType.TEXT, "Text of module", ModuleDifficulty.EASY,
        false, false, false, false, 100, null, Timestamp.from(Instant.now()),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),user, 1)
    val module2Dummy = Module("Another", ModuleType.QUIZ, "Some quiz module", null,
        true, false, false, false, 50, null, Timestamp.from(Instant.now()),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),emptyList(),user, 2)
    val lm1 = LessonModule(lesson1Dummy, module1Dummy, 1, null,1)
    val lm2 = LessonModule(lesson2Dummy, module2Dummy, 1, null,2)
    val tm1 = ModuleTopic(module1Dummy, topicDummy, 1)
    val sm1 = ModuleSubject(module1Dummy, subject1, 1)
    val module1 = module1Dummy.copy(lessons = listOf(lm1))
    val module2 = module2Dummy.copy(lessons = listOf(lm2))
    val module3 = module2Dummy.copy(subjects = listOf(sm1), topics = listOf(tm1), id = 3)

    val mfdto1 = ModuleFindDTO(module1.id, module1.name, emptyList(), emptyList(),
        module1.type, module1.assignment, module1.difficulty,
        module1.lockable, module1.lockable, module1.timeLimit, module1.manualEval, module1.hidden,
        module1.minPercent, module1.file, lm1.dependsOn?.id, module1.author.username, emptyList(), null, null, null,
        null, lm1.order, null, false, emptyList(), emptyList())
    val mfdto2 = ModuleFindDTO(module2.id, module2.name, emptyList(), emptyList(),
        module2.type, module2.assignment, module2.difficulty,
        module2.lockable, module2.lockable, module2.timeLimit, module2.manualEval, module2.hidden,
        module2.minPercent, module2.file, lm2.dependsOn?.id, module2.author.username, emptyList(), null, null, null,
        null, lm2.order, null, false, emptyList(), emptyList())

    val curdto = CourseUserReadDTO(user.id, user.username, user.name, courseUser.role.level, null)
    val mgdto1 = ModuleGetDTO(module1.id, module1.name, lm1.dependsOn?.id, curdto, emptyList(),
        emptyList(), emptyList(), module1.type,
        module1.lastModificationTime, module1.difficulty, module1.assignment, module1.minPercent, module1.lockable,
        module1.timeLimit, module1.manualEval, module1.hidden,emptyList())
    val mgdto2 = ModuleGetDTO(module2.id, module2.name, lm2.dependsOn?.id, curdto, emptyList(),
        emptyList(), emptyList(), module2.type,
        module2.lastModificationTime, module2.difficulty, module2.assignment, module2.minPercent, module2.lockable,
        module2.timeLimit, module2.manualEval, module2.hidden, emptyList()
    )
    val sfdto1 = SubjectFindDTO(subject1.id, subject1.name, subject1.code)
    val tfdto1 = TopicFindDTO(topicDummy.id, topicDummy.name)

    beforeTest { // Reset counters
        reset(repository)
    }

    "findAll" {
        given(repository.findAll()).willReturn(listOf(module1, module2))

        service.findAll(userAuthDto) shouldBe listOf(mfdto1.copy(order = null, hasStudentFile = null),
            mfdto2.copy(locked = false, order = null, hasStudentFile = null))

        verify(repository).findAll()
    }

    "findAll_403" {
        val e = shouldThrow<ResponseStatusException> {
            service.findAll(userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }
    
    "getByID" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)
        given(repository.getReferenceById(module2.id)).willReturn(module2)

        service.getByID(module1.id, userAuthDto) shouldBe mgdto1
        service.getByID(module2.id, userAuthDto) shouldBe mgdto2

        verify(repository).getReferenceById(module1.id)
        verify(repository).getReferenceById(module2.id)
    }

    "getByID_student" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)
        given(repository.getReferenceById(module2.id)).willReturn(module2)

        service.getByID(module1.id, userStudentAuthDto) shouldBe mgdto1 // no problem

        val e = shouldThrow<ResponseStatusException> {
            service.getByID(module2.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN // hidden lesson

        verify(repository).getReferenceById(module1.id)
        verify(repository).getReferenceById(module2.id)
    }

    "getByID_401" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.getByID(module1.id, null)
        }
        e.statusCode shouldBe HttpStatus.UNAUTHORIZED

        verify(repository).getReferenceById(module1.id)
    }

    val cdto = ModuleCreateDTO(module1.name, module1.type, emptyList(), module1.minPercent, module1.difficulty,
        module1.assignment, module1.lockable, module1.timeLimit, module1.manualEval, module1.hidden)

    "create" {
        given(repository.save(any<Module>())).willReturn(module1)

        service.create(cdto, userAuthDto) shouldBe mgdto1

        verify(repository).save(any())
    }

    "create_403" {
        val e = shouldThrow<ResponseStatusException> {
            service.create(cdto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    val updateDto = ModuleUpdateDTO("UpdatedName", null, 77, null,
        null, null, null, null, true, Timestamp.from(Instant.now()))

    "update" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)
        given(repository.save(any<Module>())).willReturn(module1.copy(name = updateDto.name ?: "",
            minPercent = updateDto.minPercent ?: 0, hidden = updateDto.hidden ?: false))

        service.update(module1.id, updateDto, userAuthDto) shouldBe mgdto1.copy(name = updateDto.name ?: "",
            minPercent = updateDto.minPercent ?: 0, hidden = updateDto.hidden ?: false)

        verify(repository).getReferenceById(module1.id)
        verify(repository).save(any())
    }

    "update_403" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.update(module1.id, updateDto, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(repository).getReferenceById(module1.id)
    }

    "delete" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)
        doNothing().`when`(repository).delete(any())

        service.delete(module1.id, userAuthDto)

        verify(repository).getReferenceById(module1.id)
        verify(repository).delete(any())
    }

    "delete_403" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.delete(module1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(repository).getReferenceById(module1.id)
    }

    "getModuleTeachers" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)
        given(repository.getModuleTeachers(module1)).willReturn(listOf(user))

        service.getModuleTeachers(module1.id, userAuthDto) shouldBe listOf(curdto)

        verify(repository).getReferenceById(module1.id)
        verify(repository).getModuleTeachers(module1)
    }

    "getModuleTeachers_403" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.getModuleTeachers(module1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(repository).getReferenceById(module1.id)
    }

    val mockFile = MockMultipartFile("Dummy", null)

    "postFile" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)
        given(repository.saveAndFlush(any<Module>())).willReturn(module1.copy(file = "filename"))

        service.postFile(module1.id, mockFile, userAuthDto) shouldBe mgdto1

        verify(repository).getReferenceById(module1.id)
        verify(fileService).saveFile(eq(mockFile), any(), eq(FileService.UPLOADS_PATH))
        verify(repository).saveAndFlush(any())
    }

    "postFile_403" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.postFile(module1.id, mockFile, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(repository).getReferenceById(module1.id)
    }

    val emptyByteArray = ByteArray(100) { 0 }

    "getFile" {
        given(repository.getReferenceById(module1.id)).willReturn(module1.copy(file = "file.tar"))
        given(fileService.readFile("file.tar", FileService.UPLOADS_PATH)).willReturn(emptyByteArray)

        service.getFile(module1.id, userStudentAuthDto) shouldBe emptyByteArray

        verify(repository).getReferenceById(module1.id)
        verify(fileService).readFile("file.tar", FileService.UPLOADS_PATH)
    }

    "findByTopics" {
        given(repository.findByTopics(listOf(topicDummy.id))).willReturn(listOf(module3))

        service.findByTopics(listOf(topicDummy.id), userAuthDto) shouldBe listOf(mgdto2.copy(subjects = listOf(sfdto1), topics = listOf(tfdto1), id = 3))

        verify(repository).findByTopics(listOf(topicDummy.id))
    }

    "addTopic_403" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.addTopic(module1.id, topicDummy.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(repository).getReferenceById(module1.id)
    }

    "getModuleTopics"{
        given(repository.getReferenceById(module3.id)).willReturn(module3)

        service.getModuleTopics(module3.id, userAuthDto) shouldBe listOf(tfdto1.id)

        verify(repository).getReferenceById(module3.id)
    }

    "findBySubjects" {
        given(repository.findBySubjects(listOf(subject1.id))).willReturn(listOf(module3))

        service.findBySubjects(listOf(subject1.id), userAuthDto) shouldBe listOf(mgdto2.copy(subjects = listOf(sfdto1), topics = listOf(tfdto1), id = 3))

        verify(repository).findBySubjects(listOf(subject1.id))
    }

    "addSubject_403" {
        given(repository.getReferenceById(module1.id)).willReturn(module1)

        val e = shouldThrow<ResponseStatusException> {
            service.addSubject(module1.id, subject1.id, userStudentAuthDto)
        }
        e.statusCode shouldBe HttpStatus.FORBIDDEN

        verify(repository).getReferenceById(module1.id)
    }

    "getModuleSubjects"{
        given(repository.getReferenceById(module3.id)).willReturn(module3)

        service.getModuleSubjects(module3.id, userAuthDto) shouldBe listOf(sfdto1.id)

        verify(repository).getReferenceById(module3.id)
    }

})