package cz.fit.cvut.wrzecond.trainer.entity.quiz

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
class QuestionInstance(

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "quizroom_id", nullable = false)
    val quizroom: Quizroom,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @Column(nullable = false) val orderNum: Int,

    override val id: Int = 0

) : IEntity(id){

    override fun canEdit(user: User) = true

    override fun canView(user: User?) = true
}