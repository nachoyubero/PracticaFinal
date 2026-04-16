document.addEventListener('DOMContentLoaded', () => {
    // 1. Referencias a los elementos del HTML
    const courtSelect = document.getElementById('court-select');
    const durationSelect = document.getElementById('duration');
    const totalPriceSpan = document.getElementById('total-price');
    const dateInput = document.getElementById('date');
    const form = document.getElementById('reservation-form');

    // 2. Datos de los precios por hora
    const courtData = {
        "Central": 20,
        "Lateral": 15,
        "Exterior": 10,
        "Cesped": 25
    };

    // 3. Función ÚNICA para actualizar el precio (calcula base * horas)
    function updatePrice() {
        const selectedCourt = courtSelect.value;
        // Obtenemos la duración y convertimos a factor (60min = 1, 90min = 1.5, etc.)
        const durationFactor = parseInt(durationSelect.value) / 60; 
        
        const basePrice = courtData[selectedCourt] || 0;
        const finalPrice = basePrice * durationFactor;

        totalPriceSpan.textContent = `$${finalPrice.toFixed(2)}`;
    }

    // 4. Lógica de la URL (Autoseleccionar pista)
    const urlParams = new URLSearchParams(window.location.search);
    const pistaEnUrl = urlParams.get('pista'); 

    if (pistaEnUrl) {
        courtSelect.value = pistaEnUrl;
    }

    // 5. Bloquear fechas pasadas
    const today = new Date().toISOString().split('T')[0];
    dateInput.setAttribute('min', today);

    // 6. Eventos: Actualizar precio cuando cambie algo
    courtSelect.addEventListener('change', updatePrice);
    durationSelect.addEventListener('change', updatePrice);

    // Ejecución inicial para que no salga $0 al cargar
    updatePrice();

    // 7. Envío del formulario
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        const resumen = `Reserva: Pista ${courtSelect.value} por ${durationSelect.value} min.`;
        alert(`¡Confirmado!\n${resumen}`);
    });
});