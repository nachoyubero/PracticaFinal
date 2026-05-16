// ============================================================
// courts.js  -  Carga el listado de pistas desde
//   GET /pistaPadel/courts?active=true y las pinta en #courtsList.
// ============================================================

document.addEventListener("DOMContentLoaded", async () => {
    const list = document.getElementById("courtsList");
    if (!list) return;

    list.innerHTML = "<p style='text-align:center'>Cargando pistas...</p>";

    try {
        const pistas = await api.get("/courts", { active: true });

        if (!pistas || pistas.length === 0) {
            list.innerHTML =
                "<p style='text-align:center'>No hay pistas disponibles ahora mismo.</p>";
            return;
        }

        list.innerHTML = "";
        pistas.forEach(p => {
            const card = document.createElement("div");
            card.className = "courtcard " + (p.nombre || "");
            card.innerHTML = `
                <h3>${p.nombre}</h3>
                <p>Ubicación: ${p.ubicacion ?? "—"}</p>
                <p>Precio: $${p.precioHora}/hora</p>
                <a href="availability.html?courtId=${p.idPista}" class="btn-reservar">
                    Reservar
                </a>
            `;
            list.appendChild(card);
        });
    } catch (err) {
        list.innerHTML =
            `<p style='text-align:center;color:#b00020'>
                No se pudieron cargar las pistas (${err.message}).
            </p>`;
    }
});
