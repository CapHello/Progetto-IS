package it.unina.prenotazioni.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utente{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cognome;

    @Column(nullable = false, unique = true)
    private String emailIstituzionale;

    private String password;

    // Gestione dei tentativi di accesso falliti e del blocco temporaneo (V21).
    private int tentativiFalliti;
    private LocalDateTime bloccatoFinoA;

    protected Utente() {/* Vuoto perché utilizziamo il Factory Method @Antonio Cacciatore*/}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmailIstituzionale() { return emailIstituzionale; }
    public void setEmailIstituzionale(String emailIstituzionale) { this.emailIstituzionale = emailIstituzionale; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getTentativiFalliti() { return tentativiFalliti; }
    public void setTentativiFalliti(int tentativiFalliti) { this.tentativiFalliti = tentativiFalliti; }

    public LocalDateTime getBloccatoFinoA() { return bloccatoFinoA; }
    public void setBloccatoFinoA(LocalDateTime bloccatoFinoA) { this.bloccatoFinoA = bloccatoFinoA; }

    /** True se l'account risulta attualmente bloccato (blocco temporaneo non ancora scaduto). */
    public boolean isBloccato() {
        return bloccatoFinoA != null && LocalDateTime.now().isBefore(bloccatoFinoA);
    }
}
