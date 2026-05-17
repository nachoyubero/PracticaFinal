document.addEventListener("DOMContentLoaded", async () => {
    const form = document.getElementById("editReservationForm");
    const msg = document.getElementById("editReservationMsg");
    const dateInput = document.getElementById("date");
    const timeInput = document.getElementById("time");
    const durationSelect = document.getElementById("duration");

    if (!form) return;

    if (!session.isLogged()) {
        alert("Necesitas iniciar sesión para modificar una reserva.");
        window.location.href = "login.html";
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const reservationId = params.get("id");

    if (!reservationId) {
        msg.style.color = "#b00020";
        msg.textContent = "No se ha indicado ninguna reserva.";
        return;
    }

    const today = new Date().toISOString().split("T")[0];
    dateInput.min = today;

    try {
        const reserva = await api.get(`/reservations/${reservationId}`);

        dateInput.value = reserva.fechaReserva;
        timeInput.value = (reserva.horaInicio || "").slice(0, 5);
        durationSelect.value = reserva.duracionMinutos;

    } catch (err) {
        msg.style.color = "#b00020";
        msg.textContent = "No se pudo cargar la reserva: " + err.message;
        return;
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!dateInput.value || !timeInput.value || !durationSelect.value) {
            msg.style.color = "#b00020";
            msg.textContent = "Rellena todos los campos.";
            return;
        }

        const payload = {
            fechaReserva: dateInput.value,
            horaInicio: timeInput.value.length === 5
                ? timeInput.value + ":00"
                : timeInput.value,
            duracionMinutos: parseInt(durationSelect.value, 10)
        };

        msg.style.color = "#333";
        msg.textContent = "Guardando cambios...";

        try {
            await api.patch(`/reservations/${reservationId}`, payload);

            msg.style.color = "green";
            msg.textContent = "Reserva modificada correctamente.";

            setTimeout(() => {
                window.location.href = "my-reservations.html";
            }, 800);

        } catch (err) {
            msg.style.color = "#b00020";
            msg.textContent = "Error: " + err.message;
        }
    });
});