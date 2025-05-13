package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository

abstract class IServiceImplOld<T: IEntity, F: IFindDTO, G: IGetDTO, C: ICreateDTO, U: IUpdateDTO>
    (override val repository: IRepository<T>, userRepository: UserRepository)
    : IServiceBase<T>(repository, userRepository), IService<T, F, G, C, U> {

    // === Default implementation of interface methods ===

    // Get
    override fun getByID (id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(getEntityByID(id), userDto) { entity, _ -> entity.toGetDTO() }
    override fun findAll (userDto: UserAuthenticateDto?) = tryCatch {
        val userEntity = try { getUser(userDto) } catch (_: Exception) { null }
        repository.findAll(sort).filter { it.canView(userEntity) }.map { it.toFindDTO() }
    }

    // Create, Update, Delete
    override fun create (dto: C, userDto: UserAuthenticateDto?) = tryCatch {
        checkEditAccess(dto.toEntity(), userDto) { entity, _ -> repository.saveAndFlush(entity).toGetDTO() }
    }
    override fun update (id: Int, dto: U, userDto: UserAuthenticateDto?) = checkEditAccess(getEntityByID(id), userDto) { entity, _ ->
        checkEditAccess(entity.merge(dto), userDto) { ent, _ -> repository.saveAndFlush(ent).toGetDTO() }
    }
    override fun delete (id: Int, userDto: UserAuthenticateDto?) = checkEditAccess(getEntityByID(id), userDto) { entity, _ -> repository.delete(entity) }

    // === Default implementation of interface methods ===

    /**
     * Extension function allowing for mapping Entity to GetDTO
     * @return GetDTO reflecting given entity (with nested DTOs)
     */
    abstract fun T.toGetDTO() : G

    /**
     * Extension function allowing for mapping Entity to FindDTO
     * @return FindDTO reflecting given entity
     */
    abstract fun T.toFindDTO() : F

    /**
     * Extension function allowing for mapping Create DTO to Entity
     * @return Entity containing all data from given Create DTO
     */
    abstract fun C.toEntity () : T

    /**
     * Extension function allowing for updating Entity with Update DTO
     * For each field of Update DTO, if it is NULL, it should not be updated
     * @param dto Update DTO
     * @return Entity updated with all data from given Update DTO
     */
    abstract fun T.merge(dto: U) : T

}
