import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import DeploymentRequestsView from '../views/DeploymentRequestsView.vue'
import NewRequestView from '../views/NewRequestView.vue'
import ArtifactsView from '../views/ArtifactsView.vue'
import RequestDetailView from '../views/RequestDetailView.vue'

const routes = [
  { path: '/', component: DashboardView },
  { path: '/requests', component: DeploymentRequestsView },
  { path: '/requests/new', component: NewRequestView },
  { path: '/requests/:id', component: RequestDetailView },
  { path: '/artifacts', component: ArtifactsView }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
