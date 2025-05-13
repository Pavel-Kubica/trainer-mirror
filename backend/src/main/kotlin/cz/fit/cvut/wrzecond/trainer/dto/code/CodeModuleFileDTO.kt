package cz.fit.cvut.wrzecond.trainer.dto.code

import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO

/**
 * Data transfer object for code module files
 * @property id unique identifier of file
 * @property realId same as id, for editing purposes
 * @property name file name shown in tab
 * @property codeLimit maximum characters for written code
 * @property content default file content
 * @property reference reference solution
 * @property headerFile should file be compiled?
 */
data class CodeModuleFileFindDTO(override val id: Int, val realId: Int, val name: String, val codeLimit: Int,
                                 val content: String, val reference: String, val headerFile: Boolean) : IGetDTO

/**
 * DTO used for both creating and editing files
 * if realId is set, existing file will be updated
 * otherwise, new file will be created and assigned a new id
 *
 * @property id unique identifier of file (unused)
 * @property realId id used for determining whether to edit (!= null) or create (= null)
 * @property name file name shown in tab
 * @property codeLimit maximum characters for written code
 * @property content default file content
 * @property reference reference solution
 * @property headerFile should file be compiled?
 */
data class CodeModuleFileEditDTO(val id: Int?, val realId: Int?, val name: String, val codeLimit: Int,
                                 val content: String, val reference: String, val headerFile: Boolean) : ICreateDTO, IUpdateDTO