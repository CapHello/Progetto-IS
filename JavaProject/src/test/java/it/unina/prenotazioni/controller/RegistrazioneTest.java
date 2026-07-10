package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.database.JpaUtil;
import it.unina.prenotazioni.dto.UtenteDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

// NOTE: i test case in cui si richiedeva che il codice della postazione, area e sala studio
// dovesse trovarsi all'interno di un intervallo specificato in fase di analisi non è necessario implementarli
// mi basta fare il test sull'id univoco di hibernate che deve esser necessariamente > 0 tranne nel caso di postazione che
// assume volutamente il valore 0 per indicare la strategia di assegnazione.

// il motivo principale per cui non è necessario:
// essendo che prenotazioni, area e sale sono già memorizzate all'interno del database allora il test sul codiceNumerico di ognuna
// di queste è già svolto dalla test-suite di creaSaleStudio. Inoltre come parametri in input ad effettuaPrenotazione non
// è presente il codiceNumerico di ognuno di questi oggetti citati, ma il codice identificativo generato attraverso hibernate.

@DisplayName("Suite di Test - Registrazione Utente")
class RegistrazioneTest {

    private BibliotecaFacade bibliotecaFacade;
    private EntityManager em;


    @BeforeEach
    void setUp() {
        bibliotecaFacade = BibliotecaFacade.getInstance();
        em = JpaUtil.getInstance().getEntityManager();

        // Pulizia totale del DB in-memory prima di ogni test
        svuotaDatabase(em);
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

    @Test
    @DisplayName("TC1: Registrazione Studente con dati validi")
    void registrazioneStudenteValido_Successo() {
        // Arrange
        String email = "a.rossi.tc1@studenti.unina.it"; // Email univoca per evitare conflitti nel DB
        String matricola = "N86001001"; // Matricola univoca

        // Act
        UtenteDTO risultato = bibliotecaFacade.registrazione(
                "Studente", "Antonio", "Rossi", email, "Password123!", matricola
        );

        // Assert
        assertNotNull(risultato, "Il DTO restituito non deve essere nullo");
        assertEquals("Studente", risultato.getRuolo());
        assertEquals("Antonio", risultato.getNome());
        assertEquals(email, risultato.getEmailIstituzionale());
        assertEquals(matricola, risultato.getIdentificativo());
    }

    @Test
    @DisplayName("TC2: Registrazione Bibliotecario con dati validi")
    void registrazioneBibliotecarioValido_Successo() {
        String email = "m.esposito.tc2@unina.it";
        String codice = "BIBLIO001";

        UtenteDTO risultato = bibliotecaFacade.registrazione(
                "Bibliotecario", "Maria", "Esposito", email, "SecurPass2026!", codice
        );

        assertNotNull(risultato);
        assertEquals("Bibliotecario", risultato.getRuolo());
        assertEquals(codice, risultato.getIdentificativo());
    }

    // 
    // CASI DI ERRORE [ERROR] - PARTIZIONE RUOLO
    // 

    @Test
    @DisplayName("TC3: Fallimento con Ruolo vuoto")
    void registrazioneConRuoloVuoto_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "", "Luca", "Bianchi", "l.bianchi@esterno.it", "ValidPass123!", "EST001"
            );
        });

        assertEquals("Il ruolo è obbligatorio e valido.", exception.getMessage());
    }

    @Test
    @DisplayName("TC4: Fallimento con Ruolo non ammesso (Docente)")
    void registrazioneConRuoloDocente_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Docente", "Luca", "Bianchi", "l.bianchi@esterno.it", "ValidPass123!", "EST001"
            );
        });

        assertEquals("Il ruolo è obbligatorio e valido.", exception.getMessage());
    }

    // 
    // CASI DI ERRORE [ERROR] - PARTIZIONE NOME
    // 

    @Test
    @DisplayName("TC5: Fallimento con Nome vuoto")
    void registrazioneConNomeVuoto_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "", "Rossi", "vuoto@studenti.unina.it", "Password123!", "N86001004"
            );
        });
        assertEquals("Il nome è obbligatorio.", exception.getMessage());
    }

    @Test
    @DisplayName("TC6: Fallimento con Nome troppo lungo (> 20 caratteri)")
    void registrazioneConNomeTroppoLungo_LanciaEccezione() {
        String nomeLungo = "A".repeat(51);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", nomeLungo, "Rossi", "pinguino@studenti.unina.it", "Password123!", "N86001004"
            );
        });
        assertEquals("Formato nome non valido (solo lettere, max 20 caratteri).", exception.getMessage());
    }

    @Test
    @DisplayName("TC7: Fallimento con Nome contenente numeri o simboli")
    void registrazioneConNomeNonAlfabetico_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Ant0nio!", "Rossi", "simboli@studenti.unina.it", "Password123!", "N86001005"
            );
        });
        assertEquals("Formato nome non valido (solo lettere, max 20 caratteri).", exception.getMessage());
    }

    // 
    // CASI DI ERRORE [ERROR] - PARTIZIONE COGNOME
    // 

    @Test
    @DisplayName("TC8: Fallimento con Cognome vuoto")
    void registrazioneConCognomeVuoto_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "", "m.cognomevuoto@studenti.unina.it", "Password123!", "N86001009"
            );
        });
        assertEquals("Il cognome è obbligatorio.", exception.getMessage());
    }

    @Test
    @DisplayName("TC9: Fallimento con Cognome troppo lungo (> 20 caratteri)")
    void registrazioneConCognomeTroppoLungo_LanciaEccezione() {
        String cognomeLungo = "A".repeat(51);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", cognomeLungo, "m.cognomelungo@studenti.unina.it", "Password123!", "N86001010"
            );
        });
        assertEquals("Formato cognome non valido (solo lettere, max 20 caratteri).", exception.getMessage());
    }

    @Test
    @DisplayName("TC10: Fallimento con Cognome contenente caratteri speciali/numeri")
    void registrazioneConCognomeNonValido_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "Rossi_86", "m.cognomeerrato@studenti.unina.it", "Password123!", "N86001011"
            );
        });
        assertEquals("Formato cognome non valido (solo lettere, max 20 caratteri).", exception.getMessage());
    }

    // 
    // CASI DI ERRORE [ERROR] - PARTIZIONE EMAIL (Completamento)
    // 

    @Test
    @DisplayName("TC11: Fallimento con Email vuota")
    void registrazioneConEmailVuota_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "Rossi", "", "Password123!", "N86001012"
            );
        });
        assertEquals("L'email è obbligatoria.", exception.getMessage());
    }

    @Test
    @DisplayName("TC12: Fallimento con Email con formato errato")
    void registrazioneConEmailInvalida_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Antonio", "Rossi", "antonio.rossi_unina.it", "Password123!", "N86001006"
            );
        });
        assertEquals("Formato email non valido.", exception.getMessage());
    }


    @Test
    @DisplayName("TC13: Fallimento con Email troppo lunga (> 255 caratteri)")
    void registrazioneConEmailTroppoLunga_LanciaEccezione() {
        String emailLunga = "a".repeat(255) + "@unina.it"; // Stringa > 255 caratteri
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "Rossi", emailLunga, "Password123!", "N86001013"
            );
        });
        assertEquals("Email troppo lunga (max 255 caratteri).", exception.getMessage());
    }

    @Test
    @DisplayName("TC14: Fallimento con Email già in uso (Duplicato DB)")
    void registrazioneConEmailDuplicata_LanciaEccezione() {
        String emailDuplicata = "duplicato@studenti.unina.it";

        // 1. Registriamo il primo utente con successo
        bibliotecaFacade.registrazione(
                "Studente", "Antonio", "Rossi", emailDuplicata, "Password123!", "N86001007"
        );

        // 2. Tentiamo di registrare il secondo utente con la STESSA email ma matricola diversa
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "Verdi", emailDuplicata, "AltraPass456!", "N86001008"
            );
        });
        assertEquals("Email già associata a un account.", exception.getMessage());
    }

    // 
    // CASI DI ERRORE [ERROR] - PARTIZIONE PASSWORD
    // 

    @Test
    @DisplayName("TC15: Fallimento con Password vuota")
    void registrazioneConPasswordVuota_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "Rossi", "m.passvuota@studenti.unina.it", "", "N86001014"
            );
        });
        assertEquals("La password deve contenere almeno 8 caratteri.", exception.getMessage());
    }

    @Test
    @DisplayName("TC16: Fallimento con Password troppo corta (<8 caratteri)")
    void registrazioneConPasswordCorta_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "Rossi", "m.passvuota@studenti.unina.it", "ciao", "N86001014"
            );
        });
        assertEquals("La password deve contenere almeno 8 caratteri.", exception.getMessage());
    }

    @Test
    @DisplayName("TC17: Fallimento con Password troppo lunga (> 32 caratteri)")
    void registrazioneConPasswordTroppoLunga_LanciaEccezione() {
        String passwordLunga = "P".repeat(65);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "Rossi", "m.passlunga@studenti.unina.it", passwordLunga, "N86001015"
            );
        });
        assertEquals("La password non può superare i 32 caratteri.", exception.getMessage());
    }

    // 
    // CASI DI ERRORE [ERROR] - PARTIZIONE IDENTIFICATIVO
    // 

    @Test
    @DisplayName("TC18: Fallimento con Identificativo vuoto")
    void registrazioneConIdentificativoVuoto_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "Rossi", "m.idvuoto@studenti.unina.it", "Password123!", ""
            );
        });
        assertEquals("Il codice identificativo è obbligatorio.", exception.getMessage());
    }

    @Test
    @DisplayName("TC19: Fallimento con Formato Identificativo non riconosciuto")
    void registrazioneConIdentificativoErrato_LanciaEccezione() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            // "n86001234" (lettera minuscola) anziché il formato corretto: una maiuscola + 8 cifre
            bibliotecaFacade.registrazione(
                    "Studente", "Mario", "Rossi", "m.iderrato@studenti.unina.it", "Password123!", "n86001234"
            );
        });
        assertEquals("Formato identificativo non riconosciuto.", exception.getMessage());
    }

    @Test
    @DisplayName("TC20: Fallimento con Identificativo già in uso (Duplicato DB)")
    void registrazioneConIdentificativoDuplicato_LanciaEccezione() {
        String identificativoDuplicato = "N86001018";

        // 1. Registriamo il primo utente con successo
        bibliotecaFacade.registrazione(
                "Bibliotecario", "Luigi", "Verdi", "l.verdi@studenti.unina.it", "Password123!", identificativoDuplicato
        );

        // 2. Tentiamo di registrare un secondo utente con la STESSA matricola ma email diversa
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bibliotecaFacade.registrazione(
                    "Bibliotecario", "Mario", "Rossi", "m.rossi_nuovo@studenti.unina.it", "Password456!", identificativoDuplicato
            );
        });
        assertEquals("Identificativo già in uso nel sistema.", exception.getMessage());
    }


}