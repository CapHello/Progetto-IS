package it.unina.prenotazioni.boundary.Swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardStudente {

    public static void main(String[] args){
        // test sulla singola interfaccia
        DashboardStudente dashboardStudente1 = new DashboardStudente();

        dashboardStudente1.apriDashboard("Giovanni","124",234986);
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

    public DashboardStudente() {
        // Styling aggiuntivo non configurabile nel form designer
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(VIOLA);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnNuovaPrenotazione.setFocusPainted(false);
        btnNuovaPrenotazione.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnNuovaPrenotazione.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelezionaDataAndSala selezionaDataAndSala = new SelezionaDataAndSala();
                selezionaDataAndSala.apriForm();
            }
        });
    }

    public JFrame apriDashboard(String nome, String matricola, int totaleAccessi) {
        JFrame frame = new JFrame("Dashboard Studente");
        frame.setContentPane(dashboardStudente);

        lblNome.setText("Profilo Personale di " + nome);
        lblMatricola.setText("Matricola: " + matricola);
        lblAccessi.setText("Totale accessi: " + totaleAccessi);

        comboOrdine.setModel(new DefaultComboBoxModel<>(new String[]{"data", "stato"}));

        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setResizable(false);
        frame.setVisible(true);



        return frame;
    }

    // Getter per il controller
    public void addLogoutListener(ActionListener l){
        btnLogout.addActionListener(l);
    }

    public JComboBox getComboOrdine()                          { return comboOrdine; }
    public JPanel getContentPanel()                            { return contentPanel; }
    public JLabel getLblVuoto()                                { return lblVuoto; }
}
