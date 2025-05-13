package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.sql.Timestamp

@Entity
data class Week(
    @Column(nullable = true) val name: String?,
    @Column(name = "`from`", nullable = false) val from: Timestamp,
    @Column(name = "`until`", nullable = false) val until: Timestamp,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "course_id", nullable = false)
    val course: Course,

    @OneToMany(mappedBy = "week")
    val lessons: List<Lesson>,

    override val id: Int = 0
) : IEntity(id) {

    override fun canEdit(user: User) = course.canEdit(user) || user.isAdmin
    override fun canView(user: User?) = course.canView(user) || user?.isAdmin ?: false

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  name = $name   ,   from = $from   ,   until = $until   ,   id = $id )"
    }

}
