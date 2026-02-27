// Helper function to decode JWT
function decodeJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return null;
    }
}

// Login page logic
if (document.getElementById('loginForm')) {
    const loginForm = document.getElementById('loginForm');
    const loginButton = loginForm.querySelector('button');
    const errorMessageDiv = document.getElementById('errorMessage');

    loginForm.addEventListener('submit', async function(event) {
        event.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        
        errorMessageDiv.textContent = '';
        loginButton.disabled = true;
        loginButton.innerHTML = '<span class="spinner"></span> Logging In...';

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const data = await response.json();
                const decodedToken = decodeJwt(data.token);

                if (decodedToken && (decodedToken.role === 'ADMIN' || decodedToken.role === 'SUPER_ADMIN')) {
                    localStorage.setItem('accessToken', data.token);
                    window.location.href = '/admin/dashboard';
                } else {
                    errorMessageDiv.textContent = 'Access denied. Admin role required.';
                }
            } else {
                const errorData = await response.json();
                errorMessageDiv.textContent = errorData.message || 'Invalid credentials';
            }
        } catch (error) {
            errorMessageDiv.textContent = 'A network error occurred. Please try again.';
        } finally {
            loginButton.disabled = false;
            loginButton.innerHTML = 'Login';
        }
    });
}

// Dashboard page logic
if (document.getElementById('dashboard-container')) {
    const welcomeMessage = document.getElementById('welcomeMessage');
    const roleDisplay = document.getElementById('roleDisplay');
    const logoutButton = document.getElementById('logoutButton');

    async function initializeDashboard() {
        const token = localStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }

        try {
            const response = await fetch('/api/users/me', {
                headers: { 'Authorization': 'Bearer ' + token }
            });

            if (response.ok) {
                const user = await response.json();
                if (user.role === 'ADMIN' || user.role === 'SUPER_ADMIN') {
                    welcomeMessage.textContent = 'Hallo ' + user.username;
                    roleDisplay.textContent = 'Role: ' + user.role;
                } else {
                    localStorage.removeItem('accessToken');
                    window.location.href = '/admin/login';
                }
            } else if (response.status === 401) {
                localStorage.removeItem('accessToken');
                window.location.href = '/admin/login';
            }
        } catch (error) {
            console.error('Error fetching user data:', error);
            localStorage.removeItem('accessToken');
            window.location.href = '/admin/login';
        }
    }

    logoutButton.addEventListener('click', async function() {
        const token = localStorage.getItem('accessToken');
        if (token) {
            try {
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    headers: { 'Authorization': 'Bearer ' + token }
                });
            } catch (error) {
                console.error('Logout API call failed, clearing token locally.', error);
            }
        }
        localStorage.removeItem('accessToken');
        window.location.href = '/admin/login';
    });

    initializeDashboard();
}
