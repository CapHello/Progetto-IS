package it.unina.prenotazioni.boundary;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.PrenotazioneDTO;
import it.unina.prenotazioni.dto.SalaMonitoraggioDTO;
import it.unina.prenotazioni.dto.StatisticheDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <<endpoint>> Monitoraggio del bibliotecario: stato sale (UC11), prenotazioni attive
 * per sala (UC5) e statistiche di servizio (UC13).
 */

@RestController
@RequestMapping("/api/monitoraggio")
public class BoundaryMonitoraggio {

    /** UC11: stato in tempo reale di tutte le sale. */
    @GetMapping("/sale")
    public List<SalaMonitoraggioDTO> monitoraSale() {
        return BibliotecaFacade.getInstance().monitoraSale();
    }

    /** UC5: prenotazioni occupanti della giornata per la sala indicata. */
    @GetMapping("/prenotazioni/{idSalaStudio}")
    public List<PrenotazioneDTO> monitoraPrenotazioni(@PathVariable("idSalaStudio") Long idSalaStudio) {
        return BibliotecaFacade.getInstance().monitoraPrenotazioni(idSalaStudio);
    }

    /** UC13: statistiche di servizio della giornata. */
    @GetMapping("/statistiche")
    public StatisticheDTO monitoraStatisticheServizio() {
        return BibliotecaFacade.getInstance().monitoraStatisticheServizio();
    }
}
