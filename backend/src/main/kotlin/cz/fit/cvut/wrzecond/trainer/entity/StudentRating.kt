package cz.fit.cvut.wrzecond.trainer.entity;


import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.sql.Timestamp

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["module_id","student_id"])])
data class StudentRating (
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    val student: User,

    @Column(nullable = true)
    val points: Int?,

    @Column(nullable = true) val text: String?,

    @Column(nullable = false) val published: Timestamp,


    override val id: Int = 0,
    ): IEntity(id)

{

    override fun canEdit(user: User)
            = user.id == student.id || user.isAdmin

    override fun canView(user: User?) = user?.id == student.id || user?.isAdmin ?: false

}