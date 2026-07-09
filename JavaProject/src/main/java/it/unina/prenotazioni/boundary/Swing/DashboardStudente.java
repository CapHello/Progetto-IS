package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class DashboardStudente {

    public static void main(String[] args){
        // test sulla singola interfaccia
        UtenteDTO utente = new UtenteDTO();
        utente.setId(1L);
        utente.setNome("Giovanni");
        utente.setCognome("Rossi");
        utente.setIdentificativo("N46001234");
        utente.setNumeroTotaleAccessi(12);

        new DashboardStudente().apriDashboard(utente);
    }

    private static final Color VIOLA        = new Color(124, 115, 230);
    private static final Color TESTO_GRIGIO = new Color(120, 120, 130);

    // Campi legati al .form (i nomi devono corrispondere agli attributi binding="...")
    private JPanel     dashboardStudente;
    private JPanel     header;
    private JLabel     lblTitolo;
    private JButton    btnLogout;
    private JPanel     contentPanel;
    private JLabel     lblNome;
    private JLabel     lblMatricola;
    private JLabel     lblAccessi;
    private JSeparator separatore;
    private JLabel     lblPrenotazioni;
    private JComboBox  comboOrdine;
    private JLabel     lblVuoto;
    private JButton    btnNuovaPrenotazione;

    // Utente loggato e frame corrente — servono per il wizard di prenotazione e per il logout
    private UtenteDTO utente;
    private JFrame frameCorrente;

    public DashboardStudente() {
        // Styling aggiuntivo non configurabile nel form designer
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(VIOLA);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnNuovaPrenotazione.setFocusPainted(false);
        btnNuovaPrenotazione.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnNuovaPrenotazione.addActionListener(e -> {
            frameCorrente.dispose();
            new SelezionaDataAndSala(new StatoWizard(utente)).apriForm();
        });
        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
    }

    public JFrame apriDashboard(UtenteDTO utente) {
        this.utente = utente;
        frameCorrente = new JFrame("Dashboard Studente");
        frameCorrente.setContentPane(dashboardStudente);

        lblNome.setText("Profilo Personale di " + utente.getNome() + " " + utente.getCognome());
        lblMatricola.setText("Matricola: " + utente.getIdentificativo());
        lblAccessi.setText("Totale accessi: " + utente.getNumeroTotaleAccessi());

        comboOrdine.setModel(new DefaultComboBoxModel<>(new String[]{"data", "stato"}));

        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);

        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);

        return frameCorrente;
    }

    // Getter per il controller
    public void addLogoutListener(ActionListener l){
        btnLogout.addActionListener(l);
    }

    public JComboBox getComboOrdine()                          { return comboOrdine; }
    public JPanel getContentPanel()                            { return contentPanel; }
    public JLabel getLblVuoto()                                { return lblVuoto; }
}
