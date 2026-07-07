package it.unina.prenotazioni.dto;

import java.util.ArrayList;
import java.util.List;

/** Area con l'elenco delle sue postazioni (e relativa disponibilità) per una data/fascia. */
public class AreaDettaglioDTO {
    private Long idArea;
    private String tipologia;
    private int postiDisponibili;
    private List<PostazioneDTO> postazioni = new ArrayList<>();

    public AreaDettaglioDTO() {}

    public AreaDettaglioDTO(Long idArea, String tipologia) {
        this.idArea = idArea;
        this.tipologia = tipologia;
    }

    public Long getIdArea() { return idArea; }
    public void setIdArea(Long idArea) { this.idArea = idArea; }
    public String getTipologia() { return tipologia; }
    public void setTipologia(String tipologia) { this.tipologia = tipologia; }
    public int getPostiDisponibili() { return postiDisponibili; }
    public void setPostiDisponibili(int postiDisponibili) { this.postiDisponibili = postiDisponibili; }
    public List<PostazioneDTO> getPostazioni() { return postazioni; }
    public void setPostazioni(List<PostazioneDTO> postazioni) { this.postazioni = postazioni; }
}
