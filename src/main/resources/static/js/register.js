// ============================================================
// register.js  -  Maneja el formulario de registro. Llama a
//   POST /pistaPadel/auth/register y, si todo va bien, redirige
//   al login.
// ============================================================

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("registerForm");
    if (!form) return;

    const msg = document.createElement("p");
    msg.id = "registerMsg";
    msg.style.marginTop = "1rem";
    msg.style.fontWeight = "600";
    form.appendChild(msg);

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        msg.style.color = "#333";
        msg.textContent = "Creando cuenta...";

        const payload = {
            nombre:    form.nombre.value.trim(),
            apellidos: form.apellidos.value.trim(),
            telefono:  form.telefono.value.trim() || null,
            email:     form.email.value.trim(),
            password:  form.password.value,
        };

        try {
            await api.post("/auth/register", payload);
            msg.style.color = "green";
            msg.textContent = "¡Cuenta creada! Redirigiendo al login...";
            setTimeout(() => { window.location.href = "login.html"; }, 700);
        } catch (err) {
            msg.style.color = "#b00020";
            msg.textContent = "Error: " + err.message;
        }
    });
});
