package it.unina.prenotazioni.dto;

import java.util.List;

public class SalaStudioDTO {
    private Long id;
    private String nome;
    private String descrizione;
    private int numeroPostazioniTotali;
    private List<String> fasceOrarie;
    private boolean attiva;

    public SalaStudioDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public int getNumeroPostazioniTotali() { return numeroPostazioniTotali; }
    public void setNumeroPostazioniTotali(int numeroPostazioniTotali) { this.numeroPostazioniTotali = numeroPostazioniTotali; }
    public List<String> getFasceOrarie() { return fasceOrarie; }
    public void setFasceOrarie(List<String> fasceOrarie) { this.fasceOrarie = fasceOrarie; }
    public boolean isAttiva() {return attiva;}
    public void setAttiva(boolean attiva) {this.attiva = attiva;}
}
