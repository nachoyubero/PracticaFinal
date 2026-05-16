// ============================================================
// reservations.js  -  Página de "Nueva reserva".
//   - Carga las pistas vía API (poblando el <select>).
//   - Lee querystring (?courtId=&date=&time=) para pre-rellenar
//     el formulario cuando se llega desde "Disponibilidad".
//   - Calcula el precio en vivo (pista.precioHora * duración).
//   - Al enviar, comprueba que hay sesión y hace
//     POST /pistaPadel/reservations con el cuerpo:
//       { usuario:{idUsuario}, pista:{idPista},
//         fechaReserva, horaInicio, duracionMinutos }
// ============================================================

document.addEventListener("DOMContentLoaded", async () => {
    const courtSelect    = document.getElementById("court-select");
    const durationSelect = document.getElementById("duration");
    const totalPriceSpan = document.getElementById("total-price");
    const dateInput      = document.getElementById("date");
    const timeInput      = document.getElementById("time");
    const form           = document.getElementById("reservation-form");

    if (!form) return;

    // Aviso/mensaje
    const msg = document.createElement("p");
    msg.style.marginTop  = "1rem";
    msg.style.fontWeight = "600";
    form.appendChild(msg);

    // Bloquear fechas pasadas
    const today = new Date().toISOString().split("T")[0];
    dateInput.setAttribute("min", today);

    // Sin sesión, no se puede reservar. Avisamos pero dejamos
    // que vea el formulario.
    if (!session.isLogged()) {
        msg.style.color = "#b00020";
        msg.innerHTML =
            'Necesitas <a href="login.html">iniciar sesión</a> para reservar.';
    }

    // Cargar pistas activas en el select
    let pistas = [];
    try {
        pistas = await api.get("/courts", { active: true });
    } catch (err) {
        msg.style.color = "#b00020";
        msg.textContent = "No se pudieron cargar las pistas: " + err.message;
        return;
    }

    courtSelect.innerHTML = "";
    pistas.forEach(p => {
        const opt = document.createElement("option");
        opt.value = p.idPista;
        opt.textContent = p.nombre;
        opt.dataset.precio = p.precioHora;
        courtSelect.appendChild(opt);
    });

    // Pre-rellenar desde query (?courtId=&date=&time=)
    const qs = new URLSearchParams(window.location.search);
    if (qs.get("courtId")) courtSelect.value = qs.get("courtId");
    if (qs.get("date"))    dateInput.value   = qs.get("date");
    if (qs.get("time") && timeInput) timeInput.value = qs.get("time");

    function precioActual() {
        const opt = courtSelect.options[courtSelect.selectedIndex];
        return opt && opt.dataset.precio ? parseFloat(opt.dataset.precio) : 0;
    }

    function updatePrice() {
        const horas = parseInt(durationSelect.value, 10) / 60;
        const total = precioActual() * horas;
        totalPriceSpan.textContent = `$${total.toFixed(2)}`;
    }

    courtSelect.addEventListener("change", updatePrice);
    durationSelect.addEventListener("change", updatePrice);
    updatePrice();

    // Envío
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!session.isLogged()) {
            msg.style.color = "#b00020";
            msg.innerHTML =
                'Necesitas <a href="login.html">iniciar sesión</a> para reservar.';
            return;
        }

        if (!dateInput.value || !timeInput.value) {
            msg.style.color = "#b00020";
            msg.textContent = "Indica fecha y hora.";
            return;
        }

        const user = session.getUser();
        if (!user) {
            msg.style.color = "#b00020";
            msg.textContent = "Sesión inválida, vuelve a iniciar sesión.";
            return;
        }

        const payload = {
            usuario:        { idUsuario: user.idUsuario },
            pista:          { idPista:   parseInt(courtSelect.value, 10) },
            fechaReserva:   dateInput.value,
            horaInicio:     timeInput.value.length === 5
                                ? timeInput.value + ":00"
                                : timeInput.value,
            duracionMinutos: parseInt(durationSelect.value, 10),
        };

        msg.style.color = "#333";
        msg.textContent = "Creando reserva...";
        try {
            const r = await api.post("/reservations", payload);
            msg.style.color = "green";
            msg.textContent = `¡Reserva #${r.idReserva} confirmada!`;
            setTimeout(() => { window.location.href = "my-reservations.html"; }, 800);
        } catch (err) {
            msg.style.color = "#b00020";
            msg.textContent = "Error: " + err.message;
        }
    });
});
