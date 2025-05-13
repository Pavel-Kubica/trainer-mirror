
// Returns true if str includes searchText, ignoring diacritics such that for example the searchText "rete" would find "řetězce"
export const diacriticInsensitiveStringIncludes = (str, searchText) => {
    const normalizedStr = str.normalize('NFD').replace(/\p{Diacritic}/gui, '');
    const normalizedSearchText = searchText.normalize('NFD').replace(/\p{Diacritic}/gui, '');
    return normalizedStr.includes(normalizedSearchText)
}

export const courseShortName = (course) => {
    return (course.subject ? `${course.subject?.code} - ` : 'Sandbox ') +
           course.shortName +
           (course.semester ? ` (${course.semester?.code})` : '')
}
export const courseLongName = (course) => {
    return (course.subject ? `${course.subject?.code} - ` : '') +
           course.name +
           (course.semester ? ` (${course.semester?.code})` : '')
}
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

const SET_DEFAULT_TESTER_CODE = (codeData) => {
    switch (codeData.codeType) {
        case 'SHOWCASE': {
            if (codeData.libraryType !== 'LIB_CPP')
                codeData.codeHidden = atob('I2luY2x1ZGUgImxpYi9jL3RyYWluZXIuaCIKdXNpbmcgbmFtZXNwYWNlIENUZWFjaGVyOwoKbmFtZXNwYWNlIENTdHVkZW50IHsKICAgIGludCBtYWluKCk7Cn0KCiNkZWZpbmUgTUFYX09VVFBVVF9MRU4gODE5MgoKaW50IG1haW4oaW50IGFyZ2MsIGNoYXIgKiogYXJndikgewogICAgY29uc3QgY2hhciAqIGlucHV0cyBbXSA9IHsKICAgICAgICAiIiwgIklucHV0IDEiLCAiSW5wdXQgMiIsICJJbnB1dCAzIgogICAgfTsKICAgIGludCBwYXJhbSA9IGFyZ2MgPiAxID8gYXRvaShhcmd2WzFdKSA6IDA7CiAgICBjaGFyICogaW5wdXQgPSBzdHJkdXAoaW5wdXRzW3BhcmFtXSk7CiAgICBjaGFyICogb3V0cHV0ID0gZ2VuZXJhdGVPdXRwdXQoaW5wdXRzW3BhcmFtXSwgTUFYX09VVFBVVF9MRU4sIENTdHVkZW50OjptYWluKTsKICAgIENUZXN0ZXI6Om91dHB1dFByaW50KGlucHV0LCBvdXRwdXQpOwogICAgcmV0dXJuIDA7Cn0=')
            else
                codeData.codeHidden = atob('I2luY2x1ZGUgImxpYi9jcHAvdHJhaW5lci5ocHAiCgp1c2luZyBuYW1lc3BhY2UgQ1RlYWNoZXI7CgpuYW1lc3BhY2UgQ1N0dWRlbnQgewogICAgaW50IG1haW4oKTsKfQoKI2RlZmluZSBNQVhfT1VUUFVUX0xFTiA4MTkyCgppbnQgbWFpbihpbnQgYXJnYywgY2hhciAqKiBhcmd2KSB7CiAgICBzdGQ6OnN0cmluZyBpbnB1dHMgW10gPSB7CiAgICAgICAgIiIsICJJbnB1dCAxIiwgIklucHV0IDIiLCAiSW5wdXQgMyIKICAgIH07CiAgICBpbnQgcGFyYW0gPSBhcmdjID4gMSA/IGF0b2koYXJndlsxXSkgOiAwOwogICAgc3RkOjpzdHJpbmcgaW5wdXQgPSBpbnB1dHNbcGFyYW1dOwogICAgc3RkOjpzdHJpbmcgb3V0cHV0ID0gZ2VuZXJhdGVPdXRwdXQoaW5wdXRzW3BhcmFtXSwgQ1N0dWRlbnQ6Om1haW4pOwogICAgQ1Rlc3Rlcjo6b3V0cHV0UHJpbnQoaW5wdXQsIG91dHB1dCk7CiAgICByZXR1cm4gMDsKfQ==')
            return;
        }
        case 'TEST_IO': {
            if (codeData.libraryType !== 'LIB_CPP')
                codeData.codeHidden = atob('I2luY2x1ZGUgImxpYi9jL3RyYWluZXIuaCIKdXNpbmcgbmFtZXNwYWNlIENUZWFjaGVyOwoKbmFtZXNwYWNlIENTdHVkZW50IHsKICAgIGludCBtYWluKCk7Cn0KbmFtZXNwYWNlIENSZWYgewogICAgaW50IG1haW4oKTsKfQoKdm9pZCBnaXZlbl9pbnB1dF9lcXVhbHNfcmVmKGNvbnN0IGNoYXIgKiBpbiwgaW50IGluTGVuKSB7CglpbnQgaW5wdXRMZW4gPSBpbkxlbiA/IGluTGVuIDogc3RybGVuKGluKTsKICBpbnQgTUFYX09VVFBVVF9MRU4gPSBpbnB1dExlbiAqIDMgKyAzMjsKCWNoYXIgKiBvdXRTdHVkZW50ID0gZ2VuZXJhdGVPdXRwdXQgKCBpbiwgTUFYX09VVFBVVF9MRU4sIENTdHVkZW50OjptYWluICksCgkJICogb3V0UmVmID0gZ2VuZXJhdGVPdXRwdXQgKCBpbiwgTUFYX09VVFBVVF9MRU4sIENSZWY6Om1haW4gKTsKICAgIENUZXN0ZXI6OmFzc2VydEVxdWFsICggaW4sIG91dFN0dWRlbnQsIG91dFJlZiApOwp9Cgp2b2lkIGdpdmVuX2lucHV0X2V4cGVjdGVkX291dHB1dChjb25zdCBjaGFyICogaW4sIGNvbnN0IGNoYXIgKiByZWYpIHsKICAgIGNoYXIgKiBvdXQgPSBnZW5lcmF0ZU91dHB1dCAoIGluLCBzdHJsZW4ocmVmKSArIDIsIENTdHVkZW50OjptYWluICk7CiAgICBDVGVzdGVyOjphc3NlcnRFcXVhbCAoIGluLCBvdXQsIHJlZiApOwp9Cgp2b2lkIHRlc3RFZmZlY3Rpdml0eSgpIHsKICAvLyBzaW1pbGFyIHRvIHRlc3RSYW5kb20uLi4KfQoKdm9pZCB0ZXN0UmFuZG9tKCkgewogICAgaW50IE1BWF9JTlBVVF9MRU4gPSAxMDAwLCBOVU1fVEVTVFMgPSA1MDA7CiAgICBjaGFyICogaW4gPSAoY2hhciAqKSBtYWxsb2MgKE1BWF9JTlBVVF9MRU4pOwogICAgZm9yIChpbnQgaSA9IDA7IGkgPCBOVU1fVEVTVFM7ICsraSkgewogICAgICAvLyBnZW5lcmF0ZUlucHV0KGluKTsgLi4uCiAgICAgIGdpdmVuX2lucHV0X2VxdWFsc19yZWYgKCBpbiwgc3RybGVuKGluKSApOwogICAgfQogICAgZnJlZSAoaW4pOwp9Cgp2b2lkIHRlc3RCYXNpYygpIHsKICAgIGdpdmVuX2lucHV0X2V4cGVjdGVkX291dHB1dCgiaW5wdXQxIiwgICJyZWZlcmVuY2UxIik7CiAgICBnaXZlbl9pbnB1dF9leHBlY3RlZF9vdXRwdXQoImlucHV0MiIsICJyZWZlcmVuY2UyIik7CiAgICBnaXZlbl9pbnB1dF9leHBlY3RlZF9vdXRwdXQoIiIsICJyZWZlcmVuY2UzIik7Cn0KCmludCBtYWluKGludCBhcmdjLCBjaGFyICoqIGFyZ3YpIHsKICAgIGludCBwYXJhbSA9IGFyZ2MgPiAxID8gYXRvaShhcmd2WzFdKSA6IDE7CiAgICB2b2lkICgqIHRlc3RzIFtdKSgpID0ge3Rlc3RCYXNpYywgdGVzdFJhbmRvbX07CiAgICB2b2lkICgqIGhpZGRlbiBbXSkoKSA9IHt0ZXN0RWZmZWN0aXZpdHl9OwogICAgaWYgKHBhcmFtID4gMTAwKQogICAgICBoaWRkZW5bcGFyYW0gLSAxMDFdKCk7CiAgICBlbHNlCiAgICAgIHRlc3RzW3BhcmFtIC0gMV0oKTsKICAgIHJldHVybiAwOwp9')
            else
                codeData.codeHidden = atob('I2luY2x1ZGUgImxpYi9jcHAvdHJhaW5lci5ocHAiCgp1c2luZyBuYW1lc3BhY2UgQ1RlYWNoZXI7CgpuYW1lc3BhY2UgQ1N0dWRlbnQgewogICAgaW50IG1haW4oKTsKfQpuYW1lc3BhY2UgQ1JlZiB7CiAgICBpbnQgbWFpbigpOwp9Cgp2b2lkIGdpdmVuX2lucHV0X2VxdWFsc19yZWYoY29uc3Qgc3RkOjpzdHJpbmcmIGluKSB7CglzdGQ6OnN0cmluZyBvdXRTdHVkZW50ID0gZ2VuZXJhdGVPdXRwdXQgKCBpbiwgQ1N0dWRlbnQ6Om1haW4gKSwKCQkgb3V0UmVmID0gZ2VuZXJhdGVPdXRwdXQgKCBpbiwgQ1JlZjo6bWFpbiApOwogICAgQ1Rlc3Rlcjo6YXNzZXJ0RXF1YWwgKCBpbiwgb3V0U3R1ZGVudCwgb3V0UmVmICk7Cn0KCnZvaWQgZ2l2ZW5faW5wdXRfZXhwZWN0ZWRfb3V0cHV0KGNvbnN0IHN0ZDo6c3RyaW5nJiBpbiwgY29uc3Qgc3RkOjpzdHJpbmcmIHJlZikgewogICAgc3RkOjpzdHJpbmcgb3V0ID0gZ2VuZXJhdGVPdXRwdXQgKCBpbiwgQ1N0dWRlbnQ6Om1haW4gKTsKICAgIENUZXN0ZXI6OmFzc2VydEVxdWFsICggaW4sIG91dCwgcmVmICk7Cn0KCnZvaWQgdGVzdEVmZmVjdGl2aXR5KCkgewogIC8vIHNpbWlsYXIgdG8gdGVzdFJhbmRvbS4uLgp9Cgp2b2lkIHRlc3RSYW5kb20oKSB7CiAgICBpbnQgTUFYX0lOUFVUX0xFTiA9IDEwMDAsIE5VTV9URVNUUyA9IDUwMDsKICAgIHN0ZDo6c3RyaW5nIGluOwogICAgZm9yIChpbnQgaSA9IDA7IGkgPCBOVU1fVEVTVFM7ICsraSkgewogICAgICAvLyBnZW5lcmF0ZUlucHV0KGluKTsgLi4uCiAgICAgIGdpdmVuX2lucHV0X2VxdWFsc19yZWYgKCBpbiApOwogICAgfQp9Cgp2b2lkIHRlc3RCYXNpYygpIHsKICAgIGdpdmVuX2lucHV0X2V4cGVjdGVkX291dHB1dCgiaW5wdXQxIiwgICJyZWZlcmVuY2UxIik7CiAgICBnaXZlbl9pbnB1dF9leHBlY3RlZF9vdXRwdXQoImlucHV0MiIsICJyZWZlcmVuY2UyIik7CiAgICBnaXZlbl9pbnB1dF9leHBlY3RlZF9vdXRwdXQoIiIsICJyZWZlcmVuY2UzIik7Cn0KCmludCBtYWluKGludCBhcmdjLCBjaGFyICoqIGFyZ3YpIHsKICAgIGludCBwYXJhbSA9IGFyZ2MgPiAxID8gYXRvaShhcmd2WzFdKSA6IDE7CiAgICB2b2lkICgqIHRlc3RzIFtdKSgpID0ge3Rlc3RCYXNpYywgdGVzdFJhbmRvbX07CiAgICB2b2lkICgqIGhpZGRlbiBbXSkoKSA9IHt0ZXN0RWZmZWN0aXZpdHl9OwogICAgaWYgKHBhcmFtID4gMTAwKQogICAgICBoaWRkZW5bcGFyYW0gLSAxMDFdKCk7CiAgICBlbHNlCiAgICAgIHRlc3RzW3BhcmFtIC0gMV0oKTsKICAgIHJldHVybiAwOwp9Cg==')
            return;
        }
        case 'TEST_ASSERT': {
            if (codeData.libraryType !== 'LIB_CPP')
                codeData.codeHidden = atob('I2luY2x1ZGUgImxpYi9jL3RyYWluZXIuaCIKdXNpbmcgbmFtZXNwYWNlIENUZWFjaGVyOwoKc3RydWN0IHR5cGUgewogIGludCBsZW47CiAgY2hhciBuYW1lIFsxMDI0XTsKICBjaGFyICogb3V0cHV0Owp9OwoKbmFtZXNwYWNlIENSZWYgewogICAgdHlwZSBmdW5jdGlvbm5hbWUgKGludCBwYXJhbTEsIGludCBwYXJhbTIpOwogICAgdm9pZCBmcmVlKHR5cGUpOwp9Cm5hbWVzcGFjZSBDU3R1ZGVudCB7CiAgICB0eXBlIGZ1bmN0aW9ubmFtZSAoaW50IHBhcmFtMSwgaW50IHBhcmFtMik7CiAgICB2b2lkIGZyZWUodHlwZSk7Cn0KCnZvaWQgdGVzdEVmZmVjdGl2aXR5KCkgewogICAgLy8gc2ltaWxhciB0byB0ZXN0UmFuZG9tCn0KCnZvaWQgdGVzdFJhbmRvbSgpIHsKICAgIGludCBOVU1fVEVTVFMgPSAxMDAwOwogICAgZm9yIChpbnQgaSA9IDA7IGkgPCBOVU1fVEVTVFM7ICsraSkgewogICAgICAgIHR5cGUgc3R1ZGVudCA9IENTdHVkZW50OjpmdW5jdGlvbm5hbWUoMSwgMik7CiAgICAgICAgdHlwZSB0ZWFjaGVyID0gQ1JlZjo6ZnVuY3Rpb25uYW1lKDEsIDIpOwogICAgICAgIENUZXN0ZXI6OmFzc2VydEVxdWFsICggIlRlc3QgZGVzY3JpcHRpb24gKHN1Y2ggYXMgbnVtYmVyIGRvZXNuJ3QgbWF0Y2gpIiwgc3R1ZGVudC5udW0sIHRlYWNoZXIubnVtICk7CiAgICAgICAgQ1Rlc3Rlcjo6YXNzZXJ0RXF1YWwgKCAiVGVzdCBkZXNjcmlwdGlvbiAoc3VjaCBhcyBuYW1lIGRvZXNuJ3QgbWF0Y2gpIiwgc3dyYXAoc3R1ZGVudC5uYW1lKSwgc3dyYXAodGVhY2hlci5uYW1lKSApOwogICAgICAgIENUZXN0ZXI6OmNvbXBhcmVMaW5lcyAoIHN0dWRlbnQub3V0cHV0LCB0ZWFjaGVyLm91dHB1dCApOwogICAgICAgIENTdHVkZW50OjpmcmVlKHN0dWRlbnQpOwogICAgICAgIENSZWY6OmZyZWUodGVhY2hlcik7CiAgICB9Cn0KCnZvaWQgdGVzdEJhc2ljKCkgewogICAgdHlwZSBzdHVkZW50ID0gQ1N0dWRlbnQ6OmZ1bmN0aW9ubmFtZSgxLCAyKTsKICAgIENUZXN0ZXI6OmFzc2VydEVxdWFsICggIkJhc2ljIG51bWJlciIsIHN0dWRlbnQubnVtLCAzICk7IAogICAgQ1Rlc3Rlcjo6YXNzZXJ0RXF1YWwgKCAiQmFzaWMgbmFtZSIsIHN3cmFwKHN0dWRlbnQubmFtZSksICJLYXJlbCIgKTsKICAgIENTdHVkZW50OjpmcmVlKHN0dWRlbnQpOwp9CgppbnQgbWFpbihpbnQgYXJnYywgY2hhciAqKiBhcmd2KSB7CiAgICBpbnQgcGFyYW0gPSBhcmdjID4gMSA/IGF0b2koYXJndlsxXSkgOiAxOwogICAgdm9pZCAoKiB0ZXN0cyBbXSkoKSA9IHt0ZXN0QmFzaWMsIHRlc3RSYW5kb219OwogICAgdm9pZCAoKiBoaWRkZW4gW10pKCkgPSB7dGVzdEVmZmVjdGl2aXR5fTsKICAgIGlmIChwYXJhbSA+IDEwMCkKICAgICAgaGlkZGVuW3BhcmFtIC0gMTAxXSgpOwogICAgZWxzZQogICAgICB0ZXN0c1twYXJhbSAtIDFdKCk7CiAgICByZXR1cm4gMDsKfQ==')
            else
                codeData.codeHidden = atob('I2luY2x1ZGUgImxpYi9jcHAvdHJhaW5lci5ocHAiCgp1c2luZyBuYW1lc3BhY2UgQ1RlYWNoZXI7CgpzdHJ1Y3QgdHlwZSB7CiAgaW50IGxlbjsKICBjaGFyIG5hbWUgWzEwMjRdOwogIGNoYXIgKiBvdXRwdXQ7Cn07CgpuYW1lc3BhY2UgQ1JlZiB7CiAgICB0eXBlIGZ1bmN0aW9ubmFtZSAoaW50IHBhcmFtMSwgaW50IHBhcmFtMik7CiAgICB2b2lkIGZyZWUodHlwZSk7Cn0KbmFtZXNwYWNlIENTdHVkZW50IHsKICAgIHR5cGUgZnVuY3Rpb25uYW1lIChpbnQgcGFyYW0xLCBpbnQgcGFyYW0yKTsKICAgIHZvaWQgZnJlZSh0eXBlKTsKfQoKdm9pZCB0ZXN0RWZmZWN0aXZpdHkoKSB7CiAgICAvLyBzaW1pbGFyIHRvIHRlc3RSYW5kb20KfQoKdm9pZCB0ZXN0UmFuZG9tKCkgewogICAgaW50IE5VTV9URVNUUyA9IDEwMDA7CiAgICBmb3IgKGludCBpID0gMDsgaSA8IE5VTV9URVNUUzsgKytpKSB7CiAgICAgICAgdHlwZSBzdHVkZW50ID0gQ1N0dWRlbnQ6OmZ1bmN0aW9ubmFtZSgxLCAyKTsKICAgICAgICB0eXBlIHRlYWNoZXIgPSBDUmVmOjpmdW5jdGlvbm5hbWUoMSwgMik7CiAgICAgICAgQ1Rlc3Rlcjo6YXNzZXJ0RXF1YWwgKCAiVGVzdCBkZXNjcmlwdGlvbiAoc3VjaCBhcyBudW1iZXIgZG9lc24ndCBtYXRjaCkiLCBzdHVkZW50Lm51bSwgdGVhY2hlci5udW0gKTsKICAgICAgICBDVGVzdGVyOjphc3NlcnRFcXVhbCAoICJUZXN0IGRlc2NyaXB0aW9uIChzdWNoIGFzIG5hbWUgZG9lc24ndCBtYXRjaCkiLCBzd3JhcChzdHVkZW50Lm5hbWUpLCBzd3JhcCh0ZWFjaGVyLm5hbWUpICk7CiAgICAgICAgQ1Rlc3Rlcjo6Y29tcGFyZUxpbmVzICggc3R1ZGVudC5vdXRwdXQsIHRlYWNoZXIub3V0cHV0ICk7CiAgICAgICAgQ1N0dWRlbnQ6OmZyZWUoc3R1ZGVudCk7CiAgICAgICAgQ1JlZjo6ZnJlZSh0ZWFjaGVyKTsKICAgIH0KfQoKdm9pZCB0ZXN0QmFzaWMoKSB7CiAgICB0eXBlIHN0dWRlbnQgPSBDU3R1ZGVudDo6ZnVuY3Rpb25uYW1lKDEsIDIpOwogICAgQ1Rlc3Rlcjo6YXNzZXJ0RXF1YWwgKCAiQmFzaWMgbnVtYmVyIiwgc3R1ZGVudC5udW0sIDMgKTsgCiAgICBDVGVzdGVyOjphc3NlcnRFcXVhbCAoICJCYXNpYyBuYW1lIiwgc3dyYXAoc3R1ZGVudC5uYW1lKSwgIkthcmVsIiApOwogICAgQ1N0dWRlbnQ6OmZyZWUoc3R1ZGVudCk7Cn0KCmludCBtYWluKGludCBhcmdjLCBjaGFyICoqIGFyZ3YpIHsKICAgIGludCBwYXJhbSA9IGFyZ2MgPiAxID8gYXRvaShhcmd2WzFdKSA6IDE7CiAgICB2b2lkICgqIHRlc3RzIFtdKSgpID0ge3Rlc3RCYXNpYywgdGVzdFJhbmRvbX07CiAgICB2b2lkICgqIGhpZGRlbiBbXSkoKSA9IHt0ZXN0RWZmZWN0aXZpdHl9OwogICAgaWYgKHBhcmFtID4gMTAwKQogICAgICBoaWRkZW5bcGFyYW0gLSAxMDFdKCk7CiAgICBlbHNlCiAgICAgIHRlc3RzW3BhcmFtIC0gMV0oKTsKICAgIHJldHVybiAwOwp9Cg==')
            return;
        }
        case 'WRITE_ASSERT': {
            if (codeData.libraryType !== 'LIB_CPP')
                codeData.codeHidden = atob('I2luY2x1ZGUgImxpYi9jL3RyYWluZXIuaCIKCnR5cGUgKCp0ZXN0ZWRGbkltcGwpKGludCBwYXJhbTEsIGludCBwYXJhbTIpOwp0eXBlIHRlc3RlZEZuKGludCBwYXJhbTEsIGludCBwYXJhbTIpIHsKICAgIHJldHVybiB0ZXN0ZWRGbkltcGwocGFyYW0xLCBwYXJhbTIpOwp9CgpuYW1lc3BhY2UgQ1N0dWRlbnQgewogICAgdm9pZCB0ZXN0Rm4oKTsKfQoKY2xhc3MgQ0ltcGwgewogICAgc3RhdGljIHR5cGUgdGVzdGVkRm5Db3JyZWN0KGludCBwYXJhbTEsIGludCBwYXJhbTIpIHsKICAgICAgICAvLyBjb3JyZWN0IGltcGxlbWVudGF0aW9uCiAgICB9CiAgICBzdGF0aWMgdHlwZSB0ZXN0ZWRGbldyb25nMShpbnQgcGFyYW0xLCBpbnQgcGFyYW0yKSB7CiAgICAgICAgLy8gd3JvbmcgaW1wbGVtZW50YXRpb24gMQogICAgfQogICAgc3RhdGljIHR5cGUgdGVzdGVkRm5Xcm9uZzIoaW50IHBhcmFtMSwgaW50IHBhcmFtMikgewogICAgICAgIC8vIHdyb25nIGltcGxlbWVudGF0aW9uIDIKICAgIH0KICAgIAogICAgLy8gLi4uIG1vcmUgd3JvbmcgaW1wbGVtZW50YXRpb25zCgogICAgc3RhdGljIHZvaWQgdGVzdChpbnQgaSkgewogICAgICAgIHR5cGUgKCogaW1wbHMgW10pKGludCBwYXJhbTEsIGludCBwYXJhbTIpID0ge3Rlc3RlZEZuQ29ycmVjdCwgdGVzdGVkRm5Xcm9uZzEsIHRlc3RlZEZuV3JvbmcyfTsKICAgICAgICB0ZXN0ZWRGbkltcGwgPSBpbXBsc1tpIC0gMV07CiAgICAgICAgQ1N0dWRlbnQ6OnRlc3RGbigpOwogICAgfQogICAgZnJpZW5kIGludCBtYWluKGludCBhcmdjLCBjaGFyICoqIGFyZ3YpOwp9OwoKaW50IG1haW4oaW50IGFyZ2MsIGNoYXIgKiogYXJndikgewogICAgaW50IHBhcmFtID0gYXJnYyA+IDEgPyBhdG9pKGFyZ3ZbMV0pIDogMTsKICAgIENJbXBsOjp0ZXN0KHBhcmFtKTsKICAgIHJldHVybiAwOwp9Cg==')
            else
                codeData.codeHidden = atob('I2luY2x1ZGUgImxpYi9jcHAvdHJhaW5lci5ocHAiCgp0eXBlICgqdGVzdGVkRm5JbXBsKShpbnQgcGFyYW0xLCBpbnQgcGFyYW0yKTsKdHlwZSB0ZXN0ZWRGbihpbnQgcGFyYW0xLCBpbnQgcGFyYW0yKSB7CiAgICByZXR1cm4gdGVzdGVkRm5JbXBsKHBhcmFtMSwgcGFyYW0yKTsKfQoKbmFtZXNwYWNlIENTdHVkZW50IHsKICAgIHZvaWQgdGVzdEZuKCk7Cn0KCmNsYXNzIENJbXBsIHsKICAgIHN0YXRpYyB0eXBlIHRlc3RlZEZuQ29ycmVjdChpbnQgcGFyYW0xLCBpbnQgcGFyYW0yKSB7CiAgICAgICAgLy8gY29ycmVjdCBpbXBsZW1lbnRhdGlvbgogICAgfQogICAgc3RhdGljIHR5cGUgdGVzdGVkRm5Xcm9uZzEoaW50IHBhcmFtMSwgaW50IHBhcmFtMikgewogICAgICAgIC8vIHdyb25nIGltcGxlbWVudGF0aW9uIDEKICAgIH0KICAgIHN0YXRpYyB0eXBlIHRlc3RlZEZuV3JvbmcyKGludCBwYXJhbTEsIGludCBwYXJhbTIpIHsKICAgICAgICAvLyB3cm9uZyBpbXBsZW1lbnRhdGlvbiAyCiAgICB9CiAgICAKICAgIC8vIC4uLiBtb3JlIHdyb25nIGltcGxlbWVudGF0aW9ucwoKICAgIHN0YXRpYyB2b2lkIHRlc3QoaW50IGkpIHsKICAgICAgICB0eXBlICgqIGltcGxzIFtdKShpbnQgcGFyYW0xLCBpbnQgcGFyYW0yKSA9IHt0ZXN0ZWRGbkNvcnJlY3QsIHRlc3RlZEZuV3JvbmcxLCB0ZXN0ZWRGbldyb25nMn07CiAgICAgICAgdGVzdGVkRm5JbXBsID0gaW1wbHNbaSAtIDFdOwogICAgICAgIENTdHVkZW50Ojp0ZXN0Rm4oKTsKICAgIH0KICAgIGZyaWVuZCBpbnQgbWFpbihpbnQgYXJnYywgY2hhciAqKiBhcmd2KTsKfTsKCmludCBtYWluKGludCBhcmdjLCBjaGFyICoqIGFyZ3YpIHsKICAgIGludCBwYXJhbSA9IGFyZ2MgPiAxID8gYXRvaShhcmd2WzFdKSA6IDE7CiAgICBDSW1wbDo6dGVzdChwYXJhbSk7CiAgICByZXR1cm4gMDsKfQo=')
            return;
        }
        default: {
            return;
        }
    }
}
const SET_DEFAULT_TESTS = (t, codeData) => {
    switch (codeData.codeType) {
        case 'SHOWCASE': {
            codeData.tests = [
                {
                    id: 1,
                    name: t('$vuetify.code_module.basic_test_name'),
                    description: t('$vuetify.code_module.basic_test_description'),
                    parameter: 0,
                    checkMemory: false,
                    hidden: false,
                    tmp: true,
                },
                {
                    id: 2,
                    name: t('$vuetify.code_module.basic_test_name'),
                    description: t('$vuetify.code_module.basic_test_description'),
                    parameter: 1,
                    checkMemory: false,
                    hidden: false,
                    tmp: true,
                },
                {
                    id: 3,
                    name: t('$vuetify.code_module.basic_test_name'),
                    description: t('$vuetify.code_module.basic_test_description'),
                    parameter: 2,
                    checkMemory: false,
                    hidden: false,
                    tmp: true,
                },
                {
                    id: 4,
                    name: t('$vuetify.code_module.basic_test_name'),
                    description: t('$vuetify.code_module.basic_test_description'),
                    parameter: 3,
                    checkMemory: false,
                    hidden: false,
                    tmp: true,
                },
            ]
            return;
        }
        case 'TEST_IO':
        case 'TEST_ASSERT': {
            codeData.tests = [
                {
                    id: 1,
                    name: t('$vuetify.code_module.basic_test_name'),
                    description: t('$vuetify.code_module.basic_test_description'),
                    parameter: 1,
                    checkMemory: false,
                    hidden: false,
                    tmp: true,
                },
                {
                    id: 2,
                    name: t('$vuetify.code_module.random_test_name'),
                    description: t('$vuetify.code_module.random_test_description'),
                    parameter: 2,
                    checkMemory: false,
                    hidden: false,
                    tmp: true,
                },
                {
                    id: 3,
                    name: t('$vuetify.code_module.efficiency_test_name'),
                    description: t('$vuetify.code_module.efficiency_test_description'),
                    parameter: 101,
                    timeLimit: 10,
                    checkMemory: false,
                    hidden: true,
                    tmp: true,
                }
            ]
            return;
        }
        case 'WRITE_ASSERT': {
            codeData.tests = [
                {
                    id: 1,
                    name: t('$vuetify.code_module.correct_implementation_test_name'),
                    description: t('$vuetify.code_module.correct_implementation_test_description'),
                    parameter: 1,
                    checkMemory: false,
                    hidden: false,
                    shouldFail: false,
                    tmp: true,
                },
                {
                    id: 2,
                    name: t('$vuetify.code_module.wrong_implementation_test_name'),
                    description: t('$vuetify.code_module.wrong_implementation_test_description'),
                    parameter: 2,
                    checkMemory: false,
                    hidden: false,
                    shouldFail: true,
                    tmp: true,
                },
                {
                    id: 3,
                    name: t('$vuetify.code_module.wrong_implementation_test_name'),
                    description: t('$vuetify.code_module.wrong_implementation_test_description'),
                    parameter: 3,
                    checkMemory: false,
                    hidden: false,
                    shouldFail: true,
                    tmp: true,
                }
            ]
            return;
        }
        default: {
            return;
        }
    }
}
const SET_DEFAULT_ENVELOPE = (codeData) => {
    switch (codeData.codeType) {
        case 'SHOWCASE':
        case 'TEST_IO': {
            codeData.envelopeType = codeData.libraryType !== 'LIB_CPP' ? 'ENV_C_IO' : 'ENV_CPP'
            return;
        }
        case 'TEST_ASSERT': {
            codeData.envelopeType = codeData.libraryType !== 'LIB_CPP' ? 'ENV_C' : 'ENV_CPP'
            return;
        }
        case 'WRITE_ASSERT': {
            codeData.envelopeType = 'ENV_CUSTOM'
            codeData.customEnvelope = atob('dHlwZSB0ZXN0ZWRGbihpbnQgcGFyYW0xLCBpbnQgcGFyYW0yKTsKCiNpbmNsdWRlIDxhc3NlcnQuaD4KCm5hbWVzcGFjZSBfX1NUVURFTlRfTkFNRVNQQUNFX18gewoJX19TVFVERU5UX0ZJTEVfXwp9')
            return;
        }
        default: {
            return;
        }
    }
}
const SET_DEFAULT_FILES = (codeData) => {
    switch (codeData.codeType) {
        case 'SHOWCASE':
        case 'TEST_IO':
        case 'TEST_ASSERT': {
            codeData.files = [{
                name: codeData.libraryType !== 'LIB_CPP' ? DEFAULT_CODE_FILENAME_C : DEFAULT_CODE_FILENAME_CPP,
                tmp: true,
                id: -1,
                codeLimit: 1024,
                content: codeData.libraryType !== 'LIB_CPP' ?  DEFAULT_CODE_CONTENT_C : DEFAULT_CODE_CONTENT_CPP,
                reference: '',
                headerFile: false
            }]
            return;
        }
        case 'WRITE_ASSERT': {
            codeData.files = [{
                name: codeData.libraryType !== 'LIB_CPP' ? DEFAULT_CODE_FILENAME_C : DEFAULT_CODE_FILENAME_CPP,
                tmp: true,
                id: -1,
                codeLimit: 1024,
                content: atob('I2luY2x1ZGUgPGFzc2VydC5oPgoKLy8gdHlwZSB0ZXN0ZWRGbihpbnQgcGFyYW0xLCBpbnQgcGFyYW0yKTsKCnZvaWQgdGVzdEZuKCkgewogICAgYXNzZXJ0ICggdGVzdGVkRm4oMSwgMikgKTsKICAgIC8vIFRPRE86IG1vcmUgYXNzZXJ0cy4uLgp9'),
                reference: '',
                headerFile: false
            }]
            return;
        }
        default: {
            return;
        }
    }
}
export const generateCodeModuleDefaults = (t, codeData) => {
    SET_DEFAULT_TESTS(t, codeData)
    SET_DEFAULT_ENVELOPE(codeData)
    SET_DEFAULT_TESTER_CODE(codeData)
    SET_DEFAULT_FILES(codeData)
}

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
export const LESSON_TYPES = {
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
export const LESSON_EDIT_ITEM_RULE = 2
export const LESSON_EDIT_ITEMS = [
    {id: LESSON_EDIT_ITEM_INFO, name: '$vuetify.lesson_edit_tab_information', icon: 'mdi-book-outline'},
    {id: LESSON_EDIT_ITEM_TEXT, name: '$vuetify.lesson_edit_tab_text', icon: 'mdi-text-box-outline'},
    {id: LESSON_EDIT_ITEM_RULE , name: '$vuetify.lesson_edit_tab_rules', icon: 'sports_score'}
]

export const LESSON_ITEM_INFO = 0
export const LESSON_ITEM_MOD  = 1
export const LESSON_ITEM_RULE = 2
export const LESSON_ITEMS = [
    {id: LESSON_EDIT_ITEM_INFO},
    {id: LESSON_EDIT_ITEM_TEXT},
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
    ((userStore.solvedFilter && lessonUser.module.completed) ||
        (userStore.requestedHelpFilter && unsatisfiedModuleRequest(lessonUser.module)) || // Pass either filter
        (!userStore.solvedFilter && !userStore.requestedHelpFilter) // Neither filter active
    ) &&
    (!userStore.allowedShowFilter || lessonUser.module.allowedShow)

export const isSolvable = (module) => {
    return module?.type !== "TEXT"
}

export const moduleBorder = (lesson, module) => {
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
    if (!module || module.type === 'TEXT') return '#00000020'

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
        return t('$vuetify.request_teacher_answered', module.studentRequest.teacherComment.name, module.studentRequest.teacherComment.text)

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
export const DEFAULT_CODE_FILENAME_C = 'student.c'
export const DEFAULT_CODE_FILENAME_CPP = 'student.cpp'
export const DEFAULT_CODE_CONTENT_C = '#ifndef __TRAINER__\n#include <stdio.h>\n#endif\n\n\nint main () {\n    printf("Hello, world!\\n");\n    return 0;\n}\n'
export const DEFAULT_CODE_CONTENT_CPP = '#ifndef __TRAINER__\n#include <iostream>\n#endif\n\n\nint main () {\n    std::cout << "Hello, world!" << std::endl;\n    return 0;\n}\n'

export const HIDE_PRESENTATION = true;
export const HIDE_GUIDES = false;
