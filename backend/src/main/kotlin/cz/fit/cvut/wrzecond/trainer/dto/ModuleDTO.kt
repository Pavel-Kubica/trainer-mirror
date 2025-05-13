package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.entity.ModuleDifficulty
import cz.fit.cvut.wrzecond.trainer.entity.ModuleType
import cz.fit.cvut.wrzecond.trainer.entity.StudentModuleRequestType
import java.sql.Timestamp

/**
 * Data transfer object for module list
 * @property id unique identifier of module
 * @property name module name
 * @property subjects module subjects
 * @property topics module topics
 * @property type module type
 * @property assignment module assignment text
 * @property difficulty module difficulty
 * @property locked is module locked for current user?
 * @property lockable can the module be locked (by lesson settings)?
 * @property timeLimit can the module be solved early (by lesson settings)?
 * @property manualEval is module manually evaluated?
 * @property hidden is module hidden from students?
 * @property minPercent minimum percent for module succeeding
 * @property file module file path
 * @property depends module on which the current module depends
 * @property author author of module
 * @property editors allowed editors of module
 * @property allowedShow is allowed to show the module by teacher
 * @property completedEarly did student complete the module early?
 * @property completed when did student complete the module? (null if he didn't)
 * @property studentRequest student's request
 * @property teacherComment teacher's comment
 * @property order module order in list
 * @property progress student progress in module
 * @property hasStudentFile does student module have any associated file?
 */
data class ModuleFindDTO(
    override val id: Int, val name: String, val subjects: List<SubjectFindDTO>,val topics: List<TopicFindDTO>, val type: ModuleType, val assignment: String, val difficulty: ModuleDifficulty?,
    val locked: Boolean, val lockable: Boolean, val timeLimit: Boolean, val manualEval: Boolean, val hidden: Boolean,
    val minPercent: Int, val file: String?, val depends: Int?, val author: String, val editors: List<String>,
    val allowedShow: Boolean?, val completedEarly: Boolean?, val completed: Timestamp?,
    val studentRequest: StudentRequestDTO?, val order: Int?, val progress: Int?, val hasStudentFile: Boolean?,val students: List<StudentModuleFindDTO>, val ratings: List<StudentRatingFindDTO>?
) : IFindDTO

/**
 * Data transfer object for module detail
 * @property id unique identifier of module
 * @property name module name
 * @property depends id of module this module is depending on
 * @property author author dto
 * @property editors list of users allowed to edit the module
 * @property subjects module subjects
 * @property topics module topics
 * @property lastModificationTime when was module last modified?
 * @property type module type
 * @property difficulty module difficulty
 * @property assignment module assignment
 * @property minPercent minimum percent for module unlocking
 * @property lockable can the module be locked (by lesson settings)?
 * @property timeLimit does module have time limit (by lesson settings)?
 * @property manualEval is module manually evaluated?
 * @property hidden is module hidden
 */
data class ModuleGetDTO(override val id: Int, val name: String, val depends: Int?, val author: CourseUserReadDTO,
                        val editors: List<Int>, val subjects: List<SubjectFindDTO>,
                        val topics: List<TopicFindDTO>,
                        val type: ModuleType, val lastModificationTime: Timestamp,
                        val difficulty: ModuleDifficulty?, val assignment: String, val minPercent: Int,
                        val lockable: Boolean, val timeLimit: Boolean, val manualEval: Boolean,
                        val hidden: Boolean, val ratings: List<StudentRatingFindDTO>?
) : IGetDTO

/**
 * Data transfer object for module creation
 * @property name module name
 * @property type module type
 * @property editors list of module editors
 * @property minPercent minimum percent for module succeeding
 * @property difficulty module difficulty
 * @property assignment module assignment
 * @property lockable can the module be locked (by lesson settings)?
 * @property timeLimit can the module be solved on time (by lesson settings)?
 * @property manualEval is module manually evaluated?
 * @property hidden is module hidden from students?
 */
data class ModuleCreateDTO(val name: String, val type: ModuleType, val editors: List<Int>,
                           val minPercent: Int, val difficulty: ModuleDifficulty?, val assignment: String,
                           val lockable: Boolean, val timeLimit: Boolean, val manualEval: Boolean,
                           val hidden: Boolean) : ICreateDTO

/**
 * Data transfer object for module updating
 * @property name module name
 * @property editors list of module editors
 * @property minPercent minimum percent for module succeeding
 * @property difficulty module difficulty
 * @property assignment module assignment
 * @property lockable can the module be locked (by lesson settings)?
 * @property timeLimit can the module be solved on time (by lesson settings)?
 * @property manualEval is module manually evaluated?
 * @property hidden is module hidden from students?
 * @property lastModificationTime last modification time for control
 */
data class ModuleUpdateDTO(val name: String?, val editors: List<Int>?,
                           val minPercent: Int?, val difficulty: ModuleDifficulty?, val assignment: String?,
                           val lockable: Boolean?, val timeLimit: Boolean?, val manualEval: Boolean?,
                           val hidden: Boolean?, val lastModificationTime: Timestamp) : IUpdateDTO

/**
 * Data transfer object for module user list
 * @property id user id
 * @property username username
 * @property name user's name
 * @property modules modules percent progress
 */
data class ModuleUserListDTO(val id: Int, val username: String, val name: String, val modules: List<ModuleUserDTO>)

/**
 * Data transfer object for single module user
 * @property id user id
 * @property username username
 * @property name user's name
 * @property module module percent progress
 */
data class ModuleUserSingleListDTO(val id: Int, val username: String, val name: String, val module: ModuleUserDTO)

/**
 * Data transfer object for single module – user relation
 * @property id module id
 * @property requestType active request on the module (null if none)
 * @property allowedShow has student allowed anonymized publication?
 * @property progress progress in module
 * @property completedEarly has student completed module early?
 * @property completed has student completed the module? (if yes, timestamp when)
 */
data class ModuleUserDTO(val id: Int, val requestType: StudentModuleRequestType?, val allowedShow: Boolean,
                         val progress: Int?, val completedEarly: Boolean, val completed: Timestamp?)

/**
 * Data transfer object for notifications
 * @property id module id
 * @property name module name
 */
data class ModuleNotificationDTO(val id: Int, val name: String)
/**
 * Data transfer object for student module request
 * @property requestId student module request id
 * @property requestType does student need help / did he submit for evaluation?
 * @property requestText student request text
 */
data class StudentRequestDTO(val requestId: Int?, val requestType: StudentModuleRequestType?, val requestText: String?,
                             val satisfied: Boolean?, val teacherComment: TeacherCommentDTO?)
data class TeacherCommentDTO(val name: String?, val text: String?)

data class ModuleFindTableDTO(val id: Int, val name: String, val subjects: List<SubjectFindDTO>,
                              val topics: List<TopicFindDTO>, val editors: List<String>,val type: ModuleType, val author: String)