package cz.fit.cvut.wrzecond.trainer.entity.quiz

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "quizroom_id"])])
class QuizroomStudent (

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    val student : User,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "quizroom_id", nullable = true)
    val quizroom : Quizroom,

    var points : Int,

    override val id: Int = 0
) : IEntity(id){

    override fun canEdit(user: User) = true

    override fun canView(user: User?) = true
}