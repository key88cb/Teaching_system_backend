<template>
  <div class="paper-info-management-page">
    <h1>考试情况管理</h1>

    <div class="tabs-container">
      <div class="tabs">
        <button
            :class="['tab-btn', { active: currentTab === 'notStarted' }]"
            @click="setActiveTab('notStarted')"
        >
          <i class="icon-future"></i> 未开始考试 ({{ filteredNotStartedpaperInfos.length }})
        </button>
        <button
            :class="['tab-btn', { active: currentTab === 'ongoing' }]"
            @click="setActiveTab('ongoing')"
        >
          <i class="icon-ongoing"></i> 进行中考试 ({{ filteredOngoingpaperInfos.length }})
        </button>
        <button
            :class="['tab-btn', { active: currentTab === 'ended' }]"
            @click="setActiveTab('ended')"
        >
          <i class="icon-history"></i> 已结束考试 ({{ filteredEndedpaperInfos.length }})
        </button>
      </div>
    </div>

    <div class="controls-container filter-bar">
      <div class="filter-group">
        <label for="filter-course-id">班级ID：</label>
        <select id="filter-course-id" v-model="filterCourseId" class="form-control">
          <option value="">全部班级</option>
          <option v-for="id in uniqueCourseIds" :key="id" :value="id">{{ id }}</option>
        </select>
      </div>
      <div class="filter-group">
        <label for="filter-creator">创建者：</label>
        <select id="filter-creator" v-model="filterCreator" class="form-control">
          <option value="">全部创建者</option>
          <option v-for="c in uniqueCreators" :key="c" :value="c">{{ c }}</option>
        </select>
      </div>
    </div>

    <div class="content-section">
      <div v-if="isLoading" class="loading-indicator">
        <p>正在加载考试信息...</p>
      </div>
      <div v-else>
        <div class="paperInfos-list" v-show="currentTab === 'notStarted'">
          <div v-if="filteredNotStartedpaperInfos.length === 0 && !overallNopaperInfos" class="no-results-in-tab"><p>当前筛选条件下没有未开始的考试。</p></div>
          <div
              v-for="paperInfo in filteredNotStartedpaperInfos"
              :key="paperInfo.paperId"
              class="paperInfo-card status-not-started"
          >
            <div class="card-main-info">
              <h3 class="paper-title">{{ paperInfo.paperName }}</h3>
              <p class="paper-meta">课程ID: {{ paperInfo.courseId }} | 创建者: {{ paperInfo.creator }}</p>
              <p class="paper-time">计划时间：{{ formatDate(paperInfo.openTime) }} - {{ formatDate(paperInfo.closeTime) }}</p>
            </div>
            <div class="card-actions">
              <button class="btn secondary-btn icon-btn" @click.stop="gotoedit(paperInfo)" title="修改考试内容与设置">
                <i class="icon-edit"></i> 修改设置
              </button>
              <button class="btn secondary-outline-btn icon-btn" @click.stop="openTimeEditModal(paperInfo)" title="修改考试时间">
                <i class="icon-time"></i> 修改时间
              </button>
              <button class="btn danger-btn icon-btn" @click.stop="deletepaperInfo(paperInfo)" title="删除此考试">
                <i class="icon-delete"></i> 删除考试
              </button>
            </div>
          </div>
        </div>

        <div class="paperInfos-list" v-show="currentTab === 'ongoing'">
          <div v-if="filteredOngoingpaperInfos.length === 0 && !overallNopaperInfos" class="no-results-in-tab"><p>当前筛选条件下没有进行中的考试。</p></div>
          <div
              v-for="paperInfo in filteredOngoingpaperInfos"
              :key="paperInfo.paperId"
              class="paperInfo-card status-ongoing"
          >
            <div class="card-main-info">
              <h3 class="paper-title">{{ paperInfo.paperName }}</h3>
              <p class="paper-meta">课程ID: {{ paperInfo.courseId }} | 创建者: {{ paperInfo.creator }}</p>
              <p class="paper-time important-time">剩余时间：{{ formatTime(paperInfo.remainingTime) }}</p>
              <p class="paper-time">结束时间：{{ formatDate(paperInfo.closeTime) }}</p>
            </div>
            <div class="card-actions">
              <button class="btn primary-btn icon-btn" @click.stop="viewOngoingDetails(paperInfo)" title="查看进行中的考试详情">
                <i class="icon-eye"></i> 查看实时情况
              </button>
            </div>
          </div>
        </div>

        <div class="paperInfos-list" v-show="currentTab === 'ended'">
          <div v-if="filteredEndedpaperInfos.length === 0 && !overallNopaperInfos" class="no-results-in-tab"><p>当前筛选条件下没有已结束的考试。</p></div>
          <div
              v-for="paperInfo in filteredEndedpaperInfos"
              :key="paperInfo.paperId"
              class="paperInfo-card status-ended"
          >
            <div class="card-main-info">
              <h3 class="paper-title">{{ paperInfo.paperName }}</h3>
              <p class="paper-meta">课程ID: {{ paperInfo.courseId }} | 创建者: {{ paperInfo.creator }}</p>
              <p class="paper-time">考试时间：{{ formatDate(paperInfo.openTime) }} - {{ formatDate(paperInfo.closeTime) }}</p>
            </div>
            <div class="card-actions">
              <button class="btn primary-btn icon-btn" @click.stop="searchforinfo(paperInfo.paperId,paperInfo.courseId)" title="查看已结束考试的详细报告">
                <i class="icon-report"></i> 查看考试详情
              </button>
            </div>
          </div>
        </div>
        <div v-if="overallNopaperInfos && !isLoading" class="no-results">
          <p>当前分类下没有相关考试信息。</p>
        </div>
      </div>
    </div>

    <div v-if="showTimeModal" class="modal-overlay" @click.self="showTimeModal = false">
      <div class="modal-dialog">
        <div class="modal-header">
          <h3 class="modal-title">修改考试时间</h3>
          <button class="modal-close-btn" @click="showTimeModal = false" aria-label="关闭">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group" v-if="selectedPaperForTime">
            <label :for="'start-time-' + selectedPaperForTime.paperId">开始时间：</label>
            <input :id="'start-time-' + selectedPaperForTime.paperId" type="datetime-local" v-model="selectedPaperForTime.openTime" class="form-control" />
          </div>
          <div class="form-group" v-if="selectedPaperForTime">
            <label :for="'end-time-' + selectedPaperForTime.paperId">结束时间：</label>
            <input :id="'end-time-' + selectedPaperForTime.paperId" type="datetime-local" v-model="selectedPaperForTime.closeTime" class="form-control" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn secondary-btn" @click="showTimeModal = false">取消</button>
          <button class="btn primary-btn" @click="submitUpdateTime">
            <i class="icon-check"></i> 确认修改
          </button>
        </div>
      </div>
    </div>

    <div v-if="showAlertModal" class="modal-overlay" @click.self="showAlertModal = false">
      <div class="modal-dialog alert-modal-dialog">
        <div class="modal-header">
          <h3 class="modal-title">提示</h3>
          <button class="modal-close-btn" @click="showAlertModal = false" aria-label="关闭">&times;</button>
        </div>
        <div class="modal-body">
          <p>{{ alertMessage }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn primary-btn" @click="showAlertModal = false">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
const router = useRouter();
const url_front = 'http://localhost:8080/';

const paperInfos = ref([]);
const isLoading = ref(true);
const currentTab = ref('notStarted');

const filterCourseId = ref('');
const filterCreator = ref('');

const showTimeModal = ref(false);
const showAlertModal = ref(false);
const alertMessage = ref('');
const selectedPaperForTime = ref(null);

// --- 科目ID到名称的映射 (示例) ---
const COURSE_ID_TO_NAME_MAP = {
  1: '操作系统原理', 2: '数据库基础', 3: '计算机网络',
  4: '数据结构与算法', 5: '软件工程导论',
  // 根据您的实际情况补充
};
const getCourseNameById = (courseId) => {
  return COURSE_ID_TO_NAME_MAP[courseId] || `科目ID ${courseId}`;
};
// ---

onMounted(() => {
  fetchPaperInfos();
});

// watch([filterCourseId, filterCreator], fetchPaperInfos); // Re-fetch if server supports filtering
// Client-side filtering means we don't need to watch and re-fetch here.

const fetchPaperInfos = async () => {
  isLoading.value = true;
  try {
    const url = url_front+'api/paper-questions/query-all-papers';
    const res = await fetch(url, { method: 'GET' });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
      throw new Error(errorData.message || `HTTP error ${res.status}`);
    }
    const data = await res.json();
    paperInfos.value = data.map(p => ({
      ...p,
      // Ensure dates from backend are valid before trying to use them
      openTime: p.openTime || null,
      closeTime: p.closeTime || null,
      paperName: p.paperName || '未命名试卷' // Add default for paperName
    }));
    console.log('成功拉取全部考试数据:', paperInfos.value.length);
  } catch (error) {
    alertMessage.value = `加载考试列表失败: ${error.message}`;
    showAlertModal.value = true;
    console.error("fetchPaperInfos error:", error);
    paperInfos.value = [];
  } finally {
    isLoading.value = false;
  }
};

const formatInputDateTime = (isoOrDateString) => {
  if (!isoOrDateString) return '';
  try {
    // Attempt to create a date object. Handles both ISO strings and existing date objects.
    const date = new Date(isoOrDateString);
    // Check if the date is valid
    if (isNaN(date.getTime())) {
      // If not valid, it might already be in YYYY-MM-DDTHH:mm format from input
      if (typeof isoOrDateString === 'string' && isoOrDateString.includes('T')) {
        return isoOrDateString.slice(0, 16); // Assume it's already correct for input
      }
      return ''; // Invalid date
    }
    // Correctly format valid date objects to local YYYY-MM-DDTHH:mm for the input
    const offset = date.getTimezoneOffset() * 60000;
    const localDate = new Date(date.getTime() - offset);
    return localDate.toISOString().slice(0, 16);
  } catch (e) {
    console.error("Error formatting date for input:", isoOrDateString, e);
    return ''; // Fallback for any error
  }
};


const openTimeEditModal = (paperInfo) => {
  selectedPaperForTime.value = {
    ...paperInfo,
    openTime: formatInputDateTime(paperInfo.openTime),
    closeTime: formatInputDateTime(paperInfo.closeTime)
  };
  showTimeModal.value = true;
};

const submitUpdateTime = async () => {
  if (!selectedPaperForTime.value) return;
  const paperToUpdate = selectedPaperForTime.value;

  // Values from datetime-local input are strings like "YYYY-MM-DDTHH:mm"
  const openTimeStr = paperToUpdate.openTime;
  const closeTimeStr = paperToUpdate.closeTime;

  if (!openTimeStr || !closeTimeStr) {
    alertMessage.value = '请输入有效的开始和结束时间。';
    showAlertModal.value = true;
    return;
  }

  const openTime = new Date(openTimeStr);
  const closeTime = new Date(closeTimeStr);

  if (openTime >= closeTime) {
    alertMessage.value = '开始时间不能晚于或等于结束时间。';
    showAlertModal.value = true;
    return;
  }

  try {
    // Send the string values from datetime-local directly, as per user's original code
    // Backend should be prepared to parse "YYYY-MM-DDTHH:mm" or convert to full ISO if needed.
    const payload = {
      paperId: paperToUpdate.paperId,
      courseId: paperToUpdate.courseId,
      openTime: openTimeStr, // Send the string from input
      closeTime: closeTimeStr, // Send the string from input
      paperName: paperToUpdate.paperName
    };
    // console.log("Submitting time update with payload:", payload);

    const res = await fetch(url_front+'api/paper-questions/update-paper-time', {
      method: 'POST', // Or PUT
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
      throw new Error(errorData.message || `HTTP error ${res.status}`);
    }
    alertMessage.value = '试卷时间修改成功！';
    showAlertModal.value = true;
    showTimeModal.value = false;
    await fetchPaperInfos();
  } catch (error) {
    alertMessage.value = `更新失败: ${error.message}`;
    showAlertModal.value = true;
    console.error("submitUpdateTime error:", error);
  }
};

const baseFilteredList = computed(() => {
  return paperInfos.value.filter(paperInfo => {
    const courseMatch = !filterCourseId.value || String(paperInfo.courseId) === String(filterCourseId.value);
    const creatorMatch = !filterCreator.value || paperInfo.creator === filterCreator.value;
    return courseMatch && creatorMatch;
  });
});

const notStartedpaperInfos = computed(() => {
  const now = new Date();
  return baseFilteredList.value.filter(e => e.openTime && new Date(e.openTime) > now);
});

const ongoingpaperInfos = computed(() => {
  const now = new Date();
  return baseFilteredList.value
      .filter(e => e.openTime && e.closeTime && new Date(e.openTime) <= now && new Date(e.closeTime) > now)
      .map(paperInfo => {
        const remaining = Math.max(0, Math.floor((new Date(paperInfo.closeTime) - now) / 1000));
        return { ...paperInfo, remainingTime: remaining };
      });
});

const endedpaperInfos = computed(() => {
  const now = new Date();
  return baseFilteredList.value.filter(e => e.closeTime && new Date(e.closeTime) <= now);
});

// These computed properties are now just referencing the ones above for clarity
const filteredNotStartedpaperInfos = computed(() => notStartedpaperInfos.value);
const filteredOngoingpaperInfos = computed(() => ongoingpaperInfos.value);
const filteredEndedpaperInfos = computed(() => endedpaperInfos.value);

const overallNopaperInfos = computed(() => { // Renamed from nopaperInfos for clarity
  return (
      (currentTab.value === 'notStarted' && filteredNotStartedpaperInfos.value.length === 0) ||
      (currentTab.value === 'ongoing' && filteredOngoingpaperInfos.value.length === 0) ||
      (currentTab.value === 'ended' && filteredEndedpaperInfos.value.length === 0)
  ) && !isLoading.value;
});

const gotoedit = (paperInfo) => {
  router.push({
    // name: 'ManualCreatePaperEdit', // Using path if name is not set up
    path: `/teacher/create-paper/manual-edit/${paperInfo.courseId}/${paperInfo.paperId}`,
    query: { mode: 'edit' },
    state: { paperInfo: JSON.parse(JSON.stringify(paperInfo)) }
  });
};

const searchforinfo = (paperId, courseId) => {
  router.push(`/teacher/exams_detail/${courseId}/${paperId}`);
};
const viewOngoingDetails = (paperInfo) => {
  router.push(`/teacher/exams_detail/${paperInfo.courseId}/${paperInfo.paperId}?status=ongoing`);
};

const deletepaperInfo = async (paperInfo) => {
  if (window.confirm(`您确定要删除考试 “${paperInfo.paperName}” 吗？此操作不可恢复。`)) {
    try {
      const res = await fetch(url_front+'api/paper-questions/delete-paper', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          paperId: paperInfo.paperId,
          courseId: paperInfo.courseId,
        })
      });
      if (!res.ok && res.status !== 204) {
        const errorData = await res.json().catch(() => ({ message: '网络响应错误' }));
        throw new Error(errorData.message || `HTTP error ${res.status}`);
      }
      alertMessage.value = '考试删除成功！';
      showAlertModal.value = true;
      await fetchPaperInfos();
    } catch (error) {
      alertMessage.value = `删除考试失败: ${error.message}`;
      showAlertModal.value = true;
      console.error('删除考试失败:', error);
    }
  }
};

const setActiveTab = (tab) => {
  currentTab.value = tab;
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '日期无效';
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second:'2-digit' });
};

const formatTime = (totalSeconds) => {
  if (typeof totalSeconds !== 'number' || totalSeconds < 0) return 'N/A';
  if (totalSeconds === 0) return '已结束或时间已到';
  const days = Math.floor(totalSeconds / (3600 * 24));
  const hours = Math.floor((totalSeconds % (3600 * 24)) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  let result = '';
  if (days > 0) result += `${days}天 `;
  if (hours > 0 || days > 0) result += `${String(hours).padStart(2, '0')}小时 `;
  if (minutes > 0 || hours > 0 || days > 0) result += `${String(minutes).padStart(2, '0')}分 `;
  result += `${String(seconds).padStart(2, '0')}秒`;
  return result.trim();
};

// Populate uniqueCourseIds and uniqueCreators based on the fetched paperInfos
const uniqueCourseIds = computed(() => {
  return [...new Set(paperInfos.value.map(i => i.courseId).filter(id => id != null && id !== undefined))].sort((a,b) => a-b);
});
const uniqueCreators = computed(() => {
  return [...new Set(paperInfos.value.map(i => i.creator).filter(Boolean))].sort();
});

</script>

<style scoped>
/* --- 全局与页面布局 --- */
.paper-info-management-page {
  padding: 25px 35px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f8f9fa;
  min-height: 100vh;
  color: #333;
}

h1 {
  color: #2c3e50;
  text-align: center;
  margin-bottom: 30px;
  font-size: 2.4em;
  font-weight: 600;
}

/* --- Tabs --- */
.tabs-container {
  margin-bottom: 25px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  padding: 8px;
  overflow-x: auto; /* Allow horizontal scroll for tabs on small screens */
}
.tabs {
  display: flex;
  justify-content: flex-start;
  border-bottom: 2px solid #dee2e6;
  min-width: fit-content; /* Ensure tabs don't collapse too much */
}
.tab-btn {
  padding: 12px 25px;
  font-size: 1.05em;
  font-weight: 500;
  color: #495057;
  background-color: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease;
  margin-bottom: -2px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap; /* Prevent tab text from wrapping */
}
.tab-btn:hover {
  color: #007bff;
}
.tab-btn.active {
  color: #007bff;
  border-bottom-color: #007bff;
  font-weight: 600;
}
.tab-btn i[class^="icon-"] {
  font-size: 1.2em;
}

/* --- Filter Bar --- */
.controls-container.filter-bar {
  background-color: #fff;
  padding: 20px 25px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
  margin-bottom: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-grow: 1;
  min-width: 250px; /* Adjusted for better layout */
}
.filter-group label {
  font-weight: 500;
  color: #495057;
  white-space: nowrap;
  margin-bottom: 0; /* Align with select */
}
.filter-group .form-control {
  flex-grow: 1;
}

/* --- 表单控件统一样式 --- */
.form-control {
  display: block;
  width: 100%;
  padding: 10px 15px;
  font-size: 1em;
  font-weight: 400;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  appearance: none;
  border-radius: 6px;
  transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out;
  box-sizing: border-box;
}
.form-control:focus {
  color: #495057;
  background-color: #fff;
  border-color: #86b7fe;
  outline: 0;
  box-shadow: 0 0 0 0.25rem rgba(13,110,253,.25);
}
select.form-control {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m2 5 6 6 6-6'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 0.9rem center;
  background-size: 16px 12px;
  cursor: pointer;
}

/* --- 按钮通用样式 --- */
.btn {
  padding: 10px 18px; border: none; border-radius: 6px; cursor: pointer;
  font-size: 0.95em; font-weight: 500; transition: all 0.2s ease-in-out;
  display: inline-flex; align-items: center; justify-content: center;
  gap: 8px; text-decoration: none; line-height: 1.5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}
.btn:hover { opacity: 0.85; transform: translateY(-1px); box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
.btn:active { transform: translateY(0); box-shadow: 0 2px 4px rgba(0,0,0,0.08); }
.btn i[class^="icon-"] { font-size: 1.1em; }

.primary-btn { background-color: #007bff; color: white; }
.primary-btn:hover { background-color: #0069d9; }
.secondary-btn { background-color: #6c757d; color: white; }
.secondary-btn:hover { background-color: #5a6268; }
.secondary-outline-btn {
  background-color: transparent; color: #6c757d; border: 1px solid #6c757d;
}
.secondary-outline-btn:hover { background-color: #6c757d; color: white; }
.danger-btn { background-color: #dc3545; color: white; }
.danger-btn:hover { background-color: #c82333; }

/* --- 考试列表与卡片 --- */
.content-section { margin-top: 20px; }
.paperInfos-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 25px;
}

.paperInfo-card {
  background: white;
  padding: 20px 25px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.07);
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out, border-left-color 0.2s ease-out;
  display: flex;
  flex-direction: column;
  border-left: 5px solid transparent;
}
.paperInfo-card.status-not-started { border-left-color: #17a2b8; } /* Teal */
.paperInfo-card.status-ongoing { border-left-color: #28a745; } /* Green */
.paperInfo-card.status-ended { border-left-color: #6c757d; } /* Gray */

.paperInfo-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}
.card-main-info { flex-grow: 1; }
.paper-title {
  color: #2c3e50;
  font-size: 1.2em;
  font-weight: 600;
  margin-top: 0;
  margin-bottom: 8px; /* Reduced margin */
  word-break: break-word;
}
.paper-meta {
  font-size: 0.85em;
  color: #6c757d;
  margin-bottom: 10px; /* Increased margin */
  line-height: 1.5;
}
.paper-time {
  font-size: 0.9em;
  color: #333;
  margin-bottom: 15px;
  line-height: 1.5;
}
.paper-time.important-time {
  font-weight: 600;
  color: #e67e22; /* Orange for remaining time */
}

.card-actions {
  margin-top: auto; /* Push actions to bottom */
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-start;
}
.card-actions .btn {
  padding: 8px 12px; /* Smaller buttons in cards */
  font-size: 0.9em;
}

/* --- 模态框样式 --- */
.modal-overlay {
  position: fixed; inset: 0; background-color: rgba(0, 0, 0, 0.55);
  display: flex; align-items: center; justify-content: center;
  z-index: 1050; padding: 20px; overflow-y: auto; /* Higher z-index if needed */
}
.modal-dialog {
  background: white; border-radius: 10px; box-shadow: 0 8px 25px rgba(0,0,0,0.15);
  width: 100%; max-width: 500px;
  max-height: 90vh;
  display: flex; flex-direction: column; margin: auto;
}
.modal-header {
  padding: 18px 25px; border-bottom: 1px solid #e9ecef;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.modal-title {
  margin: 0; font-size: 1.4em; font-weight: 600; color: #343a40;
}
.modal-close-btn {
  background: none; border: none; font-size: 1.8em; font-weight: 300;
  color: #6c757d; cursor: pointer; padding: 0; line-height: 1; opacity: 0.7;
}
.modal-close-btn:hover { color: #343a40; opacity: 1; }
.modal-body { padding: 25px; overflow-y: auto; flex-grow: 1; }
.modal-body .form-group { margin-bottom: 20px; }
.modal-body .form-group label { font-size: 0.95em; margin-bottom: 8px; }

.modal-footer {
  padding: 18px 25px; border-top: 1px solid #e9ecef;
  display: flex; justify-content: flex-end; gap: 10px;
  flex-shrink: 0; background-color: #f8f9fa;
  border-bottom-left-radius: 10px; border-bottom-right-radius: 10px;
}
.modal-footer .btn { font-size: 0.95em; }

.alert-modal-dialog { max-width: 400px; }
.alert-modal-dialog .modal-body { text-align: center; padding-top: 30px; padding-bottom: 30px; }
.alert-modal-dialog .modal-body p { margin: 0; font-size: 1.1em; line-height: 1.6; }
.alert-modal-dialog .modal-footer { justify-content: center; }

.datetime-pickers { display: flex; gap: 10px; align-items: center; }
.datetime-pickers span { color: #6c757d; margin: 0 5px; }
.datetime-pickers input[type="datetime-local"].form-control { flex-grow: 1; }
input[type="datetime-local"]::-webkit-calendar-picker-indicator {
  opacity: 0.7; filter: invert(0.3) brightness(0.7); cursor: pointer;
}
input[type="datetime-local"] { color-scheme: light; }

/* --- 空状态与加载状态 --- */
.loading-indicator, .no-results, .no-results-in-tab {
  grid-column: 1 / -1; text-align: center; padding: 40px 20px;
  margin-top: 20px; background-color: #f0f3f5;
  border-radius: 8px; border: 1px dashed #d0d9e0;
}
.loading-indicator p, .no-results p, .no-results-in-tab p {
  margin: 0 0 8px 0; font-size: 1.1em; color: #5a6268;
}
.no-results .sub-text { font-size: 0.9em; color: #868e96; }
.no-results-in-tab {
  border: none; background-color: transparent; padding: 20px; font-style: italic;
  box-shadow: none; margin-top: 10px;
}

/* --- 响应式调整 --- */
@media (max-width: 992px) {
  .paperInfos-list { grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); }
  .filter-group { min-width: calc(50% - 10px); } /* Two filters per row approx */
}

@media (max-width: 768px) {
  .paper-info-management-page { padding: 20px 15px; }
  h1 { font-size: 2em; margin-bottom: 25px; }
  .tabs-container { padding: 5px; }
  .tabs { flex-direction: column; align-items: stretch; border-bottom: none; }
  .tab-btn {
    justify-content: center; border-bottom: 1px solid #dee2e6;
    margin-bottom: 0; border-radius: 6px 6px 0 0; padding: 10px 15px;
  }
  .tab-btn.active {
    border-bottom-color: #007bff; border-left: 3px solid #007bff;
    border-right: 3px solid #007bff; border-top: 1px solid #dee2e6;
  }
  .tab-btn:last-child { border-bottom: 2px solid #dee2e6; }
  .tab-btn.active:last-child { border-bottom-color: #007bff; }

  .controls-container.filter-bar { padding: 15px; flex-direction: column; gap: 15px; }
  .filter-group { flex-direction: column; align-items: stretch; width: 100%; min-width: unset;}
  .filter-group label { margin-bottom: 5px; }
  .filter-group .form-control { width: 100%; }

  .paperInfos-list { grid-template-columns: 1fr; gap: 15px; }
  .paperInfo-card { padding: 15px 20px; }
  .paper-title { font-size: 1.1em; }
  .card-actions { justify-content: space-around; } /* Distribute buttons more evenly */
  .card-actions .btn { flex-grow: 1; max-width: 45%; font-size: 0.85em; padding: 8px 10px;} /* Smaller buttons */


  .modal-dialog { max-width: 95%; margin: 10px; max-height: 95vh; }
  .modal-title { font-size: 1.3em; }
  .modal-body, .modal-header, .modal-footer { padding: 15px 20px; }
  .modal-footer { flex-direction: column; }
  .modal-footer .btn { width: 100%; }
  .datetime-pickers { flex-direction: column; gap: 10px; align-items: stretch; }
}

/* Placeholder icons (replace with actual icon font/SVG classes) */
.icon-future::before { content: "📅"; margin-right: 6px;}
.icon-ongoing::before { content: "⏳"; margin-right: 6px;}
.icon-history::before { content: "📚"; margin-right: 6px;}
.icon-clear::before { content: "✗"; margin-right: 5px; font-weight: bold;}
.icon-edit::before { content: "✏️"; margin-right: 6px;}
.icon-time::before { content: "🕒"; margin-right: 6px;}
.icon-delete::before { content: "🗑️"; margin-right: 6px;}
.icon-report::before { content: "📊"; margin-right: 6px;}
.icon-check::before { content: "✓"; margin-right: 6px;}
</style>