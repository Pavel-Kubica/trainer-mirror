package cz.fit.cvut.wrzecond.trainer.entity

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["subject_id", "guarantor_id"])])
data class SubjectGuarantor(
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "subject_id", nullable = false)
    val subject: Subject,

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "guarantor_id", nullable = false)
    val guarantor: User,

    override val id: Int = 0
) : IEntity(id) {

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(  subject = $subject   ,   guarantor = $guarantor   ,   id = $id )"
    }
}
