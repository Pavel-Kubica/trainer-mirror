package cz.fit.cvut.wrzecond.trainer.dto

import java.sql.Timestamp

/**
 * Data transfer object for teacher's note finding
 * @property id unique note identifier
 * @property content note's content
 * @property created timestamp when note was created
 * @property author author of the note
 */
data class TeacherNoteFindDTO(override val id: Int, val content: String, val created: Timestamp, val author: UserFindDTO, val redacted: Boolean) : IFindDTO, IGetDTO

/**
 * Data transfer object for teacher's note creating
 * @property content note's content
 * @property created timestamp when note was created
 */
data class TeacherNoteCreateDTO(val content: String, val created: Timestamp) : ICreateDTO

/**
 * Data transfer object for teacher's note updating
 * @property content note's content
 * @property created timestamp when note was updated
 */
data class TeacherNoteUpdateDTO(val content: String, val created: Timestamp) : IUpdateDTO