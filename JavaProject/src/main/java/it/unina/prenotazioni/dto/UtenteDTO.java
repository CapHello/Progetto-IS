package it.unina.prenotazioni.dto;

public class UtenteDTO {
    private Long id;
    private String nome;
    private String cognome;
    private String emailIstituzionale;
    private String ruolo;
    private String identificativo; // matricola o codice
    private int numeroTotaleAccessi; // significativo per lo studente (profilo personale)

    public UtenteDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public String getEmailIstituzionale() { return emailIstituzionale; }
    public void setEmailIstituzionale(String emailIstituzionale) { this.emailIstituzionale = emailIstituzionale; }
    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }
    public String getIdentificativo() { return identificativo; }
    public void setIdentificativo(String identificativo) { this.identificativo = identificativo; }
    public int getNumeroTotaleAccessi() { return numeroTotaleAccessi; }
    public void setNumeroTotaleAccessi(int numeroTotaleAccessi) { this.numeroTotaleAccessi = numeroTotaleAccessi; }
}
