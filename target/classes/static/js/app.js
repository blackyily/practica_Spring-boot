// Gestión de Estado de Autenticación y Cliente REST
const TOKEN_KEY = 'agenda_jwt_token';
const USER_KEY = 'agenda_user_data';

// Helper para cookies
function setCookie(name, value, days = 1) {
    const expires = new Date(Date.now() + days * 864e5).toUTCString();
    document.cookie = `${name}=${encodeURIComponent(value)}; expires=${expires}; path=/; SameSite=Lax`;
}

function deleteCookie(name) {
    document.cookie = `${name}=; Max-Age=-99999999; path=/;`;
}

// Obtener token activo
function getStoredToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getStoredUser() {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
}

// Inicialización de la UI según el estado del token
document.addEventListener('DOMContentLoaded', () => {
    updateNavState();
    const token = getStoredToken();
    const user = getStoredUser();

    const tokenCard = document.getElementById('tokenCard');
    const sessionStatusText = document.getElementById('sessionStatusText');
    const currentUserDisplay = document.getElementById('currentUserDisplay');

    if (token && user) {
        if (sessionStatusText) sessionStatusText.textContent = `Sesión activa (${user.role})`;
        if (currentUserDisplay) currentUserDisplay.textContent = `Usuario: ${user.username} (${user.email})`;
        if (tokenCard) tokenCard.style.display = 'block';

        // Si estamos en la página de agenda y no hay contactos renderizados por SSR, consultar la API REST
        if (window.location.pathname.includes('/agenda') || window.location.pathname === '/') {
            loadContactsFromApi();
        }
    } else {
        if (sessionStatusText) sessionStatusText.textContent = 'Sin autenticar - Inicie sesión para sincronizar';
        if (currentUserDisplay) currentUserDisplay.textContent = 'Modo invitado';
    }
});

function updateNavState() {
    const token = getStoredToken();
    const loginBtn = document.getElementById('navLoginBtn');
    const registerBtn = document.getElementById('navRegisterBtn');
    const logoutBtn = document.getElementById('navLogoutBtn');

    if (token) {
        if (loginBtn) loginBtn.style.display = 'none';
        if (registerBtn) registerBtn.style.display = 'none';
        if (logoutBtn) logoutBtn.style.display = 'inline-flex';
    } else {
        if (loginBtn) loginBtn.style.display = 'inline-flex';
        if (registerBtn) registerBtn.style.display = 'inline-flex';
        if (logoutBtn) logoutBtn.style.display = 'none';
    }
}

// Petición HTTP wrapper con inyección de JWT y captura de X-Process-Time
async function apiFetch(url, options = {}) {
    const token = getStoredToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, { ...options, headers });

    // Capturar y mostrar el encabezado X-Process-Time medido por el ProcessTimeFilter
    const processTime = response.headers.get('X-Process-Time');
    if (processTime) {
        const badge = document.getElementById('processTimeBadge');
        if (badge) {
            badge.innerHTML = `<span>⚡ X-Process-Time: <strong>${processTime}</strong></span>`;
        }
    }

    if (response.status === 401) {
        logout();
        alert('Su sesión ha expirado o el token es inválido. Por favor ingrese nuevamente.');
        window.location.href = '/login';
        throw new Error('Unauthorized');
    }

    return response;
}

// Flujo de Login
async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const alertBox = document.getElementById('alertBox');
    const btn = document.getElementById('btnLogin');

    btn.disabled = true;
    btn.textContent = 'Verificando...';
    alertBox.style.display = 'none';

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem(TOKEN_KEY, data.token);
            localStorage.setItem(USER_KEY, JSON.stringify(data));
            setCookie('jwt_token', data.token, 1);
            window.location.href = '/agenda';
        } else {
            alertBox.textContent = data.message || 'Error en las credenciales ingresadas';
            alertBox.style.display = 'block';
        }
    } catch (err) {
        alertBox.textContent = 'Error al comunicarse con el servidor.';
        alertBox.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Entrar al Sistema';
    }
}

// Flujo de Registro
async function handleRegister(event) {
    event.preventDefault();
    const username = document.getElementById('regUsername').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    const alertBox = document.getElementById('alertBox');
    const btn = document.getElementById('btnRegister');

    btn.disabled = true;
    btn.textContent = 'Creando cuenta...';
    alertBox.style.display = 'none';

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem(TOKEN_KEY, data.token);
            localStorage.setItem(USER_KEY, JSON.stringify(data));
            setCookie('jwt_token', data.token, 1);
            window.location.href = '/agenda';
        } else {
            alertBox.textContent = data.message || 'No se pudo completar el registro.';
            alertBox.style.display = 'block';
        }
    } catch (err) {
        alertBox.textContent = 'Error de conexión con el backend.';
        alertBox.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Registrarse';
    }
}

// Cerrar sesión
function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    deleteCookie('jwt_token');
    updateNavState();
    window.location.href = '/login';
}

// Cargar contactos desde el endpoint REST protegido
async function loadContactsFromApi() {
    try {
        const response = await apiFetch('/api/contacts');
        if (!response.ok) return;

        const contacts = await response.json();
        renderContactsTable(contacts);
    } catch (e) {
        console.error('Error al cargar contactos:', e);
    }
}

function renderContactsTable(contacts) {
    const tbody = document.getElementById('contactsTbody');
    const countBadge = document.getElementById('contactCountBadge');
    if (!tbody) return;

    if (countBadge) countBadge.textContent = `${contacts.length} contactos`;

    if (contacts.length === 0) {
        tbody.innerHTML = `
            <tr id="emptyRow">
                <td colspan="7" class="empty-state">
                    <div class="empty-state-box">
                        <span class="empty-icon">📭</span>
                        <p>No tienes contactos registrados en tu agenda personal.</p>
                        <button class="btn btn-outline" onclick="openCreateModal()">Crear mi primer contacto</button>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = contacts.map(c => `
        <tr id="contact-row-${c.id}">
            <td class="id-col">${c.id}</td>
            <td class="font-semibold">${escapeHtml(c.name)}</td>
            <td><span class="phone-chip">${escapeHtml(c.phone)}</span></td>
            <td>${c.email ? escapeHtml(c.email) : '-'}</td>
            <td>${c.address ? escapeHtml(c.address) : '-'}</td>
            <td class="text-muted">${formatDate(c.createdAt)}</td>
            <td class="text-right actions-col">
                <button class="btn-icon btn-edit" onclick='prepareEdit(${JSON.stringify(c)})'>✏️</button>
                <button class="btn-icon btn-delete" onclick="deleteContact(${c.id})">🗑️</button>
            </td>
        </tr>
    `).join('');
}

// Modales y CRUD
function openCreateModal() {
    document.getElementById('modalTitle').textContent = 'Nuevo Contacto';
    document.getElementById('contactId').value = '';
    document.getElementById('contactName').value = '';
    document.getElementById('contactPhone').value = '';
    document.getElementById('contactEmail').value = '';
    document.getElementById('contactAddress').value = '';
    document.getElementById('contactModal').style.display = 'flex';
}

function prepareEditFromBtn(btn) {
    const contact = {
        id: btn.getAttribute('data-id'),
        name: btn.getAttribute('data-name'),
        phone: btn.getAttribute('data-phone'),
        email: btn.getAttribute('data-email'),
        address: btn.getAttribute('data-address')
    };
    prepareEdit(contact);
}

function prepareEdit(contact) {
    document.getElementById('modalTitle').textContent = 'Editar Contacto #' + contact.id;
    document.getElementById('contactId').value = contact.id;
    document.getElementById('contactName').value = contact.name || '';
    document.getElementById('contactPhone').value = contact.phone || '';
    document.getElementById('contactEmail').value = contact.email || '';
    document.getElementById('contactAddress').value = contact.address || '';
    document.getElementById('contactModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('contactModal').style.display = 'none';
}

async function saveContact(event) {
    event.preventDefault();
    const id = document.getElementById('contactId').value;
    const body = {
        name: document.getElementById('contactName').value.trim(),
        phone: document.getElementById('contactPhone').value.trim(),
        email: document.getElementById('contactEmail').value.trim() || null,
        address: document.getElementById('contactAddress').value.trim() || null
    };

    const isEdit = Boolean(id);
    const url = isEdit ? `/api/contacts/${id}` : '/api/contacts';
    const method = isEdit ? 'PUT' : 'POST';

    try {
        const response = await apiFetch(url, {
            method: method,
            body: JSON.stringify(body)
        });

        if (response.ok) {
            closeModal();
            loadContactsFromApi();
        } else {
            const err = await response.json();
            alert('Error: ' + (err.message || 'No se pudo guardar el contacto'));
        }
    } catch (e) {
        console.error(e);
    }
}

async function deleteContact(id) {
    if (!confirm(`¿Estás seguro de que deseas eliminar el contacto #${id}?`)) {
        return;
    }

    try {
        const response = await apiFetch(`/api/contacts/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            loadContactsFromApi();
        } else {
            alert('No se pudo eliminar el contacto');
        }
    } catch (e) {
        console.error(e);
    }
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
