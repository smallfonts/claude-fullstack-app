<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">User Management</h1>
      <button v-if="!showForm" class="btn btn-primary" @click="openCreateForm">+ Add User</button>
    </div>

    <div v-if="error" class="error-msg">{{ error }}</div>

    <!-- User table -->
    <div v-if="!showForm" class="card">
      <div v-if="loading" class="loading">Loading...</div>
      <div v-else-if="users.length === 0" class="empty">No users found.</div>
      <table v-else>
        <thead>
          <tr>
            <th>#</th>
            <th>Username</th>
            <th>Full Name</th>
            <th>Email</th>
            <th>Roles</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td><strong>{{ u.username }}</strong></td>
            <td>{{ u.fullName }}</td>
            <td>{{ u.email }}</td>
            <td>
              <span
                v-for="r in u.roles"
                :key="r"
                :class="`badge badge-${r.toLowerCase()}`"
                style="margin-right: 4px"
              >{{ r }}</span>
            </td>
            <td class="actions-cell">
              <button class="btn btn-secondary" style="font-size:0.8rem" @click="openEditForm(u)">Edit</button>
              <button class="btn btn-danger" style="font-size:0.8rem" @click="deleteUser(u)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create / Edit form -->
    <div v-if="showForm" class="card" style="max-width: 600px;">
      <h2 class="form-title">{{ editingUser ? 'Edit User' : 'Add User' }}</h2>
      <form @submit.prevent="saveUser">

        <div v-if="!editingUser" class="form-group">
          <label>Username</label>
          <input v-model="form.username" required class="form-input" placeholder="e.g. jsmith" />
        </div>
        <div v-else class="form-group">
          <label>Username</label>
          <div class="form-static">{{ editingUser.username }}</div>
        </div>

        <div class="form-group">
          <label>Full Name</label>
          <input v-model="form.fullName" required class="form-input" placeholder="e.g. Jane Smith" />
        </div>

        <div class="form-group">
          <label>Email</label>
          <input v-model="form.email" required type="email" class="form-input" placeholder="e.g. jane@example.com" />
        </div>

        <div v-if="!editingUser" class="form-group">
          <label>Password Hash</label>
          <input v-model="form.passwordHash" required class="form-input" placeholder="Bcrypt hash or plain text for dev" />
        </div>

        <div class="form-group">
          <label>Roles</label>
          <div class="roles-checkboxes">
            <label v-for="r in allRoles" :key="r" class="checkbox-label">
              <input type="checkbox" :value="r" v-model="form.roles" />
              {{ r }}
            </label>
          </div>
        </div>

        <div v-if="saveError" class="error-msg">{{ saveError }}</div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="cancelForm">Cancel</button>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            {{ saving ? 'Saving...' : (editingUser ? 'Save Changes' : 'Create User') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userService } from '../services/api'

const allRoles = ['READ_ONLY', 'DEPLOYMENT_REQUESTOR', 'DEPLOYMENT_APPROVER', 'ADMINISTRATOR']

const users = ref([])
const loading = ref(false)
const error = ref('')

const showForm = ref(false)
const editingUser = ref(null)
const saving = ref(false)
const saveError = ref('')

const form = ref({
  username: '',
  fullName: '',
  email: '',
  passwordHash: '',
  roles: []
})

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await userService.getAll()
    users.value = data
  } catch {
    error.value = 'Failed to load users.'
  } finally {
    loading.value = false
  }
}

function openCreateForm() {
  editingUser.value = null
  form.value = { username: '', fullName: '', email: '', passwordHash: '', roles: [] }
  saveError.value = ''
  showForm.value = true
}

function openEditForm(u) {
  editingUser.value = u
  form.value = { username: u.username, fullName: u.fullName, email: u.email, passwordHash: '', roles: [...(u.roles || [])] }
  saveError.value = ''
  showForm.value = true
}

function cancelForm() {
  showForm.value = false
  saveError.value = ''
}

async function saveUser() {
  saving.value = true
  saveError.value = ''
  try {
    if (editingUser.value) {
      const { data } = await userService.update(editingUser.value.id, {
        fullName: form.value.fullName,
        email: form.value.email,
        roles: form.value.roles
      })
      const idx = users.value.findIndex(u => u.id === editingUser.value.id)
      if (idx !== -1) users.value[idx] = data
    } else {
      const { data } = await userService.create({
        username: form.value.username,
        fullName: form.value.fullName,
        email: form.value.email,
        passwordHash: form.value.passwordHash,
        roles: form.value.roles
      })
      users.value.push(data)
    }
    showForm.value = false
  } catch (e) {
    if (e.response?.status === 409) {
      saveError.value = editingUser.value ? 'Email is already in use by another user.' : 'Username already exists.'
    } else {
      saveError.value = e.response?.data?.message || 'Failed to save user.'
    }
  } finally {
    saving.value = false
  }
}

async function deleteUser(u) {
  if (!window.confirm(`Delete user "${u.username}"? This cannot be undone.`)) return
  try {
    await userService.remove(u.id)
    users.value = users.value.filter(x => x.id !== u.id)
  } catch {
    error.value = `Failed to delete user "${u.username}".`
  }
}
</script>

<style scoped>
.page-header  { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.page-title   { font-size: 1.6rem; font-weight: 700; }
.form-title   { font-size: 1.1rem; font-weight: 700; margin-bottom: 1.25rem; }
.form-group   { margin-bottom: 1.25rem; }
label         { display: block; font-size: 0.85rem; font-weight: 600; color: #555; margin-bottom: 0.4rem; }
.form-input   { width: 100%; padding: 0.55rem 0.75rem; border: 1px solid #ddd; border-radius: 6px; font-size: 0.95rem; }
.form-static  { padding: 0.55rem 0; font-size: 0.95rem; color: #555; }
.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; }
.actions-cell { display: flex; gap: 0.5rem; }
.loading, .empty { padding: 2rem; text-align: center; color: #666; }
.error-msg    { background: #fde; color: #c0392b; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.9rem; }

.roles-checkboxes { display: flex; flex-wrap: wrap; gap: 0.75rem; }
.checkbox-label   { display: flex; align-items: center; gap: 0.35rem; font-size: 0.9rem; font-weight: 400; color: #333; cursor: pointer; }
.checkbox-label input[type="checkbox"] { width: 15px; height: 15px; cursor: pointer; }

/* Role badge variants */
.badge-read_only            { background: #e9ecef; color: #495057; }
.badge-deployment_requestor { background: #cce5ff; color: #004085; }
.badge-deployment_approver  { background: #d4edda; color: #155724; }
.badge-administrator        { background: #f3e5ff; color: #6f42c1; }
</style>
