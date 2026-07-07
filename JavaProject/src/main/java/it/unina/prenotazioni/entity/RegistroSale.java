package it.unina.prenotazioni.entity;

import it.unina.prenotazioni.database.GestorePersistenza;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facade di layer (entity) per l'aggregato Sala Studio (Sala, Area, Postazione,
 * FasciaOraria). Espone ai controller ricerca/salvataggio a grana grossa.
 */
public class RegistroSale {

    private static RegistroSale istance;
    private final GestorePersistenza gestorePersistenza;

    private RegistroSale() {
        this.gestorePersistenza = new GestorePersistenza();
    }

    public static RegistroSale getInstance() {
        if (istance == null) {
            istance = new RegistroSale();
        }
        return istance;
    }

    // --- CRUD Sala ---
    public boolean salvaSala(SalaStudio salaStudio) {
        return gestorePersistenza.salva(salaStudio);
    }

    public SalaStudio aggiornaSala(SalaStudio salaStudio) {
        return gestorePersistenza.aggiorna(salaStudio);
    }

    public SalaStudio cercaSalaPerId(Long id) {
        return gestorePersistenza.trovaPerId(SalaStudio.class, id);
    }

    public SalaStudio cercaSalaPerNome(String nome) {
        return gestorePersistenza.cercaPrimoPerCampi(SalaStudio.class, Map.of("nome", nome));
    }

    public boolean eliminaSala(Long id) {
        return gestorePersistenza.elimina(SalaStudio.class, id);
    }

    //Serve per eliminaAree() di Sala Studio
    public boolean eliminaArea(Long id) {
        return gestorePersistenza.elimina(Area.class, id);
    }

    //Serve per eliminaPostazioni() di Area
    public boolean eliminaPostazione(Long id) {
        return gestorePersistenza.elimina(Postazione.class, id);
    }

    public boolean eliminaFascia(Long id) {
        return gestorePersistenza.elimina(FasciaOraria.class, id);
    }

    public List<SalaStudio> getTutteLeSale() {
        return gestorePersistenza.cercaPerCampi(SalaStudio.class, Map.of());
    }

    /** Sale visibili/prenotabili in una certa data: quelle aperte (giorni feriali, V06). */
    public List<SalaStudio> getSaleDisponibili(LocalDate data) {
        List<SalaStudio> risultato = new ArrayList<>();
        for (SalaStudio s : getTutteLeSale()) {
            if (s.verificaDataInGiorniApertura(data)) {
                risultato.add(s);
            }
        }
        return risultato;
    }

    // --- Aree ---
    public boolean salvaArea(Area area) {
        return gestorePersistenza.salva(area);
    }

    public Area trovaAreaPerId(Long idArea) {
        return gestorePersistenza.trovaPerId(Area.class, idArea);
    }

    public List<Area> getAreePerSala(Long idSala) {
        return gestorePersistenza.cercaPerCampo(Area.class, "salaStudio.id", idSala);
    }

    // --- Postazioni ---
    public boolean salvaPostazione(Postazione postazione) {
        return gestorePersistenza.salva(postazione);
    }

    public List<Postazione> getPostazioniPerArea(Long idArea) {
        return gestorePersistenza.cercaPerCampo(Postazione.class, "area.id", idArea);
    }

    public List<Postazione> getPostazioniPerSala(Long idSala) {
        return gestorePersistenza.cercaPerCampo(Postazione.class, "area.salaStudio.id", idSala);
    }

    public Postazione trovaPostazionePerId(Long idPostazione) {
        return gestorePersistenza.trovaPerId(Postazione.class, idPostazione);
    }

    /** Postazioni libere di un'area per (data, fascia): delega alla logica di dominio dell'Area. */
    public List<Postazione> getPostazioniDisponibili(Long idArea, LocalDate data, Long idFascia) {
        Area area = trovaAreaPerId(idArea);
        FasciaOraria fascia = trovaFasciaPerId(idFascia);
        if (area == null || fascia == null) {
            return new ArrayList<>();
        }
        return area.getPostazioniDisponibili(data, fascia);
    }

    // --- Fasce orarie (slot prenotabili) ---
    public FasciaOraria trovaFasciaPerId(Long id) {
        return gestorePersistenza.trovaPerId(FasciaOraria.class, id);
    }

    public List<FasciaOraria> getFascePerSala(Long idSala) {
        return gestorePersistenza.cercaPerCampo(FasciaOraria.class, "salaStudio.id", idSala);
    }
}
