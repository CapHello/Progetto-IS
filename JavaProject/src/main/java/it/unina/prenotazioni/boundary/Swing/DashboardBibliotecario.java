package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.SalaMonitoraggioDTO;
import it.unina.prenotazioni.dto.StatisticheDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardBibliotecario {

    public static void main(String[] args) {
        // test sulla singola interfaccia
        new DashboardBibliotecario().apriDashboard();
    }

    // Campi legati al .form
    private JPanel      dashboardPane;
    private JPanel      header;
    private JLabel      lblTitolo;
    private JButton     btnLogout;
    private JPanel      pannelloContenuto;
    private JPanel      rigaTitolo;
    private JLabel      lblSezione;
    private JButton     btnAggiorna;
    private JPanel      pannelloStatistiche;
    private JLabel      lblPillOccupazione;
    private JLabel      lblPillPrenotazioni;
    private JLabel      lblPillNonConfermate;
    private JLabel      lblPillPostazioni;
    private JScrollPane scrollSale;
    private JPanel      pannelloSale;
    private JButton     btnCreaSala;

    // Frame corrente — serve per la navigazione verso CreaSala e per il logout
    private JFrame frameCorrente;

    public DashboardBibliotecario() {
        // Styling non configurabile nel form designer
        StileWizard.stilizzaLogout(btnLogout);
        StileWizard.stilizzaLogout(btnAggiorna);
        btnCreaSala.setForeground(StileWizard.VIOLA);
        btnCreaSala.setFocusPainted(false);
        btnCreaSala.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCreaSala.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(StileWizard.GRIGIO_BORDO, 1, true),
                new EmptyBorder(12, 20, 12, 20)));
        for (JLabel pill : new JLabel[]{lblPillOccupazione, lblPillPrenotazioni,
                lblPillNonConfermate, lblPillPostazioni}) {
            StileWizard.stilizzaPill(pill);
        }

        scrollSale.setBorder(null);
        scrollSale.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollSale.getVerticalScrollBar().setUnitIncrement(16);

        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
        btnAggiorna.addActionListener(e -> carica());
        btnCreaSala.addActionListener(e -> {
            frameCorrente.dispose();
            new CreaSala().apriForm();
        });

        carica();
    }

    // ── STATISTICHE E SALE ───────────────────────────────────────────────────

    private void carica() {
        StatisticheDTO statistiche = null;
        List<SalaMonitoraggioDTO> sale = new ArrayList<>();
        try {
            statistiche = BibliotecaFacade.getInstance().monitoraStatisticheServizio();
            sale = BibliotecaFacade.getInstance().monitoraSale();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
        aggiornaStatistiche(statistiche);
        costruisciSale(sale);
    }

    private void aggiornaStatistiche(StatisticheDTO statistiche) {
        if (statistiche == null) {
            return;
        }
        lblPillOccupazione.setText("<html>Tasso occupazione oggi: <b>"
                + String.format(Locale.ITALIAN, "%.1f", statistiche.getTassoOccupazione()) + "%</b></html>");
        lblPillPrenotazioni.setText("<html>Prenotazioni oggi: <b>"
                + statistiche.getPrenotazioniOggi() + "</b></html>");
        lblPillNonConfermate.setText("<html>Non confermate: <b>"
                + statistiche.getPrenotazioniNonConfermate() + "</b></html>");
        lblPillPostazioni.setText("<html>Postazioni: <b>" + statistiche.getPostazioniOccupateOggi()
                + "/" + statistiche.getPostazioniTotali() + "</b></html>");
    }

    private void costruisciSale(List<SalaMonitoraggioDTO> sale) {
        pannelloSale.removeAll();
        pannelloSale.setLayout(new BorderLayout());
        pannelloSale.setBackground(pannelloContenuto.getBackground());

        if (sale.isEmpty()) {
            JLabel lblVuoto = new JLabel("Nessuna sala presente. Creane una nuova qui sotto.");
            lblVuoto.setForeground(StileWizard.GRIGIO_TESTO);
            pannelloSale.add(lblVuoto, BorderLayout.NORTH);
        } else {
            // sale attive prima di quelle eliminate
            sale.sort(Comparator.comparing(sala -> !sala.isAttiva()));
            JPanel griglia = new JPanel(new GridLayout(0, 3, 15, 15));
            griglia.setBackground(pannelloContenuto.getBackground());
            for (SalaMonitoraggioDTO sala : sale) {
                CardSala card = new CardSala(sala);
                card.addEliminaListener(e -> elimina(sala));
                griglia.add(card.getRoot());
            }
            pannelloSale.add(griglia, BorderLayout.NORTH);
        }
        pannelloSale.revalidate();
        pannelloSale.repaint();
    }

    private void elimina(SalaMonitoraggioDTO sala) {
        int scelta = JOptionPane.showConfirmDialog(dashboardPane,
                "Eliminare la sala \"" + sala.getNomeSala()
                        + "\"? Le prenotazioni attive/confermate saranno annullate.",
                "Elimina sala", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (scelta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            BibliotecaFacade.getInstance().eliminaSalaStudio(sala.getIdSala());
            carica();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
    }

    // ── APRI DASHBOARD ───────────────────────────────────────────────────────

    public JFrame apriDashboard() {
        frameCorrente = new JFrame("Dashboard Bibliotecario");
        frameCorrente.setContentPane(dashboardPane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);

        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(dashboardPane, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
}
