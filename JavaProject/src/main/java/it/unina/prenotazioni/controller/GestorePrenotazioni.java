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
import it.unina.prenotazioni.dto.RichiestaPrenotazioneDTO;
import it.unina.prenotazioni.dto.StatisticheDTO;
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
import org.springframework.cglib.core.Local;

/**
 * <<control>> Gestore (Singleton) del ciclo di vita delle prenotazioni:
 * EffettuaPrenotazione (UC7), AnnullaPrenotazione (UC9), EffettuaCheckIn (UC10),
 * ConsultaStorico (UC12), GestisciTermine (UC16), Statistiche (UC13).
 * La scelta automatica della postazione è delegata al pattern Strategy
 * ({@code strategiaAssegnazione}).
 */
public class GestorePrenotazioni {

    private static GestorePrenotazioni istanza;

    private final RegistroPrenotazioni registroPrenotazioni = RegistroPrenotazioni.getInstance();
    private final RegistroSale registroSale = RegistroSale.getInstance();
    private final RegistroUtenti registroUtenti = RegistroUtenti.getInstance();
    private StrategiaAssegnazione strategiaAssegnazione;


    private GestorePrenotazioni() {}

    public static GestorePrenotazioni getInstance() {
        if (istanza == null) {
            istanza = new GestorePrenotazioni();
            istanza.setStrategiaAssegnazione(new AssegnazionePrimaLibera());
        }
        return istanza;
    }


    /** Permette di sostituire l'algoritmo di assegnazione automatica (pattern Strategy). */
    void setStrategiaAssegnazione(StrategiaAssegnazione strategiaAssegnazione){
        this.strategiaAssegnazione = strategiaAssegnazione;
    }

    /**
     * Validazioni preliminari di UC7: studente esistente, sala attiva e aperta nella data,
     * coerenza area/postazione. Restituisce lo studente per evitare una seconda ricerca.
     */
    private Studente risolviStudente( Long idStudente){
        Studente studente = registroUtenti.trovaStudentePerId(idStudente);
        if(studente == null){
            throw new IllegalArgumentException("Studente non trovato.");
        }
        return studente;
    }

    /** Se lo studente ha scelto una postazione specifica, questa deve esistere ed essere nell'area indicata. */
    private void verificaSuPostazione(Long idArea, Long idPostazione) {
        // idPostazione == 0 è il sentinella per "assegnazione automatica": sicuro perché gli id
        // reali partono da 1 (MySQL AUTO_INCREMENT) e non vengono mai impostati a mano.
        if(idPostazione != null && !idPostazione.equals(0L)){
            if (idPostazione < 0L) {
                throw new IllegalArgumentException("Identificativo postazione non valido.");
            }
            Postazione p = registroSale.trovaPostazionePerId(idPostazione);
            if(p == null || !p.getArea().getId().equals(idArea)){
                throw new IllegalArgumentException("La postazione selezionata non è presente nell'area");
            }
        }
    }

    /** L'area indicata deve esistere e appartenere alla sala selezionata. */
    private void verificaSuArea(Long idSala, Long idArea) {
        Area area = registroSale.trovaAreaPerId(idArea);
        if(area == null || !area.getSalaStudio().getId().equals(idSala)){
            throw new IllegalArgumentException("L'area non è presente all'interno della sala selezionata");
        }
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

        // 1. Validità e coerenza dei dati.
        // verifico la correttezza e la coerenza dei parametri in ingresso per quanto riguarda sala, area, postazione, idFascia
        Studente studente = risolviStudente(idStudente);
        SalaStudio sala = risolviSala(idSala, data);
        risolviArea(sala,idArea);
        FasciaOraria fascia = risolviFascia(sala, idFascia, data);

        // aggiunto il blocco synchronized per gestire la concorrenza tra accessi multipli
        // in tal modo due studenti sono impossibilitati di avere la stessa postazione prenotata per lo stesso giorno e data
        synchronized (this){
            // 2. Vincolo di unicità (V18).
            verificaUnicita(studente, data, fascia);

            // 3-4. Insieme delle postazioni disponibili (per area specifica o per l'intera sala (area comune)).
            List<Postazione> disponibili = postazioniDisponibili(idArea, data, fascia);


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

    private void notifica(Prenotazione prenotazione) {
        prenotazione.attach(GestoreNotifiche.getInstance());
        prenotazione.notifyObservers();
    }

    private Prenotazione creaESalva(Studente studente, LocalDate data, FasciaOraria fascia, Postazione postazione) {

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setData(data);
        prenotazione.setFasciaOraria(fascia);
        prenotazione.setPostazione(postazione);
        prenotazione.setStudente(studente);          // aggiorna il profilo (associazione effettua)
        prenotazione.setStato(StatoAttiva.getInstance());

        boolean esito = registroPrenotazioni.salvaPrenotazione(prenotazione);
        if (!esito){
            throw new RuntimeException("Errore Lato Server: non è stato possibile salvare la prenotazione, riprova");
        }

        return prenotazione;
    }

    private List<Postazione> postazioniDisponibili(Long idArea, LocalDate data, FasciaOraria fascia) {
        List<Postazione> disponibili = registroSale.getPostazioniDisponibili(idArea,data, fascia.getId());
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
     */
    public void annullaPrenotazione(Long idPrenotazione) {
        Prenotazione prenotazione = registroPrenotazioni.trovaPerId(idPrenotazione);
        if (prenotazione == null) {
            throw new IllegalArgumentException("Prenotazione non trovata");
        }

        // L'entity verifica l'intervallo di annullamento (V07) e cambia lo stato;
        // se l'annullamento non è consentito lo comunica con un'eccezione.
        try {
            prenotazione.annullaPrenotazione();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }
        registroPrenotazioni.aggiorna(prenotazione);

        // inviaNotifica(destinatari, messaggio);
        Studente destinatario = prenotazione.getStudente();
        if (destinatario != null) {
            GestoreNotifiche gestoreNotifiche = GestoreNotifiche.getInstance();
            gestoreNotifiche.inviaNotifica(List.of(gestoreNotifiche.toUtenteDTO(destinatario)),
                        "La prenotazione #" + prenotazione.getId() + " è stata annullata.");
        }
    }

    // ------------------------------------------------------------------ UC10
    /**
     * Segue il SD "EffettuaCheckIn": il check-in è consentito solo se la prenotazione
     * è ATTIVA nella data corrente; il passaggio a CONFERMATA lo fa l'entity Prenotazione.
     */
    public void effettuaCheckIn(Long idPrenotazione) {
        Prenotazione prenotazione = registroPrenotazioni.trovaPerId(idPrenotazione);
        if (prenotazione == null) {
            throw new IllegalArgumentException("Prenotazione non trovata");
        }

        // attach del GestoreNotifiche per permettere di ricevere a questo gli update
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
            // non c'è cascade MERGE da Prenotazione verso Studente.
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
        Studente studente = RegistroUtenti.getInstance().trovaStudentePerId(idStudente);
        if (studente == null) {
            throw new IllegalArgumentException("Studente non trovato");
        }
        List<PrenotazioneDTO> risultato = new ArrayList<>();
        for (Prenotazione p : RegistroPrenotazioni.getInstance()
                .cercaPrenotazioniPerStudente(studente.getMatricola())) {
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
        LocalDateTime adesso = LocalDateTime.now();
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



    /** Risolve la fascia tra quelle prenotabili della sala nella data: valida anche l'appartenenza (UC7). */
    private FasciaOraria risolviFascia(SalaStudio sala, Long idFascia, LocalDate data) {
        if(idFascia == null){
            throw new IllegalArgumentException("La fascia oraria è obbligatoria");
        }
        if (data.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("Non è possibile prenotare una fascia oraria già iniziata o passata");
        }
        for (FasciaOraria f : sala.getFasceOrariePrestabilite(data)){
            if(f.getId().equals(idFascia)){
                return f;
            }
        }
        throw new IllegalArgumentException("La fascia oraria selezionata non è disponibile o è già trascorsa");
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
