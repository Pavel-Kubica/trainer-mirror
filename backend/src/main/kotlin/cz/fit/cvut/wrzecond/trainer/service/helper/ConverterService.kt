package cz.fit.cvut.wrzecond.trainer.service.helper

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.dto.code.*
import cz.fit.cvut.wrzecond.trainer.dto.quiz.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.entity.code.CodeModule
import cz.fit.cvut.wrzecond.trainer.entity.code.CodeModuleFile
import cz.fit.cvut.wrzecond.trainer.entity.code.CodeModuleTest
import cz.fit.cvut.wrzecond.trainer.entity.code.EnvelopeType
import cz.fit.cvut.wrzecond.trainer.entity.quiz.AnswerStatusType
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Question
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuizQuestion
import cz.fit.cvut.wrzecond.trainer.entity.quiz.StudentAnswer
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuestionRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime

/**
 * Service class responsible for converting various entities and DTOs.
 *
 * This service handles the transformation of entities like Subject, Semester, Week, User, etc.,
 * into their corresponding DTO representations and vice versa.
 *
 * @property authorizationService Service responsible for handling authorization logic.
 * @property moduleRepository Repository for managing Module entities.
 * @property smRepository Repository for managing Student Module entities.
 * @property smrqRepository Repository for managing Student Module Request entities.
 * @property semesterRepository Repository for managing Semester entities.
 * @property subjectRepository Repository for managing Subject entities.
 * @property studentRatingRepository Repository for managing Student Rating entities.
 * @property studentModuleRepository Repository for managing StudentModule entities.
 * @property questionRepository Repository for managing Question entity
 * @property quizroomRepository Repository for managing Quizroom entity
 * @property userRepository Repository for managing User entities.
 */
@Service
class ConverterService(
    private val authorizationService: AuthorizationService,
    private val moduleRepository: ModuleRepository,
    private val smRepository: StudentModuleRepository,
    private val smrqRepository: StudentModuleRequestRepository,
    private val semesterRepository: SemesterRepository,
    private val subjectRepository: SubjectRepository,
    private val studentRatingRepository: StudentRatingRepository,
    private val studentModuleRepository: StudentModuleRepository,
    private val questionRepository: QuestionRepo,
    private val quizroomRepository: QuizroomRepo,
    private val userRepository: UserRepository,
    private val lessonRepository: LessonRepository
) {

    // === FIND DTO ===

    /**
     * Converts a Subject entity into a SubjectFindDTO.
     *
     * @param subject The Subject entity to be converted
     * @return A SubjectFindDTO containing the id, name, and code of the subject
     */
    fun toFindDTO(subject: Subject) = subject.run { SubjectFindDTO(id, name, code) }

    /**
     * Converts a Semester entity into a SemesterFindDTO.
     *
     * @param semester The Semester entity to be converted; it can be null
     * @return A SemesterFindDTO containing the id, code, from, and until of the semester, or null if the input semester is null
     */
    fun toFindDTO(semester: Semester?) = semester?.run { SemesterFindDTO(id, code, from, until) }

    /**
     * Converts a Week entity and optionally a User entity into a WeekFindDTO.
     *
     * @param week The Week entity to be converted
     * @param user The User entity to be optionally considered in the conversion
     * @return A WeekFindDTO containing the id, name, from, until, and CourseFindDTO
     */
    fun toFindDTO(week: Week, user: User?) = week.run {
        WeekFindDTO(id, name, from, until, toFindDTO(course, user))
    }

    /**
     * Converts a TeacherNote entity into a TeacherNoteFindDTO.
     *
     * @param teacherNote The TeacherNote entity to be converted.
     * @param user An optional User entity to be considered in the conversion. Default is null.
     * @return A TeacherNoteFindDTO containing the id, content, created timestamp, author, and redacted status of the teacher note.
     */
    fun toFindDTO(teacherNote: TeacherNote, user: User? = null) = teacherNote.run {
        TeacherNoteFindDTO(id, content, created, toFindDTO(author), redacted)
    }

    fun toFindDTO(question: Question) = question.run{
            QuestionFindDTO(id, questionData, possibleAnswersData, timeLimit, author.username, singleAnswer, questionType,
                topics.map { toFindDTO(it.topic) }, subjects.map { toFindDTO(it.subject) })
        }

    /**
     * Converts a Lesson entity and an optional User entity into a LessonFindDTO.
     *
     * @param lesson The Lesson entity to be converted.
     * @param user The User entity to be optionally considered in the conversion.
     *        If provided, it will be used to check for edit permissions and
     *        calculate the progress percentage.
     *@return LessonFindDTO.
     */
    fun toFindDTO(lesson: Lesson, user: User?) = lesson.run {
        LessonFindDTO(
            id, name, hidden, order, type, if (user != null && canEdit(user)) lockCode else null, timeEnd,
            modules.filter { it.module.type.hasProgress }.map {
                it.module.students.find { sm ->
                    sm.student.id == user?.id && sm.lesson.id == id
                }?.percent ?: 0
            }.average().toInt(),
            0
        )
    }

    /**
     * Converts a LessonModule entity and an optional User entity into a ModuleFindDTO.
     *
     * @param lm The LessonModule entity to be converted.
     * @param user The User entity to be optionally considered in the conversion. Default is null.
     *             If provided, it will be used to determine certain aspects of the module's visibility and status.
     * @return ModuleFindDTO..
     */
    fun toFindDTO(lm: LessonModule, user: User? = null) = lm.run {
        val studentModule = user?.let { smRepository.getByStudentModule(it, module, lesson) }
        val studentModuleRequest = studentModule?.let { smrqRepository.getByStudentModule(it) }

        // user = null means we're in teacher mode
        if (user != null && module.lockable && lesson.lockCode != null && studentModule?.unlocked != true)
            ModuleFindDTO(
                module.id, module.name, emptyList(), emptyList(),
                module.type, "", null,
                true, true, false, false, false, 0, null, null,
                "", emptyList(), false, false, null, null,
                order, null, null, emptyList(), emptyList()
            )
        else ModuleFindDTO(
            module.id, module.name,
            module.subjects.map { toFindDTO(it.subject) }, module.topics.map { toFindDTO(it.topic) },
            module.type, module.assignment, module.difficulty,
            false, module.lockable, module.timeLimit, module.manualEval, module.hidden, module.minPercent, module.file,
            dependsOn?.id, module.author.username, module.editors.map { it.editor.username },
            studentModule?.allowedShow, studentModule?.completedEarly, studentModule?.completedOn,
            studentModuleRequest?.let { toStudentRequestDTO(it) },
            // smrqRepository.getLastTeacherComment(studentModule)?.let { toTeacherCommentDTO(it) },
            order, studentModule?.percent, studentModule?.file != null,module.students.map { toFindDTO(it) },
            module.ratings.map { toFindDTO(it) }
        )
    }

    /**
     * Converts a Course entity and an optional User entity into a CourseFindDTO.
     *
     * @param course The Course entity to be converted.
     * @param user The optional User entity; if provided, it is used to determine the user's role and progress in the course.
     * @return A CourseFindDTO containing the id, name, short name, subject, semester, number of completed lessons,
     *         total number of lessons, and the user's role level in the course.
     */
    fun toFindDTO(course: Course, user: User?) = course.run {
        weeks.flatMap { it.lessons }
            .filter { l -> l.modules.any { md -> md.module.type.hasProgress } }
            .let { lessons ->
                CourseFindDTO(
                    id, name, shortName, subject?.let { toFindDTO(it) }, toFindDTO(semester),
                    lessons.count { !it.hidden && toFindDTO(it, user).progress == 100 }, lessons.count { !it.hidden },
                    user?.courses?.firstOrNull { it.course.id == id }?.role?.level
                )
            }
    }

    /**
     * Converts a Course entity and a Semester entity into a SemesterCourseReadDTO.
     *
     * @param course The Course entity to be converted.
     * @param semester The Semester entity to be converted.
     * @param user The optional User entity to be considered in the conversion.
     * @return A SemesterCourseReadDTO containing the converted properties from the Course and Semester entities.
     */
    fun toFindDTO(course: Course, semester: Semester, user: User?) =
        SemesterCourseReadDTO(
            course.id,
            course.name,
            course.shortName,
            course.subject?.let { toFindDTO(it) },
            toFindDTO(course.semester)
        )

    /**
     * Converts a User entity into a UserFindDTO.
     *
     * @param user The User entity to be converted
     * @return A UserFindDTO containing the id, username, and name of the user
     */
    fun toFindDTO(user: User) = user.run { UserFindDTO(id, username, name) }

    /**
     * Converts a Module entity into a ModuleFindDTO.
     *
     * @param module The Module entity to be converted.
     * @return A ModuleFindDTO containing the details of the module such as id, name, subjects, topics, type,
     *         assignment, difficulty, lockable status, time limit, manual evaluation status, hidden status,
     *         minimum percentage, file information, author, editors, students, and ratings.
     */
    fun toFindDTO(module: Module) = module.run {
        ModuleFindDTO(
            id, name, subjects.map { toFindDTO(it.subject) }, topics.map { toFindDTO(it.topic) },
            type, assignment, difficulty, false, lockable,
            timeLimit, manualEval, hidden, minPercent, file, null, author.username,

            editors.map { it.editor.username }, null, null,
            null, null, null,
            null, null, students.map { toFindDTO(it)}, ratings.map { toFindDTO(it)}
        )
    }

    /**
     * Converts a Template entity into a TemplateFindDTO.
     *
     * @param template The Template entity to be converted.
     * @return A TemplateFindDTO.
     */
    fun toFindDTO(template: Template) = template.run {
        TemplateFindDTO(
            id, name, codeType, libraryType, interactionType, envelopeType, customEnvelope, codeHidden,
            fileLimit, tests.map { toGetDTO(it) }, lastModificationTime, toFindDTO(author)
        )
    }

    /**
     * Converts a Topic entity into a TopicFindDTO.
     *
     * @param topic The Topic entity to be converted.
     * @return A TopicFindDTO containing the id and name of the topic.
     */
    fun toFindDTO(topic: Topic) = topic.run { TopicFindDTO(id, name) }

    /**
     * Converts a CodeComment entity into a CodeCommentFindDTO.
     *
     * @param codeComment The CodeComment entity to be converted.
     * @return A CodeCommentFindDTO containing the id, file name, row number, and comment of the code comment.
     */
    fun toFindDTO(codeComment: CodeComment) = codeComment.run { CodeCommentFindDTO(id, fileName, rowNumber, comment) }

    /**
     * Converts a StudentModule entity into a StudentModuleFindDTO.
     *
     * @param studentModule The StudentModule entity to be converted.
     * @return A StudentModuleFindDTO containing the id and the corresponding StudentFindDTO.
     */
    fun toFindDTO(studentModule: StudentModule) = studentModule.run { StudentModuleFindDTO(id,toFindDTO(student)) }

    /**
     * Converts a StudentRating entity into a StudentRatingFindDTO.
     *
     * @param studentRating The StudentRating entity to be converted
     * @return A StudentRatingFindDTO containing the id, points, text, published status, and the corresponding StudentFindDTO
     */
    fun toFindDTO(studentRating: StudentRating) = studentRating.run { StudentRatingFindDTO(id,points,text,published,toFindDTO(student))}

    fun toFindDTO(studentAnswer: StudentAnswer) = studentAnswer.run {
        StudAnswerFindDTO(id, quizroom?.id, student.id, question.id, answerData)
    }

    fun toFindDTO(scoringRule: ScoringRule, user: User? = null) = scoringRule.run {
        if (user != null){
            ScoringRuleFindDTO(id, name, shortName, description, points, until, toComplete, lesson.id,
                modules.map { toGetDTO(it.module)},
                lesson.students.filter{st -> st.lesson == lesson && st.student == user
                        && modules.any{srm -> srm.module == st.module}}.map{st -> toGetDTO(st)})
        }
        else{
            ScoringRuleFindDTO(id, name, shortName, description, points, until, toComplete, lesson.id,
                modules.map { toGetDTO(it.module)},
                lesson.students.filter{st -> st.lesson == lesson
                        && modules.any{srm -> srm.module == st.module}}.map{st -> toGetDTO(st)})
        }

    }

    /**
     * Converts a Log entity into a LogFindDTO.
     *
     * @param log The Log entity to be converted.
     * @return A LogFindDTO containing the id, username, ipAddress, client, timestamp, entity, entityId, and operation of the log.
     */
    fun toFindDTO(log: Log) = log.run {
        LogFindDTO(id, username, ipAddress, client, timestamp, entity, entityId, operation)
    }

    /**
     * Converts a Module object into a ModuleFindTableDTO.
     *
     * @param module the Module object to be converted.
     * @return A  ModuleFindTableDTO.
     */
    fun toFindDTOModule(module: Module) = module.run { ModuleFindTableDTO(id, name, subjects.map {toFindDTO(it.subject)}, topics.map { toFindDTO(it.topic) }, editors.map { it.editor.username }, type, author.username) }

    // === GET DTO

    /**
     * Converts a Subject to its corresponding SubjectGetDTO.
     *
     * @param subject the Subject to be converted.
     * @param user the User requesting the conversion, used to filter visible courses. Default is null.
     * @return SubjectGetDTO containing the relevant information from the subject and its viewable courses.
     */
    fun toGetDTO(subject: Subject, user: User? = null) = subject.run {
        SubjectGetDTO(id, name, code,
            courses.filter { it.canView(user) }.sortedBy { it.shortName }.map { toFindDTO(it, user) })
    }

    /**
     * Converts a Semester object into a SemesterGetDTO object.
     *
     * @param semester The Semester object to be transformed.
     * @param user An optional User object that can be provided for additional context. Defaults to null.
     * @return A SemesterGetDTO.
     */
    fun toGetDTO(semester: Semester, user: User? = null) = semester.run {
        SemesterGetDTO(id, code, from, until)
    }

    /**
     * Converts a Question object into a QuestionGetDTO object.
     *
     * @param question the Question object to be converted.
     * @param user an optional User object related to the Question.
     * @return QuestionGetDTO.
     */
    fun toGetDTO(question: Question, user: User? = null) = question.run {
        QuestionGetDTO(id, questionData, possibleAnswersData, timeLimit, author.username, singleAnswer, questionType)
    }

    /**
     * Converts a Course object into a CourseGetDTO.
     *
     * @param course The course entity to be converted.
     * @param user The user entity, which is optional. If provided, it is used to determine user's role and access rights in the course.
     * @return A CourseGetDTO object mapped from the given course.
     */
    fun toGetDTO(course: Course, user: User? = null) = course.run {
        user?.courses?.firstOrNull { it.course.id == id }?.role?.level.let { role ->
            CourseGetDTO(
                id, name, shortName, subject?.let { toFindDTO(it) }, toFindDTO(semester), role, public,
                if (user != null && authorizationService.isTrusted(user, course)) secret else null,
                weeks.sortedByDescending { it.from }.map { toGetDTO(it, user) }
            )
        }
    }

    /**
     * Converts a Week object and an optional User object to a WeekGetDTO.
     *
     * @param week The Week object to be converted.
     * @param user Optional User object that determines the visibility of lessons.
     * @return The corresponding WeekGetDTO object.
     */
    fun toGetDTO(week: Week, user: User? = null) = week.run {
        WeekGetDTO(id, name, from, until, toFindDTO(course, null),
            lessons.filter { it.canView(user) }.sortedBy { it.order }
                .map { toFindDTO(it, user) })
    }

    /**
     * Converts a Week object into a WeekDetailDTO object.
     *
     * @param week The Week object to be converted.
     * @param user The User object used to filter and determine view permissions for lessons, can be null.
     * @return The converted WeekDetailDTO object with the relevant details and filtered lessons.
     */
    fun toDetailDTO(week: Week, user: User?) = week.run {
        WeekDetailDTO(id, name, from, until, toFindDTO(course, null),
            lessons.filter { it.canView(user) }.sortedBy { it.order }
                .map { toGetDTO(it, user) })
    }

    /**
     * Converts a Lesson object to a LessonGetDTO.
     *
     * @param lesson The Lesson object to be converted.
     * @param user An optional User object to determine access permissions.
     * @param allModules A boolean indicating whether to include all modules, regardless of their hidden status.
     */
    fun toGetDTO(lesson: Lesson, user: User? = null, allModules: Boolean = false) = lesson.run {
        val canEdit = user == null || week.course.canEdit(user)
        LessonGetDTO(
            id, toFindDTO(week, user), name, hidden, type,
            if (canEdit) lockCode else null,
            timeStart, timeEnd, description, modules.filter { allModules || canEdit || !it.module.hidden }
                .map { toFindDTO(it, user) }.sortedBy { it.order }, scoringRules.map{toGetDTO(it,user)}
        )
    }

    /**
     * Converts a Module object to a ModuleGetDTO object.
     *
     * @param module An instance of the Module class containing module details
     *               to be transformed into the DTO.
     * @return A ModuleGetDTO instance populated with the relevant module details.
     */
    fun toGetDTO(module: Module) = module.run {
        ModuleGetDTO(
            id, name, null,
            CourseUserReadDTO(author.id, author.username, author.name, RoleLevel.TEACHER, null),
            editors.map { it.editor.id }, subjects.map { toFindDTO(it.subject) },
            topics.map { toFindDTO(it.topic) },
            type, lastModificationTime, difficulty, assignment,
            minPercent, lockable, timeLimit, manualEval, hidden, ratings.map { toFindDTO(it)}
        )
    }

    /**
     * Converts the given CodeModule instance to a CodeModuleGetDTO.
     *
     * @param codeModule The instance of CodeModule that needs to be converted.
     * @return A CodeModuleGetDTO.
     */
    fun toGetDTO(codeModule: CodeModule) = codeModule.run {
        CodeModuleGetDTO(id, codeType, interactionType, codeHidden, referencePublic,
            fileLimit, hideCompilerOutput, libraryType, envelopeType, customEnvelope,
            tests.map { toGetDTO(it) }.sortedBy { it.parameter }, files.map { toGetDTO(it) })
    }

    /**
     * Converts a CodeModuleTest object to a CodeModuleTestFindDTO object.
     *
     * @param codeModuleTest the CodeModuleTest object to be converted
     * @return A CodeModuleTestFindDTO.
     */
    fun toGetDTO(codeModuleTest: CodeModuleTest) = codeModuleTest.run {
        CodeModuleTestFindDTO(id, id, name, parameter, description, timeLimit, checkMemory, shouldFail, hidden)
    }

    fun toGetDTO(studentAnswer: StudentAnswer) = studentAnswer.run {
        StudAnswerGetDTO(id, quizroom?.id, student.id, question.id, answerData)
    }

    /**
     * Transforms a CodeModuleFile object into a CodeModuleFileFindDTO object using its properties.
     *
     * @param codeModuleFile The CodeModuleFile instance to be converted.
     * @return An instance of CodeModuleFileFindDTO with the properties copied from the provided codeModuleFile.
     */
    fun toGetDTO(codeModuleFile: CodeModuleFile) = codeModuleFile.run {
        CodeModuleFileFindDTO(id, id, name, codeLimit, content, reference, headerFile)
    }

    /**
     * Converts a Topic instance to a TopicFindDTO instance.
     *
     * @param topic the Topic instance to convert
     */
    fun toGetDTO(topic: Topic) = topic.run { TopicFindDTO(id, name) }

    /**
     * Converts a User object to a UserGetDto object.
     *
     * @param user The User object to be converted.
     * @return A UserGetDto object representing the input User.
     */
    fun toGetDTO(user: User) = user.run {
        UserGetDto(id, username, name, isAdmin)
    }

    /**
     * Converts a StudentModule object to a StudentModuleGetDTO object.
     *
     * @param studentModule The StudentModule object to be converted.
     * @return The corresponding StudentModuleGetDTO object.
     */
    fun toGetDTO(studentModule: StudentModule) = studentModule.run {
        StudentModuleGetDTO(id,toFindDTO(module),toFindDTO(student),toFindDTO(lesson,student),percent,openedOn,completedOn,allowedShow,completedEarly,unlocked,
        )
    }

    /**
     * Converts a StudentRating object into a StudentRatingGetDTO object.
     *
     * @param studentRating the StudentRating object that needs to be converted.
     * @return a StudentRatingGetDTO object containing the data from the given StudentRating object.
     */
    fun toGetDTO(studentRating: StudentRating) = studentRating.run {
        StudentRatingGetDTO(id,points,text,toFindDTO(student), toFindDTO(module))
    }

    /* fun toGetDTO(question: Question) = question.run {
         QuestionGetDTO(id, questionData, possibleAnswersData, timeLimit, author, singleAnswer, questionType )
     }*/

    fun toGetDTO(scoringRule: ScoringRule,user: User? = null) = scoringRule.run {
        if (user != null) {
            ScoringRuleGetDTO(id, name, shortName, description, points, until, toComplete,
                modules.map { toGetDTO(it.module) },
                lesson.students.filter { st ->
                    st.lesson == lesson && st.student == user
                            && modules.any { srm -> srm.module == st.module }
                }.map { st -> toGetDTO(st) })
        }
        else {
            ScoringRuleGetDTO(id,name,shortName, description, points, until, toComplete,
                modules.map { toGetDTO(it.module) },
                lesson.students.filter{st -> st.lesson == lesson
                        && modules.any{srm -> srm.module == st.module}}.map{st -> toGetDTO(st)})
        }

    }


    fun toGetDTO(lessonModule: LessonModule) = lessonModule.run {
        LessonModuleGetDTO(id,toFindDTO(module),toGetDTO(lesson),
            lesson.students.filter{it.lesson == lesson && it.module == module}.map{toGetDTO(it)}, order)
    }

    // === READ DTO

    /**
     * Converts a CourseUser and associated Course into a CourseUserReadDTO.
     *
     * @param courseUser The user participating in the course, containing details such as user info and role.
     * @param course The course which contains weeks and lessons with modules.
     * @return A CourseUserReadDTO including aggregated information such as the user's progress in the course.
     */
    fun toReadDTO(courseUser: CourseUser, course: Course) = courseUser.run {
        CourseUserReadDTO(
            user.id, user.username, user.name, role.level,
            course.weeks.flatMap { it.lessons }
                .filter { l -> l.modules.any { md -> md.module.type.hasProgress } } // only count from non-null lessons
                .mapNotNull { lesson -> toFindDTO(lesson, user).progress }
                .average().toInt()
        )
    }

    /**
     * Converts a StudentModule object to a StudentModuleReadDTO object.
     *
     * @param sm The StudentModule instance to be converted.
     */
    fun toReadDTO(sm: StudentModule) = sm.run {
        StudentModuleReadDTO(percent, completedEarly, completedOn)
    }

    /**
     * Converts a LessonModule object to a LessonModuleReadDTO.
     *
     * @param lm the LessonModule object to be converted.
     * @return a LessonModuleReadDTO containing the id, name, type, and order.
     */
    fun toReadDTO(lm: LessonModule) = lm.run {
        LessonModuleReadDTO(id, module.name, module.type, order)
    }

    /**
     * Converts a QuizQuestion object to a QuizQuestionReadDTO.
     *
     * @param qq The QuizQuestion object to be converted.
     * @return A QuizQuestionReadDTO containing relevant data from the QuizQuestion.
     */
    fun toReadDTO(qq: QuizQuestion) = qq.run {
        QuizQuestionReadDTO(id, question.questionData, question.questionType, orderNum)
    }

    fun toReadDTO(srm: ScoringRuleModule) = srm.run {
        ScoringRuleModuleReadDTO(id, module.id, scoringRule.id)
    }

    /**
     * Converts a StudentModuleRequest to a TeacherCommentDTO.
     *
     * @param smr the StudentModuleRequest to be converted
     * @return A TeacherCommentDTO containing the teacher's name and response
     */
    fun toTeacherCommentDTO(smr: StudentModuleRequest) = smr.run {
        TeacherCommentDTO(teacher?.name, teacherResponse)
    }

    /**
     * Converts a StudentModuleRequest object to a StudentRequestDTO.
     *
     * @param smr the StudentModuleRequest to be converted.
     * @return a StudentRequestDTO object consisting of the data from the provided StudentModuleRequest.
     */
    fun toStudentRequestDTO(smr: StudentModuleRequest) = smr.run {
        StudentRequestDTO(id, requestType, requestText, satisfied, if (satisfied) toTeacherCommentDTO(smr) else null)
    }

    /**
     * Converts a StudentModule object to a ModuleUserDTO.
     *
     * @param sm the StudentModule instance to be converted.
     * @return a ModuleUserDTO containing relevant data from the StudentModule instance.
     */
    fun toModuleUserDTO(sm: StudentModule) = sm.run {
        ModuleUserDTO(
            module.id, requests.firstOrNull { !it.satisfied }?.requestType,
            allowedShow, percent, completedEarly, completedOn
        )
    }

    // === CREATE DTO

    /**
     * Converts a SubjectCreateDTO object to a Subject entity.
     *
     * @param subject The SubjectCreateDTO instance to be converted.
     * @return A Subject entity.
     */
    fun toEntity(subject: SubjectCreateDTO) = subject.run {
        Subject(name, code, emptyList(), emptyList(), emptyList())
    }

    /**
     * Converts a CodeModuleCreateDTO object into a CodeModule entity.
     *
     * @param cm the CodeModuleCreateDTO object containing the information needed to create a CodeModule entity.
     * @return A CodeModule entity.
     */
    fun toEntity(cm: CodeModuleCreateDTO) = cm.run {
        moduleRepository.getReferenceById(moduleId).let {
            CodeModule(
                codeType, interactionType, libraryType, envelopeType, customEnvelope,
                codeHidden, fileLimit, hideCompilerOutput, referencePublic, emptyList(), emptyList(), it, it.id
            )
        }
    }

    fun toEntity(sa: StudAnswerCreateDTO, correct: AnswerStatusType) = sa.run {
        StudentAnswer(sa.data, correct,
            sa.quizroom?.let { quizroomRepository.getReferenceById(it) }, userRepository.getReferenceById(student),
            questionRepository.getReferenceById(question))
    }

    fun toEntity(question: QuestionCreateDTO) = question.run {
        Question(
            questionData, possibleAnswersData, correctAnswerData, explanation ?: "", singleAnswer,
            userRepository.getByUsername(author) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND),
            timeLimit, questionType, emptyList(), emptyList(), emptyList(), emptyList()
        )
    }

    /**
     * Converts a TeacherNoteCreateDTO object along with Module and User objects into a TeacherNote entity.
     *
     * @param teacherNote Data transfer object containing the teacher's note information to be converted.
     * @param module The module to which the teacher's note is related.
     * @param user The user who created the teacher's note.
     * @return A TeacherNote entity with the provided content, created timestamp, module, and user information.
     */
    fun toEntity(teacherNote: TeacherNoteCreateDTO, module: Module, user: User) = teacherNote.run {
        TeacherNote(content, created, false, module, user)
    }

    /**
     * Converts a CodeModuleTestEditDTO to a CodeModuleTest entity.
     *
     * @param cmt The data transfer object containing code module test information.
     * @param cm The code module associated with the test.
     * @return A CodeModuleTest entity constructed from the provided DTO and code module.
     */
    fun toEntity(cmt: CodeModuleTestEditDTO, cm: CodeModule) = cmt.run {
        CodeModuleTest(name, parameter, description, timeLimit, checkMemory, shouldFail, hidden, null, cm, cmt.realId ?: 0)
    }

    /**
     * Converts a CodeModuleTestEditDTO and a Template to a CodeModuleTest entity.
     *
     * @param cmt The data transfer object containing code module test information.
     * @param template The template to be associated with the CodeModuleTest entity.
     * @return A CodeModuleTest entity populated with data from the provided DTO and template.
     */
    fun toEntity(cmt: CodeModuleTestEditDTO, template: Template) = cmt.run {
        CodeModuleTest(name, parameter, description, timeLimit, checkMemory, shouldFail, hidden, template, null,cmt.realId ?: 0)
    }

    /**
     * Converts a CodeModuleFileEditDTO instance to a CodeModuleFile entity.
     *
     * @param cmf The Data Transfer Object representing the code module file edit.
     * @param cm The CodeModule instance associated with the file.
     * @return A new CodeModuleFile entity.
     */
    fun toEntity(cmf: CodeModuleFileEditDTO, cm: CodeModule) = cmf.run {
        CodeModuleFile(name, codeLimit, content, reference, headerFile, cm, cmf.realId ?: 0)
    }

    /**
     * Converts a ModuleCreateDTO and a User into a Module entity.
     *
     * @param module The data transfer object containing the information needed to create a module.
     * @param user The user who is creating the module.
     * @return A Module entity.
     */
    fun toEntity(module: ModuleCreateDTO, user: User) = module.run {
        val now = Timestamp.from(Instant.now())
        Module(
            name, type, assignment, difficulty, lockable, timeLimit, manualEval, hidden, minPercent,
            null, now, emptyList(), emptyList(), emptyList(),emptyList(),emptyList(), emptyList(),emptyList(),
            user
        )
    }

    /**
     * Converts a TemplateCreateDTO and User into a Template entity.
     *
     * @param template The TemplateCreateDTO object containing information to be converted.
     * @param user The User associated with the Template entity.
     * @return A Template entity.
     */
    fun toEntity(template: TemplateCreateDTO, user: User) = template.run {
        val now = Timestamp.from(Instant.now())
        Template(
            name, codeType, interactionType, libraryType, envelopeType, customEnvelope, codeHidden,
            fileLimit, now, emptyList(), userRepository.getReferenceById(author)
        )
    }

    /**
     * Converts a LessonCreateDTO and Week object into a Lesson entity.
     *
     * @param lesson The LessonCreateDTO instance containing the lesson data.
     * @param week The Week instance to which the lesson belongs.
     * @return A new Lesson entity populated with data from the provided LessonCreateDTO and Week instances.
     */
    fun toEntity(lesson: LessonCreateDTO, week: Week) = lesson.run {
        Lesson(
            name, hidden, order, timeStart, timeLimit, description, type,
            lockCode?.let { it.ifEmpty { null } }, week, emptyList(),emptyList(), emptyList()
        )
    }

    /**
     * Converts a SemesterCreateDTO object to a Semester entity.
     *
     * @param semester a data transfer object containing information about the semester to be converted.
     * @return a Semester entity corresponding to the provided data transfer object.
     */
    fun toEntity(semester: SemesterCreateDTO) = semester.run {
        Semester(code, from, until, emptyList())
    }

    /**
     * Converts a CourseCreateDTO instance to a Course entity.
     *
     * @param course The CourseCreateDTO containing the data to be transformed.
     * @return A Course entity with the data from the provided CourseCreateDTO.
     */
    fun toEntity(course: CourseCreateDTO) = course.run {
        Course(
            name = name,
            shortName = shortName,
            public = public,
            secret = null,
            subject = subject?.let { subjectRepository.getReferenceById(it) },
            semester = semester?.let { semesterRepository.getReferenceById(it) },
            weeks =  emptyList(),
            users =  emptyList()
        )
    }

    /**
     * Converts a WeekCreateDTO and associated Course into a Week entity.
     *
     * @param week The WeekCreateDTO containing the data for the new Week.
     * @param course The Course associated with the Week.
     * @return A Week entity constructed from the provided WeekCreateDTO and Course.
     */
    fun toEntity(week: WeekCreateDTO, course: Course) = week.run {
        Week(name, from, until, course, emptyList())
    }
    /**
     * Converts a TopicCreateDTO to a Topic entity.
     *
     * @param topic The TopicCreateDTO to convert.
     * @return A Topic entity.
     */
    fun toEntity(topic: TopicCreateDTO) = topic.run { Topic(name, emptyList(), emptyList()) }

    /**
     * Converts a CodeCommentEditDTO and a StudentModuleRequest into a CodeComment entity.
     *
     * @param codeComment the data transfer object that contains the details of the code comment
     * @param smr the student module request associated with the code comment
     * @return A CodeComment entity.
     */
    fun toEntity(codeComment: CodeCommentEditDTO, smr: StudentModuleRequest) = codeComment.run {
        CodeComment(smr, fileName, rowNumber, comment)
    }

    /*fun toEntity(studentModule: StudentModuleCreateDTO) = studentModule.run{
    StudentModule(
        module = module.let { moduleRepository.getReferenceById(it)},
        student = student.let { userRepository.getReferenceById(it)},

    )

}*/

    /**
     * Converts a StudentRatingCreateDTO to a StudentRating entity.
     *
     * @param studentRating the data transfer object containing the necessary information to create a new StudentRating.
     * @return a new StudentRating entity populated with data from the given DTO.
     */
    fun toEntity(studentRating: StudentRatingCreateDTO) = studentRating.run{
        StudentRating(
            points = points,
            text = text,
            student = userRepository.getReferenceById(student),
            module = moduleRepository.getReferenceById(module),
            published = Timestamp.valueOf(LocalDateTime.now()),
        )
    }

    fun toEntity(scoringRule: ScoringRuleCreateDTO) = scoringRule.run {
        ScoringRule(
            name = name,
            shortName = shortName,
            description = description,
            points = points,
            until = until,
            toComplete = 0,
            lesson = lessonRepository.getReferenceById(lesson),
            modules = emptyList(),
          //  studentModules = emptyList(),
        )
    }


    // === UPDATE DTO

    /**
     * Merges the given `SubjectUpdateDTO` into the provided `Subject`, creating a new
     * instance of `Subject` with updated fields from the DTO where available.
     *
     * @param subject The original `Subject` to be updated.
     * @param dto The `SubjectUpdateDTO` containing the new values for the update.
     * @return A new instance of `Subject` with fields updated from `dto` where not null.
     */
    fun merge(subject: Subject, dto: SubjectUpdateDTO) = subject.run {
        Subject(dto.name ?: name, dto.code ?: code, courses, guarantors, questions, id)
    }

    /**
     * Merges the properties of a given CodeModuleUpdateDTO into the existing CodeModule.
     *
     * @param cm The original CodeModule that needs to be updated.
     * @param dto The Data Transfer Object containing the new values for the CodeModule.
     * @return A new instance of COdeModule.
     */
    fun merge(cm: CodeModule, dto: CodeModuleUpdateDTO) = cm.run {
        CodeModule(
            dto.codeType ?: codeType, dto.interactionType ?: interactionType,
            dto.libraryType ?: libraryType, dto.envelopeType ?: envelopeType,
            if (dto.envelopeType == EnvelopeType.ENV_CUSTOM) dto.customEnvelope else customEnvelope,
            dto.codeHidden ?: codeHidden, dto.fileLimit ?: fileLimit, dto.hideCompilerOutput ?: false,
            dto.referencePublic ?: referencePublic, tests, files, module, id
        )
    }

    /**
     * Merges the properties of a given ModuleUpdateDTO into a Module instance.
     *
     * @param module The original Module to be updated.
     * @param dto The ModuleUpdateDTO containing update information.
     * @return A new Module instance with the updated properties.
     */
    fun merge(module: Module, dto: ModuleUpdateDTO) = module.run {
        val now = Timestamp.from(Instant.now())
        Module(
            dto.name ?: name, type, dto.assignment ?: assignment, dto.difficulty,
            dto.lockable ?: lockable, dto.timeLimit ?: timeLimit,
            dto.manualEval ?: manualEval, dto.hidden ?: hidden,
            dto.minPercent ?: minPercent,
            file, now, topics, subjects, lessons, students, editors, notes, ratings, author, id
        )
    }

    fun merge(sa: StudentAnswer, dto: StudAnswerUpdateDTO, correct: AnswerStatusType?) = sa.run {
        StudentAnswer(dto.data, correct?: isCorrect, quizroom, student, question, id)
    }

    /**
     * Merges a Template object with a TemplateUpdateDTO, updating the Template's properties
     * with the corresponding non-null values from the DTO.
     *
     * @param template The original Template to update.
     * @param dto The TemplateUpdateDTO containing updated values.
     * @return A new Template object with merged properties.
     */
    fun merge(template: Template, dto: TemplateUpdateDTO) = template.run {
        val now = Timestamp.from(Instant.now())
        Template(
            dto.name ?: name, dto.codeType ?: codeType, dto.interactionType ?: interactionType,
            dto.libraryType ?: libraryType, dto.envelopeType ?: envelopeType, dto.customEnvelope,
            dto.codeHidden ?: codeHidden, dto.fileLimit ?: fileLimit, now, tests, author, id
        )
    }

    fun merge(question: Question, dto: QuestionUpdateDTO) = question.run {
        Question(
            dto.questionData ?: questionData,
            dto.possibleAnswersData ?: possibleAnswersData, dto.correctAnswerData ?: correctAnswerData,
            dto.explanation ?: explanation, dto.singleAnswer ?: singleAnswer,
            if (dto.author != null)
                userRepository.getByUsername(dto.author) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
            else author,
            dto.timeLimit ?: timeLimit, questionType, questionAnswer, quizzes, emptyList(), emptyList(), id
        )
    }

    /**
     * Merges the provided LessonUpdateDTO into an existing Lesson to create a new Lesson instance.
     *
     * @param lesson the original Lesson to be updated
     * @param dto the LessonUpdateDTO containing the update information
     * @return a new Lesson instance with updated values from the dto where provided, other values are taken from the original lesson
     */
    fun merge(lesson: Lesson, dto: LessonUpdateDTO) = lesson.run {
        Lesson(
            dto.name ?: name, dto.hidden ?: hidden, dto.order ?: order,
            dto.timeStart, dto.timeLimit, dto.description ?: description,
            dto.type ?: type, dto.lockCode?.let { it.ifEmpty { null } }, week, modules, students, scoringRules, id
        )
    }

    /**
     * Merges the provided WeekUpdateDTO into the existing Week object.
     * Only updates fields that are not null in the WeekUpdateDTO.
     *
     * @param week The original Week object to be updated.
     * @param dto The WeekUpdateDTO containing the new values for the Week fields.
     * @return A new Week instance.
     */
    fun merge(week: Week, dto: WeekUpdateDTO) = week.run {
        Week(dto.name ?: name, dto.from ?: from, dto.until ?: until, course, lessons, id)
    }

    /**
     * Merges properties from a TopicCreateDTO into an existing Topic.
     *
     * @param topic The original Topic object.
     * @param dto The TopicCreateDTO containing attributes to be merged into the Topic.
     * @return A new Topic instance.
     */
    fun merge(topic: Topic, dto: TopicCreateDTO) = topic.run { Topic(dto.name, modules, emptyList(), id) }

    /**
     * Merges the given SemesterUpdateDTO into the existing Semester and returns a new Semester instance
     * with updated fields.
     *
     * @param semester The original Semester object to be updated.
     * @param dto The SemesterUpdateDTO containing new values for the Semester fields.
     * @return A new Semester instance with updated fields.
     */
    fun merge(semester: Semester, dto: SemesterUpdateDTO) = semester.run {
        Semester(dto.code ?: code, dto.from ?: from, dto.until ?: until, courses, id)
    }

    /**
     * Merges the properties of a CourseUpdateDTO into an existing Course object.
     *
     * @param course The original Course object to be updated.
     * @param dto The CourseUpdateDTO containing the new properties.
     * @return A new Course instance.
     */
    fun merge(course: Course, dto: CourseUpdateDTO) = course.run {
        Course(dto.name ?: name, dto.shortName ?: shortName, dto.public ?: public, secret, dto.subject?.let {
            subjectRepository.getReferenceById(it)
        }?: subject, dto.semester?.let { semesterRepository.getReferenceById(it) } ?: semester, weeks, users, id)
    }

    /**
     * Merges the fields of a StudentRatingUpdateDTO into an existing StudentRating object.
     *
     * @param studentRating The current StudentRating object to be updated.
     * @param dto The StudentRatingUpdateDTO containing the new data to be merged.
     * @return A new StudentRating object with updated fields from the DTO, and the current timestamp.
     */
    fun merge(studentRating: StudentRating, dto: StudentRatingUpdateDTO) = studentRating.run {
        StudentRating(module, student, dto.points ?: points, dto.text ?: text, Timestamp.valueOf(LocalDateTime.now()), id)
    }

    /**
     * Merges the given TeacherNoteUpdateDTO into the existing TeacherNote.
     *
     * @param note The original TeacherNote object.
     * @param dto The TeacherNoteUpdateDTO object containing the updated information.
     * @return A new TeacherNote object with updated content and created date, while retaining the original module, author, and id, and marking the note as updated.
     */
    fun merge(note: TeacherNote, dto: TeacherNoteUpdateDTO) = note.run {
        TeacherNote(dto.content, dto.created, true, module, author, id)
    }

    fun merge(existing: ScoringRule, dto: ScoringRuleUpdateDTO): ScoringRule {
        return ScoringRule(
            name = dto.name ?: existing.name,
            shortName = dto.shortName ?: existing.shortName,
            description = dto.description ?: existing.description,
            points = dto.points ?: existing.points,
            until = dto.until ?: existing.until,
            toComplete = dto.toComplete ?: existing.toComplete,
            lesson = existing.lesson,
            modules = existing.modules,
            //studentModules = existing.studentModules,
            id = existing.id
        )
    }

}