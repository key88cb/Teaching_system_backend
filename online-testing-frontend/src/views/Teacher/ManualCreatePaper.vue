<template>
  <div class="manual-create-paper">
    <h1>手动组卷</h1>

    <div class="manual-create-paper-layout">

      <div class="paper-construction-area">
        <div class="paper-form">
          <div class="form-group paper-info-group">
            <label for="paper-title">试卷名称：</label>
            <input id="paper-title" type="text" v-model="paper.title" placeholder="请输入试卷名称" />
            <label for="course-id">课程 ID：</label>
            <input id="course-id" type="number" v-model="paper.courseId" placeholder="请输入课程ID" />
            <label for="paper-creator">创建者：</label>
            <input id="paper-creator" type="text" v-model="paper.creator" placeholder="请输入出卷人名称" />
          </div>

          <div class="form-group">
            <label>题目列表 (已选 {{ paper.questions.length }} 题)：</label>
            <div class="question-list selected-questions-list">
              <div v-if="paper.questions.length === 0" class="no-questions-selected">
                <p>请从右侧题库面板选择题目添加到此处。</p>
              </div>
              <div v-for="(question, index) in paper.questions" :key="question.id + '-' + index" class="question-row">
                <input type="text" :value="question.questionText || `题目ID: ${question.id}`" disabled />
                <span>题目分数:</span>
                <input type="number" v-model.number="question.score" placeholder="分数" min="0" />
                <button class="btn remove-btn" @click="removeQuestion(index)">移除</button>
              </div>
            </div>
          </div>

          <div class="form-actions main-form-actions">
            <button class="btn cancel-creation-btn" @click="cancelPaperCreation">
              {{ (isEditMode) ? '取消修改并返回' : '取消组卷并返回' }}
            </button>
            <button
                class="view-library-btn btn"
                :class="{ 'close-mode': showLibrary }"
                @click="showLibrary = !showLibrary"
            >
              {{ showLibrary ? '隐藏题库面板' : '显示题库面板' }}
            </button>
            <button class="btn preview-btn" @click="previewPaper">预览试卷</button>
          </div>
        </div>
      </div>

      <div class="question-library-panel" v-if="showLibrary">
        <div class="library-panel-header">
          <h3>题库 (可选题目)</h3>
          <button class="btn-close-panel" @click="showLibrary = false" title="关闭题库面板">&times;</button>
        </div>

        <div class="toolbar library-toolbar">
          <div class="search-filter">
            <input
                v-model="searchQuery"
                placeholder="按科目或关键词搜索"
                class="search-input control-input"
            />
            <select v-model="selectedsubject" class="subject-filter control-input">
              <option value="">全部科目</option>
              <option v-for="subjectInBank in uniquesubjects" :key="subjectInBank" :value="subjectInBank">{{ subjectInBank }}</option>
            </select>
          </div>
        </div>
        <div class="questions-grid library-questions-grid">
          <div v-if="!questionBank || questionBank.length === 0" class="loading-indicator">
            <p>题库加载中或为空...</p>
          </div>
          <div v-else-if="filteredQuestions.length === 0" class="no-results">
            没有找到符合条件的题目。
          </div>
          <div
              v-for="questionInBank in filteredQuestions"
              :key="questionInBank.questionId"
              class="question-card library-question-card"
          >
            <button
                class="add-question-btn"
                @click="openAddScoreModal(questionInBank)"
                :disabled="paper.questions.some(q => q.id === questionInBank.questionId)"
                :title="paper.questions.some(q => q.id === questionInBank.questionId) ? '题目已在试卷中' : '添加到试卷'"
            >
              {{ paper.questions.some(q => q.id === questionInBank.questionId) ? '✓' : '+' }}
            </button>
            <div class="question-header">
              <span class="type-badge" :class="`type-${(questionInBank.questionType || 'unknown').toLowerCase().replace(/[\s/]+/g, '_')}`">
                {{ getDisplayQuestionType(questionInBank.questionType) }}
              </span>
            </div>
            <div class="question-text">{{ questionInBank.questionText }}</div>
            <div class="question-tag" v-if="questionInBank.subjectCategory || questionInBank.tags">
              科目：{{ questionInBank.subjectCategory || '无' }} / 标签：{{ questionInBank.tags || '无' }}
            </div>
            <div class="options">
              <div
                  v-for="(option, optIndex) in questionInBank.options"
                  :key="option.optionIdValue || optIndex"
                  :class="['option', { correct: typeof questionInBank.correctAnswer === 'string' && questionInBank.correctAnswer.includes(String(option.optionIdValue || option.value)) }]"
              >
                {{ String.fromCharCode(65 + optIndex) }}. {{ option.optionText }}
              </div>
            </div>
          </div>
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
          <div class="preview-questions">
            <div v-for="(item, index) in previewQuestions" :key="item.id" class="question-preview">
              <div class="question-header">
                <span class="type-badge" :class="`type-${(item.questionTypeOriginal || item.questionType || 'unknown').toLowerCase().replace(/[\s/]+/g, '_')}`">
                 {{ getDisplayQuestionType(item.questionTypeOriginal || item.questionType) }}
                </span>
                <span class="question-number">{{ index + 1 }}. {{ item.questionText }}</span>
                <span class="score">{{ item.score }}分</span>
              </div>
              <div class="options">
                <div
                    v-for="(option, optionIndex) in item.options"
                    :key="option.optionIdValue || option.value || optionIndex"
                    :class="['option', { correct: option.isCorrect || (typeof item.correctAnswer === 'string' && item.correctAnswer.includes(String(option.optionIdValue || option.value))) }]"
                >
                  {{ String.fromCharCode(65 + optionIndex) }}. {{ option.optionText || option.label }}
                </div>
              </div>
            </div>
            <div v-if="previewQuestions.length === 0 && paper.questions.length > 0" class="no-questions">
              无法加载预览题目详情，请检查题库数据。
            </div>
            <div v-if="paper.questions.length === 0" class="no-questions">
              试卷中尚无题目，请先从题库添加。
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <div class="total-score" style="margin-right: auto; align-self: center;">
            总分：{{ totalScore }}分
          </div>
          <button class="btn secondary-btn" @click="closePreviewModal">关闭</button>
          <button class="btn primary-btn" @click="goToPublish">去发布</button>
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
            <input id="exam-start-time" type="datetime-local" v-model="examSettings.startTime" />
          </div>
          <div class="form-group">
            <label for="exam-end-time">考试结束时间：</label>
            <input id="exam-end-time" type="datetime-local" v-model="examSettings.endTime" />
          </div>
          <div class="form-group">
            <label for="exam-total-score">试卷总分：</label>
            <input id="exam-total-score" type="number" min="0" v-model.number="examSettings.fullScore" disabled />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn secondary-btn" @click="cancelPublish">取消</button>
          <button class="btn primary-btn" @click="confirmPublish">确认发布</button>
        </div>
      </div>
    </div>

    <div v-if="showAddScoreModal" class="modal-overlay" @click.self="showAddScoreModal = false">
      <div class="modal-dialog" style="max-width: 400px;">
        <div class="modal-header">
          <h3 class="modal-title">设置题目分数</h3>
          <button class="modal-close-btn" @click="showAddScoreModal = false" aria-label="关闭模态框">&times;</button>
        </div>
        <div class="modal-body">
          <div style="margin-bottom: 10px;"><strong>题目：</strong>{{ currentAddQuestion?.questionText }}</div>
          <label for="question-score-input" style="display:block; margin-bottom:5px;">分数：</label>
          <input id="question-score-input" class="form-control" type="number" v-model.number="addScoreValue" min="1" placeholder="请输入分数" />
        </div>
        <div class="modal-footer">
          <button class="btn secondary-btn" @click="showAddScoreModal = false">取消</button>
          <button class="btn primary-btn" @click="confirmAddQuestion">确定添加</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
const router = useRouter();
const route = useRoute();
const originalPaperId = ref(parseInt(route.query.paperId) || null);
const originalCourseId = ref(parseInt(route.query.courseId) || null);
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
  'unknown': '未知',
  'unknown (unresolved)': '未知类型'
};

const getDisplayQuestionType = (backendType) => {
  if (!backendType) return QUESTION_TYPE_MAP_TO_CHINESE['unknown'];
  return QUESTION_TYPE_MAP_TO_CHINESE[backendType] || backendType;
};

const getInitialPaperData = () => ({
  title: '',
  courseId: null,
  creator: '',
  questions: []
});

const paper = ref(JSON.parse(JSON.stringify(getInitialPaperData())));
const isEditMode = ref(false);

const examSettings = ref({
  startTime: '',
  endTime: '',
  fullScore: 0,
});

const questionBank = ref([]);
const showPreviewModal = ref(false);
const showPublishModal = ref(false);
const showLibrary = ref(false);
const showAddScoreModal = ref(false);
const isFormDirty = ref(false);
let initialPaperSnapshot = '';

const resetPaperForm = () => {
  paper.value = JSON.parse(JSON.stringify(getInitialPaperData()));
  // examSettings are intentionally NOT reset here for the main form reset,
  // but they will be repopulated from localStorage or edit data in onMounted.
  nextTick(() => {
    initialPaperSnapshot = JSON.stringify(paper.value);
    isFormDirty.value = false;
  });
};

watch(paper, (newValue) => {
  if (JSON.stringify(newValue) !== initialPaperSnapshot) {
    isFormDirty.value = true;
  } else {
    isFormDirty.value = false;
  }
}, { deep: true });

const previewQuestions = computed(() => {
  if (paper.value.questions.length === 0) return [];
  return paper.value.questions
      .map(paperQuestion => {
        const fullQuestion = questionBank.value.find(q => q.questionId === paperQuestion.id);
        if (fullQuestion) {
          return {
            ...fullQuestion,
            score: paperQuestion.score,
            correctAnswer: typeof fullQuestion.correctAnswer === 'string' ? fullQuestion.correctAnswer : (fullQuestion.correctAnswer ? String(fullQuestion.correctAnswer) : ''),
            options: Array.isArray(fullQuestion.options) ? fullQuestion.options.map(opt => ({ ...opt })) : [],
            questionTypeOriginal: fullQuestion.questionType
          };
        }
        return {
          id: paperQuestion.id,
          questionText: paperQuestion.questionText || `题目ID: ${paperQuestion.id} (题库详情未找到)`,
          score: paperQuestion.score,
          options: [],
          questionType: '未知 (Unresolved)',
          questionTypeOriginal: 'unknown',
          correctAnswer: ''
        };
      });
});

const totalScore = computed(() => {
  return paper.value.questions.reduce((sum, q) => sum + (Number(q.score) || 0), 0);
});

const removeQuestion = (index) => {
  if (paper.value.questions.length > 0) {
    if (window.confirm('您确定要从试卷中移除这个题目吗？')) {
      paper.value.questions.splice(index, 1);
    }
  }
};

const previewPaper = () => {
  if (paper.value.questions.length === 0) {
    alert('请先添加题目到试卷列表。');
    return;
  }
  showPreviewModal.value = true
}
// onMounted(async () => {
//   try {
//     // 检查是否是“修改模式”
//     const route = useRoute()
//     // console.log('11',route.params)
//     // console.log('22',route.query.mode)
//     // console.log('sdjakj',route.params)
//     console.log('route.query', route.query)
//     console.log('route.params', route.params)
//     editId.value.paperId = route.params.paperId
//     editId.value.courseId = route.params.courseId
//
//     if (route.query&&(route.query.mode === 'edit' && window.history.state && window.history.state.paperInfo)) {
//       const paperInfo = window.history.state.paperInfo
//       // 初始化表单
//       paper.value.title = paperInfo.paperName
//       paper.value.courseId = paperInfo.courseId
//       paper.value.creator = paperInfo.creator
//       // 题目列表
//       paper.value.questions = (paperInfo.paperQuestions || []).map(q => ({
//         id: q.questionId,
//         score: q.points,
//         questionText: q.questionText
//       }))
//     }
//   } catch (error) {
//     alert('加载题目失败，请检查网络或服务状态')
//     console.error(error)
//   }
// })
onMounted(async () => {
  await fetchQuestions()

  if (route.query.mode === 'edit' && route.params.paperId && route.params.courseId) {
    const paperId = route.params.paperId
    const courseId = route.params.courseId

    try {
      const res = await fetch(url_front+`api/paper-questions/query-paper-and-questions?courseId=${courseId}&paperId=${paperId}`)
      if (!res.ok) throw new Error('加载失败')

      const data = await res.json()

      // 设置表单数据
      paper.value.title = data.paperName
      paper.value.courseId = data.courseId
      paper.value.creator = data.creator

      // 设置题目列表
      paper.value.questions = data.paperQuestions.map(q => ({
        id: q.questionId,
        score: q.points,
        questionText: q.questionText
      }))
    } catch (error) {
      alert('加载失败，请重试')
      console.error(error)
    }

    const currentPreviewQuestions = previewQuestions.value;
    const unresolvedCount = currentPreviewQuestions.filter(q => q.questionType === '未知 (Unresolved)').length;

    if (currentPreviewQuestions.length === 0 && paper.value.questions.length > 0) {
      alert('无法生成预览数据，请检查题库和已选题目。');
      return;
    }
    if (unresolvedCount > 0 && unresolvedCount === paper.value.questions.length) {
      alert(`所有已选题目 (${unresolvedCount}个) 均未能从题库加载完整信息，无法预览。请检查题库数据。`);
      return;
    } else if (unresolvedCount > 0) {
      alert(`部分题目 (${unresolvedCount}个) 未能从题库加载完整信息，预览可能不完整，但仍将显示。`);
    }
    showPreviewModal.value = true;
  }
});

const closePreviewModal = () => {
  showPreviewModal.value = false;
};

const goToPublish = () => {
  if (paper.value.questions.length === 0) {
    alert('试卷中没有题目，无法发布。');
    return;
  }
  const calculatedTotalScore = totalScore.value;
  if (calculatedTotalScore <= 0 && paper.value.questions.length > 0) {
    if(!window.confirm('试卷总分目前为0或所有题目分数未设置，确定要继续发布吗？')) {
      showPreviewModal.value && closePreviewModal();
      return;
    }
  }
  examSettings.value.fullScore = calculatedTotalScore;
  closePreviewModal();
  showPublishModal.value = true;
};

const Create_Exam_Paper = async (requestData) => {
  const endpoint = isEditMode.value && route.params.paperId
      ? url_front+`api/paper-questions/edit-paper/${route.params.paperId}`
      : url_front+'api/paper-questions/manual-create-paper';
  const method = isEditMode.value && route.params.paperId ? 'PUT' : 'POST';

  if (isEditMode.value && route.params.paperId) {
    requestData.paperId = route.params.paperId;
  }

  try {
    if (isEditMode.value && window.history.state?.paperInfo?.deleteThenCreateMode && method === 'POST') {
      console.warn(" legacy delete-then-create logic for edit mode is being used via POST.");
      const oldPaperInfo = window.history.state.paperInfo;
      const deleteRes = await fetch(url_front+'api/paper-questions/delete-paper', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paperId: oldPaperInfo.paperId, courseId: oldPaperInfo.courseId })
      });
      if (!deleteRes.ok && deleteRes.status !== 204) {
        const errorText = await deleteRes.text();
        throw new Error(`删除原试卷失败: ${errorText || deleteRes.status}`);
      }
    }

    const res = await fetch(endpoint, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestData)
    });

    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: '网络响应错误，且无法解析错误详情' }));
      throw new Error(errorData.message || `HTTP error ${res.status}`);
    }
    alert(`试卷${isEditMode.value ? '修改' : '发布'}成功！`);

    if (examSettings.value.startTime) {
      localStorage.setItem('manualCreatePaper_lastStartTime', examSettings.value.startTime);
    }
    if (examSettings.value.endTime) {
      localStorage.setItem('manualCreatePaper_lastEndTime', examSettings.value.endTime);
    }

    resetPaperForm();
    isEditMode.value = false;
    router.push('/teacher/exam-management');
  } catch (error) {
    console.error(`创建/修改试卷失败:`, error);
    alert(`创建/修改试卷失败: ${error.message}`);
  }
};

const cancelPublish = () => {
  showPublishModal.value = false;
};

async function deleteExistingPaper(paperId, courseId) {
  if (!paperId || !courseId) {
    console.error('无法删除试卷：缺少 paperId 或 courseId');
    return;
  }
  console.log('正在删除旧试卷...');
  console.log('paperId:', paperId);
  console.log('courseId:', courseId);
  try {
    const res = await fetch(url_front+'api/paper-questions/delete-paper', {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        paperId: paperId,
        courseId: courseId
      })
    });

    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
      throw new Error(errorData.message || `HTTP error ${res.status}`);
    }

    console.log('旧试卷删除成功');
  } catch (error) {
    console.error('删除旧试卷失败:', error);
    throw error;
  }
}

const confirmPublish = async () => {
  if (!paper.value.title.trim()) {
    alert('请输入试卷名称。');
    return;
  }
  if (!paper.value.courseId) {
    alert('请输入课程ID。');
    return;
  }
  if (!paper.value.creator.trim()) {
    alert('请输入创建者。');
    return;
  }
  if (paper.value.questions.length === 0) {
    alert('试卷中没有题目，无法发布。');
    return;
  }
  if (!examSettings.value.startTime || !examSettings.value.endTime) {
    alert('请填写完整的考试时间。');
    return;
  }
  if (new Date(examSettings.value.startTime) >= new Date(examSettings.value.endTime)) {
    alert('结束时间必须大于开始时间。');
    return;
  }
  examSettings.value.fullScore = totalScore.value;
  if (examSettings.value.fullScore <= 0 && paper.value.questions.length > 0) {
    if (!window.confirm('试卷总分目前为0或所有题目分数未设置，确定要发布吗？')) {
      return;
    }
  }

  const confirmActionText = isEditMode.value ? '修改并重新发布' : '发布';
  if (window.confirm(`确认要${confirmActionText}这份试卷吗？`)) {
    // 如果是编辑模式，先删除原来的试卷
    if (route.query.mode === 'edit' && route.params.paperId && route.params.courseId) {
      await deleteExistingPaper(route.params.paperId, route.params.courseId);
    }
    const manualPaperRequest = {
      courseId: paper.value.courseId,
      creator: paper.value.creator,
      openTime:examSettings.value.startTime,
      closeTime: examSettings.value.endTime,
      totalScores: examSettings.value.fullScore,
      questions: paper.value.questions.map(q => ({
        questionId: q.id,
        points: Number(q.score) || 0
      })),
      paperName: paper.value.title
    };
    Create_Exam_Paper(manualPaperRequest);
  }
};

const searchQuery = ref('');
const selectedsubject = ref('');

const uniquesubjects = computed(() => {
  return [...new Set(questionBank.value.map(q => q.subjectCategory))].filter(Boolean)
})

const fetchQuestions = async () => {
  try {
    const res = await fetch(url_front+'api/questions');
    if (!res.ok) throw new Error(`获取题库网络错误 (${res.status})`);
    const data = await res.json();
    questionBank.value = data.map(q => ({
      ...q,
      options: Array.isArray(q.options) ? q.options : [],
      correctAnswer: q.correctAnswer || ''
    }));
  } catch (error) {
    alert(`加载题库失败: ${error.message}`);
    console.error(error);
    questionBank.value = [];
  }
};

onMounted(async () => {
  await fetchQuestions();

  // --- MODIFICATION: Load last used exam times from localStorage for new papers ---
  // This will be overridden by edit mode logic if applicable
  const lastStartTime = localStorage.getItem('manualCreatePaper_lastStartTime');
  const lastEndTime = localStorage.getItem('manualCreatePaper_lastEndTime');
  if (lastStartTime && !isEditMode.value) { // Only apply if not in edit mode at this stage
    examSettings.value.startTime = lastStartTime;
  }
  if (lastEndTime && !isEditMode.value) {
    examSettings.value.endTime = lastEndTime;
  }
});
  // --- End of MODIFICATION ---

// onMounted(fetchQuestions)

const filteredQuestions = computed(() => {
  return questionBank.value.filter(question => {
    const subjectMatch = !selectedsubject.value ||
        (question.subjectCategory && question.subjectCategory === selectedsubject.value);
    const searchTerm = searchQuery.value.toLowerCase().trim();
    const searchMatch = !searchTerm ||
        (question.questionText && question.questionText.toLowerCase().includes(searchTerm)) ||
        (question.tags && question.tags.toLowerCase().includes(searchTerm)) ||
        (question.options && question.options.some(
            opt => opt.optionText && opt.optionText.toLowerCase().includes(searchTerm)
        ));
    return subjectMatch && searchMatch;
  });
});

const currentAddQuestion = ref(null);
const addScoreValue = ref(1);

const openAddScoreModal = (questionFromBank) => {
  if (paper.value.questions.some(q => q.id === questionFromBank.questionId)) {
    alert('该题目已在试卷列表中。如需修改分数，请直接在列表修改。');
    return;
  }
  currentAddQuestion.value = questionFromBank;
  addScoreValue.value = 1;
  showAddScoreModal.value = true;
};

const confirmAddQuestion = () => {
  if (!addScoreValue.value || addScoreValue.value <= 0) {
    alert('请输入有效的分数 (大于0)。');
    return;
  }
  if (!currentAddQuestion.value || !currentAddQuestion.value.questionId) {
    alert('未能获取要添加的题目信息。');
    return;
  }
  if (paper.value.questions.some(q => q.id === currentAddQuestion.value.questionId)) {
    showAddScoreModal.value = false;
    return;
  }
  paper.value.questions.push({
    id: currentAddQuestion.value.questionId,
    score: Number(addScoreValue.value),
    questionText: currentAddQuestion.value.questionText
  });
  showAddScoreModal.value = false;
};

const cancelPaperCreation = () => {
  const actionText = isEditMode.value ? '放弃修改' : '取消本次组卷';
  if (isFormDirty.value) {
    if (window.confirm(`您有未保存的更改。确定要${actionText}并返回吗？所有更改都将丢失。`)) {
      resetPaperForm();
      // If not in edit mode, also clear potentially persisted exam times from the form for a "fresh" start next time
      if (!isEditMode.value) {
        examSettings.value.startTime = '';
        examSettings.value.endTime = '';
        examSettings.value.fullScore = 0;
      }
      isEditMode.value = false;
      router.push('/teacher/dashboard');
    }
  } else {
    isEditMode.value = false;
    router.push('/teacher/dashboard');
  }
}
</script>

<style scoped>
/* --- 全局与页面布局 --- */
.manual-create-paper {
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

/* --- 两栏布局 --- */
.manual-create-paper-layout {
  display: flex;
  gap: 25px;
  align-items: flex-start;
}

.paper-construction-area {
  flex: 3;
  min-width: 0; /* Prevent flex item overflow */
}

.question-library-panel {
  flex: 2;
  background-color: #ffffff;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  padding: 20px;
  height: calc(100vh - 160px); /* Adjust as needed, considering h1 and page padding */
  display: flex;
  flex-direction: column;
  min-width: 0; /* Prevent flex item overflow */
}

.library-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #dee2e6;
}
.library-panel-header h3 {
  margin: 0;
  font-size: 1.4em;
  color: #2c3e50;
  font-weight: 600;
}
.btn-close-panel {
  background: none;
  border: none;
  font-size: 1.8em; /* Increased size for better clickability */
  color: #6c757d;
  cursor: pointer;
  padding: 0 5px;
  line-height: 1;
}
.btn-close-panel:hover { color: #343a40; }


/* --- 表单主容器与信息组 (左侧) --- */
.paper-form {
  background: white;
  padding: 25px 30px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.07);
}

.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #495057;
  font-size: 0.95em;
}
.paper-info-group {
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  background: #f8f9fa;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 10px 15px;
  align-items: center;
}
.paper-info-group label {
  margin-bottom: 0;
  grid-column: 1 / 2;
  white-space: nowrap; /* Prevent label text from wrapping */
}
.paper-info-group input[type="text"],
.paper-info-group input[type="number"] {
  grid-column: 2 / 3;
  width: 100%;
}

/* 通用输入框 */
.control-input, /* Class for search/select in library */
input[type="text"],
input[type="number"],
input[type="datetime-local"],
select {
  padding: 10px 15px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 1em;
  transition: border-color 0.2s, box-shadow 0.2s;
  height: 42px;
  box-sizing: border-box;
  width: 100%;
  color: #495057;
  background-color: #fff;
}
.control-input:focus,
input[type="text"]:focus,
input[type="number"]:focus,
input[type="datetime-local"]:focus,
select:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
  outline: none;
}
#question-score-input.form-control { /* Specific to AddScoreModal input if using form-control class */
  /* Styles are already covered by general input[type="number"] */
}

input[type="datetime-local"] {
  color: #495057; /* 确保输入框内的文本（日期时间）是深色的 */
  background-color: #fff; /* 输入框背景是白色 */
  border: 1px solid #ced4da; /* 确保边框可见 */
}

input[type="datetime-local"]::-webkit-calendar-picker-indicator {
  opacity: 0.75; /* 确保图标不是完全透明的，可以调整此值 */
  filter: invert(0.3) brightness(0.7); /* 尝试让图标变暗一些 */
  cursor: pointer; /* 表明它是可点击的 */
}

/* 当输入框获得焦点时，确保样式依然清晰 */
input[type="datetime-local"]:focus {
  border-color: #007bff; /* 蓝色边框 */
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
  outline: none;
}

/* --- 已选题目列表 (左侧) --- */
.selected-questions-list {
  background: #f0f3f5;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  min-height: 150px;
  max-height: 400px;
  overflow-y: auto;
}
.no-questions-selected {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100px;
  color: #6c757d;
  font-style: italic;
  padding: 20px;
}
.question-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px;
  background-color: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.question-row input[type="text"][disabled] {
  flex-grow: 3;
  background-color: #e9ecef;
  color: #495057;
  border: 1px solid #ced4da;
  cursor: not-allowed;
}
.question-row input[type="number"] {
  flex-grow: 1;
  min-width: 70px;
  max-width: 90px;
}
.question-row span {
  font-size: 0.9em;
  color: #495057;
  white-space: nowrap;
}

/* --- 按钮通用样式 --- */
.btn {
  padding: 10px 18px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1em;
  font-weight: 500;
  transition: all 0.2s ease-in-out;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-decoration: none;
  line-height: 1.5;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 3px 6px rgba(0,0,0,0.12);
}
.btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}
/* Primary (Blue) */
.primary-btn,
.btn.publish-btn,
.btn.confirm-btn {
  background-color: #007bff; color: white;
}
.primary-btn:hover,
.btn.publish-btn:hover,
.btn.confirm-btn:hover { background-color: #0069d9; }

/* Secondary (Gray/Green) - Preview is Green, Close is Gray */
.secondary-btn,
.btn.close-btn {
  background-color: #6c757d; color: white;
}
.secondary-btn:hover,
.btn.close-btn:hover { background-color: #5a6268; }

.btn.preview-btn { background-color: #28a745; color: white; }
.btn.preview-btn:hover { background-color: #218838; }

/* Danger (Red) - Remove, Cancel Creation (now .cancel-creation-btn) */
.danger-btn,
.btn.remove-btn {
  background-color: #dc3545; color: white;
}
.danger-btn:hover,
.btn.remove-btn:hover { background-color: #c82333; }

/* Specific Cancel Creation Button (can be styled differently if needed, e.g. more subtle) */
.cancel-creation-btn {
  background-color: #6c757d; /* Defaulting to gray like secondary/close */
  color: white;
}
.cancel-creation-btn:hover {
  background-color: #5a6268;
}


.btn.view-library-btn {
  background-color: #17a2b8; color: white; /* Teal */
}
.btn.view-library-btn:hover { background-color: #138496; }
.btn.view-library-btn.close-mode {
  background-color: #ffc107; color: #212529; /* Yellow */
}
.btn.view-library-btn.close-mode:hover { background-color: #e0a800; }

.btn.remove-btn {
  padding: 6px 10px;
  font-size: 0.9em;
  line-height: 1;
  min-width: 55px;
  white-space: nowrap;
}


.main-form-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

/* --- 题库面板样式 (右侧) --- */
.library-toolbar {
  padding: 0 0 15px 0;
  background-color: transparent;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}
.library-toolbar .search-filter {
  display: flex;
  gap: 15px;
  flex-grow: 1;
  flex-wrap: wrap;
}
.library-toolbar .search-input,
.library-toolbar .subject-filter {
  min-width: 180px;
}
.library-toolbar .search-input { flex-grow: 1; flex-basis: 200px; }
.library-toolbar .subject-filter { flex-basis: 150px; cursor: pointer; }

.library-questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 20px 2px;
  overflow-y: auto;
  flex-grow: 1;
}
.loading-indicator, .no-results {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
  background-color: #e9ecef;
  border-radius: 8px;
  font-size: 1.1em;
  margin-top: 20px;
  grid-column: 1 / -1;
}


.library-question-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px 20px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;
  border-left: 4px solid #007bff;
  position: relative;
}
.library-question-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.09);
}

.library-question-card .add-question-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  font-size: 1.5em;
  cursor: pointer;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  padding: 0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}
.library-question-card .add-question-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}
.library-question-card .add-question-btn:hover:not(:disabled) {
  background: #218838;
}

.library-question-card .question-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.library-question-card .type-badge {
  margin-right: 10px;
}
.library-question-card .question-text {
  font-size: 1.05em;
  font-weight: 500;
  color: #343a40;
  margin-bottom: 10px;
  line-height: 1.45;
  word-break: break-word;
  flex-grow: 1;
}
.library-question-card .question-tag {
  font-size: 0.85em;
  color: #5a6268;
  margin-bottom: 10px;
  background-color: #eef2f7;
  padding: 3px 8px;
  border-radius: 4px;
  align-self: flex-start;
  display: inline-block;
}
.library-question-card .options {
  font-size: 0.9em;
  padding-left: 5px;
  margin-top: auto;
  color: #495057;
}
.library-question-card .option {
  padding: 5px 0;
  word-break: break-word;
  border-radius: 3px;
}
.library-question-card .option.correct {
  color: #0f5132;
  background-color: #d1e7dd;
  padding: 4px 8px;
  font-weight: 500;
  border-radius: 4px;
}

/* --- 模态框通用样式 --- */
.modal-overlay {
  position: fixed; inset: 0; background-color: rgba(0, 0, 0, 0.65);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; padding: 20px; overflow-y: auto;
}
.modal-dialog {
  background: white; border-radius: 10px; box-shadow: 0 5px 20px rgba(0,0,0,0.2);
  width: 100%; max-width: 800px; max-height: 90vh;
  display: flex; flex-direction: column; margin: auto;
}
.modal-dialog[style*="max-width: 400px;"] { max-width: 400px !important; }

.modal-header {
  padding: 18px 25px; border-bottom: 1px solid #dee2e6;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.modal-title { /* Applied to h2/h3 inside .modal-header */
  margin: 0; font-size: 1.5em; font-weight: 600; color: #343a40;
}
.modal-dialog h3.modal-title { font-size: 1.3em; }

.modal-close-btn {
  background: none; border: none; font-size: 1.8em; font-weight: bold;
  color: #6c757d; cursor: pointer; padding: 0 5px; line-height: 1;
}
.modal-close-btn:hover { color: #343a40; }

.modal-body {
  padding: 25px; overflow-y: auto; flex-grow: 1;
}
.modal-dialog[style*="max-width: 400px;"] .modal-body > div:first-of-type { /* For "题目：" text */
  margin-bottom: 15px;
}
.modal-dialog[style*="max-width: 400px;"] .modal-body > label { /* For "分数：" label */
  margin-top: 10px;
}


.modal-footer {
  padding: 18px 25px; border-top: 1px solid #dee2e6;
  display: flex; justify-content: flex-end; gap: 12px;
  flex-shrink: 0;
}

/* --- 试卷预览模态框 --- */
.preview-questions { margin-top: 0; }
.question-preview {
  background: #f8f9fa; padding: 15px 20px; border-radius: 8px;
  margin-bottom: 15px; border-left: 4px solid #007bff;
}
.question-preview .question-header {
  display: flex; align-items: center; margin-bottom: 12px;
  color: #343a40; font-weight: 500;
}
.type-badge {
  padding: 4px 10px; border-radius: 15px; font-weight: bold;
  color: white; font-size: 0.8em; text-transform: uppercase;
  background-color: #6c757d;
  white-space: nowrap;
}
.type-badge.type-single_choice,
.type-badge.type-single-choice,
.type-badge.type-单选 { background-color: #17a2b8; }
.type-badge.type-multiple_choice,
.type-badge.type-multiple-choice,
.type-badge.type-多选  { background-color: #ffc107; color: #212529;}
.type-badge.type-true_false,
.type-badge.type-true-false,
.type-badge.type-判断 { background-color: #28a745; }
.type-badge.type-unknown,
.type-badge.type-unknown_unresolved {background-color: #dc3545;}


.question-preview .question-number { flex: 1; margin: 0 8px; }
.question-preview .score { font-weight: bold; color: #007bff; }
.question-preview .options { margin-left: 10px; margin-bottom: 0; font-size: 0.95em; }
.question-preview .option {
  display: block; margin: 6px 0; padding: 6px 10px;
  border-radius: 4px; color: #495057;
}
.question-preview .option.correct {
  background-color: #d1e7dd; color: #0f5132; font-weight: 500;
}
.total-score {
  font-size: 1.2em; font-weight: bold;
  color: #2c3e50;
}
.no-questions {
  text-align: center; padding: 40px 20px; color: #6c757d;
  background-color: #e9ecef; border-radius: 8px;
  font-size: 1.1em; margin-top: 20px;
}

/* --- 发布考试模态框 --- */
.datetime-pickers { display: flex; gap: 10px; align-items: center; }
.datetime-pickers span { color: #6c757d; margin: 0 5px; }
.datetime-pickers input[type="datetime-local"] { flex-grow: 1; }

/* --- 响应式调整 --- */
@media (max-width: 1200px) {
  .manual-create-paper-layout {
    flex-direction: column;
  }
  .paper-construction-area,
  .question-library-panel {
    flex: none;
    width: 100%;
  }
  .question-library-panel {
    margin-top: 25px;
    height: 70vh;
  }
}

@media (max-width: 768px) {
  .manual-create-paper { padding: 15px; }
  h1 { font-size: 1.8em; }
  .paper-form { padding: 20px; }
  .paper-info-group { grid-template-columns: 1fr; }
  .paper-info-group label,
  .paper-info-group input[type="text"],
  .paper-info-group input[type="number"] { grid-column: 1 / -1; }
  .paper-info-group input[type="text"]:not(:last-child),
  .paper-info-group input[type="number"]:not(:last-child) { margin-bottom: 10px; }

  .main-form-actions { flex-direction: column; gap: 10px; }
  .main-form-actions .btn { width: 100%; }

  .question-row { flex-wrap: wrap; }
  .question-row input[type="text"][disabled] { flex-basis: 100%; margin-bottom: 8px; }
  .question-row span { flex-basis: auto; }
  .question-row input[type="number"] { flex-basis: 100px; margin-right: auto; }

  .modal-dialog, .modal-dialog[style*="max-width: 400px;"] {
    max-width: 95%; margin: 10px; max-height: 95vh;
  }
  .modal-title, .modal-dialog h2.modal-title, .modal-dialog h3.modal-title { font-size: 1.3em; }
  .modal-body { padding: 20px; }
  .modal-footer {
    padding: 15px 20px; flex-direction: column;
  }
  .modal-footer .btn { width: 100%; }

  .datetime-pickers { flex-direction: column; gap: 8px; align-items: stretch; }

  .library-toolbar, .library-toolbar .search-filter {
    flex-direction: column; align-items: stretch;
  }
  .library-toolbar .search-input, .library-toolbar .subject-filter { width: 100%; }
  .library-questions-grid { grid-template-columns: 1fr; padding: 15px 0; }
}
</style>