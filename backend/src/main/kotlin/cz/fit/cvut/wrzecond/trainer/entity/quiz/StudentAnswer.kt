package cz.fit.cvut.wrzecond.trainer.entity.quiz

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
class StudentAnswer (

    @Column(nullable = true, columnDefinition = "LONGTEXT") @Lob val answerData : String,

    @Column(nullable = true) val isCorrect : AnswerStatusType,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "quizroom_id", nullable = true)
    val quizroom: Quizroom?,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    val student: User,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "question_id")
    val question: Question,

    override val id: Int = 0
) : IEntity(id) {

    override fun canEdit(user: User): Boolean {
        return true
    }

    override fun canView(user: User?): Boolean {
        return true
    }
}


enum class AnswerStatusType {
    UNKNOWN, CORRECT, INCORRECT
}