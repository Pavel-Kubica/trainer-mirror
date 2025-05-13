import axios from 'axios'
import {ref} from "vue";

// Auth
export const authApi = {
    async getOAuthSettings() {
        const response = await axios.get('/auth/settings')
        return response.data
    },

    async login(code) {
        const response = await axios.post('/auth', code)
        return response.data
    },
    async swapIdentity() {
        const response = await axios.get('/users/swap')
        return response.data
    }
}

export const gitlabImportApi = {
    async listAllProject(userToken, ownedType, search, page) {
        const response = await axios.get(`/gitlab/projects?owned=${ownedType}&page=${page}&search=${search}`, {
            headers: {
                token: userToken
            }
        })
        return response.data
    },

    async listAllFiles(userToken, repositoryId, search, path, page) {
        const response = await axios.get(`/gitlab/projects/${repositoryId}/files?search=${search}&path=${path}&page=${page}`, {
            headers: {
                token: userToken
            }
        })
        return response.data
    },

    async getFileData(userToken, repositoryId, filePath) {
        const response = await axios.get(`/gitlab/projects/${repositoryId}/files/raw`, {
            headers: {
                token: userToken,
                filePath: filePath
            }
        })
        return response.data
    }
}

// Topics
export const topicApi = {
    async listTopics() {
        const response = await axios.get('/topics')
        return response.data
    },

    async topicDetail(id) {
        const response = await axios.get(`/topics/${id}`)
        return response.data
    },

    async topicModulesList(id) {
        const response = await axios.get(`/modules/topics?topicIds=${id}`)
        return response.data
    },

    async topicDelete(id) {
        const response = await axios.delete(`/topics/${id}`)
        return response.data
    },
    async editTopic(id, topic) {
        const response = await axios.patch(`/topics/${id}`, topic)
        return response.data
    },
    async createTopic(topic) {
        const response = await axios.post(`/topics`, topic)
        return response.data
    }
}

export const subjectApi = {
    async listSubjects() {
        const response = await axios.get('/subjects')
        return response.data
    },

    async subjectDetail(id) {
        const response = await axios.get(`/subjects/${id}`)
        return response.data
    },

    async deleteSubject(id) {
        const response = await axios.delete(`/subjects/${id}`)
        return response.data
    },

    async createSubject(subject) {
        const response = await axios.post("/subjects", subject)
        return response.data
    },

    async editSubject(id, subject) {
        let guarantors = null
        if (subject.users) {
            const userPromises = subject.users.map(id => userApi.getUserById(id));
            guarantors = await Promise.all(userPromises);
        }
        const response = await axios.patch(`/subjects/${id}`, {
            name: subject.name,
            code: subject.code,
            guarantors: guarantors
        });
        return response.data
    },

    async subjectAddGuarantor(subjectId, userId) {
        const response = await axios.post(`/subjects/${subjectId}/guarantors/${userId}`)
        return response.data
    },

    async subjectDeleteGuarantor(subjectId, userId) {
        const response = await axios.delete(`/subjects/${subjectId}/guarantors/${userId}`)
        return response.data
    },

    async subjectGuarantors(subjectId) {
        const response = await axios.get(`/subjects/${subjectId}/guarantors`)
        return response.data
    },

    async listMeGuarantor() {
        const response = await axios.get(`/subjects/me`)
        return response.data
    }
}

// Courses
export const courseApi = {

    async listCourses() {
        const response = await axios.get("/courses")
        return response.data
    },

    async createCourse(course) {
        let teachers = null
        if (!course.users) {
            throw new Error('No users')
        }
        const userPromises = course.users.map(id => userApi.getUserById(id));
        teachers = await Promise.all(userPromises);
        const teachersToImport = teachers.map(user => ({
            username: user.username,
            name: user.name,
            role: "TEACHER"
        }));
        const response = await axios.post("/courses", course)
        if (course.secret) {
            await courseApi.putCourseSecret(response.data.id, course.secret)
        }
        if (teachersToImport.length) {
            await courseUserApi.courseUserImport(response.data.id, teachersToImport)
        }
        return response.data
    },

    async editCourse(id, course) {
        let teachers = null
        if (course.users) {
            const userPromises = course.users.map(id => userApi.getUserById(id));
            teachers = await Promise.all(userPromises);
        }
        const response = await axios.patch(`/courses/${id}`, {
            name: course.name,
            public: course.public,
            shortName: course.shortName,
            teachers: teachers
        });
        if (course.secret) {
            await courseApi.putCourseSecret(id, course.secret)
        }
        return response.data
    },

    async courseDetail(id) {
        const response = await axios.get(`/courses/${id}`)
        return response.data
    },

    async deleteCourse(id) {
        const response = await axios.delete(`/courses/${id}`)
        return response.data
    },

    async semSubjectCourse(semesterId, subjectId) {
        const response = ref([])
        if (semesterId === "all") {
            response.value = await axios.get(`/courses/all`, {
                params: {
                    subject: subjectId
                }
            })
        } else {
            response.value = await axios.get(`/courses/all`, {
                params: {
                    subject: subjectId,
                    semester: semesterId
                }
            })
        }
        return response.value.data
    },

    async courseWeekDetail(courseId, weekId) {
        const response = await axios.get(`/courses/${courseId}/weeks/${weekId}`)

        return response.data
    },

    async joinCourse(secret) {
        const response = await axios.put(`/courses/me`, {secret: secret})
        return response.data
    },

    async putCourseSecret(id, secret) {
        const response = await axios.put(`/courses/${id}/secret`, {secret: secret})
        return response.data
    },

    async createSandbox(id) {
        const response = await axios.put(`/courses/sandbox/${id}`)
        return response.data
    }
}

export const userApi = {
    async getAllUsers(name) {
        const response = !name ? await axios.get(`/users?&limit=10&offset=0`) : await axios.get(`/users?name=${name}&limit=10&offset=0`)
        return response.data.content;
    },

    async getUserById(id) {
        const response = await axios.get(`/users/${id}`);
        return response.data
    },

    async getSandbox(id) {
        const response = await axios.get(`/users/${id}/sandbox`)
        return response.data
    },

    async getIsTeacher() {
        const response = await axios.get(`/users/teacher`)
        return response.data
    },

    async editGitlabToken(id, token) {
        const response = await axios.patch(`/users/${id}/gitlabToken`, token)
        return response.data
    }
}


// Course users
export const courseUserApi = {
    async courseUsers(id) {
        const response = await axios.get(`/courses/${id}/users`)
        return response.data
    },

    async courseTeachers(id) {
        const response = await axios.get(`/courses/${id}/users/teachers`)
        return response.data
    },

    async courseUserDetail(id, userId) {
        const response = await axios.get(`/courses/${id}/users/${userId}`)
        return response.data
    },

    async courseUserDelete(id, userId) {
        const response = await axios.delete(`/courses/${id}/users/${userId}`)
        return response.data
    },

    async courseUserImport(id, users) {
        const response = await axios.post(`/courses/${id}/users`, users)
        return response.data
    },

}


// Weeks
export const weekApi = {
    async listTaught() {
        const response = await axios.get("/weeks/taught");
        return response.data;
    },

    async createWeek(week) {
        const response = await axios.post("/weeks", week)
        return response.data
    },

    async editWeek(id, week) {
        const response = await axios.patch(`/weeks/${id}`, week)
        return response.data
    },

    async deleteWeek(id) {
        const response = await axios.delete(`/weeks/${id}`)
        return response.data
    },

    async cloneWeek(id, courseId) {
        const response = await axios.post(`/weeks/${id}/courses/${courseId}`)
        return response.data
    },

    async editWeekLessonOrder(weekId, lessonIds) {
        const response = await axios.patch(`/weeks/${weekId}/lessons`, {lessonIds: lessonIds})
        return response.data
    },
}

// Lessons
export const lessonApi = {
    async listCurrentLessons() {
        const response = await axios.get("/lessons/current")
        return response.data
    },

    async lessonDetail(id) {
        const response = await axios.get(`/lessons/${id}`)
        return response.data
    },

    async lessonDetailTeacher(id) {
        const response = await axios.get(`/lessons/${id}/editData`)
        return response.data
    },

    async lessonWeekDetail(id) {
        const response = await axios.get(`/lessons/${id}/week`)
        return response.data
    },

    async createLesson(lesson) {
        const response = await axios.post("/lessons", lesson)
        return response.data
    },

    async editLesson(id, lesson) {
        const response = await axios.patch(`/lessons/${id}`, lesson)
        return response.data
    },

    async deleteLesson(id) {
        const response = await axios.delete(`/lessons/${id}`)
        return response.data
    },

    async cloneLesson(id, courseId) {
        const response = await axios.post(`/lessons/${id}/courses/${courseId}`)
        return response.data
    },

    async cloneLessonToWeek(id, weekId) {
        const response = await axios.post(`/lessons/${id}/weeks/${weekId}`)
        return response.data
    },

    async editLessonModuleOrder(lessonId, moduleIds) {
        const response = await axios.patch(`/lessons/${lessonId}/modules`, {moduleIds: moduleIds})
        return response.data
    },

    async unlockLesson(id, code) {
        const response = await axios.post(`/lessons/${id}/code`, {code: code})
        return response.data
    },
}

// Lesson users
export const lessonUserApi = {
    async lessonUserList(id) {
        const response = await axios.get(`/lessons/${id}/users`)
        return response.data
    },

    async lessonUserDetail(id, userId) {
        const response = await axios.get(`/lessons/${id}/users/${userId}`)
        return response.data
    },

    async lessonUserDelete(lessonId, userId) {
        const response = await axios.delete(`/lessons/${lessonId}/users/${userId}`)
        return response.data
    },

    async lessonWeekDetailTeacher(id, userId) {
        const response = await axios.get(`/lessons/${id}/users/${userId}/week`)
        return response.data
    },
}

// Lesson modules
export const lessonModuleApi = {
    async lessonModuleUserList(lessonId, moduleId) {
        const response = await axios.get(`/lessons/${lessonId}/modules/${moduleId}/users`)
        return response.data
    },

    async lessonModuleWithActiveQuizroom(lessonId) {
        const response = await axios.get(`/lessons/${lessonId}/modules/with-active-quizroom`)
        return response.data
    },

    async putLessonModule(lessonId, moduleId, module) {
        const response = await axios.put(`/lessons/${lessonId}/modules/${moduleId}`, module)
        return response.data
    },

    async copyLessonModule(lessonId, module) {
        const response = await axios.post(`/lessons/${lessonId}/modules`, module)
        return response.data
    },

    async removeLessonModule(lessonId, moduleId) {
        const response = await axios.delete(`/lessons/${lessonId}/modules/${moduleId}`)
        return response.data
    },

    async getLessonModule(lessonId, moduleId) {
        const response = await axios.get(`/lessons/${lessonId}/modules/${moduleId}`)
        return response.data
    },

    async getModulesInLesson(lessonId) {
        const response = await axios.get(`/lessons/${lessonId}/modules`)
        return response.data
    }

}

// Modules
export const moduleApi = {
    async moduleList() {
        const response = await axios.get("/modules")
        return response.data
    },

    async moduleListShort() {
        const response = await axios.get("/modules/short")
        return response.data
    },

    async moduleDetail(id) {
        const response = await axios.get(`/modules/${id}`)
        return response.data
    },

    async createModule(module) {
        const response = await axios.post("/modules", module)
        return response.data
    },

    async editModule(id, module) {
        const response = await axios.patch(`/modules/${id}`, module)
        return response.data
    },

    async deleteModule(id) {
        const response = await axios.delete(`/modules/${id}`)
        return response.data
    },

    async getModuleFile(id) {
        const response = await axios.get(`/modules/${id}/file`, {
            responseType: 'blob'
        })
        return response.data
    },

    async putModuleFile(id, file) {
        const formData = new FormData()
        formData.append("file", file)
        const response = await axios.post(`/modules/${id}/file`, formData, {
            headers: {'Content-Type': 'multipart/form-data'}
        })
        return response.data
    },

    async postImage(image) {
        const formData = new FormData()
        formData.append("image", image)
        const response = await axios.post(`/images`, formData, {
            headers: {'Content-Type': 'multipart/form-data'}
        })
        return response.data
    },

    async moduleTeachers(id) {
        const response = await axios.get(`/modules/${id}/teachers`)
        return response.data
    },

    async addModuleTopic(id, topicId) {
        const response = await axios.post(`/modules/${id}/topics/${topicId}`)
        return response.data
    },

    async deleteModuleTopic(id, topicId) {
        const response = await axios.delete(`/modules/${id}/topics/${topicId}`)
        return response.data
    },

    async getModuleTopics(id) {
        const response = await axios.get(`/modules/${id}/topics`)
        return response.data
    },

    async addModuleSubject(id, subjectId) {
        const response = await axios.post(`/modules/${id}/subjects/${subjectId}`)
        return response.data
    },

    async deleteModuleSubject(id, subjectId) {
        const response = await axios.delete(`/modules/${id}/subjects/${subjectId}`)
        return response.data
    },

    async getModuleSubjects(id) {
        const response = await axios.get(`/modules/${id}/subjects`)
        return response.data
    },

    async getModuleRatings(id) {
        const response = await axios.get(`/modules/${id}/ratings`)
        return response.data
    },

    async getReferenceSolutionAccessibleAnswer(id) {
        try {
            const response = await axios.get(`/code/is_reference_accessible/${id}`);
            return response.data;
        } catch (error) {
            return false;
        }
    }
}

// Student modules
export const studentModuleApi = {
    async putStudentModule(lessonId, moduleId, module) {
        const response = await axios.put(`/lessons/${lessonId}/modules/${moduleId}/data/me`, module)
        return response.data
    },

    async deleteStudentModule(lessonId, moduleId) {
        const response = await axios.delete(`/lessons/${lessonId}/modules/${moduleId}/data/me`)
        return response.data
    },

    async putStudentModuleFile(lessonId, moduleId, file) {
        const formData = new FormData()
        formData.append("file", file)
        const response = await axios.post(`/lessons/${lessonId}/modules/${moduleId}/file/me`, formData, {
            headers: {'Content-Type': 'multipart/form-data'}
        })
        return response.data
    },

    async getStudentModuleFile(lessonId, moduleId) {
        const response = await axios.get(`/lessons/${lessonId}/modules/${moduleId}/file/me`, {
            responseType: 'blob'
        })
        return response.data
    },

    async getStudentModuleFileTeacher(lessonId, moduleId, userId) {
        const response = await axios.get(`/lessons/${lessonId}/modules/${moduleId}/file/users/${userId}`, {
            responseType: 'blob'
        })
        return response.data
    },

    async getStudentModuleRequests(lessonId, moduleId) {
        const response = await axios.get(`/lessons/${lessonId}/modules/${moduleId}/requests/me`)
        return response.data
    },

    async getStudentModuleRequestsTeacher(lessonId, moduleId, userId) {
        const response = await axios.get(`/lessons/${lessonId}/modules/${moduleId}/requests/users/${userId}`)
        return response.data
    },

    async putStudentModuleRequest(lessonId, moduleId, request) {
        const response = await axios.put(`/lessons/${lessonId}/modules/${moduleId}/requests/me`, request)
        return response.data
    },

    async putStudentModuleRequestTeacher(lessonId, moduleId, userId, module) {
        const response = await axios.put(`/lessons/${lessonId}/modules/${moduleId}/requests/users/${userId}`, module)
        return response.data
    },

    async deleteStudentModuleRequest(lessonId, moduleId) {
        const response = await axios.delete(`/lessons/${lessonId}/modules/${moduleId}/requests/me`)
        return response.data
    },


    async getStudentModuleByModuleUserLesson(moduleId, lessonId, userId) {
        const response = await axios.get(`/lessons/${lessonId}/modules/${moduleId}/users/${userId}/all`)
        return response.data
    },

    async deleteStudentModuleRequestAnswer(lessonId, moduleId, userId) {
        const response = await axios.delete(`/lessons/${lessonId}/modules/${moduleId}/requests/users/${userId}`)
        return response.data
    },

    async getStudentModuleRequestCodeComments(requestId) {
        const response = await axios.get(`/student-help-requests/${requestId}/code-comments`)
        return response.data
    },

    async postStudentModuleRequestCodeComment(requestId, codeComment) {
        const response = await axios.post(`/student-help-requests/${requestId}/code-comments`, codeComment)
        return response.data
    },
}


export const moduleRatingApi = {

    async ratingDetail(id) {
        const response = await axios.get(`/studentRatings/${id}`)
        return response.data
    },

    async createRating(rating) {
        const response = await axios.post("/studentRatings", {
            text: rating.text,
            points: rating.points,
            student: rating.studentId,
            module: rating.moduleId
        })
        return response.data
    },

    async editRating(id, rating) {
        const response = await axios.patch(`/studentRatings/${id}`, rating)
        return response.data
    },

    async deleteRating(id) {
        const response = await axios.delete(`/studentRatings/${id}`)
        return response.data
    },
}


// Discussion
export const discussionApi = {
    async getCommentsBasedOnModule(moduleId) {
        const response = await axios.get(`modules/${moduleId}/teachersNotes`)
        return response.data
    },

    async postComment(moduleId, comment) {
        const response = await axios.post(`modules/${moduleId}/teachersNotes`, comment)
        return response.data
    },

    async deleteComment(moduleId, commentId) {
        const response = await axios.delete(`modules/${moduleId}/teachersNotes/${commentId}`)
        return response.data
    },

    async updateComment(moduleId, commentId, comment) {
        const response = await axios.patch(`modules/${moduleId}/teachersNotes/${commentId}`, comment)
        return response.data
    }
}

// Notifications
export const notificationApi = {
    async getNotifications() {
        const response = await axios.get(`/notifications`)
        return response.data
    },

    async getAllNotifications() {
        const response = await axios.get(`/notifications/all`)
        return response.data
    },

    async deleteNotifications() {
        const response = await axios.patch(`/notifications`)
        return response.data
    },
}

export const semesterApi = {
    async getAllSemesters() {
        const response = await axios.get('/semesters')
        return response.data
    },

    async semesterDetail(id) {
        const response = await axios.get(`/semesters/${id}`)
        return response.data
    },

    async getSemesterCourses(semesterId) {
        const response = await axios.get(`/semesters/${semesterId}/courses`)
        return response.data
    },

    async deleteSemester(semesterId) {
        const response = await axios.delete(`/semesters/${semesterId}`)
        return response.data
    },

    async createSemester(semester) {
        const response = await axios.post(`/semesters`, semester)
        return response.data
    },

    async editSemester(semesterId, semester) {
        const response = await axios.patch(`/semesters/${semesterId}`, semester)
        return response.data
    },

    async listSemesters() {
        const response = await axios.get("/semesters")
        return response.data
    },

    async listSubjects() {
        const response = await axios.get(`/subjects`)
        return response.data
    },

    async listSubjectsBySemester(semesterId) {
        const response = await axios.get(`/semesters/${semesterId}/subjects`)
        return response.data
    },


    async listCoursesBySemesterAndSubject(semesterId, subjectId) {
        const response = await axios.get(`/courses/all?semester=${semesterId}&subject=${subjectId}`)
        return response.data
    }

}

export const logApi = {
    async getAllLogs() {
        const response = await axios.get('/logs')
        return response.data
    },
    async deleteLogs(date) {
        const response = await axios.delete(`/logs/date/${date}`)
        return response.data
    }
}

export const templateApi = {
    async getTemplates() {
        const response = await axios.get('/templates')
        return response.data
    },
    async createTemplate(template) {
        const response = await axios.post('/templates', template)
        return response.data
    },
    async editTemplate(id, template) {
        const response = await axios.patch(`/templates/${id}`, template)
        return response.data
    },
    async deleteTemplate(id) {
        const response = await axios.delete(`/templates/${id}`)
        return response.data
    }


}

export const scoringRuleApi = {
    async listScoringRules() {
        const response = await axios.get('/scoringRules')
        return response.data
    },

    async scoringRuleDetail(id) {
        const response = await axios.get(`/scoringRules/${id}`)
        return response.data
    },

    async deleteScoringRule(id) {
        const response = await axios.delete(`/scoringRules/${id}`)
        return response.data
    },
    async editScoringRule(id, scoringRule) {
        const response = await axios.patch(`/scoringRules/${id}`, scoringRule)
        return response.data
    },
    async createScoringRule(scoringRule) {
        const response = await axios.post(`/scoringRules`, scoringRule)
        return response.data
    },


    async listScoringRuleModules(id) {
        const response = await axios.get(`/scoringRules/${id}/modules`)
        return response.data
    },

    async getScoringRuleUser(scoringRuleId, userId) {
        const response = await axios.get(`/scoringRules/${scoringRuleId}/users/${userId}`)
        return response.data
    },

    async putScoringRuleModule(scoringRuleId, moduleId, module) {
        const response = await axios.put(`/scoringRules/${scoringRuleId}/modules/${moduleId}`, module)
        return response.data
    },

    async removeScoringRuleModule(scoringRuleId, moduleId) {
        const response = await axios.delete(`/scoringRules/${scoringRuleId}/modules/${moduleId}`)
        return response.data
    },

    async lessonScoringRulesList(id) {
        const response = await axios.get(`/scoringRules/lessons?lessonIds=${id}`)
        return response.data
    },


}