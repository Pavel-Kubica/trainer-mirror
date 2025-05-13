package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Role
import cz.fit.cvut.wrzecond.trainer.entity.RoleLevel
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing Roles entities.
 */
@Repository
interface RoleRepository : IRepository<Role> {

    /**
     * Retrieves a role based on the specified level.
     *
     * @param level The level of the role to be retrieved.
     * @return The role corresponding to the given level.
     */
    @Query("SELECT r FROM Role r WHERE r.level = :level")
    fun getByLevel(level: RoleLevel) : Role

}
