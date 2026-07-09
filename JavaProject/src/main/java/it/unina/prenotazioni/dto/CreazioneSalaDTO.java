package it.unina.prenotazioni.dto;

import java.util.List;

/**
 * Parameter object di UC3: raccoglie l'intera richiesta di creazione sala (JSON del
 * front-end, mappato da Spring via @RequestBody). tipologie[i] e postazioniAree[i]
 * descrivono l'i-esima area; orariApertura/Chiusura coprono i 5 giorni Lunedì-Venerdì.
 */
public class CreazioneSalaDTO {
    private String nome;
    private String descrizione;
    private int numeroPostazioni;
    private List<String> orariApertura;
    private List<String> orariChiusura;
    private int granaMinuti;
    private List<String> tipologie;
    private List<Integer> postazioniAree;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public int getNumeroPostazioni() {
        return numeroPostazioni;
    }

    public void setNumeroPostazioni(int numeroPostazioni) {
        this.numeroPostazioni = numeroPostazioni;
    }

    public List<String> getOrariApertura() {
        return orariApertura;
    }

    public void setOrariApertura(List<String> orariApertura) {
        this.orariApertura = orariApertura;
    }

    public List<String> getOrariChiusura() {
        return orariChiusura;
    }

    public void setOrariChiusura(List<String> orariChiusura) {
        this.orariChiusura = orariChiusura;
    }

    public int getGranaMinuti() {
        return granaMinuti;
    }

    public void setGranaMinuti(int granaMinuti) {
        this.granaMinuti = granaMinuti;
    }

    public List<String> getTipologie() {
        return tipologie;
    }

    public void setTipologie(List<String> tipologie) {
        this.tipologie = tipologie;
    }

    public List<Integer> getPostazioniAree() {
        return postazioniAree;
    }

    public void setPostazioniAree(List<Integer> postazioniAree) {
        this.postazioniAree = postazioniAree;
    }
}