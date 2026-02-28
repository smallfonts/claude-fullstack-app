import { defineStore } from 'pinia'
import { deploymentRequestService } from '../services/api'

export const useDeploymentStore = defineStore('deployments', {
  state: () => ({
    requests: [],
    pendingRequests: [],
    currentRequest: null,
    loading: false,
    error: null
  }),

  getters: {
    byStatus: (state) => (status) =>
      state.requests.filter(r => r.status === status),

    statusCounts: (state) => ({
      PENDING: state.requests.filter(r => r.status === 'PENDING').length,
      APPROVED: state.requests.filter(r => r.status === 'APPROVED').length,
      IN_PROGRESS: state.requests.filter(r => r.status === 'IN_PROGRESS').length,
      SUCCESS: state.requests.filter(r => r.status === 'SUCCESS').length,
      FAILED: state.requests.filter(r => r.status === 'FAILED').length
    })
  },

  actions: {
    async fetchAll(applicationName) {
      this.loading = true
      this.error = null
      try {
        const { data } = await deploymentRequestService.getAll(applicationName)
        this.requests = data
      } catch (e) {
        this.error = e.response?.data?.message || 'Failed to load deployment requests'
      } finally {
        this.loading = false
      }
    },

    async fetchPending() {
      try {
        const { data } = await deploymentRequestService.getPending()
        this.pendingRequests = data
      } catch (e) {
        this.error = 'Failed to load pending requests'
      }
    },

    async fetchById(id) {
      this.loading = true
      try {
        const { data } = await deploymentRequestService.getById(id)
        this.currentRequest = data
      } catch (e) {
        this.error = 'Failed to load request'
      } finally {
        this.loading = false
      }
    },

    async create(payload) {
      const { data } = await deploymentRequestService.create(payload)
      this.requests.unshift(data)
      return data
    },

    async action(id, payload) {
      const { data } = await deploymentRequestService.action(id, payload)
      const idx = this.requests.findIndex(r => r.id === id)
      if (idx !== -1) this.requests[idx] = data
      if (this.currentRequest?.id === id) this.currentRequest = data
      return data
    },

    async trigger(id, userId) {
      const { data } = await deploymentRequestService.trigger(id, userId)
      const idx = this.requests.findIndex(r => r.id === id)
      if (idx !== -1) this.requests[idx] = data
      if (this.currentRequest?.id === id) this.currentRequest = data
      return data
    }
  }
})
