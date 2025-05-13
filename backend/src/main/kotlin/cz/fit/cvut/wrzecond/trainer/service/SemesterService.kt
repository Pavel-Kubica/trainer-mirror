package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Semester
import cz.fit.cvut.wrzecond.trainer.repository.CourseRepository
import cz.fit.cvut.wrzecond.trainer.repository.SemesterRepository
import cz.fit.cvut.wrzecond.trainer.repository.SubjectRepository
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.repository.LogRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class for managing Semesters.
 *
 * @property repository the repository handling Semester entities
 * @property courseRepository the repository handling Course entities
 * @property subjectRepository the repository handling Subject entities
 * @property userRepository the repository handling User entities
 * @property logRepository the repository handling Log entries
 */
@Service
class SemesterService(
    override val repository: SemesterRepository,
    private val courseRepository: CourseRepository,
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val logRepository: LogRepository
) :
    IServiceImpl<Semester, SemesterFindDTO, SemesterGetDTO, SemesterCreateDTO, SemesterUpdateDTO>(
        repository,
        userRepository
    ) {

    /**
     * Finds all semesters that the user can view.
     *
     * @param userDto Data transfer object containing user authentication information.
     * @return A list of SemesterFindDTO objects representing the semesters the user is allowed to view.
     */
    override fun findAll(userDto: UserAuthenticateDto?): List<SemesterFindDTO> = tryCatch {
        val user = getUser(userDto)
        repository.findAll(sort)
            .filter { it.canView(user) }
            .mapNotNull { converter.toFindDTO(it) }
    }

    /**
     * Retrieves a semester by its unique identifier.
     *
     * @param id The unique identifier of the resource to retrieve.
     * @param userDto An optional parameter containing user authentication details.
     * @return A fetched semester as DTO.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { semester, user ->
        converter.toGetDTO(semester, user)
    }

    /**
     * Creates a new semester entry in the database.
     *
     * @param dto the SemesterCreateDTO containing the details of the semester to be created.
     * @param userDto the UserAuthenticateDto containing the authenticated user information.
     * @throws ResponseStatusException with HttpStatus.FORBIDDEN if the authenticated user is not an admin.
     * @return the created semester as a DTO.
     */
    override fun create(dto: SemesterCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!user.isAdmin)
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val semester = repository.saveAndFlush(converter.toEntity(dto))
        logRepository.saveAndFlush(createLogEntry(userDto, semester, "create"))
        converter.toGetDTO(semester)
    }

    /**
     * Updates an existing semester based on the provided semester ID and data transfer object.
     *
     * @param id The ID of the semester to update.
     * @param dto The data transfer object containing the updated semester information.
     * @param userDto The authenticated user data transfer object, used to check edit access.
     * @return An updated as a DTO.
     */
    override fun update(id: Int, dto: SemesterUpdateDTO, userDto: UserAuthenticateDto?) =
        checkEditAccess(id, userDto) { semester, _ ->
            val updatedSemester = repository.saveAndFlush(converter.merge(semester, dto))
            logRepository.saveAndFlush(createLogEntry(userDto, updatedSemester, "update"))
            converter.toGetDTO(updatedSemester)
        }

    /**
     * Deletes a semester identified by the specified ID if the user has the appropriate access rights.
     *
     * @param id The ID of the semester to delete.
     * @param userDto The authenticated user details.
     */
    override fun delete(id: Int, userDto: UserAuthenticateDto?): Unit = checkEditAccess(id, userDto) { semester, _ ->
        repository.delete(semester)
        logRepository.saveAndFlush(createLogEntry(userDto, semester, "delete"))
    }

    /**
     * Find all courses in given semester
     * @param semesterId identifier of searched semester
     * @return [SemesterCourseReadDTO]
     */
    fun findSemesterCourses(semesterId: Int, userDto: UserAuthenticateDto?) =
        checkViewAccess(semesterId, userDto) { semester, user ->
            courseRepository.findAll(sort)
                .filter { it.canView(user) && it.semester?.id == semesterId }
                .map { converter.toFindDTO(it, semester, user) }
        }

    /**
     * Find all subjects in given semester
     * @param semesterId identifier of searched semester
     * @return [SubjectFindDTO]
     */
    fun findSemesterSubjects(semesterId: Int, userDto: UserAuthenticateDto?) =
        checkViewAccess(semesterId, userDto) { _, user ->
            subjectRepository.findAll(sort)
                .filter { subject -> subject.canView(user) &&
                        subject.courses.any { it.semester?.id == semesterId } }
                .map { converter.toFindDTO(it) }
        }
}
