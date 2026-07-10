package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.database.JpaUtil;
import it.unina.prenotazioni.dto.UtenteDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite di Test - Autenticazione Utente")
class AutenticazioneTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;

    // Costanti per i test
    private final String EMAIL_STUDENTE = "m.rossi@studenti.unina.it";
    private final String EMAIL_BIBLIOTECARIO = "m.bianchi@unina.it";
    private final String PASS_VALIDA = "Password123!";

    @BeforeEach
    void setUp() {
        bibliotecaFacade = BibliotecaFacade.getInstance();
        em = JpaUtil.getInstance().getEntityManager();

        svuotaDatabase(em);

        bibliotecaFacade.registrazione(
                "Studente", "Mario", "Rossi", EMAIL_STUDENTE, PASS_VALIDA, "N86001234"
        );
        bibliotecaFacade.registrazione(
                "Bibliotecario", "Mario", "Bianchi", EMAIL_BIBLIOTECARIO, PASS_VALIDA, "BIB001"
        );
    }

    @AfterEach
    void tearDown() {
        svuotaDatabase(em);
    }

    private void svuotaDatabase(EntityManager em) {
        EntityTransaction transaction = em.getTransaction();
        try {
            if (!transaction.isActive()) {
                transaction.begin();
            }

            // Le prenotazioni referenziano gli studenti (FK): vanno eliminate prima degli utenti.
            em.createQuery("DELETE FROM Prenotazione").executeUpdate();
            em.createQuery("DELETE FROM Utente").executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Errore durante la pulizia del database di test: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // CASI DI SUCCESSO
    // ==========================================

    @Test
    @DisplayName("TC1: Autenticazione valida Studente")
    void autenticazioneStudente_Successo() {
        UtenteDTO utente = bibliotecaFacade.autenticazione(EMAIL_STUDENTE, PASS_VALIDA);

        assertNotNull(utente, "L'utente non dovrebbe essere nullo");
        assertEquals("Studente", utente.getRuolo());
        assertEquals(EMAIL_STUDENTE, utente.getEmailIstituzionale());
    }

    @Test
    @DisplayName("TC2: Autenticazione valida Bibliotecario")
    void autenticazioneBibliotecario_Successo() {
        UtenteDTO utente = bibliotecaFacade.autenticazione(EMAIL_BIBLIOTECARIO, PASS_VALIDA);

        assertNotNull(utente);
        assertEquals("Bibliotecario", utente.getRuolo());
        assertEquals(EMAIL_BIBLIOTECARIO, utente.getEmailIstituzionale());
    }

    // ==========================================
    // CASI DI ERRORE - PARTIZIONE EMAIL
    // ==========================================

    @Test
    @DisplayName("TC3: Email inesistente")
    void autenticazioneEmailInesistente_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione("utente.fantasma@unina.it", PASS_VALIDA);
        });

        assertEquals("Credenziali non valide o utente inesistente.", exception.getMessage());
    }

    @Test
    @DisplayName("TC4: Email vuota")
    void autenticazioneEmailVuota_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione("", PASS_VALIDA);
        });
        assertEquals("L'email è obbligatoria.", exception.getMessage());
    }

    @Test
    @DisplayName("TC5: Formato email errato")
    void autenticazioneEmailFormatoErrato_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione("m.rossistudenti.unina.it", PASS_VALIDA);
        });
        assertEquals("Formato email non valido.", exception.getMessage());
    }

    @Test
    @DisplayName("TC6: Email > 255 caratteri")
    void autenticazioneEmailTroppoLunga_LanciaEccezione() {
        String emailLunga = "a".repeat(246) + "@unina.it";
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione(emailLunga, PASS_VALIDA);
        });
        assertEquals("Formato email non valido.", exception.getMessage());
    }

    // ==========================================
    // CASI DI ERRORE - PARTIZIONE PASSWORD
    // ==========================================

    @Test
    @DisplayName("TC7: Password errata (tentativi < 5)")
    void autenticazionePasswordErrata_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione(EMAIL_STUDENTE, "PasswordErrata99!");
        });

        assertTrue(exception.getMessage().contains("Credenziali") || exception.getMessage().contains("inesistente"));
    }

    @Test
    @DisplayName("TC8: Password vuota")
    void autenticazionePasswordVuota_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione(EMAIL_STUDENTE, "");
        });
        assertEquals("La password è obbligatoria.", exception.getMessage());
    }

    @Test
    @DisplayName("TC9: Password < 8 caratteri")
    void autenticazionePasswordCorta_LanciaEccezione() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione(EMAIL_STUDENTE, "Pass1");
        });
        assertEquals("La password deve contenere almeno 8 caratteri.", exception.getMessage());
    }

    @Test
    @DisplayName("TC10: Password > 64 caratteri")
    void autenticazionePasswordLunga_LanciaEccezione() {
        String passLunga = "P".repeat(65);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione(EMAIL_STUDENTE, passLunga);
        });
        assertEquals("Formato password non valido.", exception.getMessage());
    }

    // ==========================================
    // GESTIONE BLOCCO ACCOUNT (BRUTE FORCE)
    // ==========================================

    @Test
    @DisplayName("TC11: Inserimento 5° password errata (Blocco Account)")
    void autenticazioneQuintoTentativoFallito_BloccaAccount() {
        // Simuliamo 4 tentativi falliti
        for (int i = 0; i < 4; i++) {
            assertThrows(RuntimeException.class, () -> {
                bibliotecaFacade.autenticazione(EMAIL_STUDENTE, "Errata123!");
            });
        }

        // Al 5° tentativo deve scattare il blocco
        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione(EMAIL_STUDENTE, "Errata123!");
        });
        assertEquals("Credenziali errate. Account bloccato per 15 minuti per troppi tentativi falliti.", exception.getMessage());
    }

    @Test
    @DisplayName("TC12: Login con account bloccato (tempo non scaduto)")
    void autenticazioneAccountBloccatoTempoNonScaduto_AccessoNegato() {
        // Generiamo il blocco dell'account (5 tentativi errati)
        for (int i = 0; i < 5; i++) {
            try {
                bibliotecaFacade.autenticazione(EMAIL_STUDENTE, "Errata123!");
            } catch (Exception ignored) {}
        }

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bibliotecaFacade.autenticazione(EMAIL_STUDENTE, PASS_VALIDA);
        });

        // Verifica che il messaggio contenga le parole chiave (il minutaggio esatto potrebbe variare)
        assertTrue(exception.getMessage().contains("Account bloccato") && exception.getMessage().contains("minuti"));
    }

    @Test
    @DisplayName("TC13: Login dopo 15 minuti di blocco (Sblocco Account)")
    void autenticazioneDopo15MinutiBlocco_Successo() {
        // 1. Generiamo il blocco dell'account
        for (int i = 0; i < 5; i++) {
            try {
                bibliotecaFacade.autenticazione(EMAIL_STUDENTE, "Errata123!");
            } catch (Exception ignored) {}
        }

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.createQuery("UPDATE Utente u SET u.bloccatoFinoA = :vecchioTempo WHERE u.emailIstituzionale = :email")
                .setParameter("vecchioTempo", LocalDateTime.now().minusMinutes(16))
                .setParameter("email", EMAIL_STUDENTE)
                .executeUpdate();
        tx.commit();

        // 3. Ora il login con password corretta deve funzionare e resettare lo stato
        UtenteDTO utente = bibliotecaFacade.autenticazione(EMAIL_STUDENTE, PASS_VALIDA);
        assertNotNull(utente, "L'autenticazione dovrebbe avere successo dopo lo sblocco");
    }
}