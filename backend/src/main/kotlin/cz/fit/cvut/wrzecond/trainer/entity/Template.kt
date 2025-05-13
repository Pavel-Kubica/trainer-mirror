package cz.fit.cvut.wrzecond.trainer.entity

import cz.fit.cvut.wrzecond.trainer.entity.code.*

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.sql.Timestamp

@Entity
data class Template(
    @Column(nullable = false) val name: String,
    @Column(nullable = false) val codeType: CodeType,
    @Column(nullable = false) val interactionType: InteractionType,
    @Column(nullable = false) val libraryType: LibraryType,
    @Column(nullable = false) val envelopeType: EnvelopeType,
    @Column(nullable = true, columnDefinition = "LONGTEXT") @Lob val customEnvelope: String?,
    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val codeHidden: String,
    @Column(nullable = false) val fileLimit: Int,

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    val lastModificationTime: Timestamp,

    @OneToMany(mappedBy = "template")
    val tests: List<CodeModuleTest>,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,
    override val id: Int = 0
) : IEntity(id) {

    override fun canView(user: User?)
            = user?.id == author.id || user?.isAdmin == true

    override fun canEdit(user: User)
            = user.id == author.id || user.isAdmin

}