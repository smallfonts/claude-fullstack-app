import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// ---- Deployment Requests ----
export const deploymentRequestService = {
  getAll: (applicationName) =>
    api.get('/deployment-requests', { params: applicationName ? { applicationName } : {} }),

  getPending: () => api.get('/deployment-requests/pending'),

  getById: (id) => api.get(`/deployment-requests/${id}`),

  create: (data) => api.post('/deployment-requests', data),

  action: (id, data) => api.post(`/deployment-requests/${id}/action`, data),

  trigger: (id, userId) =>
    api.post(`/deployment-requests/${id}/trigger`, null, { params: { userId } })
}

// ---- uDeploy Integration ----
export const udeployService = {
  getComponents: (applicationName) =>
    api.get(`/udeploy/applications/${applicationName}/components`),

  getVersions: (componentId) =>
    api.get(`/udeploy/components/${componentId}/versions`),

  syncArtifacts: (applicationName) =>
    api.post(`/udeploy/applications/${applicationName}/sync`),

  getDeploymentStatus: (udeployRequestId) =>
    api.get(`/udeploy/requests/${udeployRequestId}/status`)
}

// ---- Users ----
export const userService = {
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  remove: (id) => api.delete(`/users/${id}`)
}

export default api
