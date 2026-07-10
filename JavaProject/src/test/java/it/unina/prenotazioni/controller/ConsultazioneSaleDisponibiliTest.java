package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.database.JpaUtil;
import it.unina.prenotazioni.dto.*;
import it.unina.prenotazioni.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite di Test - Consultazione Sale Disponibili")
class ConsultazioneSaleDisponibiliTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;

    // Date dinamiche per evitare che i test scadano
    private LocalDate DATA_VALIDA;
    private LocalDate DATA_SATURA;
    private LocalDate DATA_PASSATA;
    private LocalDate DATA_CHIUSURA;

    private Long ID_SALA_VALIDA;
    private Long ID_SALA_SENZA_FASCE;
    private Long ID_FASCIA_VALIDA;

    @BeforeEach
    void setUp() {
        bibliotecaFacade = BibliotecaFacade.getInstance();
        em = JpaUtil.getInstance().getEntityManager();

        // Calcolo date dinamiche
        DATA_VALIDA = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        DATA_SATURA = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
        DATA_PASSATA = LocalDate.now().minusDays(1);
        DATA_CHIUSURA = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));

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
            this.ID_FASCIA_VALIDA = fascia.getId();

            // 3. Setup Sala 1 (Valida e prenotabile). Creiamo 1 sola postazione per saturarla facilmente.
            // Come in produzione (creaSalaStudio), ogni giorno ha una FasciaOraria distinta:
            // Hibernate 6 deduplica i risultati JPQL per identità, quindi riusare la stessa
            // istanza 5 volte farebbe risultare un solo orario lavorativo invece di cinque.
            SalaStudio sala1 = new SalaStudio("Sala Newton", "Disponibile", 1);
            Area area1 = sala1.aggiungiArea("Comune", 1);
            sala1.addFascia(fascia); // Aggiunta agli slot prenotabili
            for (int i = 0; i < 5; i++) sala1.addOrarioLavorativo(new FasciaOraria(LocalTime.of(9, 0), LocalTime.of(11, 0)));
            em.persist(sala1);
            this.ID_SALA_VALIDA = sala1.getId();

            // 4. Setup Sala 2 (Aperta ma senza fasce prenotabili configurate per TC3)
            SalaStudio sala2 = new SalaStudio("Sala Maxwell", "Senza Slot", 10);
            sala2.aggiungiArea("Comune", 10);
            // NON invochiamo sala2.addFascia(fascia);
            for (int i = 0; i < 5; i++) sala2.addOrarioLavorativo(new FasciaOraria(LocalTime.of(9, 0), LocalTime.of(11, 0)));
            em.persist(sala2);
            this.ID_SALA_SENZA_FASCE = sala2.getId();

            // 5. Saturiamo la Sala 1 per la DATA_SATURA (TC2 e TC4)
            Studente studente = new Studente();
            studente.setNome("Studente");
            studente.setCognome("Test");
            studente.setEmailIstituzionale("test.saturazione@studenti.unina.it");
            studente.setMatricola("MAT999");
            studente.setPassword("Pass123!");
            em.persist(studente);

            Prenotazione pren = new Prenotazione();
            pren.setStudente(studente);
            pren.setPostazione(area1.getPostazioni().getFirst()); // Occupiamo l'unica postazione
            pren.setFasciaOraria(fascia);
            pren.setData(DATA_SATURA);
            pren.setStato(it.unina.prenotazioni.entity.state.StatoAttiva.getInstance());
            em.persist(pren);

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
    @DisplayName("TC2: Nessuna sala disponibile per la data (Tutte sature)")
    void consultaSaleDisponibili_SaleSature_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.consultaSaleDisponibili(DATA_SATURA);
        });
        assertEquals("Nessuna Sala Studio disponibile per la data selezionata.", exception.getMessage());
    }

    @Test
    @DisplayName("TC3: Sala senza fasce prenotabili")
    void getFasceDisponibili_SenzaFasce_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            // Chiediamo le fasce per la Sala 2, che ha orari di apertura ma nessuno slot prenotabile
            bibliotecaFacade.getFasceDisponibili(ID_SALA_SENZA_FASCE, DATA_VALIDA);
        });
        assertEquals("Non sono presenti fasce orarie prenotabili per la Sala selezionata.", exception.getMessage());
    }

    @Test
    @DisplayName("TC4: Sala/fascia senza postazioni libere (Area satura)")
    void selezionaDettaglioSala_NessunaPostazione_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            // Andiamo al dettaglio della Sala 1 nella data satura
            bibliotecaFacade.selezionaDettaglioSala(ID_SALA_VALIDA, ID_FASCIA_VALIDA, DATA_SATURA);
        });
        assertEquals("Nessuna Postazione disponibile: selezionare un'altra fascia o Sala.", exception.getMessage());
    }

    @Test
    @DisplayName("TC5: Data nel passato")
    void consultaSaleDisponibili_DataPassata_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.consultaSaleDisponibili(DATA_PASSATA);
        });
        assertEquals("Data non valida: non è possibile consultare date passate.", exception.getMessage());
    }

    /*
     * TC6: Data in formato non valido ("30-13-2026")
     * NON IMPLEMENTABILE SUL FACADE: Il metodo riceve un oggetto java.time.LocalDate.
     * Il formato String non valido ("30-13-2026") solleverà una DateTimeParseException
     * a livello di Controller/Spring Boot prima ancora di invocare la logica di business.
     */

    @Test
    @DisplayName("TC7: Data in giorno di chiusura (Sabato/Domenica)")
    void consultaSaleDisponibili_GiornoChiusura_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.consultaSaleDisponibili(DATA_CHIUSURA);
        });
        assertEquals("La biblioteca è chiusa nel giorno selezionato (apertura lun-ven).", exception.getMessage());
    }
}