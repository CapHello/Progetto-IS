package it.unina.prenotazioni.controller;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final StrategiaAssegnazione strategia;

    private GestorePrenotazioni() {
        this.strategia = new AssegnazionePrimaLibera(); // la parte è creata dal tutto (composizione)
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

    private void verificaValiditaDati(Long idSala, Long idArea, Long idPostazione,
                                      LocalDate data, Long idFascia, Long idStudente){
        Studente studente = registroUtenti.trovaStudentePerId(idStudente);

        if (studente == null) {
            throw new IllegalArgumentException("Studente non trovato");
        }
        SalaStudio sala = registroSale.cercaSalaPerId(idSala);
        if (sala == null) {
            throw new IllegalArgumentException("Sala studio non trovata");
        }
        if (!sala.verificaDataInGiorniApertura(data)) {
            throw new IllegalArgumentException("La sala è chiusa nella data selezionata (giorni feriali)");
        }

        // verifico se la Area si trovi effettivamente all'inteno nella Sala selezionata
        List<Area> aree = registroSale.getAreePerSala(idSala);
        boolean areaNonPresenteNellaSala = true;
        int i = 0;
        while (i < aree.size() && areaNonPresenteNellaSala){
            Area a = aree.get(i);
            if (a.getId().equals(idArea)) {
                areaNonPresenteNellaSala = false;
            }else
                i++;

        }
        if(areaNonPresenteNellaSala){
            throw new IllegalArgumentException("L'area non è presente all'inteno della sala selezionata");
        }



        // devo verificare se la postazione faccia parte dell'area selezionata
        if(!idPostazione.equals(0L)){
            List<Postazione> postazioni = registroSale.getPostazioniPerArea(idArea);
            boolean postazioneNonPresenteNellArea = true;
            i = 0;
            while (i < postazioni.size() && postazioneNonPresenteNellArea) {
                Postazione p = postazioni.get(i);
                if (p.getId().equals(idPostazione)) {
                    postazioneNonPresenteNellArea = false;
                } else
                    i++;

            }
            if (postazioneNonPresenteNellArea) {
                throw new IllegalArgumentException("La Postazione Selezionata non è presente nell'arae");
            }
        }



        // devo verificare se la fascia fa parte degli slotOrari della sala
        List<FasciaOraria> slotOrari = registroSale.getFascePerSala(idSala);
        boolean slotOrarioNonPresenteNellaSala = true;
        i = 0;
        while (i < slotOrari.size() && slotOrarioNonPresenteNellaSala) {
            FasciaOraria f = slotOrari.get(i);
            if (f.getId().equals(idFascia)) {
                slotOrarioNonPresenteNellaSala = false;
            } else
                i++;

        }
        if (slotOrarioNonPresenteNellaSala) {
            throw new IllegalArgumentException("Lo slot orario selezionato non è ammesso");
        }

    }

    // ------------------------------------------------------------------ UC7
    public Object effettuaPrenotazione(Long idSala, Long idArea, Long idPostazione,
                                       LocalDate data, Long idFascia, Long idStudente) {
        // 1. Validità e coerenza dei dati.
        Studente studente = registroUtenti.trovaStudentePerId(idStudente);

        // verifico la correttezza e la coerenza dei parametri in ingresso per quanto riguarda sala, area, postazione, idFascia
        verificaValiditaDati(idSala, idArea, idPostazione, data, idFascia, idStudente);

        FasciaOraria fascia = risolviFasciaDellaSala(idSala, idFascia);

        // aggiunto il blocco synchronized per gestire la concorrenza tra accessi multipli
        // in tal modo due studenti sono impossibilitati di avere la stessa postazione prenotata per lo stesso giorno e data
        synchronized (this){
            // 2. Vincolo di unicità (V18).
            if (studente.verificaPrenotazioneAttivaOConfermataInDataFascia(data, fascia.getEtichetta())) {
                throw new IllegalStateException(
                        "Esiste già una tua prenotazione attiva o confermata in questa data e fascia oraria");
            }



            // 3-4. Insieme delle postazioni disponibili (per area specifica o per l'intera sala).
            List<Postazione> disponibili = postazioniDisponibili(idSala, idArea, data, idFascia);
            if (disponibili.isEmpty()){
                throw new IllegalStateException(
                        "Non sono presenti delle postazioni disponibili per l'area selezionata");
            }





            // 5. Selezione postazione (specifica o automatica via Strategy).
            Postazione postazione = selezionaPostazione(idSala, idPostazione, data, fascia, disponibili);

            // 6. Creazione della prenotazione in stato ATTIVA.
            Prenotazione prenotazione = new Prenotazione();
            prenotazione.setData(data);
            prenotazione.setFasciaOraria(fascia);
            prenotazione.setPostazione(postazione);
            prenotazione.setStudente(studente);          // aggiorna il profilo (associazione effettua)
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
        RegistroPrenotazioni registroPrenotazioni = RegistroPrenotazioni.getInstance();
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

    // ------------------------------------------------------------------ helper
    /** Validità e coerenza dei dati della richiesta di prenotazione (UC7, passo 1). */
    private void verificaValiditaDati(Studente studente, SalaStudio sala, LocalDate data) {
        if (studente == null) {
            throw new IllegalArgumentException("Studente non trovato");
        }
        if (sala == null) {
            throw new IllegalArgumentException("Sala studio non trovata");
        }
        if (!sala.verificaDataInGiorniApertura(data)) {
            throw new IllegalArgumentException("La sala è chiusa nella data selezionata (giorni feriali)");
        }
    }

    private FasciaOraria risolviFasciaDellaSala(Long idSala, Long idFascia) {
        for (FasciaOraria f : RegistroSale.getInstance().getFascePerSala(idSala)) {
            if (f.getId().equals(idFascia)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Fascia oraria non valida per la sala selezionata");
    }

    private List<Postazione> postazioniDisponibili(Long idSala, Long idArea, LocalDate data, Long idFascia) {
        // Possiamo direttamente eliminare il metodo getPostazioniDisponibili(area,data,fasciaOraria)
        List<Postazione> disponibili = new ArrayList<>();
        if (idArea > 0) {
            Area area = registroSale.trovaAreaPerId(idArea);
            if (area == null || !idSala.equals(area.getSalaStudio().getId())) {
                throw new IllegalArgumentException("Area non valida per la sala selezionata");
            }

            disponibili.addAll(registroSale.getPostazioniDisponibili(idArea,data,idFascia));
        } else {
            // devo prendere tutte le postazioni che fanno parte dell'area con tipologia comune
            List<Area> areePerSala = registroSale.getAreePerSala(idSala);
            Area areaComune = null;
            for(Area a: areePerSala){
                if(a.getTipologia().equalsIgnoreCase("Comune")){
                    areaComune = a;
                }
            }
            if(areaComune == null){
                throw new IllegalArgumentException("Area non valida per la sala selezionata");
            }
            disponibili.addAll(registroSale.getPostazioniDisponibili(areaComune.getId(),data,idFascia));
        }
        return disponibili;
    }

    private  Postazione selezionaPostazione(Long idSala, Long idPostazione, LocalDate data,
                                           FasciaOraria fascia, List<Postazione> disponibili) {
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
        return strategia.selezionaPostazione(disponibili);
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
