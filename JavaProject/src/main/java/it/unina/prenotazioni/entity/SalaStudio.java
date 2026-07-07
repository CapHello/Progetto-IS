package it.unina.prenotazioni.entity;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SalaStudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descrizione;
    private int numeroPostazioniTotali;

    @OneToMany(mappedBy = "salaStudio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Area> aree = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "sala_slot_prenotabili",
            joinColumns = @JoinColumn(name = "sala_id"),
            inverseJoinColumns = @JoinColumn(name = "fascia_id")
    )
    private List<FasciaOraria> slotOrario = new ArrayList<>();

    // Associazione Molti-A-Molti per l'orario lavorativo
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "sala_orari_lavorativi",
            joinColumns = @JoinColumn(name = "sala_id"),
            inverseJoinColumns = @JoinColumn(name = "fascia_id")
    )
    private List<FasciaOraria> orarioLavorativo = new ArrayList<>();

    public SalaStudio() {}

    public SalaStudio(String nome, String descrizione, int numeroPostazioniTotali) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.numeroPostazioniTotali = numeroPostazioniTotali;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public int getNumeroPostazioniTotali() { return numeroPostazioniTotali; }
    public void setNumeroPostazioniTotali(int numeroPostazioniTotali) { this.numeroPostazioniTotali = numeroPostazioniTotali; }

    public List<Area> getAree() { return aree; }
    public void setAree(List<Area> aree) { this.aree = aree; }

    public List<FasciaOraria> getOrarioLavorativo() { return orarioLavorativo; }
    public void setOrarioLavorativo(List<FasciaOraria> orarioLavorativo) {
        if (orarioLavorativo != null && orarioLavorativo.size() > 5) {
            throw new IllegalArgumentException("Una sala non può avere più di 5 orari lavorativi.");
        }
        this.orarioLavorativo = orarioLavorativo;
    }

    public List<FasciaOraria> getSlotOrario() { return slotOrario; }
    public void setSlotOrari(List<FasciaOraria> slotOrario) { this.slotOrario = slotOrario; }

    // --- Costruzione in memoria (usata in CreaSalaStudio, prima della persistenza) ---

    /** Aggiunge una fascia oraria (slot prenotabile) all'orario della sala. */
    public void addFascia(FasciaOraria fascia) {
        slotOrario.add(fascia);
    }

    public void addOrarioLavorativo(FasciaOraria fascia) {
        if (this.orarioLavorativo.size() >= 5) {
            throw new IllegalStateException("Impossibile aggiungere un altro orario: limite massimo di 5 orari lavorativi raggiunto per questa sala.");
        }
        this.orarioLavorativo.add(fascia);
    }

    /**
     * Aggiunge alla sala (in memoria) una nuova Area con la tipologia indicata e
     * numPostazioni postazioni, collegate sia all'area sia alla sala (per il cascade in
     * creazione). Usata nel ciclo di CreaSalaStudio, una chiamata per ogni area indicata
     * dal bibliotecario. Verifica che l'area abbia almeno una postazione (V04) e che il
     * totale assegnato non superi la capienza della sala.
     */

    public Area aggiungiArea(String tipologia, int numPostazioni) {
        if (numPostazioni < 1) {
            throw new IllegalArgumentException("Ogni area deve contenere almeno una postazione (V04)");
        }

        // Calcoliamo quante postazioni sono già state assegnate alle altre aree
        int postazioniAssegnate = aree.stream()
                .mapToInt(a -> a.getPostazioni().size())
                .sum();

        verificaNumeroPostazioni(postazioniAssegnate + numPostazioni);

        Area area = new Area(tipologia, this);

        aree.add(area);
        for (int i = 0; i < numPostazioni; i++) {
            Postazione p = new Postazione(area);
            area.addPostazione(p);
        }
        return area;
    }

    /**
     * Crea l'area di default "comune" (V19) con le postazioni rimanenti non assegnate ad
     * aree specifiche (step 6 dello scenario CreaSalaStudio).
     */
    public void creaAreaDefault(int postazioniRimanenti) {
        aggiungiArea("comune", postazioniRimanenti);
    }

    // Il numero di postazioni assegnate alle aree non può superare il totale della sala.
    private void verificaNumeroPostazioni(int numAssegnate) {
        if (numAssegnate > numeroPostazioniTotali) {
            throw new IllegalArgumentException(
                    "Le postazioni assegnate alle aree superano il totale della sala studio");
        }
    }

    // --- Consultazione / disponibilità ---

    /** V06: la sala è aperta nei giorni feriali (lunedì-venerdì). */
    public boolean verificaDataInGiorniApertura(LocalDate data) {
        DayOfWeek giorno = data.getDayOfWeek();
        return giorno != DayOfWeek.SATURDAY && giorno != DayOfWeek.SUNDAY;
    }

    /** Fasce orarie prenotabili per la data indicata (vuoto se la sala è chiusa quel giorno). */
    public List<FasciaOraria> getFasceOrariePrestabilite(LocalDate data) {
        if (!verificaDataInGiorniApertura(data)) {
            return new ArrayList<>();
        }
        return RegistroSale.getInstance().getFascePerSala(id);
    }

    /** True se esiste almeno una postazione libera nella sala per (data, fascia). */
    public boolean verificaDisponibilita(LocalDate data, FasciaOraria fascia) {
        RegistroSale registro = RegistroSale.getInstance();
        for (Area area : registro.getAreePerSala(id)) {
            if (!area.getPostazioniDisponibili(data, fascia).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Postazioni libere di una specifica area per (data, fascia). */
    public List<Postazione> getPostazioniDisponibili(Area area, LocalDate data, FasciaOraria fascia) {
        return area.getPostazioniDisponibili(data, fascia);
    }

    public boolean verificaValiditaDati() {
        if (nome == null || nome.isEmpty() || numeroPostazioniTotali <= 0) {
            return false;
        }
        // Il vincolo di dominio impone massimo 5 orari lavorativi
        return !(orarioLavorativo != null && orarioLavorativo.size() > 5);
    }

    /**
     * Elimina tutte le aree della sala e, tramite ciascuna, le relative postazioni
     * (UC4 EliminaSalaStudio). Le prenotazioni che insistono sulle postazioni devono
     * essere già state rimosse dal GestoreSale per rispettare i vincoli di integrità.
     */
    public void eliminaAree() {
        RegistroSale registro = RegistroSale.getInstance();
        for (Area area : registro.getAreePerSala(id)) {
            area.eliminaPostazioni();
            registro.eliminaArea(area.getId());
        }
    }
}
