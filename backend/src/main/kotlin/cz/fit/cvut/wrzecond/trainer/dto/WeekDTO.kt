package cz.fit.cvut.wrzecond.trainer.dto

import java.sql.Timestamp

/**
 * Data transfer object for weeks
 * @property id unique week identifier
 * @property name week name
 * @property from week start date
 * @property until week end date
 * @property course course in which week is
 */
data class WeekFindDTO(override val id: Int, val name: String?, val from: Timestamp, val until: Timestamp,
                       val course: CourseFindDTO) : IFindDTO

/**
 * Data transfer object for weeks
 * @property id unique week identifier
 * @property name week name
 * @property from week start date
 * @property until week end date
 * @property course course in which week is
 * @property lessons list of lessons in week
 */
data class WeekGetDTO(override val id: Int, val name: String?, val from: Timestamp, val until: Timestamp,
                      val course: CourseFindDTO, val lessons: List<LessonFindDTO>) : IGetDTO

/**
 * Data transfer object for week creating
 * @property name week name
 * @property from week start date
 * @property until week end date
 * @property courseId course id to create week in
 */
data class WeekCreateDTO(val name: String, val from: Timestamp, val until: Timestamp, val courseId: Int) : ICreateDTO

/**
 * Data transfer object for week updating
 * @property name week name
 * @property from week start date
 * @property until week end date
 */
data class WeekUpdateDTO(val name: String?, val from: Timestamp?, val until: Timestamp?) : IUpdateDTO

/**
 * Data transfer object for weeks
 * @property id unique week identifier
 * @property name week name
 * @property from week start date
 * @property until week end date
 * @property course course in which week is
 * @property lessons list of lessons in week
 */
data class WeekDetailDTO(val id: Int, val name: String?, val from: Timestamp, val until: Timestamp,
                         val course: CourseFindDTO, val lessons: List<LessonGetDTO>)

/**
 * Data transfer object for updating lessons in week
 * @property lessonIds ids of lessons to put in the week
 */
data class WeekLessonOrderDTO(val lessonIds: List<Int>)