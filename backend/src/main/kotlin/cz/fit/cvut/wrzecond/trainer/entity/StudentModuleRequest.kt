package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.sql.Timestamp

@Entity
data class StudentModuleRequest(
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sm_id", nullable = false)
    val studentModule: StudentModule,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "teacher_id", nullable = true)
    val teacher: User?,

    @Column(nullable = false) val requestType: StudentModuleRequestType,
    @Column(nullable = true, columnDefinition = "LONGTEXT") @Lob val requestText: String?,
    @Column(nullable = false) val requestedOn: Timestamp,

    @Column(nullable = true, columnDefinition = "LONGTEXT") @Lob val teacherResponse: String?,
    @Column(nullable = false, columnDefinition = "boolean default false") val satisfied: Boolean,
    @Column(nullable = true) val satisfiedOn: Timestamp?,

    override val id: Int = 0,
) : IEntity(id) {

    override fun canView(user: User?)
        = studentModule.canView(user) || user?.isAdmin ?: false

    override fun canEdit(user: User)
        = studentModule.canEdit(user) || user.isAdmin

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  studentModule = $studentModule   ,   teacher = $teacher   ,   requestType = $requestType   ,   requestText = $requestText   ,   requestedOn = $requestedOn   ,   teacherResponse = $teacherResponse   ,   satisfied = $satisfied   ,   satisfiedOn = $satisfiedOn   ,   id = $id )"
    }

}

enum class StudentModuleRequestType {
    HELP, COMMENT, EVALUATE,
}
