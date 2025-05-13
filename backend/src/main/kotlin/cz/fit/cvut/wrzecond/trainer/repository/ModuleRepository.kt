package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.*
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Modules entities.
 */
@Repository
interface ModuleRepository: IRepository<Module> {

    /**
     * Retrieves a list of unique teachers associated with a given module who are not the author of the module.
     *
     * @param module The module for which the teachers are to be retrieved.
     * @param teacher The role level of the users to be considered as teachers, default is RoleLevel.TEACHER.
     * @return A list of users who are teachers associated with the specified module.
     */
    @Query(
        "SELECT DISTINCT cu.user FROM Module module" +
        " JOIN LessonModule lm ON lm.module = :module" +
        " JOIN CourseUser cu ON cu.course = lm.lesson.week.course" +
        " WHERE cu.role.level = :teacher AND cu.user.id != lm.module.author.id"
    )
    fun getModuleTeachers(module: Module, teacher: RoleLevel = RoleLevel.TEACHER) : List<User>


    /**
     * Finds distinct modules that are associated with at least one of the given topic IDs.
     *
     * @param topicIds A list of topic IDs to search for associated modules.
     * @return A list of modules that have at least one of the given topics.
     */
    @Query(
        "SELECT DISTINCT module FROM Module module" +
        " JOIN ModuleTopic mt ON mt.module = module" +
        " WHERE mt.topic.id IN :topicIds"
    )
    fun findByTopics(topicIds: List<Int>) : List<Module>

    /**
     * Finds and returns a list of distinct modules that are associated with the given subject IDs.
     *
     * @param subjectIds a list of subject IDs used to filter modules
     * @return a list of modules associated with the provided subject IDs
     */
    @Query(
            "SELECT DISTINCT module FrOM Module module" +
            " JOIN ModuleSubject ms ON ms.module = module" +
            " WHERE ms.subject.id IN :subjectIds"
    )
    fun findBySubjects(subjectIds: List<Int>) : List<Module>


}
