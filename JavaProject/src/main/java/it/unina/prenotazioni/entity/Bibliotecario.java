package it.unina.prenotazioni.entity;

import jakarta.persistence.*;

@Entity
public class Bibliotecario extends Utente {

    @Column(nullable = false, unique = true)
    private String codiceIdentificativoInterno;

    public Bibliotecario() {/* Vuoto perché utilizziamo il Factory Method @Antonio Cacciatore*/}

    // Getters and Setters
    public String getCodiceIdentificativoInterno() { return codiceIdentificativoInterno; }
    public void setCodiceIdentificativoInterno(String codiceIdentificativoInterno) { this.codiceIdentificativoInterno = codiceIdentificativoInterno; }
}
