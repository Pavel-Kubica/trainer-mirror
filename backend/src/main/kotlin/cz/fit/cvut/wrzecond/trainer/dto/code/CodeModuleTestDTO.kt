package cz.fit.cvut.wrzecond.trainer.dto.code

import cz.fit.cvut.wrzecond.trainer.dto.ICreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.IGetDTO
import cz.fit.cvut.wrzecond.trainer.dto.IUpdateDTO

/**
 * Data transfer object for code module tests
 * @property id unique identifier of test
 * @property realId same as id, for editing purposes
 * @property name test name shown in expansion panel
 * @property parameter parameter passed to tester
 * @property description test description shown in expansion panel
 * @property timeLimit number of seconds after which code execution should be stopped
 * @property checkMemory flag indicating whether unfreed memory should be indicated
 * @property shouldFail should this test fail? (used for write assert)
 * @property hidden are results of the test hidden?
 */
data class CodeModuleTestFindDTO(override val id: Int, val realId: Int, val name: String, val parameter: Int, val description: String,
    val timeLimit: Int?, val checkMemory: Boolean, val shouldFail: Boolean, val hidden: Boolean) : IGetDTO

/**
 * DTO used for both creating and editing modules
 * if realId is set, existing module will be updated
 * otherwise, new will be created and assigned a new id
 *
 * @property id unique identifier of test (unused)
 * @property realId id used for determining whether to edit (!= null) or create (= null)
 * @property name test name shown in expansion panel
 * @property parameter parameter passed to tester
 * @property description test description shown in expansion panel
 * @property timeLimit number of seconds after which code execution should be stopped
 * @property checkMemory flag indicating whether unfreed memory should be indicated
 * @property shouldFail should this test fail? (used for write assert)
 * @property hidden are results of the test hidden?
 */
data class CodeModuleTestEditDTO(val id: Int?, val realId: Int?, val name: String, val parameter: Int,
                                 val description: String, val timeLimit: Int?, val checkMemory: Boolean,
                                 val shouldFail: Boolean, val hidden: Boolean) : ICreateDTO, IUpdateDTO