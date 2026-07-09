package it.unina.prenotazioni.boundary;

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
 * <<endpoint>> Gestione sale: crea (UC3), elimina (UC4), aggiungi area, consulta
 * disponibilità (UC6), fasce e dettaglio postazioni (drill-down del wizard).
 */
@RestController
@RequestMapping("/api/sale")
public class BoundarySalaStudio {

    @PostMapping("/crea")
    public SalaStudioDTO creaSalaStudio(@RequestBody CreazioneSalaDTO dto) {
        // Spring Boot mappa automaticamente il JSON in ingresso sui campi di CreazioneSalaDTO
        return BibliotecaFacade.getInstance().creaSalaStudio(dto);
    }

    @DeleteMapping("/{idSalaStudio}")
    public void eliminaSalaStudio(@PathVariable("idSalaStudio") Long idSalaStudio) { //serve @PathVariable perchè abbiamo una rotta dinamica
        BibliotecaFacade.getInstance().eliminaSalaStudio(idSalaStudio);
    }

    @GetMapping("/disponibili")
    public List<SalaStudioDTO> consultazioneSaleDisponibili(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return BibliotecaFacade.getInstance().consultaSaleDisponibili(data);
    }

    @GetMapping("/{idSala}/fasce")
    public List<FasciaDisponibileDTO> getFasceDisponibili(
            @PathVariable("idSala") Long idSala,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return BibliotecaFacade.getInstance().getFasceDisponibili(idSala, data);
    }

    @GetMapping("/{idSala}/dettaglio")
    public DettaglioSalaDTO selezionaDettaglioSala(
            @PathVariable("idSala") Long idSala,
            @RequestParam("idFascia") Long idFascia,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return BibliotecaFacade.getInstance().selezionaDettaglioSala(idSala, idFascia, data);
    }
}
