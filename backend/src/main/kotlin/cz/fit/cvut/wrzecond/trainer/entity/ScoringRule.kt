package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.sql.Timestamp


@Entity
data class ScoringRule(
    @Column(nullable = false) val name: String,
    @Column(nullable = false) val shortName: String,
    @Column(nullable = true) val description: String?,
    @Column(nullable = false) val points: Double,
    @Column(name = "`until`",nullable = false) val until: Timestamp,
    @Column(nullable = true) val toComplete: Int?,


    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "lesson_id", nullable = false)
    val lesson: Lesson,

    @OneToMany(mappedBy = "scoringRule")
    val modules: List<ScoringRuleModule>,

    //@OneToMany(mappedBy = "scoringRule")
    //val studentModules: List<ScoringRuleStudentModule>,

    override val id: Int = 0

) : IEntity(id) {

    override fun canView(user: User?)
            = lesson.canView(user) || user?.courses?.any { it.role.level == RoleLevel.TEACHER } == true
            || user?.isAdmin == true

    override fun canEdit(user: User)
            = lesson.canEdit(user) || user.isAdmin

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  name = $name   ,   shortName = $shortName   ,   points = $points   ,   until = $until   , " +
                "  toComplete = $toComplete   ,   lesson = $lesson   , id = $id )"
    }

}