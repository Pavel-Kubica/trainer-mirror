package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.entity.RoleLevel

/**
 * Data transfer object for course list
 * @property id unique identifier of course
 * @property name course name
 * @property shortName course short name
 * @property subject course subject
 * @property semester course semester or year
 * @property lessonCompleted completed lessons in course
 * @property lessonCount number of lessons in course
 * @property role role of user in course
 */
data class CourseFindDTO(override val id: Int, val name: String, val shortName: String,
                         val subject: SubjectFindDTO?, val semester: SemesterFindDTO?,
                         // because of performance, looking for better solution
                         //val lessonCompleted: Int, val lessonCount: Int,
                         val role: RoleLevel?) : IFindDTO

/**
 * Data transfer object for course detail
 * @property id unique identifier of course
 * @property name course name
 * @property shortName course short name
 * @property subject course subject
 * @property semester course semester or year
 * @property role role of user in course
 * @property secret course join secret (show only to teachers)
 * @property weeks course weeks
 */
data class CourseGetDTO(override val id: Int, val name: String, val shortName: String,
                        val subject: SubjectFindDTO?, val semester: SemesterFindDTO?,
                        val role: RoleLevel?, val public: Boolean ,val secret: String?, val weeks: List<WeekGetDTO>) : IGetDTO

/**
 * Data transfer object for course joining / setting secret
 * @property secret course secret (null for unsetting)
 */
data class CourseSecretDTO(val secret: String?)

/**
 * Data transfer object used to change course values
 * @property name new name of course being changed
 * @property shortName new short name of course being changed
 * @property public public status of course being changed
 * @property teachers new teachers of course being changed
 * @property semester new semester of course being changed
 * @property subject course subject id
 */
data class CourseUpdateDTO (val name: String?, val shortName: String?,val public: Boolean?, val teachers: List<UserFindDTO>?, val semester: Int?, val subject: Int?) : IUpdateDTO

/**
 * Data transfer object used for creating new course
 * @property name name of newly created course
 * @property shortName short name of newly created course
 * @property public public status of newly created course
 * @property semester semester id of newly created course
 * @property subject course subject id
 */
data class CourseCreateDTO (val name: String, val shortName: String, val public: Boolean, val semester: Int?, val subject: Int?) : ICreateDTO
