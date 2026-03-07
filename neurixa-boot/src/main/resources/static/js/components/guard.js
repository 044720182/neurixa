/**
 * guard.js — Route protection for admin pages.
 *
 * Responsibilities:
 *  - Check that a valid JWT token exists in localStorage.
 *  - Verify that the token carries an ADMIN or SUPER_ADMIN role.
 *  - Immediately redirect to /admin/login when either check fails.
 *
 * Usage: import and call requireAdmin() at the top of any protected page module.
 */

import { isTokenValid, decodeToken, clearToken } from '../core/token.js';

/**
 * Guard for admin-only pages.
 * Call this at the start of your page initialisation.
 * Redirects to /admin/login if the user is not authenticated or lacks the required role.
 */
export function requireAdmin() {
  if (!isTokenValid()) {
    clearToken();
    window.location.href = '/admin/login';
    return false;
  }

  const payload = decodeToken();
  const role = payload?.role ?? '';

  if (role !== 'ADMIN' && role !== 'SUPER_ADMIN') {
    clearToken();
    window.location.href = '/admin/login';
    return false;
  }

  return true;
}

/**
 * Guard for the login page.
 * If the user is already authenticated and is an admin, redirect them straight
 * to the dashboard — no need to show the login form again.
 */
export function redirectIfAuthenticated() {
  if (!isTokenValid()) return;

  const payload = decodeToken();
  const role = payload?.role ?? '';

  if (role === 'ADMIN' || role === 'SUPER_ADMIN') {
    window.location.href = '/admin/dashboard';
  }
}

