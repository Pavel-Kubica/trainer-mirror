package cz.fit.cvut.wrzecond.trainer.dto.gitlab

data class GitlabProjectDTO (val id: Int, val name: String, val description: String?)

data class GitlabFileDTO (val id: String, val name: String, val path: String, val type: String)

data class PaginatedResponseGitlabDTO<T>(val nextPage: String, val data: List<T>)

data class GitlabRawFileDTO(val content: String)