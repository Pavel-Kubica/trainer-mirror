package cz.fit.cvut.wrzecond.trainer.controller.gitlab

import cz.fit.cvut.wrzecond.trainer.controller.IControllerImpl
import cz.fit.cvut.wrzecond.trainer.controller.Visibility
import cz.fit.cvut.wrzecond.trainer.controller.VisibilitySettings
import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleUpdateDTO
import cz.fit.cvut.wrzecond.trainer.dto.gitlab.GitlabFileDTO
import cz.fit.cvut.wrzecond.trainer.dto.gitlab.GitlabProjectDTO
import cz.fit.cvut.wrzecond.trainer.dto.gitlab.GitlabRawFileDTO
import cz.fit.cvut.wrzecond.trainer.dto.gitlab.PaginatedResponseGitlabDTO
import cz.fit.cvut.wrzecond.trainer.entity.code.CodeModule
import cz.fit.cvut.wrzecond.trainer.service.UserService
import cz.fit.cvut.wrzecond.trainer.service.code.CodeModuleService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@RestController
@Visibility(getByID = VisibilitySettings.LOGGED)
@RequestMapping("/gitlab")
class GitlabImportController(override val service: CodeModuleService, val userService: UserService) :
    IControllerImpl<CodeModule, CodeModuleFindDTO, CodeModuleGetDTO, CodeModuleCreateDTO, CodeModuleUpdateDTO>(
        service,
        userService
    ) {

    val gitlabUri = "https://gitlab.fit.cvut.cz/api/v4"
    val restTemplate = RestTemplate()

    @GetMapping("/projects")
    fun findAllProjects(
        @RequestHeader token: String, request: HttpServletRequest, response: HttpServletResponse,
        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String,
        @RequestParam(required = false, defaultValue = "1") page: Int?,
        @RequestParam(required = false, defaultValue = "") search: String,
        @RequestParam(required = false, defaultValue = "false") owned: Boolean,
    ): PaginatedResponseGitlabDTO<GitlabProjectDTO> = authenticate(request, VisibilitySettings.LOGGED, loginSecret) {
        setCookie(loginSecret, response)
        val uri = URI.create("$gitlabUri/projects?per_page=20&page=$page&search=$search&owned=$owned")
        val headers = HttpHeaders()
        headers.add("PRIVATE-TOKEN", token)
        val entityHeaders = HttpEntity<String>(headers)
        val responseProjects = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entityHeaders,
            object : ParameterizedTypeReference<List<GitlabProjectDTO>>() {})
        val nextPage = responseProjects.headers.getFirst("X-Next-Page")
            ?: responseProjects.headers.getFirst("X-Page")
        val result = PaginatedResponseGitlabDTO(nextPage ?: "", responseProjects.body ?: emptyList())
        result
    }

    @GetMapping("/projects/{id}/files")
    fun findAllProjectFiles(
        @RequestHeader token: String, request: HttpServletRequest, response: HttpServletResponse,
        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String,
        @PathVariable id: Int, @RequestParam(required = false, defaultValue = "1") page: Int?,
        @RequestParam(required = false, defaultValue = "") search: String,
        @RequestParam(required = false, defaultValue = "") path: String,
    ): PaginatedResponseGitlabDTO<GitlabFileDTO> = authenticate(request, VisibilitySettings.LOGGED, loginSecret) {
        setCookie(loginSecret, response)
        val uri = URI.create("$gitlabUri/projects/$id/repository/tree?per_page=20&page=$page&search=$search&path=$path")
        val headers = HttpHeaders()
        headers.add("PRIVATE-TOKEN", token)
        val entityHeaders = HttpEntity<String>(headers)
        val responseProjects = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entityHeaders,
            object : ParameterizedTypeReference<List<GitlabFileDTO>>() {})
        val nextPage = responseProjects.headers.getFirst("X-Next-Page")
            ?: responseProjects.headers.getFirst("X-Page")
        val result = PaginatedResponseGitlabDTO(nextPage ?: "", responseProjects.body ?: emptyList())
        result
    }

    @GetMapping("/projects/{id}/files/raw")
    fun findRawFile(
        @RequestHeader token: String, request: HttpServletRequest, response: HttpServletResponse,
        @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String,
        @PathVariable id: Int, @RequestHeader filePath: String
    ): GitlabRawFileDTO = authenticate(request, VisibilitySettings.LOGGED, loginSecret) {
        setCookie(loginSecret, response)
        val encodedPath = java.net.URLEncoder.encode(filePath, "UTF-8")
        val uri = URI.create("$gitlabUri/projects/$id/repository/files/$encodedPath/raw")
        val headers = HttpHeaders()
        headers.add("PRIVATE-TOKEN", token)
        val entityHeaders = HttpEntity<String>(headers)
        val responseProjects = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entityHeaders,
            object : ParameterizedTypeReference<String>() {})
            GitlabRawFileDTO(responseProjects.body.toString() ?: "")
    }
}