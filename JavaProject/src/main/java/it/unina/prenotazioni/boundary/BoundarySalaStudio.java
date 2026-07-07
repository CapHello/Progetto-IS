package it.unina.prenotazioni.boundary;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.DettaglioSalaDTO;
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
    public SalaStudioDTO creaSalaStudio(@RequestParam("nome") String nome,
                                        @RequestParam("descrizione") String descrizione,
                                        @RequestParam("numeroPostazioni") int numeroPostazioni,
                                        @RequestParam("orariApertura") List<String> orariApertura,
                                        @RequestParam("orariChiusura") List<String> orariChiusura,
                                        @RequestParam("granaMinuti") int granaMinuti,
                                        @RequestParam(name = "tipologie", required = false) List<String> tipologie,  //required=false perchè è opzionale
                                        @RequestParam(name = "postazioniAree", required = false) List<Integer> postazioniAree) {
        return (SalaStudioDTO) BibliotecaFacade.getInstance().creaSalaStudio(
                nome, descrizione, numeroPostazioni, orariApertura, orariChiusura, granaMinuti, tipologie, postazioniAree);
    }

    @DeleteMapping("/{idSalaStudio}")
    public void eliminaSalaStudio(@PathVariable("idSalaStudio") Long idSalaStudio) { //serve @PathVariable perchè abbiamo una rotta dinamica
        BibliotecaFacade.getInstance().eliminaSalaStudio(idSalaStudio);
    }

    @GetMapping("/disponibili")
    public List<Object> consultazioneSaleDisponibili(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return BibliotecaFacade.getInstance().consultaSaleDisponibili(data);
    }

    @GetMapping("/{idSala}/fasce")
    public List<Object> getFasceDisponibili(
            @PathVariable("idSala") Long idSala,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return BibliotecaFacade.getInstance().getFasceDisponibili(idSala, data);
    }

    @GetMapping("/{idSala}/dettaglio")
    public DettaglioSalaDTO selezionaDettaglioSala(
            @PathVariable("idSala") Long idSala,
            @RequestParam("idFascia") Long idFascia,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return (DettaglioSalaDTO) BibliotecaFacade.getInstance().selezionaDettaglioSala(idSala, idFascia, data);
    }
}
