package it.unina.prenotazioni.dto;

/** Fascia oraria prenotabile con il numero di postazioni libere (wizard step 2). */
public class FasciaDisponibileDTO {
    private Long idFascia;
    private String etichetta;      // es. "08:30-10:00"
    private int postiDisponibili;

    public FasciaDisponibileDTO() {}

    public FasciaDisponibileDTO(Long idFascia, String etichetta, int postiDisponibili) {
        this.idFascia = idFascia;
        this.etichetta = etichetta;
        this.postiDisponibili = postiDisponibili;
    }

    public Long getIdFascia() { return idFascia; }
    public void setIdFascia(Long idFascia) { this.idFascia = idFascia; }
    public String getEtichetta() { return etichetta; }
    public void setEtichetta(String etichetta) { this.etichetta = etichetta; }
    public int getPostiDisponibili() { return postiDisponibili; }
    public void setPostiDisponibili(int postiDisponibili) { this.postiDisponibili = postiDisponibili; }
}
