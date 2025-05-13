package cz.fit.cvut.wrzecond.trainer.entity

import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionTopic
import jakarta.persistence.*

@Entity
data class Topic(
    @Column(nullable = false) val name: String,

    @OneToMany(mappedBy = "topic")
    val modules: List<ModuleTopic>,

    @OneToMany(mappedBy = "topic")
    val questions: List<QuestionTopic>,

    override val id: Int = 0
) : IEntity(id) {

    override fun canView(user: User?) =
        user?.courses?.any { it.role.level == RoleLevel.TEACHER } ?: false

    override fun canEdit(user: User) =
        user.isAdmin || user.courses.any { courseUser -> courseUser.course.subject?.guarantors?.any{it.guarantor.id == user.id} == true }

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  id = $id   ,   name = $name )"
    }

}
