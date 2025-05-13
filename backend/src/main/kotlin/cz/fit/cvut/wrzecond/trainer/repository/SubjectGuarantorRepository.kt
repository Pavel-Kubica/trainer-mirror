package cz.fit.cvut.wrzecond.trainer.repository

import cz.fit.cvut.wrzecond.trainer.entity.Subject
import cz.fit.cvut.wrzecond.trainer.entity.SubjectGuarantor
import cz.fit.cvut.wrzecond.trainer.entity.User
import org.springframework.data.jdbc.repository.query.Query
import java.util.*

/**
 * Repository interface for managing SubjectGuarantors entities.
 */
interface SubjectGuarantorRepository: IRepository<SubjectGuarantor> {
    fun getSubjectGuarantorsBySubjectAndGuarantor(subject: Subject, guarantor: User) : Optional<SubjectGuarantor>

    /**
     * Retrieves a list of SubjectGuarantor entities associated with the given subject.
     *
     * @param subject The subject for which the associated SubjectGuarantor entities are to be found.
     * @return A list of SubjectGuarantor entities associated with the given subject.
     */
    @Query("SELECT sg FROM SubjectGuarantor sg WHERE sg.subject = :subject")
    fun findBySubject (subject: Subject) : List<SubjectGuarantor>

    fun findByGuarantor (guarantor: User) : List<SubjectGuarantor>

}