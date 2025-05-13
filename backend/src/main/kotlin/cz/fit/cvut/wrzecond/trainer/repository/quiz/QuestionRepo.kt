package cz.fit.cvut.wrzecond.trainer.repository.quiz

import cz.fit.cvut.wrzecond.trainer.entity.quiz.Question
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Question entities.
 */
@Repository
interface QuestionRepo : IRepository<Question> {

    /**
     * Retrieves all questions submitted by a specific author.
     *
     * @param username The username of the author whose questions are to be retrieved.
     * @return A list of Question objects.
     */
    fun getAllByAuthorUsername(username: String) : List<Question>

    /**
     * Retrieves a list of distinct questions that are associated with the given topic IDs.
     *
     * @param topicIds a list of topic IDs to filter the questions by
     * @return a list of distinct questions associated with the provided topic IDs
     */
    @Query(
        "SELECT DISTINCT question FROM Question question" +
                " JOIN QuestionTopic qt ON qt.question = question" +
                " WHERE qt.topic.id IN :topicIds"
    )
    fun findByTopics(topicIds: List<Int>) : List<Question>

    /**
     * Retrieves a distinct list of questions associated with the given subject IDs.
     *
     * @param subjectsIds A list of subject IDs to filter the questions by.
     * @return A list of questions that are associated with the specified subject IDs.
     */
    @Query(
        "SELECT DISTINCT question FROM Question question" +
                " JOIN QuestionSubject qt ON qt.question = question" +
                " WHERE qt.subject.id IN :subjectsIds"
    )
    fun findBySubjects(subjectsIds: List<Int>) : List<Question>
}