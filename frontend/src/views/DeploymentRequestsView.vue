<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Deployment Requests</h1>
      <router-link to="/requests/new" class="btn btn-primary">+ New Request</router-link>
    </div>

    <div class="card">
      <div class="filters">
        <input v-model="appFilter" placeholder="Filter by application..." class="filter-input" />
        <select v-model="statusFilter" class="filter-select">
          <option value="">All Statuses</option>
          <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
        </select>
      </div>

      <div v-if="store.loading" class="loading">Loading...</div>
      <div v-else-if="filtered.length === 0" class="empty">No deployment requests found.</div>
      <table v-else>
        <thead>
          <tr>
            <th>#</th>
            <th>Application</th>
            <th>Artifact</th>
            <th>Environment</th>
            <th>Status</th>
            <th>Requested By</th>
            <th>Created</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in filtered" :key="r.id">
            <td>{{ r.id }}</td>
            <td><strong>{{ r.applicationName }}</strong></td>
            <td>{{ r.componentArtifact?.componentName }} @ {{ r.componentArtifact?.artifactVersion }}</td>
            <td>{{ r.deploymentMethod?.targetEnvironment || '-' }}</td>
            <td>
              <span :class="`badge badge-${r.status?.toLowerCase()}`">{{ r.status }}</span>
            </td>
            <td>{{ r.requestedBy?.fullName }}</td>
            <td>{{ formatDate(r.createdAt) }}</td>
            <td>
              <router-link :to="`/requests/${r.id}`" class="btn btn-secondary" style="font-size:0.8rem">
                View
              </router-link>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useDeploymentStore } from '../stores/deploymentStore'

const store = useDeploymentStore()
const appFilter = ref('')
const statusFilter = ref('')
const statuses = ['PENDING', 'APPROVED', 'REJECTED', 'IN_PROGRESS', 'SUCCESS', 'FAILED', 'CANCELLED']

const filtered = computed(() =>
  store.requests.filter(r => {
    const matchApp = !appFilter.value || r.applicationName?.toLowerCase().includes(appFilter.value.toLowerCase())
    const matchStatus = !statusFilter.value || r.status === statusFilter.value
    return matchApp && matchStatus
  })
)

onMounted(() => store.fetchAll())

function formatDate(d) {
  return d ? new Date(d).toLocaleString() : '-'
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.page-title { font-size: 1.6rem; font-weight: 700; }
.filters { display: flex; gap: 1rem; margin-bottom: 1rem; }
.filter-input, .filter-select {
  padding: 0.5rem 0.75rem; border: 1px solid #ddd; border-radius: 6px;
  font-size: 0.9rem; outline: none;
}
.filter-input { flex: 1; }
.loading, .empty { padding: 2rem; text-align: center; color: #666; }
</style>
