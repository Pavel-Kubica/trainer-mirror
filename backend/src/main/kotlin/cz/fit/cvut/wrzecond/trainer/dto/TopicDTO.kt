package cz.fit.cvut.wrzecond.trainer.dto

/**
 * Data transfer object for topics
 * @property id unique topic identifier
 * @property name topic name
 */
data class TopicFindDTO(override val id: Int, val name: String?) : IFindDTO, IGetDTO

/**
 * Data transfer object for topic creating
 * @property name topic name
 */

data class TopicCreateDTO(val name: String) : ICreateDTO, IUpdateDTO