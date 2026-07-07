package it.unina.prenotazioni.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Studente extends Utente{

    @Column(nullable = false, unique = true)
    private String matricola;

    private int numeroTotaleAccessi;

    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prenotazione> prenotazioni = new ArrayList<>();

    public Studente() { /* Vuoto perché utilizziamo il Factory Method @Antonio Cacciatore*/ }

    // Getters and Setters
    public String getMatricola() { return matricola; }
    public void setMatricola(String matricola) { this.matricola = matricola; }

    public int getNumeroTotaleAccessi() { return numeroTotaleAccessi; }
    public void setNumeroTotaleAccessi(int numeroTotaleAccessi) { this.numeroTotaleAccessi = numeroTotaleAccessi; }

    public List<Prenotazione> getPrenotazioni() { return prenotazioni; }
    public void setPrenotazioni(List<Prenotazione> prenotazioni) { this.prenotazioni = prenotazioni; }

    public void addPrenotazione(Prenotazione prenotazione) {
        prenotazioni.add(prenotazione);
        prenotazione.setStudente(this);
    }

    public void removePrenotazione(Prenotazione prenotazione) {
        prenotazioni.remove(prenotazione);
        prenotazione.setStudente(null);
    }

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
