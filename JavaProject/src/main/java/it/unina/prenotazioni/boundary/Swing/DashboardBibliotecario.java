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

/**
 * Dashboard del bibliotecario, replica Swing di dashboard-bibliotecario.html:
 * statistiche di servizio (UC13) e stato delle sale (UC11) con eliminazione (UC4)
 * e accesso alla creazione di una nuova sala (UC3). Dati ricaricati a ogni azione.
 */
public class DashboardBibliotecario {

    public static void main(String[] args) {
        // test sulla singola interfaccia: richiede MySQL attivo
        new DashboardBibliotecario().apriDashboard();
    }

    private static final Color VERDE_BADGE  = new Color(72, 187, 120);   // posti liberi
    private static final Color GIALLO_BADGE = new Color(236, 201, 75);   // attive non confermate
    private static final Color ROSSO_BADGE  = new Color(245, 101, 101);  // confermate (presenti)
    private static final Color PILL_SFONDO  = new Color(237, 242, 247);
    private static final Color PILL_TESTO   = new Color(74, 85, 104);

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

    // ── CARICAMENTO DATI (UC13 + UC11) ───────────────────────────────────────

    /** Ricarica statistiche e stato delle sale, come la carica() della GUI web. */
    private void carica() {
        StatisticheDTO statistiche = null;
        List<SalaMonitoraggioDTO> sale = new ArrayList<>();
        try {
            statistiche = BibliotecaFacade.getInstance().monitoraStatisticheServizio();
            sale = BibliotecaFacade.getInstance().monitoraSale();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
        costruisciStatistiche(statistiche);
        costruisciSale(sale);
    }

    private void costruisciStatistiche(StatisticheDTO statistiche) {
        pannelloStatistiche.removeAll();
        pannelloStatistiche.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pannelloStatistiche.setBackground(pannelloContenuto.getBackground());
        if (statistiche != null) {
            pannelloStatistiche.add(creaPill("Tasso occupazione oggi: <b>"
                    + String.format(Locale.ITALIAN, "%.1f", statistiche.getTassoOccupazione()) + "%</b>"));
            pannelloStatistiche.add(creaPill("Prenotazioni oggi: <b>" + statistiche.getPrenotazioniOggi() + "</b>"));
            pannelloStatistiche.add(creaPill("Non confermate: <b>" + statistiche.getPrenotazioniNonConfermate() + "</b>"));
            pannelloStatistiche.add(creaPill("Postazioni: <b>" + statistiche.getPostazioniOccupateOggi()
                    + "/" + statistiche.getPostazioniTotali() + "</b>"));
        }
        pannelloStatistiche.revalidate();
        pannelloStatistiche.repaint();
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
            // Le sale attive prima di quelle eliminate, come nella GUI web
            sale.sort(Comparator.comparing(sala -> !sala.isAttiva()));
            JPanel griglia = new JPanel(new GridLayout(0, 3, 15, 15));
            griglia.setBackground(pannelloContenuto.getBackground());
            for (SalaMonitoraggioDTO sala : sale) {
                griglia.add(creaCardSala(sala));
            }
            pannelloSale.add(griglia, BorderLayout.NORTH);
        }
        pannelloSale.revalidate();
        pannelloSale.repaint();
    }

    // ── CARD SALA ────────────────────────────────────────────────────────────

    private JPanel creaCardSala(SalaMonitoraggioDTO sala) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                sala.isAttiva() ? new LineBorder(StileWizard.GRIGIO_BORDO, 1, true)
                                : BorderFactory.createDashedBorder(new Color(204, 204, 204)),
                new EmptyBorder(14, 14, 14, 14)));

        String nomeHtml = sala.isAttiva()
                ? "<html><b>" + sala.getNomeSala() + "</b></html>"
                : "<html><b><font color='#999999'>" + sala.getNomeSala()
                        + "</font></b> <font color='#E53E3E' size='2'><b>(Eliminata)</b></font></html>";
        JLabel lblNome = new JLabel(nomeHtml);
        lblNome.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lblNome.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblNome);
        card.add(Box.createVerticalStrut(10));

        // Badge liberi / attive / confermate (grigi se la sala è eliminata)
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        badges.setBackground(Color.WHITE);
        boolean attiva = sala.isAttiva();
        badges.add(creaBadge(sala.getPostiLiberi(), attiva ? VERDE_BADGE : Color.GRAY, "Posti liberi"));
        badges.add(creaBadge(sala.getPostiAttivi(), attiva ? GIALLO_BADGE : Color.GRAY, "Attive (non confermate)"));
        badges.add(creaBadge(sala.getPostiConfermati(), attiva ? ROSSO_BADGE : Color.GRAY, "Confermate (presenti)"));
        badges.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(badges);
        card.add(Box.createVerticalStrut(10));

        JPanel aree = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        aree.setBackground(Color.WHITE);
        if (sala.getAree().isEmpty()) {
            aree.add(creaPill("nessuna area"));
        } else {
            for (String area : sala.getAree()) {
                aree.add(creaPill(area));
            }
        }
        aree.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(aree);
        card.add(Box.createVerticalStrut(10));

        if (sala.isAttiva()) {
            JButton btnElimina = new JButton("Elimina sala");
            btnElimina.setForeground(new Color(229, 62, 62));
            btnElimina.setBackground(Color.WHITE);
            btnElimina.setFocusPainted(false);
            btnElimina.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnElimina.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(229, 62, 62), 1, true),
                    new EmptyBorder(6, 14, 6, 14)));
            btnElimina.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnElimina.addActionListener(e -> elimina(sala));
            card.add(btnElimina);
        } else {
            JLabel lblRimossa = new JLabel("Rimossa dall'elenco pubblico");
            lblRimossa.setForeground(StileWizard.GRIGIO_TESTO);
            lblRimossa.setFont(new Font("SansSerif", Font.ITALIC, 12));
            lblRimossa.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblRimossa);
        }
        return card;
    }

    /** UC4: eliminazione (soft delete) con conferma, poi ricarica dei dati. */
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

    // ── ELEMENTI GRAFICI ─────────────────────────────────────────────────────

    private JLabel creaPill(String html) {
        JLabel pill = new JLabel("<html>" + html + "</html>");
        pill.setOpaque(true);
        pill.setBackground(PILL_SFONDO);
        pill.setForeground(PILL_TESTO);
        pill.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pill.setBorder(new EmptyBorder(4, 10, 4, 10));
        return pill;
    }

    private JLabel creaBadge(int valore, Color sfondo, String descrizione) {
        JLabel badge = new JLabel(String.valueOf(valore), JLabel.CENTER);
        badge.setOpaque(true);
        badge.setBackground(sfondo);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 13));
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        badge.setToolTipText(descrizione);
        return badge;
    }

    // ── APRI DASHBOARD ───────────────────────────────────────────────────────

    public JFrame apriDashboard() {
        frameCorrente = new JFrame("Dashboard Bibliotecario");
        frameCorrente.setContentPane(dashboardPane);
        frameCorrente.setSize(1000, 760);
        frameCorrente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(true);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(dashboardPane, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
}
