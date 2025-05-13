package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.Log
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.LocalDate

/**
 * Generic interface for service
 * contains definition of basic CRUD operations
 */
interface IService<T: IEntity, F: IFindDTO, G: IGetDTO, C: ICreateDTO, U: IUpdateDTO> {

    /** Repository performing CRUD operations on service entity */
    val repository: IRepository<T>

    /**
     * Gets Read DTO of entity with given ID
     * @param id ID of entity
     * @param userDto User DTO of currently logged user, null if unauthenticated
     * @throws ResponseStatusException with code 404 if entity was not found
     * @throws ResponseStatusException with code 401 if unauthenticated
     * @throws ResponseStatusException with code 403 if unauthorized
     * @return Read DTO of entity with given ID
     */
    fun getByID (id: Int, userDto: UserAuthenticateDto?) : G

    /**
     * Find all entities in database
     * @param userDto User DTO of currently logged user, null if unauthenticated
     * @return list of Read DTO reflecting all entities user can view
     */
    fun findAll (userDto: UserAuthenticateDto?) : List<F>

    /**
     * Create entities based on given create DTOs
     * @param dto Create DTO to create entity
     * @param userDto User DTO of currently logged user, null if unauthenticated
     * @throws ResponseStatusException with code 400 if create DTO is invalid
     * @throws ResponseStatusException with code 401 if unauthenticated
     * @throws ResponseStatusException with code 403 if unauthorized
     * @return Read DTO of created entity
     */
    fun create (dto: C, userDto: UserAuthenticateDto?) : G

    /**
     * Update entity based on given update DTO
     * @param id ID of entity being updated
     * @param dto Update DTO to update entity with
     * @param userDto User DTO of currently logged user, null if unauthenticated
     * @throws ResponseStatusException with code 400 if create DTOs are invalid
     * @throws ResponseStatusException with code 404 if entity was not found
     * @throws ResponseStatusException with code 401 if unauthenticated
     * @throws ResponseStatusException with code 403 if unauthorized
     * @return Read DTO of updated entity
     */
    fun update (id: Int, dto: U, userDto: UserAuthenticateDto?) : G

    /**
     * Delete entity with given ID
     * @param id ID of entity to delete
     * @param userDto User DTO of currently logged user, null if unauthenticated
     * @throws ResponseStatusException with code 404 if entity was not found
     * @throws ResponseStatusException with code 401 if unauthenticated
     * @throws ResponseStatusException with code 403 if unauthorized
     */
    fun delete (id: Int, userDto: UserAuthenticateDto?)

}
