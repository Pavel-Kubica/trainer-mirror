package cz.fit.cvut.wrzecond.trainer.entity.quiz

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
class Quizroom(

    @Column(nullable = false) val createdBy : String,
    @Column(nullable = false) val roomPassword : String,
    @Column(nullable = false) val currQuestion : Int,
    @Column(nullable = false) val timeLeft : Int,   //prerobit na date aby nebolo potreba updatovat kazdu sekundu
    @Column(nullable = false) val roomState : Boolean,
    val quizState : String,


    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "quiz_id")
    val quiz : Quiz,

    @OneToMany(mappedBy = "quizroom")
    val answersInRoom : List<StudentAnswer>,

    override val id: Int = 0
) : IEntity(id){

    override fun canEdit(user: User): Boolean {
        return quiz.canEdit(user) || user.isAdmin
    }

    override fun canView(user: User?): Boolean {
        return quiz.canView(user) || user?.isAdmin ?: false
    }
}