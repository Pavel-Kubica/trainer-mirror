


export const DIFFICULTY_VALUES = (t) => [
    {item: null, title: t('$vuetify.module_difficulty_none')},
    {item: 'BEGINNER', title: t('$vuetify.module_difficulty_beginner')},
    {item: 'EASY', title: t('$vuetify.module_difficulty_easy')},
    {item: 'MEDIUM', title: t('$vuetify.module_difficulty_medium')},
    {item: 'DIFFICULT', title: t('$vuetify.module_difficulty_difficult')},
    {item: 'EXTREME', title: t('$vuetify.module_difficulty_extreme')},
]
export const MODULE_TYPE_VALUES = (t) => [
    {item: 'TEXT', title: t('$vuetify.module_type_text')},
    {item: 'CODE', title: t('$vuetify.module_type_code')},
    {item: 'QUIZ', title: t('$vuetify.module_type_quiz')},
    {item: 'ASSIGNMENT', title: t('$vuetify.module_type_assignment')},
    {item: 'SELFTEST', title: t('$vuetify.module_type_selftest')},
    {item: 'TEMPLATE', title: t('$vuetify.template_title')},
]

export const CODE_MODULE_TYPE_VALUES = (t) => [
    {item: 'SHOWCASE', title: t('$vuetify.code_module.type_showcase')},
    {item: 'TEST_IO', title: t('$vuetify.code_module.type_test_io')},
    {item: 'TEST_ASSERT', title: t('$vuetify.code_module.type_test_assert')},
    {item: 'WRITE_ASSERT', title: t('$vuetify.code_module.type_write_assert')},
]
export const CODE_MODULE_INTERACTION_TYPE_VALUES = (t) => [
    {item: 'EDITOR', title: t('$vuetify.code_module.interaction_editor')},
    {item: 'UPDOWN_ONLY', title: t('$vuetify.code_module.interaction_updown')},
]
export const CODE_MODULE_CODE_LIMIT_VALUES = (t) => [
    {size: 1024, title: t('$vuetify.code_module.code_limit_small')},
    {size: 5120, title: t('$vuetify.code_module.code_limit_medium')},
    {size: 10240, title: t('$vuetify.code_module.code_limit_large')},
]
export const CODE_MODULE_FILE_LIMIT_VALUES = (t) => [
    {size: 10240, title: t('$vuetify.code_module.file_limit_small')},
    {size: 40960, title: t('$vuetify.code_module.file_limit_medium')},
    {size: 81920, title: t('$vuetify.code_module.file_limit_large')},
]
export const CODE_MODULE_LIBRARY_TYPE_VALUES = (t) => [
    {item: 'NO_LIB', title: t('$vuetify.code_module.library_type_none')},
    {item: 'LIB_C', title: t('$vuetify.code_module.library_type_c')},
    {item: 'LIB_CPP', title: t('$vuetify.code_module.library_type_cpp')},
]
export const CODE_MODULE_ENVELOPE_TYPE_VALUES = (t) => [
    {item: 'ENV_C', title: t('$vuetify.code_module.env_type_c')},
    {item: 'ENV_C_IO', title: t('$vuetify.code_module.env_type_c_io')},
    {item: 'ENV_CPP', title: t('$vuetify.code_module.env_type_cpp')},
    {item: 'ENV_CPP_STL', title: t('$vuetify.code_module.env_type_cpp_stl')},
    {item: 'ENV_CUSTOM', title: t('$vuetify.code_module.env_type_custom')},
]
export const CODE_MODULE_FILE_LABELS = (t) => ({
    'SHOWCASE': t('$vuetify.code_module.file_label_showcase'),
    'TEST_IO': t('$vuetify.code_module.file_label_test_io'),
})
export const CODE_MODULE_CODE_COMMENT_MAX_LENGTH = 512

export const errorCodes = {
    ERR_NETWORK: '$vuetify.error_network_connection',
    ERR_BAD_REQUEST: '$vuetify.error_bad_request',
    ERR_BAD_RESPONSE: '$vuetify.error_bad_response',
    ERR_CONCURRENT: '$vuetify.error_concurrent_edit',
}
export const getErrorMessage = (t, err) => t('$vuetify.error', errorCodes[err] ? t(errorCodes[err]) : err)

export const LESSON_TYPE_ICONS = {
    TUTORIAL: 'mdi-book-outline',
    TUTORIAL_PREPARATION: 'mdi-book-education-outline',
    INDIVIDUAL_TASK: 'mdi-home-outline',
    SUPPLEMENTARY: 'mdi-book-plus-outline',
    FREQUENT_MISTAKES: 'mdi-alert-outline',
    INFORMATION: 'mdi-information-outline',
}
export const getIconByLessonType = (type) => LESSON_TYPE_ICONS[type] ?? 'mdi-help-circle-outline'
const LESSON_TYPES = {
    TUTORIAL: '$vuetify.lesson_type_tutorial',
    TUTORIAL_PREPARATION: '$vuetify.lesson_type_tutorial_preparation',
    INDIVIDUAL_TASK: '$vuetify.lesson_type_individual_task',
    SUPPLEMENTARY: '$vuetify.lesson_type_supplementary',
    FREQUENT_MISTAKES: '$vuetify.lesson_type_frequent_mistakes',
    INFORMATION: '$vuetify.lesson_type_information',
}
export const LESSON_TYPES_ITEMS = (t) => Object.keys(LESSON_TYPES).map((key) => ({type: key, name: t(LESSON_TYPES[key])}))
export const getNameByLessonType = (type) => LESSON_TYPES[type] ?? 'mdi-help-circle-outline'

export const LESSON_EDIT_ITEM_INFO = 0
export const LESSON_EDIT_ITEM_TEXT = 1
export const LESSON_EDIT_ITEM_MOD  = 2
export const LESSON_EDIT_ITEM_RULE = 3
export const LESSON_EDIT_ITEMS = [
    {id: LESSON_EDIT_ITEM_INFO, name: '$vuetify.lesson_edit_tab_information', icon: 'mdi-book-outline'},
    {id: LESSON_EDIT_ITEM_TEXT, name: '$vuetify.lesson_edit_tab_text', icon: 'mdi-text-box-outline'},
    {id: LESSON_EDIT_ITEM_MOD , name: '$vuetify.lesson_edit_tab_modules', icon: 'mdi-office-building-outline'},
    {id: LESSON_EDIT_ITEM_RULE , name: '$vuetify.lesson_edit_tab_rules', icon: 'sports_score'}
]

export const LESSON_ITEM_INFO = 0
export const LESSON_ITEM_MOD  = 1
export const LESSON_ITEM_RULE = 2
export const LESSON_ITEMS = [
    {id: LESSON_EDIT_ITEM_INFO},
    {id: LESSON_EDIT_ITEM_TEXT},
    {id: LESSON_EDIT_ITEM_MOD},
    {id: LESSON_EDIT_ITEM_RULE}
]

export const MODULE_EDIT_ITEM_INFO = 0
export const MODULE_EDIT_ITEM_TEXT = 1
export const MODULE_EDIT_CODE_INFO = 10
export const MODULE_EDIT_CODE_TEST = 11
export const MODULE_EDIT_CODE_STUD = 12
export const MODULE_EDIT_CODE_ENV = 13
export const MODULE_EDIT_CODE_TEAC = 14
export const MODULE_EDIT_QUIZ_CREATE = 20
export const MODULE_EDIT_SELFTEST_CREATE = 21

export const MODULE_EDIT_ITEMS = {
    [MODULE_EDIT_ITEM_INFO]: {id: MODULE_EDIT_ITEM_INFO, name: "$vuetify.module_edit_tab_information", icon: 'mdi-book-outline'},
    [MODULE_EDIT_ITEM_TEXT]: {id: MODULE_EDIT_ITEM_TEXT, name: "$vuetify.module_edit_tab_assignment", icon: 'mdi-text-box-outline'},
    [MODULE_EDIT_CODE_INFO]: {id: MODULE_EDIT_CODE_INFO, name: "$vuetify.code_module.edit_tab_information", icon: 'mdi-book-outline'},
    [MODULE_EDIT_CODE_TEST]: {id: MODULE_EDIT_CODE_TEST, name: "$vuetify.code_module.edit_tab_tests", icon: 'mdi-test-tube'},
    [MODULE_EDIT_CODE_STUD]: {id: MODULE_EDIT_CODE_STUD, name: "$vuetify.code_module.edit_tab_student", icon: 'mdi-school'},
    [MODULE_EDIT_CODE_ENV]: {id: MODULE_EDIT_CODE_ENV, name: "$vuetify.code_module.edit_tab_envelope", icon: 'mdi-email-outline'},
    [MODULE_EDIT_CODE_TEAC]: {id: MODULE_EDIT_CODE_TEAC, name: "$vuetify.code_module.edit_tab_teacher", icon: 'mdi-sunglasses'},
    [MODULE_EDIT_QUIZ_CREATE]: {id: MODULE_EDIT_QUIZ_CREATE, name: "$vuetify.quiz_module.create_new_quiz_tab", icon: 'mdi-book-outline'},
    [MODULE_EDIT_SELFTEST_CREATE]: {id: MODULE_EDIT_SELFTEST_CREATE, name: "$vuetify.selftest_module.create_new_selftest_tab", icon: 'mdi-book-outline'},

}

const todayDate = new Date()
export const isHalloween = ((todayDate.getDate() === 31 && todayDate.getMonth() + 1 === 10) || (todayDate.getDate() === 1 && todayDate.getMonth() + 1 === 11))
export const isChristmas = (todayDate.getMonth() + 1 === 12)
const anonEmojis = isHalloween ? ['🎃', '👻', '🦇', '💀', '☠️', '🧛', '🧟']
    : (isChristmas ? ['🎅', '🎄', '🌲', '🧣', '🧤', '⛄', '️☃️', '❄️', '🦌', '🎁']
        : ['🥸', '😏','😎','😒','😗', '😜','😧','😬','😴'])
const ANON_EMOJI_LEN = anonEmojis.length

const hashText = (text) => {
    let hash = 0, i, chr
    if (text.length === 0)
        return hash
    for (i = 0; i < text.length; i++) {
        chr = text.charCodeAt(i)
        hash = ((hash << 5) - hash) + chr
        hash |= 0; // Convert to 32bit integer
    }
    return ((hash % ANON_EMOJI_LEN) + ANON_EMOJI_LEN) % ANON_EMOJI_LEN
}

export const anonEmoji = (username) => anonEmojis[hashText((new Date()).toLocaleDateString() + username)]

export const lessonUserFilter = (userStore, lessonUser) =>
    (!userStore.solvedFilter || lessonUser.module.completed) &&
    (!userStore.requestedHelpFilter || unsatisfiedModuleRequest(lessonUser.module)) &&
    (!userStore.allowedShowFilter || lessonUser.module.allowedShow)

export const moduleBorder = (lesson, module) => {
    console.log("moduleBorder - ", module)
    let color = '#CCCCCC'
    if (!module || module.type === 'TEXT') return `5px solid ${color}`

    if (module.completed) color = '#4CAF50'
    else if (module.progress >= module.minPercent) color = '#FFA500'
    else if (module.progress !== undefined) color = '#FF0000'
    if (unsatisfiedModuleRequest(module) && module.studentRequest.requestType === 'HELP') color= '#FFFF00'
    if (unsatisfiedModuleRequest(module) && module.studentRequest.requestType === 'EVALUATE') color = '#00CCFF'
    if (moduleLocked(lesson, module)) color= '#CCCCCC'
    return `5px solid ${color}`
}

export const ruleBorder =  (lesson,scoringRule) => {
    console.log('ruleBorder: lesson - ', lesson)
    console.log('scoringRule - ', scoringRule)

    let color = '#CCCCCC'
    if (!scoringRule) {return `5px solid ${color}`}
    let completedModules = scoringRule.students.filter((module) => module.completedOn!==null)
    if (completedModules.length >= scoringRule.toComplete) color = '#4CAF50'
    else color = '#FF0000'
    return `5px solid ${color}`
}


export const smBorder = (sm, module) => {
    let color = '#CCCCCC'
    if (sm.completed) color = '#4CAF50'
    else if (sm.progress >= module.minPercent) color = '#FFA500'
    else if (sm.progress !== undefined) color = '#FF0000'
    if (unsatisfiedModuleRequest(module) && module.studentRequest.requestType === 'HELP') color= '#FFFF00'
    if (unsatisfiedModuleRequest(module) && module.studentRequest.requestType === 'EVALUATE') color = '#00CCFF'
    return `5px solid ${color}`
}

export const testBorder = (test) => {
    if (!test.total)
        return ''
    let color = test.passed === test.total ? '#4CAF50' : '#FF0000'
    return `5px solid ${color}`
}

export const testTitle = (test) => {
    if (!test.total) return ''
    let title = ` ${test.passed} / ${test.total}   `
    title += test.passed === test.total ? '✅' : '❌'
    return title
}

export const moduleBg = (module) => {
    let color = ''
    if (!module || module.type === 'TEXT') return color

    if (module.completed) color = '#4CAF5033'
    else if (module.progress >= module.minPercent) color = '#FFA50033'
    else if (module.progress !== undefined) color = '#FF000033'
    if (unsatisfiedModuleRequest(module) && module.studentRequest.requestType === 'HELP') color= '#FFFF0033'
    if (unsatisfiedModuleRequest(module) && module.studentRequest.requestType === 'EVALUATE') color = '#00CCFF33'
    return color
}

export const moduleLocked = (lesson, module) => {
    if (!module) return false
    if (module.locked) return true
    return module.depends && lesson.modules.some((mod) => mod.id === module.depends && mod.progress < 100)
}

export const moduleRequestInfo = (t, module) => {
    if (!unsatisfiedModuleRequest(module) && module.studentRequest?.teacherComment)
        return t('$vuetify.request_teacher_answer', module.studentRequest.teacherComment.name, module.studentRequest.teacherComment.text)

    if (unsatisfiedModuleRequest(module)) {
        const type = module.studentRequest.requestType === 'EVALUATE' ? 'eval' : 'help'
        if (module.teacher)
            return t(`$vuetify.pending_request_${type}_teacher`, module.studentRequest.requestText)
        return t(`$vuetify.pending_request_${type}`)
    }

    return null
}

export const unsatisfiedModuleRequest = (module) => {
    return Boolean(module?.studentRequest?.satisfied === false)
}

export const moduleRequestIsHelp = (module) => {
    if (module.type === 'ASSIGNMENT') return false
    if (unsatisfiedModuleRequest(module))
        return module.studentRequest.requestType === 'HELP'
    return module.progress < module.minPercent
}
export const moduleRequestTeacher = (module) => {
    if (!module.studentRequest || module.studentRequest.satisfied === true) return 'comment';
    if (unsatisfiedModuleRequest(module) && module.studentRequest.requestType === 'EVALUATE') return 'eval';
    return 'help';
}

export const moduleFileDownloadName = (module, extension = '.cpp') => {
    return module.name.normalize("NFD")
            .replace(/\p{Diacritic}/gu, "")
            .toLocaleLowerCase()
            .replaceAll(' ', '-')
        + '-' + (new Date()).toISOString().split(".")[0]
        + extension
}

export const downloadFileUrl = async (url, fileName) => {
    let a = document.createElement("a")
    a.download = fileName
    a.href = url
    a.click()
    setTimeout(() => URL.revokeObjectURL(url))
}
export const downloadFileBlob = async (blob, fileName) => {
    await downloadFileUrl(URL.createObjectURL(blob), fileName)
}

export const hasTouchScreen = () => (('ontouchstart' in window) || (navigator.maxTouchPoints > 0) || (navigator.msMaxTouchPoints > 0));

export const TAB_ASSIGNMENT = 'ASSIGNMENT'
export const TAB_MODULE_DETAIL = 'MODULE_DETAIL'
export const TAB_MODULE_RATING = 'STUDENT_RATING'

export const TEST_COMPILATION_ID = 0
export const TEST_COMPILATION = (t) => ({id: TEST_COMPILATION_ID, name: t('$vuetify.code_module.test_compilation'),
    description: t('$vuetify.code_module.test_compilation_info'), total: 1})
export const MODULE_FILE_CODE = 'student.cpp'
export const DEFAULT_CODE_CONTENT = '#ifndef __TRAINER__\n#include <stdio.h>\n#endif\n\n\nint main () {\n    printf("Hello, world!\\n");\n    return 0;\n}\n'

export const HIDE_PRESENTATION = true;
export const HIDE_GUIDES = false;
