package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import java.sql.Timestamp

@Entity
data class Semester(
    @Column(nullable = false) val code: String,
    @Column(name = "`from`", nullable = false) val from: Timestamp,
    @Column(name = "`until`", nullable = false) val until: Timestamp,

    @OneToMany(mappedBy = "semester")
    val courses: List<Course>,

    override val id: Int = 0
) : IEntity(id){

    override fun canEdit(user: User) = user.isAdmin

    override fun canView(user: User?)
        = user?.isAdmin ?: false
            || courses.any { it.subject?.guarantors?.any { it.guarantor.id == user?.id } ?: false }

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  code = $code   ,   from = $from   ,   until = $until   ,   id = $id )"
    }
}
