// Helper condivisi da tutte le pagine: dialogo con l'API REST e "sessione" lato client.
const API = '/api';

/**
 * Chiamata REST generica. I parametri viaggiano come query string (GET/DELETE),
 * come form url-encoded (POST/PUT) o, con isJson = true, come corpo JSON
 * (usato da /sale/crea, il cui endpoint riceve un DTO via @RequestBody).
 * In caso di errore lancia un'eccezione col messaggio del backend ({"errore": "..."}).
 */
async function api(method, path, params, isJson = false) {
  const opts = { method, headers: {} };
  let url = API + path;

  if (params) {
    if (isJson && (method !== 'GET' && method !== 'DELETE')) {
      opts.headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(params);
    }
    else {
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
  }

  const res = await fetch(url, opts);
  const testo = await res.text();
  const dati = testo ? JSON.parse(testo) : null;

  if (!res.ok) {
    throw new Error(dati && dati.errore ? dati.errore : ('Errore ' + res.status));
  }

  return dati;
}

// --- "Sessione" lato client: l'utente loggato vive in sessionStorage (per scheda,
// --- svuotato alla chiusura); il backend resta stateless, senza sessioni server.
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

/** Scrive (o azzera, se il messaggio è vuoto) il testo d'errore nell'elemento indicato. */
function mostraErrore(id, messaggio) {
  const el = document.getElementById(id);
  if (el) el.textContent = messaggio || '';
}
