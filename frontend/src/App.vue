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
  { value: 'OPEN', label: 'Ouvert', icon: 'bi-stars' },
  { value: 'IN_PROGRESS', label: 'En cours', icon: 'bi-lightning-charge-fill' },
  { value: 'DONE', label: 'Terminé', icon: 'bi-check2-circle' },
  { value: 'ARCHIVED', label: 'Archivé', icon: 'bi-archive' }
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

const isEditing = computed(() => form.id !== null)

onMounted(loadTickets)

async function loadTickets() {
  loading.value = true
  error.value = ''
  try {
    const response = await fetch('/api/tickets')
    if (!response.ok) {
      throw new Error('Impossible de charger les tickets')
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
      throw new Error("Impossible d'enregistrer le ticket. Vérifiez le format du dépôt et du lien.")
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
  if (!confirm(`Supprimer "${ticket.title}" ?`)) {
    return
  }
  error.value = ''
  try {
    const response = await fetch(`/api/tickets/${ticket.id}`, { method: 'DELETE' })
    if (!response.ok) {
      throw new Error('Impossible de supprimer le ticket')
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
    <section class="hero">
      <div class="container py-5">
        <div class="row align-items-center g-4">
          <div class="col-lg-7">
            <span class="eyebrow"><i class="bi bi-github"></i> Assistant open source Java</span>
            <h1>Radar de tickets OSS</h1>
            <p class="lead">
              Suivez les issues accessibles des dépôts Java, sélectionnez les meilleures
              opportunités et accompagnez chaque ticket de la découverte à la contribution.
            </p>
            <div class="hero-actions">
              <a class="btn btn-light btn-lg shadow-sm" href="#ticket-form">
                <i class="bi bi-plus-circle"></i> Ajouter un ticket
              </a>
              <button class="btn btn-outline-light btn-lg" type="button" @click="loadTickets">
                <i class="bi bi-arrow-clockwise"></i> Actualiser
              </button>
            </div>
          </div>
          <div class="col-lg-5">
            <div class="glass-card">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <span class="text-white-50">Tickets sélectionnés</span>
                <i class="bi bi-stars fs-3"></i>
              </div>
              <div class="display-3 fw-bold">{{ tickets.length }}</div>
              <p class="mb-0 text-white-50">Initialisés depuis la recherche GitHub MCP pour les projets Java.</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="container content-area">
      <div v-if="error" class="alert alert-danger shadow-sm" role="alert">
        <i class="bi bi-exclamation-triangle-fill"></i> {{ error }}
      </div>

      <div class="row g-4 mb-4">
        <div v-for="stat in stats" :key="stat.value" class="col-6 col-lg-3">
          <button
            type="button"
            class="stat-card"
            :class="{ active: selectedStatus === stat.value }"
            @click="selectedStatus = selectedStatus === stat.value ? 'ALL' : stat.value"
          >
            <i :class="['bi', stat.icon]"></i>
            <span>{{ stat.label }}</span>
            <strong>{{ stat.count }}</strong>
          </button>
        </div>
      </div>

      <div class="row g-4">
        <aside class="col-lg-4">
          <form id="ticket-form" class="editor-card" @submit.prevent="saveTicket">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <div>
                <span class="section-kicker">Éditeur de ticket</span>
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
              placeholder="proprietaire/depot"
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

            <button class="btn btn-primary btn-lg w-100 mt-4" type="submit" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
              {{ isEditing ? 'Enregistrer les modifications' : 'Ajouter le ticket' }}
            </button>
          </form>
        </aside>

        <section class="col-lg-8">
          <div class="toolbar-card">
            <div>
              <span class="section-kicker">Tableau radar</span>
              <h2>Bons tickets pour les projets Java OSS</h2>
            </div>
            <div class="toolbar-controls">
              <input
                v-model="search"
                class="form-control"
                placeholder="Rechercher un titre ou un dépôt"
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
    </section>
  </main>
</template>
