package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.sql.Timestamp

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["student_id", "lesson_id", "module_id"])])
data class StudentModule(
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "lesson_id", nullable = false)
    val lesson: Lesson,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    val student: User,

    @Column(nullable = true)
    val percent: Int?,

    @Column(nullable = true) val openedOn: Timestamp?,
    @Column(nullable = true) val completedOn: Timestamp?,

    @Column(nullable = false, columnDefinition = "boolean default false")
    val allowedShow: Boolean,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val completedEarly: Boolean,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val unlocked: Boolean,

    @Column(nullable = true) val file: String?,

    @OneToMany(mappedBy = "studentModule")
    val requests: List<StudentModuleRequest>,

   // @OneToMany(mappedBy = "studentModule")
   // val scoringRules: List<ScoringRuleStudentModule>,

    override val id: Int = 0,
) : IEntity(id) {

    override fun canView(user: User?)
            = lesson.canView(user) || user?.isAdmin ?: false

    override fun canEdit(user: User)
            = lesson.canEdit(user) || user.isAdmin

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  percent = $percent   ,   openedOn = $openedOn   ,   completedOn = $completedOn   ,   allowedShow = $allowedShow   ,   completedEarly = $completedEarly   ,   unlocked = $unlocked   ,   file = $file   ,   id = $id )"
    }

}
