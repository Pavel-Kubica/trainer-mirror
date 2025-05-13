package cz.fit.cvut.wrzecond.trainer.entity.code

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.Template
import cz.fit.cvut.wrzecond.trainer.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
data class CodeModuleTest(
    @Column(nullable = false) val name: String,
    @Column(nullable = false) val parameter: Int,
    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val description: String,

    @Column(nullable = true) val timeLimit: Int?,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val checkMemory: Boolean,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val shouldFail: Boolean,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val hidden: Boolean,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "template_id", nullable = true)
    val template: Template?,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "code_module_id", nullable = true)
    val codeModule: CodeModule?,
    override val id: Int,
) : IEntity(id) {

    override fun canView(user: User?) =
        codeModule?.canView(user) ?: template?.canView(user) ?: false

    override fun canEdit(user: User) =
        codeModule?.canEdit(user) ?: template?.canEdit(user) ?: false
}