import { createRouter, createWebHistory } from 'vue-router'

// 公共页面
import LoginView from '../views/Login.vue'

// 学生页面
import StudentDashboard from '../views/student/Dashboard.vue'
import StudentExamDetail from '../views/student/ExamDetail.vue'
import StudentResults from '../views/student/Results.vue'
import StudentExamQuestions from '../views/student/ExamQuestions.vue'
import StudentPastPapers from '../views/student/PastPapers.vue'
// 新增学生页面
import StudentResultDetail from '../views/student/ResultDetail.vue'
import StudentPastPaperDetail from '../views/student/PastPaperDetail.vue'

// 教师页面
import TeacherDashboard from '../views/teacher/Dashboard.vue'
import QuestionBank from '../views/teacher/QuestionBank.vue'
import CreatePaper from '../views/teacher/CreatePaper.vue'
import ManualCreatePaper from '../views/teacher/ManualCreatePaper.vue'
import AutoCreatePaper from '../views/teacher/AutoCreatePaper.vue'
import TeacherPastPapers from '../views/teacher/PastPapers.vue'
import ExamManagement from '../views/teacher/ExamManagement.vue'
import ExamDetail from '../views/Teacher/ExamDetails.vue' // 教师查看考试详情
import StudentsScores from '../views/teacher/ExamStudentScore.vue'
import TeacherResultDetail from '../views/teacher/TeacherResultDetail.vue'
import TeacherEndExam from '../views/teacher/Examfinished.vue'
import TeacherPastPaperDetail from '../views/teacher/PastPaperDetail.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false } // 登录页不需要认证
  },
  // 学生路由
  {
    path: '/student/dashboard',
    name: 'StudentHome',
    component: StudentDashboard,
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/results',
    name: 'StudentResultList',
    component: StudentResults,
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/past-papers',
    name: 'StudentPastPaperList',
    component: StudentPastPapers,
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/exam/:id/detail',
    name: 'StudentExamDetail',
    component: StudentExamDetail,
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/exam/:courseId/:paperId',
    name: 'StudentExamQuestions',
    component: StudentExamQuestions,
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/result/:courseId/:paperId/:studentId/details',
    name: 'StudentResultDetails',
    component: StudentResultDetail,
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/past-paper/:courseId/:paperId/details',
    name: 'StudentPastPaperDetails',
    component: StudentPastPaperDetail,
    meta: { requiresAuth: true, role: 'student' }
  },
  // 教师路由
  {
    path: '/teacher/dashboard',
    name: 'TeacherHome',
    component: TeacherDashboard,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/question-bank',
    name: 'QuestionBank',
    component: QuestionBank,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path:'/teacher/exams_detail/:courseId/:paperId',
    name: 'TeacherExamDetails',
    component: ExamDetail,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/create-paper',
    name: 'CreatePaper',
    component: CreatePaper,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/create-paper/manual',
    name: 'ManualCreatePaper',
    component: ManualCreatePaper,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/create-paper/manual-edit/:courseId/:paperId',
    name: 'ManualCreatePaperEdit',
    component: ManualCreatePaper,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/create-paper/auto',
    name: 'AutoCreatePaper',
    component: AutoCreatePaper,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/past-papers',
    name: 'TeacherPastPapers',
    component: TeacherPastPapers,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/exam-management',
    name: 'ExamManagement',
    component: ExamManagement,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/exam-details-student-score/:courseId/:paperId',
    name: 'StudentsScores',
    component: StudentsScores,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/past-paper/:courseId/:paperId/details',
    name: 'TeacherPastPaperDetails',
    component: TeacherPastPaperDetail,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/exam-detail/student-exam-detail/:courseId/:paperId/:studentId',
    name: 'TeacherResultDetail',
    component: TeacherResultDetail,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/endedexam-detail',
    name: 'EndedExamDetail',
    component: TeacherEndExam,
    meta: { requiresAuth: true, role: 'teacher' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL || '/'),
  routes
})

// 全局前置导航守卫
router.beforeEach((to, _from, next) => { // 'from' 修改为 '_from'
  const isAuthenticated = !!localStorage.getItem('userToken');
  const userRole = localStorage.getItem('userRole');

  if (to.meta.requiresAuth) {
    if (isAuthenticated) {
      if (to.meta.role) {
        if (to.meta.role === userRole) {
          next();
        } else {
          if (userRole === 'student') {
            next({ name: 'StudentHome' });
          } else if (userRole === 'teacher') {
            next({ name: 'TeacherHome' });
          } else {
            localStorage.removeItem('userToken');
            localStorage.removeItem('userRole');
            next({ name: 'Login' });
          }
        }
      } else {
        next();
      }
    } else {
      next({ name: 'Login', query: { redirect: to.fullPath } });
    }
  } else {
    if (to.name === 'Login' && isAuthenticated) {
      if (userRole === 'student') {
        next({ name: 'StudentHome' });
      } else if (userRole === 'teacher') {
        next({ name: 'TeacherHome' });
      } else {
        next();
      }
    } else {
      next();
    }
  }
});

export default router
