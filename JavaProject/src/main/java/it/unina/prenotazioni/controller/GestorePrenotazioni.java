package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.controller.strategy.AssegnazionePrimaLibera;
import it.unina.prenotazioni.controller.strategy.StrategiaAssegnazione;
import it.unina.prenotazioni.dto.PrenotazioneDTO;
import it.unina.prenotazioni.dto.RichiestaPrenotazioneDTO;
import it.unina.prenotazioni.dto.StatisticheDTO;
import it.unina.prenotazioni.entity.*;
import it.unina.prenotazioni.entity.state.StatoAttiva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Gestore (Singleton) del ciclo di vita delle prenotazioni:
 * EffettuaPrenotazione (UC7), AnnullaPrenotazione (UC9), EffettuaCheckIn (UC10),
 * ConsultaStorico (UC12), GestisciTermine (UC16), Statistiche (UC13).
 * La scelta automatica della postazione è delegata al pattern Strategy
 * ({@code strategiaAssegnazione}).
 */
public class GestorePrenotazioni {

    private static GestorePrenotazioni instance;

    private final RegistroPrenotazioni registroPrenotazioni = RegistroPrenotazioni.getInstance();
    private final RegistroSale registroSale = RegistroSale.getInstance();
    private final RegistroUtenti registroUtenti = RegistroUtenti.getInstance();
    private StrategiaAssegnazione strategiaAssegnazione;


    private GestorePrenotazioni() {}

    public static GestorePrenotazioni getInstance() {
        if (instance == null) {
            instance = new GestorePrenotazioni();
            instance.setStrategiaAssegnazione(new AssegnazionePrimaLibera());
        }
        return instance;
    }


    /** Permette di sostituire l'algoritmo di assegnazione automatica (pattern Strategy). */
    void setStrategiaAssegnazione(StrategiaAssegnazione strategiaAssegnazione){
        this.strategiaAssegnazione = strategiaAssegnazione;
    }

    /** Risolve lo studente a partire dall'id; errore se non è registrato. */
    private Studente risolviStudente(Long idStudente){
        Studente studente = registroUtenti.trovaStudentePerId(idStudente);
        if(studente == null){
            throw new IllegalArgumentException("Studente non trovato.");
        }
        return studente;
    }


    // ------------------------------------------------------------------ UC7
    /**
     * Valida i dati, verifica unicità (V18) e disponibilità in mutua esclusione,
     * crea la prenotazione in stato ATTIVA e notifica lo studente a salvataggio riuscito.
     */
    public PrenotazioneDTO effettuaPrenotazione(RichiestaPrenotazioneDTO richiesta) {
        Long idSala = richiesta.getIdSala();
        Long idArea = richiesta.getIdArea();
        Long idPostazione = richiesta.getIdPostazione();
        LocalDate data = richiesta.getData();
        Long idFascia = richiesta.getIdFascia();
        Long idStudente = richiesta.getIdStudente();

        // 1. Validità e coerenza dei dati in ingresso: studente, sala, area e fascia.
        Studente studente = risolviStudente(idStudente);
        SalaStudio sala = risolviSala(idSala, data);
        Area area = risolviArea(sala, idArea);
        FasciaOraria fascia = risolviFascia(sala, idFascia, data);

        // Coerenza dei riferimenti: la postazione indicata (se non automatica) deve stare nell'area scelta.
        verificaPostazioneInArea(area, idPostazione);

        // Verifica di unicità, scelta della postazione e salvataggio avvengono in mutua esclusione:
        // senza, due studenti potrebbero ottenere la stessa postazione per la stessa data e fascia.
        synchronized (this){
            // 2. Vincolo di unicità (V18).
            verificaUnicita(studente, data, fascia);

            // 3-4. Insieme delle postazioni disponibili (per area specifica o per l'intera sala (area comune)).
            List<Postazione> disponibili = postazioniDisponibili(area, data, fascia);

            // 5. Selezione postazione (specifica o automatica via Strategy).
            Postazione postazione = selezionaPostazione(idPostazione, data, fascia, disponibili);

            // 6. Creazione della prenotazione in stato ATTIVA.
            Prenotazione prenotazione = creaESalva(studente, data, fascia, postazione);
            // 7. Notifica SOLO dopo il salvataggio riuscito: l'id esiste (assegnato dal DB)
            //    e non si notificano prenotazioni mai salvate.
            notifica(prenotazione);
            return toDTO(prenotazione);
        }

    }

    private void verificaPostazioneInArea(Area area, Long idPostazione) {
        if(idPostazione == null){
            throw new IllegalArgumentException("Postazione non inserita");
        }
        if(idPostazione != 0){
            Postazione postazione = registroSale.trovaPostazionePerId(idPostazione);
            if(postazione == null){
                throw new IllegalArgumentException("Identificativo postazione non valido.");
            }
            // L'appartenenza dell'area alla sala l'ha già verificata risolviArea.
            if(!area.getId().equals(postazione.getArea().getId())){
                throw new IllegalArgumentException("La postazione non si trova all'interno dell'area selezionata");
            }
        }
    }

    private void notifica(Prenotazione prenotazione) {
        prenotazione.attach(GestoreNotifiche.getInstance());
        prenotazione.notifyObservers();
    }

    private Prenotazione creaESalva(Studente studente, LocalDate data, FasciaOraria fascia, Postazione postazione) {

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setData(data);
        prenotazione.setFasciaOraria(fascia);
        prenotazione.setPostazione(postazione);
        prenotazione.setStudente(studente);          // associazione studente-prenotazione ("effettua" nel modello)
        prenotazione.setStato(StatoAttiva.getInstance());

        boolean esito = registroPrenotazioni.salvaPrenotazione(prenotazione);
        if (!esito){
            throw new RuntimeException("Errore Lato Server: non è stato possibile salvare la prenotazione, riprova");
        }

        return prenotazione;
    }

    private List<Postazione> postazioniDisponibili(Area area, LocalDate data, FasciaOraria fascia) {
        // Area e fascia sono già state risolte e validate: si interroga direttamente l'entity.
        List<Postazione> disponibili = area.getPostazioniDisponibili(data, fascia);
        if(disponibili.isEmpty()){
            throw new IllegalArgumentException("Non sono presenti delle postazioni disponibili per l'area selezionata");
        }
        return disponibili;
    }

    private void verificaUnicita(Studente studente, LocalDate data, FasciaOraria fascia) {
        if(studente.verificaPrenotazioneAttivaOConfermataInDataFascia(data, fascia.getEtichetta())){
            throw new IllegalArgumentException("Esiste già una tua prenotazione attiva o confermata in questa data e fascia oraria");
        }
    }

    private Area risolviArea(SalaStudio sala, Long idArea) {
        if (idArea == null) {
            throw new IllegalArgumentException("Area non specificata");
        }
        if (idArea < 1){
            throw new IllegalArgumentException("Identificativo area non valido.");
        }

        Area area = registroSale.trovaAreaPerId(idArea);
        if(area == null || !area.getSalaStudio().getId().equals(sala.getId())){
            throw new IllegalArgumentException("L'area non è presente all'interno della sala selezionata");
        }

        return area;
    }

    private SalaStudio risolviSala(Long idSala, LocalDate data) {
        if(idSala == null){
            throw new IllegalArgumentException("La selezione della sala studio è obbligatoria");
        }
        if(idSala < 1){
            throw new IllegalArgumentException("Identificativo sala non valido");
        }
        SalaStudio sala;
        try{
            sala = registroSale.cercaSalaPerId(idSala);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Sala studio non trovata.");
        }
        if(sala == null){
            throw new IllegalArgumentException("Sala studio non trovata.");
        }
        if(!sala.isAttiva()){
            throw new IllegalArgumentException("La sala non è attiva, non sarà più disponibile");
        }
        if(!sala.verificaDataInGiorniApertura(data)){
            throw new IllegalArgumentException("La sala è chiusa nella data selezionata (giorni feriali)");
        }

        return sala;
    }

    // ------------------------------------------------------------------ UC9
    /**
     * Segue il SD "AnnullaPrenotazione": il vincolo temporale (V07) e il cambio di stato
     * li verifica l'entity Prenotazione. La postazione torna libera da sola, perché una
     * prenotazione ANNULLATA non conta più nel calcolo della disponibilità.
     * @param idPrenotazione idPrenotazione
     * @param idStudente idStudente
     */
    public void annullaPrenotazione(Long idPrenotazione, Long idStudente) {
        Prenotazione prenotazione = registroPrenotazioni.trovaPerId(idPrenotazione);
        if (prenotazione == null) {
            throw new IllegalArgumentException("Prenotazione non trovata");
        }
        verificaProprieta(prenotazione, idStudente);

        // L'entity verifica l'intervallo di annullamento (V07) e cambia lo stato;
        // se l'annullamento non è consentito lo comunica con un'eccezione.
        prenotazione.annullaPrenotazione();
        registroPrenotazioni.aggiorna(prenotazione);

        // inviaNotifica(destinatari, messaggio);
        Studente destinatario = prenotazione.getStudente();
        if (destinatario != null) {
            GestoreNotifiche gestoreNotifiche = GestoreNotifiche.getInstance();
            gestoreNotifiche.inviaNotifica(List.of(gestoreNotifiche.toUtenteDTO(destinatario)),
                        "La prenotazione #" + prenotazione.getId() + " è stata annullata.");
        }
    }

    /** La prenotazione può essere gestita solo dallo studente che l'ha effettuata. */
    private void verificaProprieta(Prenotazione prenotazione, Long idStudente) {
        Studente proprietario = prenotazione.getStudente();
        if (idStudente == null || proprietario == null
                || !proprietario.getId().equals(idStudente)) {
            throw new IllegalArgumentException("Accesso non consentito alla prenotazione.");
        }
    }

    // ------------------------------------------------------------------ UC10
    /**
     * Segue il SD "EffettuaCheckIn": il check-in è consentito solo se la prenotazione
     * è ATTIVA nella data corrente; il passaggio a CONFERMATA lo fa l'entity Prenotazione.
     * @param idPrenotazione idPrenotazione
     * @param idStudente idStudente
     */
    public void effettuaCheckIn(Long idPrenotazione, Long idStudente) {
        Prenotazione prenotazione = registroPrenotazioni.trovaPerId(idPrenotazione);
        if (prenotazione == null) {
            throw new IllegalArgumentException("Prenotazione non trovata");
        }
        verificaProprieta(prenotazione, idStudente);

        // attach del GestoreNotifiche: riceverà l'update al cambio di stato
        prenotazione.attach(GestoreNotifiche.getInstance());

        // L'entity verifica che la prenotazione sia ATTIVA nella data corrente;
        // se non lo è lancia un'eccezione col motivo.
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
            // Prenotazione attiva nella data corrente: check-in sull'entity e salvataggio.
            prenotazione.effettuaCheckin();
            registroPrenotazioni.aggiorna(prenotazione);

            // Il check-in conta come accesso. Aggiorna esplicito dello studente:
            Studente s = prenotazione.getStudente();
            s.setNumeroTotaleAccessi(s.getNumeroTotaleAccessi() + 1);
            registroUtenti.aggiorna(s);
        } else {
            throw new IllegalStateException("Check-in non consentito: " + motivoNonConsentito);
        }
    }

    // ------------------------------------------------------------------ UC12
    /** Storico completo delle prenotazioni dello studente, in qualunque stato. */
    public List<PrenotazioneDTO> consultaStoricoPrenotazioni(Long idStudente) {
        Studente studente = registroUtenti.trovaStudentePerId(idStudente);
        if (studente == null) {
            throw new IllegalArgumentException("Studente non trovato");
        }
        List<PrenotazioneDTO> risultato = new ArrayList<>();
        for (Prenotazione p : registroPrenotazioni.cercaPrenotazioniPerStudente(studente.getMatricola())) {
            risultato.add(toDTO(p));
        }
        return risultato;
    }

    // ------------------------------------------------------------------ UC16
    /**
     * Invocato periodicamente dallo scheduler: fa scadere le ATTIVE oltre la tolleranza
     * di check-in (V08) e conclude le CONFERMATE a fine fascia. L'attach dell'observer
     * prima della transizione fa partire la notifica automatica.
     */
    public void gestisciTerminePrenotazione() {
        LocalDateTime adesso = LocalDateTime.now(ZoneId.of("Europe/Rome"));
        for (Prenotazione p : registroPrenotazioni.getPrenotazioniInScadenza()) {
            StatoEnum stato = p.getStato().getStatoEnum();
            LocalDateTime inizio = LocalDateTime.of(p.getData(), p.getFasciaOraria().getOraInizio());
            LocalDateTime fine = LocalDateTime.of(p.getData(), p.getFasciaOraria().getOraFine());

            if (stato == StatoEnum.ATTIVA && adesso.isAfter(inizio.plusMinutes(Prenotazione.TOLLERANZA_CHECKIN_MINUTI))) { // tolleranza check-in (V08)
                // Check-in non effettuato entro la tolleranza: diventa SCADUTA (RF18).
                p.attach(GestoreNotifiche.getInstance());
                p.gestisciTermine();
                registroPrenotazioni.aggiorna(p);
            } else if (stato == StatoEnum.CONFERMATA && adesso.isAfter(fine)) {
                // Slot terminato: diventa CONCLUSA (RF19).
                p.attach(GestoreNotifiche.getInstance());
                p.gestisciTermine();
                registroPrenotazioni.aggiorna(p);
            }
        }
    }

    // ------------------------------------------------------------------ UC13
    /** Statistiche della giornata: prenotazioni, non confermate, tasso di occupazione. */
    public StatisticheDTO monitoraStatisticheServizio() {
        LocalDate oggi = LocalDate.now(ZoneId.of("Europe/Rome"));
        List<Prenotazione> tutte = registroPrenotazioni.getTutte();

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
        for (SalaStudio sala : registroSale.getTutteLeSale()) {
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

    /** Risolve la fascia tra quelle prenotabili della sala nella data: valida anche l'appartenenza (UC7). */
    private FasciaOraria risolviFascia(SalaStudio sala, Long idFascia, LocalDate data) {
        if(idFascia == null){
            throw new IllegalArgumentException("La fascia oraria è obbligatoria");
        }
        // Data e ora correnti sempre nel fuso Europe/Rome, come nel resto del progetto.
        LocalDate oggi = LocalDate.now(ZoneId.of("Europe/Rome"));
        if (data.isBefore(oggi)){
            throw new IllegalArgumentException("Non è possibile prenotare una fascia oraria già iniziata o passata");
        }
        for (FasciaOraria f : sala.getFasceOrariePrestabilite(data)){
            if(f.getId().equals(idFascia)){
                if(data.isEqual(oggi) && !f.getOraInizio().isAfter(LocalTime.now(ZoneId.of("Europe/Rome")))){
                    throw new IllegalArgumentException("La fascia oraria selezionata non è disponibile");
                }
                return f;
            }
        }
        throw new IllegalArgumentException("La fascia oraria selezionata non è disponibile");
    }

    /** Postazione scelta esplicitamente (se indicata e ancora libera) oppure assegnazione automatica via Strategy. */
    private  Postazione selezionaPostazione(Long idPostazione, LocalDate data,
                                           FasciaOraria fascia, List<Postazione> disponibili) {
        // idPostazione == 0 è il sentinella per "assegnazione automatica": sicuro perché gli id
        // reali partono da 1 (MySQL AUTO_INCREMENT) e non vengono mai impostati a mano.
        if (idPostazione == null){
            throw new IllegalArgumentException("Identificativo postazione non valido.");
        }
        if (idPostazione < 0) {
            throw new IllegalArgumentException("Identificativo postazione non valido.");
        } else if (idPostazione > 0) {
            // Postazione scelta esplicitamente dallo studente.
            Postazione scelta = registroSale.trovaPostazionePerId(idPostazione);
            if(scelta == null){
                throw new IllegalArgumentException("Postazione non trovata");
            }

            if (!scelta.disponibilita(data, fascia)) {
                throw new IllegalStateException("La postazione selezionata non è più disponibile");
            }
            return scelta;
        }
        // Assegnazione automatica (Strategy: prima libera).
        return strategiaAssegnazione.selezionaPostazione(disponibili);
    }

    /** Converte l'entity nel DTO per le boundary (nessun tipo entity attraversa il confine). */
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
}
