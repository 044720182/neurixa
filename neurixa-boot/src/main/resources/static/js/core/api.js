/**
 * api.js — Centralised HTTP client for the Neurixa backend.
 *
 * Responsibilities:
 *  - Attach the Authorization: Bearer <token> header to every request.
 *  - Parse JSON responses automatically.
 *  - Redirect to /admin/login on any 401 Unauthorized response.
 *  - Expose typed helpers: get(), post(), put(), del(), postForm().
 */

import { getToken, clearToken } from './token.js';

const BASE_URL = ''; // same-origin — Spring Boot serves API and UI together

// ─────────────────────────────────────────────────────────────────────────────
// Core fetch wrapper
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Perform an HTTP request and return { status, data }.
 * data is the parsed JSON body, or null when the response has no body.
 * Throws an ApiError for any non-2xx status (except 401 which redirects).
 */
async function request(method, path, body = null, extraHeaders = {}) {
  const token = getToken();

  const headers = {
    ...(body !== null ? { 'Content-Type': 'application/json' } : {}),
    ...(token         ? { 'Authorization': `Bearer ${token}` } : {}),
    ...extraHeaders,
  };

  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    ...(body !== null ? { body: JSON.stringify(body) } : {}),
  });

  // 401 — token expired or invalid → clear and redirect to login
  if (response.status === 401) {
    clearToken();
    window.location.href = '/admin/login';
    return { status: 401, data: null };
  }

  const contentType = response.headers.get('Content-Type') ?? '';
  let data = null;
  if (contentType.includes('application/json')) {
    data = await response.json();
  }

  if (!response.ok) {
    throw new ApiError(response.status, data?.message ?? response.statusText, data);
  }

  return { status: response.status, data };
}

// ─────────────────────────────────────────────────────────────────────────────
// Public helpers
// ─────────────────────────────────────────────────────────────────────────────

/** GET request */
export function get(path, extraHeaders = {}) {
  return request('GET', path, null, extraHeaders);
}

/** POST with a JSON body */
export function post(path, body = null, extraHeaders = {}) {
  return request('POST', path, body, extraHeaders);
}

/** PUT with a JSON body */
export function put(path, body = null, extraHeaders = {}) {
  return request('PUT', path, body, extraHeaders);
}

/** DELETE request */
export function del(path, extraHeaders = {}) {
  return request('DELETE', path, null, extraHeaders);
}

/**
 * POST multipart/form-data — used for file uploads.
 * Content-Type is intentionally omitted so the browser adds the correct boundary.
 */
export async function postForm(path, formData) {
  const token = getToken();
  const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

  const response = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers,
    body: formData,
  });

  if (response.status === 401) {
    clearToken();
    window.location.href = '/admin/login';
    return { status: 401, data: null };
  }

  const contentType = response.headers.get('Content-Type') ?? '';
  let data = null;
  if (contentType.includes('application/json')) {
    data = await response.json();
  }

  if (!response.ok) {
    throw new ApiError(response.status, data?.message ?? response.statusText, data);
  }

  return { status: response.status, data };
}

// ─────────────────────────────────────────────────────────────────────────────
// ApiError
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Thrown when the server responds with a non-2xx status (except 401).
 */
export class ApiError extends Error {
  constructor(status, message, body = null) {
    super(message);
    this.name   = 'ApiError';
    this.status = status;
    this.body   = body;
  }
}

