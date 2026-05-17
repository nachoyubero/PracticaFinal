// ============================================================
// availability.js  -  Página de disponibilidad:
//   - Pinta un acordeón por pista activa.
//   - Al pulsar "Buscar" llama a /availability?date=...
//   - Para cada pista, reconstruye los slots de 30 min (09:00-22:00)
//     y comprueba si cada slot encaja dentro de alguna "franja libre"
//     devuelta por el backend.
// ============================================================

const OPEN_HOUR  = 9;
const CLOSE_HOUR = 22;
const SLOT_MIN   = 30;

document.addEventListener("DOMContentLoaded", async () => {
    const form     = document.getElementById("searchAvailabilityForm");
    const dateInp  = document.getElementById("fechaBusqueda");
    const list     = document.getElementById("courtsList");
    if (!form || !dateInp || !list) return;

    // Sólo fechas desde hoy.
    const today = new Date().toISOString().split("T")[0];
    dateInp.min = today;
    if (!dateInp.value) dateInp.value = today;

    // Cargamos las pistas una sola vez para tener sus nombres.
    let pistas = [];
    try {
        pistas = await api.get("/courts", { active: true });
    } catch (err) {
        list.innerHTML =
            `<p style="color:#b00020">No se pudieron cargar las pistas: ${err.message}</p>`;
        return;
    }

    // Renderiza el acordeón de cada pista. Pinta los slots como
    // "Ocupado" por defecto y luego, si la búsqueda devuelve franjas
    // libres, los marca como "Libre".
    function renderTablas(fecha, disponibilidades) {
        list.innerHTML = "";

        if (pistas.length === 0) {
            list.innerHTML =
                "<p>No hay pistas activas. El administrador debe crear pistas.</p>";
            return;
        }

        pistas.forEach(p => {
            const disp = disponibilidades.find(d => d.idPista === p.idPista);
            const franjas = disp ? disp.franjasDisponibles : [];

            const details = document.createElement("details");
            details.className = "court-accordion";
            details.open = true;

            const tbody = construirSlots(franjas, p.idPista, fecha);

            details.innerHTML = `
                <summary>
                    <span>🎾 ${p.nombre}</span>
                    <span>▼</span>
                </summary>
                <div class="accordion-content">
                    <table class="timetable">
                        <thead>
                            <tr><th>Tramo</th><th>Estado</th><th>Acción</th></tr>
                        </thead>
                        <tbody>${tbody}</tbody>
                    </table>
                </div>
            `;
            list.appendChild(details);
        });
    }

    // Devuelve las filas <tr> de un horario en HTML.
    function construirSlots(franjasLibres, courtId, fecha) {
        let html = "";
        for (let h = OPEN_HOUR; h < CLOSE_HOUR; h++) {
            for (let m = 0; m < 60; m += SLOT_MIN) {
                const iniMin = h * 60 + m;
                const finMin = iniMin + SLOT_MIN;
                const ini = toHHMM(iniMin);
                const fin = toHHMM(finMin);

                const libre = franjasLibres.some(f =>
                    timeToMin(f.inicio) <= iniMin &&
                    timeToMin(f.fin)    >= finMin
                );

                if (libre) {
                    html += `
                        <tr>
                            <td>${ini} - ${fin}</td>
                            <td><span class="status-libre">Libre</span></td>
                            <td>
                                <a href="reservations.html?courtId=${courtId}&date=${fecha}&time=${ini}"
                                   class="btn-table-reservar">Reservar</a>
                            </td>
                        </tr>`;
                } else {
                    html += `
                        <tr>
                            <td>${ini} - ${fin}</td>
                            <td><span class="status-ocupado">Ocupado</span></td>
                            <td>-</td>
                        </tr>`;
                }
            }
        }
        return html;
    }

    function toHHMM(totalMin) {
        const h = String(Math.floor(totalMin / 60)).padStart(2, "0");
        const m = String(totalMin % 60).padStart(2, "0");
        return `${h}:${m}`;
    }

    function timeToMin(hhmm) {
        // Backend manda "09:00:00", "09:00", o un objeto. Cubrimos los casos.
        if (!hhmm) return 0;
        const s = String(hhmm);
        const [h, m] = s.split(":");
        return parseInt(h, 10) * 60 + parseInt(m, 10);
    }

    // Lanzamos la primera búsqueda con la fecha actual.
    async function buscar() {
        list.innerHTML = "<p style='text-align:center'>Buscando huecos...</p>";
        try {
            const data = await api.get("/availability", { date: dateInp.value });
            renderTablas(dateInp.value, data);
        } catch (err) {
            list.innerHTML =
                `<p style='color:#b00020;text-align:center'>${err.message}</p>`;
        }
    }

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        buscar();
    });

    buscar();   // primera carga
});
