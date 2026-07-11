package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.database.JpaUtil;
import it.unina.prenotazioni.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite di Test - Elimina Sala Studio")
class EliminaSalaStudioTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;

    private Long SALA_VALIDA;
    private List<Long> PRENOTAZIONI_ATTIVE_IDS;

    @BeforeEach
    void setUp() {
        bibliotecaFacade = BibliotecaFacade.getInstance();
        em = JpaUtil.getInstance().getEntityManager();
        PRENOTAZIONI_ATTIVE_IDS = new ArrayList<>();

        preparaDatabase();
    }

    private void preparaDatabase() {
        EntityTransaction tx = em.getTransaction();
        try {
            if (!tx.isActive()) tx.begin();

            // 1. Pulizia DB (SQL Nativo per Join Tables + JPQL per Entity)
            em.createNativeQuery("DELETE FROM sala_orari_lavorativi").executeUpdate();
            em.createNativeQuery("DELETE FROM sala_slot_prenotabili").executeUpdate();
            em.createQuery("DELETE FROM Prenotazione").executeUpdate();
            em.createQuery("DELETE FROM Postazione").executeUpdate();
            em.createQuery("DELETE FROM Area").executeUpdate();
            em.createQuery("DELETE FROM FasciaOraria").executeUpdate();
            em.createQuery("DELETE FROM SalaStudio").executeUpdate();
            em.createQuery("DELETE FROM Utente").executeUpdate();

            // 2. Creazione Fascia Oraria di test
            FasciaOraria fascia = new FasciaOraria(LocalTime.of(9, 0), LocalTime.of(11, 0));
            em.persist(fascia);

            // 3. Creazione Sala Studio con 30 postazioni divise in 2 aree (Pre-condizione TC1)
            SalaStudio sala = new SalaStudio("Sala da Eliminare", "Test Eliminazione", 30);

            Area area1 = sala.aggiungiArea("Area 1", 15);
            Area area2 = sala.aggiungiArea("Area 2", 15);
            em.persist(sala);
            this.SALA_VALIDA = sala.getId();

            // 4. Creazione di 5 Studenti e 5 Prenotazioni ATTIVE (Pre-condizione TC1)
            LocalDate dataPrenotazione = LocalDate.now().plusDays(2); // Data futura

            for (int i = 0; i < 5; i++) {
                // Creiamo uno studente univoco
                Studente s = new Studente();
                s.setNome("Studente" + i);
                s.setCognome("Test");
                s.setEmailIstituzionale("test" + i + "@studenti.unina.it");
                s.setPassword("Pass123!");
                s.setMatricola("MAT" + i);
                em.persist(s);

                // Creiamo una prenotazione in stato ATTIVA associata all'Area 1
                Prenotazione p = new Prenotazione();
                p.setStudente(s);
                p.setPostazione(area1.getPostazioni().get(i));
                p.setFasciaOraria(fascia);
                p.setData(dataPrenotazione);
                p.setStato(it.unina.prenotazioni.entity.state.StatoAttiva.getInstance());

                em.persist(p);
                PRENOTAZIONI_ATTIVE_IDS.add(p.getId());
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Errore durante la preparazione del DB di test: " + e.getMessage());
        }
    }

    // ==========================================
    // CASO DI SUCCESSO
    // ==========================================

    @Test
    @DisplayName("TC1: Eliminazione valida (Soft delete, reset codice e annullamento prenotazioni)")
    void eliminaSalaStudio_Valida_Successo() {
        // Act
        bibliotecaFacade.eliminaSalaStudio(SALA_VALIDA);

        em.clear();

        // Assert 1: La sala deve esistere ancora (soft delete), ma con attiva = false
        SalaStudio salaEliminata = em.find(SalaStudio.class, SALA_VALIDA);
        assertNotNull(salaEliminata, "La sala non deve essere distrutta fisicamente dal DB (Soft Delete)");
        assertFalse(salaEliminata.isAttiva(), "Il flag 'attiva' della sala deve essere false");

        // Assert 2: Verifichiamo che tutte e 5 le prenotazioni siano passate allo stato ANNULLATA
        for (Long idPrenotazione : PRENOTAZIONI_ATTIVE_IDS) {
            Prenotazione p = em.find(Prenotazione.class, idPrenotazione);
            assertEquals("ANNULLATA", p.getStato().getStatoEnum().name(),
                    "La prenotazione occupante deve essere stata annullata");
        }
    }

    // ==========================================
    // CASI DI ERRORE
    // ==========================================

    @Test
    @DisplayName("TC2: ID vuoto/null")
    void eliminaSalaStudio_IdNull_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.eliminaSalaStudio(null);
        });
        assertEquals("L'ID della sala è obbligatorio.", exception.getMessage());
    }

    @Test
    @DisplayName("TC3: ID fuori range (inferiore)")
    void eliminaSalaStudio_IdInferioreZero_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.eliminaSalaStudio(0L); // 0 o valori negativi
        });
        assertEquals("ID Sala non valido. L'ID deve essere maggiore di zero.", exception.getMessage());
    }

    /*
    * TC4: ID fuori range (superiore, > 100)
    * NON IMPLEMENTABILE sull'id del database: lo genera Hibernate (auto-increment)
    * e può legittimamente superare 100.
    */

    @Test
    @DisplayName("TC5: Sala inesistente")
    void eliminaSalaStudio_SalaInesistente_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.eliminaSalaStudio(88L);
        });
        assertEquals("Sala studio non trovata", exception.getMessage());
    }

    /*
     * TC6: Tipo non valido (alfanumerico)
     * NON IMPLEMENTABILE IN JAVA: Il Facade richiede un Long (idSalaStudio).
     * Passare la stringa "dodici" causa un errore di compilazione. Il controllo
     * viene fatto dal framework di presentazione (Spring) durante il binding dei parametri.
     */
}