package it.unina.prenotazioni.dto;

import java.util.ArrayList;
import java.util.List;

/** Dettaglio di una sala per (data, fascia): aree e postazioni con disponibilità (wizard step 3-4). */
public class DettaglioSalaDTO {
    private Long idSala;
    private String nomeSala;
    private String data;          // ISO, es. "2026-06-18"
    private Long idFascia;
    private String fasciaOraria;  // etichetta, es. "08:30-10:00"
    private List<AreaDettaglioDTO> aree = new ArrayList<>();

    public DettaglioSalaDTO() {}

    public Long getIdSala() { return idSala; }
    public void setIdSala(Long idSala) { this.idSala = idSala; }
    public String getNomeSala() { return nomeSala; }
    public void setNomeSala(String nomeSala) { this.nomeSala = nomeSala; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public Long getIdFascia() { return idFascia; }
    public void setIdFascia(Long idFascia) { this.idFascia = idFascia; }
    public String getFasciaOraria() { return fasciaOraria; }
    public void setFasciaOraria(String fasciaOraria) { this.fasciaOraria = fasciaOraria; }
    public List<AreaDettaglioDTO> getAree() { return aree; }
    public void setAree(List<AreaDettaglioDTO> aree) { this.aree = aree; }
}
