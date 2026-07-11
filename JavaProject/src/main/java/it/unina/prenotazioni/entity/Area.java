package it.unina.prenotazioni.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * <<entity>> Zona tipizzata di una sala (es. "silenziosa", "comune") che raggruppa
 * le postazioni; ogni sala ha sempre almeno l'area di default "comune" (V19).
 */
@Entity
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipologia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sala_studio_id", nullable = false)
    private SalaStudio salaStudio;

    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Postazione> postazioni = new ArrayList<>();

    public Area() {}

    public Area(String tipologia, SalaStudio salaStudio) {
        this.tipologia = tipologia;
        this.salaStudio = salaStudio;
    }

    // Getters (niente setter: i campi sono valorizzati dal costruttore e da JPA via field access)
    public Long getId() { return id; }

    public String getTipologia() { return tipologia; }

    public SalaStudio getSalaStudio() { return salaStudio; }

    public List<Postazione> getPostazioni() { return postazioni; }

    /**
     * Postazioni libere dell'area per (data, fascia). Itera sulle proprie postazioni
     * (caricate dal Registro per evitare la navigazione lazy su entità distaccate) e
     * interroga ciascuna con disponibilita(data, fascia).
     */
    public List<Postazione> getPostazioniDisponibili(LocalDate data, FasciaOraria fascia) {
        List<Postazione> disponibili = new ArrayList<>();
        RegistroSale registro = RegistroSale.getInstance();
        for (Postazione p : registro.getPostazioniPerArea(id)) {
            if (p.disponibilita(data, fascia)) {
                disponibili.add(p);
            }
        }
        return disponibili;
    }

    /** Genera in memoria le postazioni dell'area; la persistenza avviene a cascata (UC3). */
    public void creaPostazioni(int numeroPostazioni) {
        for (int i = 0; i < numeroPostazioni; i++) {
            Postazione p = new Postazione(this);
            this.postazioni.add(p);
        }
    }

}
