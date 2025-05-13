package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import jakarta.transaction.Transactional
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
data class Course(
    @Column(nullable = false) val name: String,
    @Column(nullable = false) val shortName: String,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val public: Boolean,
    @Column(nullable = true, unique = true) val secret: String?,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "subject_id", nullable = true)
    val subject: Subject?,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "semester_id", nullable = true)
    val semester: Semester?,

    @OneToMany(mappedBy = "course")
    val weeks: List<Week>,
    @OneToMany(mappedBy = "course")
    val users: List<CourseUser>,

    override val id: Int = 0
) : IEntity(id) {

    override fun canView(user: User?)
        = public
            || users.any { it.user.id == user?.id }
            || subject?.guarantors?.any { it.guarantor.id == user?.id } ?: false
            || user?.isAdmin ?: false

    override fun canEdit(user: User)
        = users.any { it.role.level == RoleLevel.TEACHER && it.user.id == user.id }
            || subject?.guarantors?.any { it.guarantor.id == user.id } ?: false
            || user.isAdmin

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  name = $name   ,   shortName = $shortName   ,   public = $public   ,   secret = $secret   ,   subject = $subject   ,   semester = $semester   ,   id = $id )"
    }

}
