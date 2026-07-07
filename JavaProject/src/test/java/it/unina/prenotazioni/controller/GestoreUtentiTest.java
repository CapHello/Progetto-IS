package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.database.GestorePersistenza;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.MockedConstruction;
import it.unina.prenotazioni.entity.RegistroUtenti;

@ExtendWith(MockitoExtension.class)
public class GestoreUtentiTest {

    private GestoreUtenti gestoreUtenti;

    @BeforeEach
    void setUp() {
        gestoreUtenti = GestoreUtenti.getInstance();
    }

    @Test
    void testRegistrazioneStudenteValida() {
        try (MockedConstruction<RegistroUtenti> mocked = mockConstruction(RegistroUtenti.class,
                (mock, context) -> {
                    when(mock.registraUtente(any())).thenReturn(true);
                })) {
            
            assertDoesNotThrow(() -> {
                gestoreUtenti.registrazione(
                    "Studente", 
                    "Mario", 
                    "Rossi", 
                    "m.rossi@studenti.unina.it", 
                    "Password123!", 
                    "N86001234"
                );
            });

            assertEquals(1, mocked.constructed().size());
            verify(mocked.constructed().get(0), times(1)).registraUtente(any());
        }
    }

    @Test
    void testRegistrazioneBibliotecarioValida() {
        try (MockedConstruction<RegistroUtenti> mocked = mockConstruction(RegistroUtenti.class,
                (mock, context) -> {
                    when(mock.registraUtente(any())).thenReturn(true);
                })) {
            
            assertDoesNotThrow(() -> {
                gestoreUtenti.registrazione(
                    "Bibliotecario", 
                    "Laura", 
                    "Bianchi", 
                    "l.bianchi@unina.it", 
                    "SecurePass8!", 
                    "BIB01"
                );
            });
            assertEquals(1, mocked.constructed().size());
        }
    }

    // Test Case ID 3: Ruolo vuoto [ERROR]
    @Test
    void testRegistrazioneRuoloVuoto() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gestoreUtenti.registrazione(
                "", 
                "Mario", 
                "Rossi", 
                "m.rossi@unina.it", 
                "Password123!", 
                "N86001234"
            );
        });
        assertTrue(exception.getMessage().contains("ruolo è obbligatorio"));
    }

    // Test Case ID 5: Nome vuoto [ERROR]
    @Test
    void testRegistrazioneNomeVuoto() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gestoreUtenti.registrazione(
                "Studente", 
                "", 
                "Rossi", 
                "m.rossi@unina.it", 
                "Password123!", 
                "N86001234"
            );
        });
        assertTrue(exception.getMessage().contains("nome è obbligatorio"));
    }

    // Test Case ID 16: Password < 8 caratteri [ERROR]
    @Test
    void testRegistrazionePasswordCorta() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gestoreUtenti.registrazione(
                "Studente", 
                "Mario", 
                "Rossi", 
                "m.rossi@unina.it", 
                "Pass1", 
                "N86001234"
            );
        });
        assertTrue(exception.getMessage().contains("almeno 8 caratteri"));
    }
}
