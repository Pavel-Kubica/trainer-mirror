package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ScoringRuleService (
    override val repository: ScoringRuleRepository,
    private val scoringRuleModuleRepository: ScoringRuleModuleRepo,
   // private val scoringRuleStudentModuleRepository: ScoringRuleStudentModuleRepo,
    private val moduleRepository: ModuleRepository,
    private val studentModuleRepository: StudentModuleRepository,
    private val userRepository: UserRepository,
    private val lessonRepository: LessonRepository,
    private val authorizationService: AuthorizationService,
    private val logRepository: LogRepository,
    private val logService: LogService
) :
    IServiceImpl<ScoringRule, ScoringRuleFindDTO, ScoringRuleGetDTO, ScoringRuleCreateDTO, ScoringRuleUpdateDTO>(
        repository,
        userRepository
    ) {

    /**
     * Retrieves all scoring rules that the authenticated user is allowed to view.
     *
     * @param userDto the authenticated user's data transfer object. This parameter is used to
     *                identify and authorize the user attempting to retrieve the data.
     *                If the user is not trusted, an exception is thrown.
     * @return a list of data transfer objects representing the entities that the user is allowed to view.
     *         It filters the entities based on the user's permissions and roles.
     *
     * @throws ResponseStatusException if the user is not authorized to perform this action.
     */
    override fun findAll(userDto: UserAuthenticateDto?): List<ScoringRuleFindDTO> = tryCatch {
        val user = getUser(userDto)
        repository.findAll(sort)
            .mapNotNull { converter.toFindDTO(it,user) }
    }

    /**
     * Retrieves a scoring rule by its ID.
     *
     * @param id The unique identifier of the entity to be retrieved.
     * @param userDto An optional parameter containing user authentication details.
     * Used to check access permissions before retrieving the entity.
     * @return Retrieved scoring rule as ScoringRuleGetDTO object.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { scoringRule, user ->
        val user = getUser(userDto)
        converter.toGetDTO(scoringRule,user)
    }

    /**
     * Creates a new scoring rule with the given details.
     *
     * @param dto Data Transfer Object containing the information needed to create a new scoring rule.
     * @param userDto Data Transfer Object containing authentication information for the user initiating the request.
     * @throws ResponseStatusException if the user is not trusted to perform this operation.
     * @return The created scoring rule converted to a GetDTO format.
     */
    override fun create(dto: ScoringRuleCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        val sr = converter.toEntity(dto)
        println(sr)
        val scoringRule = repository.saveAndFlush(converter.toEntity(dto))

        logService.log(userDto, scoringRule, "create")
        converter.toGetDTO(scoringRule)
    }

    /**
     * Updates an existing scoring rule with the data provided in the ScoringRuleUpdateDTO.
     *
     * @param id the unique identifier of the scoring rule to be updated
     * @param dto the data transfer object containing the updated information
     * @param userDto the data transfer object containing the authenticated user's information, can be null
     * @throws ResponseStatusException if the lastModificationTime in dto is before the scoring rule's lastModificationTime
     * @return The updated scoring rule converted to a GetDTO format.
     */
    override fun update(id: Int, dto: ScoringRuleUpdateDTO, userDto: UserAuthenticateDto?) =
        checkEditAccess(id, userDto) { scoringRule, _ ->
            val updatedRule = repository.saveAndFlush(converter.merge(scoringRule, dto))
            logService.log(userDto, updatedRule, "update")
            converter.toGetDTO(updatedRule)
        }

    /**
     * Deletes a scoring rule by its specified ID if the user is authorized.
     *
     * @param id The ID of the scoring rule to be deleted.
     * @param userDto The data transfer object for user authentication.
     */
    override fun delete(id: Int, userDto: UserAuthenticateDto?): Unit = checkEditAccess(id, userDto) { scoringRule, _ ->
        repository.delete(scoringRule)
        logService.log(userDto, scoringRule, "delete")
    }

    /**
     * Get scoring rules in a lesson
     * @property lessonId lesson id to display the detail for
     * @property userDto teacher's dto
     * @return ScoringRuleGetDTO
     */
    fun getScoringRulesByLesson(lessonId: Int, userDto: UserAuthenticateDto?)
            = checkAccess(lessonId, userDto) { lesson, _ ->
        repository.getScoringRulesByLesson(lesson).map{converter.toGetDTO(it)}
    }

    /**
     * Get the rules form the particular lessons
     * @property lessonIds lessons, for which the rules are searched
     * @property userDto teacher's dto
     * @return ScoringRuleGetDTO
     */
    fun findByLessons(lessonIds: List<Int>, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        repository.findByLessons(lessonIds).map { converter.toGetDTO(it) }
    }


    /**
     * Get student rule for rule user list
     * @property id scoring rule id
     * @property userDto user id
     * @return ScoringRuleUserDTO
     */
    fun getScoringRuleUser(id: Int, userId: Int, userDto: UserAuthenticateDto?) = tryCatch {

        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val scoringRule = repository.getReferenceById(id)

        ScoringRuleUserDTO(converter.toGetDTO(scoringRule), userId, (scoringRule.lesson.students.filter{ st ->
            st.lesson == scoringRule.lesson && st.student.id == userId && st.completedOn!=null
                    && scoringRule.modules.any { srm -> srm.module == st.module }}).size >= (scoringRule.toComplete ?: 0),
            scoringRule.lesson.students.filter { st ->
            st.lesson == scoringRule.lesson && st.student.id == userId
                    && scoringRule.modules.any { srm -> srm.module == st.module }
        }.map { st -> converter.toGetDTO(st) })

    }


    private fun <X> checkAccess(lessonId: Int, userDto: UserAuthenticateDto?,
                                block: (Lesson, User) -> X) = tryCatch {
        val user = getUser(userDto)
        val lesson = lessonRepository.getReferenceById(lessonId)
        if (!lesson.canEdit(user)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        block(lesson, user)
    }

    }
