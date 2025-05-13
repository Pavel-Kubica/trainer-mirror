import {courseShortName} from "@/plugins/constants";

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
        super(courseShortName(course), 'course-detail', {
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
        super('$vuetify.nav_edit_lesson', 'lesson-edit', {
            lesson: lesson.id
        })
    }
}

export class LessonDetail extends NavigationElement {
    constructor(lesson, week) {
        const name = (week ? `${week.name} ` : "") + (lesson ? `(${lesson.name})` : "")
        super(name, 'lesson-detail', {
            lesson: lesson ? lesson.id : week.lessons[0]
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


export class LessonProgressOverview extends NavigationElement {
    constructor(lesson, t) {
        super(t('$vuetify.nav_lesson_user_list'), 'lesson-user-list', {
            lesson: lesson.id
        })
    }
}

export class LessonSolutionsModule extends NavigationElement {
    constructor(lesson, module,/* scoringRule,*/ user) {
        super('$vuetify.nav_lesson_solutions', 'lesson-solutions-module', {
            lesson: lesson.id,
            module: module.id,
           // scoringRule: scoringRule.id,
            user: user.id
        })
    }
}

export class LessonSolutionsUser extends NavigationElement {
    constructor(lesson, user, module) {
        super('$vuetify.nav_lesson_solutions', 'lesson-solutions-user', {
            lesson: lesson.id,
            user: user.id,
            module: module.id
        })
    }
}

export class LessonUserDetail extends NavigationElement {
    constructor(lesson, user) {
        super('$vuetify.nav_lesson_solutions', 'lesson-user', {
            lesson: lesson.id,
            user: user.id
        })
    }
}

export class ScoringRuleUserDetail extends NavigationElement {
    constructor(lesson, scoringRule, user) {
        super(scoringRule.shortName, 'lesson-solutions-scoring-rule', {
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
        super(t('$vuetify.nav_rule_user_list'), 'rule_user_list', {
            lesson: lesson
        })
    }
}


export class ModuleCreate extends NavigationElement {
    constructor(lesson) {
        super('$vuetify.nav_create_module', 'module-create', {
            lesson: lesson.id
        })
    }
}

export class LessonModuleEdit extends NavigationElement {
    constructor(lesson, module) {
        super(module.name, 'module-edit', {
            lesson: lesson.id,
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
        super(guide.name, 'guide-detail', {
            guide: guide.id
        })
    }
}

export class GuideMarkdown extends NavigationElement {
    constructor(guideId, markdown) {
        super(markdown.name, 'guide-markdown', {
            guide: guideId,
            markdown: markdown.id
        })
    }
}

export class Login extends NavigationElement {
    constructor() {
        super('$vuetify.login', 'login', null, true)
    }
}