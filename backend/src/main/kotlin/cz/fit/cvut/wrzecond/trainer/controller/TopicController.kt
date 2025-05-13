package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.dto.TopicCreateDTO
import cz.fit.cvut.wrzecond.trainer.dto.TopicFindDTO
import cz.fit.cvut.wrzecond.trainer.entity.Topic
import cz.fit.cvut.wrzecond.trainer.service.TopicService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * TopicController is responsible for handling HTTP requests related to topics.
 *
 * @param service Service for handling topic-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/topics")
class TopicController (override val service: TopicService, userService: UserService)
    : IControllerImpl<Topic, TopicFindDTO, TopicFindDTO, TopicCreateDTO, TopicCreateDTO>(service, userService)