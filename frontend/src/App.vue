<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const emptyForm = {
  id: null,
  title: '',
  repository: '',
  link: '',
  status: 'OPEN',
  assigneeUsername: ''
}

const statuses = [
  { value: 'OPEN', label: 'Ouvert', icon: 'bi-stars' },
  { value: 'IN_PROGRESS', label: 'En cours', icon: 'bi-lightning-charge-fill' },
  { value: 'DONE', label: 'Terminé', icon: 'bi-check2-circle' },
  { value: 'ARCHIVED', label: 'Archivé', icon: 'bi-archive' }
]

const tickets = ref([])
const users = ref([])
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
      ticket.repository.toLowerCase().includes(query) ||
      ticket.assignee?.username?.toLowerCase().includes(query)
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

onMounted(loadInitialData)

async function loadInitialData() {
  loading.value = true
  error.value = ''
  try {
    await Promise.all([loadUsers(), loadTickets()])
    setDefaultAssignee()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  const response = await fetch('/api/users')
  if (!response.ok) {
    throw new Error('Impossible de charger les utilisateurs')
  }
  users.value = await response.json()
}

async function loadTickets() {
  error.value = ''
  try {
    const response = await fetch('/api/tickets')
    if (!response.ok) {
      throw new Error('Impossible de charger les tickets')
    }
    tickets.value = await response.json()
  } catch (err) {
    error.value = err.message
  }
}

async function saveTicket() {
  saving.value = true
  error.value = ''
  const payload = {
    title: form.title.trim(),
    repository: form.repository.trim(),
    link: form.link.trim(),
    status: form.status,
    assigneeUsername: form.assigneeUsername
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
      throw new Error("Le ticket n'a pas pu être enregistré. Vérifiez le dépôt, le lien et l'assignation.")
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
  if (!confirm(`Supprimer « ${ticket.title} » ?`)) {
    return
  }
  error.value = ''
  try {
    const response = await fetch(`/api/tickets/${ticket.id}`, { method: 'DELETE' })
    if (!response.ok) {
      throw new Error("Le ticket n'a pas pu être supprimé")
    }
    await loadTickets()
  } catch (err) {
    error.value = err.message
  }
}

function editTicket(ticket) {
  Object.assign(form, {
    id: ticket.id,
    title: ticket.title,
    repository: ticket.repository,
    link: ticket.link,
    status: ticket.status,
    assigneeUsername: ticket.assignee?.username ?? ''
  })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function resetForm() {
  Object.assign(form, emptyForm)
  setDefaultAssignee()
}

function statusMeta(statusValue) {
  return statuses.find((status) => status.value === statusValue) ?? statuses[0]
}

function setDefaultAssignee() {
  if (!form.assigneeUsername && users.value.length > 0) {
    form.assigneeUsername = users.value[0].username
  }
}
</script>

<template>
  <main class="app-shell">
    <div class="enterprise-shell">
      <nav class="topbar">
        <a class="brand-mark" href="#" aria-label="Accueil d'OSS Ticket Radar">
          <span class="brand-icon"><i class="bi bi-grid-1x2-fill"></i></span>
          <span>
            <strong>OSS Ticket Radar</strong>
            <small>Portefeuille de contributions</small>
          </span>
        </a>
        <div class="topbar-actions">
          <span class="environment-pill"><i class="bi bi-shield-check"></i> Espace de travail entreprise</span>
          <button class="btn btn-outline-secondary" type="button" @click="loadTickets">
            <i class="bi bi-arrow-clockwise"></i> Synchroniser
          </button>
          <a class="btn btn-primary" href="#ticket-form">
            <i class="bi bi-plus-lg"></i> Nouveau ticket
          </a>
        </div>
      </nav>

      <section class="container-fluid dashboard-layout">
        <aside class="sidebar-panel">
          <div class="sidebar-section">
            <span class="sidebar-label">Espace de travail</span>
            <a class="sidebar-link active" href="#"><i class="bi bi-kanban"></i> Tableau radar</a>
            <a class="sidebar-link" href="#ticket-form"><i class="bi bi-pencil-square"></i> Éditeur de ticket</a>
          </div>
          <div class="sidebar-section">
            <span class="sidebar-label">Gouvernance</span>
            <div class="compliance-card">
              <i class="bi bi-patch-check-fill"></i>
              <strong>Sources sélectionnées</strong>
              <span>Le dépôt, les URL d'issues et les responsables sont validés avant publication.</span>
            </div>
          </div>
        </aside>

        <div class="workspace-panel">
          <header class="dashboard-header">
            <div>
              <span class="eyebrow"><i class="bi bi-github"></i> Opérations open source Java</span>
              <h1>Gérez les tickets prêts à contribuer avec une vision claire.</h1>
              <p class="lead">
                Suivez les issues Java open source prometteuses, gardez le contexte des dépôts et
                des responsables visible, et faites avancer le travail jusqu'à la livraison.
              </p>
            </div>
            <div class="executive-card">
              <span class="section-kicker">Santé du portefeuille</span>
              <strong>{{ completionRate }}%</strong>
              <span>taux de réalisation pour {{ users.length }} contributeurs</span>
              <div class="progress" role="progressbar" :aria-valuenow="completionRate" aria-valuemin="0" aria-valuemax="100">
                <div class="progress-bar" :style="{ width: `${completionRate}%` }"></div>
              </div>
            </div>
          </header>

          <div v-if="error" class="alert alert-danger shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill"></i> {{ error }}
          </div>

          <section class="metric-grid" aria-label="Indicateurs de statut des tickets">
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
                    <span class="section-kicker">Saisie des tickets</span>
                    <h2>{{ isEditing ? 'Modifier le ticket' : 'Ajouter un ticket' }}</h2>
                  </div>
                  <button v-if="isEditing" class="btn btn-sm btn-outline-secondary" type="button" @click="resetForm">
                    Annuler
                  </button>
                </div>

                <label class="form-label" for="title">Titre</label>
                <input id="title" v-model="form.title" class="form-control form-control-lg" required />

                <label class="form-label mt-3" for="repository">Dépôt GitHub</label>
                <input
                  id="repository"
                  v-model="form.repository"
                  class="form-control form-control-lg"
                  placeholder="owner/repository"
                  pattern="^[\w.-]+/[\w.-]+$"
                  required
                />

                <label class="form-label mt-3" for="link">Lien de l'issue</label>
                <input id="link" v-model="form.link" class="form-control form-control-lg" type="url" required />

                <label class="form-label mt-3" for="status">Statut</label>
                <select id="status" v-model="form.status" class="form-select form-select-lg">
                  <option v-for="status in statuses" :key="status.value" :value="status.value">
                    {{ status.label }}
                  </option>
                </select>

                <label class="form-label mt-3" for="assignee">Responsable</label>
                <select id="assignee" v-model="form.assigneeUsername" class="form-select form-select-lg" required>
                  <option disabled value="">Sélectionner un utilisateur</option>
                  <option v-for="user in users" :key="user.id" :value="user.username">
                    {{ user.username }}
                  </option>
                </select>

                <button class="btn btn-primary btn-lg w-100 mt-4" type="submit" :disabled="saving">
                  <span v-if="saving" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                  {{ isEditing ? 'Enregistrer les modifications' : 'Ajouter le ticket' }}
                </button>
              </form>
            </aside>

            <section class="col-xl-8">
              <div class="toolbar-card">
                <div>
                  <span class="section-kicker">Tableau radar</span>
                  <h2>Bons tickets pour les projets Java open source</h2>
                  <p>{{ visibleCount }} sur {{ tickets.length }} tickets affichés</p>
                </div>
                <div class="toolbar-controls">
                  <input
                    v-model="search"
                    class="form-control"
                    placeholder="Rechercher par titre, dépôt ou responsable"
                    type="search"
                  />
                  <select v-model="selectedStatus" class="form-select">
                    <option value="ALL">Tous les statuts</option>
                    <option v-for="status in statuses" :key="status.value" :value="status.value">
                      {{ status.label }}
                    </option>
                  </select>
                </div>
              </div>

              <div v-if="loading" class="loading-card">
                <div class="spinner-border text-primary" role="status"></div>
                <span>Chargement des tickets...</span>
              </div>

              <div v-else-if="filteredTickets.length === 0" class="empty-card">
                <i class="bi bi-search"></i>
                <h3>Aucun ticket trouvé</h3>
                <p>Ajustez les filtres ou ajoutez une nouvelle issue GitHub au tableau.</p>
              </div>

              <div v-else class="ticket-grid">
                <article v-for="ticket in filteredTickets" :key="ticket.id" class="ticket-card">
                  <div class="ticket-header">
                    <span class="repo-pill"><i class="bi bi-box-seam"></i> {{ ticket.repository }}</span>
                    <span class="status-pill" :class="ticket.status.toLowerCase().replace('_', '-')">
                      <i :class="['bi', statusMeta(ticket.status).icon]"></i>
                      {{ statusMeta(ticket.status).label }}
                    </span>
                    <span class="assignee-pill">
                      <i class="bi bi-person-check"></i> {{ ticket.assignee?.username ?? 'Non assigné' }}
                    </span>
                  </div>
                  <h3>{{ ticket.title }}</h3>
                  <div class="ticket-actions">
                    <a class="btn btn-sm btn-dark" :href="ticket.link" target="_blank" rel="noreferrer">
                      Ouvrir sur GitHub <i class="bi bi-box-arrow-up-right"></i>
                    </a>
                    <button class="btn btn-sm btn-outline-primary" type="button" @click="editTicket(ticket)">
                      Modifier
                    </button>
                    <button class="btn btn-sm btn-outline-danger" type="button" @click="removeTicket(ticket)">
                      Supprimer
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
