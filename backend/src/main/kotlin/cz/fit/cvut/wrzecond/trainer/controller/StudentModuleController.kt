package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.StudentModuleEditDTO
import cz.fit.cvut.wrzecond.trainer.dto.StudentModuleRequestEditDTO
import cz.fit.cvut.wrzecond.trainer.dto.StudentModuleRequestTeacherDTO
import cz.fit.cvut.wrzecond.trainer.dto.UserFindDTO
import cz.fit.cvut.wrzecond.trainer.service.StudentModuleService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * StudentModuleController is responsible for handling HTTP requests related to student modules.
 *
 * @param service Service for handling studentModule-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@RequestMapping("/lessons/{lessonId}/modules/{moduleId}")
class StudentModuleController(private val service: StudentModuleService, userService: UserService)
: IControllerAuth(userService) {

    /**
     * Adds student module based on lesson and module for current user.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A studentModuleReadDTO object.
     */
    @PutMapping("/data/me")
    fun putStudentModule(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                         @RequestBody dto: StudentModuleEditDTO,
                         request: HttpServletRequest, response: HttpServletResponse,
                         @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.putStudentModule(lessonId, moduleId, dto, user)
        }

    /**
     * Removes student module based on lesson and module for current user.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/data/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStudentModule(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                            request: HttpServletRequest, response: HttpServletResponse,
                            @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.deleteUserModule(lessonId, moduleId, user) }

    /**
     * Retrieves student module based on lesson and module for current user.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return An optional studentModuleGetDTO object.
     */
    @GetMapping("users/{userId}/all")
    fun getByUserModuleLesson(@PathVariable lessonId: Int, @PathVariable moduleId: Int, @PathVariable userId: Int, request: HttpServletRequest,
                              response: HttpServletResponse, @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.getByUserModuleLesson(lessonId, moduleId, userId, user)
    }


    /**
     * Uploads a student module file associated with the specified lesson and module for the current user.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param file The multipart file object to be uploaded.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A studentModuleReadDTO object.
     */
    @PostMapping("/file/me", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postStudentModuleFile(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                              @RequestPart("file") file: MultipartFile,
                              request: HttpServletRequest, response: HttpServletResponse,
                              @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.putStudentModuleFile(lessonId, moduleId, file, user)
    }

    /**
     * Retrieves a student module file associated with the specific lesson and module for the current user.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return ByteArray which represents a retrieved file.
     */
    @GetMapping("/file/me", produces = ["application/x-tar"])
    fun getStudentModuleFile(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
            = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.getStudentModuleFile(lessonId, moduleId, user)
    }

    /**
     * Retrieves a student module file associated with the specified lesson, module, and user.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param userId The ID of the user.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return ByteArray which represents a retrieved file.
     */
    @GetMapping("/file/users/{userId}", produces = ["application/x-tar"])
    fun getStudentModuleFile(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                             @PathVariable userId: Int,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.getStudentModuleFile(lessonId, moduleId, userId, user)
    }

    /**
     * Retrieves a student module request associated with the specific lesson and module for the current user.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A StudentRequestDTO object.
     */
    @GetMapping("/requests/me")
    fun getStudentModuleRequest(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                                request: HttpServletRequest, response: HttpServletResponse,
                                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getStudentModuleRequest(lessonId, moduleId, user)
        }

    /**
     * Retrieves a student module request associated with the specific lesson, module and student.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param userId The ID of the student.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A StudentRequestDTO object.
     */
    @GetMapping("/requests/users/{userId}")
    fun getStudentModuleRequest(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                                @PathVariable userId: Int,
                                request: HttpServletRequest, response: HttpServletResponse,
                                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.getStudentModuleRequest(lessonId, moduleId, userId, user)
        }

    /**
     * Updates an existing student module request associated with the specific lesson and module.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param dto DTO containing the update details.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A studentModuleReadDTO object.
     */
    @PutMapping("/requests/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun putStudentModuleRequest(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                                @RequestBody dto: StudentModuleRequestEditDTO,
                                request: HttpServletRequest, response: HttpServletResponse,
                                @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
            setCookie(loginSecret, response)
            service.putStudentModuleRequest(lessonId, moduleId, dto, user)
        }

    /**
     * Deletes an existing student module request associated with the specific lesson and module.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/requests/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStudentModuleRequest(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                                   request: HttpServletRequest, response: HttpServletResponse,
                                   @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) {user ->
            setCookie(loginSecret, response)
            service.deleteStudentModuleRequest(lessonId, moduleId, user)
        }

    /**
     * Updates student module request data from a teacher's perspective.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param userId The ID of the student whose module will be updated.
     * @param dto DTO containing the update details.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A studentModuleReadDTO object.
     */
    @PutMapping("/requests/users/{userId}")
    fun putStudentModuleData(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                             @PathVariable userId: Int,
                             @RequestBody dto: StudentModuleRequestTeacherDTO,
                             request: HttpServletRequest, response: HttpServletResponse,
                             @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.putStudentModuleRequestTeacher(lessonId, moduleId, userId, dto, user)
    }

    /**
     * Deletes the answer to a student request made by a teacher.
     *
     * @param lessonId The ID of the lesson.
     * @param moduleId The ID of the module.
     * @param userId the ID of the student whose request answer needs to be deleted.
     * @param request The HTTP request object that contains request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @DeleteMapping("/requests/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStudentRequestAnswer(@PathVariable lessonId: Int, @PathVariable moduleId: Int,
                                   @PathVariable userId: Int,
                                   request: HttpServletRequest, response: HttpServletResponse,
                                   @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
        = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.deleteStudentRequestAnswerTeacher(lessonId, moduleId, userId, user)
    }

}
