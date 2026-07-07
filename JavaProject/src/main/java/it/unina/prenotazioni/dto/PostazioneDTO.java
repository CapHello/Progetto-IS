package it.unina.prenotazioni.dto;

public class PostazioneDTO {
    private Long id;
    private int numero;          // numero d'ordine del posto nell'area (1..N), per la GUI
    private String tipologiaArea;
    private boolean disponibile;

    public PostazioneDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getTipologiaArea() { return tipologiaArea; }
    public void setTipologiaArea(String tipologiaArea) { this.tipologiaArea = tipologiaArea; }
    public boolean isDisponibile() { return disponibile; }
    public void setDisponibile(boolean disponibile) { this.disponibile = disponibile; }
}
