import axios from 'axios'

const codeApiClient = {
    async codeDetail(id) {
        const response = await axios.get(`/code/${id}`)
        return response.data
    },
    async postCode(moduleId, code, templateId = null) {
        code.moduleId = moduleId
        code.templateId = templateId
        const response = await axios.post(`/code`, code)
        return response.data
    },
    async patchCode(id, code) {
        const response = await axios.patch(`/code/${id}`, code)
        return response.data
    },

    async deleteTest(id) {
        const response = await axios.delete(`/code/tests/${id}`)
        return response.data
    },
    async deleteFile(id) {
        const response = await axios.delete(`/code/files/${id}`)
        return response.data
    }
}

export default codeApiClient