package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "sandbox_id"])])
data class SandboxUser (

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sandbox_id", nullable = false)
    val sandbox: Course,

    override val id: Int = 0
) : IEntity(id) {
    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id, user = $user, sandbox = $sandbox )"
    }
}