/**
 * dashboard.js — Dashboard page controller.
 *
 * Responsibilities:
 *  - Enforce admin-only access via guard.js.
 *  - Bootstrap the dashboard: load current user, stats, users table, articles.
 *  - Wire up all interactive UI: sidebar navigation, user management,
 *    article management, file/folder manager, logout.
 *
 * All API calls go through core/api.js which automatically attaches the
 * Authorization header and redirects to /admin/login on 401.
 */

import { get, post, put, del, postForm, ApiError } from '../core/api.js';
import { clearToken } from '../core/token.js';
import { requireAdmin } from '../components/guard.js';

// Guard: abort immediately if not authenticated as admin
if (!requireAdmin()) throw new Error('Unauthorized');

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

let currentUser = null;

const usersState = { page: 0, size: 20, hasNext: false, hasPrevious: false };
const articlesState = { page: 0, size: 10, hasNext: false, hasPrevious: false };
const foldersState = { page: 0, size: 20, hasNext: false, hasPrevious: false };
const filesState   = { page: 0, size: 20, hasNext: false, hasPrevious: false };

// Local cache used by view/edit/delete actions (populated during list loads)
let cachedUsers    = [];
let cachedArticles = [];

// ─────────────────────────────────────────────────────────────────────────────
// Bootstrap
// ─────────────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', async () => {
  await loadCurrentUser();
  await loadDashboardStats();
  loadUsersList(0);
  loadArticles(0);
});

// ─────────────────────────────────────────────────────────────────────────────
// Current user  GET /api/admin/users/me
// ─────────────────────────────────────────────────────────────────────────────

async function loadCurrentUser() {
  try {
    const { data } = await get('/api/admin/users/me');
    currentUser = data;
    document.getElementById('userName').textContent = data.username;
    document.getElementById('userRole').textContent = data.role;
  } catch (err) {
    console.error('Failed to load current user:', err);
    // 401 is already handled by api.js (redirect); handle 403 here
    if (err instanceof ApiError && err.status === 403) {
      clearToken();
      window.location.href = '/admin/login';
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dashboard stats  GET /api/admin/users
// ─────────────────────────────────────────────────────────────────────────────

async function loadDashboardStats() {
  try {
    const { data } = await get('/api/admin/users?page=0&size=1&sortBy=createdAt&sortDirection=desc');
    const total      = data.totalElements ?? 0;
    const { data: activeData }  = await get('/api/admin/users?page=0&size=1&locked=false');
    const { data: adminData }   = await get('/api/admin/users?page=0&size=1&role=ADMIN');
    const { data: lockedData }  = await get('/api/admin/users?page=0&size=1&locked=true');

    document.getElementById('totalUsers').textContent  = total;
    document.getElementById('activeUsers').textContent = activeData.totalElements ?? 0;
    document.getElementById('adminUsers').textContent  = adminData.totalElements  ?? 0;
    document.getElementById('lockedUsers').textContent = lockedData.totalElements ?? 0;
  } catch (err) {
    console.error('Failed to load dashboard stats:', err);
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Users list  GET /api/admin/users
// ─────────────────────────────────────────────────────────────────────────────

async function loadUsersList(page) {
  usersState.page = page;
  usersState.size = parseInt(document.getElementById('usersPageSize').value, 10) || 20;

  const params = new URLSearchParams({ page, size: usersState.size, sortBy: 'createdAt', sortDirection: 'desc' });
  const search = document.getElementById('usersSearch').value.trim();
  const role   = document.getElementById('usersRole').value;
  const locked = document.getElementById('usersLocked').value;
  if (search) params.set('search', search);
  if (role)   params.set('role',   role);
  if (locked) params.set('locked', locked);

  try {
    const { data } = await get(`/api/admin/users?${params}`);
    cachedUsers            = data.content ?? [];
    usersState.hasNext     = data.hasNext     ?? false;
    usersState.hasPrevious = data.hasPrevious ?? false;

    document.getElementById('usersTotal').textContent         = `Total: ${data.totalElements ?? 0}`;
    document.getElementById('usersPageIndicator').textContent = `Page ${page + 1}`;
    document.getElementById('usersPrev').disabled             = !usersState.hasPrevious;
    document.getElementById('usersNext').disabled             = !usersState.hasNext;

    renderUsersTable(cachedUsers);
  } catch (err) {
    console.error('Failed to load users:', err);
  }
}

function renderUsersTable(users) {
  const tbody = document.getElementById('usersTableBody');
  if (!users.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No users found.</td></tr>';
    return;
  }
  tbody.innerHTML = users.map(u => `
    <tr>
      <td class="text-truncate" style="max-width:120px" title="${u.id}">${u.id}</td>
      <td>${u.username}</td>
      <td>${u.email}</td>
      <td><span class="badge bg-${roleBadge(u.role)}">${u.role}</span></td>
      <td><span class="badge bg-${u.locked ? 'danger' : 'success'}">${u.locked ? 'Locked' : 'Active'}</span></td>
      <td>
        <div class="btn-group btn-group-sm">
          <button class="btn btn-outline-primary"  onclick="viewUser('${u.id}')"   title="View"><i class="bi bi-eye"></i></button>
          <button class="btn btn-outline-secondary" onclick="editUser('${u.id}')"   title="Edit"><i class="bi bi-pencil"></i></button>
          <button class="btn btn-outline-${u.locked ? 'success' : 'warning'}" onclick="toggleLock('${u.id}', ${u.locked})" title="${u.locked ? 'Unlock' : 'Lock'}">
            <i class="bi bi-${u.locked ? 'unlock' : 'lock'}"></i>
          </button>
          <button class="btn btn-outline-info"     onclick="changeRole('${u.id}')" title="Change Role"><i class="bi bi-person-badge"></i></button>
          <button class="btn btn-outline-danger"   onclick="deleteUser('${u.id}')" title="Delete"><i class="bi bi-trash"></i></button>
        </div>
      </td>
    </tr>`).join('');
}

function roleBadge(role) {
  return role === 'SUPER_ADMIN' ? 'danger' : role === 'ADMIN' ? 'warning' : 'info';
}

// Pagination
window.usersPrev = () => { if (usersState.hasPrevious) loadUsersList(usersState.page - 1); };
window.usersNext = () => { if (usersState.hasNext)     loadUsersList(usersState.page + 1); };

// ─────────────────────────────────────────────────────────────────────────────
// View user
// ─────────────────────────────────────────────────────────────────────────────

window.viewUser = (id) => {
  const u = cachedUsers.find(x => x.id === id);
  if (!u) return;
  document.getElementById('userDetails').innerHTML = `
    <div class="row">
      <div class="col-md-6">
        <p><strong>ID:</strong> <code>${u.id}</code></p>
        <p><strong>Username:</strong> ${u.username}</p>
        <p><strong>Email:</strong> ${u.email}</p>
      </div>
      <div class="col-md-6">
        <p><strong>Role:</strong> <span class="badge bg-${roleBadge(u.role)}">${u.role}</span></p>
        <p><strong>Status:</strong> <span class="badge bg-${u.locked ? 'danger' : 'success'}">${u.locked ? 'Locked' : 'Active'}</span></p>
        <p><strong>Email Verified:</strong> ${u.emailVerified ? 'Yes' : 'No'}</p>
        <p><strong>Failed Logins:</strong> ${u.failedLoginAttempts}</p>
      </div>
    </div>
    <p><strong>Created:</strong> ${new Date(u.createdAt).toLocaleString()}</p>
    <p><strong>Updated:</strong> ${new Date(u.updatedAt).toLocaleString()}</p>`;
  showSection('users-view');
};

// ─────────────────────────────────────────────────────────────────────────────
// Edit user  PUT /api/admin/users/{id}
// ─────────────────────────────────────────────────────────────────────────────

window.editUser = (id) => {
  const u = cachedUsers.find(x => x.id === id);
  if (!u) return;
  document.getElementById('editUserId').value    = u.id;
  document.getElementById('editUserEmail').value = u.email;
  document.getElementById('editUserRole').value  = u.role;
  showSection('users-edit');
};

document.getElementById('userForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const id    = document.getElementById('editUserId').value;
  const email = document.getElementById('editUserEmail').value.trim();
  const role  = document.getElementById('editUserRole').value;
  try {
    await put(`/api/admin/users/${id}`, { email, role });
    showToast('User updated successfully.', 'success');
    showSection('users-list');
    loadUsersList(usersState.page);
    loadDashboardStats();
  } catch (err) {
    showToast(err.message || 'Failed to update user.', 'danger');
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// Lock / Unlock  POST /api/admin/users/{id}/lock|unlock
// ─────────────────────────────────────────────────────────────────────────────

window.toggleLock = async (id, isCurrentlyLocked) => {
  const action = isCurrentlyLocked ? 'unlock' : 'lock';
  try {
    await post(`/api/admin/users/${id}/${action}`);
    showToast(`User ${action}ed.`, 'success');
    loadUsersList(usersState.page);
    loadDashboardStats();
  } catch (err) {
    showToast(err.message || `Failed to ${action} user.`, 'danger');
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// Change role  PUT /api/admin/users/{id}/role
// ─────────────────────────────────────────────────────────────────────────────

window.changeRole = (id) => {
  const u = cachedUsers.find(x => x.id === id);
  if (!u) return;
  document.getElementById('roleUserId').value      = u.id;
  document.getElementById('roleUserName').textContent = u.username;
  document.getElementById('roleSelect').value      = u.role;
  showSection('users-role');
};

document.getElementById('roleForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const id   = document.getElementById('roleUserId').value;
  const role = document.getElementById('roleSelect').value;
  try {
    await put(`/api/admin/users/${id}/role`, { role });
    showToast('Role updated successfully.', 'success');
    showSection('users-list');
    loadUsersList(usersState.page);
    loadDashboardStats();
  } catch (err) {
    showToast(err.message || 'Failed to update role.', 'danger');
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// Delete user  DELETE /api/admin/users/{id}
// ─────────────────────────────────────────────────────────────────────────────

window.deleteUser = (id) => {
  const u = cachedUsers.find(x => x.id === id);
  if (!u) return;
  document.getElementById('deleteConfirmation').innerHTML = `
    <div class="alert alert-danger">
      <h6>Are you sure you want to delete this user?</h6>
      <p><strong>Username:</strong> ${u.username}</p>
      <p><strong>Email:</strong> ${u.email}</p>
      <p class="mb-0">This action cannot be undone.</p>
    </div>
    <div class="d-flex gap-2">
      <button class="btn btn-danger" onclick="confirmDelete('${u.id}')">Delete User</button>
      <button class="btn btn-secondary" onclick="showSection('users-list')">Cancel</button>
    </div>`;
  showSection('users-delete');
};

window.confirmDelete = async (id) => {
  try {
    await del(`/api/admin/users/${id}`);
    showToast('User deleted.', 'success');
    showSection('users-list');
    loadUsersList(usersState.page);
    loadDashboardStats();
  } catch (err) {
    showToast(err.message || 'Failed to delete user.', 'danger');
  }
};

// ─────────────────────────────────────────────────────────────────────────────
// Articles  GET /api/blog/articles  POST /api/blog/articles  etc.
// ─────────────────────────────────────────────────────────────────────────────

async function loadArticles(page) {
  articlesState.page = page;
  articlesState.size = parseInt(document.getElementById('articlesPageSize').value, 10) || 10;

  try {
    const { data } = await get(`/api/blog/articles?page=${page}&size=${articlesState.size}`);
    cachedArticles            = data.content ?? [];
    articlesState.hasNext     = data.hasNext     ?? false;
    articlesState.hasPrevious = data.hasPrevious ?? false;

    document.getElementById('articlesTotal').textContent         = `Total: ${data.totalElements ?? 0}`;
    document.getElementById('articlesPageIndicator').textContent = `Page ${page + 1}`;
    document.getElementById('articlesPrev').disabled             = !articlesState.hasPrevious;
    document.getElementById('articlesNext').disabled             = !articlesState.hasNext;

    renderArticlesTable(cachedArticles);
  } catch (err) {
    console.error('Failed to load articles:', err);
  }
}

function renderArticlesTable(items) {
  const tbody = document.getElementById('articlesTableBody');
  if (!items.length) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No articles found.</td></tr>';
    return;
  }
  tbody.innerHTML = items.map(a => `
    <tr>
      <td>${a.title}</td>
      <td><code>${a.slug}</code></td>
      <td><span class="badge bg-${a.status === 'PUBLISHED' ? 'success' : 'secondary'}">${a.status}</span></td>
      <td>${a.publishedAt ? new Date(a.publishedAt).toLocaleString() : '—'}</td>
      <td>${a.viewCount ?? 0}</td>
      <td>
        <div class="btn-group btn-group-sm">
          <button class="btn btn-outline-primary"   onclick="publishArticle('${a.id}')"  ${a.status === 'PUBLISHED' ? 'disabled' : ''}>Publish</button>
          <button class="btn btn-outline-secondary" onclick="restoreArticle('${a.id}')">Restore</button>
          <button class="btn btn-outline-danger"    onclick="deleteArticle('${a.id}')">Delete</button>
        </div>
      </td>
    </tr>`).join('');
}

window.articlesPrev = () => { if (articlesState.hasPrevious) loadArticles(articlesState.page - 1); };
window.articlesNext = () => { if (articlesState.hasNext)     loadArticles(articlesState.page + 1); };

window.publishArticle = async (id) => {
  try {
    await post(`/api/blog/articles/${id}/publish`);
    loadArticles(articlesState.page);
  } catch (err) { showToast(err.message || 'Publish failed.', 'danger'); }
};

window.restoreArticle = async (id) => {
  try {
    await post(`/api/blog/articles/${id}/restore`);
    loadArticles(articlesState.page);
  } catch (err) { showToast(err.message || 'Restore failed.', 'danger'); }
};

window.deleteArticle = async (id) => {
  if (!confirm('Delete this article?')) return;
  try {
    await del(`/api/blog/articles/${id}`);
    loadArticles(articlesState.page);
  } catch (err) { showToast(err.message || 'Delete failed.', 'danger'); }
};

document.getElementById('articleForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  try {
    await post('/api/blog/articles', {
      title:   document.getElementById('articleTitle').value,
      content: document.getElementById('articleContent').value,
      excerpt: document.getElementById('articleExcerpt').value,
    });
    showToast('Article created.', 'success');
    e.target.reset();
    showSection('blog-articles');
    loadArticles(0);
  } catch (err) {
    showToast(err.message || 'Failed to create article.', 'danger');
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// Files & Folders
// ─────────────────────────────────────────────────────────────────────────────

async function loadFolderContents() {
  const parentId = document.getElementById('currentFolderId').value.trim();
  const params   = new URLSearchParams({
    pageFolders: foldersState.page, sizeFolders: foldersState.size,
    pageFiles:   filesState.page,   sizeFiles:   filesState.size,
  });
  if (parentId) params.set('parentId', parentId);

  try {
    const { data } = await get(`/api/folders/contents/paged?${params}`);

    const folders = data.folders?.content ?? [];
    const files   = data.files?.content   ?? [];

    foldersState.hasNext     = data.folders?.hasNext     ?? false;
    foldersState.hasPrevious = data.folders?.hasPrevious ?? false;
    filesState.hasNext       = data.files?.hasNext       ?? false;
    filesState.hasPrevious   = data.files?.hasPrevious   ?? false;

    document.getElementById('foldersTotal').textContent         = `Total: ${data.folders?.totalElements ?? 0}`;
    document.getElementById('filesTotal').textContent           = `Total: ${data.files?.totalElements ?? 0}`;
    document.getElementById('foldersPageIndicator').textContent = `Page ${foldersState.page + 1}`;
    document.getElementById('filesPageIndicator').textContent   = `Page ${filesState.page + 1}`;
    document.getElementById('foldersPrev').disabled             = !foldersState.hasPrevious;
    document.getElementById('foldersNext').disabled             = !foldersState.hasNext;
    document.getElementById('filesPrev').disabled               = !filesState.hasPrevious;
    document.getElementById('filesNext').disabled               = !filesState.hasNext;

    const ftbody = document.getElementById('foldersTableBody');
    ftbody.innerHTML = folders.length
      ? folders.map(f => `
          <tr>
            <td>${f.name}</td>
            <td><small class="text-muted">${f.path}</small></td>
            <td><button class="btn btn-sm btn-outline-primary" onclick="enterFolder('${f.id}')">Open</button></td>
          </tr>`).join('')
      : '<tr><td colspan="3" class="text-center text-muted">No folders.</td></tr>';

    const fBody = document.getElementById('filesTableBody');
    fBody.innerHTML = files.length
      ? files.map(f => `
          <tr>
            <td>${f.name}</td>
            <td><span class="badge bg-secondary">${f.mimeType}</span></td>
            <td>${formatBytes(f.size)}</td>
            <td>
              <button class="btn btn-sm btn-outline-danger" onclick="deleteFile('${f.id}')">
                <i class="bi bi-trash"></i>
              </button>
            </td>
          </tr>`).join('')
      : '<tr><td colspan="4" class="text-center text-muted">No files.</td></tr>';

  } catch (err) {
    console.error('Failed to load folder contents:', err);
  }
}

window.loadFolderContents = loadFolderContents;

window.enterFolder = (id) => {
  document.getElementById('currentFolderId').value = id;
  foldersState.page = 0;
  filesState.page   = 0;
  loadFolderContents();
};

window.foldersPrev = () => { if (foldersState.hasPrevious) { foldersState.page--; loadFolderContents(); } };
window.foldersNext = () => { if (foldersState.hasNext)     { foldersState.page++; loadFolderContents(); } };
window.filesPrev   = () => { if (filesState.hasPrevious)   { filesState.page--;   loadFolderContents(); } };
window.filesNext   = () => { if (filesState.hasNext)       { filesState.page++;   loadFolderContents(); } };

window.createFolder = async () => {
  const name     = document.getElementById('newFolderName').value.trim();
  const parentId = document.getElementById('currentFolderId').value.trim() || null;
  if (!name) return;
  try {
    await post('/api/folders', { name, parentId });
    document.getElementById('newFolderName').value = '';
    loadFolderContents();
  } catch (err) {
    showToast(err.message || 'Failed to create folder.', 'danger');
  }
};

window.uploadFile = async () => {
  const fileInput = document.getElementById('uploadInput');
  if (!fileInput.files.length) return;
  const form     = new FormData();
  const folderId = document.getElementById('currentFolderId').value.trim();
  form.append('file', fileInput.files[0]);
  if (folderId) form.append('folderId', folderId);
  try {
    await postForm('/api/files/upload', form);
    fileInput.value = '';
    showToast('File uploaded.', 'success');
    loadFolderContents();
  } catch (err) {
    showToast(err.message || 'Upload failed.', 'danger');
  }
};

window.deleteFile = async (id) => {
  if (!confirm('Delete this file?')) return;
  try {
    await del(`/api/files/${id}`);
    loadFolderContents();
  } catch (err) {
    showToast(err.message || 'Failed to delete file.', 'danger');
  }
};

function formatBytes(bytes) {
  if (bytes < 1024)       return `${bytes} B`;
  if (bytes < 1048576)    return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1048576).toFixed(1)} MB`;
}

// ─────────────────────────────────────────────────────────────────────────────
// Logout  POST /api/auth/logout
// ─────────────────────────────────────────────────────────────────────────────

document.getElementById('logoutButton').addEventListener('click', async () => {
  try {
    await post('/api/auth/logout');
  } catch {
    // swallow — token is cleared regardless
  }
  clearToken();
  window.location.href = '/admin/login';
});

// ─────────────────────────────────────────────────────────────────────────────
// Refresh button
// ─────────────────────────────────────────────────────────────────────────────

window.refreshData = () => {
  loadCurrentUser();
  loadDashboardStats();
  loadUsersList(usersState.page);
  loadArticles(articlesState.page);
};

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar navigation
// ─────────────────────────────────────────────────────────────────────────────

const SECTION_TITLES = {
  'dashboard':    'Dashboard',
  'users-list':   'Users List',
  'users-edit':   'Edit User',
  'users-view':   'View User',
  'users-delete': 'Delete User',
  'users-role':   'Change Role',
  'settings':     'Settings',
  'reports':      'Reports',
  'blog-articles': 'Articles',
  'blog-create':   'Create Article',
  'blog-comments': 'Comments Moderation',
  'files-manager': 'Files Manager',
};

window.showSection = (sectionId) => {
  document.querySelectorAll('.content-section').forEach(s => (s.style.display = 'none'));
  const section = document.getElementById(`${sectionId}-section`);
  if (section) section.style.display = 'block';
  document.getElementById('pageTitle').textContent = SECTION_TITLES[sectionId] ?? 'Dashboard';

  // Sync active nav link
  document.querySelectorAll('.sidebar .nav-link').forEach(l => l.classList.remove('active'));
  const link = document.querySelector(`.sidebar [href="#${sectionId}"]`);
  if (link) link.classList.add('active');
};

window.toggleSubmenu = (el) => {
  const submenu = el.nextElementSibling;
  const icon    = el.querySelector('.float-end');
  const open    = submenu.style.display !== 'none' && submenu.style.display !== '';
  submenu.style.display = open ? 'none' : 'block';
  icon?.classList.toggle('bi-chevron-down', open);
  icon?.classList.toggle('bi-chevron-up',   !open);
};

// ─────────────────────────────────────────────────────────────────────────────
// Toast notifications
// ─────────────────────────────────────────────────────────────────────────────

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const id        = `toast-${Date.now()}`;
  const bg        = type === 'success' ? 'bg-success' : type === 'danger' ? 'bg-danger' : 'bg-secondary';
  const html = `
    <div id="${id}" class="toast align-items-center text-white ${bg} border-0" role="alert" aria-live="assertive">
      <div class="d-flex">
        <div class="toast-body">${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    </div>`;
  container.insertAdjacentHTML('beforeend', html);
  const el    = document.getElementById(id);
  const toast = new bootstrap.Toast(el, { delay: 3500 });
  toast.show();
  el.addEventListener('hidden.bs.toast', () => el.remove());
}

