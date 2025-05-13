package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.entity.Lesson
import cz.fit.cvut.wrzecond.trainer.entity.LessonType
import cz.fit.cvut.wrzecond.trainer.entity.ModuleType
import cz.fit.cvut.wrzecond.trainer.entity.StudentModule

/**
 * Data transfer object for lesson module list
 * @property id lesson module identifier
 * @property name module name
 * @property type module type
 * @property order module order in lesson
 */
data class LessonModuleReadDTO(val id: Int, val name: String, val type: ModuleType, val order: Int)

/**
 * Data transfer object for lesson module creating
 * @property order module order in lesson
 * @property depends module dependency in lesson
 */
data class LessonModuleEditDTO(val order: Int?, val depends: Int?)

/**
 * Data transfer object for lesson module copying
 * @param copiedId original module id
 * @property order module order in lesson
 */
data class LessonModuleCopyDTO(val copiedId: Int, val order: Int)


/**
 * Data transfer object for lesson module list
 * @property id lesson module identifier
 * @property module module
 * @property lesson lesson
 * @property order module order in lesson
 * @property scoringRules scoringRules
 */
data class LessonModuleGetDTO(val id: Int, val module: ModuleFindDTO, val lesson: LessonGetDTO, val students: List<StudentModuleGetDTO>, val order: Int )