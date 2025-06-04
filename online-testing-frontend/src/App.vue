// 网页UI使用框架 https://chhhhhzh.github.io/SE_project_front/
<template>
  <div class="layout">
    <!-- 条件渲染侧边栏 -->
    <aside v-if="isLoggedIn" class="sidebar">
      <h2>在线测试子系统</h2>
      <nav>
        <!-- 根据角色动态显示菜单 -->
        <router-link to="/student/dashboard" v-if="isStudent">考试信息</router-link>
        <router-link to="/student/results" v-if="isStudent">答题记录</router-link>
        <router-link to="/student/past-papers" v-if="isStudent">历年真题</router-link>

        <router-link to="/teacher/dashboard" v-if="isTeacher">教师首页</router-link>
        <router-link to="/teacher/question-bank" v-if="isTeacher">题库管理</router-link>
        <router-link to="/teacher/create-paper" v-if="isTeacher">编辑发布试卷</router-link>
        <router-link to="/teacher/past-papers" v-if="isTeacher">查看历年卷</router-link>
        <router-link to="/teacher/exam-management" v-if="isTeacher">考试情况管理</router-link>
        <router-link to="/teacher/endedexam-detail" v-if="isTeacher">考试成绩管理</router-link>
      </nav>
    </aside>

    <main class="content">
      <!-- ⭐ 顶栏，右上角用户模块 -->
      <div class="top-bar" v-if="isLoggedIn">
        <div class="time-display">
          {{ currentTime }}
        </div>
        <div class="user-area" @click="toggleDropdown">
          <img src="./assets/default_teacher.png" alt="头像" class="avatar" />
          <span class="username">{{ userId }}</span>
          <svg class="arrow" viewBox="0 0 1024 1024" width="12" height="12">
            <path d="M512 672L192 352h640z" fill="#333" />
          </svg>
        </div>

        <div v-if="dropdownVisible" class="dropdown-menu">
          <div class="dropdown-item" @click="logout">退出登录</div>
        </div>
      </div>

      <div class="page-container">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'; // 引入 watch
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

// 用户信息 - 使用 ref 进行响应式管理
const username = ref('访客');
const userRole = ref(null); // 'student' or 'teacher'
const isAuthenticated = ref(false); // 关键：响应式的登录状态

// 用 computed 实时获取 userId
const userId = computed(() => {
  const user = JSON.parse(localStorage.getItem('user'))
  return user ? user.userId : '默认userId'
})

// 用户信息
const role = ref(null) // 'student' or 'teacher'
const dropdownVisible = ref(false);
const currentTime = ref('');
let intervalId = null;

// 函数：从 localStorage 更新认证状态和用户信息到响应式 ref
const updateAuthState = () => {
  const token = localStorage.getItem('userToken');
  const roleFromStorage = localStorage.getItem('userRole');
  const usernameFromStorage = localStorage.getItem('username'); // 假设登录时也保存了用户名

  isAuthenticated.value = !!token; // 更新响应式登录状态

  if (isAuthenticated.value) {
    userRole.value = roleFromStorage;
    username.value = usernameFromStorage || (roleFromStorage === 'student' ? '学生用户' : (roleFromStorage === 'teacher' ? '教师用户' : '用户'));
  } else {
    userRole.value = null;
    username.value = '访客';
  }
  console.log('Auth state updated in App.vue:', { // 调试信息
    isAuthenticated: isAuthenticated.value,
    userRole: userRole.value,
    username: username.value
  });
};

// 登录状态判断 (基于 isAuthenticated ref)
const isLoggedIn = computed(() => isAuthenticated.value);

// 角色判断 (基于 userRole ref 和 isAuthenticated ref)
const isStudent = computed(() => isAuthenticated.value && userRole.value === 'student');
const isTeacher = computed(() => isAuthenticated.value && userRole.value === 'teacher');

const formatTime = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

onMounted(() => {
  currentTime.value = formatTime();
  intervalId = setInterval(() => {
    currentTime.value = formatTime();
  }, 1000);

  updateAuthState(); // 页面加载时首次更新认证状态

  // 监听 localStorage 变化，以便在其他标签页登出或登录时同步状态
  window.addEventListener('storage', (event) => {
    if (event.key === 'userToken' || event.key === 'userRole' || event.key === 'username') {
      updateAuthState();
    }
  });
});

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId);
  }
  window.removeEventListener('storage', updateAuthState);
});

// 新增：监听路由变化，并在变化后更新认证状态
// 这确保了从登录页跳转到其他页面时，App.vue能感知到localStorage的变化
watch(
    () => route.fullPath, // 监听完整路径的变化
    (newPath, oldPath) => {
      if (newPath !== oldPath) { // 确保路径确实改变了
        updateAuthState(); // 重新检查并更新认证状态
      }
    }
);

const toggleDropdown = () => {
  dropdownVisible.value = !dropdownVisible.value;
};

const logout = () => {
  localStorage.removeItem('userToken');
  localStorage.removeItem('userRole');
  localStorage.removeItem('username');
  localStorage.removeItem('userId'); // 如果您之前使用了 'user' 对象，也考虑是否需要清除

  updateAuthState(); // 关键：清除 localStorage后，立即更新响应式状态

  dropdownVisible.value = false;
  router.replace({ name: 'Login' }); // 使用 replace 跳转
};
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', system-ui, -apple-system, sans-serif;
  background-color: var(--background-light);
}

.sidebar {
  width: 260px;
  background: linear-gradient(135deg, #1a237e, #0d47a1);
  color: white;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: var(--shadow-lg);
  transition: var(--transition-base);
  z-index: 100;
}

.sidebar h2 {
  margin-bottom: 32px;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.5px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar nav a {
  display: block;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  margin-bottom: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  transition: var(--transition-base);
  font-size: 15px;
}

.sidebar nav a:hover {
  background-color: rgba(255, 255, 255, 0.15);
  transform: translateX(4px);
}

.sidebar nav a.router-link-active {
  background-color: rgba(255, 255, 255, 0.2);
  font-weight: 500;
  color: white;
  box-shadow: var(--shadow-sm);
}

.content {
  flex: 1;
  height: 100vh;
  overflow-y: auto;
  background-color: var(--background-light);
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.top-bar {
  width: 100%;
  height: 64px;
  background-color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  box-sizing: border-box;
  border-bottom: 1px solid var(--border-color);
  position: relative;
  box-shadow: var(--shadow-sm);
}

.time-display {
  font-size: 14px;
  color: var(--text-light);
  font-weight: 500;
}

.user-area {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  padding: 8px 12px;
  border-radius: 8px;
  transition: var(--transition-base);
}

.user-area:hover {
  background-color: var(--background-light);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
  border: 2px solid var(--border-color);
  transition: var(--transition-base);
}

.user-area:hover .avatar {
  border-color: var(--primary-color);
}

.username {
  font-size: 15px;
  color: var(--text-color);
  margin-right: 8px;
  font-weight: 500;
}

.arrow {
  transform: rotate(0deg);
  transition: var(--transition-base);
}

.user-area:hover .arrow {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 72px;
  right: 32px;
  background: white;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-lg);
  border-radius: 8px;
  overflow: hidden;
  z-index: 100;
  min-width: 160px;
  animation: slideDown 0.2s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  padding: 12px 20px;
  font-size: 14px;
  color: var(--text-color);
  cursor: pointer;
  transition: var(--transition-base);
}

.dropdown-item:hover {
  background-color: var(--background-light);
  color: var(--primary-color);
}

.page-container {
  flex: 1;
  padding: 24px;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .sidebar {
    width: 200px;
    padding: 16px;
  }

  .top-bar {
    padding: 0 16px;
  }

  .page-container {
    padding: 16px;
  }
}
</style>