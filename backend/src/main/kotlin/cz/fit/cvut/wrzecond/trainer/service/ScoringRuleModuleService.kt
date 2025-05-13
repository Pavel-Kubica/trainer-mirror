package cz.fit.cvut.wrzecond.trainer.service

import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.repository.*
import cz.fit.cvut.wrzecond.trainer.entity.*
import cz.fit.cvut.wrzecond.trainer.dto.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException


@Service
class ScoringRuleModuleService(
    override val repository: ScoringRuleModuleRepo,
    private val userRepository: UserRepository,
    private val lessonModuleRepository: LessonModuleRepository,
    private val scoringRuleRepository: ScoringRuleRepository,
    //private val scoringRuleStudentModuleRepo: ScoringRuleStudentModuleRepo,
    private val studentModuleRepository: StudentModuleRepository,
    private val moduleRepository: ModuleRepository,
    private val lessonRepository: LessonRepository,
    private val logRepository: LogRepository,
    private val logService: LogService

) : IServiceBase<ScoringRuleModule>(repository, userRepository) {


    /**
     * Add new module to a given scoring rule
     * @param scoringRuleId identifier of the lesson
     * @param lessonModuleId identifier of the module
     * @param userDto user performing the request
     * @return ScoringRuleModuleReadDTO in case of success
     */
    fun putScoringRuleModule(scoringRuleId: Int, lessonModuleId: Int, userDto: UserAuthenticateDto?)
    = checkAccess(scoringRuleRepository.getReferenceById(scoringRuleId).lesson.id
        ,lessonModuleRepository.getReferenceById(lessonModuleId).module.id, userDto) { lesson, module, user ->
        val moduleId = lessonModuleRepository.getReferenceById(lessonModuleId).module.id
        val msr = repository.getByRuleAndModule(moduleId,scoringRuleId)
        val id = msr?.id ?: 0
        if (id == 0 && ! module.canView(user))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        val newMsr = ScoringRuleModule(
            lessonModuleRepository.getReferenceById(lessonModuleId).module,
            scoringRuleRepository.getReferenceById(scoringRuleId))
        val savedNewMsr = repository.saveAndFlush(newMsr)
        val scoringRule = scoringRuleRepository.getReferenceById(scoringRuleId)
       /* val allCourseUsers = lesson.week.course.users

        for (courseUser in allCourseUsers) {
            val existingStudentModule = studentModuleRepository.findByUserModuleLesson(courseUser.user, module, lesson)
            if (existingStudentModule != null) {
                val existingRuleStudentModule = scoringRuleStudentModuleRepo.getByRuleAndModule(scoringRuleId, existingStudentModule.id)
                if (existingRuleStudentModule == null) {
                    val newRuleStudentModule = ScoringRuleStudentModule(scoringRule = scoringRule, studentModule = existingStudentModule)
                    scoringRuleStudentModuleRepo.save(newRuleStudentModule)
                }
            }
        }*/
        logService.log(userDto, savedNewMsr, "create")
        converter.toReadDTO(savedNewMsr)

    }


    /**
     * Remove module from a given scoring rule
     * @param scoringRuleId identifier of the lesson
     * @param lessonModuleId identifier of the module
     * @param userDto user performing the request
     */
    fun delScoringRuleModule(scoringRuleId: Int, lessonModuleId: Int, userDto: UserAuthenticateDto?): Unit
            = checkAccess(lessonModuleRepository.getReferenceById(lessonModuleId).lesson.id,
        lessonModuleRepository.getReferenceById(lessonModuleId).module.id, userDto) { _, _, _ ->
        val moduleId = lessonModuleRepository.getReferenceById(lessonModuleId).module.id
        val srm = repository.getByRuleAndModule(moduleId,scoringRuleId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        repository.delete(srm)
        val sr = scoringRuleRepository.getReferenceById(scoringRuleId)
            if (sr.toComplete!=null)
                if (sr.toComplete > sr.modules.size){
                    val updatedSr = sr.copy(
                        toComplete = sr.modules.size
                    )
                    scoringRuleRepository.save(updatedSr)
                }
        logService.log(userDto, srm, "delete")
    }

    private fun <X> checkAccess(lessonId: Int, moduleId: Int, userDto: UserAuthenticateDto?,
                                block: (Lesson, Module, User) -> X) = tryCatch {
        val user = getUser(userDto)
        val lesson = lessonRepository.getReferenceById(lessonId)
        val module = moduleRepository.getReferenceById(moduleId)
        if (!lesson.canEdit(user) || !module.canView(user)) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        block(lesson, module, user)
    }

}