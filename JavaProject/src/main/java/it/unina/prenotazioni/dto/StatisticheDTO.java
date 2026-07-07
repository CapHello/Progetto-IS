package it.unina.prenotazioni.dto;

/** Statistiche di servizio per il bibliotecario (UC13): occupazione, volumi, non confermate. */
public class StatisticheDTO {
    private int prenotazioniOggi;
    private int prenotazioniNonConfermate;   // ATTIVE oggi non ancora confermate via check-in
    private int postazioniTotali;
    private int postazioniOccupateOggi;
    private double tassoOccupazione;          // percentuale 0..100

    public StatisticheDTO() {}

    public int getPrenotazioniOggi() { return prenotazioniOggi; }
    public void setPrenotazioniOggi(int prenotazioniOggi) { this.prenotazioniOggi = prenotazioniOggi; }
    public int getPrenotazioniNonConfermate() { return prenotazioniNonConfermate; }
    public void setPrenotazioniNonConfermate(int prenotazioniNonConfermate) { this.prenotazioniNonConfermate = prenotazioniNonConfermate; }
    public int getPostazioniTotali() { return postazioniTotali; }
    public void setPostazioniTotali(int postazioniTotali) { this.postazioniTotali = postazioniTotali; }
    public int getPostazioniOccupateOggi() { return postazioniOccupateOggi; }
    public void setPostazioniOccupateOggi(int postazioniOccupateOggi) { this.postazioniOccupateOggi = postazioniOccupateOggi; }
    public double getTassoOccupazione() { return tassoOccupazione; }
    public void setTassoOccupazione(double tassoOccupazione) { this.tassoOccupazione = tassoOccupazione; }
}
