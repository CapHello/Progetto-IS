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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite di Test - Annulla Prenotazione")
class AnnullaPrenotazioneTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;

    // ID delle prenotazioni pre-configurate nel DB
    private Long ID_STUDENTE;
    private Long ID_PREN_ATTIVA_VALIDA;
    private Long ID_PREN_ALTRO_UTENTE;
    private Long ID_PREN_ANNULLATA;
    private Long ID_PREN_SCADUTA;
    private Long ID_PREN_CONCLUSA;
    private Long ID_PREN_CONFERMATA;
    private Long ID_PREN_MENO_DI_6_ORE;

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

            // 2. Setup base: Sala, Area, Postazione, Studente
            SalaStudio sala = new SalaStudio("Sala Newton", "Test", 10);
            Area area = sala.aggiungiArea("Comune", 10);
            em.persist(sala);
            Postazione postazione = area.getPostazioni().get(0);

            Studente studente = new Studente();
            studente.setNome("Mario");
            studente.setCognome("Rossi");
            studente.setEmailIstituzionale("m.rossi@studenti.unina.it");
            studente.setPassword("Pass123!");
            studente.setMatricola("N86001234");
            em.persist(studente);
            ID_STUDENTE = studente.getId();

            // Secondo studente per il TC3 (prenotazione di un altro utente)
            Studente altroStudente = new Studente();
            altroStudente.setNome("Luigi");
            altroStudente.setCognome("Bianchi");
            altroStudente.setEmailIstituzionale("l.bianchi@studenti.unina.it");
            altroStudente.setPassword("Pass123!");
            altroStudente.setMatricola("N86005678");
            em.persist(altroStudente);

            // 3. Setup Fasce Orarie e Date per la simulazione temporale
            // Fascia sicura: tra 10 giorni
            LocalDate dataFutura = LocalDate.now(ZoneId.of("Europe/Rome")).plusDays(10);
            FasciaOraria fasciaFutura = new FasciaOraria(LocalTime.of(9, 0), LocalTime.of(11, 0));
            em.persist(fasciaFutura);

            // Fascia critica: inizio tra esattamente 2 ore da adesso (Scatta il vincolo < 6h)
            LocalDateTime traDueOre = LocalDateTime.now(ZoneId.of("Europe/Rome")).plusHours(2);
            LocalDate dataOggi = traDueOre.toLocalDate();
            FasciaOraria fasciaVicino = new FasciaOraria(traDueOre.toLocalTime(), traDueOre.toLocalTime().plusHours(2));
            em.persist(fasciaVicino);

            // 4. Creazione delle prenotazioni per coprire tutti i test case del Pattern State
            ID_PREN_ATTIVA_VALIDA = creaPrenotazione(studente, postazione, fasciaFutura, dataFutura, StatoAttiva.getInstance());
            ID_PREN_ALTRO_UTENTE = creaPrenotazione(altroStudente, postazione, fasciaFutura, dataFutura, StatoAttiva.getInstance());
            ID_PREN_ANNULLATA = creaPrenotazione(studente, postazione, fasciaFutura, dataFutura, StatoAnnullata.getInstance());
            ID_PREN_SCADUTA = creaPrenotazione(studente, postazione, fasciaFutura, dataFutura, StatoScaduta.getInstance());
            ID_PREN_CONCLUSA = creaPrenotazione(studente, postazione, fasciaFutura, dataFutura, StatoConclusa.getInstance());
            ID_PREN_CONFERMATA = creaPrenotazione(studente, postazione, fasciaFutura, dataFutura, StatoConfermata.getInstance());

            // Prenotazione per il TC8 (Meno di 6 ore)
            ID_PREN_MENO_DI_6_ORE = creaPrenotazione(studente, postazione, fasciaVicino, dataOggi, StatoAttiva.getInstance());

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
    // CASI DI TEST
    // ==========================================

    @Test
    @DisplayName("TC1: Annullamento valido (Stato attiva, Anticipo >= 6h)")
    void annullaPrenotazione_Valida_Successo() {
        bibliotecaFacade.annullaPrenotazione(ID_PREN_ATTIVA_VALIDA, ID_STUDENTE);

        em.clear(); // Svuotiamo la cache per forzare la rilettura
        Prenotazione p = em.find(Prenotazione.class, ID_PREN_ATTIVA_VALIDA);

        assertEquals("ANNULLATA", p.getStato().getStatoEnum().name());
    }

    @Test
    @DisplayName("TC2: Id di prenotazione inesistente")
    void annullaPrenotazione_IdInesistente_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.annullaPrenotazione(99999L, ID_STUDENTE);
        });
        assertTrue(exception.getMessage().contains("Prenotazione non trovata") || exception instanceof NullPointerException);
    }

    @Test
    @DisplayName("TC3: Id di prenotazione di un altro utente")
    void annullaPrenotazione_AltroUtente_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.annullaPrenotazione(ID_PREN_ALTRO_UTENTE, ID_STUDENTE);
        });
        assertEquals("Accesso non consentito alla prenotazione.", exception.getMessage());
    }

    @Test
    @DisplayName("TC4: Prenotazione già annullata")
    void annullaPrenotazione_GiaAnnullata_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.annullaPrenotazione(ID_PREN_ANNULLATA, ID_STUDENTE);
        });
        assertEquals("La prenotazione non può essere annullata nello stato attuale: ANNULLATA", exception.getMessage());
    }

    @Test
    @DisplayName("TC5: Prenotazione già scaduta")
    void annullaPrenotazione_GiaScaduta_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.annullaPrenotazione(ID_PREN_SCADUTA, ID_STUDENTE);
        });
        assertEquals("La prenotazione non può essere annullata nello stato attuale: SCADUTA", exception.getMessage());
    }

    @Test
    @DisplayName("TC6: Prenotazione conclusa")
    void annullaPrenotazione_Conclusa_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.annullaPrenotazione(ID_PREN_CONCLUSA, ID_STUDENTE);
        });
        assertEquals("La prenotazione non può essere annullata nello stato attuale: CONCLUSA", exception.getMessage());
    }

    @Test
    @DisplayName("TC7: Check-in già effettuato (Stato Confermata)")
    void annullaPrenotazione_GiaConfermata_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.annullaPrenotazione(ID_PREN_CONFERMATA, ID_STUDENTE);
        });
        assertEquals("La prenotazione non può essere annullata nello stato attuale: CONFERMATA", exception.getMessage());
    }

    @Test
    @DisplayName("TC8: Anticipo inferiore a 6 ore (Vincolo V07)")
    void annullaPrenotazione_AnticipoInferiore6Ore_LanciaEccezione() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bibliotecaFacade.annullaPrenotazione(ID_PREN_MENO_DI_6_ORE, ID_STUDENTE);
        });
        assertEquals("Annullamento non consentito: mancano meno di 6 ore all'inizio della fascia oraria", exception.getMessage());
    }
}