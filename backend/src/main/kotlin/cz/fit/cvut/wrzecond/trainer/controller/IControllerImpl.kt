package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO
import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.service.IService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Basic controller implementation
 * allows all CRUD methods (when authed)
 */
abstract class IControllerImpl<T: IEntity, F: IFindDTO, G: IGetDTO, C: ICreateDTO, U: IUpdateDTO>
    (override val service: IService<T, F, G, C, U>, userService: UserService)
    : IControllerAuth(userService), IController<T, F, G, C, U> {

    // Helper formatter
    protected val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    // === INTERFACE METHOD IMPLEMENTATION ===

    @GetMapping
    override fun all (request: HttpServletRequest, response: HttpServletResponse,
                      @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, { it.findAll }, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findAll(user) }

    @GetMapping("/{id}")
    override fun getByID (@PathVariable id: Int,
                          request: HttpServletRequest, response: HttpServletResponse,
                          @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, { it.getByID }, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getByID(id, user) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create (@RequestBody dto: C,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, { it.create }, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.create(dto, user) }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    override fun update (@PathVariable id: Int, @RequestBody dto: U,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, { it.update }, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.update(id, dto, user) }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun delete (@PathVariable id: Int,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, { it.delete }, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.delete(id, user) }
    // === INTERFACE METHOD IMPLEMENTATION ===



}
