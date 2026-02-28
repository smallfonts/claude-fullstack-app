<template>
  <div>
    <h1 class="page-title">Dashboard</h1>

    <div class="stats-grid">
      <div class="card stat-card">
        <div class="stat-value pending">{{ counts.PENDING }}</div>
        <div class="stat-label">Pending</div>
      </div>
      <div class="card stat-card">
        <div class="stat-value approved">{{ counts.APPROVED }}</div>
        <div class="stat-label">Approved</div>
      </div>
      <div class="card stat-card">
        <div class="stat-value in-progress">{{ counts.IN_PROGRESS }}</div>
        <div class="stat-label">In Progress</div>
      </div>
      <div class="card stat-card">
        <div class="stat-value success">{{ counts.SUCCESS }}</div>
        <div class="stat-label">Successful</div>
      </div>
      <div class="card stat-card">
        <div class="stat-value failed">{{ counts.FAILED }}</div>
        <div class="stat-label">Failed</div>
      </div>
    </div>

    <div class="card mt-2">
      <h2>Pending Approvals</h2>
      <div v-if="store.loading" class="loading">Loading...</div>
      <div v-else-if="store.pendingRequests.length === 0" class="empty">No pending requests.</div>
      <table v-else>
        <thead>
          <tr>
            <th>#</th>
            <th>Application</th>
            <th>Artifact</th>
            <th>Requested By</th>
            <th>Created</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in store.pendingRequests" :key="r.id">
            <td>{{ r.id }}</td>
            <td><strong>{{ r.applicationName }}</strong></td>
            <td>{{ r.componentArtifact?.componentName }} @ {{ r.componentArtifact?.artifactVersion }}</td>
            <td>{{ r.requestedBy?.fullName }}</td>
            <td>{{ formatDate(r.createdAt) }}</td>
            <td>
              <router-link :to="`/requests/${r.id}`" class="btn btn-primary" style="font-size:0.8rem">
                Review
              </router-link>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useDeploymentStore } from '../stores/deploymentStore'

const store = useDeploymentStore()

const counts = computed(() => store.statusCounts)

onMounted(async () => {
  await store.fetchAll()
  await store.fetchPending()
})

function formatDate(d) {
  return d ? new Date(d).toLocaleString() : '-'
}
</script>

<script>
import { computed } from 'vue'
export default {}
</script>

<style scoped>
.page-title { font-size: 1.6rem; font-weight: 700; margin-bottom: 1.5rem; }
.stats-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 1rem; margin-bottom: 1.5rem; }
.stat-card { text-align: center; }
.stat-value { font-size: 2.5rem; font-weight: 700; }
.stat-label { font-size: 0.85rem; color: #666; margin-top: 0.25rem; }
.pending { color: #f39c12; }
.approved { color: #27ae60; }
.in-progress { color: #2980b9; }
.success { color: #27ae60; }
.failed { color: #e74c3c; }
.mt-2 { margin-top: 1.5rem; }
.mt-2 h2 { margin-bottom: 1rem; font-size: 1.1rem; }
.loading, .empty { padding: 2rem; text-align: center; color: #666; }
</style>
