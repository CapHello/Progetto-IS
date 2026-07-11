package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.database.JpaUtil;
import it.unina.prenotazioni.entity.*;
import it.unina.prenotazioni.entity.state.StatoAttiva;
import it.unina.prenotazioni.entity.state.StatoConfermata;
import it.unina.prenotazioni.entity.state.StatoPrenotazione;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test strutturali - gestisciTerminePrenotazione")
class GestisciTerminePrenotazioneTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;

    private Long ID_STUDENTE;
    private Long ID_POSTAZIONE;

    private static final LocalTime INIZIO = LocalTime.of(10, 0);
    private static final LocalTime FINE   = LocalTime.of(12, 0);

    @BeforeEach
    void setUp() {
        bibliotecaFacade = BibliotecaFacade.getInstance();
        em = JpaUtil.getInstance().getEntityManager();
        prepareDatabase();
    }

    @AfterEach
    void tearDown() {
        if (em != null && em.isOpen()) em.close();
    }

    private void prepareDatabase() {
        EntityTransaction tx = em.getTransaction();
        try {
            if (!tx.isActive()) tx.begin();

            em.createNativeQuery("DELETE FROM sala_orari_lavorativi").executeUpdate();
            em.createNativeQuery("DELETE FROM sala_slot_prenotabili").executeUpdate();

            em.createQuery("DELETE FROM Prenotazione").executeUpdate();
            em.createQuery("DELETE FROM Postazione").executeUpdate();
            em.createQuery("DELETE FROM Area").executeUpdate();
            em.createQuery("DELETE FROM FasciaOraria").executeUpdate();
            em.createQuery("DELETE FROM SalaStudio").executeUpdate();
            em.createQuery("DELETE FROM Utente").executeUpdate();

            FasciaOraria fasciaSala = new FasciaOraria(INIZIO, FINE);
            em.persist(fasciaSala);

            SalaStudio sala = new SalaStudio("Sala Test Termine", "Sala per i test strutturali", 5);

            sala.addFascia(fasciaSala);
            for (int i = 0; i < 5; i++) {
                sala.addOrarioLavorativo(fasciaSala);
            }
            Area area = sala.aggiungiArea("Specifica", 5);
            em.persist(sala);
            this.ID_POSTAZIONE = area.getPostazioni().getFirst().getId();

            Studente studente = new Studente();
            studente.setNome("Giovanni");
            studente.setCognome("Fragola");
            studente.setEmailIstituzionale("gio.fragola@studenti.unina.it");
            studente.setPassword("PasswordSicura123!");
            studente.setMatricola("N46903411");
            studente.setNumeroTotaleAccessi(0);
            em.persist(studente);
            this.ID_STUDENTE = studente.getId();

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Errore durante la preparazione del database di test", e);
        }
    }

    private Long creaPrenotazione(StatoPrenotazione stato, LocalDate data) {
        EntityTransaction tx = em.getTransaction();
        try{
            if (!tx.isActive()) tx.begin();

            FasciaOraria fascia = new FasciaOraria(INIZIO, FINE);
            em.persist(fascia);

            Prenotazione pren = new Prenotazione();
            pren.setData(data);
            pren.setFasciaOraria(fascia);
            pren.setPostazione(em.find(Postazione.class, ID_POSTAZIONE));
            pren.setStudente(em.find(Studente.class, ID_STUDENTE));
            pren.setStato(stato);              // imposta anche nomeStato, così getPrenotazioniInScadenza la trova

            em.persist(pren);
            tx.commit();
            return pren.getId();
        }catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Errore durante la creazione della prenotazione all'interno del database", e);
        }
    }

    // Rilegge lo stato dal DB: em.clear() evita di pescare dalla cache di primo livello.
    private StatoEnum statoDellaPrenotazione(Long idPrenotazione){
        em.clear();
        return em.find(Prenotazione.class, idPrenotazione).getStato().getStatoEnum();
    }

    @Test
    @DisplayName("TC1 [1-2-3-4-5-8-2-9]: Prenotazione ATTIVA oltre la tolleranza diventa SCADUTA")
    void prenotazioneAttivaOltreTolleranza(){
        // Data di ieri: adesso.isAfter(inizio + 10) = true
        Long id = creaPrenotazione(StatoAttiva.getInstance(), LocalDate.now().minusDays(1));

        bibliotecaFacade.gestisciTerminePrenotazioni();

        assertEquals(StatoEnum.SCADUTA, statoDellaPrenotazione(id), "Una prenotazione ATTIVA oltre la tolleranza di check-in deve diventare SCADUTA");
    }

    @Test
    @DisplayName("TC2 [1-2-3-4-6-7-8-2-9]: Prenotazione CONFERMATA con slot terminato diventa CONCLUSA")
    void prenotazioneConfermataSlotTerminato(){
        Long id = creaPrenotazione(StatoConfermata.getInstance(), LocalDate.now().minusDays(1));

        bibliotecaFacade.gestisciTerminePrenotazioni();

        assertEquals(StatoEnum.CONCLUSA, statoDellaPrenotazione(id), "Una prenotazione CONFERMATA con slot terminato deve diventare CONCLUSA");
    }

    @Test
    @DisplayName("TC3 [1-2-3-4-6-8-2-9]: In scadenza ma nessuna condizione vera, nessuna transizione")
    void prenotazioneInScadenzaNessunaCondizioneVerificata(){
        Long id = creaPrenotazione(StatoAttiva.getInstance(), LocalDate.now().plusDays(1));

        bibliotecaFacade.gestisciTerminePrenotazioni();

        assertEquals(StatoEnum.ATTIVA, statoDellaPrenotazione(id), "Nessuna soglia temporale superata, la prenotazione non ha cambiato stato");
    }

    @Test
    @DisplayName("TC4 [1-2-9]: Nessuna prenotazione in scadenza, corpo del ciclo mai eseguito")
    void nessunaPrenotazioneInScadenza(){
        // prepareDatabase non crea prenotazioni: il metodo deve solo terminare senza errori.
        bibliotecaFacade.gestisciTerminePrenotazioni();
    }
}
