package cz.fit.cvut.wrzecond.trainer.service.helper

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.*
import kotlin.random.Random


/**
 * Service class for managing file operations including saving, reading, and deleting files.
 */
@Service
class FileService {

    /**
     * Tries to save the given file to given directory with given name
     * @property file the file to save
     * @property fileName name to be saved with
     * @property uploadDir directory to be saved to
     * @throws java.io.IOException in case of failure
     */
    fun saveFile(file: MultipartFile, fileName: String, uploadDir: String) {
        // Create uploads path if it does not exist
        val uploadsPath = Paths.get(uploadDir)
        if (!uploadsPath.exists())
            uploadsPath.createDirectories()

        val destinationFile = uploadsPath
            .resolve(Paths.get(fileName))
            .normalize().toAbsolutePath()

        file.inputStream.use { inputStream -> Files.copy(
            inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING
        ) }
    }

    /**
     * Tries to read file at given path and return its contents as byte array
     * @property fileName the file name to read
     * @property uploadDir the path at which to read
     * @return byte array representing contents of the file
     * @throws java.io.IOException in case of error
     */
    fun readFile(fileName: String, uploadDir: String) : ByteArray =
        Paths.get(uploadDir)
            .resolve(Paths.get(fileName))
            .normalize().toAbsolutePath()
            .inputStream()
            .use { inputStream -> inputStream.readAllBytes() }

    /**
     * Tries to delete file at given path
     * @property fileName the file name to read
     * @property uploadDir the path at which to read
     * @return nothing in case of success
     * @throws java.io.IOException in case of error
     */
    fun deleteFile(fileName: String, uploadDir: String) =
        Paths.get(uploadDir)
            .resolve(Paths.get(fileName))
            .deleteIfExists()

    /**
     * Companion object containing constants and utility methods for file operations in the FileService class.
     */
    companion object {
        const val UPLOADS_PATH = "uploads/"
        const val UPLOADS_PATH_STUDENTS = "uploads/students/"
        const val UPLOADS_PATH_IMAGES = "uploads/images/"

        private const val FILE_NAME_LENGTH = 16
        private val CHAR_POOL = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        fun generateFileName () = (1..FILE_NAME_LENGTH)
            .map { Random.nextInt(0, CHAR_POOL.size) }
            .map(CHAR_POOL::get)
            .joinToString("")
    }
}
