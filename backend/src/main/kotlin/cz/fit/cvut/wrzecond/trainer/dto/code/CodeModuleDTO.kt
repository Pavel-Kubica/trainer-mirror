package cz.fit.cvut.wrzecond.trainer.dto.code

import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IFindDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO
import cz.fit.cvut.wrzecond.trainer.entity.code.CodeType
import cz.fit.cvut.wrzecond.trainer.entity.code.EnvelopeType
import cz.fit.cvut.wrzecond.trainer.entity.code.InteractionType
import cz.fit.cvut.wrzecond.trainer.entity.code.LibraryType

/**
 * This class should not be used
 */
data class CodeModuleFindDTO(override val id: Int) : IFindDTO

/**
 * Data transfer object for code module detail
 * @property id identifier of code module
 * @property codeType type of code module
 * @property interactionType possible types of interaction with the module
 * @property codeHidden code used to test student code
 * @property referencePublic parameter defining whether reference solution should be downloadable
 * @property fileLimit maximum file size for file uploading
 * @property libraryType type of library linked to resulting code
 * @property envelopeType envelope into which student/ref codes are inserted
 * @property customEnvelope if custom envelope is used, its content is here
 * @property tests list of tests ran on the code
 * @property files list of files for students/reference
 */
data class CodeModuleGetDTO(override val id: Int, val codeType: CodeType, val interactionType: InteractionType,
                            val codeHidden: String, val referencePublic: Boolean, val fileLimit: Int, val hideCompilerOutput: Boolean,
                            val libraryType: LibraryType, val envelopeType: EnvelopeType, val customEnvelope: String?,
                            val tests: List<CodeModuleTestFindDTO>, val files: List<CodeModuleFileFindDTO>) : IGetDTO

/**
 * Data transfer object for code module creating
 * @property moduleId identifier of module to which code module should be linked
 * @property codeType type of code module
 * @property interactionType possible types of interaction with the module
 * @property codeHidden code used to test student code
 * @property referencePublic parameter defining whether reference solution should be downloadable
 * @property fileLimit maximum file size for file uploading
 * @property libraryType type of library linked to resulting code
 * @property envelopeType envelope into which student/ref codes are inserted
 * @property customEnvelope if custom envelope is used, its content is here
 * @property tests list of tests ran on the code
 * @property files list of files for students/reference
 */
data class CodeModuleCreateDTO(val moduleId: Int, val codeType: CodeType, val interactionType: InteractionType,
                               val codeHidden: String, val referencePublic: Boolean, val fileLimit: Int, val hideCompilerOutput: Boolean,
                               val libraryType: LibraryType, val envelopeType: EnvelopeType, val customEnvelope: String?,
                               val tests: List<CodeModuleTestEditDTO>, val files: List<CodeModuleFileEditDTO>) : ICreateDTO

/**
 * Data transfer object for code module updating
 * @property codeType type of code module
 * @property interactionType possible types of interaction with the module
 * @property codeHidden code used to test student code
 * @property referencePublic parameter defining whether reference solution should be downloadable
 * @property fileLimit maximum file size for file uploading
 * @property libraryType type of library linked to resulting code
 * @property envelopeType envelope into which student/ref codes are inserted
 * @property customEnvelope if custom envelope is used, its content is here
 * @property tests list of tests ran on the code
 * @property files list of files for students/reference
 */
data class CodeModuleUpdateDTO(val codeType: CodeType?, val interactionType: InteractionType?,
                               val codeHidden: String?, val referencePublic: Boolean?, val fileLimit: Int?, val hideCompilerOutput: Boolean?,
                               val libraryType: LibraryType?, val envelopeType: EnvelopeType?, val customEnvelope: String?,
                               val tests: List<CodeModuleTestEditDTO>?, val files: List<CodeModuleFileEditDTO>?) : IUpdateDTO
