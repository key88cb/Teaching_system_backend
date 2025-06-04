<template>
  <div class="page-wrapper">
    <h1 class="page-title">教师控制台</h1>
    <p class="welcome-text">欢迎回来，{{ username }}，Do you have anything to tell me?</p>

    <div class="actions">
      <div class="action-card" @click="navigateTo('/teacher/question-bank')">
        <div class="card-icon">📚</div>
        <h3 class="section-title">题库管理</h3>
        <p class="card-description">添加、编辑和删除试题</p>
      </div>

      <div class="action-card" @click="navigateTo('/teacher/create-paper')">
        <div class="card-icon">📝</div>
        <h3 class="section-title">编辑发布试卷</h3>
        <p class="card-description">手动或自动组卷并发布考试</p>
      </div>

      <div class="action-card" @click="navigateTo('/teacher/past-papers')">
        <div class="card-icon">📋</div>
        <h3 class="section-title">查看历年卷</h3>
        <p class="card-description">查看所有历史试卷及答案</p>
      </div>

      <div class="action-card" @click="navigateTo('/teacher/exam-management')">
        <div class="card-icon">📊</div>
        <h3 class="section-title">考试情况管理</h3>
        <p class="card-description">查看已发布考试及学生答题情况</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('王老师')

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.welcome-text {
  color: var(--text-light);
  font-size: 1.1rem;
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-md);
  background-color: white;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
}

.actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-lg);
  margin-top: var(--spacing-lg);
}

.action-card {
  background: white;
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  border: 1px solid var(--border-color);
  transition: var(--transition-base);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.card-icon {
  font-size: 2.5rem;
  margin-bottom: var(--spacing-md);
}

.section-title {
  color: var(--text-color);
  font-size: 1.25rem;
  margin: var(--spacing-sm) 0;
}

.card-description {
  color: var(--text-light);
  font-size: 0.95rem;
  line-height: 1.5;
  margin: 0;
}

@media (max-width: 768px) {
  .actions {
    grid-template-columns: 1fr;
  }
  
  .action-card {
    padding: var(--spacing-md);
  }
  
  .card-icon {
    font-size: 2rem;
  }
}
</style>
