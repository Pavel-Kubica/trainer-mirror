package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.service.ModuleService
import cz.fit.cvut.wrzecond.trainer.service.TeacherNotesService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * ModuleController is responsible for handling HTTP requests related to modules.
 *
 * @param service Service for handling module-related operations
 * @param userService Service for handling user-related operations.
 * @param teacherNotesService Service for handling teacherNotes-related operations.
 */
@RestController
@Visibility
@RequestMapping("/modules")
class ModuleController(
    override val service: ModuleService,
    userService: UserService,
    private val teacherNotesService: TeacherNotesService
) :
    IControllerImpl<Module, ModuleFindDTO, ModuleGetDTO, ModuleCreateDTO, ModuleUpdateDTO>(service, userService) {

    /**
     * Retrieves the list of teachers associated with a specific module.
     *
     * @param id The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of CourseUserReadDTOs, which represents a list of teachers for the specified module.
     */
    @GetMapping("/{id}/teachers")
    fun getModuleTeachers(@PathVariable id: Int,
                          request: HttpServletRequest, response: HttpServletResponse,
                          @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getModuleTeachers(id, user) }

    /**
     * Retrieves a short version of module details.
     *
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of ModuleFindTableDTOs.
     */
    @GetMapping("/short")
    fun getModuleShort(request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String
    ) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getAllTable(user) }

    /**
     * Retrieves a file from specific module in tar format.
     *
     * @param id The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The login secret cookie used for user authentication.
     * @return ByteArray, which represents a requested file.
     */
    @GetMapping("/{id}/file", produces = ["application/x-tar"])
    fun getModuleFile(@PathVariable id: Int,
                      request: HttpServletRequest, response: HttpServletResponse,
                      @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getFile(id, user) }

    /**
     * Upload a file for a specified module.
     *
     * @param id The ID of the module.
     * @param file The multipart file to be uploaded.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A ModuleGetDTo object.
     */
    @PostMapping("/{id}/file", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postModuleFile(@PathVariable id: Int, @RequestPart("file") file: MultipartFile,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.postFile(id, file, user) }

    /**
     * Retrieves modules based on a list of topic associated with it.
     *
     * @param topicIds The list of topic IDs.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of ModuleGetDTOs.
     */
    @GetMapping("/topics")
    fun getModulesByTopics(@RequestParam topicIds: List<Int>,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findByTopics(topicIds, user) }

    /**
     * Retrieves topics associated with specific module.
     *
     * @param id The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of Ints, which represents a list of IDs of the topics associated with the module.
     */
    @GetMapping("/{id}/topics")
    fun getModuleTopics(@PathVariable id: Int,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getModuleTopics(id, user) }

    /**
     * Adds topic to the module.
     *
     * @param id The ID of the module.
     * @param topicId The ID of the topic.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A ModuleGetDTO object.
     */
    @PostMapping("/{id}/topics/{topicId}")
    fun addModuleTopic(@PathVariable id: Int, @PathVariable topicId: Int,
                       request: HttpServletRequest, response: HttpServletResponse,
                       @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.addTopic(id, topicId, user) }

    /**
     * Removes topic from the module.
     *
     * @param id The ID of the module.
     * @param topicId The ID of the topic.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{id}/topics/{topicId}")
    fun removeModuleTopic(@PathVariable id: Int, @PathVariable topicId: Int,
                          request: HttpServletRequest, response: HttpServletResponse,
                          @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.removeTopic(id, topicId, user) }

    /**
     * Retrieves modules based on a list of subjects associated with it.
     *
     * @param subjectIds The list of subjects IDs.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of ModuleGetDTOs.
     */
    @GetMapping("/subjects")
    fun getModulesBySubjects(@RequestParam subjectIds: List<Int>,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.findBySubjects(subjectIds, user) }

    /**
     * Retrieves subjects associated with specific module.
     *
     * @param id The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A list of Ints, which represents a list of IDs of the subjects associated with the module.
     */
    @GetMapping("/{id}/subjects")
    fun getModuleSubjects(@PathVariable id: Int,
                          request: HttpServletRequest, response: HttpServletResponse,
                          @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getModuleSubjects(id, user) }

    /**
     * Adds subject to the module.
     *
     * @param id The ID of the module.
     * @param subjectId The ID of the subject.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A ModuleGetDTO object.
     */
    @PostMapping("/{id}/subjects/{subjectId}")
    fun addModuleSubject(@PathVariable id: Int, @PathVariable subjectId: Int,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.addSubject(id, subjectId, user) }

    /**
     * Removes subject from the module.
     *
     * @param id The ID of the module.
     * @param subjectId The ID of the subject.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/{id}/subjects/{subjectId}")
    fun removeModuleSubject(@PathVariable id: Int, @PathVariable subjectId: Int,
                            request: HttpServletRequest, response: HttpServletResponse,
                            @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.removeSubject(id, subjectId, user) }

    /**
     * Retrieves ratings for a specified module.
     *
     * @param id The ID of the module.
     * @param request The HttpServletRequest object that contains the request made by the client.
     * @param response The HttpServletResponse object that contains the response sent by the server.
     * @param loginSecret The value of the loginSecret cookie, which is used for authentication.
     * @return A list of StudentRatingFindDTOs.
     */
    @GetMapping("/{id}/ratings")
    fun getModuleRatings(@PathVariable id: Int,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret){ user ->
                setCookie(loginSecret, response)
                service.getModuleRatings(id, user)}

    /**
     * Retrieves teacher notes for a specified module.
     *
     * @param id The ID of the module.
     * @param request The HttpServletRequest object that contains the request made by the client.
     * @param response The HttpServletResponse object that contains the response sent by the server.
     * @param loginSecret The value of the loginSecret cookie, which is used for authentication.
     * @return A list of TeacherNoteFindDTO.
     */
    @GetMapping("/{id}/teachersNotes")
    fun getModuleTeachersNotes(@PathVariable id: Int,
                               request: HttpServletRequest, response: HttpServletResponse,
                               @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getTeacherNotes(id, user) }

    /**
     * Retrieves a specific teacher note for a specified module.
     *
     * @param id The ID of the module.
     * @param note_id The ID of the note.
     * @param request The HttpServletRequest object that contains the request made by the client.
     * @param response The HttpServletResponse object that contains the response sent by the server.
     * @param loginSecret The value of the loginSecret cookie, which is used for authentication.
     * @return A list of TeacherNoteFindDTO.
     */
    @GetMapping("/{id}/teachersNotes/{note_id}")
    fun getModuleTeachersNotes(@PathVariable id: Int, @PathVariable note_id: Int,
                               request: HttpServletRequest, response: HttpServletResponse,
                               @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getTeacherNotesById(id, note_id, user) }

    /**
     * Removes a specific teacher note for a specified module.
     *
     * @param id The ID of the module.
     * @param note_id The ID of the note.
     * @param request The HttpServletRequest object that contains the request made by the client.
     * @param response The HttpServletResponse object that contains the response sent by the server.
     * @param loginSecret The value of the loginSecret cookie, which is used for authentication.
     */
    @DeleteMapping("/{id}/teachersNotes/{note_id}")
    fun removeTeacherNotes(@PathVariable id: Int, @PathVariable note_id: Int,
                           request: HttpServletRequest, response: HttpServletResponse,
                           @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            teacherNotesService.delete(note_id, user) }

    /**
     * Adds a teacher note for a specified module.
     *
     * @param id The ID of the module.
     * @param note The DTO containing new note information.
     * @param request The HttpServletRequest object that contains the request made by the client.
     * @param response The HttpServletResponse object that contains the response sent by the server.
     * @param loginSecret The value of the loginSecret cookie, which is used for authentication.
     * @return A TeacherNoteFindDTO object.
     */
    @PostMapping("/{id}/teachersNotes")
    fun addTeacherNotes(@PathVariable id: Int, @RequestBody note: TeacherNoteCreateDTO,
                        request: HttpServletRequest, response: HttpServletResponse,
                        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            teacherNotesService.create(id, note, user) }

    /**
     * Updates a teacher note for a specified module.
     *
     * @param id The ID of the module.
     * @param note_id The ID of the note.
     * @param note The DTO containing updated note information.
     * @param request The HttpServletRequest object that contains the request made by the client.
     * @param response The HttpServletResponse object that contains the response sent by the server.
     * @param loginSecret The value of the loginSecret cookie, which is used for authentication.
     * @return A TeacherNoteFindDTO object.
     */
    @PatchMapping("/{id}/teachersNotes/{note_id}")
    fun updateTeacherNotes(
        @PathVariable id: Int,
        @PathVariable note_id: Int,
        @RequestBody note: TeacherNoteUpdateDTO,
        request: HttpServletRequest, response: HttpServletResponse,
        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String
    ) =
        authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            teacherNotesService.update(note_id, note, user) }
}
