package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.dto.*;
import it.unina.prenotazioni.entity.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * <control> Gestore (Singleton) delle Sale Studio: creazione (UC3), eliminazione (UC4),
 * consultazione disponibilità (UC6), dettaglio postazioni e monitoraggio sale (UC11).
 */
public class GestoreSale {

    private static GestoreSale instance;

    private final RegistroSale registroSale = RegistroSale.getInstance();
    private final RegistroPrenotazioni registroPrenotazioni = RegistroPrenotazioni.getInstance();

    private GestoreSale() {}

    public static GestoreSale getInstance() {
        if (instance == null) {
            instance = new GestoreSale();
        }
        return instance;
    }

    // ------------------------------------------------------------------ UC3

    /**
     * Crea la sala col suo intero aggregato: orari lavorativi, slot prenotabili e aree
     * (tipologie[i] + postazioniAree[i]; le postazioni non assegnate confluiscono
     * nell'area di default "comune"). Il salvataggio è unico e a cascata.
     */
    public SalaStudioDTO creaSalaStudio(CreazioneSalaDTO richiestaCreazione) {

        //verifica input
        verificaValiditaDati(richiestaCreazione.getNome(), richiestaCreazione.getDescrizione(), richiestaCreazione.getNumeroPostazioni(), richiestaCreazione.getGranaMinuti());
        verificaValiditaListeOrari(richiestaCreazione.getOrariApertura(), richiestaCreazione.getOrariChiusura());

        List<String> tipi = (richiestaCreazione.getTipologie() != null) ? richiestaCreazione.getTipologie() : new ArrayList<>();
        List<Integer> posti = (richiestaCreazione.getPostazioniAree() != null) ? richiestaCreazione.getPostazioniAree() : new ArrayList<>();
        if (tipi.size() != posti.size()) {
            throw new IllegalArgumentException("Dati delle aree incoerenti (tipologie e postazioni non corrispondono)");
        }

        //Istanziazione
        SalaStudio sala = new SalaStudio(richiestaCreazione.getNome(), richiestaCreazione.getDescrizione(), richiestaCreazione.getNumeroPostazioni());

        //configurazione
        configuraOrariESlot(richiestaCreazione.getOrariApertura(), richiestaCreazione.getOrariChiusura(), richiestaCreazione.getGranaMinuti(), sala);
        configuraAree(richiestaCreazione.getNumeroPostazioni(), tipi, posti, sala);

        //Persistenza
        registroSale.salvaSala(sala);
        return toDTO(sala);
    }

    // ------------------------------------------------------------------ helper per UC3

    /** Le liste orari devono coprire esattamente i 5 giorni lavorativi (Lunedì-Venerdì). */
    private void verificaValiditaListeOrari(List<String> orariApertura, List<String> orariChiusura) {
        if (orariApertura == null || orariChiusura == null ||
                orariApertura.size() != 5 || orariChiusura.size() != 5) {
            throw new IllegalArgumentException("Devi fornire esattamente 5 orari di apertura e chiusura (dal Lunedì al Venerdì).");
        }
    }

    /**
     * Registra i 5 orari lavorativi (l'ordine di inserimento definisce il giorno: 0=Lunedì…4=Venerdì)
     * e genera gli slot prenotabili, deduplicati per etichetta tra i giorni.
     */
    private void configuraOrariESlot(List<String> orariApertura, List<String> orariChiusura, int granaMinuti, SalaStudio sala) {
        Map<String, FasciaOraria> slotUnivoci = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            LocalTime apertura = parseOrario(orariApertura.get(i));
            LocalTime chiusura = parseOrario(orariChiusura.get(i));

            if (!apertura.isBefore(chiusura)) {
                throw new IllegalArgumentException("L'orario di apertura deve precedere la chiusura per il giorno " + (i+1));
            }

            // Indice 0 = Lunedì, Indice 1 = Martedì ... Indice 4 = Venerdì
            sala.addOrarioLavorativo(new FasciaOraria(apertura, chiusura));

            // Generiamo i micro-slot per il giorno corrente
            List<FasciaOraria> slotGiorno = generaSlot(apertura, chiusura, granaMinuti);
            for (FasciaOraria f : slotGiorno) {
                if (slotUnivoci.putIfAbsent(f.getEtichetta(), f) == null) {
                    sala.addFascia(f);
                }
            }
        }
    }

    /** Crea le aree richieste e l'area "comune" con le postazioni rimanenti (V19). */
    private static void configuraAree(int numeroPostazioni, List<String> tipi, List<Integer> posti, SalaStudio sala) {
        // Aree specifiche indicate dal bibliotecario.
        int sommaAree = getSommaAree(numeroPostazioni, tipi, posti);

        for (int i = 0; i < tipi.size(); i++) {
            sala.aggiungiArea(tipi.get(i).trim(), posti.get(i));
        }
        int rimanenti = numeroPostazioni - sommaAree;
        if (rimanenti > 0) {
            sala.creaAreaDefault(rimanenti);
        }
    }

    /** Valida le aree richieste (nome obbligatorio, "comune" riservato, V04) e ne somma le postazioni. */
    private static int getSommaAree(int numeroPostazioni, List<String> tipi, List<Integer> posti) {
        int sommaAree = 0;
        for (int i = 0; i < tipi.size(); i++) {
            if (tipi.get(i) == null || tipi.get(i).trim().isEmpty()) {
                throw new IllegalArgumentException("Il nome/tipologia di ogni area è obbligatorio");
            }
            if ("comune".equalsIgnoreCase(tipi.get(i).trim())) {
                throw new IllegalArgumentException("Il nome 'comune' è riservato all'area di default");
            }
            if (posti.get(i) == null || posti.get(i) < 1) {
                throw new IllegalArgumentException("Ogni area deve contenere almeno una postazione (V04)");
            }
            sommaAree += posti.get(i);
        }

        if (sommaAree > numeroPostazioni) {
            throw new IllegalArgumentException("Le aree specifiche (" + sommaAree
                    + " postazioni) occupano più postazioni di quelle possibili (totale sala "
                    + numeroPostazioni + ")");
        }
        return sommaAree;
    }

    // ------------------------------------------------------------------ UC4
    /**
     * Soft delete: disattiva la sala (lo storico resta), annulla forzatamente le
     * prenotazioni che occupano slot e notifica gli studenti coinvolti.
     */
    public void eliminaSalaStudio(Long idSalaStudio) {

        if (idSalaStudio == null) {
            throw new IllegalArgumentException("L'ID della sala è obbligatorio.");
        }

        if (idSalaStudio < 1) {
            throw new IllegalArgumentException("ID Sala non valido. L'ID deve essere maggiore di zero.");
        }

        SalaStudio sala = registroSale.cercaSalaPerId(idSalaStudio);
        if (sala == null) {
            throw new IllegalArgumentException("Sala studio non trovata");
        }

        sala.setAttiva(false);

        registroSale.aggiornaSala(sala);

        List<Prenotazione> prenotazioniSala = registroPrenotazioni.cercaTuttePerSala(idSalaStudio);
        List<UtenteDTO> destinatari = new ArrayList<>();

        for (Prenotazione p : prenotazioniSala) {
            if (RegistroPrenotazioni.occupaSlot(p)) {

                p.criticalAnnullaPrenotazione();
                registroPrenotazioni.aggiorna(p);

                if (p.getStudente() != null) {
                    destinatari.add(GestoreNotifiche.getInstance().toUtenteDTO(p.getStudente()));
                }
            }
        }

        GestoreNotifiche.getInstance().inviaNotifica(destinatari,
                "La sala '" + sala.getNome() + "' è stata chiusa o rimossa definitivamente. " +
                        "Le tue prenotazioni ancora attive sono state annullate automaticamente.");
    }

    // ------------------------------------------------------------------ UC6
    /** Sale attive, aperte nella data e con almeno un posto libero. */
    public List<SalaStudioDTO> consultazioneSaleDisponibili(LocalDate data) {
        verificaDataConsultabile(data);

        List<SalaStudioDTO> risultato = new ArrayList<>();
        for (SalaStudio sala : registroSale.getSaleDisponibili(data)) {
            risultato.add(toDTO(sala));
        }
        if (risultato.isEmpty()) {
            throw new IllegalStateException("Nessuna Sala Studio disponibile per la data selezionata.");
        }
        return risultato;
    }

    /**
     * La data di consultazione deve essere odierna o futura e cadere in un giorno di
     * apertura (lun-ven). Il controllo sulla data passata precede quello di chiusura:
     * un sabato già trascorso deve essere segnalato come data passata.
     */
    private void verificaDataConsultabile(LocalDate data) {
        if (data.isBefore(LocalDate.now(ZoneId.of("Europe/Rome")))) {
            throw new IllegalArgumentException("Data non valida: non è possibile consultare date passate.");
        }
        DayOfWeek giorno = data.getDayOfWeek();
        if (giorno == DayOfWeek.SATURDAY || giorno == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("La biblioteca è chiusa nel giorno selezionato (apertura lun-ven).");
        }
    }

    /** Fasce prenotabili nella data con conteggio dei posti liberi (wizard di prenotazione, step 2). */
    public List<FasciaDisponibileDTO> getFasceDisponibili(Long idSala, LocalDate data) {
        SalaStudio sala = registroSale.cercaSalaPerId(idSala);
        if (sala == null) {
            throw new IllegalArgumentException("Sala studio non trovata");
        }
        List<FasciaDisponibileDTO> risultato = new ArrayList<>();
        if (!sala.verificaDataInGiorniApertura(data)) {
            return risultato;
        }

        int indiceGiorno = data.getDayOfWeek().getValue() - 1;

        List<FasciaOraria> orariLavorativi = registroSale.getOrariLavorativiPerSala(idSala);
        if (orariLavorativi.isEmpty() || orariLavorativi.size() <= indiceGiorno) {
            return risultato;
        }

        FasciaOraria orarioDelGiorno = orariLavorativi.get(indiceGiorno);
        LocalTime aperturaGiorno = orarioDelGiorno.getOraInizio();
        LocalTime chiusuraGiorno = orarioDelGiorno.getOraFine();

        List<FasciaOraria> slotOrario = registroSale.getFascePerSala(idSala);
        if (slotOrario.isEmpty()) {
            throw new IllegalStateException("Non sono presenti fasce orarie prenotabili per la Sala selezionata.");
        }

        for (FasciaOraria f : slotOrario) {
            if (!f.getOraInizio().isBefore(aperturaGiorno) && !f.getOraFine().isAfter(chiusuraGiorno)) {
                int posti = contaPostiDisponibili(idSala, data, f);
                risultato.add(new FasciaDisponibileDTO(f.getId(), f.getEtichetta(), posti));
            }
        }
        return risultato;
    }

    /** Dettaglio aree/postazioni di una sala per (data, fascia) (wizard step 3-4). */
    public DettaglioSalaDTO selezionaDettaglioSala(Long idSala, Long idFascia, LocalDate data) {
        SalaStudio sala = registroSale.cercaSalaPerId(idSala);
        if (sala == null) {
            throw new IllegalArgumentException("Sala studio non trovata");
        }
        FasciaOraria fascia = registroSale.trovaFasciaPerId(idFascia);
        if (fascia == null) {
            throw new IllegalArgumentException("Fascia oraria inesistente.");
        }

        DettaglioSalaDTO dto = new DettaglioSalaDTO();
        dto.setIdSala(idSala);
        dto.setNomeSala(sala.getNome());
        dto.setData(data.toString());
        dto.setIdFascia(idFascia);
        dto.setFasciaOraria(fascia.getEtichetta());

        int postiLiberiTotali = 0;
        for (Area area : registroSale.getAreePerSala(idSala)) {
            AreaDettaglioDTO adto = new AreaDettaglioDTO(area.getId(), area.getTipologia());
            // Postazioni ordinate per id: la posizione (1..N) è il "numero" mostrato in GUI.
            List<Postazione> postazioni = new ArrayList<>(registroSale.getPostazioniPerArea(area.getId()));
            postazioni.sort(Comparator.comparing(Postazione::getId));
            int liberi = 0;
            int numero = 1;
            for (Postazione p : postazioni) {
                boolean disponibile = p.disponibilita(data, fascia);
                adto.getPostazioni().add(toPostazioneDTO(p, area.getTipologia(), disponibile, numero));
                if (disponibile) liberi++;
                numero++;
            }
            adto.setPostiDisponibili(liberi);
            postiLiberiTotali += liberi;
            dto.getAree().add(adto);
        }
        if (postiLiberiTotali == 0) {
            throw new IllegalStateException("Nessuna Postazione disponibile: selezionare un'altra fascia o Sala.");
        }
        return dto;
    }

    // ------------------------------------------------------------------ UC11
    /**
     * Monitoraggio in tempo reale: per ogni sala il numero di postazioni libere, occupate
     * da prenotazioni ATTIVE (non confermate) e CONFERMATE nella giornata corrente, più le
     * tipologie di area presenti.
     */
    public List<SalaMonitoraggioDTO> monitoraSale() {
        LocalDate oggi = LocalDate.now(ZoneId.of("Europe/Rome"));
        List<SalaMonitoraggioDTO> risultato = new ArrayList<>();

        for (SalaStudio sala : registroSale.getTutteLeSale()) {
            Map<Long, Character> statoPostazione = creaMappaStatoPostazioni(sala.getId(), oggi);

            SalaMonitoraggioDTO dto = calcolaStatisticheSala(sala, statoPostazione);

            risultato.add(dto);
        }

        return risultato;
    }

    // ------------------------------------------------------------------ helper per UC11
    private Map<Long, Character> creaMappaStatoPostazioni(Long idSala, LocalDate data) {
        Map<Long, Character> statoPostazione = new HashMap<>();

        for (Prenotazione p : registroPrenotazioni.cercaPrenotazioniPerSalaEData(idSala, data)) {
            if (p.getPostazione() == null) continue;

            Long pid = p.getPostazione().getId();

            if (p.getStato().getStatoEnum() == StatoEnum.CONFERMATA) {
                statoPostazione.put(pid, 'C');
            } else {
                statoPostazione.computeIfAbsent(pid, k -> 'A');
            }
        }
        return statoPostazione;
    }

    private SalaMonitoraggioDTO calcolaStatisticheSala(SalaStudio sala, Map<Long, Character> statoPostazione) {
        SalaMonitoraggioDTO dto = new SalaMonitoraggioDTO();
        dto.setIdSala(sala.getId());
        dto.setNomeSala(sala.getNome());
        dto.setAttiva(sala.isAttiva());

        int liberi = 0;
        int attivi = 0;
        int confermati = 0;

        for (Area area : registroSale.getAreePerSala(sala.getId())) {
            dto.getAree().add(area.getTipologia());

            for (Postazione p : registroSale.getPostazioniPerArea(area.getId())) {
                Character st = statoPostazione.get(p.getId());

                if (st == null) {
                    liberi++;
                } else if (st == 'C') {
                    confermati++;
                } else {
                    attivi++;
                }
            }
        }

        dto.setPostiLiberi(liberi);
        dto.setPostiAttivi(attivi);
        dto.setPostiConfermati(confermati);

        return dto;
    }
    // ------------------------------------------------------------------ helper

    /** Somma i posti liberi di tutte le aree della sala per (data, fascia). */
    private int contaPostiDisponibili(Long idSala, LocalDate data, FasciaOraria fascia) {
        int posti = 0;
        for (Area area : registroSale.getAreePerSala(idSala)) {
            posti += area.getPostazioniDisponibili(data, fascia).size();
        }
        return posti;
    }

    /** Validazioni di formato per la creazione: nome, descrizione, capienza e grana degli slot. */
    private void verificaValiditaDati(String nome, String descrizione, int numeroPostazioni, int granaMinuti) {
        if (nome == null || !nome.matches("[\\p{L}0-9 ]{1,50}")) {
            throw new IllegalArgumentException("Nome sala non valido (1-50 caratteri, senza simboli speciali)");
        }
        if (descrizione != null && descrizione.length() > 256) {
            throw new IllegalArgumentException("Descrizione troppo lunga (max 256 caratteri)");
        }
        if (numeroPostazioni < 1) {
            throw new IllegalArgumentException("La sala deve avere almeno una postazione");
        }
        if (granaMinuti < 1) {
            throw new IllegalArgumentException("La grana di suddivisione deve essere positiva");
        }
    }

    /** Converte "HH:mm" in LocalTime traducendo gli errori di formato in messaggi per la GUI. */
    private LocalTime parseOrario(String orario) {
        try {
            return LocalTime.parse(orario);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Formato orario non valido (atteso HH:mm): " + orario);
        }
    }

    /**
     * Divide l'orario di apertura in slot consecutivi di grana fissa; l'eventuale resto
     * finale viene scartato. La condizione fine.isAfter(inizio) ferma il ciclo se
     * l'aggiunta dei minuti supera la mezzanotte (LocalTime è circolare).
     */
    private List<FasciaOraria> generaSlot(LocalTime apertura, LocalTime chiusura, int grana) {
        List<FasciaOraria> slot = new ArrayList<>();
        LocalTime inizio = apertura;
        LocalTime fine = inizio.plusMinutes(grana);
        while (fine.isAfter(inizio) && !fine.isAfter(chiusura)) {
            slot.add(new FasciaOraria(inizio, fine));
            inizio = fine;
            fine = inizio.plusMinutes(grana);
        }
        return slot;
    }

    /** Converte l'entity nel DTO per le boundary (nessun tipo entity attraversa il confine). */
    private SalaStudioDTO toDTO(SalaStudio sala) {
        SalaStudioDTO dto = new SalaStudioDTO();
        dto.setId(sala.getId());
        dto.setNome(sala.getNome());
        dto.setDescrizione(sala.getDescrizione());
        dto.setNumeroPostazioniTotali(sala.getNumeroPostazioniTotali());
        dto.setAttiva(sala.isAttiva());

        // Le fasce si rileggono dal registro: si espongono solo quelle effettivamente persistite.
        List<String> fasce = new ArrayList<>();
        for (FasciaOraria f : registroSale.getFascePerSala(sala.getId())) {
            fasce.add(f.getEtichetta());
        }
        dto.setFasceOrarie(fasce);

        return dto;
    }

    /** DTO di una postazione per il dettaglio sala; il numero è la posizione (1..N) nell'area. */
    private PostazioneDTO toPostazioneDTO(Postazione p, String tipologiaArea, boolean disponibile, int numero) {
        PostazioneDTO dto = new PostazioneDTO();
        dto.setId(p.getId());
        dto.setNumero(numero);
        dto.setTipologiaArea(tipologiaArea);
        dto.setDisponibile(disponibile);
        return dto;
    }

}
