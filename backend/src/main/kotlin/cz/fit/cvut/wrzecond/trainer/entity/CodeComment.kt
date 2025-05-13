package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
data class CodeComment (

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "smr_id", nullable = false)
    val moduleRequest: StudentModuleRequest,

    @Column(nullable = false) val fileName: String,
    @Column(nullable = false, name = "`row_number`") val rowNumber: Int,
    @Column(nullable = false, length = 512) @Lob val comment: String,

    override val id: Int = 0,
) : IEntity(id) {

    override fun canView(user: User?)
        = moduleRequest.canView(user) || user?.isAdmin ?: false

    override fun canEdit(user: User)
        = moduleRequest.canEdit(user) || user.isAdmin

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  moduleRequest = $moduleRequest   ,   fileName = $fileName   ,   rowNumber = $rowNumber   ,   comment = $comment   ,   id = $id )"
    }

}