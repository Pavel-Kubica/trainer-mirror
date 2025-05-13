package cz.fit.cvut.wrzecond.trainer.entity

import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionSubject
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity
data class Subject(
    @Column(nullable = false) val name: String,
    @Column(nullable = false) val code: String,

    @OneToMany(mappedBy = "subject")
    val courses: List<Course>,
    @OneToMany(mappedBy = "subject")
    val guarantors: List<SubjectGuarantor>,
    @OneToMany(mappedBy = "subject")
    val questions: List<QuestionSubject>,

    override val id: Int = 0
) : IEntity(id) {
    override fun canEdit(user: User) = user.isAdmin
    override fun canView(user: User?) =
        (user != null &&
                (guarantors.any { it.guarantor.id == user.id } || courses.any { it.canEdit(user) })
                ) || user?.isAdmin ?: false

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  name = $name   ,   code = $code   ,   id = $id )"
    }
}
