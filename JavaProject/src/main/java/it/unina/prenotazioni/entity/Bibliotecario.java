package it.unina.prenotazioni.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * <entity> Utente che amministra le sale e il servizio (UC3, UC4, UC11, UC13);
 * identificato dal codice interno.
 */
@Entity
public class Bibliotecario extends Utente {

    @Column(nullable = false, unique = true)
    private String codiceIdentificativoInterno;

    public Bibliotecario() { /* Costruttore vuoto richiesto da JPA: l'istanza nasce da BibliotecarioFactory (UC1). */ }

    // Getters and Setters
    public String getCodiceIdentificativoInterno() { return codiceIdentificativoInterno; }
    public void setCodiceIdentificativoInterno(String codiceIdentificativoInterno) { this.codiceIdentificativoInterno = codiceIdentificativoInterno; }
}
