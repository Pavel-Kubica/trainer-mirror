package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import java.sql.Timestamp

@Entity
data class User(
    @Column(nullable = false, unique = true) var loginSecret: String,
    @Column(nullable = false, unique = true) val username: String,
    @Column(nullable = false) val name: String,
    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    val lastNotificationTime: Timestamp,
    @Column(nullable = false)
    var blocked: Boolean = false,

    @OneToMany(mappedBy = "user")
    val courses: List<CourseUser>,
    @OneToMany(mappedBy = "student")
    val modules: List<StudentModule>,
    @OneToMany(mappedBy = "author")
    val teacherModules: List<Module>,
    @OneToMany(mappedBy = "author")
    val teachersNotes: List<TeacherNote>,
    @OneToMany(mappedBy = "student")
    val ratings: List<StudentRating>,

    override val id: Int = 0,

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    val isAdmin: Boolean = false,

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    var testMode: Boolean = false,

    @Column(nullable = true)
    var gitlabToken: String? = "",

) : IEntity(id) {

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  isAdmin = $isAdmin   ,   id = $id   ,   lastNotificationTime = $lastNotificationTime   ,   name = $name   ,   username = $username   ,   loginSecret = $loginSecret )"
    }
}
