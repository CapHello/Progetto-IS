package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.database.JpaUtil;
import it.unina.prenotazioni.dto.SalaStudioDTO;
import it.unina.prenotazioni.entity.FasciaOraria;
import it.unina.prenotazioni.entity.SalaStudio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite di Test - Consultazione Sale Disponibili")
class ConsultazioneSaleDisponibiliTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;

    // Date dinamiche per evitare che i test scadano
    private LocalDate DATA_VALIDA;
    private LocalDate DATA_PASSATA;
    private LocalDate DATA_CHIUSURA;

    @BeforeEach
    void setUp() {
        bibliotecaFacade = BibliotecaFacade.getInstance();
        em = JpaUtil.getInstance().getEntityManager();

        // Calcolo date dinamiche
        DATA_VALIDA = LocalDate.now(ZoneId.of("Europe/Rome")).with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        DATA_PASSATA = LocalDate.now(ZoneId.of("Europe/Rome")).minusDays(1);
        DATA_CHIUSURA = LocalDate.now(ZoneId.of("Europe/Rome")).with(TemporalAdjusters.next(DayOfWeek.SATURDAY));

        preparaDatabase();
    }

    private void preparaDatabase() {
        EntityTransaction tx = em.getTransaction();
        try {
            if (!tx.isActive()) tx.begin();

            // 1. Pulizia DB
            em.createNativeQuery("DELETE FROM sala_orari_lavorativi").executeUpdate();
            em.createNativeQuery("DELETE FROM sala_slot_prenotabili").executeUpdate();
            em.createQuery("DELETE FROM Prenotazione").executeUpdate();
            em.createQuery("DELETE FROM Postazione").executeUpdate();
            em.createQuery("DELETE FROM Area").executeUpdate();
            em.createQuery("DELETE FROM FasciaOraria").executeUpdate();
            em.createQuery("DELETE FROM SalaStudio").executeUpdate();
            em.createQuery("DELETE FROM Utente").executeUpdate();

            // 2. Setup Fascia Oraria valida
            FasciaOraria fascia = new FasciaOraria(LocalTime.of(9, 0), LocalTime.of(11, 0));
            em.persist(fascia);

            // 3. Setup Sala valida e prenotabile.
            // Come in produzione (creaSalaStudio), ogni giorno ha una FasciaOraria distinta:
            // Hibernate 6 deduplica i risultati JPQL per identità, quindi riusare la stessa
            // istanza 5 volte farebbe risultare un solo orario lavorativo invece di cinque.
            SalaStudio sala = new SalaStudio("Sala Newton", "Disponibile", 1);
            sala.aggiungiArea("Comune", 1);
            sala.addFascia(fascia); // Aggiunta agli slot prenotabili
            for (int i = 0; i < 5; i++) sala.addOrarioLavorativo(new FasciaOraria(LocalTime.of(9, 0), LocalTime.of(11, 0)));
            em.persist(sala);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Errore durante il setup: " + e.getMessage());
        }
    }

    // ==========================================
    // CASI DI SUCCESSO E DI ERRORE
    // ==========================================

    @Test
    @DisplayName("TC1: Disponibilità completa (tutto valido)")
    void consultaSaleDisponibili_Valido_RitornaElenco() {
        List<SalaStudioDTO> sale = bibliotecaFacade.consultaSaleDisponibili(DATA_VALIDA);

        assertNotNull(sale);
        assertFalse(sale.isEmpty(), "L'elenco delle sale disponibili non deve essere vuoto");
        // Verifica che la Sala Newton sia presente
        assertTrue(sale.stream().anyMatch(s -> s.getNome().equals("Sala Newton")));
    }

    @Test
    @DisplayName("TC2: Data nel passato")
    void consultaSaleDisponibili_DataPassata_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.consultaSaleDisponibili(DATA_PASSATA);
        });
        assertEquals("Data non valida: non è possibile consultare date passate.", exception.getMessage());
    }

    /*
     * TC3: Data in formato non valido ("30-13-2026")
     * NON IMPLEMENTABILE SUL FACADE: Il metodo riceve un oggetto java.time.LocalDate.
     * Il formato String non valido ("30-13-2026") solleverà una DateTimeParseException
     * a livello di Controller/Spring Boot prima ancora di invocare la logica di business.
     */

    @Test
    @DisplayName("TC4: Data in giorno di chiusura (Sabato/Domenica)")
    void consultaSaleDisponibili_GiornoChiusura_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.consultaSaleDisponibili(DATA_CHIUSURA);
        });
        assertEquals("La biblioteca è chiusa nel giorno selezionato (apertura lun-ven).", exception.getMessage());
    }
}