package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.controller.factory.StudenteFactory;
import it.unina.prenotazioni.database.JpaUtil;
import it.unina.prenotazioni.dto.*;
import it.unina.prenotazioni.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

// NOTE: i test case in cui si richiedeva che il codice della postazione, area e sala studio
// dovesse trovarsi all'interno di un intervallo specificato in fase di analisi non è necessario implementarli
// mi è basta fare il test sull'id univoco di hibernate che deve esser necessariamente > 0 tranne nel caso di postazione che
// assume volutamente il valore 0 per indicare la strategia di assegnazione.




@DisplayName("Suite di Test - Effettua Prenotazione (Parte 1)")
class EffettuaPrenotazioneTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;

    private Long SALA_VALIDA;
    private Long AREA_VALIDA;
    private Long AREA_COMUNE;
    private Long POSTAZIONE_VALIDA;
    private Long ID_STUDENTE;
    private Long FASCIA_09_30;

    private final Long ASSEGNAZIONE_AUTOMATICA = 0L;
    private final LocalDate DATA_VALIDA = LocalDate.of(2026, 8, 27);

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

            em.createNativeQuery("DELETE FROM sala_orari_lavorativi").executeUpdate();
            em.createNativeQuery("DELETE FROM sala_slot_prenotabili").executeUpdate();

            em.createQuery("DELETE FROM Prenotazione").executeUpdate();
            em.createQuery("DELETE FROM Postazione").executeUpdate();
            em.createQuery("DELETE FROM Area").executeUpdate();
            em.createQuery("DELETE FROM FasciaOraria").executeUpdate();
            em.createQuery("DELETE FROM SalaStudio").executeUpdate();
            em.createQuery("DELETE FROM Utente").executeUpdate();

            FasciaOraria fascia = new FasciaOraria(LocalTime.of(9, 30), LocalTime.of(10, 30));
            em.persist(fascia);
            this.FASCIA_09_30 = fascia.getId();

            SalaStudio sala = new SalaStudio("Sala Prenotazioni", "Sala per testare le prenotazioni", 20);

            sala.addFascia(fascia);

            for (int i = 0; i < 5; i++) {
                sala.addOrarioLavorativo(fascia);
            }

            // CREAZIONE DELLE DUE AREE
            Area areaSpecifica = sala.aggiungiArea("Specifica", 10);
            Area areaComune = sala.aggiungiArea("comune", 10);

            em.persist(sala);

            this.SALA_VALIDA = sala.getId();

            this.AREA_VALIDA = areaSpecifica.getId();
            this.AREA_COMUNE = areaComune.getId();

            this.POSTAZIONE_VALIDA = areaSpecifica.getPostazioni().get(0).getId();

            Studente studente = new Studente();
            studente.setNome("Antonio");
            studente.setCognome("Caprio");
            studente.setEmailIstituzionale("a.caprio@studenti.unina.it");
            studente.setPassword("PasswordSicura123!");
            studente.setMatricola("N86001234");
            studente.setNumeroTotaleAccessi(0);

            em.persist(studente);
            this.ID_STUDENTE = studente.getId();

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Errore durante la preparazione del DB di test: " + e.getMessage(), e);
        }
    }

    @Test
    @DisplayName("TC1: Tutti validi - area specifica, postazione specifica")
    void effettuaPrenotazione_TuttiValidi_Successo() {
        PrenotazioneDTO risultato = bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
        ));

        assertNotNull(risultato);
        assertEquals("ATTIVA", risultato.getStato());
        assertEquals(POSTAZIONE_VALIDA, risultato.getIdPostazione());
    }

    @Test
    @DisplayName("TC2: Tutti validi - area specifica, assegnazione automatica")
    void effettuaPrenotazione_AreaSpecificaAuto_Successo() {
        PrenotazioneDTO risultato = bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                SALA_VALIDA, AREA_VALIDA, ASSEGNAZIONE_AUTOMATICA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
        ));

        assertNotNull(risultato);
        assertEquals("ATTIVA", risultato.getStato());
        assertNotNull(risultato.getIdPostazione(), "Il sistema deve aver assegnato una postazione");
        assertTrue(risultato.getIdPostazione() > 0);
    }

    @Test
    @DisplayName("TC3: Tutti validi - area comune, postazione specifica")
    void effettuaPrenotazione_AreaComuneSpecifica_Successo() {
        Long idPostazioneAreaComune = em.find(Area.class, AREA_COMUNE).getPostazioni().get(0).getId();

        PrenotazioneDTO risultato = bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                SALA_VALIDA, AREA_COMUNE, idPostazioneAreaComune, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
        ));

        assertNotNull(risultato);
        assertEquals("ATTIVA", risultato.getStato());
        assertEquals(idPostazioneAreaComune, risultato.getIdPostazione());
    }

    @Test
    @DisplayName("TC4: Tutti validi - area comune, assegnazione automatica")
    void effettuaPrenotazione_AreaComuneAuto_Successo() {
        PrenotazioneDTO risultato = bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                SALA_VALIDA, AREA_COMUNE, ASSEGNAZIONE_AUTOMATICA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
        ));

        assertNotNull(risultato);
        assertEquals("ATTIVA", risultato.getStato());
        assertNotNull(risultato.getIdPostazione(), "Il sistema deve aver assegnato una postazione");
        assertTrue(risultato.getIdPostazione() > 0);
    }

    // ==========================================
    // CASI DI ERRORE - SALA (idSala)
    // ==========================================

    @Test
    @DisplayName("TC5: idSala mancante (null)")
    void effettuaPrenotazione_SalaNull_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    null, AREA_VALIDA, POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("La selezione della sala studio è obbligatoria", exception.getMessage());
    }

    /*
     * TC6: idSala con tipo errato ("5A")
     * NON IMPLEMENTABILE: Il compilatore Java impedisce di passare una stringa "5A"
     * a un parametro tipizzato come Long (idSala). L'errore viene intercettato
     * in fase di compilazione o deserializzazione dal Controller.
     */

    @Test
    @DisplayName("TC7: idSala fuori dall'intervallo")
    void effettuaPrenotazione_SalaFuoriRange_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    0L, AREA_VALIDA, POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("Identificativo sala non valido", exception.getMessage());
    }

    @Test
    @DisplayName("TC8: idSala non esistente")
    void effettuaPrenotazione_SalaNonEsistente_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    77L, AREA_VALIDA, POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("Sala studio non trovata.", exception.getMessage());
    }

    // ==========================================
    // CASI DI ERRORE - DATA
    // ==========================================

    @Test
    @DisplayName("TC9: Data mancante (null)")
    void effettuaPrenotazione_DataNull_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, null, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("La data è obbligatoria", exception.getMessage());
    }

    @Test
    @DisplayName("TC10: Data nel passato")
    void effettuaPrenotazione_DataPassata_LanciaEccezione() {
        LocalDate dataPassata = LocalDate.of(2024, 1, 10);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, dataPassata, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("Non è possibile prenotare una fascia oraria già iniziata o passata", exception.getMessage());
    }

    /*
     * TC11: Formato data non valido ("32/13/2026")
     * NON IMPLEMENTABILE: La classe java.time.LocalDate solleva un'eccezione DateTimeParseException
     * al momento della sua creazione, molto prima che il dato raggiunga il Facade.
     */

    @Test
    @DisplayName("TC12: Data in giorno di chiusura (Sabato/Domenica)")
    void effettuaPrenotazione_DataChiusura_LanciaEccezione() {
        LocalDate sabato = LocalDate.of(2026, 7, 4); // 4 Luglio 2026 è sabato
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, sabato, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("La sala è chiusa nella data selezionata (giorni feriali)", exception.getMessage());
    }

    // ==========================================
    // CASI DI ERRORE - FASCIA ORARIA
    // ==========================================

    @Test
    @DisplayName("TC13: Fascia oraria mancante (null)")
    void effettuaPrenotazione_FasciaNull_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, DATA_VALIDA, null, ID_STUDENTE
            ));
        });
        assertEquals("La fascia oraria è obbligatoria", exception.getMessage());
    }

    /*
     * TC14: Formato fascia oraria non valido ("25:00-30:00")
     * NON IMPLEMENTABILE: Il Facade riceve un Long idFascia, bypassando il problema del formato.
     */

    @Test
    @DisplayName("TC15: Fascia non prevista dalla sala")
    void effettuaPrenotazione_FasciaNonPrevista_LanciaEccezione() {
        Long idFasciaNonPrevista = 99L; // id di una fascia che non appartiene alla sala
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, DATA_VALIDA, idFasciaNonPrevista, ID_STUDENTE
            ));
        });
        assertEquals("La fascia oraria selezionata non è disponibile", exception.getMessage());
    }
    // MOTIVO DEL TEST COMMENTATO: questo test non è deterministico a meno che non modifichi il clock. Il motivo sta che
    // in risolvi salaStudio verifico prima che il giorno corrente sia feriale o meno, in caso di Sabato o Domenica viene sollevata
    // la stessa eccezione di TC12.
    // Il test passa correttamente se il giorno corrente è feriale.
    //
    // Per modificare il clock dovrei aggiungere attributo privato Clock clock e il setter tramite cui modificarlo.
    
//    @Test
//    @DisplayName("TC16: Fascia oraria già trascorsa nella giornata corrente")
//    void effettuaPrenotazione_FasciaTrascorsaOggi_LanciaEccezione() {
//        LocalDate dataOdierna = LocalDate.now();
//        Long idFasciaPassata = 0L; // id fittizio: il TC richiede una fascia già trascorsa oggi
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
//            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
//                    SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, dataOdierna, idFasciaPassata, ID_STUDENTE
//            ));
//        });
//        assertEquals("La fascia oraria selezionata è già trascorsa nella giornata corrente", exception.getMessage());
//    }

    // ==========================================
    // CASI DI ERRORE - AREA
    // ==========================================

    /*
     * TC17: IdArea con tipo errato ("2A")
     * NON IMPLEMENTABILE: Java non ammette il passaggio di una String a un Long.
     */

    @Test
    @DisplayName("TC18: IdArea < 1")
    void effettuaPrenotazione_AreaFuoriRange_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, 0L, POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("Identificativo area non valido.", exception.getMessage());
    }

    @Test
    @DisplayName("TC19: idArea non esistente nella sala selezionata")
    void effettuaPrenotazione_AreaNonEsistente_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, 999L, POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("L'area non è presente all'interno della sala selezionata", exception.getMessage());
    }


    @Test
    @DisplayName("TC20: Area selezionata satura per la fascia oraria")
    void effettuaPrenotazione_AreaSaturaFascia_LanciaEccezione() {
        // Pre-condizione: Riempiamo tutti i posti dell'area per quella fascia
        saturaArea(AREA_VALIDA, DATA_VALIDA, FASCIA_09_30);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, ASSEGNAZIONE_AUTOMATICA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("Non sono presenti delle postazioni disponibili per l'area selezionata", exception.getMessage());
    }

    @Test
    @DisplayName("TC21: Area selezionata satura per la data (tutte le fasce)")
    void effettuaPrenotazione_AreaSaturaData_LanciaEccezione() {
        saturaArea(AREA_VALIDA, DATA_VALIDA, FASCIA_09_30);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, ASSEGNAZIONE_AUTOMATICA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("Non sono presenti delle postazioni disponibili per l'area selezionata",exception.getMessage());
    }

    /*
     * TC22: idPostazione con tipo errato ("abc")
     * NON IMPLEMENTABILE: Come per l'idSala, Java non permette di passare una String ("abc")
     * a un parametro tipizzato come Long (idPostazione).
     */

    @Test
    @DisplayName("TC23: idPostazione fuori dall'intervallo dell'area (V20)")
    void effettuaPrenotazione_PostazioneFuoriRange_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {

            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, -1L, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("Identificativo postazione non valido.", exception.getMessage());
    }

    @Test
    @DisplayName("TC24: Postazione specifica non disponibile al momento della selezione")
    void effettuaPrenotazione_PostazioneSpecificaOccupata_LanciaEccezione() {
        // Pre-condizione: Un altro studente occupa la postazione desiderata
        occupaPostazione(POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("La postazione selezionata non è più disponibile", exception.getMessage());
    }

// ==========================================
// CASI DI ERRORE - VINCOLI DI DOMINIO (V09, V18)
// ==========================================

    @Test
    @DisplayName("TC25: Studente con prenotazione già esistente nella stessa data e fascia (V18)")
    void effettuaPrenotazione_DoppiaPrenotazioneStudente_LanciaEccezione() {
        // 1. Lo studente effettua una prenotazione regolare
        bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
        ));

        // 2. Tenta di prenotare un'altra postazione per la STESSA data e fascia
        Long altraPostazione = em.find(Area.class, AREA_VALIDA).getPostazioni().get(1).getId();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, altraPostazione, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        assertEquals("Esiste già una tua prenotazione attiva o confermata in questa data e fascia oraria", exception.getMessage());
    }
    // TODO da rivedere: non penso si faccia in questo modo un test sull'accesso concorrente
    @Test
    @DisplayName("TC26: Postazione non più disponibile (Race Condition V09)")
    void effettuaPrenotazione_RaceCondition_LanciaEccezione() {
        // In un test sincrono (single-thread), la race condition si simula impostando
        // lo stato della postazione su "occupato" un attimo prima della chiamata,
        // fingendo che una transazione concorrente abbia committato millisecondi prima.
        occupaPostazione(POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.effettuaPrenotazione(new RichiestaPrenotazioneDTO(
                    SALA_VALIDA, AREA_VALIDA, POSTAZIONE_VALIDA, DATA_VALIDA, FASCIA_09_30, ID_STUDENTE
            ));
        });
        
        assertTrue(exception.getMessage().contains("La postazione selezionata non è più disponibile"));
    }

    //
    // METODI DI SUPPORTO PER IL SETUP DEL DB
    //

    /**
     * Occupa fisicamente una postazione nel database simulando la prenotazione
     * da parte di uno "studente fantasma".
     */
    private void occupaPostazione(Long idPostazione, LocalDate data, Long idFascia) {
        EntityTransaction tx = em.getTransaction();
        try {
            if (!tx.isActive()) tx.begin();

            // Creiamo un utente fittizio per evitare conflitti (V18) con il nostro ID_STUDENTE
            Studente fantasma = new Studente();
            fantasma.setNome("Studente");
            fantasma.setCognome("Fantasma");
            fantasma.setEmailIstituzionale("ghost" + System.nanoTime() + "@studenti.unina.it");
            fantasma.setMatricola("GHOST" + System.nanoTime()); // Matricola univoca
            fantasma.setPassword("Pass123!");
            em.persist(fantasma);

            Postazione p = em.find(Postazione.class, idPostazione);
            FasciaOraria f = em.find(FasciaOraria.class, idFascia);

            Prenotazione pren = new Prenotazione();
            pren.setData(data);
            pren.setPostazione(p);
            pren.setFasciaOraria(f);
            pren.setStudente(fantasma);
            pren.setStato(it.unina.prenotazioni.entity.state.StatoAttiva.getInstance());

            em.persist(pren);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Errore durante l'occupazione della postazione: " + e.getMessage());
        }
    }

    /**
     * Cicla tutte le postazioni di un'area e le occupa, saturandola.
     */
    private void saturaArea(Long idArea, LocalDate data, Long idFascia) {
        Area area = em.find(Area.class, idArea);
        for (Postazione p : area.getPostazioni()) {
            occupaPostazione(p.getId(), data, idFascia);
        }
    }
}


