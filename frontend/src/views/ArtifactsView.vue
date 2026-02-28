<template>
  <div>
    <h1 class="page-title">Artifacts</h1>

    <div class="card">
      <div class="sync-bar">
        <input v-model="appName" placeholder="Application name..." class="filter-input" />
        <button class="btn btn-primary" @click="sync" :disabled="!appName || syncing">
          {{ syncing ? 'Syncing...' : '🔄 Sync from uDeploy' }}
        </button>
      </div>
      <div v-if="error" class="error-msg">{{ error }}</div>
    </div>

    <div class="card mt-2">
      <div v-if="syncing" class="loading">Fetching artifacts from uDeploy...</div>
      <div v-else-if="artifacts.length === 0" class="empty">
        Enter an application name and sync to load artifacts from uDeploy.
      </div>
      <table v-else>
        <thead>
          <tr>
            <th>#</th>
            <th>Application</th>
            <th>Component</th>
            <th>Version</th>
            <th>uDeploy Version ID</th>
            <th>Synced At</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in artifacts" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.applicationName }}</td>
            <td>{{ a.componentName }}</td>
            <td><strong>{{ a.artifactVersion }}</strong></td>
            <td class="mono">{{ a.udeployVersionId }}</td>
            <td>{{ formatDate(a.syncedAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { udeployService } from '../services/api'

const appName = ref('')
const artifacts = ref([])
const syncing = ref(false)
const error = ref('')

async function sync() {
  syncing.value = true
  error.value = ''
  try {
    const { data } = await udeployService.syncArtifacts(appName.value)
    artifacts.value = data
  } catch (e) {
    error.value = 'Failed to sync artifacts. Check application name and uDeploy configuration.'
  } finally {
    syncing.value = false
  }
}

function formatDate(d) {
  return d ? new Date(d).toLocaleString() : '-'
}
</script>

<style scoped>
.page-title { font-size: 1.6rem; font-weight: 700; margin-bottom: 1.5rem; }
.sync-bar { display: flex; gap: 1rem; }
.filter-input { flex: 1; padding: 0.55rem 0.75rem; border: 1px solid #ddd; border-radius: 6px; font-size: 0.95rem; }
.loading, .empty { padding: 2rem; text-align: center; color: #666; }
.mt-2 { margin-top: 1.5rem; }
.mono { font-family: monospace; font-size: 0.85rem; color: #666; }
.error-msg { background: #fde; color: #c0392b; padding: 0.75rem; border-radius: 6px; margin-top: 1rem; font-size: 0.9rem; }
</style>
