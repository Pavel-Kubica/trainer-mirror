package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Topic
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Topics entities.
 */
@Repository
interface TopicRepository : IRepository<Topic>