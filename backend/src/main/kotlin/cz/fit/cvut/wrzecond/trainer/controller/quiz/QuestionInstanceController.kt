package cz.fit.cvut.wrzecond.trainer.controller.quiz

import cz.fit.cvut.wrzecond.trainer.controller.Visibility
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * QuestionInstanceController is responsible for handling HTTP requests related to self-tests.
 */
@RestController
@Visibility
@RequestMapping("/selftest")
class QuestionInstanceController {
}