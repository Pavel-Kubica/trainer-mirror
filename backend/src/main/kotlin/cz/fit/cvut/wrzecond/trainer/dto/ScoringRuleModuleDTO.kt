package cz.fit.cvut.wrzecond.trainer.dto



/**
 * Data transfer object for lesson module list
 * @property module module id
 * @property scoringRule scoringRule id
 */

data class ScoringRuleModuleReadDTO(val id: Int, val lessonModule: Int, val scoringRule: Int)

/**
 * Data transfer object for lesson module creating
 * @property module module id
 * @property scoringRule scoringRule id
 */

data class ScoringRuleModuleEditDTO(val lessonModule: Int, val scoringRule: ScoringRuleGetDTO)