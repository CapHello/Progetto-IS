package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.dto.AreaDettaglioDTO;
import it.unina.prenotazioni.dto.DettaglioSalaDTO;
import it.unina.prenotazioni.dto.FasciaDisponibileDTO;
import it.unina.prenotazioni.dto.PostazioneDTO;
import it.unina.prenotazioni.dto.SalaMonitoraggioDTO;
import it.unina.prenotazioni.dto.SalaStudioDTO;
import it.unina.prenotazioni.dto.UtenteDTO;
import it.unina.prenotazioni.entity.Area;
import it.unina.prenotazioni.entity.FasciaOraria;
import it.unina.prenotazioni.entity.Postazione;
import it.unina.prenotazioni.entity.Prenotazione;
import it.unina.prenotazioni.entity.RegistroPrenotazioni;
import it.unina.prenotazioni.entity.RegistroSale;
import it.unina.prenotazioni.entity.SalaStudio;
import it.unina.prenotazioni.entity.StatoEnum;
import it.unina.prenotazioni.entity.Studente;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestore (Singleton) delle Sale Studio: creazione (UC3), eliminazione (UC4),
 * consultazione disponibilità (UC6), dettaglio postazioni e monitoraggio sale (UC11).
 */
public class GestoreSale {

    private static GestoreSale istanza;

    private final RegistroSale registroSale = RegistroSale.getInstance();
    private final RegistroPrenotazioni registroPrenotazioni = RegistroPrenotazioni.getInstance();

    private GestoreSale() {}

    public static GestoreSale getInstance() {
        if (istanza == null) {
            istanza = new GestoreSale();
        }
        return istanza;
    }

    // ------------------------------------------------------------------ UC3
    // La creazione della sala include la definizione delle aree (ciclo dello scenario
    // CreaSalaStudio): tipologie[i] + postazioniAree[i] descrivono le aree; le postazioni
    // non assegnate confluiscono nell'area di default "comune".
    public SalaStudioDTO creaSalaStudio(String nome, String descrizione, int numeroPostazioni,
                                 List<String> orariApertura, List<String> orariChiusura, int granaMinuti,
                                 List<String> tipologie, List<Integer> postazioniAree) {

        verificaValiditaDati(nome, descrizione, numeroPostazioni, granaMinuti);

        if (orariApertura == null || orariChiusura == null ||
                orariApertura.size() != 5 || orariChiusura.size() != 5) {
            throw new IllegalArgumentException("Devi fornire esattamente 5 orari di apertura e chiusura (dal Lunedì al Venerdì).");
        }

        List<String> tipi = (tipologie != null) ? tipologie : new ArrayList<>();
        List<Integer> posti = (postazioniAree != null) ? postazioniAree : new ArrayList<>();
        if (tipi.size() != posti.size()) {
            throw new IllegalArgumentException("Dati delle aree incoerenti (tipologie e postazioni non corrispondono)");
        }
        int sommaAree = getSommaAree(numeroPostazioni, tipi, posti);

        SalaStudio sala = new SalaStudio(nome, descrizione, numeroPostazioni);

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
                String key = f.getEtichetta(); // Es. "09:00-09:30"
                if (!slotUnivoci.containsKey(key)) {
                    slotUnivoci.put(key, f);
                    sala.addFascia(f); // Aggiunge alla lista slotOrario della sala
                }
            }
        }

        // Aree specifiche indicate dal bibliotecario.
        for (int i = 0; i < tipi.size(); i++) {
            sala.aggiungiArea(tipi.get(i).trim(), posti.get(i));
        }
        int rimanenti = numeroPostazioni - sommaAree;
        sala.creaAreaDefault(rimanenti);

        // 5. Persistenza
        registroSale.salvaSala(sala);
        return toDTO(sala);
    }

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

        if (sommaAree >= numeroPostazioni) {
            throw new IllegalArgumentException("Le aree specifiche (" + sommaAree
                    + " postazioni) devono lasciarne almeno una all'area comune (totale sala "
                    + numeroPostazioni + ")");
        }
        return sommaAree;
    }

    // ------------------------------------------------------------------ UC4
    public void eliminaSalaStudio(Long idSalaStudio) {
        SalaStudio sala = registroSale.cercaSalaPerId(idSalaStudio);
        if (sala == null) {
            throw new IllegalArgumentException("Sala studio non trovata");
        }

        List<Prenotazione> prenotazioniSala = registroPrenotazioni.cercaTuttePerSala(idSalaStudio);

        // Annulla (forzato) le prenotazioni attive/confermate e raccoglie i destinatari.
        List<UtenteDTO> destinatari = new ArrayList<>();
        for (Prenotazione p : prenotazioniSala) {
            if (RegistroPrenotazioni.occupaSlot(p)) {
                p.criticalAnnullaPrenotazione();
                if (p.getStudente() != null) {
                    destinatari.add(toUtenteDTO(p.getStudente()));
                }
            }
        }
        GestoreNotifiche.getInstance().inviaNotifica(destinatari,
                "La sala '" + sala.getNome() + "' è stata eliminata: le prenotazioni attive/confermate sono annullate.");

        // Rimozione in ordine di integrità referenziale: prenotazioni → postazioni/aree → fasce → sala.
        for (Prenotazione p : prenotazioniSala) {
            registroPrenotazioni.elimina(p.getId());
        }
        sala.eliminaAree();

        if (sala.getOrarioLavorativo() != null) {
            for(FasciaOraria f: new  ArrayList<>(sala.getOrarioLavorativo())) {
                registroSale.eliminaFascia(f.getId());
            }
            sala.getOrarioLavorativo().clear();
        }

        for (FasciaOraria f : registroSale.getFascePerSala(idSalaStudio)) {
            registroSale.eliminaFascia(f.getId());
        }
        registroSale.eliminaSala(idSalaStudio);
    }

    // ------------------------------------------------------------------ UC6
    public List<Object> consultazioneSaleDisponibili(LocalDate data) {
        List<Object> risultato = new ArrayList<>();
        for (SalaStudio sala : registroSale.getSaleDisponibili(data)) {
            risultato.add(toDTO(sala));
        }
        return risultato;
    }

    public List<Object> getFasceDisponibili(Long idSala, LocalDate data) {
        SalaStudio sala = registroSale.cercaSalaPerId(idSala);
        if (sala == null) {
            throw new IllegalArgumentException("Sala studio non trovata");
        }

        List<Object> risultato = new ArrayList<>();
        if (!sala.verificaDataInGiorniApertura(data)) {
            return risultato; // sala chiusa nei weekend
        }

        // DayOfWeek va da 1 (Lunedì) a 7 (Domenica).
        // Sottraendo 1, mappiamo Lunedì all'indice 0, Martedì a 1, ecc.
        int indiceGiorno = data.getDayOfWeek().getValue() - 1;

        // Recuperiamo l'orario specifico di QUEL giorno della settimana
        FasciaOraria orarioDelGiorno = sala.getOrarioLavorativo().get(indiceGiorno);
        LocalTime aperturaGiorno = orarioDelGiorno.getOraInizio();
        LocalTime chiusuraGiorno = orarioDelGiorno.getOraFine();

        for (FasciaOraria f : sala.getSlotOrario()) {
            // Mostriamo lo slot SOLO SE è compreso nell'orario lavorativo del giorno richiesto
            if (!f.getOraInizio().isBefore(aperturaGiorno) && !f.getOraFine().isAfter(chiusuraGiorno)) {
                int posti = contaPostiDisponibili(idSala, data, f);
                risultato.add(new FasciaDisponibileDTO(f.getId(), f.getEtichetta(), posti));
            }
        }

        return risultato;
    }

    /** Dettaglio aree/postazioni di una sala per (data, fascia) (wizard step 3-4). */
    public Object selezionaDettaglioSala(Long idSala, Long idFascia, LocalDate data) {
        SalaStudio sala = registroSale.cercaSalaPerId(idSala);
        if (sala == null) {
            throw new IllegalArgumentException("Sala studio non trovata");
        }
        FasciaOraria fascia = registroSale.trovaFasciaPerId(idFascia);
        if (fascia == null) {
            throw new IllegalArgumentException("Fascia oraria non valida per la sala selezionata");
        }

        DettaglioSalaDTO dto = new DettaglioSalaDTO();
        dto.setIdSala(idSala);
        dto.setNomeSala(sala.getNome());
        dto.setData(data.toString());
        dto.setIdFascia(idFascia);
        dto.setFasciaOraria(fascia.getEtichetta());

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
            dto.getAree().add(adto);
        }
        return dto;
    }

    // ------------------------------------------------------------------ UC11
    /**
     * Monitoraggio in tempo reale: per ogni sala il numero di postazioni libere, occupate
     * da prenotazioni ATTIVE (non confermate) e CONFERMATE nella giornata corrente, più le
     * tipologie di area presenti.
     */
    public List<Object> monitoraSale() {
        LocalDate oggi = LocalDate.now();
        List<Object> risultato = new ArrayList<>();
        for (SalaStudio sala : registroSale.getTutteLeSale()) {
            // Stato per postazione oggi: 'C' = confermata (presenza), 'A' = attiva non confermata.
            Map<Long, Character> statoPostazione = new HashMap<>();
            for (Prenotazione p : registroPrenotazioni.cercaPrenotazioniPerSalaEData(sala.getId(), oggi)) {
                if (p.getPostazione() == null) continue;
                Long pid = p.getPostazione().getId();
                if (p.getStato().getStatoEnum() == StatoEnum.CONFERMATA) {
                    statoPostazione.put(pid, 'C');
                } else if (statoPostazione.get(pid) == null) {
                    statoPostazione.put(pid, 'A');
                }
            }
            SalaMonitoraggioDTO dto = new SalaMonitoraggioDTO();
            dto.setIdSala(sala.getId());
            dto.setNomeSala(sala.getNome());
            int liberi = 0, attivi = 0, confermati = 0;
            for (Area area : registroSale.getAreePerSala(sala.getId())) {
                dto.getAree().add(area.getTipologia());
                for (Postazione p : registroSale.getPostazioniPerArea(area.getId())) {
                    Character st = statoPostazione.get(p.getId());
                    if (st == null) liberi++;
                    else if (st == 'C') confermati++;
                    else attivi++;
                }
            }
            dto.setPostiLiberi(liberi);
            dto.setPostiAttivi(attivi);
            dto.setPostiConfermati(confermati);
            risultato.add(dto);
        }
        return risultato;
    }

    // ------------------------------------------------------------------ helper
    private int contaPostiDisponibili(Long idSala, LocalDate data, FasciaOraria fascia) {
        int posti = 0;
        for (Area area : registroSale.getAreePerSala(idSala)) {
            posti += area.getPostazioniDisponibili(data, fascia).size();
        }
        return posti;
    }

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

    private LocalTime parseOrario(String orario) {
        try {
            return LocalTime.parse(orario);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Formato orario non valido (atteso HH:mm): " + orario);
        }
    }

    private List<FasciaOraria> generaSlot(LocalTime apertura, LocalTime chiusura, int grana) {
        List<FasciaOraria> slot = new ArrayList<>();
        LocalTime inizio = apertura;
        LocalTime fine = inizio.plusMinutes(grana);
        while (fine.isAfter(inizio) && !fine.isAfter(chiusura)) {
            // Rimosso il parametro 'sala', ora usiamo il costruttore corretto di FasciaOraria
            slot.add(new FasciaOraria(inizio, fine));
            inizio = fine;
            fine = inizio.plusMinutes(grana);
        }
        return slot;
    }

    private SalaStudioDTO toDTO(SalaStudio sala) {
        SalaStudioDTO dto = new SalaStudioDTO();
        dto.setId(sala.getId());
        dto.setNome(sala.getNome());
        dto.setDescrizione(sala.getDescrizione());
        dto.setNumeroPostazioniTotali(sala.getNumeroPostazioniTotali());
        List<String> fasce = new ArrayList<>();
        for (FasciaOraria f : registroSale.getFascePerSala(sala.getId())) {
            fasce.add(f.getEtichetta());
        }
        dto.setFasceOrarie(fasce);
        return dto;
    }

    private PostazioneDTO toPostazioneDTO(Postazione p, String tipologiaArea, boolean disponibile, int numero) {
        PostazioneDTO dto = new PostazioneDTO();
        dto.setId(p.getId());
        dto.setNumero(numero);
        dto.setTipologiaArea(tipologiaArea);
        dto.setDisponibile(disponibile);
        return dto;
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
