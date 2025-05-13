package cz.fit.cvut.wrzecond.trainer.service.helper

import cz.fit.cvut.wrzecond.trainer.entity.Course
import cz.fit.cvut.wrzecond.trainer.entity.RoleLevel
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.SubjectGuarantorRepository
import org.springframework.stereotype.Service

/**
 * Service class for handling authorization logic within the application.
 * This service provides methods to determine if a user has trusted roles,
 * such as an admin, teacher, or guarantor.
 *
 * @param subjectGuarantorRepository Repository interface for managing SubjectGuarantors entities.
 */
@Service
class AuthorizationService (
    private val subjectGuarantorRepository: SubjectGuarantorRepository
) {

    /**
     * Function to check if user is trusted, that is, if he is admin, teacher or guarantor.
     * @property user user to be checked
     * @return true if user is trusted, false otherwise
     */
    fun isTrusted(user: User) : Boolean {
        val isAdmin = user.isAdmin
        val isTeacher = user.courses.any { it.role.level == RoleLevel.TEACHER }
        val isGuarantor = subjectGuarantorRepository.findAll().any { it.guarantor.id == user.id }
        return isAdmin || isTeacher || isGuarantor
    }

    /**
     * Function to check if user is trusted in a course,
     * that is, if he is admin, teacher of the course or guarantor of the course's subject.
     * @property user user to be checked
     * @property course course to be checked
     * @return true if user is trusted, false otherwise
     */
    fun isTrusted(user: User, course: Course) : Boolean {
        val isAdmin = user.isAdmin
        val isTeacher = course.users.any { it.role.level == RoleLevel.TEACHER && it.user.id == user.id }
        val isGuarantor = course.subject?.guarantors?.any { it.guarantor.id == user.id } ?: false
        return isAdmin || isTeacher || isGuarantor
    }

}