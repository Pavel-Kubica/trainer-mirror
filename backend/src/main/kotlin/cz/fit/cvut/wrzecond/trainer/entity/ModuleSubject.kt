package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["module_id", "subject_id"])])
data class ModuleSubject(
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "subject_id", nullable = false)
    val subject: Subject,

    override val id: Int = 0
) : IEntity(id) {
    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id )"
    }
}