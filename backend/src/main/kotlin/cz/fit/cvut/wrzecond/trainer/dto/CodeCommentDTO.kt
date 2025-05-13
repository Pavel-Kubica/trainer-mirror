package cz.fit.cvut.wrzecond.trainer.dto

/**
 * Data transfer object for code comment find
 * @property id unique identifier of code comment
 * @property fileName name of file where comment is
 * @property rowNumber row number where comment is
 * @property comment comment text
 */
data class CodeCommentFindDTO(override val id: Int, val fileName: String, val rowNumber: Int, val comment: String) : IFindDTO

/**
 * Data transfer object for code comment edit
 * @property fileName name of file where comment is
 * @property rowNumber row number where comment is
 * @property comment comment text
 */
data class CodeCommentEditDTO(val fileName: String, val rowNumber: Int, val comment: String)