package cz.fit.cvut.wrzecond.trainer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * The main entry point for the Trainer application.
 */
@SpringBootApplication
class TrainerApplication

/**
 * The main entry point of the Trainer application.
 *
 * @param args Command-line arguments passed to the application.
 */
fun main(args: Array<String>) {
    runApplication<TrainerApplication>(*args)
}
