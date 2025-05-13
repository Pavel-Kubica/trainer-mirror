package cz.fit.cvut.wrzecond.trainer.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.Column
import jakarta.persistence.Entity
import org.springframework.format.annotation.DateTimeFormat
import java.sql.Timestamp

@Entity
data class Log (
        @Column(nullable = false) val username: String,
        @Column(nullable = false) val ipAddress: String,
        @Column(nullable = false) val client: String,
        @Column(nullable = false) val timestamp: Timestamp,
        @Column(nullable = false) val entity: String,
        @Column(nullable = false) val entityId: Int,
        @Column(nullable = false) val operation: String,

        override val id: Int = 0
) : IEntity(id) {
    override fun canView(user: User?)
        = user?.isAdmin ?: false

    override fun canEdit(user: User)
        = user.isAdmin
}