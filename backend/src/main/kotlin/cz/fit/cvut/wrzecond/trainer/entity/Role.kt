package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*

@Entity
data class Role (
    @Column(nullable = false) val level: RoleLevel,
    override val id: Int = 0
) : IEntity(id) {
    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  level = $level   ,   id = $id )"
    }
}

enum class RoleLevel {
    STUDENT, TEACHER
}
