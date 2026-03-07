/**
 * login.js — Login page controller.
 *
 * Responsibilities:
 *  - Redirect already-authenticated admins away from the login page.
 *  - Handle the login form submission.
 *  - Call POST /api/auth/login.
 *  - Validate the admin role in the returned JWT.
 *  - Store the token and redirect to /admin/dashboard on success.
 *  - Display a clear error message on failure.
 */

// On the login page we call the auth endpoint directly with a raw fetch
// so that a 401 (wrong credentials) is NOT auto-redirected by api.js.
import { ApiError } from '../core/api.js';
import { setToken, decodeToken } from '../core/token.js';
import { redirectIfAuthenticated } from '../components/guard.js';

// Redirect already-authenticated admins straight to the dashboard
redirectIfAuthenticated();

// ─── DOM references ───────────────────────────────────────────────────────────
const form          = document.getElementById('loginForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const submitBtn     = document.getElementById('submitBtn');
const errorDiv      = document.getElementById('errorMessage');

// ─── Helpers ──────────────────────────────────────────────────────────────────
function setLoading(loading) {
  submitBtn.disabled = loading;
  submitBtn.innerHTML = loading
    ? '<span class="spinner"></span> Logging In...'
    : 'Login';
}

function showError(msg) { errorDiv.textContent = msg; }
function clearError()   { errorDiv.textContent = ''; }

// ─── Submit handler ───────────────────────────────────────────────────────────
form.addEventListener('submit', async (event) => {
  event.preventDefault();
  clearError();
  setLoading(true);

  try {
    // Use raw fetch — api.js would redirect on 401, but here 401 = wrong credentials.
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: usernameInput.value.trim(),
        password: passwordInput.value,
      }),
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      showError(response.status === 401
        ? 'Invalid username or password.'
        : (data.message || 'Login failed. Please try again.'));
      return;
    }

    // Decode JWT payload to confirm admin role before storing the token
    const payload = decodeToken(data.token);
    const role    = payload?.role ?? '';

    if (role !== 'ADMIN' && role !== 'SUPER_ADMIN') {
      showError('Access denied. Admin role required.');
      return;
    }

    setToken(data.token);
    window.location.href = '/admin/dashboard';

  } catch {
    showError('A network error occurred. Please check your connection.');
  } finally {
    setLoading(false);
  }
});

