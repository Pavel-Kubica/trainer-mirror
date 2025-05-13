package cz.fit.cvut.wrzecond.trainer.repository.code

import cz.fit.cvut.wrzecond.trainer.entity.code.CodeModule
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing CodeModule entities.
 */
@Repository
interface CodeModuleRepository: IRepository<CodeModule> {

    /**
     * Retrieves a CodeModule entity based on the provided module ID.
     *
     * @param id The ID of the module associated with the CodeModule.
     * @return The CodeModule entity that corresponds to the given module ID.
     */
    @Query("SELECT cm FROM CodeModule cm WHERE cm.module.id = :id")
    fun getByModuleId(id: Int) : CodeModule

}
