package cz.fit.cvut.wrzecond.trainer.dto

import java.sql.Timestamp

/**
 * Data Transfer Object for representing log information.
 *
 * @property id The unique identifier of the log entry.
 * @property username The username associated with the log entry.
 * @property ipAddress The IP address of the user.
 * @property client The client information related of the user.
 * @property timestamp The timestamp when the log entry was created.
 * @property entity The entity type related to the log entry.
 * @property entityId The unique identifier of the entity related to the log entry.
 * @property operation The operation performed, logged in this entry.
 */
data class LogFindDTO(override val id: Int, val username: String,
                      val ipAddress: String, val client: String,
                      val timestamp: Timestamp, val entity: String,
                      val entityId: Int, val operation: String,) : IFindDTO