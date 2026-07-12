package it.unina.prenotazioni.boundary.SpringBoot;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.SalaMonitoraggioDTO;
import it.unina.prenotazioni.dto.StatisticheDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Monitoraggio del bibliotecario: stato sale (UC11) e statistiche
 * di servizio (UC13).
 */

@RestController
@RequestMapping("/api/monitoraggio")
public class BoundaryMonitoraggio {

    /** UC11: stato in tempo reale di tutte le sale. */
    @GetMapping("/sale")
    public List<SalaMonitoraggioDTO> monitoraSale() {
        return BibliotecaFacade.getInstance().monitoraSale();
    }

    /** UC13: statistiche di servizio della giornata. */
    @GetMapping("/statistiche")
    public StatisticheDTO monitoraStatisticheServizio() {
        return BibliotecaFacade.getInstance().monitoraStatisticheServizio();
    }
}
