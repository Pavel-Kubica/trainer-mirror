package cz.fit.cvut.wrzecond.trainer.service.quiz

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizroomStudentCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizroomStudentFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizroomStudentGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.quiz.QuizroomStudentUpdateDTO
import cz.fit.cvut.wrzecond.trainer.entity.quiz.QuizroomStudent
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomRepo
import cz.fit.cvut.wrzecond.trainer.repository.quiz.QuizroomStudentRepo
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.service.IServiceBase
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class for managing QuizroomStudent entities and their interactions with quiz rooms and users.
 *
 * @property repository The repository handling QuizroomStudent entities.
 * @property quizroomRepo The repository handling Quizroom entities.
 * @property userRepo The repository handling User entities.
 */
@Service
class QuizroomStudentService (override val repository: QuizroomStudentRepo, private val quizroomRepo: QuizroomRepo,
                              private val userRepo: UserRepository)
    : IServiceBase<QuizroomStudent> (repository,userRepo) {


    /**
     * Retrieves all students associated with a specific quiz room, based on the given quiz room ID,
     * and filters them based on the access rights of the provided authenticated user.
     *
     * @param quizroomId The ID of the quiz room for which students are to be retrieved.
     * @param userDto An optional DTO containing authentication information about the user making the request.
     * @return A list of DTOs representing students in the specified quiz room if found, filtered by the user's access rights.
     * @throws ResponseStatusException if the quiz room is not found or if the user is not authorized to view the students.
     */
    fun findAllQuizroomStudents(quizroomId: Int, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if(!quizroomRepo.existsById(quizroomId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val quizroom = quizroomRepo.findById(quizroomId).get()
        if(!quizroom.canView(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        repository.findAll().filter { it.quizroom.id == quizroomId }.filter { it.canView(user) }.map { it.toFindDto() }
    }

    /**
     * Retrieves a specific student associated with a specified quiz room.
     *
     * @param quizroomId The ID of the quiz room.
     * @param studentId The ID of the student.
     * @param userDto An optional DTO containing authentication information about the user making the request.
     * @return A QuizroomStudentGetDTO object.
     * @throws ResponseStatusException if either the quiz room or the student is not found,
     *         if they are not associated, or if the user is not authorized to view the student.
     */
    fun getQuizroomStudent(quizroomId: Int, studentId: Int, userDto: UserAuthenticateDto?) = tryCatch{
        val user = getUser(userDto)
        if(!quizroomRepo.existsById(quizroomId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if(!userRepo.existsById(studentId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val quizroom = quizroomRepo.findById(quizroomId).get()
        val student = userRepo.findById(studentId).get()

        if(!repository.existsByQuizroomAndStudent(quizroom,student))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if(!quizroom.canView(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        repository.findByQuizroomAndStudent(quizroom, student).toGetDTO()

    }

    /**
     * Creates a new association between a student and a quiz room.
     *
     * @param quizroomId The ID of the quiz room where the student is to be added.
     * @param dto The data transfer object containing details of the student and quiz room.
     * @param userDto An optional DTO containing authentication information about the user making the request.
     * @return A QuizroomStudentGetDTO object.
     * @throws ResponseStatusException if either the quiz room or the student is not found.
     */
    @Transactional
    fun createQuizroomStudent(quizroomId: Int, dto: QuizroomStudentCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
       // val user = getUser(userDto)
        if(!quizroomRepo.existsById(quizroomId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if(!userRepo.existsById(dto.student))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val quizroom = quizroomRepo.findById(quizroomId).get()
        val student = userRepo.findById(dto.student).get()

        repository.saveAndFlush(QuizroomStudent(student,quizroom,0)).toGetDTO()

    }

    /**
     * Updates the details of a student associated with a specific quiz room.
     *
     * @param quizroomId The ID of the quiz room.
     * @param studentId The ID of the student.
     * @param dto Data Transfer Object containing the updated details for the student in the quiz room.
     * @param userDto An optional DTO containing authentication information about the user making the request.
     * @return A QuizroomStudentGetDTO object.
     * @throws ResponseStatusException if either the quiz room or the student is not found, or if they are not associated.
     */
    fun updateQuizroomStudent(quizroomId: Int, studentId: Int, dto : QuizroomStudentUpdateDTO, userDto: UserAuthenticateDto?) = tryCatch{
        val user = getUser(userDto)

        if(!quizroomRepo.existsById(quizroomId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if(!userRepo.existsById(studentId))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val quizroom = quizroomRepo.findById(quizroomId).get()
        val student = userRepo.findById(studentId).get()

        if(!repository.existsByQuizroomAndStudent(quizroom, student))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val studInRoom = repository.findByQuizroomAndStudent(quizroom, student)
        repository.saveAndFlush(studInRoom.merge(dto)).toGetDTO()
    }

    /**
     * Converts a `QuizroomStudent` entity to a `QuizroomStudentFindDTO` object.
     *
     * @return A `QuizroomStudentFindDTO` containing the ID, student's username, quiz room ID, and points of the `QuizroomStudent`.
     */
    private fun QuizroomStudent.toFindDto() = QuizroomStudentFindDTO(id,student.username,quizroom.id,points)

    /**
     * Converts a `QuizroomStudent` entity to a `QuizroomStudentGetDTO` object.
     *
     * @return A `QuizroomStudentGetDTO` containing the ID, student's ID, quiz room ID, and points of the `QuizroomStudent`.
     */
    private fun QuizroomStudent.toGetDTO() = QuizroomStudentGetDTO(id, student.id, quizroom.id, points)

    /**
     * Converts a `QuizroomStudentCreateDTO` to a `QuizroomStudent` entity.
     *
     * Throws `ResponseStatusException`:
     * - If the student ID does not exist in `userRepo`.
     * - If the quizroom ID does not exist in `quizroomRepo`.
     *
     * @return A new `QuizroomStudent` entity with the associated `student`, `quizroom` and initial points set to 0.
     */
    private fun QuizroomStudentCreateDTO.toEntity(): QuizroomStudent {
        if(!userRepo.existsById(student))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if(!quizroomRepo.existsById(quizroom))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return QuizroomStudent(userRepo.findById(student).get(), quizroomRepo.findById(quizroom).get(), 0)
    }

    /**
     * Merges the current `QuizroomStudent` instance with the provided `QuizroomStudentUpdateDTO`.
     *
     * This function updates the current `QuizroomStudent` with the details provided in the `QuizroomStudentUpdateDTO`.
     * It performs validation checks to ensure that the student and quiz room IDs provided in the DTO exist in the system.
     * If valid, the function creates and returns a new `QuizroomStudent` instance with the updated information.
     *
     * @param dto The `QuizroomStudentUpdateDTO` containing the new details for the student in the quiz room.
     * @return A new `QuizroomStudent` instance with the updated information.
     * @throws ResponseStatusException if the student or quiz room ID in the DTO does not exist.
     */
    private fun QuizroomStudent.merge(dto: QuizroomStudentUpdateDTO): QuizroomStudent {
        if (dto.student != null && !userRepo.existsById(dto.student))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if (dto.quizroom != null && !quizroomRepo.existsById(dto.quizroom))
            throw ResponseStatusException(HttpStatus.NOT_FOUND)

        if (dto.student != null) {
            if (dto.quizroom != null) {
                return QuizroomStudent(
                    userRepo.findById(dto.student).get(),
                    quizroomRepo.findById(dto.quizroom).get(),
                    dto.points ?: points,
                    id
                )
            }
            return QuizroomStudent(userRepo.findById(dto.student).get(), quizroom, dto.points ?: points, id)
        } else {
            if (dto.quizroom != null) {
                return QuizroomStudent(student, quizroomRepo.findById(dto.quizroom).get(), dto.points ?: points, id)
            }
            return QuizroomStudent(student, quizroom, dto.points ?: points, id)
        }
    }


}