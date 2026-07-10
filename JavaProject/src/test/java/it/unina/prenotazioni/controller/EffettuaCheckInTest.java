package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.database.JpaUtil;
import it.unina.prenotazioni.entity.*;
import it.unina.prenotazioni.entity.state.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite di Test - Effettua Check-in")
class EffettuaCheckInTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;

    private Long ID_PREN_VALIDA;
    private Long ID_PREN_ANNULLATA;
    private Long ID_PREN_SCADUTA;
    private Long ID_PREN_CONCLUSA;
    private Long ID_PREN_CONFERMATA;
    private Long ID_PREN_ALTRA_DATA;
    private Long ID_PREN_RITARDO_TOLLERANZA;

    @BeforeEach
    void setUp() {
        bibliotecaFacade = BibliotecaFacade.getInstance();
        em = JpaUtil.getInstance().getEntityManager();
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

            // 2. Setup Base
            SalaStudio sala = new SalaStudio("Sala Maxwell", "Test Check-in", 10);
            Area area = sala.aggiungiArea("Comune", 10);
            em.persist(sala);
            Postazione postazione = area.getPostazioni().get(0);

            Studente studente = new Studente();
            studente.setNome("Antonio");
            studente.setCognome("Caprio");
            studente.setEmailIstituzionale("a.caprio@studenti.unina.it");
            studente.setPassword("Pass123!");
            studente.setMatricola("N86001234");
            em.persist(studente);

            // 3. Setup Tempistiche Dinamiche
            LocalTime oraAttuale = LocalTime.now();
            LocalDate dataOggi = LocalDate.now();

            // Fascia valida: iniziata da 2 minuti (rientra nei 10 min di tolleranza V08)
            FasciaOraria fasciaValida = new FasciaOraria(oraAttuale.minusMinutes(2), oraAttuale.plusHours(2));
            em.persist(fasciaValida);

            // Fascia ritardo: iniziata da 15 minuti (fuori tolleranza V08)
            FasciaOraria fasciaRitardo = new FasciaOraria(oraAttuale.minusMinutes(15), oraAttuale.plusHours(2));
            em.persist(fasciaRitardo);

            LocalDate dataFutura = dataOggi.plusDays(1); // Per TC8

            // 4. Creazione Prenotazioni
            ID_PREN_VALIDA = creaPrenotazione(studente, postazione, fasciaValida, dataOggi, StatoAttiva.getInstance());
            ID_PREN_ALTRA_DATA = creaPrenotazione(studente, postazione, fasciaValida, dataFutura, StatoAttiva.getInstance());
            ID_PREN_RITARDO_TOLLERANZA = creaPrenotazione(studente, postazione, fasciaRitardo, dataOggi, StatoAttiva.getInstance());

            // Prenotazioni con stati non validi (per TC4, TC5, TC6, TC7)
            ID_PREN_ANNULLATA = creaPrenotazione(studente, postazione, fasciaValida, dataOggi, StatoAnnullata.getInstance());
            ID_PREN_SCADUTA = creaPrenotazione(studente, postazione, fasciaValida, dataOggi, StatoScaduta.getInstance());
            ID_PREN_CONCLUSA = creaPrenotazione(studente, postazione, fasciaValida, dataOggi, StatoConclusa.getInstance());
            ID_PREN_CONFERMATA = creaPrenotazione(studente, postazione, fasciaValida, dataOggi, StatoConfermata.getInstance());

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Errore durante il setup: " + e.getMessage());
        }
    }

    private Long creaPrenotazione(Studente studente, Postazione postazione, FasciaOraria fascia, LocalDate data, StatoPrenotazione stato) {
        Prenotazione p = new Prenotazione();
        p.setStudente(studente);
        p.setPostazione(postazione);
        p.setFasciaOraria(fascia);
        p.setData(data);
        p.setStato(stato);
        em.persist(p);
        return p.getId();
    }

    // ==========================================
    // CASI DI SUCCESSO
    // ==========================================

    @Test
    @DisplayName("TC1: Check-in valido (Stato attiva, Giorno corrente, entro 10 min)")
    void effettuaCheckin_Valido_Successo() {
        bibliotecaFacade.effettuaCheckin(ID_PREN_VALIDA);

        em.clear(); // Pulisco la cache per rileggere l'entità dal DB
        Prenotazione p = em.find(Prenotazione.class, ID_PREN_VALIDA);

        assertEquals("CONFERMATA", p.getStato().getStatoEnum().name(), "La prenotazione deve passare allo stato CONFERMATA");
    }

    // ==========================================
    // CASI DI ERRORE
    // ==========================================

    @Test
    @DisplayName("TC2: Id di prenotazione inesistente")
    void effettuaCheckin_IdInesistente_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.effettuaCheckin(99999L);
        });
        assertTrue(exception.getMessage().contains("Prenotazione non trovata") || exception instanceof NullPointerException);
    }

    /*
     * TC3: Id di prenotazione di un altro utente
     * NON IMPLEMENTABILE SUL FACADE: Come per l'annullamento, l'Id utente non è nella
     * firma del metodo. Il controllo va effettuato al livello del controller REST/Security.
     */

    @Test
    @DisplayName("TC4: Prenotazione già annullata")
    void effettuaCheckin_GiaAnnullata_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.effettuaCheckin(ID_PREN_ANNULLATA);
        });
        assertTrue(exception.getMessage().contains("La prenotazione non è in stato ATTIVA"));
    }

    @Test
    @DisplayName("TC5: Prenotazione già scaduta (o annullata in background)")
    void effettuaCheckin_GiaScaduta_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.effettuaCheckin(ID_PREN_SCADUTA);
        });
        assertTrue(exception.getMessage().contains("La prenotazione non è in stato ATTIVA"));
    }

    @Test
    @DisplayName("TC6: Prenotazione conclusa")
    void effettuaCheckin_Conclusa_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.effettuaCheckin(ID_PREN_CONCLUSA);
        });
        assertTrue(exception.getMessage().contains("La prenotazione non è in stato ATTIVA"));
    }

    @Test
    @DisplayName("TC7: Check-in già effettuato (Stato confermata)")
    void effettuaCheckin_GiaConfermata_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.effettuaCheckin(ID_PREN_CONFERMATA);
        });
        assertTrue(exception.getMessage().contains("La prenotazione non è in stato ATTIVA"));
    }

    @Test
    @DisplayName("TC8: Giorno diverso da quello corrente")
    void effettuaCheckin_GiornoDiverso_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.effettuaCheckin(ID_PREN_ALTRA_DATA);
        });
        assertTrue(exception.getMessage().contains("Il check-in è consentito solo nel giorno della prenotazione"));
    }

}