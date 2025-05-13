package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.IEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Generic repository interface
 * extends JpaRepository, which defines all CRUD operations on entity
 */
interface IRepository<T: IEntity> : JpaRepository<T, Int>
