<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Deployment Request #{{ id }}</h1>
      <router-link to="/requests" class="btn btn-secondary">← Back</router-link>
    </div>

    <div v-if="store.loading" class="loading card">Loading...</div>
    <div v-else-if="!req" class="card">Request not found.</div>

    <template v-else>
      <!-- Details Card -->
      <div class="card">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">Application</span>
            <span class="detail-value">{{ req.applicationName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Status</span>
            <span :class="`badge badge-${req.status?.toLowerCase()}`">{{ req.status }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Component</span>
            <span class="detail-value">{{ req.componentArtifact?.componentName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Artifact Version</span>
            <span class="detail-value">{{ req.componentArtifact?.artifactVersion }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Deployment Method</span>
            <span class="detail-value">{{ req.deploymentMethod?.name }} ({{ req.deploymentMethod?.targetEnvironment }})</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Requested By</span>
            <span class="detail-value">{{ req.requestedBy?.fullName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Actioned By</span>
            <span class="detail-value">{{ req.actionedBy?.fullName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Created</span>
            <span class="detail-value">{{ formatDate(req.createdAt) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">Deployed At</span>
            <span class="detail-value">{{ formatDate(req.deployedAt) || '-' }}</span>
          </div>
          <div class="detail-item" v-if="req.udeployRequestId">
            <span class="detail-label">uDeploy Request ID</span>
            <span class="detail-value">{{ req.udeployRequestId }}</span>
          </div>
          <div class="detail-item" v-if="req.previousDeploymentRequestId">
            <span class="detail-label">Previous Deployment</span>
            <router-link :to="`/requests/${req.previousDeploymentRequestId}`">
              #{{ req.previousDeploymentRequestId }}
            </router-link>
          </div>
        </div>

        <div v-if="req.requestNotes" class="notes-section">
          <strong>Request Notes:</strong>
          <p>{{ req.requestNotes }}</p>
        </div>
        <div v-if="req.actionNotes" class="notes-section">
          <strong>Action Notes:</strong>
          <p>{{ req.actionNotes }}</p>
        </div>
      </div>

      <!-- Action Card (only for PENDING) -->
      <div v-if="req.status === 'PENDING'" class="card mt-2">
        <h2>Action Request</h2>
        <div class="form-group">
          <label>Actioned By (Approver)</label>
          <select v-model="actionForm.actionedByUserId" class="form-input">
            <option value="" disabled>Select approver</option>
            <option v-for="u in approvers" :key="u.id" :value="u.id">{{ u.fullName }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>Notes</label>
          <textarea v-model="actionForm.actionNotes" class="form-input" rows="2"></textarea>
        </div>
        <div class="action-buttons">
          <button class="btn btn-success" @click="action(true)" :disabled="actioning">✓ Approve</button>
          <button class="btn btn-danger" @click="action(false)" :disabled="actioning">✗ Reject</button>
        </div>
        <div v-if="actionError" class="error-msg">{{ actionError }}</div>
      </div>

      <!-- Trigger Card (only for APPROVED) -->
      <div v-if="req.status === 'APPROVED'" class="card mt-2">
        <h2>Trigger Deployment</h2>
        <div class="form-group">
          <label>Triggered By</label>
          <select v-model="triggerUserId" class="form-input">
            <option value="" disabled>Select approver</option>
            <option v-for="u in approvers" :key="u.id" :value="u.id">{{ u.fullName }}</option>
          </select>
        </div>
        <button class="btn btn-primary" @click="triggerDeploy" :disabled="triggering || !triggerUserId">
          {{ triggering ? 'Triggering...' : '🚀 Trigger Deployment' }}
        </button>
        <div v-if="triggerError" class="error-msg">{{ triggerError }}</div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useDeploymentStore } from '../stores/deploymentStore'
import { userService } from '../services/api'

const route = useRoute()
const store = useDeploymentStore()
const id = computed(() => route.params.id)
const req = computed(() => store.currentRequest)

const users = ref([])
const approvers = computed(() => users.value.filter(u => u.role === 'DEPLOYMENT_APPROVER'))

const actionForm = ref({ actionedByUserId: '', actionNotes: '', approved: null })
const actioning = ref(false)
const actionError = ref('')

const triggerUserId = ref('')
const triggering = ref(false)
const triggerError = ref('')

onMounted(async () => {
  await store.fetchById(id.value)
  const { data } = await userService.getAll()
  users.value = data
})

async function action(approved) {
  if (!actionForm.value.actionedByUserId) { actionError.value = 'Please select an approver.'; return }
  actioning.value = true
  actionError.value = ''
  try {
    await store.action(id.value, { ...actionForm.value, approved })
  } catch (e) {
    actionError.value = e.response?.data?.message || 'Action failed'
  } finally {
    actioning.value = false
  }
}

async function triggerDeploy() {
  triggering.value = true
  triggerError.value = ''
  try {
    await store.trigger(id.value, triggerUserId.value)
  } catch (e) {
    triggerError.value = e.response?.data?.message || 'Trigger failed'
  } finally {
    triggering.value = false
  }
}

function formatDate(d) {
  return d ? new Date(d).toLocaleString() : null
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.page-title { font-size: 1.6rem; font-weight: 700; }
.detail-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; }
.detail-item { display: flex; flex-direction: column; gap: 0.25rem; }
.detail-label { font-size: 0.75rem; text-transform: uppercase; color: #888; font-weight: 600; }
.detail-value { font-size: 0.95rem; }
.notes-section { margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #eee; }
.notes-section p { margin-top: 0.25rem; color: #555; }
.mt-2 { margin-top: 1.5rem; }
.mt-2 h2 { margin-bottom: 1rem; font-size: 1.1rem; }
.form-group { margin-bottom: 1rem; }
label { display: block; font-size: 0.85rem; font-weight: 600; color: #555; margin-bottom: 0.4rem; }
.form-input { width: 100%; padding: 0.55rem 0.75rem; border: 1px solid #ddd; border-radius: 6px; font-size: 0.95rem; }
.action-buttons { display: flex; gap: 0.75rem; margin-top: 0.5rem; }
.loading { padding: 2rem; text-align: center; color: #666; }
.error-msg { background: #fde; color: #c0392b; padding: 0.75rem; border-radius: 6px; margin-top: 1rem; font-size: 0.9rem; }
</style>
