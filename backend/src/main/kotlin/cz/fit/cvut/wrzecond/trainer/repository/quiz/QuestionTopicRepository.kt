package cz.fit.cvut.wrzecond.trainer.repository.quiz

import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuestionTopic
import cz.fit.cvut.wrzecond.trainer.entity.Topic
import cz.fit.cvut.wrzecond.trainer.entity.quiz.Question
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing QuestionTopics entities.
 */
@Repository
interface QuestionTopicRepository : IRepository<QuestionTopic> {

    /**
     * Deletes an entry from the database based on the provided question and topic.
     *
     * @param question The question that identifies the entry to be deleted.
     * @param topic The topic that identifies the entry to be deleted.
     */
    fun deleteByQuestionAndTopic(question: Question, topic: Topic)
}