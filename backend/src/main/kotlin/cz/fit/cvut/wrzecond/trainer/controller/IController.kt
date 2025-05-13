package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO
import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.service.IService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * Generic controller interface
 * automatically creates CRUD methods and maps them
 */
interface IController<T: IEntity, F: IFindDTO, G: IGetDTO, C: ICreateDTO, U: IUpdateDTO> {

    /** Service performing all logic on given DTOs */
    val service: IService<T, F, G, C, U>

    /**
     * Find all entities
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return list of FindDTO reflecting all entities
     */
    @GetMapping
    fun all (request: HttpServletRequest, response: HttpServletResponse,
             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) : List<F>

    /**
     * Gets ReadDTO of entity with given ID
     * @param id ID of entity
     * @param request HTTP request, could be used for authentication
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @throws ResponseStatusException with code 404 if entity was not found
     * @return Get DTO of entity with given ID
     */
    @GetMapping("/{id}")
    fun getByID (@PathVariable id: Int,
                 request: HttpServletRequest, response: HttpServletResponse,
                 @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) : G

    /**
     * Create entity based on given create DTO
     * @param dto Create DTOs to create entity
     * @param request HTTP request, could be used for authentication
     * @param response HTTP response. On success, 'Location' header with created entity ID is added
     * @param loginSecret The cookie value used for user authentication.
     * @throws ResponseStatusException with code 400 if create DTO is invalid
     * @return Read DTO of created entity
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create (@RequestBody dto: C,
                request: HttpServletRequest, response: HttpServletResponse,
                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) : G

    /**
     * Update entity based on given update DTO
     * @param id ID of entity being updated
     * @param dto Update DTO to update entity with
     * @param request HTTP request, could be used for authentication
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @throws ResponseStatusException with code 400 if create DTOs are invalid
     * @throws ResponseStatusException with code 404 if entity was not found
     * @return Read DTO of updated entity
     */
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun update (@PathVariable id: Int, @RequestBody dto: U,
                request: HttpServletRequest, response: HttpServletResponse,
                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) : G

    /**
     * Delete entity with given ID
     * @param id ID of entity to delete
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @throws ResponseStatusException with code 404 if entity was not found
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete (@PathVariable id: Int,
                request: HttpServletRequest, response: HttpServletResponse,
                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)

}
