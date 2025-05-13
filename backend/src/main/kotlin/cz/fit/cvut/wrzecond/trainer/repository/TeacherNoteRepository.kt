package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.TeacherNote

/**
 * Repository interface for managing TeacherNotes entities.
 */
interface TeacherNoteRepository: IRepository<TeacherNote> {

    /**
     * Finds a list of TeacherNotes by given id and module.
     *
     * @param id The id of the TeacherNote to find.
     * @param moduleId The module object that the TeacherNote belongs to.
     * @return A list of TeacherNotes that match the given id and module.
     */
    fun findByIdAndModule(id: Int, moduleId: Module): List<TeacherNote>
}