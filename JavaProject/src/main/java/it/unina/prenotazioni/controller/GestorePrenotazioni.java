package it.unina.prenotazioni.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unina.prenotazioni.controller.strategy.AssegnazionePrimaLibera;
import it.unina.prenotazioni.controller.strategy.StrategiaAssegnazione;
import it.unina.prenotazioni.dto.PrenotazioneDTO;
import it.unina.prenotazioni.dto.StatisticheDTO;
import it.unina.prenotazioni.dto.UtenteDTO;
import it.unina.prenotazioni.entity.Area;
import it.unina.prenotazioni.entity.FasciaOraria;
import it.unina.prenotazioni.entity.Postazione;
import it.unina.prenotazioni.entity.Prenotazione;
import it.unina.prenotazioni.entity.RegistroPrenotazioni;
import it.unina.prenotazioni.entity.RegistroSale;
import it.unina.prenotazioni.entity.RegistroUtenti;
import it.unina.prenotazioni.entity.SalaStudio;
import it.unina.prenotazioni.entity.StatoEnum;
import it.unina.prenotazioni.entity.Studente;
import it.unina.prenotazioni.entity.state.StatoAttiva;

/**
 * Gestore (Singleton) delle prenotazioni: EffettuaPrenotazione (UC7), AnnullaPrenotazione
 * (UC9), EffettuaCheck-in (UC10), MonitoraPrenotazioni (UC5), ConsultaStorico (UC12),
 * GestisciTerminePrenotazione (UC16), MonitoraStatisticheServizio (UC13).
 * Come da diagramma delle classi, gli unici attributi sono l'istanza Singleton
 * {@code instance} e la strategia di assegnazione {@code strategia} (composizione 1-1,
 * pattern Strategy); le dipendenze d'uso verso i registri del livello entity e verso
 * GestoreNotifiche non sono attributi e vengono risolte localmente nei metodi.
 */
public class GestorePrenotazioni {

    private static GestorePrenotazioni istanza;

    private final RegistroPrenotazioni registroPrenotazioni = RegistroPrenotazioni.getInstance();
    private final RegistroSale registroSale = RegistroSale.getInstance();
    private final RegistroUtenti registroUtenti = RegistroUtenti.getInstance();
    private StrategiaAssegnazione strategiaAssegnazione;


    private GestorePrenotazioni() {
        this.strategiaAssegnazione = new AssegnazionePrimaLibera(); // la parte è creata dal tutto (composizione)
    }

    public static GestorePrenotazioni getInstance() {
        if (istanza == null) {
            istanza = new GestorePrenotazioni();
            istanza.setStrategiaAssegnazione(new AssegnazionePrimaLibera());
        }
        return istanza;
    }


    public void setStrategiaAssegnazione(StrategiaAssegnazione strategiaAssegnazione){
        this.strategiaAssegnazione = strategiaAssegnazione;
    }

    private Studente risolviStudente(Long idSala, Long idArea, Long idPostazione,
                                     LocalDate data, Long idStudente){
        Studente studente = registroUtenti.trovaStudentePerId(idStudente);

        if (studente == null) {
            throw new IllegalArgumentException("Studente non trovato");
        }
        SalaStudio sala = registroSale.cercaSalaPerId(idSala);
        if (sala == null) {
            throw new IllegalArgumentException("Sala studio non trovata");
        }
        if (!sala.isAttiva()){
            throw new IllegalArgumentException("La sala non è attiva, non sarà più disponibile");
        }
        if (!sala.verificaDataInGiorniApertura(data)) {
            throw new IllegalArgumentException("La sala è chiusa nella data selezionata (giorni feriali)");
        }
        // per robustezza perché non potrà mai presentarsi essendo che il front-end non permette di non selezionare alcuna area
        if (idArea == null) {
            throw new IllegalArgumentException("Area non specificata");
        }


        verificaSuArea(idSala, idArea);

        // devo verificare se la postazione faccia parte dell'area selezionata
        verificaSuPostazione(idArea, idPostazione);

        return studente;


    }

    private void verificaSuPostazione(Long idArea, Long idPostazione) {
        // idPostazione == 0 è il valore sentinella per "assegnazione automatica".
        // Sicuro perché gli id reali sono assegnati solo da MySQL AUTO_INCREMENT (partono da 1) e
        // nessun punto del codice imposta manualmente l'id di una Postazione prima del persist.
        if(idPostazione != null && !idPostazione.equals(0L)){
            Postazione p = registroSale.trovaPostazionePerId(idPostazione);
            if(p == null || !p.getArea().getId().equals(idArea)){
                throw new IllegalArgumentException("La postazione selezionata non è presente nell'area");
            }
        }
    }

    private void verificaSuArea(Long idSala, Long idArea) {
        // verifico se la Area si trovi effettivamente all'inteno nella Sala selezionata
        // La lista non è mai vuota essendo che non è possibile creare una Sala con nessun'area presente.
        // è almeno sempre presente un'area o l'area di default
        Area area = registroSale.trovaAreaPerId(idArea);
        if(area == null || !area.getSalaStudio().getId().equals(idSala)){
            throw new IllegalArgumentException("L'area non è presente all'interno della sala selezionata");
        }
    }

    private void verificaDataFasciaFutura(LocalDate data, FasciaOraria fascia) {
        LocalDateTime inizioSlot = LocalDateTime.of(data, fascia.getOraInizio());
        if (!inizioSlot.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Non è possibile prenotare una fascia oraria già iniziata o passata");
        }
    }

    // ------------------------------------------------------------------ UC7
    public Object effettuaPrenotazione(Long idSala, Long idArea, Long idPostazione,
                                       LocalDate data, Long idFascia, Long idStudente) {


        // 1. Validità e coerenza dei dati.
        // verifico la correttezza e la coerenza dei parametri in ingresso per quanto riguarda sala, area, postazione, idFascia
        Studente studente = risolviStudente(idSala, idArea, idPostazione, data, idStudente);

        FasciaOraria fascia = risolviFasciaDellaSala(idSala, idFascia, data);

        verificaDataFasciaFutura(data, fascia);

        // aggiunto il blocco synchronized per gestire la concorrenza tra accessi multipli
        // in tal modo due studenti sono impossibilitati di avere la stessa postazione prenotata per lo stesso giorno e data
        synchronized (this){
            // 2. Vincolo di unicità (V18).
            if (studente.verificaPrenotazioneAttivaOConfermataInDataFascia(data, fascia.getEtichetta())) {
                throw new IllegalStateException(
                        "Esiste già una tua prenotazione attiva o confermata in questa data e fascia oraria");
            }



            // 3-4. Insieme delle postazioni disponibili (per area specifica o per l'intera sala (area comune)).
            List<Postazione> disponibili = registroSale.getPostazioniDisponibili(idArea, data, fascia.getId());
            if (disponibili.isEmpty()){
                throw new IllegalStateException(
                        "Non sono presenti delle postazioni disponibili per l'area selezionata");
            }





            // 5. Selezione postazione (specifica o automatica via Strategy).
            Postazione postazione = selezionaPostazione(idPostazione, data, fascia, disponibili);

            // 6. Creazione della prenotazione in stato ATTIVA.
            Prenotazione prenotazione = new Prenotazione();
            prenotazione.setData(data);
            prenotazione.setFasciaOraria(fascia);
            prenotazione.setPostazione(postazione);
            prenotazione.setStudente(studente);          // aggiorna il profilo (associazione effettua)
            prenotazione.attach(GestoreNotifiche.getInstance());
            prenotazione.setStato(StatoAttiva.getInstance());

            boolean esito = registroPrenotazioni.salvaPrenotazione(prenotazione);


            // 7 dovrei notificare gli utenti, ma questo comportamento è già modellato all'interno di setStato() di prenotazione
            if (esito){
                return toDTO(prenotazione);
            }
            else{
                throw new RuntimeException("Errore Lato Server: non è stato possibile salvare la prenotazione, riprova");
            }
        }

    }

    // ------------------------------------------------------------------ UC9
    /**
     * Traduce il Sequence Diagram "AnnullaPrenotazione": la verifica del vincolo temporale
     * (1.1.1, V07) e la transizione di stato (1.1.3) sono responsabilità dell'entity
     * Prenotazione; il rilascio della postazione (1.1.4-1.1.5) è conseguenza dello stato
     * ANNULLATA, che esclude la prenotazione dal calcolo della disponibilità.
     */
    public void annullaPrenotazione(Long idPrenotazione) {
        Prenotazione prenotazione = registroPrenotazioni.trovaPerId(idPrenotazione);
        if (prenotazione == null) {
            throw new IllegalArgumentException("Prenotazione non trovata");
        }

        // AnnullaPrenotazione() sull'entity → verificaIntervalloAnnullamentoPrenotazione(),
        //      setStato("annullata") → flagIntervalloAnnullamentoPrenotazione
        //      (l'esito negativo è comunicato dall'entity tramite eccezione).
        boolean flagIntervalloAnnullamentoPrenotazione;
        String motivoNonValido = null;
        try {
            prenotazione.annullaPrenotazione();
            flagIntervalloAnnullamentoPrenotazione = true;
        } catch (IllegalStateException e) {
            flagIntervalloAnnullamentoPrenotazione = false;
            motivoNonValido = e.getMessage();
        }

        if (flagIntervalloAnnullamentoPrenotazione) {
            // alt [AnnullamentoPrenotazioneValido] annullamentoConfermato → persistenza.
            registroPrenotazioni.aggiorna(prenotazione);

            // inviaNotifica(destinatari, messaggio);
            Studente destinatario = prenotazione.getStudente();
            if (destinatario != null) {
                GestoreNotifiche.getInstance().inviaNotifica(List.of(toUtenteDTO(destinatario)),
                        "La prenotazione #" + prenotazione.getId() + " è stata annullata.");
            }
            //annullamentoPrenotazioneConfermato (ritorno regolare).
        } else {
            // alt [AnnullamentoPrenotazioneNonValido] limiteTemporaleAnnullamentoPrenotazioneSuperato.
            throw new IllegalStateException(motivoNonValido);
        }
    }

    // ------------------------------------------------------------------ UC10
    /**
     * Traduce il Sequence Diagram "EffettuaCheckIn": il check-in è consentito solo se la
     * prenotazione è ATTIVA nella data corrente; la conferma (setStato "confermata") è
     * responsabilità dell'entity Prenotazione.
     */
    public void effettuaCheckIn(Long idPrenotazione) {
        Prenotazione prenotazione = registroPrenotazioni.trovaPerId(idPrenotazione);
        if (prenotazione == null) {
            throw new IllegalArgumentException("Prenotazione non trovata");
        }

        // attach del GestoreNotifiche per permettere di ricevere a questo gli update
        prenotazione.attach(GestoreNotifiche.getInstance());

        // verificaPrenotazioneAttivaInDataCorrente() →  flagAttivaInDataCorrente
        //      (l'esito negativo è comunicato dall'entity tramite eccezione).
        boolean flagAttivaInDataCorrente;
        String motivoNonConsentito = null;
        try {
            prenotazione.verificaPrenotazioneAttivaInDataCorrente();
            flagAttivaInDataCorrente = true;
        } catch (IllegalStateException e) {
            flagAttivaInDataCorrente = false;
            motivoNonConsentito = e.getMessage();
        }

        if (flagAttivaInDataCorrente) {
            // alt [Prenotazione attiva nella data corrente] — EffettuaCheckIn() sull'entity
            //     → setStato("confermata") → prenotazioneConfermata → persistenza.
            prenotazione.effettuaCheckin();
            registroPrenotazioni.aggiorna(prenotazione);

            Studente s = prenotazione.getStudente();
            s.setNumeroTotaleAccessi(s.getNumeroTotaleAccessi() + 1);
            registroUtenti.aggiorna(s);
            //checkInEffettuato (ritorno regolare).
        } else {
            // alt [Prenotazione non attiva nella data corrente] checkInNonConsentito.
            throw new IllegalStateException("Check-in non consentito: " + motivoNonConsentito);
        }
    }

    // ------------------------------------------------------------------ UC5
    public List<Object> monitoraPrenotazioni(Long idSalaStudio) {
        List<Object> risultato = new ArrayList<>();
        for (Prenotazione p : RegistroPrenotazioni.getInstance()
                .cercaPrenotazioniPerSalaEData(idSalaStudio, LocalDate.now())) {
            risultato.add(toDTO(p));
        }
        return risultato;
    }

    // ------------------------------------------------------------------ UC12
    public List<Object> consultaStoricoPrenotazioni(Long idStudente) {
        Studente studente = RegistroUtenti.getInstance().trovaStudentePerId(idStudente);
        if (studente == null) {
            throw new IllegalArgumentException("Studente non trovato");
        }
        List<Object> risultato = new ArrayList<>();
        for (Prenotazione p : RegistroPrenotazioni.getInstance()
                .cercaPrenotazioniPerStudente(studente.getMatricola())) {
            risultato.add(toDTO(p));
        }
        return risultato;
    }

    // ------------------------------------------------------------------ UC16
    public void gestisciTerminePrenotazione() {
        LocalDateTime adesso = LocalDateTime.now();
        for (Prenotazione p : registroPrenotazioni.getPrenotazioniInScadenza()) {
            StatoEnum stato = p.getStato().getStatoEnum();
            LocalDateTime inizio = LocalDateTime.of(p.getData(), p.getFasciaOraria().getOraInizio());
            LocalDateTime fine = LocalDateTime.of(p.getData(), p.getFasciaOraria().getOraFine());

            if (stato == StatoEnum.ATTIVA && adesso.isAfter(inizio.plusMinutes(10))) { // tolleranza check-in (V08)
                // Check-in non effettuato entro la tolleranza → SCADUTA (RF18).
                p.attach(GestoreNotifiche.getInstance());
                p.gestisciTermine();
                registroPrenotazioni.aggiorna(p);
            } else if (stato == StatoEnum.CONFERMATA && adesso.isAfter(fine)) {
                // Slot terminato → CONCLUSA (RF19).
                p.attach(GestoreNotifiche.getInstance());
                p.gestisciTermine();
                registroPrenotazioni.aggiorna(p);
            }
        }
    }

    // ------------------------------------------------------------------ UC13
    public Object monitoraStatisticheServizio() {
        LocalDate oggi = LocalDate.now();
        List<Prenotazione> tutte = RegistroPrenotazioni.getInstance().getTutte();

        int prenotazioniOggi = 0;
        int nonConfermate = 0;
        Set<Long> postazioniOccupate = new HashSet<>();
        for (Prenotazione p : tutte) {
            if (!oggi.equals(p.getData())) {
                continue;
            }
            StatoEnum s = p.getStato().getStatoEnum();
            if (s == StatoEnum.ATTIVA || s == StatoEnum.CONFERMATA || s == StatoEnum.CONCLUSA) {
                prenotazioniOggi++;
            }
            if (s == StatoEnum.ATTIVA) {
                nonConfermate++;
            }
            if ((s == StatoEnum.ATTIVA || s == StatoEnum.CONFERMATA) && p.getPostazione() != null) {
                postazioniOccupate.add(p.getPostazione().getId());
            }
        }

        int totali = 0;
        for (SalaStudio sala : RegistroSale.getInstance().getTutteLeSale()) {
            totali += sala.getNumeroPostazioniTotali();
        }

        StatisticheDTO dto = new StatisticheDTO();
        dto.setPrenotazioniOggi(prenotazioniOggi);
        dto.setPrenotazioniNonConfermate(nonConfermate);
        dto.setPostazioniTotali(totali);
        dto.setPostazioniOccupateOggi(postazioniOccupate.size());
        dto.setTassoOccupazione(totali > 0 ? (postazioniOccupate.size() * 100.0 / totali) : 0.0);
        return dto;
    }



    private FasciaOraria risolviFasciaDellaSala(Long idSala, Long idFascia, LocalDate data) {
        SalaStudio sala = registroSale.cercaSalaPerId(idSala);
        if (sala == null){
            throw new IllegalArgumentException("Sala studio non trovata");
        }
        for (FasciaOraria f : sala.getFasceOrariePrestabilite(data)) {
            if (f.getId().equals(idFascia)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Lo slot orario selezionato non è ammesso");
    }

    private  Postazione selezionaPostazione(Long idPostazione, LocalDate data,
                                           FasciaOraria fascia, List<Postazione> disponibili) {
        // idPostazione == 0 è il sentinel per "assegnazione automatica" (scelto dal front-end).
        // Sicuro perché gli id reali sono assegnati solo da MySQL AUTO_INCREMENT (parte da 1) e
        // nessun punto del codice imposta manualmente l'id di una Postazione prima del persist.
        if (idPostazione != null && idPostazione > 0) {
            // Postazione scelta esplicitamente dallo studente.
            Postazione scelta = registroSale.trovaPostazionePerId(idPostazione);
            if (!scelta.disponibilita(data, fascia)) {
                throw new IllegalStateException("La postazione selezionata non è più disponibile");
            }
            return scelta;
        }
        // Assegnazione automatica (Strategy: prima libera).
        if (disponibili.isEmpty()) {
            throw new IllegalStateException("Nessuna postazione disponibile per l'area e la fascia selezionate");
        }
        return strategiaAssegnazione.selezionaPostazione(disponibili);
    }

    private PrenotazioneDTO toDTO(Prenotazione p) {
        PrenotazioneDTO dto = new PrenotazioneDTO();
        dto.setIdPrenotazione(p.getId());
        dto.setData(p.getData());
        dto.setStato(p.getStato().getStatoEnum().name());
        dto.setFasciaOraria(p.getFasciaOraria().getEtichetta());
        Postazione postazione = p.getPostazione();
        dto.setIdPostazione(postazione.getId());
        dto.setNumeroPostazione(numeroPostazioneInArea(postazione));
        dto.setNomeSala(postazione.getSalaStudio().getNome());
        dto.setTipologiaArea(postazione.getArea() != null ? postazione.getArea().getTipologia() : "comune");
        return dto;
    }

    /** Numero d'ordine (1..N) della postazione all'interno della sua area, ordinando per id. */
    private int numeroPostazioneInArea(Postazione postazione) {
        Area area = postazione.getArea();
        if (area == null) {
            return 0;
        }
        List<Postazione> lista = new ArrayList<>(RegistroSale.getInstance().getPostazioniPerArea(area.getId()));
        lista.sort(Comparator.comparing(Postazione::getId));
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId().equals(postazione.getId())) {
                return i + 1;
            }
        }
        return 0;
    }

    private UtenteDTO toUtenteDTO(Studente s) {
        UtenteDTO dto = new UtenteDTO();
        dto.setId(s.getId());
        dto.setNome(s.getNome());
        dto.setCognome(s.getCognome());
        dto.setEmailIstituzionale(s.getEmailIstituzionale());
        dto.setRuolo("Studente");
        dto.setIdentificativo(s.getMatricola());
        return dto;
    }
}
