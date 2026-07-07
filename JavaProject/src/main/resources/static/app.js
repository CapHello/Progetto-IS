// Helper condivisi per il dialogo con l'API REST e la gestione della sessione.
const API = '/api';

/**
 * Chiamata REST generica. I parametri sono inviati come query (GET/DELETE) o come
 * form url-encoded (POST/PUT). In caso di errore lancia un'eccezione con il messaggio
 * restituito dal backend ({"errore": "..."}).
 */
async function api(method, path, params) {
  const opts = { method, headers: {} };
  let url = API + path;
  if (params) {
    const usp = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v === undefined || v === null || v === '') return;
      if (Array.isArray(v)) {
        // Liste (es. tipologie[], postazioniAree[]) → parametri ripetuti, ordine preservato.
        v.forEach(item => { if (item !== undefined && item !== null && item !== '') usp.append(k, item); });
      } else {
        usp.append(k, v);
      }
    });
    if (method === 'GET' || method === 'DELETE') {
      const qs = usp.toString();
      if (qs) url += '?' + qs;
    } else {
      opts.headers['Content-Type'] = 'application/x-www-form-urlencoded';
      opts.body = usp.toString();
    }
  }
  const res = await fetch(url, opts);
  const testo = await res.text();
  const dati = testo ? JSON.parse(testo) : null;
  if (!res.ok) {
    throw new Error(dati && dati.errore ? dati.errore : ('Errore ' + res.status));
  }
  return dati;
}

// --- Sessione (lato client) ---
function salvaUtente(u) { sessionStorage.setItem('utente', JSON.stringify(u)); }
function getUtente() {
  const s = sessionStorage.getItem('utente');
  return s ? JSON.parse(s) : null;
}
function logout() { sessionStorage.removeItem('utente'); location.href = 'index.html'; }

/** Protegge le pagine per ruolo: reindirizza al login se non autorizzato. */
function richiediRuolo(ruolo) {
  const u = getUtente();
  if (!u || u.ruolo !== ruolo) { location.href = 'index.html'; return null; }
  return u;
}

function mostraErrore(id, messaggio) {
  const el = document.getElementById(id);
  if (el) el.textContent = messaggio || '';
}
