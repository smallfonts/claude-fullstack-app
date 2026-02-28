import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import DeploymentRequestsView from '../views/DeploymentRequestsView.vue'
import NewRequestView from '../views/NewRequestView.vue'
import ArtifactsView from '../views/ArtifactsView.vue'
import RequestDetailView from '../views/RequestDetailView.vue'
import UserManagementView from '../views/UserManagementView.vue'

const routes = [
  { path: '/', component: DashboardView },
  { path: '/requests', component: DeploymentRequestsView },
  { path: '/requests/new', component: NewRequestView },
  { path: '/requests/:id', component: RequestDetailView },
  { path: '/artifacts', component: ArtifactsView },
  { path: '/users', component: UserManagementView }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
