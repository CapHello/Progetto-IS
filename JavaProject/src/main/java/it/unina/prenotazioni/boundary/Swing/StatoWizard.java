package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.dto.DettaglioSalaDTO;
import it.unina.prenotazioni.dto.UtenteDTO;

import java.time.LocalDate;

class StatoWizard {

    static final Long ASSEGNAZIONE_AUTOMATICA = 0L;

    private final UtenteDTO studente;

    private LocalDate data;
    private Long      idSala;
    private String    nomeSala;
    private Long      idFascia;
    private String    etichettaFascia;
    private DettaglioSalaDTO dettaglio;
    private Long      idArea;
    private String    tipologiaArea;
    private Long      idPostazione;        // null = non scelta, 0 = assegnazione automatica
    private String    etichettaPostazione;

    StatoWizard(UtenteDTO studente) {
        this.studente = studente;
    }

    UtenteDTO getStudente() { return studente; }

    LocalDate getData()          { return data; }
    void setData(LocalDate data) { this.data = data; }

    Long getIdSala()            { return idSala; }
    void setIdSala(Long idSala) { this.idSala = idSala; }

    String getNomeSala()              { return nomeSala; }
    void setNomeSala(String nomeSala) { this.nomeSala = nomeSala; }

    Long getIdFascia()              { return idFascia; }
    void setIdFascia(Long idFascia) { this.idFascia = idFascia; }

    String getEtichettaFascia()                     { return etichettaFascia; }
    void setEtichettaFascia(String etichettaFascia) { this.etichettaFascia = etichettaFascia; }

    DettaglioSalaDTO getDettaglio()                { return dettaglio; }
    void setDettaglio(DettaglioSalaDTO dettaglio)  { this.dettaglio = dettaglio; }

    Long getIdArea()            { return idArea; }
    void setIdArea(Long idArea) { this.idArea = idArea; }

    String getTipologiaArea()                   { return tipologiaArea; }
    void setTipologiaArea(String tipologiaArea) { this.tipologiaArea = tipologiaArea; }

    Long getIdPostazione()                  { return idPostazione; }
    void setIdPostazione(Long idPostazione) { this.idPostazione = idPostazione; }

    String getEtichettaPostazione()                         { return etichettaPostazione; }
    void setEtichettaPostazione(String etichettaPostazione) { this.etichettaPostazione = etichettaPostazione; }
}
