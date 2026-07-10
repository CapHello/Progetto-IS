package it.unina.prenotazioni.boundary.SpringBoot;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.PrenotazioneDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * <<endpoint>> Effettua (UC7), Annulla (UC9) ed Effettua Check-in (UC10) delle prenotazioni.
 */
@RestController
@RequestMapping("/api/prenotazioni")
public class BoundaryPrenotazione {

    /** UC7: effettua una prenotazione (idPostazione = 0 richiede l'assegnazione automatica). */
    @PostMapping("/effettua")
    public PrenotazioneDTO effettuaPrenotazione(
            @RequestParam("idSala") Long idSala,
            @RequestParam(name = "idArea", required = false) Long idArea,
            @RequestParam(name = "idPostazione", required = false) Long idPostazione,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam("idFascia") Long idFascia,
            @RequestParam("idStudente") Long idStudente) {
        return BibliotecaFacade.getInstance()
                .effettuaPrenotazione(idSala, idArea, idPostazione, data, idFascia, idStudente);
    }

    /** UC9: annulla la prenotazione (consentito fino a 6 ore prima dell'inizio, V07). */
    @PutMapping("/{idPrenotazione}/annulla")
    public void annullaPrenotazione(@PathVariable("idPrenotazione") Long idPrenotazione) {
        BibliotecaFacade.getInstance().annullaPrenotazione(idPrenotazione);
    }

    /** UC10: check-in nel giorno della prenotazione, entro inizio fascia + tolleranza (V08). */
    @PutMapping("/{idPrenotazione}/checkin")
    public void effettuaCheckIn(@PathVariable("idPrenotazione") Long idPrenotazione) {
        BibliotecaFacade.getInstance().effettuaCheckin(idPrenotazione);
    }
}
