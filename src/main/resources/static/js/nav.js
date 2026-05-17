document.addEventListener("DOMContentLoaded", () => {
    const nav = document.querySelector("nav.main-nav");
    if (!nav) return;

    const loginLink    = nav.querySelector('a[href$="login.html"]');
    const registerLink = nav.querySelector('a[href$="register.html"]');

    if (session.isLogged()) {
        if (loginLink)    loginLink.style.display    = "none";
        if (registerLink) registerLink.style.display = "none";

        const user = session.getUser();

        if (user && user.rol === "ADMIN") {
            const adminReservas = document.createElement("a");
            adminReservas.href = "admin-reservations.html";
            adminReservas.textContent = "Reservas admin";
            nav.appendChild(adminReservas);

            const adminPistas = document.createElement("a");
            adminPistas.href = "admin-courts.html";
            adminPistas.textContent = "Gestionar pistas";
            nav.appendChild(adminPistas);

            const adminUsuarios = document.createElement("a");
            adminUsuarios.href = "admin-users.html";
            adminUsuarios.textContent = "Usuarios";
            nav.appendChild(adminUsuarios);
        }

        const btn = document.createElement("a");
        btn.href = "#";
        btn.textContent = "Cerrar sesión";
        btn.addEventListener("click", async (e) => {
            e.preventDefault();
            try { await api.post("/auth/logout"); } catch (_) {}
            session.clear();
            window.location.href = "index.html";
        });
        nav.appendChild(btn);

        const saludo = document.createElement("div");
        saludo.textContent = user
            ? `Hola, ${user.nombre} (${user.rol})`
            : "Sesión activa";

        Object.assign(saludo.style, {
            position: "fixed",
            top: "12px",
            right: "16px",
            background: "rgba(0,0,0,0.6)",
            color: "white",
            padding: "6px 14px",
            borderRadius: "20px",
            fontSize: "14px",
            fontWeight: "600",
            zIndex: "1000",
            backdropFilter: "blur(4px)",
            pointerEvents: "none",
        });

        document.body.appendChild(saludo);
    }
});