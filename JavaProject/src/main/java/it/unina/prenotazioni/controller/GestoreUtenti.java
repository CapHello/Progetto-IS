package it.unina.prenotazioni.controller;

import it.unina.prenotazioni.controller.factory.BibliotecarioFactory;
import it.unina.prenotazioni.controller.factory.StudenteFactory;
import it.unina.prenotazioni.controller.factory.UtenteFactory;
import it.unina.prenotazioni.dto.UtenteDTO;
import it.unina.prenotazioni.entity.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * <<control>> Gestore (Singleton) di Registrazione (UC1), Autenticazione (UC2) e
 * profilo personale (UC8). Usa le Factory per creare la sottoclasse corretta di
 * Utente e gestisce tentativi falliti e blocco temporaneo dell'account (V21).
 */
public class GestoreUtenti {

    private static final int MAX_TENTATIVI = 5;
    private static final int MINUTI_BLOCCO = 15;

    /** V13: email istituzionale, dominio unina.it o suoi sottodomini (es. studenti.unina.it). */
    private static final String REGEX_EMAIL_ISTITUZIONALE = "^[^@\\s]+@([^@\\s]+\\.)*unina\\.it$";

    /** V14: matricola studente, una lettera maiuscola seguita da 8 cifre. */
    private static final String REGEX_MATRICOLA = "[A-Z]\\d{8}";

    private final RegistroUtenti registroUtenti = RegistroUtenti.getInstance();

    private static GestoreUtenti istanza;

    private GestoreUtenti() {}

    public static GestoreUtenti getInstance() {
        if (istanza == null) {
            istanza = new GestoreUtenti();
        }
        return istanza;
    }

    /** Guardia di validazione: lancia IllegalArgumentException col messaggio dato se la condizione è falsa. */
    private void richiedi(boolean condizione, String messaggio) {
        if (!condizione) {
            throw new IllegalArgumentException(messaggio);
        }
    }

    /** Controlli comuni a entrambi i ruoli: ruolo ammesso, nome presente, password 8-32 caratteri. */
    private void verificaValidita(String ruolo, String nome, String password) {
        // I messaggi sono attesi dai test di unità: non modificarne le sottostringhe.
        richiedi(ruolo != null && !ruolo.isEmpty() && verificaRuolo(ruolo), "Il ruolo è obbligatorio e valido.");
        richiedi(nome != null && !nome.isEmpty(), "Il nome è obbligatorio.");
        richiedi(password != null && password.length() >= 8, "La password deve contenere almeno 8 caratteri.");
        richiedi(password.length() <= 32, "La password non può superare i 32 caratteri.");
    }

    // ------------------------------------------------------------------ UC1
    /**
     * Valida i dati (V12-V14, V20), verifica l'unicità di email e identificativo,
     * crea l'utente tramite la Factory del ruolo e lo persiste.
     */
    public UtenteDTO registrazione(String ruolo, String nome, String cognome,
                                String email, String password, String identificativo) {
        verificaValidita(ruolo, nome, password);

        // Validazioni di formato (V12-V14, V20) specifiche per ruolo.
        boolean studente = "Studente".equalsIgnoreCase(ruolo);
        if (studente) {
            verificaCorrettezzaStudente(nome, cognome, email, identificativo);
        } else {
            verificaCorrettezzaBibliotecario(nome, cognome, email, identificativo);
        }

        if (registroUtenti.esisteEmailIstituzionale(email)) {
            throw new IllegalArgumentException("Email già associata a un account.");
        }

        boolean identificativoGiaUsato = studente
                ? registroUtenti.cercaStudentePerMatricola(identificativo) != null
                : registroUtenti.cercaBibliotecarioPerCodice(identificativo) != null;
        if (identificativoGiaUsato) {
            throw new IllegalArgumentException("Identificativo già in uso nel sistema.");
        }

        // Il ruolo decide la Factory; da qui in poi si lavora solo col tipo astratto Utente.
        UtenteFactory factory = studente ? new StudenteFactory() : new BibliotecarioFactory();
        Utente nuovoUtente = factory.creaUtente(nome, cognome, email, password, identificativo);

        registroUtenti.registraUtente(nuovoUtente);
        return toDTO(nuovoUtente);
    }

    // ------------------------------------------------------------------ UC2
    /**
     * Valida il formato delle credenziali, poi le verifica sul registro; dopo 5 tentativi
     * falliti blocca temporaneamente l'account per 15 minuti (V21). L'errore di verifica è
     * lo stesso per email inesistente e password errata, per non rivelare quali email
     * sono registrate.
     */
    public UtenteDTO autenticazione(String email, String password) {
        verificaFormatoCredenziali(email, password);

        Utente utente = registroUtenti.cercaUtentePerEmail(email);

        if (utente == null) {
            throw new SecurityException("Credenziali non valide o utente inesistente.");
        }
        if (utente.isBloccato()) {
            throw new SecurityException("Account bloccato per troppi tentativi falliti. Riprovare tra "
                    + MINUTI_BLOCCO + " minuti.");
        }

        if (!verificaCredenziali(utente, password)) {
            incrementaTentativi(utente);
            boolean appenaBloccato = utente.getTentativiFalliti() >= MAX_TENTATIVI;
            if (appenaBloccato) {
                bloccaAccountTemporaneamente(utente, MINUTI_BLOCCO);
            }
            registroUtenti.aggiorna(utente);
            if (appenaBloccato) {
                throw new SecurityException("Credenziali errate. Account bloccato per " + MINUTI_BLOCCO
                        + " minuti per troppi tentativi falliti.");
            }
            throw new SecurityException("Credenziali non valide o utente inesistente.");
        }

        resetTentativi(utente);
        registroUtenti.aggiorna(utente);
        return toDTO(utente);
    }

    /**
     * Formato delle credenziali in ingresso (prima della verifica sul registro): un input
     * malformato viene rifiutato subito e non consuma tentativi di accesso. I limiti sono
     * quelli imposti dalla registrazione (password 8-32; email istituzionale sotto i 255).
     */
    private void verificaFormatoCredenziali(String email, String password) {
        richiedi(email != null && !email.isEmpty(), "L'email è obbligatoria.");
        richiedi(emailBenFormata(email), "Formato email non valido.");
        richiedi(password != null && !password.isEmpty(), "La password è obbligatoria.");
        richiedi(password.length() >= 8, "La password deve contenere almeno 8 caratteri.");
        richiedi(password.length() <= 32, "Formato password non valido.");
    }

    /** Regola unica dell'email valida (V13), condivisa tra registrazione e autenticazione. */
    private boolean emailBenFormata(String email) {
        return email.length() < 255 && email.matches(REGEX_EMAIL_ISTITUZIONALE);
    }

    // -------------------------------------------------------------- UC8 (profilo)
    /** Profilo personale dello studente (dati anagrafici e totale accessi). */
    public UtenteDTO visualizzaProfilo(Long idStudente) {
        Studente studente = registroUtenti.trovaStudentePerId(idStudente);
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
        if (!matricola.matches(REGEX_MATRICOLA)) {
            throw new IllegalArgumentException(
                    "Formato identificativo non riconosciuto: la matricola deve essere una lettera maiuscola seguita da 8 cifre (es. N86001234).");
        }
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
        // Stesso limite dell'autenticazione: chi si registra deve poi potersi autenticare.
        if (email.length() >= 255) {
            throw new IllegalArgumentException("Email troppo lunga (max 255 caratteri).");
        }
        if (!emailBenFormata(email)) {
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
        utente.setBloccatoFinoA(LocalDateTime.now(ZoneId.of("Europe/Rome")).plusMinutes(durataMinuti));
        utente.setTentativiFalliti(0);
    }

    private void resetTentativi(Utente utente) {
        utente.setTentativiFalliti(0);
        utente.setBloccatoFinoA(null);
    }

    /** Converte l'entity nel DTO, ricavando ruolo e identificativo dalla sottoclasse concreta. */
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
