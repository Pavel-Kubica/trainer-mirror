package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.service.helper.AuthorizationService
import cz.fit.cvut.wrzecond.trainer.service.helper.FileService
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import cz.fit.cvut.wrzecond.trainer.service.LogService

/**
 * Service class for managing modules.
 *
 * @property repository Repository for module entities.
 * @property authorizationService Service for handling authorization.
 * @property moduleEditorRepository Repository for module editor entities.
 * @property smRepository Repository for student module entities.
 * @property topicRepository Repository for topic entities.
 * @property moduleTopicRepository Repository for module topic entities.
 * @property subjectRepository Repository for subject entities.
 * @property moduleSubjectRepository Repository for module subject entities.
 * @property teacherNoteRepository Repository for teacher note entities.
 * @property studentRatingRepository Repository for student rating entities.
 * @property logService Service for log entities.
 * @property userRepository Repository for user entities.
 */
@Service
class ModuleService(
    override val repository: ModuleRepository,
    private val authorizationService: AuthorizationService,
    private val moduleEditorRepository: ModuleEditorRepository,
    private val smRepository: StudentModuleRepository,
    private val topicRepository: TopicRepository, private val moduleTopicRepository: ModuleTopicRepository,
    private val subjectRepository: SubjectRepository, private val moduleSubjectRepository: ModuleSubjectRepository,
    private val teacherNoteRepository: TeacherNoteRepository, private val studentRatingRepository: StudentRatingRepository,
    private val logService: LogService, val userRepository: UserRepository
) : IServiceImpl<Module, ModuleFindDTO, ModuleGetDTO, ModuleCreateDTO, ModuleUpdateDTO>(repository, userRepository) {

    /**
     * Retrieves all modules that the authenticated user is allowed to view.
     *
     * @param userDto the authenticated user's data transfer object. This parameter is used to
     *                identify and authorize the user attempting to retrieve the data.
     *                If the user is not trusted, an exception is thrown.
     * @return a list of data transfer objects representing the entities that the user is allowed to view.
     *         It filters the entities based on the user's permissions and roles.
     *
     * @throws ResponseStatusException if the user is not authorized to perform this action.
     */
    @Transactional
    override fun findAll(userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        repository.findAll()
            .filter { it.canView(user) }
            .filter {
                // teacher can only see modules without subjects or with subjects they teach
                it.author == user ||
                        it.editors.any { me -> me.editor.id == user.id } ||
                        it.subjects.isEmpty() ||
                        it.subjects.any { sm -> sm.subject.canView(user) }
            }
            .map { converter.toFindDTO(it) }
    }

    /**
     * Retrieves all modules that the authenticated user is allowed to view.
     * Unlike the previous function returned DTOs have less information.
     *
     * @param userDto Data Transfer Object containing the authenticated user's information.
     *                It is used to identify and authorize the user attempting to access the modules.
     *                If the user is not trusted, an exception is thrown.
     * @throws ResponseStatusException with HttpStatus.FORBIDDEN if the user is not authorized to perform this action.
     * @return A list of module data transfer objects that the user is permitted to view.
     */
    fun getAllTable(userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        repository.findAll()
            .filter { it.canView(user) }
            .filter {
                // teacher can only see modules without subjects or with subjects they teach
                it.author == user ||
                        it.editors.any { me -> me.editor.id == user.id } ||
                        it.subjects.isEmpty() ||
                        it.subjects.any { sm -> sm.subject.canView(user) }
            }
            .map { converter.toFindDTOModule(it) }
    }

    /**
     * Retrieves a module by its ID.
     *
     * @param id The unique identifier of the entity to be retrieved.
     * @param userDto An optional parameter containing user authentication details.
     * Used to check access permissions before retrieving the entity.
     * @return Retrieved module as ModuleGetDTO object.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(id, userDto) { module, _ ->
            converter.toGetDTO(module)
        }

    /**
     * Creates a new module with the given details.
     *
     * @param dto Data Transfer Object containing the information needed to create a new module.
     * @param userDto Data Transfer Object containing authentication information for the user initiating the request.
     * @throws ResponseStatusException if the user is not trusted to perform this operation.
     * @return The created module converted to a GetDTO format.
     */
    @Transactional
    override fun create(dto: ModuleCreateDTO, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val modulePersisted = repository.save(converter.toEntity(dto, user))
        logService.log(userDto, modulePersisted, "create")
        moduleEditorRepository.saveAllAndFlush(dto.editors.map {  userId ->
            ModuleEditor(modulePersisted, userRepository.getReferenceById(userId))
        }).map{
            logService.log(userDto, it, "create")
        }
        converter.toGetDTO(modulePersisted)
    }

    /**
     * Updates an existing module with the data provided in the ModuleUpdateDTO.
     *
     * @param id the unique identifier of the module to be updated
     * @param dto the data transfer object containing the updated information
     * @param userDto the data transfer object containing the authenticated user's information, can be null
     * @throws ResponseStatusException if the lastModificationTime in dto is before the module's lastModificationTime
     * @return The updated module converted to a GetDTO format.
     */
    @Transactional
    override fun update(id: Int, dto: ModuleUpdateDTO, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { module, user ->
            // someone changed in the meantime
            if (dto.lastModificationTime < module.lastModificationTime)
                throw ResponseStatusException(HttpStatus.PRECONDITION_FAILED)
            val modulePersisted = repository.save(converter.merge(module, dto))
            logService.log(userDto, modulePersisted, "update")
            if (dto.editors != null && dto.editors != module.editors.map { it.id } && user.id == module.author.id) {
                moduleEditorRepository.deleteAll(module.editors)
                module.editors.map {
                    logService.log(userDto, it, "delete")
                }
                moduleEditorRepository.flush()
                moduleEditorRepository.saveAllAndFlush(dto.editors.map { userId ->
                    ModuleEditor(modulePersisted, userRepository.getReferenceById(userId))
                }).map{
                    logService.log(userDto, it, "update")
                }
            }
            converter.toGetDTO(modulePersisted)
        }

    /**
     * Deletes a module by its specified ID if the user is authorized.
     *
     * @param id The ID of the module to be deleted.
     * @param userDto The data transfer object for user authentication.
     */
    @Transactional
    override fun delete(id: Int, userDto: UserAuthenticateDto?): Unit
        = checkEditAccess(id, userDto) { module, user ->
            if (module.author.username != user.username) // only author can delete
                throw ResponseStatusException(HttpStatus.FORBIDDEN)

            // remove student files
            smRepository.findByModule(module).mapNotNull { it.file }.forEach { file ->
                fileService.deleteFile(file, FileService.UPLOADS_PATH_STUDENTS)
            }

            // remove module file and module
            module.file?.let { file -> fileService.deleteFile(file, FileService.UPLOADS_PATH) }
            repository.delete(module)
            logService.log(userDto, module, "delete")
        }

    /**
     * Get list of module teachers (teacher only)
     * @property id module id
     * @property user teacher retrieving the list
     * @return [CourseUserReadDTO]
     */
    fun getModuleTeachers(id: Int, user: UserAuthenticateDto?)
        = checkEditAccess(id, user) { module, _ ->
            repository.getModuleTeachers(module).map {teacher ->
                CourseUserReadDTO(teacher.id, teacher.username, teacher.name, RoleLevel.TEACHER, null)
            }
        }

    /**
     * Upload module file (teacher only)
     * @property id module id
     * @property file file to be uploaded
     * @property userDto teacher who is uploading file
     * @return ModuleGetDTO
     */
    fun postFile(id: Int, file: MultipartFile, userDto: UserAuthenticateDto?)
        = checkEditAccess(id, userDto) { module, _ ->
            val fileName = "${FileService.generateFileName()}.tar"
            fileService.saveFile(file, fileName, FileService.UPLOADS_PATH)
            val updatedModule = repository.saveAndFlush(module.copy(file = fileName))
            logService.log(userDto, updatedModule, "update")
            converter.toGetDTO(updatedModule)
        }

    /**
     * Get module file
     * @property id module id
     * @property userDto user retrieving the file
     * @return ByteArray containing file information
     */
    fun getFile(id: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(id, userDto) { module, _ ->
            val filePath = module.file ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
            fileService.readFile(filePath, FileService.UPLOADS_PATH)
        }


    /**
     * Get list of modules with at least one of the given topics
     * @property topicIds list of topic ids
     * @property userDto user retrieving the list
     * @return List of [ModuleGetDTO]
     */

    fun findByTopics(topicIds: List<Int>, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        repository.findByTopics(topicIds).map { converter.toGetDTO(it) }
    }

    /**
     * Add a topic to the module (teacher only)
     * @property id module id
     * @property topicId topic id to be added
     * @property userDto teacher adding the topic
     * @return ModuleGetDTO
     */

    fun addTopic(id: Int, topicId: Int, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { module, _ ->
        val topic = topicRepository.getReferenceById(topicId)
        moduleTopicRepository.saveAndFlush(ModuleTopic(module, topic))
        converter.toGetDTO(module)
    }

    /**
     * Remove a topic from the module (teacher only)
     * @property id module id
     * @property topicId topic id to be removed
     * @property userDto teacher removing the topic
     * @return ModuleGetDTO
     */
    @Transactional
    fun removeTopic(id: Int, topicId: Int, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { module, _ ->
        val topic = topicRepository.getReferenceById(topicId)
        moduleTopicRepository.deleteByModuleAndTopic(module, topic)
        null
    }


    /**
     * Retrieves the list of topic IDs associated with a specific module.
     *
     * @param id The unique identifier of the module whose topics are to be retrieved.
     * @param userDto Contains the authenticated user's details for permission checks; may be null.
     * @return A list of topic IDs associated with the module.
     */
    fun getModuleTopics(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { module, _ ->
        module.topics.map {
            it.topic.id
        }
    }

    /**
     * Get list of modules with at least one of the given subjects
     * @property subjectIds list of subject ids
     * @property userDto user retrieving the list
     * @return List of [ModuleGetDTO]
     */
    fun findBySubjects(subjectIds: List<Int>, userDto: UserAuthenticateDto?) = tryCatch {
        val user = getUser(userDto)
        if (!authorizationService.isTrusted(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        repository.findBySubjects(subjectIds).map { converter.toGetDTO(it) }
    }

    /**
     * Add a subject to the module (teacher only)
     * @property id module id
     * @property subjectId subject id to be added
     * @property userDto teacher adding the topic
     * @return ModuleGetDTO
     */
    fun addSubject(id: Int, subjectId: Int, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { module, _ ->
        val subject = subjectRepository.getReferenceById(subjectId)
        moduleSubjectRepository.saveAndFlush(ModuleSubject(module, subject))
        converter.toGetDTO(module)
    }

    /**
     * Remove a subject from the module (teacher only)
     * @property id module id
     * @property subjectId subject id to be removed
     * @property userDto teacher removing the topic
     * @return ModuleGetDTO
     */
    @Transactional
    fun removeSubject(id: Int, subjectId: Int, userDto: UserAuthenticateDto?) = checkEditAccess(id, userDto) { module, _ ->
        val subject = subjectRepository.getReferenceById(subjectId)
        moduleSubjectRepository.deleteByModuleAndSubject(module, subject)
        null
    }

    /**
     * Retrieves the list of subject IDs associated with a specific module.
     *
     * @param id The unique identifier of the module whose subjects are to be retrieved.
     * @param userDto Contains the authenticated user's details for permission checks; may be null.
     * @return A list of subject IDs associated with the module.
     */
    fun getModuleSubjects(id: Int, userDto: UserAuthenticateDto?)
            = checkViewAccess(id, userDto) {module, _ ->
        module.subjects.map {
            it.subject.id
        }
    }

    /**
     * Retrieves a list of module ratings for a specific module, sorted by the published date in descending order.
     *
     * @param id The unique identifier of the module for which ratings are to be retrieved.
     * @param userDto Contains the authenticated user's details for permission checks; may be null.
     * @return A list of StudentRatingFindDTO objects representing the ratings of the specified module.
     */
    fun getModuleRatings(id: Int, userDto: UserAuthenticateDto?)
            = checkViewAccess(id,userDto){_, _ ->
        studentRatingRepository.getByModule(id).sortedByDescending { it.published }.map{converter.toFindDTO(it)}
    }

    /**
     * Retrieves a list of teacher notes for a specific module.
     *
     * @param id The unique identifier of the module for which teacher notes are to be retrieved.
     * @param userDto Contains the authenticated user's details for permission checks; may be null.
     * @return A list of data transfer objects representing the teacher notes for the specified module.
     */
    fun getTeacherNotes(id: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { module, _ ->
        module.notes.map {
            converter.toFindDTO(it)
        }
    }

    /**
     * Retrieves a specific teacher note by its ID for a given module.
     *
     * @param id The unique identifier of the module.
     * @param noteId The unique identifier of the teacher note.
     * @param userDto Contains the authenticated user's details for permission checks; may be null.
     * @return The teacher note data transfer object if access is granted and note is found.
     */
    fun getTeacherNotesById(id: Int, noteId: Int, userDto: UserAuthenticateDto?) = checkViewAccess(id, userDto) { module, _ ->
        teacherNoteRepository.findByIdAndModule(noteId, module).map {
            converter.toFindDTO(it)
        }
    }
}
