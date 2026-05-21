<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const emptyForm = {
  id: null,
  title: '',
  repository: '',
  link: '',
  status: 'OPEN'
}

const statuses = [
  { value: 'OPEN', label: 'Open', icon: 'bi-stars' },
  { value: 'IN_PROGRESS', label: 'In progress', icon: 'bi-lightning-charge-fill' },
  { value: 'DONE', label: 'Done', icon: 'bi-check2-circle' },
  { value: 'ARCHIVED', label: 'Archived', icon: 'bi-archive' }
]

const tickets = ref([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const selectedStatus = ref('ALL')
const search = ref('')
const form = reactive({ ...emptyForm })

const filteredTickets = computed(() => {
  const query = search.value.trim().toLowerCase()
  return tickets.value.filter((ticket) => {
    const matchesStatus = selectedStatus.value === 'ALL' || ticket.status === selectedStatus.value
    const matchesQuery =
      !query ||
      ticket.title.toLowerCase().includes(query) ||
      ticket.repository.toLowerCase().includes(query)
    return matchesStatus && matchesQuery
  })
})

const stats = computed(() =>
  statuses.map((status) => ({
    ...status,
    count: tickets.value.filter((ticket) => ticket.status === status.value).length
  }))
)

const visibleCount = computed(() => filteredTickets.value.length)
const completionRate = computed(() => {
  if (tickets.value.length === 0) {
    return 0
  }
  const completedTickets = tickets.value.filter((ticket) => ticket.status === 'DONE').length
  return Math.round((completedTickets / tickets.value.length) * 100)
})

const isEditing = computed(() => form.id !== null)

onMounted(loadTickets)

async function loadTickets() {
  loading.value = true
  error.value = ''
  try {
    const response = await fetch('/api/tickets')
    if (!response.ok) {
      throw new Error('Could not load tickets')
    }
    tickets.value = await response.json()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function saveTicket() {
  saving.value = true
  error.value = ''
  const payload = {
    title: form.title.trim(),
    repository: form.repository.trim(),
    link: form.link.trim(),
    status: form.status
  }
  const url = isEditing.value ? `/api/tickets/${form.id}` : '/api/tickets'
  const method = isEditing.value ? 'PUT' : 'POST'

  try {
    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    if (!response.ok) {
      throw new Error('Ticket could not be saved. Check the repository and link format.')
    }
    resetForm()
    await loadTickets()
  } catch (err) {
    error.value = err.message
  } finally {
    saving.value = false
  }
}

async function removeTicket(ticket) {
  if (!confirm(`Remove "${ticket.title}"?`)) {
    return
  }
  error.value = ''
  try {
    const response = await fetch(`/api/tickets/${ticket.id}`, { method: 'DELETE' })
    if (!response.ok) {
      throw new Error('Ticket could not be removed')
    }
    await loadTickets()
  } catch (err) {
    error.value = err.message
  }
}

function editTicket(ticket) {
  Object.assign(form, ticket)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function resetForm() {
  Object.assign(form, emptyForm)
}

function statusMeta(statusValue) {
  return statuses.find((status) => status.value === statusValue) ?? statuses[0]
}
</script>

<template>
  <main class="app-shell">
    <div class="enterprise-shell">
      <nav class="topbar">
        <a class="brand-mark" href="#" aria-label="OSS Ticket Radar home">
          <span class="brand-icon"><i class="bi bi-grid-1x2-fill"></i></span>
          <span>
            <strong>OSS Ticket Radar</strong>
            <small>Contribution portfolio</small>
          </span>
        </a>
        <div class="topbar-actions">
          <span class="environment-pill"><i class="bi bi-shield-check"></i> Enterprise workspace</span>
          <button class="btn btn-outline-secondary" type="button" @click="loadTickets">
            <i class="bi bi-arrow-clockwise"></i> Sync
          </button>
          <a class="btn btn-primary" href="#ticket-form">
            <i class="bi bi-plus-lg"></i> New ticket
          </a>
        </div>
      </nav>

      <section class="container-fluid dashboard-layout">
        <aside class="sidebar-panel">
          <div class="sidebar-section">
            <span class="sidebar-label">Workspace</span>
            <a class="sidebar-link active" href="#"><i class="bi bi-kanban"></i> Radar board</a>
            <a class="sidebar-link" href="#ticket-form"><i class="bi bi-pencil-square"></i> Ticket editor</a>
          </div>
          <div class="sidebar-section">
            <span class="sidebar-label">Governance</span>
            <div class="compliance-card">
              <i class="bi bi-patch-check-fill"></i>
              <strong>Curated sources</strong>
              <span>Repository and issue URLs are validated before publishing.</span>
            </div>
          </div>
        </aside>

        <div class="workspace-panel">
          <header class="dashboard-header">
            <div>
              <span class="eyebrow"><i class="bi bi-github"></i> Java open source operations</span>
              <h1>Manage contribution-ready tickets with portfolio-grade clarity.</h1>
              <p class="lead">
                Track promising Java OSS issues, keep repository context visible, and move work
                through a controlled discovery-to-delivery workflow.
              </p>
            </div>
            <div class="executive-card">
              <span class="section-kicker">Portfolio health</span>
              <strong>{{ completionRate }}%</strong>
              <span>completion rate</span>
              <div class="progress" role="progressbar" :aria-valuenow="completionRate" aria-valuemin="0" aria-valuemax="100">
                <div class="progress-bar" :style="{ width: `${completionRate}%` }"></div>
              </div>
            </div>
          </header>

          <div v-if="error" class="alert alert-danger shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill"></i> {{ error }}
          </div>

          <section class="metric-grid" aria-label="Ticket status metrics">
            <button
              v-for="stat in stats"
              :key="stat.value"
              type="button"
              class="stat-card"
              :class="{ active: selectedStatus === stat.value }"
              @click="selectedStatus = selectedStatus === stat.value ? 'ALL' : stat.value"
            >
              <span class="stat-icon"><i :class="['bi', stat.icon]"></i></span>
              <span>{{ stat.label }}</span>
              <strong>{{ stat.count }}</strong>
            </button>
          </section>

          <div class="row g-4">
            <aside class="col-xl-4">
              <form id="ticket-form" class="editor-card" @submit.prevent="saveTicket">
                <div class="panel-heading">
                  <div>
                    <span class="section-kicker">Ticket intake</span>
                    <h2>{{ isEditing ? 'Edit ticket' : 'Add a ticket' }}</h2>
                  </div>
                  <button v-if="isEditing" class="btn btn-sm btn-outline-secondary" type="button" @click="resetForm">
                    Cancel
                  </button>
                </div>

                <label class="form-label" for="title">Title</label>
                <input id="title" v-model="form.title" class="form-control form-control-lg" required />

                <label class="form-label mt-3" for="repository">GitHub repository</label>
                <input
                  id="repository"
                  v-model="form.repository"
                  class="form-control form-control-lg"
                  placeholder="owner/repository"
                  pattern="^[\w.-]+/[\w.-]+$"
                  required
                />

                <label class="form-label mt-3" for="link">Issue link</label>
                <input id="link" v-model="form.link" class="form-control form-control-lg" type="url" required />

                <label class="form-label mt-3" for="status">Status</label>
                <select id="status" v-model="form.status" class="form-select form-select-lg">
                  <option v-for="status in statuses" :key="status.value" :value="status.value">
                    {{ status.label }}
                  </option>
                </select>

                <button class="btn btn-primary btn-lg w-100 mt-4" type="submit" :disabled="saving">
                  <span v-if="saving" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                  {{ isEditing ? 'Save changes' : 'Add ticket' }}
                </button>
              </form>
            </aside>

            <section class="col-xl-8">
              <div class="toolbar-card">
                <div>
                  <span class="section-kicker">Radar board</span>
                  <h2>Good tickets for Java OSS projects</h2>
                  <p>{{ visibleCount }} of {{ tickets.length }} tickets shown</p>
                </div>
                <div class="toolbar-controls">
                  <input
                    v-model="search"
                    class="form-control"
                    placeholder="Search title or repository"
                    type="search"
                  />
                  <select v-model="selectedStatus" class="form-select">
                    <option value="ALL">All statuses</option>
                    <option v-for="status in statuses" :key="status.value" :value="status.value">
                      {{ status.label }}
                    </option>
                  </select>
                </div>
              </div>

              <div v-if="loading" class="loading-card">
                <div class="spinner-border text-primary" role="status"></div>
                <span>Loading tickets...</span>
              </div>

              <div v-else-if="filteredTickets.length === 0" class="empty-card">
                <i class="bi bi-search"></i>
                <h3>No tickets found</h3>
                <p>Adjust the filters or add a new GitHub issue to the board.</p>
              </div>

              <div v-else class="ticket-grid">
                <article v-for="ticket in filteredTickets" :key="ticket.id" class="ticket-card">
                  <div class="ticket-header">
                    <span class="repo-pill"><i class="bi bi-box-seam"></i> {{ ticket.repository }}</span>
                    <span class="status-pill" :class="ticket.status.toLowerCase().replace('_', '-')">
                      <i :class="['bi', statusMeta(ticket.status).icon]"></i>
                      {{ statusMeta(ticket.status).label }}
                    </span>
                  </div>
                  <h3>{{ ticket.title }}</h3>
                  <div class="ticket-actions">
                    <a class="btn btn-sm btn-dark" :href="ticket.link" target="_blank" rel="noreferrer">
                      Open on GitHub <i class="bi bi-box-arrow-up-right"></i>
                    </a>
                    <button class="btn btn-sm btn-outline-primary" type="button" @click="editTicket(ticket)">
                      Edit
                    </button>
                    <button class="btn btn-sm btn-outline-danger" type="button" @click="removeTicket(ticket)">
                      Remove
                    </button>
                  </div>
                </article>
              </div>
            </section>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
