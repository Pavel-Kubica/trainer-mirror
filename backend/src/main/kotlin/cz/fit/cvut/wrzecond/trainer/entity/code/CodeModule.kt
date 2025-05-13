package cz.fit.cvut.wrzecond.trainer.entity.code

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
data class CodeModule(
    @Column(nullable = false) val codeType: CodeType,
    @Column(nullable = false) val interactionType: InteractionType,
    @Column(nullable = false) val libraryType: LibraryType,
    @Column(nullable = false) val envelopeType: EnvelopeType,
    @Column(nullable = true, columnDefinition = "LONGTEXT") @Lob val customEnvelope: String?,
    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val codeHidden: String,
    @Column(nullable = false) val fileLimit: Int,

    @Column(nullable = false, columnDefinition = "boolean default false")
    val hideCompilerOutput: Boolean,

    @Column(nullable = false, columnDefinition = "boolean default false")
    val referencePublic: Boolean,

    @OneToMany(mappedBy = "codeModule")
    val tests: List<CodeModuleTest>,

    @OneToMany(mappedBy = "codeModule")
    val files: List<CodeModuleFile>,

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "module_id", nullable = false)
    val module: Module,
    override val id: Int,
) : IEntity(id) {

    override fun canView(user: User?)
        = module.canView(user) || user?.isAdmin ?: false

    override fun canEdit(user: User)
        = module.canEdit(user) || user.isAdmin

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  codeType = $codeType   ,   interactionType = $interactionType   ,   libraryType = $libraryType   ,   envelopeType = $envelopeType   ,   customEnvelope = $customEnvelope   ,   codeHidden = $codeHidden   ,   fileLimit = $fileLimit   ,   referencePublic = $referencePublic   ,   module = $module   ,   id = $id )"
    }

}

enum class CodeType {
    SHOWCASE, TEST_ASSERT, TEST_IO, WRITE_ASSERT, WRITE_IO
}

enum class InteractionType {
    EDITOR, UPDOWN_ONLY
}

enum class EnvelopeType {
    ENV_C, ENV_C_IO, ENV_CPP, ENV_CPP_STL, ENV_CUSTOM
}

enum class LibraryType {
    NO_LIB, LIB_C, LIB_CPP
}
