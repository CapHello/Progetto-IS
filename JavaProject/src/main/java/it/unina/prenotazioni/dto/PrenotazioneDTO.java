package it.unina.prenotazioni.dto;

import java.time.LocalDate;

/** Prenotazione per le boundary (UC7, UC5, UC12): stato, fascia e postazione in forma leggibile dalla GUI. */
public class PrenotazioneDTO {
    private Long idPrenotazione;
    private LocalDate data;
    private String stato;
    private String fasciaOraria; // es. "09:00-11:00"
    private String nomeSala;
    private String tipologiaArea;
    private Long idPostazione;
    private int numeroPostazione; // numero d'ordine del posto nell'area (1..N), per la GUI

    public PrenotazioneDTO() {}

    public Long getIdPrenotazione() { return idPrenotazione; }
    public void setIdPrenotazione(Long idPrenotazione) { this.idPrenotazione = idPrenotazione; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }
    public String getFasciaOraria() { return fasciaOraria; }
    public void setFasciaOraria(String fasciaOraria) { this.fasciaOraria = fasciaOraria; }
    public String getNomeSala() { return nomeSala; }
    public void setNomeSala(String nomeSala) { this.nomeSala = nomeSala; }
    public String getTipologiaArea() { return tipologiaArea; }
    public void setTipologiaArea(String tipologiaArea) { this.tipologiaArea = tipologiaArea; }
    public Long getIdPostazione() { return idPostazione; }
    public void setIdPostazione(Long idPostazione) { this.idPostazione = idPostazione; }
    public int getNumeroPostazione() { return numeroPostazione; }
    public void setNumeroPostazione(int numeroPostazione) { this.numeroPostazione = numeroPostazione; }
}
