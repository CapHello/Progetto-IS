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

    /** Salva la sala e, a cascata, aree, postazioni e fasce; false se il salvataggio fallisce. */
    public boolean salvaSala(SalaStudio salaStudio) {
        return gestorePersistenza.salva(salaStudio);
    }

    /** Aggiorna una sala esistente (es. soft delete di UC4). */
    public SalaStudio aggiornaSala(SalaStudio salaStudio) {
        return gestorePersistenza.aggiorna(salaStudio);
    }

    /** Carica una sala per id; null se non esiste. */
    public SalaStudio cercaSalaPerId(Long id) {
        return gestorePersistenza.trovaPerId(SalaStudio.class, id);
    }

    /** Tutte le sale, incluse quelle disattivate (monitoraggio UC11). */
    public List<SalaStudio> getTutteLeSale() {
        return gestorePersistenza.cercaPerCampi(SalaStudio.class, Map.of());
    }

    /** Solo le sale non eliminate (attiva = true). */
    public List<SalaStudio> getSaleAttive() {
        return gestorePersistenza.eseguiQueryCustom(
                "SELECT s FROM SalaStudio s WHERE s.attiva = true",
                SalaStudio.class,
                null
        );
    }

    /**
     * Sale visibili/prenotabili in una certa data: quelle aperte
     * in cui esiste almeno una fascia oraria con posti disponibili.
     */
    public List<SalaStudio> getSaleDisponibili(LocalDate data) {
        List<SalaStudio> risultato = new ArrayList<>();

        for (SalaStudio s : getSaleAttive()) {
            if (s.verificaDataInGiorniApertura(data)) {
                boolean salaHaPosti = false;

                List<FasciaOraria> fasce = s.getFasceOrariePrestabilite(data);

                for (FasciaOraria fascia : fasce) {
                    if (s.verificaDisponibilita(data, fascia)) {
                        salaHaPosti = true;
                        break;
                    }
                }

                if (salaHaPosti) {
                    risultato.add(s);
                }
            }
        }
        return risultato;
    }

    // --- Aree ---

    /** Carica un'area per id; null se non esiste. */
    public Area trovaAreaPerId(Long idArea) {
        return gestorePersistenza.trovaPerId(Area.class, idArea);
    }

    /** Aree di una sala (query dedicata: evita la navigazione lazy su entità detached). */
    public List<Area> getAreePerSala(Long idSala) {
        return gestorePersistenza.cercaPerCampo(Area.class, "salaStudio.id", idSala);
    }

    // --- Postazioni ---

    /** Postazioni di un'area (query dedicata: evita la navigazione lazy su entità detached). */
    public List<Postazione> getPostazioniPerArea(Long idArea) {
        return gestorePersistenza.cercaPerCampo(Postazione.class, "area.id", idArea);
    }

    /** Carica una postazione per id; null se non esiste. */
    public Postazione trovaPostazionePerId(Long idPostazione) {
        return gestorePersistenza.trovaPerId(Postazione.class, idPostazione);
    }

    /**
     * Postazioni libere di un'area per (data, fascia): delega alla logica di
     * dominio dell'Area.
     */
    public List<Postazione> getPostazioniDisponibili(Long idArea, LocalDate data, Long idFascia) {
        Area area = trovaAreaPerId(idArea);
        FasciaOraria fascia = trovaFasciaPerId(idFascia);
        if (area == null || fascia == null) {
            return new ArrayList<>();
        }
        return area.getPostazioniDisponibili(data, fascia);
    }

    // --- Fasce orarie (slot prenotabili) ---

    /** Carica una fascia per id; null se non esiste. */
    public FasciaOraria trovaFasciaPerId(Long id) {
        return gestorePersistenza.trovaPerId(FasciaOraria.class, id);
    }

    /**
     * Orari lavorativi di una sala. NB: questa JPQL non garantisce l'ordine per giorno;
     * per l'orario di uno specifico giorno usare SalaStudio.getOrarioLavorativoPerData.
     */
    public List<FasciaOraria> getOrariLavorativiPerSala(Long idSala) {
        String jpql = "SELECT f FROM SalaStudio s JOIN s.orarioLavorativo f WHERE s.id = :idSala";
        return gestorePersistenza.eseguiQueryCustom(
                jpql,
                FasciaOraria.class,
                Map.of("idSala", idSala)
        );
    }


    /** Tutti gli slot prenotabili della sala (unione dei giorni, senza filtro per data). */
    public List<FasciaOraria> getFascePerSala(Long idSala) {
        String jpql = "SELECT f FROM SalaStudio s JOIN s.slotOrario f WHERE s.id = :idSala";
        return gestorePersistenza.eseguiQueryCustom(jpql, FasciaOraria.class, Map.of("idSala", idSala));
    }
}
