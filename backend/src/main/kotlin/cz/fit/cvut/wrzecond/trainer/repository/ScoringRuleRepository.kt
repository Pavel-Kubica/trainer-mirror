package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Lesson
import cz.fit.cvut.wrzecond.trainer.entity.LessonModule
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.ScoringRule
import org.springframework.data.jpa.repository.Query

interface ScoringRuleRepository : IRepository<ScoringRule>{

    @Query("SELECT sr FROM ScoringRule sr WHERE sr.lesson = :lesson")
    fun getScoringRulesByLesson (lesson: Lesson) : List<ScoringRule>

    @Query(
        "SELECT DISTINCT sr FROM ScoringRule sr" +
                " WHERE sr.lesson.id IN :lessonIds"
    )
    fun findByLessons(lessonIds: List<Int>) : List<ScoringRule>
}