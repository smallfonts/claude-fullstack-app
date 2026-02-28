<template>
  <div>
    <h1 class="page-title">New Deployment Request</h1>

    <div class="card" style="max-width: 700px;">
      <form @submit.prevent="submit">
        <div class="form-group">
          <label>Application Name</label>
          <div class="input-row">
            <input v-model="form.applicationName" required placeholder="e.g. MyApp" class="form-input" />
            <button type="button" class="btn btn-secondary" @click="syncArtifacts" :disabled="!form.applicationName || syncing">
              {{ syncing ? 'Syncing...' : 'Sync Artifacts' }}
            </button>
          </div>
        </div>

        <div class="form-group">
          <label>Component Artifact</label>
          <select v-model="form.componentArtifactId" required class="form-input">
            <option value="" disabled>Select an artifact</option>
            <option v-for="a in artifacts" :key="a.id" :value="a.id">
              {{ a.componentName }} @ {{ a.artifactVersion }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Deployment Method</label>
          <select v-model="form.deploymentMethodId" required class="form-input">
            <option value="" disabled>Select a method</option>
            <option v-for="m in methods" :key="m.id" :value="m.id">
              {{ m.name }} ({{ m.targetEnvironment }})
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Requested By</label>
          <select v-model="form.requestedByUserId" required class="form-input">
            <option value="" disabled>Select user</option>
            <option v-for="u in users" :key="u.id" :value="u.id" :disabled="u.role === 'READ_ONLY'">
              {{ u.fullName }} ({{ u.role }})
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Notes</label>
          <textarea v-model="form.requestNotes" class="form-input" rows="3" placeholder="Optional notes for this request..."></textarea>
        </div>

        <div v-if="error" class="error-msg">{{ error }}</div>

        <div class="form-actions">
          <router-link to="/requests" class="btn btn-secondary">Cancel</router-link>
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? 'Submitting...' : 'Submit Request' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDeploymentStore } from '../stores/deploymentStore'
import { udeployService, userService } from '../services/api'
import axios from 'axios'

const router = useRouter()
const store = useDeploymentStore()

const form = ref({
  applicationName: '',
  componentArtifactId: '',
  deploymentMethodId: '',
  requestedByUserId: '',
  requestNotes: ''
})

const artifacts = ref([])
const methods = ref([])
const users = ref([])
const syncing = ref(false)
const submitting = ref(false)
const error = ref('')

onMounted(async () => {
  try {
    const [usersRes, methodsRes] = await Promise.all([
      userService.getAll(),
      axios.get('/api/deployment-methods')
    ])
    users.value = usersRes.data
    methods.value = methodsRes.data
  } catch (e) {
    // methods endpoint may not exist yet — silently ignore
    try {
      const usersRes = await userService.getAll()
      users.value = usersRes.data
    } catch {}
  }
})

async function syncArtifacts() {
  syncing.value = true
  error.value = ''
  try {
    const { data } = await udeployService.syncArtifacts(form.value.applicationName)
    artifacts.value = data
  } catch (e) {
    error.value = 'Failed to sync artifacts from uDeploy. Check application name and uDeploy config.'
  } finally {
    syncing.value = false
  }
}

async function submit() {
  submitting.value = true
  error.value = ''
  try {
    await store.create({
      applicationName: form.value.applicationName,
      componentArtifactId: Number(form.value.componentArtifactId),
      deploymentMethodId: Number(form.value.deploymentMethodId),
      requestedByUserId: Number(form.value.requestedByUserId),
      requestNotes: form.value.requestNotes
    })
    router.push('/requests')
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to create deployment request'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-title { font-size: 1.6rem; font-weight: 700; margin-bottom: 1.5rem; }
.form-group { margin-bottom: 1.25rem; }
label { display: block; font-size: 0.85rem; font-weight: 600; color: #555; margin-bottom: 0.4rem; }
.form-input { width: 100%; padding: 0.55rem 0.75rem; border: 1px solid #ddd; border-radius: 6px; font-size: 0.95rem; }
.input-row { display: flex; gap: 0.5rem; }
.input-row .form-input { flex: 1; }
.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; }
.error-msg { background: #fde; color: #c0392b; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.9rem; }
</style>
