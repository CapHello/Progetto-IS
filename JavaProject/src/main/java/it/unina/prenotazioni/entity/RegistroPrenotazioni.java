package it.unina.prenotazioni.entity;

import it.unina.prenotazioni.database.GestorePersistenza;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facade di layer (entity) per l'accesso alle Prenotazioni. Espone ai controller
 * operazioni di ricerca/salvataggio a grana grossa, nascondendo la persistenza.
 */
public class RegistroPrenotazioni {

    private static RegistroPrenotazioni istance;
    private final GestorePersistenza gestorePersistenza;

    private RegistroPrenotazioni() {
        this.gestorePersistenza = new GestorePersistenza();
    }

    public static RegistroPrenotazioni getInstance() {
        if (istance == null) {
            istance = new RegistroPrenotazioni();
        }
        return istance;
    }

    /** Salva una nuova prenotazione; false se il salvataggio fallisce. */
    public boolean salvaPrenotazione(Prenotazione prenotazione) {
        return gestorePersistenza.salva(prenotazione);
    }

    /** Aggiorna i dati di una Prenotazione esistente nel database. */
    public Prenotazione aggiorna(Prenotazione prenotazione) {
        return gestorePersistenza.aggiorna(prenotazione);
    }

    /** Carica una prenotazione per id; null se non esiste. */
    public Prenotazione trovaPerId(Long id) {
        return gestorePersistenza.trovaPerId(Prenotazione.class, id);
    }

    /** Tutte le prenotazioni di uno studente (per matricola): storico / profilo personale. */
    public List<Prenotazione> cercaPrenotazioniPerStudente(String matricola) {
        return gestorePersistenza.cercaPerCampo(Prenotazione.class, "studente.matricola", matricola);
    }

    /** Prenotazioni ATTIVE o CONFERMATE dello studente in una certa data (vincolo di unicità V18). */
    public List<Prenotazione> cercaPrenotazioneAttiva(String matricola, LocalDate data) {
        List<Prenotazione> occupanti = new ArrayList<>();
        List<Prenotazione> trovate = gestorePersistenza.cercaPerCampi(
                Prenotazione.class,
                Map.of("studente.matricola", matricola, "data", data));
        for (Prenotazione p : trovate) {
            if (occupaSlot(p)) {
                occupanti.add(p);
            }
        }
        return occupanti;
    }

    /** Prenotazioni che insistono su una postazione in una determinata data e fascia oraria. */
    public List<Prenotazione> cercaPrenotazioniPerPostazione(Long idPostazione, LocalDate data, Long idFascia) {
        return gestorePersistenza.cercaPerCampi(
                Prenotazione.class,
                Map.of("postazione.id", idPostazione, "data", data, "fasciaOraria.id", idFascia));
    }

    /** Prenotazioni della giornata corrente per una sala (monitoraggio bibliotecario, UC11). */
    public List<Prenotazione> cercaPrenotazioniPerSalaEData(Long idSala, LocalDate data) {
        List<Prenotazione> risultato = new ArrayList<>();
        List<Prenotazione> trovate = gestorePersistenza.cercaPerCampi(
                Prenotazione.class,
                Map.of("postazione.area.salaStudio.id", idSala, "data", data));
        for (Prenotazione p : trovate) {
            if (occupaSlot(p)) {
                risultato.add(p);
            }
        }
        return risultato;
    }

    /** Tutte le prenotazioni (qualsiasi stato) che insistono su postazioni di una sala. */
    public List<Prenotazione> cercaTuttePerSala(Long idSala) {
        return gestorePersistenza.cercaPerCampo(Prenotazione.class, "postazione.area.salaStudio.id", idSala);
    }

    /** Prenotazioni potenzialmente in scadenza: le ATTIVE (che possono scadere) e le CONFERMATE (che possono concludersi). */
    public List<Prenotazione> getPrenotazioniInScadenza() {
        List<Prenotazione> risultato = new ArrayList<>();
        risultato.addAll(gestorePersistenza.cercaPerCampo(Prenotazione.class, "nomeStato", StatoEnum.ATTIVA.name()));
        risultato.addAll(gestorePersistenza.cercaPerCampo(Prenotazione.class, "nomeStato", StatoEnum.CONFERMATA.name()));
        return risultato;
    }

    /** Tutte le prenotazioni (per le statistiche di servizio, UC13). */
    public List<Prenotazione> getTutte() {
        return gestorePersistenza.cercaPerCampi(Prenotazione.class, Map.of());
    }

    /** Una prenotazione "occupa" lo slot (postazione, data, fascia) se è ATTIVA o CONFERMATA. */
    public static boolean occupaSlot(Prenotazione p) {
        StatoEnum s = p.getStato().getStatoEnum();
        return s == StatoEnum.ATTIVA || s == StatoEnum.CONFERMATA;
    }
}
