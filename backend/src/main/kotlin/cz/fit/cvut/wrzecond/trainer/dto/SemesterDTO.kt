package cz.fit.cvut.wrzecond.trainer.dto

import java.sql.Timestamp

/**
 * Data transfer object for semester list
 * @property id unique identifier of semester
 * @property code semester code
 * @property from semester begin date
 * @property until semester end date
 */
data class SemesterFindDTO(override val id: Int, val code: String, val from: Timestamp, val until: Timestamp) : IFindDTO

/**
 * Data transfer object for semester detail
 * @property id unique identifier of semester
 * @property code semester code
 * @property from semester begin date
 * @property until semester end date
 */
data class SemesterGetDTO(override val id: Int, val code: String, val from: Timestamp, val until: Timestamp) : IGetDTO

/**
 * Data transfer object for semester creation
 * @property code semester code
 * @property from semester begin date
 * @property until semester end date
 */
data class SemesterCreateDTO(val code: String, val from: Timestamp, val until: Timestamp) : ICreateDTO

/**
 * Data transfer object for semester editing
 * @property code semester code
 * @property from semester begin date
 * @property until semester end date
 */
data class SemesterUpdateDTO(val code: String?, val from: Timestamp?, val until: Timestamp?) : IUpdateDTO

/**
 * Data transfer object for course list in semester
 * @property id unique identifier of course
 * @property name course name
 * @property shortName course short name
 * @property subject course subject
 * @property semester course semester
 */
data class SemesterCourseReadDTO(override val id: Int, val name: String, val shortName: String,
                                 val subject: SubjectFindDTO?, val semester: SemesterFindDTO?) : IFindDTO