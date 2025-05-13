package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

/**
 * Service class for managing Lessons.
 *
 * @param repository The repository for managing Lesson entities.
 * @param courseRepository The repository for managing Course entities.
 * @param lessonModuleRepository The repository for managing LessonModule entities.
 * @param studentModuleRepository The repository for managing StudentModule entities.
 * @param weekRepository The repository for managing Week entities.
 * @param logService The service for managing Log entities.
 * @param userRepository The repository for managing User entities.
 */
@Service
class LessonService (override val repository: LessonRepository, private val courseRepository: CourseRepository,
                     private val lessonModuleRepository: LessonModuleRepository,
                     private val lessonModuleService: LessonModuleService,
                     private val studentModuleRepository: StudentModuleRepository,
                     private val moduleRepository: ModuleRepository,
                     private val scoringRuleRepository: ScoringRuleRepository,
                     private val scoringRuleModuleRepository: ScoringRuleModuleRepo,
                     private val weekRepository: WeekRepository,
                     private val logService: LogService,
                     private val logRepository: LogRepository,
                     private val userRepository: UserRepository,
                     private val srRepository: ScoringRuleRepository,
                     private val srmRepository: ScoringRuleModuleRepo)
: IServiceImpl<Lesson, LessonFindDTO, LessonGetDTO, LessonCreateDTO, LessonUpdateDTO>(repository, userRepository){

    /**
     * Fetches a lesson by its ID and returns it as a DTO.
     *
     * @param id The unique identifier of the lesson.
     * @param userDto The data transfer object containing the user's authentication information.
     * @return Fetched lesson as LessonGetDTO.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(id, userDto) { lesson, user ->
            converter.toGetDTO(lesson, user)
        }

    /**
     * Creates a new lesson based on the provided LessonCreateDTO.
     *
     * @param dto Data transfer object containing the details needed to create a lesson.
     * @param userDto Contains user authentication information used to determine edit permissions.
     * @return Created lesson as LessonGetDTO.
     * @throws ResponseStatusException if the user does not have permission to edit the week.
     */
    @Transactional
    override fun create(dto: LessonCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        val week = weekRepository.getReferenceById(dto.weekId)
        if (!week.canEdit(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val lesson = repository.saveAndFlush(converter.toEntity(dto, week))
        logService.log(userDto, lesson, "create")
        converter.toGetDTO(lesson)
    }

    /**
     * Updates an existing lesson with new data.
     *
     * @param id The unique identifier of the lesson to be updated.
     * @param dto The data transfer object containing the updated lesson information.
     * @param userDto The data transfer object containing user authentication information, if available.
     * @return Updated lesson as LessonGetDTO.
     */
    override fun update(id: Int, dto: LessonUpdateDTO, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { lesson, _ ->
            val updatedLesson = repository.saveAndFlush(converter.merge(lesson, dto))
            logService.log(userDto, updatedLesson, "update")
            converter.toGetDTO(updatedLesson)
        }

    /**
     * Deletes a lesson from the repository.
     *
     * @param id The ID of the lesson to be deleted.
     * @param userDto An optional data transfer object containing user authentication details.
     */
    @Transactional
    override fun delete(id: Int, userDto: UserAuthenticateDto?): Unit
        = checkEditAccess(id, userDto) { lesson, _ ->
            repository.delete(lesson)
            logService.log(userDto, lesson, "delete")
        }

    /**
     * Special variant of getByID, without user data
     * accessible only by teachers
     * @property id lesson id to be retrieved
     * @property userDto teacher dto
     * @return Lesson
     */
    fun teacherGetByID(id: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { lesson, _ ->
            converter.toGetDTO(lesson)
        }

    /**
     * Find user's lessons that are currently running
     * @property userDto user to search lessons for
     * @return list of lessons running at given time
     */
    fun currentLessons(userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        repository.findUserLessonsAtTime(user, Timestamp.from(Instant.now()))
            .map { converter.toGetDTO(it, user) }
    }

    /**
     * Get detail of week in which a given lesson is
     * (used for left sidebar information)
     * @property id lesson to find the week of
     * @property userDto user to display the data for
     * @return WeekDetailDTO
     */
    fun lessonWeekDetail(id: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(id, userDto) { lesson, user ->
            converter.toDetailDTO(lesson.week, user)
        }

    /**
     * Unlock all lesson modules upon writing lock code
     * @property id lesson to unlock modules in
     * @property unlockDTO dto containing the code
     * @property userDto user to unlock the modules for
     */
    fun unlockLessonModules(id: Int, unlockDTO: LessonUnlockDTO, userDto: UserAuthenticateDto?) : Unit
        = checkViewAccess(id, userDto) { lesson, user ->
            if (lesson.lockCode != unlockDTO.code)
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)

        // Set unlocked parameter to true and create module data if needed
        studentModuleRepository.saveAllAndFlush(
            lesson.modules.map { lm ->
                val sm = studentModuleRepository.getByStudentModule(user, lm.module, lesson)
                sm?.copy(unlocked = true) ?: StudentModule(lesson, lm.module, user, null,
                    Timestamp.from(Instant.now()), null, false, false,
                    unlocked = true, null, emptyList()
                )
            }
        )
    }

    /**
     * Clone lesson (with all the modules) between two courses
     * @property id lesson id to be copied
     * @property courseId course to copy into
     * @property userDto teacher who is copying the lesson (must have edit access)
     */
    @Transactional
    fun cloneLesson(id: Int, courseId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { lesson, user ->
            // Check edit rights
            val newCourse = courseRepository.getReferenceById(courseId)
            if (!newCourse.canEdit(user))
                throw ResponseStatusException(HttpStatus.FORBIDDEN)

        // Find the week to copy into
        val week = newCourse.weeks.maxByOrNull { it.from } ?: weekRepository.save(Week("New week",
            Timestamp.from(Instant.now()), Timestamp.from(Instant.now()), newCourse, emptyList()))

            // Step 1: create the lesson (hidden, without lock code)
            val newLesson = repository.saveAndFlush(lesson.copy(hidden = true, lockCode = null, week = week, modules = emptyList(), scoringRules = emptyList(), students = emptyList(), id = 0))
            logService.log(userDto, newLesson, "create")

            val allCourseUsers = newLesson.week.course.users

            // Step 2: copy the modules
           lesson.modules.map { lm ->
                // Keep the order, dependency and module, but change the lesson
                val newLm = lessonModuleRepository.saveAndFlush(LessonModule(newLesson,
                    lm.module, lm.order, lm.dependsOn))
                logService.log(userDto, newLm, "create")
               studentModuleRepository.saveAllAndFlush( allCourseUsers.filter {
                   studentModuleRepository.findByUserModuleLesson(it.user, newLm.module, newLm.lesson)==null
               }.map{u2 ->
                   StudentModule(lesson = newLm.lesson, module = newLm.module, student = u2.user, null,
                   null, null, true, false, false, null,
                   emptyList()
               )
               }).map{logService.log(userDto, it, "create")}
           }

        // Step 3: copy the rules
        lesson.scoringRules.map{sr ->
            val newRule = srRepository.saveAndFlush(ScoringRule(sr.name,sr.shortName,
                sr.description,sr.points,sr.until,sr.toComplete,newLesson, emptyList()))
            logService.log(userDto, newRule, "create")
            srmRepository.saveAllAndFlush(sr.modules.map{srm ->
                ScoringRuleModule(srm.module,newRule)
            }).map {
                logService.log(userDto, it, "create")
            }
        }

        // Step 4: return new lesson with modules
        converter.toGetDTO(newLesson, user)
    }
    /**
     * Clone lesson (with all the modules) to another week
     * @property id lesson id to be copied
     * @property weekId week to copy into
     * @property userDto teacher who is copying the lesson (must have edit access)
     */
    @Transactional
    fun cloneLessonToWeek(id: Int, weekId: Int, userDto: UserAuthenticateDto?)
            = checkEditAccess(id, userDto) { lesson, user ->
        // Check edit rights
        val week = weekRepository.getReferenceById(weekId)
        if (!week.canEdit(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        // Step 1: create the lesson (hidden, without lock code)
        val newLesson = repository.saveAndFlush(lesson.copy(lockCode = null, week = week, modules = emptyList(), students = emptyList(), scoringRules = emptyList(), id = 0))
        logService.log(userDto, newLesson, "create")

        // Step 2: copy modules and scoring rules
        lesson.modules.forEach { lm ->
            lessonModuleService.putLessonModule(newLesson.id, lm.module.id, LessonModuleEditDTO(lm.order, lm.dependsOn?.id), userDto)
        }

        lesson.scoringRules.map { rule ->
            val newRule = scoringRuleRepository.saveAndFlush(rule.copy(lesson = newLesson, modules = emptyList(), id = 0))
            logService.log(userDto, newRule, "create")
            scoringRuleModuleRepository.saveAllAndFlush(rule.modules.map { rm ->
                ScoringRuleModule(rm.module, newRule)
            }).map {
                logService.log(userDto, it, "create")
            }
        }


        // Step 3: return new lesson with modules
        converter.toGetDTO(newLesson, user)
    }

    /**
     * Updates lesson order and week
     * @property id of week to update lessons in
     * @property dto lesson ids to update
     * @property userDto user performing request
     */
    fun editModuleOrder(id: Int, dto: LessonModuleOrderDTO, userDto: UserAuthenticateDto?) : Unit
            = checkEditAccess(id, userDto) { lesson, _ ->
        lessonModuleRepository.saveAllAndFlush(dto.moduleIds
            .map {
                val module = moduleRepository.getReferenceById(it)
                val lessonModule = lessonModuleRepository.getByLessonModule(lesson, module)
                if (lessonModule == null || lessonModule.lesson.id != id) throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                else lessonModule
            }
            .mapIndexed { ix, lessonModule -> lessonModule.copy(order = ix) }
        ).map {
            logService.log(userDto, it, "update")
        }
    }

}
