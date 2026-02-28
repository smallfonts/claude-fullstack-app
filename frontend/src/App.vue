<template>
  <div id="app-container">
    <nav class="navbar">
      <div class="nav-brand">🚀 DeployTrack</div>
      <div class="nav-links">
        <router-link to="/">Dashboard</router-link>
        <router-link to="/requests">Deployment Requests</router-link>
        <router-link to="/requests/new">New Request</router-link>
        <router-link to="/artifacts">Artifacts</router-link>
      </div>
    </nav>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: #f5f7fa; color: #2c3e50; }
#app-container { min-height: 100vh; }

.navbar {
  background: #1a1a2e;
  color: white;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  height: 60px;
  gap: 2rem;
}
.nav-brand { font-size: 1.2rem; font-weight: 700; color: #e94560; }
.nav-links { display: flex; gap: 1.5rem; }
.nav-links a { color: #ccc; text-decoration: none; font-size: 0.95rem; transition: color 0.2s; }
.nav-links a:hover, .nav-links a.router-link-active { color: white; }

.main-content { padding: 2rem; max-width: 1400px; margin: 0 auto; }

/* Shared utility styles */
.card { background: white; border-radius: 8px; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.btn { padding: 0.5rem 1.2rem; border: none; border-radius: 6px; cursor: pointer; font-size: 0.9rem; font-weight: 500; transition: opacity 0.2s; }
.btn:hover { opacity: 0.85; }
.btn-primary { background: #e94560; color: white; }
.btn-success { background: #27ae60; color: white; }
.btn-danger { background: #e74c3c; color: white; }
.btn-secondary { background: #95a5a6; color: white; }

.badge { display: inline-block; padding: 0.25rem 0.6rem; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
.badge-pending { background: #fff3cd; color: #856404; }
.badge-approved { background: #d4edda; color: #155724; }
.badge-rejected { background: #f8d7da; color: #721c24; }
.badge-in_progress { background: #cce5ff; color: #004085; }
.badge-success { background: #d4edda; color: #155724; }
.badge-failed { background: #f8d7da; color: #721c24; }

table { width: 100%; border-collapse: collapse; }
th, td { padding: 0.75rem 1rem; text-align: left; border-bottom: 1px solid #eee; }
th { font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: #666; }
tr:hover td { background: #fafafa; }
</style>
