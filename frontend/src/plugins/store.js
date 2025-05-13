import { defineStore } from 'pinia'
import {authApi} from "@/service/api";


export const useUserStore = defineStore('user', {
    state: () => {
        return {
            user: null,
            realUser: null,
            anonymous: false,
            darkMode: false,
            semester: 0,
            gitlabToken: '',
            originalMode: '',
            notifications: 0,
            hideSatisfiedNotifications: false,
            requestedHelpFilter: false,
            allowedShowFilter: false,
            solvedFilter: false,
            hiddenWeeks: {},
            hiddenSubjects: {},
            hiddenScoringRules: {},
            unfoldedLessons: [],
            lessonEditItemIds: {},
            moduleEditItemIds: {},
            requestText: '',
            locale: navigator.language === 'cs-CZ' ? 'customCs' : 'customEn'
        }
    },
    getters: {
        isLoggedIn: (state) => state.user != null,
        isAnyTeacher: (state) => state.user.isTeacher !== false && (state.user.isTeacher === true || state.user.isTeacher.length),
        isTeacher: (state) => ((course) => state.user.isTeacher !== false &&
            (state.user.isTeacher === true || state.user.isTeacher.includes(course.id))),
        isAdmin: (state) => state.user.isAdmin,
        isGuarantor: (state) => state.user.isGuarantor.length > 0,
        isTestUser:(state) => state.user.username !== state.realUser.username,
    },
    actions: {
        setUser(user) {
            this.user = user
            this.realUser = user
            this.gitlabToken = user.gitlabToken
        },
        setNewGitlabToken(token) {
            this.gitlabToken = token
        },
        swapIdentity() {
            authApi.swapIdentity()
            if (this.user.username === this.realUser.username)
                this.user = { id: 1, username: 'test01', name: 'Testovací student', isTeacher: [], isGuarantor: [], isAdmin: false, testUser: true }
            else {
                this.user = this.realUser
            }
        },
        toggleAnonymous() {
            this.anonymous = !this.anonymous
        },
        toggleDarkMode() {
            this.darkMode = !this.darkMode
        },
        toggleLocale() {
            this.locale = this.locale === 'customCs' ? 'customEn' : 'customCs'
        },
        setNotifications(notifications) {
            this.notifications = notifications
        },
        logout() {
            if (this.user.username === 'test01')
                authApi.swapIdentity()
            this.setUser(null)
        }
    },
    persist: true
})
