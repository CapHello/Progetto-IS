package it.unina.prenotazioni.boundary.Swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.PrenotazioneDTO;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardStudente {

    private static final Color VIOLA = new Color(124, 115, 230);
    private static final Color TESTO_GRIGIO = new Color(120, 120, 130);

    // Campi legati al .form (i nomi devono corrispondere agli attributi binding="...")
    private JPanel dashboardStudente;
    private JPanel header;
    private JLabel lblTitolo;
    private JButton btnLogout;
    private JPanel contentPanel;
    private JLabel lblNome;
    private JLabel lblMatricola;
    private JLabel lblAccessi;
    private JSeparator separatore;
    private JLabel lblPrenotazioni;
    private JComboBox comboOrdine;
    private JScrollPane scrollPrenotazioni;
    private JPanel pannelloPrenotazioni;
    private JButton btnNuovaPrenotazione;

    // Stato interno
    private UtenteDTO utente;
    private JFrame frameCorrente;
    private List<PrenotazioneDTO> prenotazioni = new ArrayList<>();

    public DashboardStudente() {
        // Styling aggiuntivo non configurabile nel form designer
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(VIOLA);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnNuovaPrenotazione.setFocusPainted(false);
        btnNuovaPrenotazione.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        scrollPrenotazioni.setBorder(null);
        scrollPrenotazioni.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPrenotazioni.getVerticalScrollBar().setUnitIncrement(16);

        comboOrdine.setModel(new DefaultComboBoxModel<>(new String[]{"data", "stato"}));
        comboOrdine.addActionListener(e -> render());

        btnNuovaPrenotazione.addActionListener(e -> {
            frameCorrente.dispose();
            new SelezionaDataAndSala(new StatoWizard(utente)).apriForm();
        });
        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
    }

    // --- PROFILO E STORICO ---

    private void carica() {
        try {
            UtenteDTO profilo = BibliotecaFacade.getInstance().visualizzaProfiloPersonale(utente.getId());
            lblNome.setText("Profilo Personale di " + profilo.getNome() + " " + profilo.getCognome());
            lblMatricola.setText("Matricola: " + profilo.getIdentificativo());
            lblAccessi.setText("Totale accessi: " + profilo.getNumeroTotaleAccessi());
            prenotazioni = BibliotecaFacade.getInstance().consultaStoricoPrenotazioni(utente.getId());
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
        render();
    }

    private void render() {
        String criterio = (String) comboOrdine.getSelectedItem();
        List<PrenotazioneDTO> lista = new ArrayList<>(prenotazioni);
        if ("stato".equals(criterio)) {
            lista.sort(Comparator.comparing(PrenotazioneDTO::getStato));
        } else {
            lista.sort(Comparator.comparing(
                    (PrenotazioneDTO p) -> p.getData() == null ? LocalDate.MIN : p.getData()).reversed());
        }

        pannelloPrenotazioni.removeAll();
        pannelloPrenotazioni.setLayout(new BorderLayout());
        pannelloPrenotazioni.setBackground(contentPanel.getBackground());

        if (lista.isEmpty()) {
            JLabel lblVuoto = new JLabel("Non hai ancora prenotazioni.", JLabel.CENTER);
            lblVuoto.setForeground(TESTO_GRIGIO);
            pannelloPrenotazioni.add(lblVuoto, BorderLayout.NORTH);
        } else {
            JPanel griglia = new JPanel(new GridLayout(0, 2, 15, 15));
            griglia.setBackground(contentPanel.getBackground());
            for (PrenotazioneDTO prenotazione : lista) {
                CardPrenotazione card = new CardPrenotazione(prenotazione);
                card.addCheckInListener(e -> checkin(prenotazione.getIdPrenotazione()));
                card.addAnnullaListener(e -> annulla(prenotazione.getIdPrenotazione()));
                griglia.add(card.getRoot());
            }
            pannelloPrenotazioni.add(griglia, BorderLayout.NORTH);
        }
        pannelloPrenotazioni.revalidate();
        pannelloPrenotazioni.repaint();
    }

    // --- AZIONI ---

    private void checkin(Long idPrenotazione) {
        try {
            BibliotecaFacade.getInstance().effettuaCheckin(idPrenotazione);
            carica();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
    }

    private void annulla(Long idPrenotazione) {
        int scelta = JOptionPane.showConfirmDialog(dashboardStudente,
                "Confermi l'annullamento della prenotazione?",
                "Annulla prenotazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (scelta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            BibliotecaFacade.getInstance().annullaPrenotazione(idPrenotazione);
            carica();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
    }

    // --- APRI DASHBOARD ---

    public JFrame apriDashboard(UtenteDTO utente) {
        this.utente = utente;
        frameCorrente = new JFrame("Dashboard Studente");
        frameCorrente.setContentPane(dashboardStudente);

        lblNome.setText("Profilo Personale di " + utente.getNome() + " " + utente.getCognome());
        lblMatricola.setText("Matricola: " + utente.getIdentificativo());
        lblAccessi.setText("Totale accessi: " + utente.getNumeroTotaleAccessi());
        carica();

        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);

        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        frameCorrente.pack();
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(dashboardStudente, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
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
        dashboardStudente = new JPanel();
        dashboardStudente.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        dashboardStudente.setBackground(new Color(-5192482));
        header = new JPanel();
        header.setLayout(new GridLayoutManager(1, 3, new Insets(10, 15, 10, 15), -1, -1));
        header.setBackground(new Color(-8621082));
        header.setOpaque(true);
        dashboardStudente.add(header, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblTitolo = new JLabel();
        Font lblTitoloFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 20, lblTitolo.getFont());
        if (lblTitoloFont != null) lblTitolo.setFont(lblTitoloFont);
        lblTitolo.setForeground(new Color(-1));
        lblTitolo.setText("  Dashboard Studente");
        header.add(lblTitolo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        header.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnLogout = new JButton();
        btnLogout.setText("Logout");
        header.add(btnLogout, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(7, 2, new Insets(25, 35, 25, 35), 5, 8));
        contentPanel.setBackground(new Color(-5192482));
        contentPanel.setOpaque(true);
        dashboardStudente.add(contentPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        lblNome = new JLabel();
        Font lblNomeFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 20, lblNome.getFont());
        if (lblNomeFont != null) lblNome.setFont(lblNomeFont);
        lblNome.setText("Profilo Personale di ...");
        contentPanel.add(lblNome, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(209, 32), null, 0, false));
        lblMatricola = new JLabel();
        lblMatricola.setForeground(new Color(-8882046));
        lblMatricola.setText("Matricola: —");
        contentPanel.add(lblMatricola, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblAccessi = new JLabel();
        lblAccessi.setForeground(new Color(-8882046));
        lblAccessi.setText("Totale accessi: 0");
        contentPanel.add(lblAccessi, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        separatore = new JSeparator();
        contentPanel.add(separatore, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblPrenotazioni = new JLabel();
        Font lblPrenotazioniFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblPrenotazioni.getFont());
        if (lblPrenotazioniFont != null) lblPrenotazioni.setFont(lblPrenotazioniFont);
        lblPrenotazioni.setText("Prenotazioni");
        contentPanel.add(lblPrenotazioni, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(88, 48), null, 0, false));
        comboOrdine = new JComboBox();
        contentPanel.add(comboOrdine, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(105, 48), null, 0, false));
        scrollPrenotazioni = new JScrollPane();
        contentPanel.add(scrollPrenotazioni, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(920, 650), null, 0, false));
        pannelloPrenotazioni = new JPanel();
        pannelloPrenotazioni.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        pannelloPrenotazioni.setBackground(new Color(-5192482));
        pannelloPrenotazioni.setOpaque(true);
        scrollPrenotazioni.setViewportView(pannelloPrenotazioni);
        btnNuovaPrenotazione = new JButton();
        btnNuovaPrenotazione.setBackground(new Color(-8621082));
        btnNuovaPrenotazione.setForeground(new Color(-1));
        btnNuovaPrenotazione.setText("+ Nuova Prenotazione");
        contentPanel.add(btnNuovaPrenotazione, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        return dashboardStudente;
    }

}
