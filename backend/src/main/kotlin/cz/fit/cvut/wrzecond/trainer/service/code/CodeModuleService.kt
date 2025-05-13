package cz.fit.cvut.wrzecond.trainer.service.code

import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleUpdateDTO
import cz.fit.cvut.wrzecond.trainer.entity.Module
import cz.fit.cvut.wrzecond.trainer.entity.code.CodeModule
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.repository.code.CodeModuleFileRepository
import cz.fit.cvut.wrzecond.trainer.repository.code.CodeModuleRepository
import cz.fit.cvut.wrzecond.trainer.repository.code.CodeModuleTestRepository
import cz.fit.cvut.wrzecond.trainer.service.IServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * Service class for managing CodeModule entities.
 *
 * @property repository The repository for CodeModule entities
 * @property cmtRepository The repository for CodeModuleTest entities
 * @property cmfRepository The repository for CodeModuleFile entities
 * @property userRepository The repository for User entities
 */
@Service
class CodeModuleService(override val repository: CodeModuleRepository,
                        private val cmtRepository: CodeModuleTestRepository,
                        private val cmfRepository: CodeModuleFileRepository,
                        userRepository: UserRepository)
    : IServiceImpl<CodeModule, CodeModuleFindDTO, CodeModuleGetDTO, CodeModuleCreateDTO, CodeModuleUpdateDTO>(repository, userRepository) {

    /**
     * Retrieves an entity by its ID.
     *
     * @param id The ID of the entity to be retrieved.
     * @param userDto Optional user authentication details used to validate access privileges.
     * @return A CodeModuleGetDTO object.
     */
    override fun getByID(id: Int, userDto: UserAuthenticateDto?)
        = checkViewAccess(getByModuleId(id), userDto) { cm, _ -> converter.toGetDTO(cm) }

    /**
     * Creates and saves a new CodeModule entity based on the provided DTO.
     *
     * @param dto The DTO containing details for the new Code Module.
     * @param userDto Optional user authentication details used to validate access privileges.
     * @return The saved and converted to CodeModuleGetDTO CodeModule entity.
     */
    override fun create(dto: CodeModuleCreateDTO, userDto: UserAuthenticateDto?)
        = checkEditAccess(converter.toEntity(dto), userDto) { cm, _ ->
            val createdCm = repository.saveAndFlush(cm)
            cmtRepository.saveAllAndFlush(dto.tests.map { converter.toEntity(it, createdCm) })
            cmfRepository.saveAllAndFlush(dto.files.map { converter.toEntity(it, createdCm) })
            converter.toGetDTO(repository.getReferenceById(createdCm.id)) // re-fetch with tests
        }

    /**
     * Updates a code module identified by its ID using the provided update data.
     *
     * @param id The ID of the code module to update.
     * @param dto The DTO containing update information for the code module.
     * @param userDto Optional user authentication details used to validate access privileges.
     */
    override fun update(id: Int, dto: CodeModuleUpdateDTO, userDto: UserAuthenticateDto?)
        = checkEditAccess(getByModuleId(id), userDto) { codeModule, _ ->
            checkEditAccess(converter.merge(codeModule, dto), userDto) { cm, _ ->
                val editedCm = repository.saveAndFlush(cm)
                dto.tests?.let { tests -> cmtRepository.saveAllAndFlush(tests.map { converter.toEntity(it, editedCm) }) }
                dto.files?.let { files -> cmfRepository.saveAllAndFlush(files.map { converter.toEntity(it, editedCm) }) }
                converter.toGetDTO(repository.getReferenceById(editedCm.id)) // re-fetch with tests and files
            }
        }

    /**
     * Deletes an entity by its ID.
     *
     * @param id The ID of the entity to be deleted.
     * @param userDto Optional user authentication details used to validate access privileges.
     */
    override fun delete(id: Int, userDto: UserAuthenticateDto?)
        = checkEditAccess(getByModuleId(id), userDto) { entity, _ -> repository.delete(entity) }

    /**
     * Deletes a code module test by its ID.
     *
     * @param cmtId The unique identifier of the code module test to be deleted.
     * @param userDto Optional user authentication details used to validate access privileges.
     */
    fun deleteTest(cmtId: Int, userDto: UserAuthenticateDto?) = tryCatch {
        cmtRepository.getReferenceById(cmtId).run {
            if (codeModule != null) {
                checkEditAccess(codeModule, userDto) { _, _ -> cmtRepository.delete(this) }
            }else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
        }
    }

    /**
     * Deletes a file based on the given cmfId.
     *
     * @param cmfId An ID of the file to be deleted.
     * @param userDto Optional parameter for user authentication data to verify access rights.
     */
    fun deleteFile(cmfId: Int, userDto: UserAuthenticateDto?) = tryCatch {
        cmfRepository.getReferenceById(cmfId).run {
            checkEditAccess(codeModule, userDto) { _, _ -> cmfRepository.delete(this) }
        }
    }

    /**
     * Copy code module from old to new module
     * @property oldModule old module entity
     * @property newModule new module entity
     */
    fun copyModule(oldModule: Module, newModule: Module) {
        val oldCode = repository.getByModuleId(oldModule.id)
        val newCode = oldCode.copy(module = newModule, tests = emptyList(), files = emptyList(), id = 0)
        val newCodePersisted = repository.saveAndFlush(newCode)

        // Copy tests
        val newTests = oldCode.tests.map { cmt -> cmt.copy(codeModule = newCodePersisted, id = 0) }
        cmtRepository.saveAllAndFlush(newTests)
        // Copy files
        val newFiles = oldCode.files.map { cmf -> cmf.copy(codeModule = newCodePersisted, id = 0) }
        cmfRepository.saveAllAndFlush(newFiles)
    }

    /**
     * Helper function to replace module id by code id
     */
    private fun getByModuleId(moduleId: Int) = tryCatch { repository.getByModuleId(moduleId) }

}
