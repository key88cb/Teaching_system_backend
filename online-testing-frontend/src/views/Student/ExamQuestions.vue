<template>
  <div class="exam-taking-page">
    <div class="exam-sticky-header">
      <div class="exam-title-bar">
        <h1 class="exam-name">{{ paperInfo?.paperName || '在线考试' }}</h1>
        <div class="timer-display">
          <i class="icon-clock"></i> 剩余时间：<strong>{{ formatTime(remainingTime) }}</strong>
        </div>
      </div>
    </div>

    <div class="exam-content-container">
      <div v-if="isLoading" class="loading-indicator">
        <p>正在加载试卷题目...</p>
      </div>
      <div v-else-if="paperInfo && paperInfo.paperQuestions && paperInfo.paperQuestions.length > 0" class="questions-area">
        <div v-for="(question, index) in paperInfo.paperQuestions" :key="question.questionId" class="question-display-card">
          <h3 class="question-text-content">第 {{ index + 1 }} 题：{{ question.questionText }}</h3>

          <div class="options-group styled-form-controls">
            <div
                v-for="(option, optionIndex) in getQuestionOptionsForDisplay(question)"
                :key="option.value"
                class="option-item"
                :class="{ 'is-selected': isOptionSelected(question, option.value) }"
                @click="selectInteractiveOption(question, option.value)"
            >
              <template v-if="question.questionType === 'Single Choice' || question.questionType === 'True/False' || question.questionType === '单选题' || question.questionType === '判断题'">
                <input
                    type="radio"
                    :id="`q${question.questionId}-opt${option.value}`"
                    :name="'question-' + question.questionId"
                    :value="option.value"
                    v-model="studentAnswers[question.questionId]"
                />
                <label :for="`q${question.questionId}-opt${option.value}`" class="option-label-text">
                  <span class="option-letter">{{ option.value }}.</span> {{ option.label }}
                </label>
              </template>
              <template v-if="question.questionType === 'Multiple Choice' || question.questionType === '多选题'">
                <input
                    type="checkbox"
                    :id="`q${question.questionId}-opt${option.value}`"
                    :value="option.value"
                    :checked="isOptionSelected(question, option.value)"
                    @change="handleMultiChoiceChange(question.questionId, option.value)"
                />
                <label :for="`q${question.questionId}-opt${option.value}`" class="option-label-text">
                  <span class="option-letter">{{ option.value }}.</span> {{ option.label }}
                </label>
              </template>
            </div>
            <div v-if="getQuestionOptionsForDisplay(question).length === 0" class="no-options-info">
              此题目选项未能正确加载。
            </div>
          </div>
        </div>
      </div>
      <div v-else class="no-questions-message">
        <p>试卷题目加载失败或暂无题目。</p>
      </div>
    </div>

    <div class="exam-footer-actions">
      <button class="btn primary-btn submit-exam-btn" @click="confirmAndSubmitExam" :disabled="isSubmitting || isLoading">
        <i class="icon-submit-paper"></i> {{ isSubmitting ? '正在提交...' : '提交答卷' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'; // 引入 watch
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const url_front = 'http://localhost:8080/';

const isLoading = ref(true);
const isSubmitting = ref(false);
const paperInfo = ref(null);
const studentAnswers = ref({});
const remainingTime = ref(0);
let timer = null;
const startTime = ref();

const QUESTION_TYPE_MAP_TO_CHINESE = {
  'Single Choice': '单选题',
  'Multiple Choice': '多选题',
  'True/False': '判断题',
  '单选题': '单选题',
  '多选题': '多选题',
  '判断题': '判断题',
  'unknown': '未知题型'
};
const getDisplayQuestionType = (backendType) => {
  if (!backendType) return QUESTION_TYPE_MAP_TO_CHINESE['unknown'];
  return QUESTION_TYPE_MAP_TO_CHINESE[backendType] || backendType;
};

const formatTime = (totalSeconds) => {
  if (typeof totalSeconds !== 'number' || totalSeconds < 0) return '00:00';
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
};

const getAnswersStorageKey = (paperId, courseId) => {
  return `examAnswers-${paperId}-${courseId}`;
};

const saveAnswersToLocalStorage = () => {
  if (paperInfo.value && paperInfo.value.paperId && paperInfo.value.courseId) {
    const key = getAnswersStorageKey(paperInfo.value.paperId, paperInfo.value.courseId);
    localStorage.setItem(key, JSON.stringify(studentAnswers.value));
    console.log('答案已保存到localStorage:', studentAnswers.value);
  }
};

// 新增：从 localStorage 加载答案
const loadAnswersFromLocalStorage = () => {
  if (paperInfo.value && paperInfo.value.paperId && paperInfo.value.courseId) {
    const key = getAnswersStorageKey(paperInfo.value.paperId, paperInfo.value.courseId);
    const savedAnswers = localStorage.getItem(key);
    if (savedAnswers) {
      try {
        const parsedAnswers = JSON.parse(savedAnswers);
        // 合并，确保所有题目都有初始值，即使localStorage中没有
        const initialAnswers = {};
        paperInfo.value.paperQuestions.forEach(q => {
          const type = q.questionType;
          initialAnswers[q.questionId] = (type === 'Multiple Choice' || type === '多选题') ? [] : '';
        });
        studentAnswers.value = { ...initialAnswers, ...parsedAnswers };
        console.log('从localStorage加载答案:', studentAnswers.value);
      } catch (e) {
        console.error('解析localStorage中的答案失败:', e);
        // 如果解析失败，则使用默认空答案初始化
        initializeEmptyAnswers();
      }
    } else {
      // 如果localStorage中没有保存的答案，则使用默认空答案初始化
      initializeEmptyAnswers();
    }
  } else {
    // 如果试卷信息不完整，也使用默认空答案初始化
    initializeEmptyAnswers();
  }
};

// 新增：初始化空答案的辅助函数
const initializeEmptyAnswers = () => {
  const initAnswers = {};
  if (paperInfo.value && paperInfo.value.paperQuestions) {
    paperInfo.value.paperQuestions.forEach(q => {
      const type = q.questionType;
      initAnswers[q.questionId] = (type === 'Multiple Choice' || type === '多选题') ? [] : '';
    });
  }
  studentAnswers.value = initAnswers;
};


let startCountdown = () => {};
startCountdown = () => {
  if (!paperInfo.value || !paperInfo.value.closeTime) {
    console.error('无法开始倒计时：closeTime 未定义。');
    remainingTime.value = 0;
    return;
  }

  const closeTimeDate = new Date(paperInfo.value.closeTime);
  if (isNaN(closeTimeDate.getTime())) {
    console.error('倒计时错误：无效的考试结束时间。', paperInfo.value.closeTime);
    remainingTime.value = 0;
    return;
  }

  const updateRemainingTime = () => {
    const now = new Date();
    const diffSeconds = Math.max(0, Math.floor((closeTimeDate.getTime() - now.getTime()) / 1000));
    remainingTime.value = diffSeconds;

    if (diffSeconds <= 0) {
      clearInterval(timer);
      if (!isSubmitting.value) {
        alert('考试时间已到，系统将自动提交您的答卷。');
        submitExam();
      }
    }
  };

  updateRemainingTime();
  if (timer) clearInterval(timer);
  timer = setInterval(updateRemainingTime, 1000);
};


const fetchPaperQuestions = async (paperId, courseId) => {
  isLoading.value = true;
  try {
    const params = new URLSearchParams({ courseId: String(courseId), paperId: String(paperId) });
    const url = url_front+`api/paper-questions/query-paper-and-questions?${params}`;
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`获取试卷失败 (${response.status})`);
    }
    const data = await response.json();
    const questions = Array.isArray(data.paperQuestions) ?
        data.paperQuestions.map(q => ({
          ...q,
          options: Array.isArray(q.options) ? q.options :
              (q.optionA !== undefined ? [q.optionA, q.optionB, q.optionC, q.optionD].filter(opt => opt !== undefined && opt !== null) : [])
        }))
        : [];

    paperInfo.value = {
      ...data,
      paperName: data.paperName || '在线考试',
      paperQuestions: questions
    };

    // 试卷数据获取成功后，加载localStorage中的答案
    loadAnswersFromLocalStorage();

    console.log('成功加载试卷:', paperInfo.value);
    startCountdown();

  } catch (error) {
    alert(`无法加载试卷：${error.message}`);
    console.error(error);
    paperInfo.value = { paperName: '加载失败', paperQuestions: [], closeTime: new Date().toISOString() };
    initializeEmptyAnswers(); // 确保在加载失败时也有答案结构
  } finally {
    isLoading.value = false;
  }
};

onMounted(async () => {
  const paperId = parseInt(route.params.paperId);
  const courseId = parseInt(route.params.courseId);
  if (isNaN(paperId) || isNaN(courseId)) {
    alert("试卷参数无效！");
    router.push('/student/dashboard');
    return;
  }
  // startTime 逻辑
  if (!startTime.value) {
    if(!localStorage.getItem(`startTime-${paperId}-${courseId}`)){
      startTime.value = getCurrentTime();
      localStorage.setItem(`startTime-${paperId}-${courseId}`, startTime.value);
    }
    else{
      startTime.value = localStorage.getItem(`startTime-${paperId}-${courseId}`);
    }
    console.log("Start time set:", startTime.value);
  }
  await fetchPaperQuestions(paperId, courseId); // fetchPaperQuestions内部会调用loadAnswers
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
  // saveAnswersToLocalStorage(); // 但要注意，如果是因为提交成功而卸载，则不应保存
});
let getCurrentTime=()=>{  };
getCurrentTime = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');
  const formattedTime = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  return formattedTime;
};

let getQuestionOptionsForDisplay = (question) => {  };
getQuestionOptionsForDisplay = (question) => {
  const letters = ['A', 'B', 'C', 'D', 'E', 'F'];
  let labelsToMap = [];

  if (Array.isArray(question.options) && question.options.every(opt => typeof opt === 'string')) {
    labelsToMap = question.options;
    if (question.questionType === 'True/False' || question.questionType === '判断题') {
      const trueFalseDefaults = ['正确', '错误'];
      labelsToMap = [
        labelsToMap[0] !== undefined ? labelsToMap[0] : trueFalseDefaults[0],
        labelsToMap[1] !== undefined ? labelsToMap[1] : trueFalseDefaults[1]
      ].slice(0,2);
    }
  }
  else {
    optionLettersLoop: for (const letter of letters) {
      if (question[`option${letter}`] !== undefined && question[`option${letter}`] !== null) {
        labelsToMap.push(question[`option${letter}`]);
      } else {
        // if (['A','B','C','D'].includes(letter)) { }
      }
    }
    if (question.questionType === 'True/False' || question.questionType === '判断题') {
      if(labelsToMap.length < 2) {
        labelsToMap = [labelsToMap[0] || '正确', labelsToMap[1] || '错误'];
      }
      labelsToMap = labelsToMap.slice(0,2);
    }
  }

  return labelsToMap.slice(0, letters.length).map((label, index) => ({
    value: letters[index],
    label: label
  }));
};


const handleMultiChoiceChange = (questionId, optionValue) => {
  const currentAnswers = studentAnswers.value[questionId];
  if (Array.isArray(currentAnswers)) {
    const index = currentAnswers.indexOf(optionValue);
    if (index === -1) {
      currentAnswers.push(optionValue);
    } else {
      currentAnswers.splice(index, 1);
    }
  }
  // studentAnswers.value[questionId] = currentAnswers; // 不需要这行，因为数组是响应式的
};

let isOptionSelected = (question, optionValue) => {  };
isOptionSelected = (question, optionValue) => {
  const answer = studentAnswers.value[question.questionId];
  const type = question.questionType;
  if (type === 'Multiple Choice' || type === '多选题') {
    return Array.isArray(answer) && answer.includes(optionValue);
  }
  return answer === optionValue;
};


const selectInteractiveOption = (question, optionValue) => {
  const questionId = question.questionId;
  const type = question.questionType;
  if (type === 'Multiple Choice' || type === '多选题') {
    handleMultiChoiceChange(questionId, optionValue);
  } else {
    studentAnswers.value[questionId] = optionValue;
  }
  // saveAnswersToLocalStorage(); // 答案改变后立即保存 - 移动到 watcher 中
};

watch(studentAnswers, (newAnswers) => {
  if (paperInfo.value && paperInfo.value.paperId && paperInfo.value.courseId && !isLoading.value) { // 确保试卷信息已加载
    saveAnswersToLocalStorage();
  }
}, { deep: true }); // 使用 deep watcher 来检测嵌套对象/数组的变化

let confirmAndSubmitExam = () => { /* ... (保持不变) ... */ };
confirmAndSubmitExam = () => {
  if(window.confirm('您确定要提交答卷吗？提交后将无法修改。')) {
    submitExam();
  }
};


const submitExam = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;
  clearInterval(timer);

  const studentId = 123; // TODO: 替换为实际学生ID

  if (!paperInfo.value || !paperInfo.value.paperId || !paperInfo.value.courseId) {
    alert("试卷信息不完整，无法提交。");
    isSubmitting.value = false;
    return;
  }

  const answersPayload = Object.entries(studentAnswers.value).map(([questionIdStr, answer]) => {
    const questionId = parseInt(questionIdStr);
    const question = paperInfo.value.paperQuestions.find(q => q.questionId === questionId);
    let submittedAnswer = '';

    if (question) {
      const type = question.questionType;
      if (type === 'Multiple Choice' || type === '多选题') {
        submittedAnswer = Array.isArray(answer) ? answer.sort().join('') : '';
      } else {
        submittedAnswer = answer || '';
      }
    }
    return { questionId: questionId, answer: submittedAnswer };
  });

  const dto = {
    paperId: paperInfo.value.paperId,
    courseId: paperInfo.value.courseId,
    studentId: studentId,
    answers: answersPayload,
    startTime:startTime.value,
    finishTime:getCurrentTime()
  };

  console.log("Submitting DTO:", dto);
  try {
    const res = await fetch(url_front+'api/exam/submit-exam', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dto)
    });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
      throw new Error(errorData.message || `提交失败 (${res.status})`);
    }
    alert('答卷提交成功！');
    // 新增：提交成功后清除 localStorage 中的答案
    if (paperInfo.value && paperInfo.value.paperId && paperInfo.value.courseId) {
      const key = getAnswersStorageKey(paperInfo.value.paperId, paperInfo.value.courseId);
      localStorage.removeItem(key);
      localStorage.removeItem(`startTime-${paperInfo.value.paperId}-${paperInfo.value.courseId}`); // 也清除开始时间
      console.log('localStorage已清除');
    }
    await router.push('/student/dashboard');
  } catch (error) {
    alert(`提交失败：${error.message}，请稍后重试。`);
    console.error(error);
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
/* --- (样式保持不变) --- */
.exam-taking-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f0f3f7; /* Light, neutral background */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #343a40;
}

.exam-sticky-header {
  background-color: #fff;
  padding: 15px 35px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #e0e0e0;
}
.exam-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}
.exam-name {
  font-size: 1.6em;
  color: #2c3e50;
  font-weight: 600;
  margin: 0;
  text-align: left;
}
.timer-display {
  font-size: 1.1em;
  color: #007bff;
  font-weight: 500;
  background-color: #e7f3ff;
  padding: 8px 15px;
  border-radius: 20px;
  display: flex;
  align-items: center;
}
.timer-display i {
  margin-right: 8px;
  font-size: 1.2em;
}
.timer-display strong {
  font-variant-numeric: tabular-nums;
}

.exam-content-container {
  padding: 25px 30px; /* Adjusted padding */
  flex-grow: 1;
  max-width: 860px; /* Optimal reading width for questions */
  width: 100%;
  margin: 20px auto 0 auto;
}

/* --- 题目卡片 --- */
.questions-area {
  /* Styles for the container of all questions if needed */
}
.question-display-card {
  background: white;
  padding: 25px 30px; /* Increased padding */
  border-radius: 12px; /* Softer radius */
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06); /* Softer shadow */
  margin-bottom: 25px;
  border: 1px solid #e9ecef; /* Subtle border */
}
.question-card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 18px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}
.question-index-badge {
  background-color: #007bff;
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.9em;
  font-weight: 600;
  flex-shrink: 0;
}
.question-type-display {
  font-size: 0.85em; /* Smaller type display */
  color: #6c757d;
  font-weight: 500;
  background-color: #e9ecef;
  padding: 4px 10px;
  border-radius: 15px;
  flex-shrink: 0;
}
.question-points-display {
  font-size: 0.9em;
  color: #28a745;
  font-weight: 500;
  margin-left: auto;
  flex-shrink: 0;
}

.question-text-content {
  font-size: 1.15em; /* Clear question text size */
  font-weight: 500;
  color: #212529;
  line-height: 1.65; /* Improved line height */
  margin: 0 0 25px 0; /* Increased margin below question text */
  padding-left: 5px; /* Slight indent */
}

/* --- Options Styling --- */
.options-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.option-item { /* This div is now clickable */
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
  background-color: #fdfdff;
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
}
.option-item:hover {
  border-color: #a0cfff; /* Light blue border on hover */
  background-color: #f8fcff; /* Very light blue background on hover */
}
.option-item.is-selected {
  border-color: #007bff;
  background-color: #e7f3ff; /* Light blue for selected */
  box-shadow: 0 0 0 2px rgba(0,123,255,.25); /* Focus-like ring for selected */
}

/* Custom Radio/Checkbox Styles */
.styled-form-controls input[type="radio"],
.styled-form-controls input[type="checkbox"] {
  appearance: none;
  -webkit-appearance: none;
  width: 20px;
  height: 20px;
  border: 2px solid #adb5bd;
  border-radius: 50%;
  margin-right: 12px;
  cursor: pointer;
  position: relative;
  flex-shrink: 0;
  transition: background-color 0.15s ease-in-out, border-color 0.15s ease-in-out;
}
.styled-form-controls input[type="checkbox"] {
  border-radius: 4px;
}

.styled-form-controls input[type="radio"]:checked,
.styled-form-controls input[type="checkbox"]:checked {
  background-color: #007bff;
  border-color: #007bff;
}
.styled-form-controls input[type="radio"]:checked::before {
  content: ''; display: block; width: 10px; height: 10px;
  background-color: white; border-radius: 50%;
  position: absolute; top: 50%; left: 50%;
  transform: translate(-50%, -50%);
}
.styled-form-controls input[type="checkbox"]:checked::before {
  content: '✓'; display: block; color: white;
  font-size: 14px; font-weight: bold; text-align: center;
  line-height: 17px; /* Adjusted for 18px box (20px - 2*1px border) */
}
.styled-form-controls input[type="radio"]:focus,
.styled-form-controls input[type="checkbox"]:focus {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13,110,253,.25);
  outline: none;
}

.option-label-text {
  font-size: 1em;
  line-height: 1.5;
  color: #343a40;
  flex-grow: 1;
  user-select: none; /* Prevent text selection when clicking label */
}
.option-letter {
  font-weight: 600;
  margin-right: 8px;
  color: #0056b3; /* Darker blue for letter */
}

/* --- Footer Actions --- */
.exam-footer-actions {
  padding: 20px 35px;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.08);
  text-align: center;
  position: sticky;
  bottom: 0;
  z-index: 100;
  border-top: 1px solid #e0e0e0;
}
.submit-exam-btn.btn {
  padding: 12px 30px;
  font-size: 1.1em;
  min-width: 220px; /* Wider button */
}
.submit-exam-btn i {
  font-size: 1.2em;
}
.submit-exam-btn:disabled {
  background-color: #a0cfff !important; /* Lighter blue when disabled */
  border-color: #a0cfff !important;
  color: #f0f0f0 !important;
}


/* --- Loading/Empty States --- */
.loading-indicator, .no-questions-message {
  text-align: center;
  padding: 50px 20px;
  margin-top: 20px;
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  font-size: 1.1em;
  color: #6c757d;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.loading-indicator p, .no-questions-message p {
  margin: 0;
}
.no-options-info {
  font-style: italic;
  color: #888;
  padding: 10px 0;
  text-align: center;
}

/* --- 按钮通用样式 (copied from previous for consistency if needed) --- */
.btn {
  padding: 10px 18px; border: none; border-radius: 8px; cursor: pointer;
  font-size: 1em; font-weight: 500; transition: all 0.2s ease-in-out;
  display: inline-flex; align-items: center; justify-content: center;
  gap: 8px; text-decoration: none; line-height: 1.5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}
.btn:hover:not(:disabled) { opacity: 0.85; transform: translateY(-1px); box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.btn:active:not(:disabled) { transform: translateY(0); box-shadow: 0 2px 4px rgba(0,0,0,0.08); }
.btn.primary-btn { background-color: #007bff; color: white; }
.btn.primary-btn:hover:not(:disabled) { background-color: #0069d9; }


/* --- Responsive --- */
@media (max-width: 768px) {
  .exam-taking-page { padding-bottom: 80px; }
  .exam-sticky-header { padding: 10px 20px; }
  .exam-title-bar { flex-direction: column; align-items: flex-start; gap: 8px;}
  .exam-name { font-size: 1.3em; }
  .timer-display { font-size: 1em; padding: 6px 12px; align-self: flex-end; }

  .exam-content-container { padding: 20px 15px; margin-top: 15px;}
  .question-display-card { padding: 20px; }
  .question-card-header { flex-direction: column; align-items: flex-start; gap: 8px; padding-bottom: 10px; margin-bottom: 12px;}
  .question-index-badge { font-size: 0.85em; padding: 4px 10px;}
  .question-type-display { font-size: 0.85em; }
  .question-points-display { margin-left: 0; align-self: flex-start; margin-top: 5px;}
  .question-text-content { font-size: 1.05em; }
  .options-group { gap: 10px; }
  .option-item { padding: 10px 12px; }
  .styled-form-controls input[type="radio"],
  .styled-form-controls input[type="checkbox"] {
    width: 18px; height: 18px; margin-right: 10px;
  }
  .styled-form-controls input[type="checkbox"]:checked::before { font-size: 12px; line-height: 16px;}
  .styled-form-controls input[type="radio"]:checked::before { width: 8px; height: 8px;}


  .exam-footer-actions { padding: 15px 20px; }
  .submit-exam-btn.btn { width: 100%; font-size: 1em; padding: 12px 20px; }
}

/* Placeholder Icons (replace with your actual icon solution) */
.icon-clock::before { content: "⏱️"; }
</style>