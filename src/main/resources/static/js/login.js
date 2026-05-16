// ============================================================
// login.js  -  Maneja el formulario de login. Llama a
//   POST /pistaPadel/auth/login, guarda el token, después pide
//   GET /pistaPadel/auth/me para tener los datos del usuario.
// ============================================================

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    if (!form) return;

    // Si ya hay sesión, no tiene sentido estar en login
    if (session.isLogged()) {
        window.location.href = "index.html";
        return;
    }

    // Hueco para los mensajes (creado dinámicamente)
    const msg = document.createElement("p");
    msg.id = "loginMsg";
    msg.style.marginTop = "1rem";
    msg.style.fontWeight = "600";
    form.appendChild(msg);

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        msg.style.color = "#333";
        msg.textContent = "Comprobando...";

        const email    = form.email.value.trim();
        const password = form.password.value;

        try {
            const { token } = await api.post("/auth/login", { email, password });
            session.saveToken(token);

            const me = await api.get("/auth/me");
            session.saveUser(me);

            msg.style.color = "green";
            msg.textContent = "¡Bienvenido!";
            setTimeout(() => { window.location.href = "index.html"; }, 400);
        } catch (err) {
            msg.style.color = "#b00020";
            msg.textContent = "Error: " + err.message;
        }
    });
});
