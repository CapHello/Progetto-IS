package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.controller.factory.BibliotecarioFactory;
import it.unina.prenotazioni.controller.factory.StudenteFactory;
import it.unina.prenotazioni.dto.UtenteDTO;
import it.unina.prenotazioni.entity.Bibliotecario;
import it.unina.prenotazioni.entity.RegistroUtenti;
import it.unina.prenotazioni.entity.Studente;
import it.unina.prenotazioni.entity.Utente;

import java.time.LocalDateTime;

/**
 * Gestore (Singleton) responsabile di Registrazione (UC1) e Autenticazione (UC2).
 * Usa le Factory per creare la sottoclasse corretta di Utente e RegistroUtenti per la
 * persistenza. Gestisce inoltre tentativi falliti e blocco temporaneo (V21).
 */
public class GestoreUtenti {

    private static final int MAX_TENTATIVI = 5;
    private static final int MINUTI_BLOCCO = 15;

    private static GestoreUtenti istanza;

    private GestoreUtenti() {}

    public static GestoreUtenti getInstance() {
        if (istanza == null) {
            istanza = new GestoreUtenti();
        }
        return istanza;
    }

    // ------------------------------------------------------------------ UC1
    public Object registrazione(String ruolo, String nome, String cognome,
                                String email, String password, String identificativo) {
        // Controlli con i messaggi attesi dai test di unità (non modificarne le sottostringhe).
        if (ruolo == null || ruolo.isEmpty() || !verificaRuolo(ruolo)) {
            throw new IllegalArgumentException("Il ruolo è obbligatorio e valido.");
        }
        if (nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("Il nome è obbligatorio.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La password deve contenere almeno 8 caratteri.");
        }
        if (password.length() > 32) {
            throw new IllegalArgumentException("La password non può superare i 32 caratteri.");
        }

        // Validazioni di formato (V12-V14, V20) specifiche per ruolo.
        boolean studente = "Studente".equalsIgnoreCase(ruolo);
        if (studente) {
            verificaCorrettezzaStudente(nome, cognome, email, identificativo);
        } else {
            verificaCorrettezzaBibliotecario(nome, cognome, email, identificativo);
        }

        // Un solo RegistroUtenti per l'intera operazione.
        RegistroUtenti registro = RegistroUtenti.getInstance();
        if (registro.esisteEmailIstituzionale(email)) {
            throw new IllegalArgumentException("Email già associata a un account.");
        }

        Utente nuovoUtente;
        if (studente) {
            if (registro.cercaStudentePerMatricola(identificativo) != null) {
                throw new IllegalArgumentException("Matricola già in uso.");
            }
            nuovoUtente = new StudenteFactory().creaUtente(nome, cognome, email, password, identificativo);
        } else {
            if (registro.cercaBibliotecarioPerCodice(identificativo) != null) {
                throw new IllegalArgumentException("Codice identificativo già in uso.");
            }
            nuovoUtente = new BibliotecarioFactory().creaUtente(nome, cognome, email, password, identificativo);
        }

        registro.registraUtente(nuovoUtente);
        return toDTO(nuovoUtente);
    }

    // ------------------------------------------------------------------ UC2
    public Object autenticazione(String email, String password) {
        RegistroUtenti registro = RegistroUtenti.getInstance();
        Utente utente = registro.cercaUtentePerEmail(email);

        if (utente == null) {
            throw new SecurityException("Credenziali errate");
        }
        if (utente.isBloccato()) {
            throw new SecurityException("Account temporaneamente bloccato. Riprovare più tardi.");
        }

        if (!verificaCredenziali(utente, password)) {
            incrementaTentativi(utente);
            if (utente.getTentativiFalliti() >= MAX_TENTATIVI) {
                bloccaAccountTemporaneamente(utente, MINUTI_BLOCCO);
            }
            registro.aggiorna(utente);
            throw new SecurityException("Credenziali errate");
        }

        resetTentativi(utente);
        registro.aggiorna(utente);
        return toDTO(utente);
    }

    // -------------------------------------------------------------- UC8 (profilo)
    public Object visualizzaProfilo(Long idStudente) {
        RegistroUtenti registro = RegistroUtenti.getInstance();
        Studente studente = registro.trovaStudentePerId(idStudente);
        if (studente == null) {
            throw new IllegalArgumentException("Studente non trovato");
        }
        return toDTO(studente);
    }

    // ------------------------------------------------------------------ helper privati
    private boolean verificaRuolo(String ruolo) {
        return "Studente".equalsIgnoreCase(ruolo) || "Bibliotecario".equalsIgnoreCase(ruolo);
    }

    private void verificaCorrettezzaStudente(String nome, String cognome, String email, String matricola) {
        validaNomeCognome(nome, cognome);
        validaEmail(email);
        validaIdentificativo(matricola, "matricola");
    }

    private void verificaCorrettezzaBibliotecario(String nome, String cognome, String email, String codice) {
        validaNomeCognome(nome, cognome);
        validaEmail(email);
        validaIdentificativo(codice, "codice identificativo");
    }

    private void validaNomeCognome(String nome, String cognome) {
        if (!nome.matches("\\p{L}{1,20}")) {
            throw new IllegalArgumentException("Formato nome non valido (solo lettere, max 20 caratteri).");
        }
        if (cognome == null || cognome.isEmpty()) {
            throw new IllegalArgumentException("Il cognome è obbligatorio.");
        }
        if (!cognome.matches("\\p{L}{1,20}")) {
            throw new IllegalArgumentException("Formato cognome non valido (solo lettere, max 20 caratteri).");
        }
    }

    private void validaEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("L'email è obbligatoria.");
        }
        if (email.length() > 35) {
            throw new IllegalArgumentException("Email troppo lunga (max 35 caratteri).");
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Formato email non valido.");
        }
    }

    private void validaIdentificativo(String identificativo, String etichetta) {
        if (identificativo == null || identificativo.isEmpty()) {
            throw new IllegalArgumentException("Il codice identificativo è obbligatorio.");
        }
        if (!identificativo.matches("[A-Za-z0-9]+")) {
            throw new IllegalArgumentException("Formato " + etichetta + " non valido.");
        }
    }

    private boolean verificaCredenziali(Utente utente, String password) {
        return password != null && password.equals(utente.getPassword());
    }

    private void incrementaTentativi(Utente utente) {
        utente.setTentativiFalliti(utente.getTentativiFalliti() + 1);
    }

    private void bloccaAccountTemporaneamente(Utente utente, int durataMinuti) {
        utente.setBloccatoFinoA(LocalDateTime.now().plusMinutes(durataMinuti));
        utente.setTentativiFalliti(0);
    }

    private void resetTentativi(Utente utente) {
        utente.setTentativiFalliti(0);
        utente.setBloccatoFinoA(null);
    }

    private UtenteDTO toDTO(Utente utente) {
        UtenteDTO dto = new UtenteDTO();
        dto.setId(utente.getId());
        dto.setNome(utente.getNome());
        dto.setCognome(utente.getCognome());
        dto.setEmailIstituzionale(utente.getEmailIstituzionale());
        if (utente instanceof Studente s) {
            dto.setRuolo("Studente");
            dto.setIdentificativo(s.getMatricola());
            dto.setNumeroTotaleAccessi(s.getNumeroTotaleAccessi());
        } else if (utente instanceof Bibliotecario b) {
            dto.setRuolo("Bibliotecario");
            dto.setIdentificativo(b.getCodiceIdentificativoInterno());
        }
        return dto;
    }
}
