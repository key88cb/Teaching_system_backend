<template>
  <div class="auto-create-paper">
    <h1>自动组卷</h1>

    <div class="paper-form-container">
      <div class="paper-form">
        <div class="form-section">
          <h3 class="section-title">试卷基本信息</h3>
          <div class="form-group">
            <label for="paper-title">试卷名称：</label>
            <input id="paper-title" type="text" v-model="paper.title" placeholder="请输入试卷名称" class="form-control" />
          </div>
          <div class="form-group">
            <label for="paper-course">课程ID：</label>
            <select id="paper-course" v-model="paper.course" class="form-control">
              <option value="">请选择课程ID</option>
              <option v-for="courseItem in courses" :key="courseItem" :value="courseItem">{{ courseItem }}</option>
            </select>
          </div>
          <div class="form-group">
            <label for="paper-creator">创建者：</label>
            <input id="paper-creator" type="text" v-model="paper.creator" placeholder="请输入创建者" class="form-control" />
          </div>
        </div>

        <div class="form-section">
          <h3 class="section-title">组卷配置</h3>
          <div class="form-group">
            <label>题型分布：</label>
            <div class="checkbox-group styled-checkbox-group">
              <label>
                <input type="checkbox" v-model="config.types" value="单选" @change="clearInputIfDisabled('singleChoice')" />
                <span class="checkbox-label">单选题</span>
              </label>
              <label>
                <input type="checkbox" v-model="config.types" value="多选" @change="clearInputIfDisabled('multiChoice')" />
                <span class="checkbox-label">多选题</span>
              </label>
              <label>
                <input type="checkbox" v-model="config.types" value="判断" @change="clearInputIfDisabled('judgment')" />
                <span class="checkbox-label">判断题</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label>每种题型数量：</label>
            <div class="quantity-inputs no-wrap-groups">
              <div class="input-unit-group" :class="{ 'field-disabled': !config.types.includes('单选') }">
                <span class="input-label">单选题：</span>
                <div class="input-with-unit">
                  <input type="number" class="form-control quantity-input" min="0" max="50" 
                         v-model.number="config.singleChoiceCount"
                         :disabled="!config.types.includes('单选')" />
                  <span class="unit">道</span>
                </div>
              </div>
              <div class="input-unit-group" :class="{ 'field-disabled': !config.types.includes('多选') }">
                <span class="input-label">多选题：</span>
                <div class="input-with-unit">
                  <input type="number" class="form-control quantity-input" min="0" max="50" v-model.number="config.multiChoiceCount"
                         :disabled="!config.types.includes('多选')" />
                  <span class="unit">道</span>
                </div>
              </div>
              <div class="input-unit-group" :class="{ 'field-disabled': !config.types.includes('判断') }">
                <span class="input-label">判断题：</span>
                <div class="input-with-unit">
                  <input type="number" class="form-control quantity-input" min="0" max="50" v-model.number="config.judgmentCount"
                         :disabled="!config.types.includes('判断')" />
                  <span class="unit">道</span>
                </div>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>每种题型单题分数：</label>
            <div class="quantity-inputs no-wrap-groups">
              <div class="input-unit-group" :class="{ 'field-disabled': !config.types.includes('单选') }">
                <span class="input-label">单选题：</span>
                <div class="input-with-unit">
                  <input type="number" class="form-control quantity-input" min="1" max="100" v-model.number="config.singleChoiceScore" :disabled="!config.types.includes('单选')" />
                  <span class="unit">分</span>
                </div>
              </div>
              <div class="input-unit-group" :class="{ 'field-disabled': !config.types.includes('多选') }">
                <span class="input-label">多选题：</span>
                <div class="input-with-unit">
                  <input type="number" class="form-control quantity-input" min="1" max="100" v-model.number="config.multiChoiceScore" :disabled="!config.types.includes('多选')" />
                  <span class="unit">分</span>
                </div>
              </div>
              <div class="input-unit-group" :class="{ 'field-disabled': !config.types.includes('判断') }">
                <span class="input-label">判断题：</span>
                <div class="input-with-unit">
                  <input type="number" class="form-control quantity-input" min="1" max="100" v-model.number="config.judgmentScore" :disabled="!config.types.includes('判断')" />
                  <span class="unit">分</span>
                </div>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label for="topics-input">考点范围 (可选)：</label>
            <input id="topics-input" type="text" v-model="config.topics" placeholder="输入考点关键词，用英文逗号分隔" class="form-control" />
          </div>
        </div>

        <div class="form-actions">
          <button class="btn primary-btn preview-btn" @click="previewPaper">
            <i class="icon-eye"></i> 生成并预览试卷
          </button>
        </div>
      </div>
    </div>

    <div v-if="showPreviewModal" class="modal-overlay" @click.self="closePreviewModal">
      <div class="modal-dialog">
        <div class="modal-header">
          <h2 class="modal-title">试卷预览 - {{ paper.title }}</h2>
          <button class="modal-close-btn" @click="closePreviewModal" aria-label="关闭模态框">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="isLoadingPreview" class="loading-indicator">
            <p>正在生成预览，请稍候...</p>
          </div>
          <div v-else class="preview-questions">
            <div v-for="(question, index) in previewQuestions" :key="question.questionId || question.id" class="question-preview">
              <div class="question-header">
                <span class="type-badge" :class="`type-${(question.questionTypeOriginal || question.questionType || 'unknown').toLowerCase().replace(/[\s/]+/g, '_')}`">
                  {{ getDisplayQuestionType(question.questionTypeOriginal || question.questionType) }}
                </span>
                <span class="question-number">{{ index + 1 }}. {{ question.questionText }}</span>
                <span class="score">{{ question.points }}分</span>
              </div>
              <div class="options">
                <div
                    v-for="(option, optionIndex) in question.options"
                    :key="option.optionIdValue || option.value || optionIndex"
                    :class="['option', { correct: option.isCorrect || (typeof question.correctAnswer === 'string' && question.correctAnswer.includes(String(option.optionIdValue || option.value))) }]"
                >
                  {{ String.fromCharCode(65 + optionIndex) }}. {{ option.optionText || option.label }}
                </div>
              </div>
            </div>
            <div v-if="!isLoadingPreview && previewQuestions.length === 0" class="no-questions">
              未能根据当前配置生成题目，请调整配置或检查题库。
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <div class="total-score" style="margin-right: auto; align-self: center;">
            总分：{{ totalScore }}分
          </div>
          <button class="btn secondary-btn" @click="closePreviewModal">关闭</button>
          <button class="btn primary-outline-btn generate-btn" @click="generateNewPaper">
            <i class="icon-refresh"></i> 重新生成
          </button>
          <button class="btn primary-btn publish-btn" @click="goToPublish">
            <i class="icon-send"></i> 去发布
          </button>
        </div>
      </div>
    </div>

    <div v-if="showPublishModal" class="modal-overlay" @click.self="cancelPublish">
      <div class="modal-dialog">
        <div class="modal-header">
          <h2 class="modal-title">发布考试</h2>
          <button class="modal-close-btn" @click="cancelPublish" aria-label="关闭模态框">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="exam-start-time">考试开始时间：</label>
            <input id="exam-start-time" type="datetime-local" v-model="examSettings.startTime" class="form-control" />
          </div>
          <div class="form-group">
            <label for="exam-end-time">考试结束时间：</label>
            <input id="exam-end-time" type="datetime-local" v-model="examSettings.endTime" class="form-control" />
          </div>
          <div class="form-group">
            <label for="exam-total-score">试卷总分：</label>
            <input id="exam-total-score" type="number" min="0" v-model.number="examSettings.fullScore" class="form-control" disabled />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn secondary-btn" @click="cancelPublish">取消</button>
          <button class="btn primary-btn" @click="confirmPublish">
            <i class="icon-check"></i> 确认发布
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter,useRoute } from 'vue-router';

const router = useRouter();
const url_front = 'http://localhost:8080/';

const QUESTION_TYPE_MAP_TO_CHINESE = {
  'Single Choice': '单选',
  'Multiple Choice': '多选',
  'True/False': '判断',
  'SINGLE_CHOICE': '单选',
  'MULTIPLE_CHOICE': '多选',
  'TRUE_FALSE': '判断',
  '单选': '单选',
  '多选': '多选',
  '判断': '判断',
  'unknown': '未知类型',
  'unknown (unresolved)': '未知类型'
};

const getDisplayQuestionType = (backendType) => {
  if (!backendType) return QUESTION_TYPE_MAP_TO_CHINESE['unknown'];
  const mapped = QUESTION_TYPE_MAP_TO_CHINESE[backendType];
  return mapped || backendType;
};

const paper = ref({
  title: '',
  course: '',
  creator: '',
  questions: []
});

const examSettings = ref({
  startTime: '',
  endTime: '',
  fullScore: 0,
});

const confirmedQuestions = ref([]);
const isLoadingPreview = ref(false);

const config = ref({
  types: ['单选'],
  singleChoiceCount: 5,
  multiChoiceCount: 3,
  judgmentCount: 2,
  topics: '',
  singleChoiceScore: 2,
  multiChoiceScore: 5,
  judgmentScore: 1
});

// --- MODIFICATION: Function to clear inputs when checkbox is unchecked ---
const clearInputIfDisabled = (typeKey) => {
  let typeChinese = '';
  let countField = '';
  let scoreField = '';

  if (typeKey === 'singleChoice') {
    typeChinese = '单选';
    countField = 'singleChoiceCount';
    scoreField = 'singleChoiceScore';
  } else if (typeKey === 'multiChoice') {
    typeChinese = '多选';
    countField = 'multiChoiceCount';
    scoreField = 'multiChoiceScore';
  } else if (typeKey === 'judgment') {
    typeChinese = '判断';
    countField = 'judgmentCount';
    scoreField = 'judgmentScore';
  }

  if (!config.value.types.includes(typeChinese)) {
    config.value[countField] = 0; // Or null, or keep as is based on preference
    config.value[scoreField] = 1; // Or null, or keep as is
  }
};
// --- End of MODIFICATION ---


const showPreviewModal = ref(false);
const showPublishModal = ref(false);

const courses = ref(['操作系统原理', '数据库基础', '计算机网络', '数据结构与算法', '软件工程导论']);

const getQuestionScore = (question) => {
  const type = question?.questionTypeOriginal || question?.questionType;
  if (!type) return 0;
  if (type.includes('Single') || type.includes('单选')) return config.value.singleChoiceScore;
  if (type.includes('Multiple') || type.includes('多选')) return config.value.multiChoiceScore;
  if (type.includes('True/False') || type.includes('判断')) return config.value.judgmentScore;
  return 0;
};

const previewQuestions = computed(() => {
  return paper.value.questions.map(q => ({
    ...q,
    points: getQuestionScore(q)
  }));
});

const totalScore = computed(() => {
  return paper.value.questions.reduce((sum, q) => sum + getQuestionScore(q), 0);
});

async function fetchAndSelectQuestions(questionTypeBackend, count, tagsArray) {
  if (count <= 0) return [];
  try {
    const url = new URL(url_front+'api/questions');
    url.searchParams.append('questionType', questionTypeBackend);
    if (tagsArray && tagsArray.length > 0) {
      tagsArray.forEach(tag => url.searchParams.append('tags', tag));
    }
    const res = await fetch(url.toString());
    if (!res.ok) {
      console.error(`Failed to fetch ${questionTypeBackend}: ${res.status}`);
      return [];
    }
    const data = await res.json();
    const shuffled = [...data].sort(() => 0.5 - Math.random());
    return shuffled.slice(0, count);
  } catch (error) {
    console.error(`Error fetching ${questionTypeBackend}:`, error);
    return [];
  }
}

async function generatePaperQuestions() {
  isLoadingPreview.value = true;
  paper.value.questions = [];
  let allSelectedQuestions = [];
  const topics = config.value.topics?.trim();
  const tagsArray = topics ? topics.split(',').map(t => t.trim()).filter(Boolean) : null;

  const typeConfigs = [
    { type: '单选', backendType: 'Single Choice', count: config.value.singleChoiceCount },
    { type: '多选', backendType: 'Multiple Choice', count: config.value.multiChoiceCount },
    { type: '判断', backendType: 'True/False', count: config.value.judgmentCount },
  ];

  for (const tc of typeConfigs) {
    if (config.value.types.includes(tc.type) && tc.count > 0) {
      const questionsForType = await fetchAndSelectQuestions(tc.backendType, tc.count, tagsArray);
      allSelectedQuestions.push(...questionsForType.map(q => ({...q, questionTypeOriginal: q.questionType })));
    }
  }

  paper.value.questions = allSelectedQuestions.map(question => ({
    ...question,
    options: (question.questionTypeOriginal === 'True/False' || question.questionType === 'True/False') && Array.isArray(question.options)
        ? question.options.slice(0, 2)
        : question.options,
    points: getQuestionScore(question)
  }));

  confirmedQuestions.value = [...paper.value.questions];
  examSettings.value.fullScore = totalScore.value;
  isLoadingPreview.value = false;
}

async function previewPaper() {
  if (!paper.value.title) { alert('请填写试卷名称'); return; }
  if (!paper.value.course) { alert('请选择课程ID'); return; }
  if (!paper.value.creator) { alert('请填写创建者'); return; }
  if (config.value.types.length === 0) { alert('请至少选择一种题型'); return; }
  let totalRequestedCount = 0;
  if(config.value.types.includes('单选')) totalRequestedCount += config.value.singleChoiceCount;
  if(config.value.types.includes('多选')) totalRequestedCount += config.value.multiChoiceCount;
  if(config.value.types.includes('判断')) totalRequestedCount += config.value.judgmentCount;

  if(totalRequestedCount === 0) {
    alert('请至少为选中的题型设置题目数量。');
    return;
  }
  await generatePaperQuestions();
  if (paper.value.questions.length < totalRequestedCount && paper.value.questions.length > 0) { // Only show if some questions were generated but less than requested
    alert('未能获取足够数量的题目，实际生成题目数量可能少于请求数量。请检查题库或调整筛选条件。');
  }
  if (paper.value.questions.length === 0 && totalRequestedCount > 0) {
    alert('未能根据当前配置生成任何题目，请调整配置或检查题库。');
    return;
  }
  showPreviewModal.value = true;
}

function closePreviewModal() {
  showPreviewModal.value = false;
}

async function generateNewPaper() {
  if (showPreviewModal.value) closePreviewModal();
  await previewPaper();
}

function goToPublish() {
  if (paper.value.questions.length === 0) {
    alert('试卷中没有题目，无法发布。');
    return;
  }
  examSettings.value.fullScore = totalScore.value;
  if (examSettings.value.fullScore <= 0 && paper.value.questions.length > 0) {
    if(!window.confirm('试卷总分目前为0或所有题目分数未设置，确定要继续发布吗？')) {
      showPreviewModal.value && closePreviewModal();
      return;
    }
  }
  closePreviewModal();
  showPublishModal.value = true;
}

async function confirmPublish() {
  if (!examSettings.value.startTime || !examSettings.value.endTime) {
    alert('请填写完整的考试时间'); return;
  }
  if (new Date(examSettings.value.startTime) >= new Date(examSettings.value.endTime)) {
    alert('结束时间必须大于开始时间'); return;
  }
  if (paper.value.questions.length === 0) {
    alert('试卷中没有题目，无法发布。'); return;
  }
  examSettings.value.fullScore = totalScore.value;
  if (examSettings.value.fullScore <= 0 && paper.value.questions.length > 0) {
    if(!window.confirm('试卷总分目前为0或所有题目分数未设置，确定要发布吗？')) {
      return;
    }
  }

  if (window.confirm('确认要发布这份试卷吗？')) {
    try {
      // 构建请求体
      console.log(examSettings.value.startTime)
      const payload = {
        courseId: getCourseIdFromName(paper.value.course),
        creator: paper.value.creator,
        openTime: examSettings.value.startTime,
        closeTime: examSettings.value.endTime,
        paperName: paper.value.title,
        totalScores: examSettings.value.fullScore,
        questionTypeConfigs: buildQuestionTypeConfigs(),
        questions: confirmedQuestions.value.map(q => ({
          questionId: q.questionId,
          points: getQuestionScore(q)
        }))
      };

      // 发布新试卷
      const res = await fetch(url_front+'api/paper-questions/auto-create-paper', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
        throw new Error(errorData.message || `HTTP error ${res.status}`);
      }

      alert('试卷发布成功！');
      showPublishModal.value = false;

      // 清除缓存并跳转
      localStorage.setItem('autoCreatePaper_lastStartTime', examSettings.value.startTime);
      localStorage.setItem('autoCreatePaper_lastEndTime', examSettings.value.endTime);

      // Reset form for next creation
      //paper.value = getInitialPaperData();
      config.value = { // Reset config to defaults
        types: ['单选'],
        singleChoiceCount: 5,
        multiChoiceCount: 3,
        judgmentCount: 2,
        topics: '',
        singleChoiceScore: 2,
        multiChoiceScore: 5,
        judgmentScore: 1
      };
      confirmedQuestions.value = [];
      // examSettings are kept from localStorage for next time

      router.push('/teacher/exam-management');
    } catch (error) {
      console.error('发布试卷失败:', error);
      alert(`发布试卷失败: ${error.message}`);
    }
  }
}

function getCourseIdFromName(courseName) {
  const courseMap = {
    '操作系统原理': 1,
    '数据库基础': 2,
    '计算机网络': 3,
    '数据结构与算法': 4,
    '软件工程导论': 5
  };
  return courseMap[courseName] || null;
}

function buildQuestionTypeConfigs() {
  const types = [];
  const topics = config.value.topics?.trim();
  const tagsArray = topics ? topics.split(',').map(t => t.trim()).filter(Boolean) : null;

  if (config.value.types.includes('单选') && config.value.singleChoiceCount > 0) {
    types.push({
      type: 'Single Choice',
      numberOfQuestions: config.value.singleChoiceCount,
      pointsPerQuestion: config.value.singleChoiceScore,
      tags: tagsArray
    });
  }
  if (config.value.types.includes('多选') && config.value.multiChoiceCount > 0) {
    types.push({
      type: 'Multiple Choice',
      numberOfQuestions: config.value.multiChoiceCount,
      pointsPerQuestion: config.value.multiChoiceScore,
      tags: tagsArray
    });
  }
  if (config.value.types.includes('判断') && config.value.judgmentCount > 0) {
    types.push({
      type: 'True/False',
      numberOfQuestions: config.value.judgmentCount,
      pointsPerQuestion: config.value.judgmentScore,
      tags: tagsArray
    });
  }
  return types;
}

function cancelPublish() {
  showPublishModal.value = false;
}

onMounted(() => {
  const lastStartTime = localStorage.getItem('autoCreatePaper_lastStartTime');
  const lastEndTime = localStorage.getItem('autoCreatePaper_lastEndTime');
  if (lastStartTime) examSettings.value.startTime = lastStartTime;
  if (lastEndTime) examSettings.value.endTime = lastEndTime;
});

</script>

<style scoped>
/* --- 全局与页面布局 --- */
.auto-create-paper {
  padding: 20px 30px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8f9fa;
  min-height: 100vh;
  color: #333;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  font-size: 2.2em;
  font-weight: 600;
}

.paper-form-container {
  max-width: 900px; /* MODIFICATION: 扩大总内容框 */
  margin: 0 auto;
}

.paper-form {
  background: white;
  padding: 30px 35px;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e9ecef;
}
.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}
.section-title {
  font-size: 1.3em;
  color: #007bff;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #007bff;
  display: inline-block;
}

.form-group {
  margin-bottom: 25px;
}
.form-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  color: #495057;
  font-size: 1em;
}

/* --- 输入控件样式 --- */
.form-control,
input[type="text"],
input[type="number"], /* Applied to quantity inputs too */
select {
  display: block;
  width: 100%;
  padding: 12px 18px;
  font-size: 1em;
  font-weight: 400;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  appearance: none;
  border-radius: 8px;
  transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out;
  box-sizing: border-box;
}
.form-control:focus,
input[type="text"]:focus,
input[type="number"]:focus,
select:focus {
  color: #495057;
  background-color: #fff;
  border-color: #86b7fe;
  outline: 0;
  box-shadow: 0 0 0 0.25rem rgba(13,110,253,.25);
}
select.form-control {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m2 5 6 6 6-6'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 16px 12px;
}

/* --- Checkbox Group --- */
.styled-checkbox-group {
  display: flex;
  gap: 25px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.styled-checkbox-group label {
  display: flex;
  align-items: center;
  font-weight: normal;
  cursor: pointer;
  font-size: 0.95em;
  color: #333;
}
.styled-checkbox-group input[type="checkbox"] {
  appearance: none;
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  border: 2px solid #adb5bd;
  border-radius: 4px;
  margin-right: 10px;
  cursor: pointer;
  position: relative;
  top: -1px;
  transition: background-color 0.15s ease-in-out, border-color 0.15s ease-in-out;
}
.styled-checkbox-group input[type="checkbox"]:checked {
  background-color: #007bff;
  border-color: #007bff;
}
.styled-checkbox-group input[type="checkbox"]:checked::before {
  content: '✓';
  display: block;
  color: white;
  font-size: 12px;
  font-weight: bold;
  text-align: center;
  line-height: 16px;
}
.styled-checkbox-group input[type="checkbox"]:focus {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13,110,253,.25);
}
.checkbox-label {
  user-select: none;
}

/* --- Quantity Inputs (Number + Unit) --- */
.quantity-inputs { /* Parent for a row of type-specific inputs */
  display: flex; /* MODIFICATION: Use flex to keep groups on one line */
  gap: 20px;
  align-items: flex-start; /* Align groups at their top */
  flex-wrap: nowrap; /* MODIFICATION: Prevent wrapping of the groups */
  overflow-x: auto; /* Allow horizontal scroll if they still don't fit, though flex: 1 on group should prevent */
}
.quantity-inputs.no-wrap-groups { /* Explicit class to ensure no wrapping for the 3 groups */
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* MODIFICATION: Ensure 3 columns */
  gap: 20px;
  align-items: flex-start;
}


.input-unit-group { /* Each individual "单选题: [input] 道" group */
  display: flex;
  flex-direction: column;
  align-items: stretch;
  flex: 1; /* MODIFICATION: Allow groups to share space */
  min-width: 150px; /* MODIFICATION: Minimum width for each group before shrinking */
}
.input-unit-group .input-label {
  font-size: 0.9em;
  color: #555;
  margin-bottom: 6px;
  font-weight: normal;
  display: block;
}
.input-unit-group .input-with-unit {
  display: flex;
  align-items: center;
}
.input-unit-group .form-control.quantity-input { /* The number input itself */
  text-align: center;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
  flex-grow: 1; /* Allow input to take space if .unit is not fixed width */
  min-width: 60px; /* Ensure input itself has some min width */
}
.input-unit-group .unit {
  font-size: 0.95em;
  color: #555;
  white-space: nowrap;
  padding: 12px 10px;
  background-color: #e9ecef;
  border: 1px solid #ced4da;
  border-left: none;
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
  display: flex; /* To center text if needed, though padding usually suffices */
  align-items: center;
}

/* MODIFICATION: Styles for disabled (unchecked type) quantity/score fields */
.input-unit-group.field-disabled .input-label,
.input-unit-group.field-disabled .unit {
  opacity: 0.5;
  color: #868e96; /* More muted grey */
}
.input-unit-group.field-disabled input[type="number"].quantity-input,
.input-unit-group input[type="number"].quantity-input:disabled { /* Target direct :disabled as well */
  background-color: #e9ecef; /* Standard disabled background */
  color: #6c757d; /* Muted text color */
  opacity: 0.65; /* Make it look more faded */
  border-color: #d6d8db;
  cursor: not-allowed;
}
.input-unit-group.field-disabled input[type="number"].quantity-input:hover,
.input-unit-group input[type="number"].quantity-input:disabled:hover {
  box-shadow: none; /* Remove focus/hover shadow when disabled */
}

/* --- Form Actions --- */
.form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

/* --- 按钮通用样式 & Variants (Copied from previous beautification) --- */
.btn {
  padding: 12px 24px; border: none; border-radius: 8px; cursor: pointer;
  font-size: 1em; font-weight: 500; transition: all 0.2s ease-in-out;
  display: inline-flex; align-items: center; justify-content: center;
  gap: 8px; text-decoration: none; line-height: 1.5;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}
.btn:hover { opacity: 0.85; transform: translateY(-2px); box-shadow: 0 4px 10px rgba(0,0,0,0.12); }
.btn:active { transform: translateY(0); box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
.btn i[class^="icon-"] { font-size: 1.1em; }

.primary-btn { background-color: #007bff; color: white; }
.primary-btn:hover { background-color: #0069d9; }
.primary-outline-btn { background-color: transparent; color: #007bff; border: 2px solid #007bff; }
.primary-outline-btn:hover { background-color: #007bff; color: white; }
.secondary-btn { background-color: #6c757d; color: white; }
.secondary-btn:hover { background-color: #5a6268; }

/* --- 模态框通用样式 (Copied and refined from previous beautification) --- */
.modal-overlay {
  position: fixed; inset: 0; background-color: rgba(0, 0, 0, 0.55);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; padding: 20px; overflow-y: auto;
}
.modal-dialog {
  background: white; border-radius: 10px; box-shadow: 0 8px 25px rgba(0,0,0,0.15);
  width: 100%; max-width: 800px; max-height: 90vh;
  display: flex; flex-direction: column; margin: auto;
}
.modal-dialog[style*="max-width: 400px;"] { max-width: 400px !important; }
.modal-header {
  padding: 20px 30px; border-bottom: 1px solid #e9ecef;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.modal-title { margin: 0; font-size: 1.6em; font-weight: 600; color: #343a40; }
.modal-dialog h3.modal-title { font-size: 1.4em; }
.modal-close-btn {
  background: none; border: none; font-size: 2em; font-weight: 300;
  color: #6c757d; cursor: pointer; padding: 0; line-height: 1; opacity: 0.7;
}
.modal-close-btn:hover { color: #343a40; opacity: 1; }
.modal-body { padding: 30px; overflow-y: auto; flex-grow: 1; }
.modal-footer {
  padding: 20px 30px; border-top: 1px solid #e9ecef;
  display: flex; justify-content: flex-end; gap: 12px;
  flex-shrink: 0; background-color: #f8f9fa;
  border-bottom-left-radius: 10px; border-bottom-right-radius: 10px;
}

/* --- 试卷预览模态框 --- */
.preview-questions { margin-top: 0; }
.question-preview {
  background: #f8f9fa; padding: 18px 22px; border-radius: 8px;
  margin-bottom: 18px; border-left: 5px solid #007bff;
}
.question-preview .question-header {
  display: flex; align-items: center; margin-bottom: 12px;
  color: #343a40; font-weight: 500;
}
.type-badge {
  padding: 5px 12px; border-radius: 20px; font-weight: 500;
  color: white; font-size: 0.85em; text-transform: capitalize;
  background-color: #6c757d; white-space: nowrap; margin-right: 10px;
}
.type-badge.type-single_choice, .type-badge.type-单选 { background-color: #17a2b8; }
.type-badge.type-multiple_choice, .type-badge.type-多选  { background-color: #fd7e14; }
.type-badge.type-true_false, .type-badge.type-判断 { background-color: #20c997; }
.type-badge.type-unknown, .type-badge.type-unknown_unresolved {background-color: #dc3545;}

.question-preview .question-number { flex-grow: 1; margin: 0 10px; font-weight: 500; }
.question-preview .score { font-weight: 600; color: #007bff; }
.question-preview .options { margin-left: 15px; margin-top: 8px; font-size: 0.95em; }
.question-preview .option {
  display: block; margin: 8px 0; padding: 8px 10px;
  border-radius: 6px; color: #495057; background-color: #fff; border: 1px solid #eee;
}
.question-preview .option.correct {
  background-color: #d1e7dd; color: #0f5132; font-weight: 500; border-color: #a3cfbb;
}
.total-score { font-size: 1.25em; font-weight: 600; color: #2c3e50; }
.loading-indicator, .no-questions {
  text-align: center; padding: 40px 20px; color: #6c757d;
  background-color: #f0f3f5; border-radius: 8px;
  font-size: 1.1em; margin: 20px 0;
}

/* --- 发布考试模态框 --- */
.datetime-pickers { display: flex; gap: 15px; align-items: center; }
.datetime-pickers span { color: #6c757d; margin: 0 5px; font-size: 1.1em; }
.datetime-pickers input[type="datetime-local"].form-control { flex-grow: 1; }
input[type="datetime-local"]::-webkit-calendar-picker-indicator {
  opacity: 0.7; filter: invert(0.3) brightness(0.7); cursor: pointer;
}
input[type="datetime-local"] { color-scheme: light; }

/* --- 响应式调整 --- */
@media (max-width: 992px) { /* Widen form earlier */
  .quantity-inputs.no-wrap-groups {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); /* Allow wrapping on medium screens */
  }
}

@media (max-width: 768px) {
  .auto-create-paper { padding: 15px; }
  h1 { font-size: 1.8em; }
  .paper-form-container { padding: 0; max-width: 100%;}
  .paper-form { padding: 20px 20px; } /* Adjusted padding for smaller screens */
  .section-title { font-size: 1.15em; }
  .form-group label { font-size: 0.9em; }

  .styled-checkbox-group { flex-direction: column; gap: 15px; align-items: flex-start;}
  .quantity-inputs.no-wrap-groups { grid-template-columns: 1fr; gap: 15px;}

  .input-unit-group .input-label { text-align: left; margin-bottom: 5px; }
  .input-unit-group input[type="number"].quantity-input { width: auto; flex-grow: 1;}

  .modal-dialog { max-width: 95%; margin: 10px; max-height: 95vh; }
  .modal-title { font-size: 1.3em; }
  .modal-body, .modal-header, .modal-footer { padding: 15px 20px; }
  .modal-footer { flex-direction: column; }
  .modal-footer .btn { width: 100%; }
  .total-score { text-align: center; margin-bottom: 10px; margin-right: 0;}

  .datetime-pickers { flex-direction: column; gap: 10px; align-items: stretch; }
}

/* Placeholder icons */
.icon-refresh::before { content: "🔄"; margin-right: 6px; }
.icon-send::before { content: "➤"; margin-right: 6px; }
.icon-check::before { content: "✓"; margin-right: 6px; }
</style>