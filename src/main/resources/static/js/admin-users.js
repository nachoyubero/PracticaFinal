document.addEventListener("DOMContentLoaded", async () => {
    const user = session.getUser();

    if (!session.isLogged() || !user || user.rol !== "ADMIN") {
        alert("Solo los administradores pueden acceder a esta página.");
        window.location.href = "index.html";
        return;
    }

    const list = document.getElementById("adminUsersList");

    async function cargarUsuarios() {
        list.innerHTML = "<p>Cargando usuarios...</p>";

        try {
            const usuarios = await api.get("/users");

            if (!usuarios || usuarios.length === 0) {
                list.innerHTML = "<p>No hay usuarios registrados.</p>";
                return;
            }

            list.innerHTML = "";

            usuarios.forEach(u => {
                const card = document.createElement("div");
                card.className = "courtcard";

                const rol = u.rol?.nombreRol ?? "—";

                card.innerHTML = `
                    <h3>${u.nombre} ${u.apellidos}</h3>
                    <p><strong>ID:</strong> ${u.idUsuario}</p>
                    <p><strong>Email:</strong> ${u.email}</p>
                    <p><strong>Teléfono:</strong> ${u.telefono ?? "—"}</p>
                    <p><strong>Rol:</strong> ${rol}</p>
                    <p><strong>Activo:</strong> ${u.activo ? "Sí" : "No"}</p>
                `;

                if (u.idUsuario !== user.idUsuario) {
                    const btnActivo = document.createElement("button");
                    btnActivo.textContent = u.activo ? "Desactivar usuario" : "Activar usuario";

                    btnActivo.addEventListener("click", async () => {
                        try {
                            await api.patch(`/users/${u.idUsuario}`, {
                                activo: !u.activo
                            });
                            await cargarUsuarios();
                        } catch (err) {
                            alert("No se pudo modificar el usuario: " + err.message);
                        }
                    });

                    card.appendChild(btnActivo);
                }

                list.appendChild(card);
            });

        } catch (err) {
            list.innerHTML = `<p style="color:#b00020">${err.message}</p>`;
        }
    }

    await cargarUsuarios();
});