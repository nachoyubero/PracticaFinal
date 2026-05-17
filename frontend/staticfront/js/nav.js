// ============================================================
// nav.js  -  Actualiza la barra de navegación según haya o no
// sesión activa. Si hay sesión: oculta "Iniciar sesión" /
// "Registro", añade un botón "Cerrar sesión" en la nav, y
// muestra un saludo flotante arriba a la derecha.
// ============================================================

document.addEventListener("DOMContentLoaded", () => {
    const nav = document.querySelector("nav.main-nav");
    if (!nav) return;

    const loginLink    = nav.querySelector('a[href$="login.html"]');
    const registerLink = nav.querySelector('a[href$="register.html"]');

    if (session.isLogged()) {
        if (loginLink)    loginLink.style.display    = "none";
        if (registerLink) registerLink.style.display = "none";

        // Botón "Cerrar sesión" dentro de la nav
        const btn = document.createElement("a");
        btn.href = "#";
        btn.textContent = "Cerrar sesión";
        btn.addEventListener("click", async (e) => {
            e.preventDefault();
            try { await api.post("/auth/logout"); } catch (_) { /* da igual */ }
            session.clear();
            window.location.href = "index.html";
        });
        nav.appendChild(btn);

        // Saludo flotante arriba a la derecha
        const user = session.getUser();
        const saludo = document.createElement("div");
        saludo.textContent = user ? `Hola, ${user.nombre}` : "Sesión activa";
        Object.assign(saludo.style, {
            position:        "fixed",
            top:             "12px",
            right:           "16px",
            background:      "rgba(0,0,0,0.6)",
            color:           "white",
            padding:         "6px 14px",
            borderRadius:    "20px",
            fontSize:        "14px",
            fontWeight:      "600",
            zIndex:          "1000",
            backdropFilter:  "blur(4px)",
            pointerEvents:   "none",
        });
        document.body.appendChild(saludo);
    }
});