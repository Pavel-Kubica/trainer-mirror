export class NavigationElement {
    name
    path
    params
    anonymous

    constructor(name, path, params = null, anonymous = false) {
        this.name = name
        this.path = path
        this.params = params
        this.anonymous = anonymous
    }

    routerPath = () => { return {
        name: this.path,
        params: this.params
    } }
}

export class CourseList extends NavigationElement {
    constructor() {
        super('Trainer', 'course-list')
    }
}

export class CourseEdit extends NavigationElement {
    constructor(course) {
        super(course.name, 'course-edit', {
            course: course.id,
            semester: course.semester.id,
            subject: course.subject.id
        });
    }
}

export class CourseCreate extends NavigationElement {
    constructor(semester,subject) {
        super('$vuetify.nav_create_course', 'course-create', {
            semester: semester,
            subject: subject
        });
    }
}

export class CourseDetail extends NavigationElement {
    constructor(course) {
        if (course.subject === null && course.semester === null)
            super(`Sandbox`, 'course-detail', {
                course: course.id
            })
        else
            super(` ${course.subject?.code} (${course.semester?.code})`, 'course-detail', {
                course: course.id
            })
    }
}

export class CourseUserList extends NavigationElement {
    constructor(course) {
        super('$vuetify.nav_users', 'course-users', {
            course: course.id
        })
    }
}

export class TopicList extends NavigationElement {
    constructor() {
        super('Topics', 'topic-list')
    }
}

export class CourseUserDetail extends NavigationElement {
    constructor(course, user) {
        super(user.username, 'course-user', {
            course: course.id,
            user: user.id
        }, user.username)
    }
}

export class CourseUserImport extends NavigationElement {
    constructor(course) {
        super('$vuetify.nav_import_users', 'course-import', {
            course: course.id
        })
    }
}

export class LessonCreate extends NavigationElement {
    constructor(week) {
        super('$vuetify.nav_create_lesson', 'lesson-create', {
            course: week.course.id,
            week: week.id
        })
    }
}

export class LessonEdit extends NavigationElement {
    constructor(lesson) {
        super(lesson.name, 'lesson-edit', {
            lesson: lesson.id
        })
    }
}

export class LessonDetail extends NavigationElement {
    constructor(lesson) {
        super(lesson.name, 'lesson-detail', {
            lesson: lesson.id
        })
    }
}

export class ModuleDetail extends NavigationElement {
    constructor(lesson, module) {
        super(module.name, 'lesson-module-detail', {
            lesson: lesson.id,
            module: module.id
        })
    }
}

export class ScoringRuleDetail extends NavigationElement {
    constructor(lesson, scoringRule) {
        super(scoringRule.name, 'scoring_rule_detail', {
            lesson: lesson?.id,
            scoringRule: scoringRule?.id ?? -1,
        });
    }
}


export class LessonUserList extends NavigationElement {
    constructor(lesson, t) {
        super(t('$vuetify.nav_lesson_user_list', lesson.name), 'lesson-user-list', {
            lesson: lesson.id
        })
    }
}

export class LessonModuleUser extends NavigationElement {
    constructor(lesson, module,/* scoringRule,*/ user) {
        super(module.name, 'lesson-module-user', {
            lesson: lesson.id,
            module: module.id,
           // scoringRule: scoringRule.id,
            user: user.id
        })
    }
}

export class LessonUserDetail extends NavigationElement {
    constructor(lesson, user, showLesson) {
        super(showLesson ? lesson.name : user.name, 'lesson-user', {
            lesson: lesson.id,
            user: user.id
        })
    }
}

export class ModuleUserDetail extends NavigationElement {
    constructor(lesson, module, user) {
        super(module.name, 'module-user-detail', {
            lesson: lesson.id,
            module: module.id ?? -1,
            user: user.id
        })
    }
}

export class ScoringRuleUserDetail extends NavigationElement {
    constructor(lesson, scoringRule, user) {
        super('scoring_rule_user_detail', 'scoring_rule_user_detail', {
            lesson: lesson?.id,
            scoringRule: scoringRule?.id ?? -1,
            user: user?.id
        });
    }
}

export class ScoringRuleList extends NavigationElement {
    constructor(lesson, user) {
        super('$vuetify.lesson_scoring_rule_list', 'lesson_scoring_rule_list', {
            lesson: lesson,
            user: user
        })
    }
}


export class ScoringRuleDetailUserList extends NavigationElement {
    constructor(lesson,scoringRule, t) {
        super(t('$vuetify.nav_rule_detail_user_list', scoringRule.name), 'rule_detail_user_list', {
            lesson: lesson,
            scoringRule: scoringRule.id
        })
    }
}

export class ScoringRuleUserList extends NavigationElement {
    constructor(lesson, t) {
        console.log("lesson constructor - ", lesson)
        super(t('$vuetify.nav_rule_user_list'), 'rule_user_list', {
            lesson: lesson
        })
    }
}


export class ModuleCreate extends NavigationElement {
    constructor() {
        super('$vuetify.nav_create_module', 'module-create')
    }
}

export class ModuleEdit extends NavigationElement {
    constructor(module) {
        super(module.name, 'module-edit', {
            module: module.id
        })
    }
}

export class ModuleRead extends NavigationElement {
    constructor(module) {
        super(module.name, 'module-read', {
            module: module.id
        })
    }
}

export class NotificationList extends NavigationElement {
    constructor() {
        super('$vuetify.nav_notification_list', 'notifications')
    }
}

export class SubjectsList extends NavigationElement {
    constructor(semester) {
        super('$vuetify.subject_list', 'subjects',{
            semester: semester
        })
    }
}


export class SubjectCreate extends NavigationElement {
    constructor() {
        super('$vuetify.nav_create_subject', 'subject-create');
    }
}

export class SubjectEdit extends NavigationElement {
    constructor(subject) {
        super(subject.name, 'subject-edit', {
            subject: subject.id
        });
    }
}


export class SemesterList extends NavigationElement{
    constructor(){
        super('$vuetify.semester_title', 'semester-list')
    }
}

export class LogList extends NavigationElement{
    constructor() {
        super('$vuetify.log_button', 'log-list');
    }
}

export class ModuleRating extends NavigationElement{
    constructor(){
        super('$vuetify.semester_title', 'rating-list')
    }

}

export class Guide extends NavigationElement{
    constructor() {
        super('$vuetify.guide_page_title','guide');
    }
}

export class Presentation extends NavigationElement{
    constructor() {
        super('$vuetify.presentation_page_title','presentation');
    }
}

export class GuideDetail extends NavigationElement {
    constructor(guide) {
        super(`${guide.id}`, 'guide-detail', {
            guide: guide.id
        })
    }
}

export class GuideMarkdown extends NavigationElement {
    constructor(guideId, markdownId) {
        super(`${guideId}`, 'guide-markdown', {
            guide: guideId,
            markdown: markdownId
        })
    }
}

export class Login extends NavigationElement {
    constructor() {
        super('$vuetify.login', 'login', null, true)
    }
}