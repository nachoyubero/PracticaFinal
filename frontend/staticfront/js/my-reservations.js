// ============================================================
// my-reservations.js  -  Lista las reservas del usuario logueado
// (GET /pistaPadel/reservations) y permite cancelar cada una
// (DELETE /pistaPadel/reservations/{id}). El backend rechaza la
// cancelación si quedan menos de 24h.
// ============================================================

document.addEventListener("DOMContentLoaded", async () => {
    const list = document.getElementById("courtsList");
    if (!list) return;

    if (!session.isLogged()) {
        list.innerHTML =
            '<p style="text-align:center">' +
              'Necesitas <a href="login.html">iniciar sesión</a> ' +
              'para ver tus reservas.' +
            '</p>';
        return;
    }

    list.innerHTML = "<p style='text-align:center'>Cargando reservas...</p>";

    try {
        const reservas = await api.get("/reservations");
        if (!reservas || reservas.length === 0) {
            list.innerHTML =
                '<p style="text-align:center">Aún no tienes reservas. ' +
                '<a href="reservations.html">¡Crea una!</a></p>';
            return;
        }

        list.innerHTML = "";
        reservas.forEach(r => {
            const card = document.createElement("div");
            card.className = "courtcard";
            card.innerHTML = `
                <h3>Reserva #${r.idReserva}</h3>
                <p><strong>Pista:</strong> ${r.pista ? r.pista.nombre : "—"}</p>
                <p><strong>Fecha:</strong> ${r.fechaReserva}</p>
                <p><strong>Hora:</strong> ${(r.horaInicio || "").slice(0,5)}
                   (${r.duracionMinutos} min)</p>
                <p><strong>Estado:</strong> ${r.estado}</p>
            `;

            // Solo permitimos cancelar reservas ACTIVA. Las
            // CANCELADAS o pasadas no llevan botón.
            if (r.estado === "ACTIVA") {
                const cont = document.createElement("div");
                cont.style.cssText = "display:flex;gap:1rem;margin-top:.5rem;";

                const btn = document.createElement("button");
                btn.className = "btn-reservar";
                btn.style.backgroundColor = "#ff6b6b";
                btn.style.color = "white";
                btn.textContent = "Cancelar";

                btn.addEventListener("click", async () => {
                    if (!confirm(`¿Cancelar la reserva #${r.idReserva}?`)) return;
                    btn.disabled = true;
                    btn.textContent = "Cancelando...";
                    try {
                        await api.del(`/reservations/${r.idReserva}`);
                        card.remove();
                        if (list.children.length === 0) {
                            list.innerHTML =
                                '<p style="text-align:center">' +
                                  'Ya no tienes reservas activas.' +
                                '</p>';
                        }
                    } catch (err) {
                        btn.disabled = false;
                        btn.textContent = "Cancelar";
                        alert("No se pudo cancelar: " + err.message);
                    }
                });

                cont.appendChild(btn);
                card.appendChild(cont);
            }

            list.appendChild(card);
        });
    } catch (err) {
        list.innerHTML =
            `<p style='color:#b00020;text-align:center'>${err.message}</p>`;
    }
});
