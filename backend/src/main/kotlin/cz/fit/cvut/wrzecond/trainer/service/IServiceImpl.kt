package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import cz.fit.cvut.wrzecond.trainer.entity.Log
import cz.fit.cvut.wrzecond.trainer.repository.IRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp

/**
 * Shortcut for defining subclass of both IServiceBase and IService
 * provides base interface with NOT_IMPLEMENTED error
 */
abstract class IServiceImpl<T: IEntity, F: IFindDTO, G: IGetDTO, C: ICreateDTO, U: IUpdateDTO>
    (override val repository: IRepository<T>, userRepository: UserRepository)
    : IServiceBase<T>(repository, userRepository), IService<T, F, G, C, U> {

    override fun findAll(userDto: UserAuthenticateDto?) : List<F>
        = throw ResponseStatusException(HttpStatus.NOT_IMPLEMENTED)
    override fun getByID(id: Int, userDto: UserAuthenticateDto?) : G
        = throw ResponseStatusException(HttpStatus.NOT_IMPLEMENTED)
    override fun create(dto: C, userDto: UserAuthenticateDto?) : G
        = throw ResponseStatusException(HttpStatus.NOT_IMPLEMENTED)
    override fun update(id: Int, dto: U, userDto: UserAuthenticateDto?) : G
        = throw ResponseStatusException(HttpStatus.NOT_IMPLEMENTED)
    override fun delete(id: Int, userDto: UserAuthenticateDto?) : Unit
        = throw ResponseStatusException(HttpStatus.NOT_IMPLEMENTED)

}
