package it.unina.prenotazioni.boundary.Swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.SalaMonitoraggioDTO;
import it.unina.prenotazioni.dto.StatisticheDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardBibliotecario {

    // Campi legati al .form
    private JPanel dashboardPane;
    private JPanel header;
    private JLabel lblTitolo;
    private JButton btnLogout;
    private JPanel pannelloContenuto;
    private JPanel rigaTitolo;
    private JLabel lblSezione;
    private JButton btnAggiorna;
    private JPanel pannelloStatistiche;
    private JLabel lblPillOccupazione;
    private JLabel lblPillPrenotazioni;
    private JLabel lblPillNonConfermate;
    private JLabel lblPillPostazioni;
    private JScrollPane scrollSale;
    private JPanel pannelloSale;
    private JButton btnCreaSala;

    // Frame corrente: serve per la navigazione verso CreaSala e per il logout
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

    // --- STATISTICHE E SALE ---

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

    // --- APRI DASHBOARD ---

    public JFrame apriDashboard() {
        frameCorrente = new JFrame("Dashboard Bibliotecario");
        frameCorrente.setContentPane(dashboardPane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);

        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(dashboardPane, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        dashboardPane = new JPanel();
        dashboardPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        dashboardPane.setBackground(new Color(-5192482));
        header = new JPanel();
        header.setLayout(new GridLayoutManager(1, 3, new Insets(10, 15, 10, 15), -1, -1));
        header.setBackground(new Color(-8621082));
        header.setOpaque(true);
        dashboardPane.add(header, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblTitolo = new JLabel();
        Font lblTitoloFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 20, lblTitolo.getFont());
        if (lblTitoloFont != null) lblTitolo.setFont(lblTitoloFont);
        lblTitolo.setForeground(new Color(-1));
        lblTitolo.setText("  Dashboard Bibliotecario");
        header.add(lblTitolo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        header.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnLogout = new JButton();
        btnLogout.setText("Logout");
        header.add(btnLogout, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloContenuto = new JPanel();
        pannelloContenuto.setLayout(new GridLayoutManager(4, 1, new Insets(15, 30, 20, 30), -1, 10));
        pannelloContenuto.setBackground(new Color(-5192482));
        pannelloContenuto.setOpaque(true);
        dashboardPane.add(pannelloContenuto, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        rigaTitolo = new JPanel();
        rigaTitolo.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rigaTitolo.setBackground(new Color(-5192482));
        rigaTitolo.setOpaque(true);
        pannelloContenuto.add(rigaTitolo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblSezione = new JLabel();
        Font lblSezioneFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 18, lblSezione.getFont());
        if (lblSezioneFont != null) lblSezione.setFont(lblSezioneFont);
        lblSezione.setText("Stato Sale Studio");
        rigaTitolo.add(lblSezione, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        rigaTitolo.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnAggiorna = new JButton();
        btnAggiorna.setText("Aggiorna");
        rigaTitolo.add(btnAggiorna, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloStatistiche = new JPanel();
        pannelloStatistiche.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), 8, -1));
        pannelloStatistiche.setBackground(new Color(-5192482));
        pannelloStatistiche.setOpaque(true);
        pannelloContenuto.add(pannelloStatistiche, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblPillOccupazione = new JLabel();
        lblPillOccupazione.setText("Tasso occupazione oggi: —");
        pannelloStatistiche.add(lblPillOccupazione, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblPillPrenotazioni = new JLabel();
        lblPillPrenotazioni.setText("Prenotazioni oggi: —");
        pannelloStatistiche.add(lblPillPrenotazioni, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblPillNonConfermate = new JLabel();
        lblPillNonConfermate.setText("Non confermate: —");
        pannelloStatistiche.add(lblPillNonConfermate, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblPillPostazioni = new JLabel();
        lblPillPostazioni.setText("Postazioni: —");
        pannelloStatistiche.add(lblPillPostazioni, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        pannelloStatistiche.add(spacer3, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        scrollSale = new JScrollPane();
        pannelloContenuto.add(scrollSale, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(920, 560), null, 0, false));
        pannelloSale = new JPanel();
        pannelloSale.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        pannelloSale.setBackground(new Color(-5192482));
        pannelloSale.setOpaque(true);
        scrollSale.setViewportView(pannelloSale);
        btnCreaSala = new JButton();
        btnCreaSala.setBackground(new Color(-1));
        Font btnCreaSalaFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 15, btnCreaSala.getFont());
        if (btnCreaSalaFont != null) btnCreaSala.setFont(btnCreaSalaFont);
        btnCreaSala.setText("+  Crea nuova Sala Studio");
        pannelloContenuto.add(btnCreaSala, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 60), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return dashboardPane;
    }

}
