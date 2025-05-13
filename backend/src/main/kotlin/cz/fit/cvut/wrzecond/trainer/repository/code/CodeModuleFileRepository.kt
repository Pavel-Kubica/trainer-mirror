package cz.fit.cvut.wrzecond.trainer.repository.code

import cz.fit.cvut.wrzecond.trainer.entity.code.CodeModuleFile
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing CodeModuleFile entities.
 */
@Repository
interface CodeModuleFileRepository: IRepository<CodeModuleFile>
