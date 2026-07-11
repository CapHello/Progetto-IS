package it.unina.prenotazioni.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * <entity> Utente che prenota le postazioni; identificato dalla matricola.
 * numeroTotaleAccessi conta i check-in andati a buon fine (UC10).
 */
@Entity
public class Studente extends Utente{

    @Column(nullable = false, unique = true)
    private String matricola;

    private int numeroTotaleAccessi;

    // Lato inverso dell'associazione con Prenotazione: serve solo al mapping JPA,
    // non lo leggiamo mai (le ricerche passano dal RegistroPrenotazioni).
    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prenotazione> prenotazioni = new ArrayList<>();

    public Studente() { /* Costruttore vuoto richiesto da JPA: l'istanza nasce da StudenteFactory (UC1). */ }

    // Getters and Setters
    public String getMatricola() { return matricola; }
    public void setMatricola(String matricola) { this.matricola = matricola; }

    public int getNumeroTotaleAccessi() { return numeroTotaleAccessi; }
    public void setNumeroTotaleAccessi(int numeroTotaleAccessi) { this.numeroTotaleAccessi = numeroTotaleAccessi; }

    /**
     * Vincolo di unicità (V18): true se lo studente possiede già una prenotazione in
     * stato ATTIVA o CONFERMATA nella stessa data e fascia oraria (qualsiasi sala).
     */
    public boolean verificaPrenotazioneAttivaOConfermataInDataFascia(LocalDate data, String fasciaEtichetta) {
        RegistroPrenotazioni registro = RegistroPrenotazioni.getInstance();
        for (Prenotazione p : registro.cercaPrenotazioneAttiva(matricola, data)) {
            if (p.getFasciaOraria() != null && fasciaEtichetta.equals(p.getFasciaOraria().getEtichetta())) {
                return true;
            }
        }
        return false;
    }
}
