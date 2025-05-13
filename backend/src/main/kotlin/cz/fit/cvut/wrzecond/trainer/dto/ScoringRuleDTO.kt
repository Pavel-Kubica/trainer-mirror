package cz.fit.cvut.wrzecond.trainer.dto

import java.sql.Time
import java.sql.Timestamp


/**
 * Data transfer object for scoring rule list
 * @property id unique identifier of scoring rule
 * @property name scoring rule name
 * @property shortName scoring rule short name
 * @property description scoring rule description
 * @property points scoring rule points
 * @property toComplete scoring rule points needed to complete rule
 * @property lesson scoring rule
 * @property modules modules included in the scoring rule
 * @property students student modules included in the scoring rule
 */

data class ScoringRuleFindDTO(override val id: Int, val name: String, val shortName: String,
                              val description: String?, val points: Double, val until: Timestamp,
                              val toComplete: Int?, val lesson: Int,
                              val modules: List<ModuleGetDTO>,
                              val students: List<StudentModuleGetDTO>) : IFindDTO


/**
 * Data transfer object for scoring rule detail
 * @property id unique identifier of scoring rule
 * @property name scoring rule name
 * @property shortName scoring rule short name
 * @property description scoring rule description
 * @property points scoring rule points
 * @property until scoring rule end
 * @property toComplete scoring rule points needed to complete rule
 * @property lesson scoring rule
 */

data class ScoringRuleGetDTO(override val id: Int, val name: String, val shortName: String,
                             val description: String?, val points: Double, val until: Timestamp, val toComplete: Int?,
                             val modules: List<ModuleGetDTO>,
                             val students: List<StudentModuleGetDTO>) : IGetDTO



/**
 * Data transfer object for scoring rule creation
 * @property name scoring rule name
 * @property shortName scoring rule short name
 * @property description scoring rule description
 * @property points scoring rule points
 * @property until scoring rule end
 * @property toComplete scoring rule points needed to complete rule
 * @property lesson scoring rule
 */

data class ScoringRuleCreateDTO(val name: String, val shortName: String,
                                val description: String?, val points: Double, val until: Timestamp,  val toComplete: Int?, val lesson: Int) : ICreateDTO


/**
 * Data transfer object for scoring rule update
 * @property name scoring rule name
 * @property shortName scoring rule short name
 * @property description scoring rule description
 * @property points scoring rule points
 * @property until scoring rule end
 * @property toComplete scoring rule points needed to complete rule
 */
data class ScoringRuleUpdateDTO(val name: String?, val shortName: String?,val description: String?,
                                val points: Double?, val until: Timestamp?,  val toComplete: Int?) : IUpdateDTO



/**
 * Data transfer object for scoring rule update
 * @property rule scoring rule in the list
 * @property user unique identifier of the user
 * @property studentModules student modules included in the scoring rule
 *
 */
data class ScoringRuleUserDTO(val rule: ScoringRuleGetDTO, val user: Int, val completed: Boolean, val studentModules: List<StudentModuleGetDTO>)