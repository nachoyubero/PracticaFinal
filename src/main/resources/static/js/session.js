// ============================================================
// session.js  -  Gestiona el token y los datos del usuario
// guardados en localStorage. Pequeño "store" centralizado para
// no esparcir llamadas a localStorage por toda la app.
// ============================================================

const session = {
    saveToken(token) {
        localStorage.setItem("token", token);
    },

    getToken() {
        return localStorage.getItem("token");
    },

    saveUser(user) {
        localStorage.setItem("user", JSON.stringify(user));
    },

    getUser() {
        const raw = localStorage.getItem("user");
        if (!raw) return null;
        try { return JSON.parse(raw); } catch { return null; }
    },

    isLogged() {
        return !!this.getToken();
    },

    clear() {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
    },
};
