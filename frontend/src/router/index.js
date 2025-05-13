import { createRouter, createWebHistory } from 'vue-router'
import {useUserStore} from "@/plugins/store";

const routes = [
  {
    path: '/',
    name: 'course-list',
    component: () => import('../views/CourseListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/courses/:course/users',
    name: 'course-users',
    component: () => import('../views/CourseUserListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/courses/:course/users/new',
    name: 'course-import',
    component: () => import('../views/CourseUserImportView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/courses/:course',
    name: 'course-detail',
    component: () => import('../views/CourseDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path:'/subjects/:semester/:subject/new',
    name:'course-create',
    component:() => import('../views/CourseCreateEditView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/subjects/:semester/:subject/:course/edit',
    name: 'course-edit',
    component: () => import('../views/CourseCreateEditView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/courses/:course/weeks/:week',
    name: 'lesson-create',
    component: () => import('../views/LessonCreateEditView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson',
    name: 'lesson-detail',
    component: () => import('../views/LessonModuleDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/modules/:module',
    name: 'lesson-module-detail',
    component: () => import('../views/LessonModuleDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/scoringRules/:scoringRule',
    name: 'scoring_rule_detail',
    component: () => import('../views/LessonModuleDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/users',
    name: 'lesson-user-list',
    component: () => import('../views/LessonProgressOverviewView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/scoringRules/:user',
    name: 'lesson_scoring_rule_list',
    component: () => import('../views/LessonScoringRuleDetailView.vue'),
    meta: { requiresAuth: true },
  },
  /* {
     path: '/lessons/:lesson/scoringRules/:scoringRule/users/:user',
     name: 'lesson-scoring-rule-user-detail1',
     component: () => import('../views/LessonScoringRuleUserDetailView1.vue'),
     meta: { requiresAuth: true },
   },*/
  {
    path: '/lessons/:lesson/scoringRules/:scoringRule/users',
    name: 'rule_detail_user_list',
    component: () => import('../views/ScoringRuleDetailUserListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/scoringRules/users',
    name: 'rule_user_list',
    component: () => import('../views/ScoringRuleUserListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/modules/:module/users/:user',
    name: 'lesson-solutions-module',
    component: () => import('../views/LessonSolutionsView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/users/:user/modules/:module',
    name: 'lesson-solutions-user',
    component: () => import('../views/LessonSolutionsView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/users/:user/scoringRules/:scoringRule',
    name: 'lesson-solutions-scoring-rule',
    component: () => import('../views/LessonSolutionsView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/users/:user',
    name: 'lesson-user',
    component: () => import('../views/LessonSolutionsView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/edit',
    name: 'lesson-edit',
    component: () => import('../views/LessonCreateEditView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/modules/:module/edit',
    name: 'module-edit',
    component: () => import('../views/ModuleCreateEditView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/lessons/:lesson/modules/new',
    name: 'module-create',
    component: () => import('../views/ModuleCreateEditView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/notifications',
    name: 'notifications',
    component: () => import('../views/NotificationView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/subjects/:semester',
    name: 'subjects',
    component: () => import('../views/SubjectListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/subjects/:semester/:subject/edit',
    name: 'subject-edit',
    component: () => import('../views/SubjectCreateEditView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/subjects/new',
    name: 'subject-create',
    component: () => import('../views/SubjectCreateEditView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/semesters',
    name: 'semester-list',
    component: () => import('../views/SemesterListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/logs',
    name: 'log-list',
    component: () => import('../views/LogListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/topics',
    name: 'topic-list',
    component: () => import('../views/TopicListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/guide',
    name: 'guide',
    component: () => import('../views/GuideListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/guide/:guide',
    name: 'guide-detail',
    component: () => import('../views/GuideDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path:'/guide/:guide/:markdown',
    name:'guide-markdown',
    component: () => import('../views/GuideMarkdownView.vue'),
    meta: { requiresAuth: true },
  },
  {
      path: '/presentation',
      name: 'presentation',
      component: () => import('../views/PresentationView.vue'),
      meta: { requiresAuth: false },
  }

]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to) => {
  const store = useUserStore()
  const isLoggedIn = store.isLoggedIn
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

  if (requiresAuth && !isLoggedIn) {
    return { name: 'login' }  // Redirect to login page
  }

  if (to.name === 'login' && isLoggedIn) {
    return { name: 'course-list' }  // Redirect to course list
  }

  // Continue to the route
});

export default router
