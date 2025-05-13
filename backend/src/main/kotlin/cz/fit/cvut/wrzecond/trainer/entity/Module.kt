package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.sql.Timestamp

@Entity
data class Module(
    @Column(nullable = false) val name: String,
    @Column(nullable = false) val type: ModuleType,
    @Column(nullable = false, columnDefinition = "LONGTEXT") @Lob val assignment: String,
    @Column(nullable = true) val difficulty: ModuleDifficulty?,

    @Column(nullable = false, columnDefinition = "boolean default false")
    val lockable: Boolean,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val timeLimit: Boolean,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val manualEval: Boolean,
    @Column(nullable = false, columnDefinition = "boolean default false")
    val hidden: Boolean,

    @Column(nullable = false) val minPercent: Int,
    @Column(nullable = true) val file: String?,
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    val lastModificationTime: Timestamp,

    @OneToMany(mappedBy = "module")
    val topics: List<ModuleTopic>,
    @OneToMany(mappedBy = "module")
    val subjects: List<ModuleSubject>,
    @OneToMany(mappedBy = "module")
    val lessons: List<LessonModule>,
    @OneToMany(mappedBy = "module")
    val students: List<StudentModule>,
    @OneToMany(mappedBy = "module")
    val editors: List<ModuleEditor>,
    @OneToMany(mappedBy = "module")
    val notes: List<TeacherNote>,
    @OneToMany(mappedBy = "module")
    val ratings: List<StudentRating>,


    @ManyToOne
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,

    override val id: Int = 0
) : IEntity(id) {

    override fun canView(user: User?)
        = user?.id == author.id || (!hidden && lessons.any { it.lesson.canView(user) })
            || user?.courses?.any { it.role.level == RoleLevel.TEACHER } == true
            || user?.isAdmin == true

    // Only author and editors can edit the module
    override fun canEdit(user: User)
        = user.id == author.id || editors.any { it.editor.id == user.id } || user.isAdmin

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  name = $name   ,   type = $type   ,   assignment = $assignment   ,   difficulty = $difficulty   ,   lockable = $lockable   ,   timeLimit = $timeLimit   ,   manualEval = $manualEval   ,   hidden = $hidden   ,   minPercent = $minPercent   ,   file = $file   ,   lastModificationTime = $lastModificationTime   ,   id = $id   ,   author = $author )"
    }

}

enum class ModuleType(val hasProgress: Boolean) {
    QUIZ (true),
    CODE (true),
    TEXT (false),
    ASSIGNMENT (true),
    SELFTEST(false)
}

enum class ModuleDifficulty {
    BEGINNER, EASY, MEDIUM, DIFFICULT, EXTREME
}
