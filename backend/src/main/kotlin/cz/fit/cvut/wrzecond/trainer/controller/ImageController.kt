package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.controller.helper.setCookie
import cz.fit.cvut.wrzecond.trainer.service.helper.ImageService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * ImageController is responsible for handling HTTP requests related to images.
 *
 * @param service Service for handling image-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@RequestMapping("/images")
class ImageController(private val service: ImageService, userService: UserService) : IControllerAuth(userService) {

    /**
     * Upload an image.
     *
     * @param image The image file to be uploaded, wrapped in a MultipartFile object.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postImage(@RequestPart("image") image: MultipartFile,
                  request: HttpServletRequest, response: HttpServletResponse,
                  @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.LOGGED, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.postImage(user, image)
    }

    /**
     * Retrieves an image.
     *
     * @param image The image to be retrieved.
     * @param request The HTTP request object containing request details.
     * @param response The HTTP response object for sending response details.
     * @param loginSecret The cookie value used for user authentication.
     * @return A ByteArray which represents an image.
     */
    @GetMapping("/{image}", produces = ["image/jpeg", "image/png"])
    fun getImage(@PathVariable image: String,
                 request: HttpServletRequest, response: HttpServletResponse,
                 @CookieValue(value = "loginSecret", defaultValue = "") loginSecret: String)
    = authenticate(request, VisibilitySettings.ALL, loginSecret) { user ->
        setCookie(loginSecret, response)
        service.getImage(image, user) }

}