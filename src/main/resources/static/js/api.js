// ============================================================
// api.js  -  Cliente HTTP central. Se encarga de:
//   - Construir la URL base (/pistaPadel/...)
//   - Añadir el header "Authorization: Bearer <token>" si hay
//     sesión guardada en localStorage.
//   - Lanzar un error legible si el backend devuelve != 2xx.
// ============================================================

// Si en el futuro montas el frontend en otro host (por ejemplo,
// con Live Server en :5500 y el backend en :8080) sólo tienes
// que cambiar esta línea a "http://localhost:8080".
const API_BASE = "/pistaPadel";

/**
 * Lanza una petición HTTP al backend.
 * @param {string} path  Ruta sin el prefijo /pistaPadel  (p.ej. "/courts")
 * @param {object} opts  { method, body, query }
 * @returns {Promise<any>} JSON parseado, o null si no hay cuerpo (204)
 */
async function apiRequest(path, opts = {}) {
    const { method = "GET", body, query } = opts;

    // Query string
    let url = API_BASE + path;
    if (query) {
        const qs = new URLSearchParams();
        Object.entries(query).forEach(([k, v]) => {
            if (v !== undefined && v !== null && v !== "") qs.append(k, v);
        });
        const s = qs.toString();
        if (s) url += "?" + s;
    }

    // Headers
    const headers = { "Accept": "application/json" };
    if (body !== undefined) headers["Content-Type"] = "application/json";

    const token = localStorage.getItem("token");
    if (token) headers["Authorization"] = "Bearer " + token;

    // Fetch
    const res = await fetch(url, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    // Sin contenido
    if (res.status === 204) return null;

    // Intentamos leer JSON; si no es JSON, leemos texto.
    let data = null;
    const ct = res.headers.get("content-type") || "";
    if (ct.includes("application/json")) {
        data = await res.json().catch(() => null);
    } else {
        const txt = await res.text().catch(() => "");
        data = txt ? { mensaje: txt } : null;
    }

    if (!res.ok) {
        const msg =
            (data && (data.mensaje || data.message || data.error)) ||
            `Error ${res.status}`;
        const err = new Error(msg);
        err.status = res.status;
        err.data = data;
        throw err;
    }

    return data;
}

// Atajos cómodos:
const api = {
    get:    (path, query)        => apiRequest(path, { method: "GET",    query }),
    post:   (path, body, query)  => apiRequest(path, { method: "POST",   body, query }),
    patch:  (path, body, query)  => apiRequest(path, { method: "PATCH",  body, query }),
    del:    (path, query)        => apiRequest(path, { method: "DELETE", query }),
};
