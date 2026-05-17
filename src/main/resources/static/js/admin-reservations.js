document.addEventListener("DOMContentLoaded", async () => {
    const user = session.getUser();

    if (!session.isLogged() || !user || user.rol !== "ADMIN") {
        alert("Solo los administradores pueden acceder a esta página.");
        window.location.href = "index.html";
        return;
    }

    const form = document.getElementById("filtrosReservasAdmin");
    const limpiar = document.getElementById("limpiarFiltros");
    const list = document.getElementById("adminReservationsList");

    async function cargarReservas(query = {}) {
        list.innerHTML = "<p>Cargando reservas...</p>";

        try {
            const reservas = await api.get("/admin/reservations", query);

            if (!reservas || reservas.length === 0) {
                list.innerHTML = "<p>No hay reservas con esos filtros.</p>";
                return;
            }

            list.innerHTML = "";

            reservas.forEach(r => {
                const card = document.createElement("div");
                card.className = "courtcard";

                card.innerHTML = `
                    <h3>Reserva #${r.idReserva}</h3>
                    <p><strong>Usuario:</strong> ${r.usuario?.nombre ?? "—"} ${r.usuario?.apellidos ?? ""}</p>
                    <p><strong>Email:</strong> ${r.usuario?.email ?? "—"}</p>
                    <p><strong>ID usuario:</strong> ${r.usuario?.idUsuario ?? "—"}</p>
                    <p><strong>Pista:</strong> ${r.pista?.nombre ?? "—"}</p>
                    <p><strong>ID pista:</strong> ${r.pista?.idPista ?? "—"}</p>
                    <p><strong>Fecha:</strong> ${r.fechaReserva}</p>
                    <p><strong>Hora:</strong> ${(r.horaInicio || "").slice(0,5)}</p>
                    <p><strong>Duración:</strong> ${r.duracionMinutos} min</p>
                    <p><strong>Estado:</strong> ${r.estado}</p>
                `;

                if (r.estado === "ACTIVA") {
                    const btnCancelar = document.createElement("button");
                    btnCancelar.textContent = "Cancelar reserva";
                    btnCancelar.addEventListener("click", async () => {
                        if (!confirm(`¿Cancelar la reserva #${r.idReserva}?`)) return;

                        try {
                            await api.del(`/reservations/${r.idReserva}`);
                            await cargarReservas(obtenerFiltros());
                        } catch (err) {
                            alert("No se pudo cancelar: " + err.message);
                        }
                    });

                    card.appendChild(btnCancelar);
                }

                list.appendChild(card);
            });

        } catch (err) {
            list.innerHTML = `<p style="color:#b00020">${err.message}</p>`;
        }
    }

    function obtenerFiltros() {
        return {
            date: form.date.value,
            courtId: form.courtId.value,
            userId: form.userId.value
        };
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        await cargarReservas(obtenerFiltros());
    });

    limpiar.addEventListener("click", async () => {
        form.reset();
        await cargarReservas();
    });

    await cargarReservas();
});