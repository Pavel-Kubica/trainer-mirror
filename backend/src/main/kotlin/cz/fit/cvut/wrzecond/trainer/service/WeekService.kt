package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class for managing operations related to Weeks.
 *
 * @property repository the repository for handling Week entities.
 * @property courseRepository the repository for handling Course entities.
 * @property lessonRepository the repository for handling Lesson entities.
 * @property lessonModuleRepository the repository for handling LessonModule entities.
 * @property logRepository the repository for handling Log entities.
 * @property userRepository the repository for handling User entities.
 */
@Service
class WeekService (override val repository: WeekRepository, private val courseRepository: CourseRepository,
                   private val lessonRepository: LessonRepository, private val lessonModuleRepository: LessonModuleRepository,
                   private val logRepository: LogRepository,
                   userRepository: UserRepository)
: IServiceImpl<Week, WeekFindDTO, WeekGetDTO, WeekCreateDTO, WeekUpdateDTO>(repository, userRepository) {

    /**
     * Creates a new week based on the provided data transfer objects.
     *
     * @param dto Data transfer object containing the details for the new week.
     * @param userDto Data transfer object containing authenticated user details. Nullable.
     * @throws ResponseStatusException if the user does not have permission to edit the course.
     * @return The created week's data transfer object representation.
     */
    @Transactional
    override fun create(dto: WeekCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        val course = courseRepository.getReferenceById(dto.courseId)
        if (!course.canEdit(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val week = repository.saveAndFlush(converter.toEntity(dto, course))
        logRepository.saveAndFlush(createLogEntry(userDto, week, "create"))
        converter.toGetDTO(week)
    }

    /**
     * Updates a week based on the provided ID and update data.
     *
     * @param id The ID of the week to be updated.
     * @param dto The data transfer object containing the week update information.
     * @param userDto The authenticated user performing the update; can be null.
     * @return The updated week's data transfer object representation.
     */
    override fun update(id: Int, dto: WeekUpdateDTO, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { week, _ ->
            val updatedWeek = repository.saveAndFlush(converter.merge(week, dto))
            logRepository.saveAndFlush(createLogEntry(userDto, updatedWeek, "update"))
            converter.toGetDTO(updatedWeek)
        }

    /**
     * Deletes a week entry identified by the given ID.
     *
     * @param id The unique identifier of the week entry to be deleted.
     * @param userDto A DTO containing authentication details of the user requesting the deletion.
     */
    @Transactional
    override fun delete(id: Int, userDto: UserAuthenticateDto?): Unit
        = checkEditAccess(id, userDto) { week, _ ->
            repository.delete(week)
            logRepository.saveAndFlush(createLogEntry(userDto, week, "delete"))
        }

    /**
     * Updates lesson order and week
     * @property id of week to update lessons in
     * @property dto lesson ids to update
     * @property userDto user performing request
     */
    fun editLessonOrder(id: Int, dto: WeekLessonOrderDTO, userDto: UserAuthenticateDto?) : Unit
        = checkEditAccess(id, userDto) { week, _ ->
            lessonRepository.saveAllAndFlush(dto.lessonIds
                .map {
                    val lesson = lessonRepository.getReferenceById(it)
                    if (lesson.week.course.id != week.course.id) throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                    else lesson
                }
                .mapIndexed { ix, lesson -> lesson.copy(order = ix, week = week) }
            ).map {
                logRepository.saveAndFlush(createLogEntry(userDto, it, "update"))
            }
        }

    /**
     * Clone week (with all the lessons and modules) between two courses
     * @property id week id to be copied
     * @property courseId course to copy into
     * @property userDto teacher who is copying the lesson (must have edit access)
     */
    @Transactional
    fun cloneWeek(id: Int, courseId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { week, user ->
            // Check edit rights
            val newCourse = courseRepository.getReferenceById(courseId)
            if (!newCourse.canEdit(user))
                throw ResponseStatusException(HttpStatus.FORBIDDEN)

            // Step 1: create the week
            val newWeek = week.copy(course = newCourse, lessons = emptyList(), id = 0)
            val weekFlushed = repository.saveAndFlush(newWeek)
            logRepository.saveAndFlush(createLogEntry(userDto, newWeek, "create"))

            // Step 2: copy the lessons (hidden, without lock code)
            val newLessons = lessonRepository.saveAllAndFlush(week.lessons.map {
                it.copy(hidden = true, lockCode = null, week = weekFlushed, modules = emptyList(), id = 0)
            })

            newLessons.map {
                logRepository.saveAndFlush(createLogEntry(userDto, it, "create"))
            }

            // Step 3: copy the modules
            week.lessons.forEachIndexed { ix, lesson ->
                lessonModuleRepository.saveAllAndFlush(lesson.modules.map { lm ->
                    // Keep the order, dependency and module, but change the lesson
                    LessonModule(newLessons[ix], lm.module, lm.order, lm.dependsOn)
                }).map {
                    logRepository.saveAndFlush(createLogEntry(userDto, it, "create"))
                }
            }

            // Step 3: return new week with lessons and modules
            converter.toGetDTO(weekFlushed, user)
        }

}
