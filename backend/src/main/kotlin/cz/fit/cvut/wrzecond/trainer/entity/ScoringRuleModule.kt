package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction


@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["module_id","scoring_rule_id"])])
data class ScoringRuleModule(
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "scoring_rule_id", nullable = false)
    val scoringRule: ScoringRule,

    override val id: Int = 0
) : IEntity(id) {
    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }
}