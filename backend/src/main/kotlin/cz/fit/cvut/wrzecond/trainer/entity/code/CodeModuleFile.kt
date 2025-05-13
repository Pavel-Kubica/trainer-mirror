package cz.fit.cvut.wrzecond.trainer.entity.code

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
data class CodeModuleFile(
    @Column(nullable = false) val name: String,
    @Column(nullable = false) val codeLimit: Int,
    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val content: String,
    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val reference: String,

    @Column(nullable = false, columnDefinition = "boolean default false")
    val headerFile: Boolean,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "code_module_id", nullable = false)
    val codeModule: CodeModule,
    override val id: Int,
) : IEntity(id) {

    override fun canView(user: User?) = codeModule.canView(user) || user?.isAdmin ?: false
    override fun canEdit(user: User ) = codeModule.canEdit(user) || user.isAdmin

}
