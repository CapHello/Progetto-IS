package it.unina.prenotazioni.boundary.SpringBoot;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.CreazioneSalaDTO;
import it.unina.prenotazioni.dto.DettaglioSalaDTO;
import it.unina.prenotazioni.dto.FasciaDisponibileDTO;
import it.unina.prenotazioni.dto.SalaStudioDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestione sale: crea (UC3), elimina (UC4), consulta disponibilità (UC6),
 * fasce e dettaglio postazioni (passi del wizard di prenotazione).
 */
@RestController
@RequestMapping("/api/sale")
public class BoundarySalaStudio {

    /**
     * UC3: crea una sala; il corpo JSON viene mappato da Spring sui campi di CreazioneSalaDTO.
     * @param dto dto
     */
    @PostMapping("/crea")
    public SalaStudioDTO creaSalaStudio(@RequestBody CreazioneSalaDTO dto) {
        return BibliotecaFacade.getInstance().creaSalaStudio(dto);
    }

    /**
     * UC4: elimina (disattiva) la sala indicata nella rotta.
     * @param idSalaStudio idSalaStudio
     */
    @DeleteMapping("/{idSalaStudio}")
    public void eliminaSalaStudio(@PathVariable("idSalaStudio") Long idSalaStudio) {
        BibliotecaFacade.getInstance().eliminaSalaStudio(idSalaStudio);
    }

    /**
     * UC6: sale disponibili nella data indicata.
     * @param data data
     */
    @GetMapping("/disponibili")
    public List<SalaStudioDTO> consultazioneSaleDisponibili(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return BibliotecaFacade.getInstance().consultaSaleDisponibili(data);
    }

    /**
     * Wizard step 2: fasce prenotabili della sala nella data, con posti liberi.
     * @param idSala idSala
     * @param data data
     */
    @GetMapping("/{idSala}/fasce")
    public List<FasciaDisponibileDTO> getFasceDisponibili(
            @PathVariable("idSala") Long idSala,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return BibliotecaFacade.getInstance().getFasceDisponibili(idSala, data);
    }

    /**
     * Wizard step 3-4: dettaglio aree/postazioni per (data, fascia).
     * @param idSala idSala
     * @param idFascia idFascia
     * @param data data
     */
    @GetMapping("/{idSala}/dettaglio")
    public DettaglioSalaDTO selezionaDettaglioSala(
            @PathVariable("idSala") Long idSala,
            @RequestParam("idFascia") Long idFascia,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return BibliotecaFacade.getInstance().selezionaDettaglioSala(idSala, idFascia, data);
    }
}
