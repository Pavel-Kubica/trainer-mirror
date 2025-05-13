package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.SandboxUser

/**
 * Repository interface for managing SandboxUsers entities.
 */
interface SandboxUserRepository: IRepository<SandboxUser> {
    /**
     * Retrieves a SandboxUser entity by its user ID.
     *
     * @param id the ID of the user for whom the SandboxUser entity is to be retrieved.
     * @return the SandboxUser entity if found, null otherwise.
     */
    fun getSandboxUserByUserId (id: Int): SandboxUser?
}