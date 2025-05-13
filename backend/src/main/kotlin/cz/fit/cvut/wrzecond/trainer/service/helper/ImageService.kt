package cz.fit.cvut.wrzecond.trainer.service.helper

import cz.fit.cvut.wrzecond.trainer.dto.UserAuthenticateDto
import cz.fit.cvut.wrzecond.trainer.entity.User
import cz.fit.cvut.wrzecond.trainer.repository.UserRepository
import cz.fit.cvut.wrzecond.trainer.service.IServiceBase
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.FileNotFoundException

/**
 * Service class responsible for handling image-related operations,
 * such as uploading images and retrieving images.
 *
 * @param repository Repository interface for managing User entities.
 * @param authorizationService Service for handling authorization logic within the application.
 */
@Service
class ImageService(override val repository: UserRepository,
                   private val authorizationService: AuthorizationService
    ) : IServiceBase<User>(repository, repository) {

    /**
     * Uploads an image file for a user.
     *
     * @param user The user authentication details used to validate the request.
     * @param image The image file to be uploaded, wrapped in a MultipartFile object.
     * @throws ResponseStatusException If the user is not trusted or the image type is unsupported.
     * @return The URL of the uploaded image as a string.
     */
    fun postImage(user: UserAuthenticateDto?, image: MultipartFile) : String {
        val userEntity = getUser(user)
        if (!authorizationService.isTrusted(userEntity))
            throw ResponseStatusException(HttpStatus.FORBIDDEN)

        val extension = when (image.contentType) {
            "image/jpeg" -> "jpeg"
            "image/png" -> "png"
            else -> throw ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        }
        val fileName = "${FileService.generateFileName()}.$extension"

        fileService.saveFile(image, fileName, FileService.UPLOADS_PATH_IMAGES)
        return "/images/${fileName}"
    }

    /**
     * Retrieves an image.
     *
     * @param image The name of the image file to be retrieved.
     * @param user The user authentication details.
     * @throws ResponseStatusException If the image file is not found.
     * @return A ByteArray representing the contents of the requested image file.
     */
    fun getImage(image: String, user: UserAuthenticateDto?) = try {
        fileService.readFile(image, FileService.UPLOADS_PATH_IMAGES)
    }
    catch (exc: FileNotFoundException) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
    catch (exc: NoSuchFileException) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

}