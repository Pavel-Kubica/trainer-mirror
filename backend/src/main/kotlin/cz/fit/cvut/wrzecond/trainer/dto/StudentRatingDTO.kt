package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.entity.StudentModule
import cz.fit.cvut.wrzecond.trainer.entity.StudentModuleRequestType
import java.sql.Timestamp

/**
 * Data transfer object for student rating detail
 * @property id unique identifier of student rating
 * @property points points given by student
 * @property text student rating review
 * @property student user who wrote review
 * @property module module review describes
 */
data class StudentRatingGetDTO(override val id: Int, val points: Int?, val text: String?,
    val student: UserFindDTO?, val module: ModuleFindDTO?) : IGetDTO

/**
 * Data transfer object for student rating list
 * @property id unique identifier of student rating
 * @property points points given by student
 * @property text student rating review
 * @property published time of rating publishing
 */

data class StudentRatingFindDTO(override val id: Int, val points: Int?, val text: String?, val published: Timestamp, val student: UserFindDTO?): IFindDTO

/**
 * Data transfer object for student rating update
 * @property points points given by student
 * @property text student rating review
 */

data class StudentRatingUpdateDTO (val points: Int?, val text: String?) :IUpdateDTO

/**
 * Data transfer object for student rating creation
 * @property points points given by student
 * @property text student rating review
 * @property student user who wrote review
 * @property module module review describes
 */

data class StudentRatingCreateDTO (val points: Int, val text: String, val student: Int, val module: Int) : ICreateDTO
