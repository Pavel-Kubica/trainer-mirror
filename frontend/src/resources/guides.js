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
import teacher_interface from "@/md_files/guide/en/teacher/teacher_interface.md"
import student_overview from "@/md_files/guide/en/teacher/student_overview.md"
import test_student from "@/md_files/guide/en/teacher/test_student.md"
const mdFilesForTeachersEn = [
    {id:0, name: "Teacher interface", content: teacher_interface},
    {id:1, name: "Overview of students", content: student_overview},
    {id:2, name: "Test student", content: test_student}

]

import admin_interface_en from "@/md_files/guide/en/admin/admin_interface.md"
const mdFilesForAdminsEn = [
    {id: 0, name: 'Admin interface', content: admin_interface_en}
];

import guarantor_interface_en from "@/md_files/guide/en/guarantor/guarantor_interface.md"
const mdFilesForGuarantorsEn = [
    {id: 0, name: 'Guarantor interface', content: guarantor_interface_en}
];


const mdFilesForAdminsCz = [
]

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



const guideForAdmins = {
    id: 0, nameEn: 'Guide for an admin', nameCz: 'Návod pro správce',
    mdFilesEn: mdFilesForAdminsEn,
    mdFilesCz: mdFilesForAdminsCz
}

const guideForGuarantors = {
    id: 1, nameEn: 'Guide for a guarantor', nameCz: 'Návod pro garanta',
    mdFilesEn: mdFilesForGuarantorsEn,
    mdFilesCz: []
}

const guideForStudents = {
    id: 2, nameEn: 'Guide for students', nameCz: 'Návod pro studenty',
    mdFilesEn: mdFileForStudentsEn,
    mdFilesCz: mdFileForStudentsCz
}

const guideForTeachers = {
    id: 3, nameEn: 'Guide for teachers', nameCz: 'Návod pro učitelé',
    mdFilesEn: mdFilesForTeachersEn,
    mdFilesCz: []
}

const guides = [
    guideForAdmins,
    guideForGuarantors,
    guideForStudents,
    guideForTeachers,
];

export default guides