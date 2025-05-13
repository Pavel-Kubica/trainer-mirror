package cz.fit.cvut.wrzecond.trainer.controller

import cz.fit.cvut.wrzecond.trainer.dto.*
import cz.fit.cvut.wrzecond.trainer.entity.Template
import cz.fit.cvut.wrzecond.trainer.service.TemplateService
import cz.fit.cvut.wrzecond.trainer.service.UserService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * TemplateController is responsible for handling HTTP requests related to templates.
 *
 * @param service Service for handling template-related operations
 * @param userService Service for handling user-related operations.
 */
@RestController
@Visibility
@RequestMapping("/templates")
class TemplateController (override val service: TemplateService, userService: UserService)
    : IControllerImpl<Template, TemplateFindDTO, TemplateFindDTO, TemplateCreateDTO, TemplateUpdateDTO>(service, userService)