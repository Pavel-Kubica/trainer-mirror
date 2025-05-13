package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.entity.RoleLevel

/**
 * Data transfer object for course user list
 * @property course the course
 * @property users list of users
 */
data class CourseUserList(val course: CourseFindDTO, val users: List<CourseUserReadDTO>)

/**
 * Data transfer object for course user detail
 * @property course the course (with weeks and progress)
 * @property user the user
 */
data class CourseUserDetail(val course: CourseGetDTO, val user: UserFindDTO)

/**
 * Data transfer object for course user list / detail
 * @property id unique identifier of course user
 * @property username username
 * @property name user's name
 * @property role user's role in course
 * @property progress user's progress in course
 */
data class CourseUserReadDTO(override val id: Int, val username: String, val name: String,
                             val role: RoleLevel, val progress: Int?) : IFindDTO, IGetDTO

/**
 * Data transfer object for course user creating
 * @property username username
 * @property name user's name
 * @property role user's role in course
 */
data class CourseUserEditDTO(val username: String, val name: String?, val role: RoleLevel) : ICreateDTO, IUpdateDTO
