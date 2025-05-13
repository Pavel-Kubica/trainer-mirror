package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.sql.Timestamp

@Entity
data class Lesson(
    @Column(nullable = false) val name: String,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val hidden: Boolean,
    @Column(name = "`order`", nullable = false) val order: Int,

    @Column(nullable = true) val timeStart: Timestamp?,
    @Column(nullable = true) val timeEnd: Timestamp?,

    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val description: String,
    @Column(nullable = false) val type: LessonType,
    @Column(nullable = true) val lockCode: String?,
    @Column(nullable = true) val referenceSolutionAccessibleFrom: Timestamp?,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "week_id", nullable = false)
    val week: Week,

    @OneToMany(mappedBy = "lesson")
    val modules: List<LessonModule>,

    @OneToMany(mappedBy = "lesson")
    val students: List<StudentModule>,

    @OneToMany(mappedBy = "lesson")
    val scoringRules: List<ScoringRule>,

    override val id: Int = 0
) : IEntity(id) {

    // Either the lesson is public and has already started (in which case student can see it)
    // or it is hidden and the user has teacher rights
    override fun canView(user: User?)
        = (!hidden && week.course.canView(user) && (timeStart == null || (timeStart <= Timestamp(System.currentTimeMillis())))) || (user != null && week.course.canEdit(user)) || user?.isAdmin ?: false

    override fun canEdit(user: User)
        = week.course.canEdit(user) || user.isAdmin

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  name = $name   ,   hidden = $hidden   ,   order = $order   ,   timeStart = $timeStart   ,   timeEnd = $timeEnd   ,   description = $description   ,   type = $type   ,   lockCode = $lockCode   ,   week = $week   ,   id = $id )"
    }

}

enum class LessonType {
    TUTORIAL,
    TUTORIAL_PREPARATION,
    INDIVIDUAL_TASK,
    SUPPLEMENTARY,
    FREQUENT_MISTAKES,
    INFORMATION // organization of course
}
