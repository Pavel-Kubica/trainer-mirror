package cz.fit.cvut.wrzecond.trainer.entity.quiz

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.RoleLevel
import cz.fit.cvut.wrzecond.trainer.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
class Question(

    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val questionData: String,
    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val possibleAnswersData: String,
    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val correctAnswerData: String,
    @Column(columnDefinition = "LONGTEXT") @Lob val explanation: String,
    val singleAnswer: Boolean,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "user_id", nullable = false)
    val author: User,

    @Column(nullable = false) val timeLimit: Int,

    @Column(nullable = true) val questionType: QuestionType,

    @OneToMany(mappedBy = "question")
    val questionAnswer: List<StudentAnswer>,

    @OneToMany(mappedBy = "question")
    //@OnDelete(action = OnDeleteAction.CASCADE)
    val quizzes: List<QuizQuestion>,

    @OneToMany(mappedBy = "question")
    val topics: List<QuestionTopic>,

    @OneToMany(mappedBy = "question")
    val subjects: List<QuestionSubject>,

    override val id: Int = 0

) : IEntity(id) {

    override fun canEdit(user: User): Boolean {
        return user.username == author.username ||
                user.isAdmin ||
                subjects.any{
                    cs -> cs.subject.guarantors.any{
                        subjectGuarantor -> subjectGuarantor.guarantor == user
                    }
                }
    }

    override fun canView(user: User?): Boolean {
        return user?.isAdmin == true || subjects.any{qs -> qs.subject.guarantors.any{subjectGuarantor -> subjectGuarantor.guarantor == user }} ||
               user?.let {
            it.username == author.username || it.courses.any { course ->
                subjects.any { subject ->
                    subject.subject == course.course.subject && course.role.level == RoleLevel.TEACHER
                }
            }
        } ?: false
    }
}


/**
 * LEGACY questions are questions with 4 answers
 * The rest is self-explanatory
 *
 * NOVE TYPY PRIDAVAT NA KONIEC ZOZNAMU INAK SA MOZU (TAKMER URCITE) POKAZIT OTAZKY, PRETOZE TYP OTAZKY JE V DB REPREZENTOVANY AKO PORADOVE CISLO
 * */
enum class QuestionType {
    LEGACY, TRUEFALSE, MULTICHOICE, OPEN, FILLTEXT, SORT, CONNECT
}