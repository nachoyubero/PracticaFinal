document.addEventListener("DOMContentLoaded", async () => {
    const user = session.getUser();

    if (!session.isLogged() || !user || user.rol !== "ADMIN") {
        alert("Solo los administradores pueden acceder a esta página.");
        window.location.href = "index.html";
        return;
    }

    const form = document.getElementById("crearPistaForm");
    const msg = document.getElementById("crearPistaMsg");
    const list = document.getElementById("adminCourtsList");

    async function cargarPistas() {
        list.innerHTML = "<p>Cargando pistas...</p>";

        try {
            const pistas = await api.get("/courts");

            if (!pistas || pistas.length === 0) {
                list.innerHTML = "<p>No hay pistas creadas.</p>";
                return;
            }

            list.innerHTML = "";

            pistas.forEach(p => {
                const card = document.createElement("div");
                card.className = "courtcard";

                card.innerHTML = `
                    <h3>${p.nombre}</h3>
                    <p><strong>ID:</strong> ${p.idPista}</p>
                    <p><strong>Ubicación:</strong> ${p.ubicacion ?? "—"}</p>
                    <p><strong>Precio:</strong> ${p.precioHora} €/hora</p>
                    <p><strong>Activa:</strong> ${p.activa ? "Sí" : "No"}</p>
                `;

                const btnEstado = document.createElement("button");
                btnEstado.textContent = p.activa ? "Desactivar" : "Activar";
                btnEstado.addEventListener("click", async () => {
                    try {
                        await api.patch(`/courts/${p.idPista}`, {
                            activa: !p.activa
                        });
                        await cargarPistas();
                    } catch (err) {
                        alert("Error al modificar pista: " + err.message);
                    }
                });

                const btnBorrar = document.createElement("button");
                btnBorrar.textContent = "Borrar";
                btnBorrar.style.marginLeft = "1rem";
                btnBorrar.addEventListener("click", async () => {
                    if (!confirm(`¿Seguro que quieres borrar la pista ${p.nombre}?`)) return;

                    try {
                        await api.del(`/courts/${p.idPista}`);
                        await cargarPistas();
                    } catch (err) {
                        alert("No se pudo borrar: " + err.message);
                    }
                });

                card.appendChild(btnEstado);
                card.appendChild(btnBorrar);
                list.appendChild(card);
            });

        } catch (err) {
            list.innerHTML = `<p style="color:#b00020">${err.message}</p>`;
        }
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const payload = {
            nombre: form.nombre.value.trim(),
            ubicacion: form.ubicacion.value.trim(),
            precioHora: parseFloat(form.precioHora.value),
            activa: true,
            fechaAlta: new Date().toISOString().split("T")[0]
        };

        try {
            await api.post("/courts", payload);
            msg.style.color = "green";
            msg.textContent = "Pista creada correctamente.";
            form.reset();
            await cargarPistas();
        } catch (err) {
            msg.style.color = "#b00020";
            msg.textContent = "Error: " + err.message;
        }
    });

    await cargarPistas();
});