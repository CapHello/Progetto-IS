package it.unina.prenotazioni.dto;

import java.time.LocalDate;

/**
 * Parameter object di UC7: raggruppa i dati della richiesta di prenotazione,
 * come CreazioneSalaDTO fa per UC3. idPostazione null o 0 = assegnazione automatica.
 */
public class RichiestaPrenotazioneDTO {
    private Long idSala;
    private Long idArea;
    private Long idPostazione;
    private LocalDate data;
    private Long idFascia;
    private Long idStudente;

    public RichiestaPrenotazioneDTO() {}

    public RichiestaPrenotazioneDTO(Long idSala, Long idArea, Long idPostazione,
                                    LocalDate data, Long idFascia, Long idStudente) {
        this.idSala = idSala;
        this.idArea = idArea;
        this.idPostazione = idPostazione;
        this.data = data;
        this.idFascia = idFascia;
        this.idStudente = idStudente;
    }

    public Long getIdSala() { return idSala; }
    public void setIdSala(Long idSala) { this.idSala = idSala; }

    public Long getIdArea() { return idArea; }
    public void setIdArea(Long idArea) { this.idArea = idArea; }

    public Long getIdPostazione() { return idPostazione; }
    public void setIdPostazione(Long idPostazione) { this.idPostazione = idPostazione; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Long getIdFascia() { return idFascia; }
    public void setIdFascia(Long idFascia) { this.idFascia = idFascia; }

    public Long getIdStudente() { return idStudente; }
    public void setIdStudente(Long idStudente) { this.idStudente = idStudente; }
}
