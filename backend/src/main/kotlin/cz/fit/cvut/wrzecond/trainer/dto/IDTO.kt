package cz.fit.cvut.wrzecond.trainer.dto

/**
 * General CreateDTO interface
 * used for entity creation in POST collection requests
 * all fields must be not-null, as entity is being created
 * there is NO id field, as it will be auto-generated
 */
interface ICreateDTO

/**
 * General UpdateDTO interface
 * used for entity update in PATCH requests
 * fields can be null, as we can want only to update part of entity
 * there is NO id field, as it is part of request path
 */
interface IUpdateDTO

/**
 * General GetDTO interface
 * used for reading one entity in GET requests
 * often contains nested DTOs
 * nullability of fields reflects nullability of database fields
 */
interface IGetDTO {
    /** Unique identifier of entity */
    val id: Int
}

/**
 * General FindDTO interface
 * used for reading list of entities in GET requests
 * nullability of fields reflects nullability of database fields
 */
interface IFindDTO {
    /** Unique identifier of entity */
    val id: Int
}

/**
 * General EditDTO interface
 * used for getting entity details while editing
 * nullability of fields reflects nullability of database fields
 */
interface IEditDTO {
    /** Unique identifier of entity */
    val id: Int
}
