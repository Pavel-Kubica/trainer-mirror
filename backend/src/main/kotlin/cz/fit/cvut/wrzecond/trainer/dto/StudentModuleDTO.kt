package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.entity.StudentModuleRequestType
import java.sql.Timestamp

/**
 * Data transfer object for student modules
 * @property id unique student module identifier
 * @property student student to whom model belongs to
 */

data class StudentModuleFindDTO(override val id: Int, val student: UserFindDTO) : IFindDTO

/**
 * Data transfer object for student modules
 * @property id unique student module identifier
 * @property lesson lesson in which student module is
 * @property module module which student module represents
 * @property student student to whom model belongs to
 * @property percent
 * @property openedOn time when module was opened
 * @property completedOn time when module was completed
 * @property allowedShow
 * @property completedEarly if module was completed early
 * @property unlocked if module is unlocked
 * @property rating rating which student gave to module
 */

data class StudentModuleGetDTO(override val id: Int, val module: ModuleFindDTO,
                               val student: UserFindDTO,
                               val lesson: LessonFindDTO,
                               val percent: Int?, val openedOn: Timestamp?,
                               val completedOn: Timestamp?,
                               val allowedShow: Boolean?,
                               val completedEarly: Boolean?,
                               val unlocked: Boolean) : IGetDTO


/**
 * Data transfer object for student module
 * @property percent module percent
 * @property completedEarly did he complete early?
 * @property completed did he complete?
 */


data class StudentModuleReadDTO(val percent: Int?, val completedEarly: Boolean?, val completed: Timestamp?)

/**
 * Data transfer object for student module updating
 * @property allowedShow should teacher be able to see it
 * @property percent module percent
 */
data class StudentModuleEditDTO(val allowedShow: Boolean?, val percent: Int?)

/**
 * Data transfer object for student module request creating
 * @property requestText the text to display to the teacher
 * @property requestType type of request that should be active
 */
data class StudentModuleRequestEditDTO(val requestText: String, val requestType: StudentModuleRequestType)

/**
 * Data transfer object for student module updating by teacher
 * @property responseText teacher comment
 * @property evaluation teacher evaluation
 * @property percent percent to evaluate with
 */
data class StudentModuleRequestTeacherDTO(val responseText: String, val evaluation: Boolean?, val percent: Int?)
