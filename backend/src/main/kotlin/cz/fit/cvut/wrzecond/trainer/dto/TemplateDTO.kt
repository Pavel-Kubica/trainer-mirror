package cz.fit.cvut.wrzecond.trainer.dto

import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleTestEditDTO
import cz.fit.cvut.wrzecond.trainer.dto.code.CodeModuleTestFindDTO
import cz.fit.cvut.wrzecond.trainer.entity.code.CodeType
import cz.fit.cvut.wrzecond.trainer.entity.code.EnvelopeType
import cz.fit.cvut.wrzecond.trainer.entity.code.InteractionType
import cz.fit.cvut.wrzecond.trainer.entity.code.LibraryType
import java.sql.Timestamp

/**
 * Data Transfer Object for finding template details.
 *
 * @property id Unique identifier of the template
 * @property name Name of the template
 * @property codeType Type of code associated with the template
 * @property libraryType Type of library used in the template
 * @property interactionType Type of user interaction for the template
 * @property envelopeType Type of envelope used in the template
 * @property customEnvelope Custom envelope if specified
 * @property codeHidden Code that is hidden
 * @property fileLimit Limit on the number of files
 * @property tests List of code module tests associated with the template
 * @property lastModificationTime Timestamp of the last modification
 * @property author Author details of the template
 */
data class TemplateFindDTO(override val id: Int, val name: String, val codeType: CodeType, val libraryType: LibraryType,
                           val interactionType: InteractionType, val envelopeType: EnvelopeType,
                           val customEnvelope: String?, val codeHidden: String, val fileLimit: Int,
                           val tests: List<CodeModuleTestFindDTO>, val lastModificationTime: Timestamp,
                           val author: UserFindDTO) : IFindDTO, IGetDTO

/**
 * Data Transfer Object for creating a new template.
 *
 * @property name Name of the template.
 * @property codeType Type of code the template is associated with.
 * @property libraryType Type of library the template is using.
 * @property interactionType Type of interaction the template supports.
 * @property envelopeType Type of envelope the template uses.
 * @property customEnvelope Optional custom envelope content.
 * @property codeHidden Code that is hidden within the template.
 * @property fileLimit Maximum file limit for the template.
 * @property tests List of tests associated with the template.
 * @property author ID of the author creating the template.
 */
data class TemplateCreateDTO(val name: String, val codeType: CodeType, val libraryType: LibraryType,
                             val interactionType: InteractionType, val envelopeType: EnvelopeType,
                             val customEnvelope: String?, val codeHidden: String, val fileLimit: Int,
                             val tests: List<CodeModuleTestEditDTO>, val author: Int) : ICreateDTO

/**
 * Data Transfer Object for updating a template.
 *
 * @property name Optional name of the template.
 * @property codeType Optional type of code associated with the template.
 * @property libraryType Optional type of library the template uses.
 * @property interactionType Optional type of interaction associated with the template.
 * @property envelopeType Optional type of envelope used in the template.
 * @property customEnvelope Optional custom envelope string.
 * @property codeHidden Optional hidden code in the template.
 * @property fileLimit Optional limit on the number of files allowed.
 * @property tests Optional list of code module tests for the template.
 * @property lastModificationTime Optional timestamp of the last modification.
 */
data class TemplateUpdateDTO(val name: String?, val codeType: CodeType?, val libraryType: LibraryType?,
                             val interactionType: InteractionType?, val envelopeType: EnvelopeType?, val customEnvelope: String?,
                             val codeHidden: String?, val fileLimit: Int?, val tests: List<CodeModuleTestEditDTO>?,
                             val lastModificationTime: Timestamp?,) : IUpdateDTO
