package cz.fit.cvut.wrzecond.trainer.dto

/**
 * Data transfer object for subject list
 * @property id unique identifier of subject
 * @property name subject name
 * @property code subject code
 */
data class SubjectFindDTO(override val id: Int, val name: String, val code: String) : IFindDTO

/**
 * Data transfer object for subject detail
 * @property id unique identifier of subject
 * @property name subject name
 * @property code subject code
 * @property courses subject courses
 */
data class SubjectGetDTO(override val id: Int, val name: String, val code: String, val courses: List<CourseFindDTO>) : IGetDTO

/**
 * Data transfer object for subject creation
 * @property name subject name
 * @property code subject code
 */
data class SubjectCreateDTO(val name: String, val code: String ) : ICreateDTO

/**
 * Data transfer object for subject updating
 * @property name new name of subject
 * @property code new code of subject
 * @property guarantors new guarantors of subject being changed
 */
data class SubjectUpdateDTO(val name: String?, val code: String?, val guarantors: List<UserFindDTO>?) : IUpdateDTO
