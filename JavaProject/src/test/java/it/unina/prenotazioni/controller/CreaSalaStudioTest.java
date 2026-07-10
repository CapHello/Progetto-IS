package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.database.JpaUtil;
import it.unina.prenotazioni.dto.CreazioneSalaDTO;
import it.unina.prenotazioni.dto.SalaStudioDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite di Test - Crea Sala Studio")
class CreaSalaStudioTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;

    @BeforeEach
    void setUp() {
        bibliotecaFacade = BibliotecaFacade.getInstance();
        em = JpaUtil.getInstance().getEntityManager();
        svuotaTabelle();
    }

    private void svuotaTabelle() {
        EntityTransaction tx = em.getTransaction();
        try {
            if (!tx.isActive()) tx.begin();
            // Pulizia delle Join Tables e delle tabelle gerarchiche
            em.createNativeQuery("DELETE FROM sala_orari_lavorativi").executeUpdate();
            em.createNativeQuery("DELETE FROM sala_slot_prenotabili").executeUpdate();
            em.createQuery("DELETE FROM Prenotazione").executeUpdate();
            em.createQuery("DELETE FROM Postazione").executeUpdate();
            em.createQuery("DELETE FROM Area").executeUpdate();
            em.createQuery("DELETE FROM FasciaOraria").executeUpdate();
            em.createQuery("DELETE FROM SalaStudio").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Errore durante la pulizia del DB di test: " + e.getMessage());
        }
    }

    /** Helper: Genera un DTO valido di base (TC1) per evitare duplicazioni di codice */
    private CreazioneSalaDTO creaRichiestaValida() {
        CreazioneSalaDTO dto = new CreazioneSalaDTO();
        dto.setNome("Sala Newton");
        dto.setDescrizione("Piano terra");
        dto.setNumeroPostazioni(50);

        // Simuliamo l'inserimento per i 5 giorni lavorativi (Lun-Ven)
        dto.setOrariApertura(new ArrayList<>(List.of("08:30", "08:30", "08:30", "08:30", "08:30")));
        dto.setOrariChiusura(new ArrayList<>(List.of("18:30", "18:30", "18:30", "18:30", "18:30")));

        dto.setGranaMinuti(120); // Es. fasce da 2 ore (che genera anche la fascia 09:00-11:00)

        // 2 Aree aggiuntive documentate nel TC1
        dto.setTipologie(new ArrayList<>(List.of("Silenziosa")));
        dto.setPostazioniAree(new ArrayList<>(List.of(25))); // Totale 50

        return dto;
    }

    // ==========================================
    // CASO DI SUCCESSO
    // ==========================================

    @Test
    @DisplayName("TC1: Tutti input validi")
    void creaSalaStudio_Successo() {
        CreazioneSalaDTO richiesta = creaRichiestaValida();

        SalaStudioDTO risultato = bibliotecaFacade.creaSalaStudio(richiesta);

        assertNotNull(risultato);
        assertEquals("Sala Newton", risultato.getNome());
        assertEquals(50, risultato.getNumeroPostazioniTotali());
        assertTrue(risultato.getId() > 0, "La sala deve essere stata salvata con un ID generato");
    }

    // ==========================================
    // CASI DI ERRORE - NOME E DESCRIZIONE
    // ==========================================

    @Test
    @DisplayName("TC2: Nome vuoto")
    void creaSalaStudio_NomeVuoto_LanciaEccezione() {
        CreazioneSalaDTO richiesta = creaRichiestaValida();
        richiesta.setNome("");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.creaSalaStudio(richiesta);
        });
        assertEquals("Nome sala non valido (1-50 caratteri, senza simboli speciali)", exception.getMessage());
    }

    @Test
    @DisplayName("TC3: Nome stringa > 50 caratteri")
    void creaSalaStudio_NomeTroppoLungo_LanciaEccezione() {
        CreazioneSalaDTO richiesta = creaRichiestaValida();
        richiesta.setNome("A".repeat(51));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.creaSalaStudio(richiesta);
        });
        assertEquals("Nome sala non valido (1-50 caratteri, senza simboli speciali)", exception.getMessage());
    }

    @Test
    @DisplayName("TC4: Nome con caratteri speciali")
    void creaSalaStudio_NomeCaratteriSpeciali_LanciaEccezione() {
        CreazioneSalaDTO richiesta = creaRichiestaValida();
        richiesta.setNome("Sala Newton @#");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.creaSalaStudio(richiesta);
        });
        assertEquals("Nome sala non valido (1-50 caratteri, senza simboli speciali)", exception.getMessage());
    }

    @Test
    @DisplayName("TC5: Descrizione > 256 caratteri")
    void creaSalaStudio_DescrizioneTroppoLunga_LanciaEccezione() {
        CreazioneSalaDTO richiesta = creaRichiestaValida();
        richiesta.setDescrizione("D".repeat(257));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.creaSalaStudio(richiesta);
        });
        assertEquals("Descrizione troppo lunga (max 256 caratteri)", exception.getMessage());
    }

    // ==========================================
    // CASI DI ERRORE - POSTAZIONI
    // ==========================================

    @Test
    @DisplayName("TC6: Postazioni <= 0")
    void creaSalaStudio_PostazioniZero_LanciaEccezione() {
        CreazioneSalaDTO richiesta = creaRichiestaValida();
        richiesta.setNumeroPostazioni(0);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.creaSalaStudio(richiesta);
        });
        assertEquals("La sala deve avere almeno una postazione", exception.getMessage());
    }

    /*
     * TC7: Postazioni non numeriche ("Cinquanta")
     * TC8: Postazioni valore decimale (50.5)
     * NON IMPLEMENTABILI IN JAVA: Il DTO richiede un tipo primitivo 'int'.
     * L'errore viene intercettato al livello del parsing (Spring/JSON)
     * prima di raggiungere il Facade.
     */

    // ==========================================
    // CASI DI ERRORE - ORARI E FASCE
    // ==========================================

    @Test
    @DisplayName("TC9: Orario formato non valido")
    void creaSalaStudio_OrarioFormatoNonValido_LanciaEccezione() {
        CreazioneSalaDTO richiesta = creaRichiestaValida();
        richiesta.getOrariApertura().set(0, "25:00"); // Formato orario inesistente

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.creaSalaStudio(richiesta);
        });
        assertTrue(exception.getMessage().contains("Formato orario non valido (atteso HH:mm)"));
    }

    @Test
    @DisplayName("TC10: Apertura >= Chiusura")
    void creaSalaStudio_AperturaMaggioreChiusura_LanciaEccezione() {
        CreazioneSalaDTO richiesta = creaRichiestaValida();
        richiesta.getOrariApertura().set(0, "18:30"); // Inverte apertura e chiusura
        richiesta.getOrariChiusura().set(0, "08:30");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.creaSalaStudio(richiesta);
        });
        assertTrue(exception.getMessage().contains("L'orario di apertura deve precedere la chiusura per il giorno"));
    }

    // ==========================================
    // CASI DI ERRORE - AREE
    // ==========================================

    @Test
    @DisplayName("TC11: Aree aggiunte < 0")
    void creaSalaStudio_AreeNegative_LanciaEccezione() {
        CreazioneSalaDTO richiesta = creaRichiestaValida();
        // Simuliamo l'inserimento di un'area con un numero negativo di postazioni
        richiesta.getPostazioniAree().set(0, -1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.creaSalaStudio(richiesta);
        });

        assertTrue(exception.getMessage().contains("non può essere negativo") ||
                exception.getMessage().contains("almeno una postazione"));
    }

    /*
     * TC12: Fasce orarie fuori orario
     * Nota: Poiché CreazioneSalaDTO usa 'granaMinuti' per generare le fasce automaticamente
     * all'interno degli orari di apertura, non è possibile inviare manualmente fasce "fuori orario".
     * L'eventuale verifica andrebbe fatta sul fatto che granaMinuti non superi l'orario totale.
     */

    /*
     * TC13: Numero Aree non numerico ("Due")
     * TC14: Aree valore decimale (2.5)
     * NON IMPLEMENTABILI IN JAVA: Il DTO usa una List<Integer> e il compilatore blocca
     * le stringhe o i double prima dell'esecuzione.
     */

    // ==========================================
    // CASI DI ERRORE - Numero Sale
    // ==========================================


    @Test
    @DisplayName("TC: Creazione fallisce al superamento del limite di 100 sale attive")
    void creaSalaStudio_LimiteCentosaleSuperato_LanciaEccezione() {
        // Setup: Saturiamo il limite creando esattamente 100 sale
        for (int i = 1; i <= 100; i++) {
            CreazioneSalaDTO dto = new CreazioneSalaDTO();
            dto.setNome("Sala " + i);
            dto.setDescrizione("Descrizione test sala " + i);
            dto.setNumeroPostazioni(10);
            dto.setOrariApertura(List.of("09:00", "09:00", "09:00", "09:00", "09:00"));
            dto.setOrariChiusura(List.of("18:00", "18:00", "18:00", "18:00", "18:00"));
            dto.setGranaMinuti(60);
            dto.setTipologie(List.of("Area Default"));
            dto.setPostazioniAree(List.of(10));

            bibliotecaFacade.creaSalaStudio(dto);
        }

        // Act & Assert: Tentiamo di creare la 101esima sala
        CreazioneSalaDTO dto101 = new CreazioneSalaDTO();
        dto101.setNome("Sala 101");
        dto101.setDescrizione("Descrizione test sala eccedente");
        dto101.setNumeroPostazioni(10);
        dto101.setOrariApertura(List.of("09:00", "09:00", "09:00", "09:00", "09:00"));
        dto101.setOrariChiusura(List.of("18:00", "18:00", "18:00", "18:00", "18:00"));
        dto101.setGranaMinuti(60);
        dto101.setTipologie(List.of("Area Default"));
        dto101.setPostazioniAree(List.of(10));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.creaSalaStudio(dto101);
        });

        assertEquals("Numero massimo di sale raggiunto (100 codici esauriti)", exception.getMessage());
    }
}