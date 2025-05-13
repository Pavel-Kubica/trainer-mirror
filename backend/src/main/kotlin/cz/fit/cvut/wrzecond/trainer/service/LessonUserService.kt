package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Lesson
import cz.fit.cvut.wrzecond.trainer.repository.LessonRepository
import cz.fit.cvut.wrzecond.trainer.repository.StudentModuleRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class responsible for handling operations related to lessons and users.
 *
 * @property repository The repository used for accessing lesson data.
 * @property studentModuleRepository The repository used for accessing student module data.
 * @property userRepository The repository used for accessing user data.
 */
@Service
class LessonUserService(override val repository: LessonRepository, private val studentModuleRepository: StudentModuleRepository,
                        private val userRepository: UserRepository) : IServiceBase<Lesson>(repository, userRepository) {

    /**
     * Get preview of all users in given lesson (for teacher)
     * @property id lesson id to display the detail for
     * @property userDto teacher's dto
     * @return LessonUserList
     */
    fun getLessonUsers(id: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { lesson, user ->
            LessonUserListDTO(
                lesson.id, converter.toFindDTO(lesson.week.course, user), lesson.name,
                lesson.modules.map { converter.toFindDTO(it) }.sortedBy { it.order },
                userRepository.findByLesson(lesson).map { student ->
                    ModuleUserListDTO(
                        student.id, student.username, student.name, student.modules
                            .filter { it.lesson.id == lesson.id }
                            .map { sm -> converter.toModuleUserDTO(sm) }
                    )
                }
            )
        }

    /**
     * Teacher detail of one given student
     * @property id lesson id
     * @property studentId student id
     * @property userDto dto of teacher
     * @return LessonUserReadDTO
     */
    fun getLessonUser(id: Int, studentId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { lesson, _ ->
            val studentEntity = userRepository.getReferenceById(studentId)
            if (!lesson.canView(studentEntity))
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            LessonUserReadDTO(
                converter.toGetDTO(lesson, studentEntity, allModules = true),
                converter.toFindDTO(studentEntity)
            )
        }

    /**
     * Get detail of week in which a given lesson is for given student
     * @property id lesson to find the week of
     * @property studentId student to find it for
     * @property userDto user to display the data for
     * @return WeekDetailDTO
     */
    fun getLessonUserWeek(id: Int, studentId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { lesson, _ ->
            val studentEntity = userRepository.getReferenceById(studentId)
            if (!lesson.canView(studentEntity))
                throw ResponseStatusException(HttpStatus.NOT_FOUND)
            converter.toDetailDTO(lesson.week, studentEntity)
        }

    /**
     * Reset student's progress in lesson (teacher side)
     * @property id lesson id
     * @property studentId student id
     * @property userDto dto of teacher
     */
    fun resetLessonUser(id: Int, studentId: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { lesson, _ ->
            val studentEntity = userRepository.getReferenceById(studentId)
            val studentModules = studentModuleRepository.findByStudentLesson(studentEntity, lesson)
            studentModuleRepository.deleteAll(studentModules)
        }

}
