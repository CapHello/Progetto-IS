package it.unina.prenotazioni.boundary;

import it.unina.prenotazioni.controller.BibliotecaFacade;
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

    @GetMapping("/sale")
    public List<Object> monitoraSale() {
        return BibliotecaFacade.getInstance().monitoraSale();
    }

    @GetMapping("/prenotazioni/{idSalaStudio}")
    public List<Object> monitoraPrenotazioni(@PathVariable("idSalaStudio") Long idSalaStudio) {
        return BibliotecaFacade.getInstance().monitoraPrenotazioni(idSalaStudio);
    }

    @GetMapping("/statistiche")
    public StatisticheDTO monitoraStatisticheServizio() {
        return (StatisticheDTO) BibliotecaFacade.getInstance().monitoraStatisticheServizio();
    }
}
