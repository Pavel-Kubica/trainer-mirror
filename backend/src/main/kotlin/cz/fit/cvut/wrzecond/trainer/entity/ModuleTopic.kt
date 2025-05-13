package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["module_id", "topic_id"])])
data class ModuleTopic(
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "topic_id", nullable = false)
    val topic: Topic,

    override val id: Int = 0
) : IEntity(id) {
    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id )"
    }
}