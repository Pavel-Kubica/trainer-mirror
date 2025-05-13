import axios from 'axios'

export default {

    // QuizModule

    async listQuestions() {
        const response = await axios.get("/questions")
        return response.data
    },

    async listQuestionsByUsername(username) {
        const response = await axios.get(`/questions?user=${username}`)
        return response.data
    },

    async questionDetail(id) {
        const response = await axios.get(`/questions/${id}`)
        return response.data
    },

    async createQuestion(question) {
        const response = await axios.post("/questions", question)
        return response.data
    },

    async editQuestion(id, question) {
        const response = await axios.put(`/questions/${id}`, question)
        return response.data
    },

    async editQuestionAndReasignPoints(id, roomId, question) {
        const response = await axios.put(`/questions/${id}/quizroom/${roomId}`, question)
        return response.data
    },

    async deleteQuestion(id) {
        const response = await axios.delete(`/questions/${id}`)
        return response.data
    },

    async questionCorrect(id) {
        const response = await axios.get(`/questions/${id}/correct`)
        return response.data
    },

    async createQuiz(quiz) {
        const response = await axios.post("/quizzes", quiz)
        return response.data
    },

    async editQuiz(id, quiz) {
        const response = await axios.patch(`/quizzes/${id}`, quiz)
        return response.data
    },

    async listQuizzes() {
        const response = await axios.get("/quizzes")
        return response.data
    },

    async quizDetail(id) {
        const response = await axios.get(`/quizzes/${id}`)
        return response.data
    },

    async quizDetailByModule(moduleId) {
        const response = await axios.get(`/quizzes/module/${moduleId}`)
        return response.data
    },

    async createQuizroom(quizroom) {
        const response = await axios.post("/rooms", quizroom)
        return response.data
    },

    async createSelftest(quizroom) {
        const response = await axios.post("/rooms/selftest", quizroom)
        return response.data
    },

    async selftestDetail(id) {
        const response = await axios.get(`/rooms/${id}/selftest`)
        return response.data
    },

    async quizroomDetail(id) {
        const response = await axios.get(`/rooms/${id}`)
        return response.data
    },

    async editQuizroom(id,quizroom) {
        const response = await axios.put(`/rooms/${id}`, quizroom)
        return response.data
    },

    async quizroomDetailByPassword(password) {
        const response = await axios.get(`/rooms?passwd=${password}`)
        return response.data
    },

    async listQuizroomsByStudent(studentId, quizId) {
        const response = await axios.get(`/rooms/all/${studentId}/quizzes/${quizId}`)
        return response.data
    },

    async getLastQuizroomByStudent(studentId, moduleId) {
        const response = await axios.get(`/rooms/last/${studentId}/modules/${moduleId}`)
        return response.data
    },

    async putQuizroom(roomId, module) {
        const response = await axios.put(`/rooms/${roomId}`, module)
        return response.data
    },

    async createAnswer(answer) {
        const response = await axios.post("/answers", answer)
        return response.data
    },

    async updateAnswer(id, answer) {
        const response = await axios.put(`/answers/${id}`, answer)
        return response.data
    },

    async listStudentAnswersFromQuestion(quizroom, question) {
        const response = await axios.get(`/answers?quizroom=${quizroom}&question=${question}`)
        return response.data
    },

    async studentsAnswerFromQuestionDetail(quizroom, question, student) {
        const response = await axios.get(`/answers?quizroom=${quizroom}&question=${question}&student=${student}`)
        return response.data
    },

    async createQuizroomStudent(quizroom, dto) {
        const response = await axios.post(`/rooms/${quizroom}/students`, dto)
        return response.data
    },

    async listQuizroomStudents(quizroom) {
        const response = await axios.get(`/rooms/${quizroom}/students`)
        return response.data
    },

    async quizroomStudentDetail(quizroom,student) {
        const response = await axios.get(`/rooms/${quizroom}/students/${student}`)
        return response.data
    },

    async editQuizroomStudent(quizroom, student, payload) {
        const response = await axios.put(`/rooms/${quizroom}/students/${student}`, payload)
        return response.data
    },

    async putQuizQuestion(quizId, questionId, question) {
        const response = await axios.put(`/quizzes/${quizId}/questions/${questionId}`, question)
        return response.data
    },

    async removeQuizQuestion(quizId, questionId) {
        const response = await axios.delete(`/quizzes/${quizId}/questions/${questionId}`)
        return response.data
    },

    async addQuestionTopic(id, topicId) {
        const response = await axios.post(`/questions/${id}/topics/${topicId}`)
        return response.data
    },

    async deleteQuestionTopic(id, topicId) {
        const response = await axios.delete(`/questions/${id}/topics/${topicId}`)
        return response.data
    },

    async getQuestionTopics(id) {
        const response = await axios.get(`/questions/${id}/topics`)
        return response.data
    },

    async getQuestionSubjects(id) {
        const response = await axios.get(`/questions/${id}/subjects`)
        return response.data
    },

    async addQuestionSubject(id, subjectId) {
        const response = await axios.post(`/questions/${id}/subjects/${subjectId}`)
        return response.data
    },

    async deleteQuestionSubject(id, subjectId) {
        const response = await axios.delete(`/questions/${id}/subjects/${subjectId}`)
        return response.data
    }

}