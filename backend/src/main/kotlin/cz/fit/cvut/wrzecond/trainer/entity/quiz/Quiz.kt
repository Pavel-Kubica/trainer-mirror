package cz.fit.cvut.wrzecond.trainer.entity.quiz

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
class Quiz(

    @Column(nullable = false) val numOfQuestions : Int,
    @Column(nullable = false) val name : String,
    @Column(nullable = true) val numOfAttempts : Int,

    @OneToMany(mappedBy = "quiz")
    //@OnDelete(action = OnDeleteAction.CASCADE)
    val questions : List<QuizQuestion>,

    @OneToMany(mappedBy = "quiz")
    val quizRooms : List<Quizroom>,

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "module_id", nullable = true)
    val module: Module,

    override val id: Int = 0
) : IEntity(id) {
    override fun canView(user: User?): Boolean {
        return module.canView(user) || user?.isAdmin ?: false
    }

    override fun canEdit(user: User): Boolean {
        return module.canEdit(user) || user.isAdmin
    }
}
