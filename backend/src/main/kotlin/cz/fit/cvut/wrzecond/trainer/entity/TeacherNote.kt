package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.sql.Timestamp

@Entity
data class TeacherNote(
    @Column(nullable = false, columnDefinition = "TEXT") val content: String,
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    val created: Timestamp,
    @Column(nullable = false)
    val redacted: Boolean = false,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    val author: User,

    override val id: Int = 0
) : IEntity(id) {
    override fun canView(user: User?) = user?.id == author.id || user?.id == module.author.id
            || user?.courses?.any { it.role.level == RoleLevel.TEACHER } == true
            || user?.isAdmin == true

    override fun canEdit(user: User) = user.id == author.id || user.id == module.author.id
            || user.courses.any { it.role.level == RoleLevel.TEACHER } || user.isAdmin
}