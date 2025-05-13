// How to add images to md_files:
// Step 1 - place your image in the public/images_for_md_files folder
// Step 2 - reference the image in your md_file like this: /images_for_md_files/image

import course_en from "@/md_files/guide/en/student/course.md";
import lesson_detail_en from "@/md_files/guide/en/student/lesson.md";
import module_en from "@/md_files/guide/en/student/module.md";
import course_cz from "@/md_files/guide/cz/student/course.md";
import lesson_detail_cz from "@/md_files/guide/cz/student/lesson.md";
import module_cz from "@/md_files/guide/cz/student/module.md";

// id indexing for md_files should start from 0
// English and Czech must have equivalent guides at the same id

import teacher_interface_en from "@/md_files/guide/en/teacher/teacher_interface.md"
import student_overview_en from "@/md_files/guide/en/teacher/student_overview.md"
import test_student_en from "@/md_files/guide/en/teacher/test_student.md"
import code_module_en from "@/md_files/guide/en/teacher/code_module.md"
const mdFilesForTeachersEn = [
    {id:0, name: "Teacher interface", content: teacher_interface_en},
    {id:1, name: "Students", content: student_overview_en},
    {id:2, name: "Test student", content: test_student_en},
    {id:3, name: "Code module", content: code_module_en},
]
import teacher_interface_cz from "@/md_files/guide/cz/teacher/teacher_interface.md"
import student_overview_cz from "@/md_files/guide/cz/teacher/student_overview.md"
import test_student_cz from "@/md_files/guide/cz/teacher/test_student.md"
import code_module_cz from "@/md_files/guide/cz/teacher/code_module.md"
const mdFilesForTeachersCz = [
    {id: 0, name: "Učitelské rozhraní", content: teacher_interface_cz},
    {id: 1, name: "Studenti", content: student_overview_cz},
    {id: 2, name: "Testovací student", content: test_student_cz},
    {id: 3, name: "Kódový modul", content: code_module_cz}
]

import admin_interface_en from "@/md_files/guide/en/admin/admin_interface.md"
const mdFilesForAdminsEn = [
    {id: 0, name: 'Admin interface', content: admin_interface_en}
];

import admin_interface_cz from "@/md_files/guide/cz/admin/admin_interface.md"
const mdFilesForAdminsCz = [
    {id: 0, name: 'Rozhraní správce', content: admin_interface_cz}
];

import guarantor_interface_en from "@/md_files/guide/en/guarantor/guarantor_interface.md"
const mdFilesForGuarantorsEn = [
    {id: 0, name: 'Guarantor interface', content: guarantor_interface_en}
];

import guarantor_interface_cz from "@/md_files/guide/cz/guarantor/guarantor_interface.md"
const mdFilesForGuarantorsCz = [
    {id: 0, name: 'Rozhraní garanta', content: guarantor_interface_cz}
];

const mdFileForStudentsEn = [
    {id: 0, name: 'Courses', content: course_en},
    {id: 1, name: 'Lesson', content: lesson_detail_en },
    {id: 2, name: 'Module', content: module_en}
];

const mdFileForStudentsCz = [
    {id: 0, name: 'Kurzy', content: course_cz},
    {id: 1, name: 'Lekce', content: lesson_detail_cz },
    {id: 2, name: 'Modul', content: module_cz}
];

export const ADMIN_GUIDE_ID = 0;
export const GUARANTOR_GUIDE_ID = 1;
export const STUDENT_GUIDE_ID = 2;
export const TEACHER_GUIDE_ID = 3;

const guideForAdmins = {
    id: ADMIN_GUIDE_ID, name: '$vuetify.guide_admins',
    mdFilesEn: mdFilesForAdminsEn,
    mdFilesCz: mdFilesForAdminsCz,
    access: "ADMIN"
}

const guideForGuarantors = {
    id: GUARANTOR_GUIDE_ID, name: '$vuetify.guide_guarantors',
    mdFilesEn: mdFilesForGuarantorsEn,
    mdFilesCz: mdFilesForGuarantorsCz,
    access: "GUARANTOR"
}

const guideForStudents = {
    id: STUDENT_GUIDE_ID, name: '$vuetify.guide_students',
    mdFilesEn: mdFileForStudentsEn,
    mdFilesCz: mdFileForStudentsCz,
    access: "ANY"
}

const guideForTeachers = {
    id: TEACHER_GUIDE_ID, name: '$vuetify.guide_teachers',
    mdFilesEn: mdFilesForTeachersEn,
    mdFilesCz: mdFilesForTeachersCz,
    access: "TEACHER"
}

const guides = [
    guideForAdmins,
    guideForGuarantors,
    guideForStudents,
    guideForTeachers,
];

export default guides