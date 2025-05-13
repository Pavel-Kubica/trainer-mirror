package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.ScoringRuleModule
import cz.fit.cvut.wrzecond.trainer.entity.StudentRating
import org.springframework.data.jpa.repository.Query

interface ScoringRuleModuleRepo : IRepository<ScoringRuleModule> {

    @Query("SELECT srm FROM ScoringRuleModule srm WHERE srm.module.id =:moduleId AND srm.scoringRule.id =:ruleId")
    fun getByRuleAndModule (moduleId: Int, ruleId: Int) : ScoringRuleModule?
}