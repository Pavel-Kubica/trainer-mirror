package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["module_id", "editor_id"])])
data class ModuleEditor(
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "editor_id", nullable = false)
    val editor: User,

    override val id: Int = 0
) : IEntity(id) {
    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id )"
    }
}
