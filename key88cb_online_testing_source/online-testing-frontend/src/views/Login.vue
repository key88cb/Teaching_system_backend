<template>
  <div class="login">
    <h1>在线测试系统</h1>
    <div class="login-container">
      <div class="user-type">
        <button
            :class="['type-btn', { active: userType === 'student' }]"
            @click="setUserType('student')"
        >
          学生登录
        </button>
        <button
            :class="['type-btn', { active: userType === 'teacher' }]"
            @click="setUserType('teacher')"
        >
          教师登录
        </button>
      </div>

      <form @submit.prevent="login" class="login-form">
        <div class="form-group">
          <label v-if="userType === 'student'">学号</label>
          <label v-else>工号</label>
          <input
              type="text"
              v-model="credentials.id"
              :placeholder="userType === 'student' ? '请输入学号' : '请输入工号'"
              required
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
              type="password"
              v-model="credentials.password"
              placeholder="请输入密码"
              required
          />
        </div>

        <button type="submit" class="submit-btn">
          登录
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router'; // 1. 引入 useRoute

const router = useRouter();
const route = useRoute(); // 2. 获取 route 对象以访问查询参数
const userType = ref('student');
const credentials = ref({
  id: '',
  password: ''
});

const setUserType = (type) => {
  userType.value = type;
  credentials.value = { id: '', password: '' };
};


const login = async () => { // 建议将 login 函数设为 async 以便处理异步API调用
  try {
    // TODO: 在这里添加您实际的异步登录验证逻辑
    // 例如: const response = await YourAuthService.login(userType.value, credentials.value);
    // 假设登录成功，并且您从后端获取了 token 和确认的用户角色
    // const token = response.data.token;
    // const actualUserRole = response.data.role; // 从后端获取的真实角色

    // ---- 模拟登录成功和获取角色 ----
    const token = 'mock-token-for-' + userType.value; // 示例 token
    const actualUserRole = userType.value; // 在此示例中，我们假设前端选择的角色就是后端验证后的角色
    // ---- 模拟结束 ----

    // 存储认证信息到 localStorage (与导航守卫中的逻辑对应)
    localStorage.setItem('userToken', token);
    localStorage.setItem('userRole', actualUserRole);
    // console.log(`Logging in as ${userType.value} with ID: ${credentials.value.id} and Password: ${credentials.value.password}`)
    const userInfo = {
      role: userType.value,
      userId: credentials.value.id
    }
    localStorage.setItem('user', JSON.stringify(userInfo))
    // 如果有userId, 也一并存储
    // localStorage.setItem('userId', response.data.userId);


    // 检查是否有重定向路径
    const redirectPath = route.query.redirect;

    if (redirectPath && typeof redirectPath === 'string') {
      // 如果有重定向路径，则跳转到该路径
      // 使用 router.replace 可以避免登录页成为历史记录的一部分
      router.replace(redirectPath);
    } else {
      // 没有重定向路径，则根据角色跳转到默认的仪表盘
      if (actualUserRole === 'student') {
        router.replace({ name: 'StudentHome' }); // 确保 'StudentHome' 是学生仪表盘的正确路由名称
      } else if (actualUserRole === 'teacher') {
        router.replace({ name: 'TeacherHome' }); // 确保 'TeacherHome' 是教师仪表盘的正确路由名称
      } else {
        // 未知角色或默认情况，可以跳转到根路径或错误页
        router.replace('/');
      }
    }
  } catch (error) {
    console.error("登录失败:", error);
    // 在这里处理登录失败的情况，例如显示错误提示
    // alert('登录失败：' + (error.response?.data?.message || error.message || '请检查您的凭据。'));
  }
};
</script>
<style scoped>
.login {
  padding: 20px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--background-light), #e3f2fd);
}

h1 {
  color: var(--text-color);
  font-size: 2.5em;
  margin-bottom: 2rem;
  text-align: center;
  font-weight: 600;
  letter-spacing: 1px;
}

.login-container {
  width: 100%;
  max-width: 420px;
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: var(--shadow-lg);
  transition: var(--transition-base);
}

.login-container:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.user-type {
  display: flex;
  margin-bottom: 2rem;
  gap: 1rem;
}

.type-btn {
  flex: 1;
  padding: 0.8rem;
  background-color: var(--background-light);
  color: var(--text-color);
  font-weight: 500;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition-base);
}

.type-btn:hover {
  background-color: var(--background-light);
  transform: translateY(-1px);
}

.type-btn.active {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333333;
  font-size: 0.95rem;
}

.form-group input {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 1rem;
  transition: var(--transition-base);
  background-color: white;
  color: var(--text-color);
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
  background-color: white;
}

.form-group input::placeholder {
  color: #9e9e9e;
  font-weight: 400;
}

.submit-btn {
  width: 100%;
  padding: 0.8rem;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition-base);
  margin-top: 1rem;
}

.submit-btn:hover {
  background-color: #357abd;
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.submit-btn:active {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

@media (max-width: 480px) {
  .login-container {
    padding: 1.5rem;
  }

  h1 {
    font-size: 2em;
  }

  .type-btn {
    padding: 0.6rem;
  }
}
</style>
