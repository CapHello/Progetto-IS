package it.unina.prenotazioni.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Riepilogo di monitoraggio di una sala per la giornata corrente (UC11): postazioni
 * libere, occupate da prenotazioni ATTIVE (non ancora confermate) e CONFERMATE, più
 * l'elenco delle tipologie di area presenti.
 */
public class SalaMonitoraggioDTO {
    private Long idSala;
    private String nomeSala;
    private int postiLiberi;
    private int postiAttivi;
    private int postiConfermati;
    private List<String> aree = new ArrayList<>();
    private boolean attiva;

    public SalaMonitoraggioDTO() {}

    public Long getIdSala() { return idSala; }
    public void setIdSala(Long idSala) { this.idSala = idSala; }
    public String getNomeSala() { return nomeSala; }
    public void setNomeSala(String nomeSala) { this.nomeSala = nomeSala; }
    public int getPostiLiberi() { return postiLiberi; }
    public void setPostiLiberi(int postiLiberi) { this.postiLiberi = postiLiberi; }
    public int getPostiAttivi() { return postiAttivi; }
    public void setPostiAttivi(int postiAttivi) { this.postiAttivi = postiAttivi; }
    public int getPostiConfermati() { return postiConfermati; }
    public void setPostiConfermati(int postiConfermati) { this.postiConfermati = postiConfermati; }
    public List<String> getAree() { return aree; }
    public void setAree(List<String> aree) { this.aree = aree; }
    public boolean isAttiva() {return attiva;}
    public void setAttiva(boolean attiva) {this.attiva = attiva;}
}
