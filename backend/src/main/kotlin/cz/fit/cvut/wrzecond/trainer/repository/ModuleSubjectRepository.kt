package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.ModuleSubject
import cz.fit.cvut.wrzecond.trainer.entity.Subject
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing ModuleSubjects entities.
 */
@Repository
interface ModuleSubjectRepository : IRepository<ModuleSubject> {
    /**
     * Deletes the association between a given module and subject.
     *
     * @param module The module entity from which the subject should be dissociated.
     * @param subject The subject entity to be dissociated from the module.
     */
    fun deleteByModuleAndSubject(module: Module, subject: Subject)
}