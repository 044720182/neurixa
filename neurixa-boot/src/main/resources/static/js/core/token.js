/**
 * token.js — JWT token storage and decoding.
 *
 * Responsible for:
 *  - reading / writing the access token in localStorage
 *  - decoding the JWT payload (no signature verification — server handles that)
 */

const TOKEN_KEY = 'accessToken';

/**
 * Persist a JWT token.
 * @param {string} token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

/**
 * Retrieve the stored JWT token, or null if absent.
 * @returns {string|null}
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

/** Remove the stored token (logout / expired). */
export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

/**
 * Decode the payload section of a JWT without verifying its signature.
 * Returns null if the token is missing or malformed.
 * @param {string} [token]
 * @returns {object|null}
 */
export function decodeToken(token) {
  const t = token ?? getToken();
  if (!t) return null;
  try {
    const base64Url = t.split('.')[1];
    const base64   = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const json     = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

/**
 * Return true when a token exists AND has not yet expired.
 * @returns {boolean}
 */
export function isTokenValid() {
  const payload = decodeToken();
  if (!payload) return false;
  // JWT `exp` is in seconds; Date.now() is in milliseconds.
  if (payload.exp && Date.now() >= payload.exp * 1000) {
    clearToken();
    return false;
  }
  return true;
}

