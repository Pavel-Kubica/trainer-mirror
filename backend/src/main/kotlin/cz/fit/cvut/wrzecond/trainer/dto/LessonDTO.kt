package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.entity.LessonType
import java.sql.Timestamp

/**
 * Data transfer object for lesson list
 * @property id unique identifier of lesson
 * @property name lesson name
 * @property hidden is lesson hidden for students (student always sees false)
 * @property order lesson order in list
 * @property lockCode code used for unlocking modules in lesson
 * @property timeLimit time limit for user to solve lesson in time
 * @property progress student progress in lesson
 */
data class LessonFindDTO(override val id: Int, val name: String, val hidden: Boolean,
                         val order: Int, val type: LessonType, val lockCode: String?, val timeLimit: Timestamp?,
                         val progress: Int?, val progressRules: Int?) : IFindDTO

/**
 * Data transfer object for lesson list
 * @property id unique identifier of lesson
 * @property week which week the lesson is part of
 * @property name lesson name
 * @property hidden is lesson hidden from students
 * @property type lesson type
 * @property lockCode code used for unlocking modules in lesson
 * @property timeStart when does lesson start?
 * @property timeLimit time limit for user to solve lesson in time
 * @property description lesson description (summary, how to get points)
 * @property modules lesson modules
 * @property scoringRules lesson scoring rules
 */
data class LessonGetDTO(override val id: Int, val week: WeekFindDTO, val name: String,
                        val hidden: Boolean, val type: LessonType, val lockCode: String?,
                        val timeStart: Timestamp?, val timeLimit: Timestamp?,
                        val description: String, val modules: List<ModuleFindDTO>,
                        val scoringRules: List<ScoringRuleGetDTO>) : IGetDTO

/**
 * Data transfer object for lesson creation
 * @property weekId id of week for lesson to be added to
 * @property name lesson name
 * @property hidden hide lesson from students
 * @property order lesson order in list
 * @property type lesson type
 * @property lockCode code used for unlocking modules in lesson
 * @property timeStart when does lesson start
 * @property timeLimit time limit for user to solve lesson in time
 * @property description lesson description
 */
data class LessonCreateDTO(val weekId: Int, val name: String, val hidden: Boolean, val order: Int,
            val type: LessonType, val lockCode: String?, val timeStart: Timestamp?,
            val timeLimit: Timestamp?, val description: String) : ICreateDTO

/**
 * Data transfer object for lesson editing
 * @property name lesson name
 * @property hidden should we hide lesson from student
 * @property order lesson order in list
 * @property type lesson type
 * @property lockCode code used for unlocking modules in lesson
 * @property timeStart when does lesson start
 * @property timeLimit time limit for user to solve lesson in time
 * @property description lesson description
 */
data class LessonUpdateDTO(val name: String?, val hidden: Boolean?, val order: Int?, val type: LessonType?,
        val lockCode: String?, val timeStart: Timestamp?, val timeLimit: Timestamp?, val description: String?) : IUpdateDTO

/**
 * Data transfer object for lesson detail in teacher view
 * @property lesson the lesson used
 * @property user the user information
 */
data class LessonUserReadDTO(val lesson: LessonGetDTO, val user: UserFindDTO)

/**
 * Data transfer object for lesson user list
 * @property id lesson id
 * @property course lesson course
 * @property name lesson name
 * @property modules modules list
 * @property users user modules list
 */
data class LessonUserListDTO(val id: Int, val course: CourseFindDTO, val name: String,
                             val modules: List<ModuleFindDTO>, val users: List<ModuleUserListDTO>)

/**
 * Data transfer object for LessonModule user list
 * @property lesson lesson information
 * @property module module information
 * @property users user module list
 */
data class LessonModuleUserListDTO(val lesson: LessonGetDTO, val module: ModuleFindDTO, val users: List<ModuleUserSingleListDTO>)

/**
 * Data transfer object used for unlocking lesson
 * @property code code to unlock lesson
 */
data class LessonUnlockDTO(val code: String)

/**
 * Data transfer object for notifications
 * @property id lesson id
 * @property name lesson name
 */
data class LessonNotificationDTO(val id: Int, val name: String)