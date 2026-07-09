package it.unina.prenotazioni.boundary.SpringBoot;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.UtenteDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <<endpoint>> Profilo personale (UC8) e storico prenotazioni (UC12) dello studente.
 */
@RestController
@RequestMapping("/api/studente")
public class BoundaryStudente {

    @GetMapping("/{idStudente}/profilo")
    public UtenteDTO visualizzaProfiloPersonale(@PathVariable("idStudente") Long idStudente) {
        return (UtenteDTO) BibliotecaFacade.getInstance().visualizzaProfiloPersonale(idStudente);
    }

    @GetMapping("/{idStudente}/storico")
    public List<Object> consultaStoricoPrenotazioni(@PathVariable("idStudente") Long idStudente) {
        return BibliotecaFacade.getInstance().consultaStoricoPrenotazioni(idStudente);
    }
}
