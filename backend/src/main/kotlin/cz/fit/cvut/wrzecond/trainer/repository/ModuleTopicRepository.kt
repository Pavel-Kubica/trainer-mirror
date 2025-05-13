package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.ModuleTopic
import cz.fit.cvut.wrzecond.trainer.entity.Topic
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing ModuleTopics entities.
 */
@Repository
interface ModuleTopicRepository : IRepository<ModuleTopic> {

    /**
     * Deletes the association between a given module and a topic.
     *
     * @param module The module from which the topic will be removed.
     * @param topic The topic that will be removed from the module.
     */
    fun deleteByModuleAndTopic(module: Module, topic: Topic)
}