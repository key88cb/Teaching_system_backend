<template>
  <div class="page-wrapper">
    <h1 class="page-title">创建试卷</h1>
    <p class="welcome-text">请选择您想要的出卷方式</p>

    <div class="option-blocks">
      <div class="option-block" @click="goToManualPaper">
        <div class="card-icon">✍️</div>
        <h3 class="section-title">手动出卷</h3>
        <p class="card-description">自主选择题目组成试卷</p>
      </div>

      <div class="option-block" @click="goToAutoPaper">
        <div class="card-icon">🤖</div>
        <h3 class="section-title">自动出卷</h3>
        <p class="card-description">系统自动选择题目生成试卷</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToManualPaper = () => {
  router.push('/teacher/create-paper/manual')
}

const goToAutoPaper = () => {
  router.push('/teacher/create-paper/auto')
}
</script>

<style scoped>
.page-wrapper {
  padding: var(--spacing-lg);
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  color: var(--text-color);
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0;
  text-align: center;
}

.welcome-text {
  color: var(--text-light);
  font-size: 1.1rem;
  margin: var(--spacing-md) 0 var(--spacing-xl);
  text-align: center;
}

.option-blocks {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-xl);
  margin-top: var(--spacing-lg);
  padding: 0 var(--spacing-md);
}

.option-block {
  background: white;
  padding: var(--spacing-xl);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  border: 1px solid var(--border-color);
  transition: var(--transition-base);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.option-block::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-color);
  opacity: 0;
  transition: var(--transition-base);
}

.option-block:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.option-block:hover::before {
  opacity: 1;
}

.card-icon {
  font-size: 3rem;
  margin-bottom: var(--spacing-md);
}

.section-title {
  color: var(--text-color);
  font-size: 1.4rem;
  margin: var(--spacing-sm) 0;
  font-weight: 600;
}

.card-description {
  color: var(--text-light);
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: var(--spacing-lg);
}

.feature-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  justify-content: center;
  margin-top: auto;
}

.feature-tag {
  background-color: var(--primary-color);
  color: var(--primary-color);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-sm);
  font-size: 0.9rem;
  font-weight: 500;
}

@media (max-width: 768px) {
  .option-blocks {
    grid-template-columns: 1fr;
    padding: 0;
  }
  
  .option-block {
    padding: var(--spacing-lg);
  }
  
  .card-icon {
    font-size: 2.5rem;
  }
  
  .section-title {
    font-size: 1.3rem;
  }
  
  .card-description {
    font-size: 0.95rem;
  }
}
</style>
