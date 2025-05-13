package cz.fit.cvut.wrzecond.trainer.dto.quiz

import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO

/**
 * Data Transfer Object representing a student within a quiz room.
 *
 * @property id Unique identifier of the entity.
 * @property student The username of the student.
 * @property quizroom The unique identifier of the quiz room.
 * @property points The points the student has earned in the quiz room.
 */
//-> out
data class QuizroomStudentFindDTO(override val id: Int, val student : String, val quizroom : Int, val points : Int) :
    IFindDTO
/**
 * Data Transfer Object for retrieving QuizroomStudent details.
 *
 * @property id Unique identifier of the QuizroomStudent entity.
 * @property student Unique identifier of the student who is part of the Quizroom.
 * @property quizroom Unique identifier of the quizroom the student is associated with.
 * @property points Points accumulated by the student in the quizroom.
 */
//-> out
data class QuizroomStudentGetDTO(override val id: Int, val student : Int, val quizroom : Int, val points : Int) :
    IGetDTO


/**
 * Data Transfer Object for updating a student's details in a quiz room.
 *
 * @property student The ID of the student. Nullable for cases where the student ID is not being updated.
 * @property quizroom The ID of the quiz room. Nullable for cases where the quiz room ID is not being updated.
 * @property points The points awarded to the student. Nullable for cases where the points are not being updated.
 */
//<- in
data class QuizroomStudentUpdateDTO (val student : Int?, val quizroom : Int?, val points : Int?) : IUpdateDTO
/**
 * Data Transfer Object for creating a QuizroomStudent entity.
 *
 * @param student The ID of the student being added to the quiz room.
 * @param quizroom The ID of the quiz room where the student is being added.
 */
//<- in
data class QuizroomStudentCreateDTO (val student : Int, val quizroom : Int) : ICreateDTO
