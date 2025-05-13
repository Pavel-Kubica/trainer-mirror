package cz.fit.cvut.wrzecond.trainer.service.specification

import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.entity.User_
import org.springframework.data.jpa.domain.Specification

/**
 * Class for user's specifications which are used in filtering
 */
class UserSpecification {
    companion object {
        /**
         * Specification to filter users whose name is like @param name
         */
        fun userNameLike(name: String?): Specification<User> {
            return Specification { root, _, criteriaBuilder ->
                name?.let {
                    criteriaBuilder.like(root.get(User_.name), "%" + name + "%")
                }
            }
        }
    }
}