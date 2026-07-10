package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.AreaDettaglioDTO;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Locale;

public class SelezionaArea {

    public static void main(String[] args) {
        // test sulla singola interfaccia
        UtenteDTO utente = new UtenteDTO();
        utente.setId(1L);
        StatoWizard stato = new StatoWizard(utente);
        stato.setData(LocalDate.now());
        stato.setIdSala(1L);
        stato.setNomeSala("Sala1");
        stato.setIdFascia(1L);
        stato.setEtichettaFascia("08:30-10:30");
        new SelezionaArea(stato).apriForm();
    }

    // Campi legati al .form
    private JPanel selezionaAreaPane;
    private JPanel header;
    private JLabel lblTitolo;
    private JButton btnLogout;
    private JPanel pannelloSteps;
    private JLabel lblSteps;
    private JPanel pannelloContenuto;
    private JPanel cardArea;
    private JLabel lblTitoloCard;
    private JLabel lblSottotitolo;
    private JPanel pannelloAree;
    private JPanel pannelloAzioni;
    private JLabel lblIndietro;
    private JButton btnContinua;

    // Stato interno
    private final StatoWizard stato;
    private JFrame frameCorrente;
    private ButtonGroup gruppoAree = new ButtonGroup();

    public SelezionaArea(StatoWizard stato) {
        this.stato = stato;

        // Styling non configurabile nel form designer
        StileWizard.stilizzaLogout(btnLogout);
        StileWizard.stilizzaBottone(btnContinua, StileWizard.VIOLA);
        StileWizard.stilizzaIndietro(lblIndietro);
        StileWizard.bordaCard(cardArea);
        lblSteps.setText(StileWizard.htmlSteps(3));
        lblSottotitolo.setText("<html>Scegli un'area della sala <b>" + stato.getNomeSala()
                + "</b> (l'area <i>comune</i> è sempre disponibile).</html>");

        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
        lblIndietro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameCorrente.dispose();
                new SelezionaFascia(stato).apriForm();
            }
        });
        btnContinua.addActionListener(e -> continua());

        caricaAree();
    }

    // ── AREE ─────────────────────────────────────────────────────────────────

    private void caricaAree() {
        pannelloAree.removeAll();
        pannelloAree.setLayout(new BorderLayout());
        pannelloAree.setBackground(Color.WHITE);
        gruppoAree = new ButtonGroup();
        stato.setIdArea(null);
        stato.setTipologiaArea(null);

        try {
            stato.setDettaglio(BibliotecaFacade.getInstance()
                    .selezionaDettaglioSala(stato.getIdSala(), stato.getIdFascia(), stato.getData()));
        } catch (RuntimeException ex) {
            stato.setDettaglio(null);
            mostraErrore(ex.getMessage());
        }

        if (stato.getDettaglio() == null || stato.getDettaglio().getAree().isEmpty()) {
            JLabel lblVuoto = new JLabel("Nessuna area presente nella sala.");
            lblVuoto.setForeground(StileWizard.GRIGIO_TESTO);
            pannelloAree.add(lblVuoto, BorderLayout.NORTH);
        } else {
            JPanel griglia = new JPanel(new GridLayout(0, 4, 12, 12));
            griglia.setBackground(Color.WHITE);
            for (AreaDettaglioDTO area : stato.getDettaglio().getAree()) {
                JRadioButton radio = StileWizard.creaRadioCard("<html><b>Area: " + area.getTipologia()
                        + "</b><br><font color='#48BB78'>●</font><font color='gray'><small> "
                        + area.getPostiDisponibili() + " postazioni disponibili</small></font></html>");
                radio.addActionListener(e -> {
                    stato.setIdArea(area.getIdArea());
                    stato.setTipologiaArea(area.getTipologia());
                });
                gruppoAree.add(radio);
                griglia.add(radio);
            }
            pannelloAree.add(griglia, BorderLayout.NORTH);
        }
        pannelloAree.revalidate();
        pannelloAree.repaint();
    }

    private void continua() {
        if (stato.getIdArea() == null) {
            mostraErrore("Seleziona un'area");
            return;
        }
        frameCorrente.dispose();
        new SelezionaPostazione(stato).apriForm();
    }

    // ── APRI FORM ────────────────────────────────────────────────────────────

    public JFrame apriForm() {
        frameCorrente = new JFrame("Nuova Prenotazione");
        frameCorrente.setContentPane(selezionaAreaPane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(selezionaAreaPane, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
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
        selezionaAreaPane = new JPanel();
        selezionaAreaPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        selezionaAreaPane.setBackground(new Color(-5192482));
        header = new JPanel();
        header.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(10, 15, 10, 15), -1, -1));
        header.setBackground(new Color(-8621082));
        header.setOpaque(true);
        selezionaAreaPane.add(header, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblTitolo = new JLabel();
        Font lblTitoloFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 20, lblTitolo.getFont());
        if (lblTitoloFont != null) lblTitolo.setFont(lblTitoloFont);
        lblTitolo.setForeground(new Color(-1));
        lblTitolo.setText("  Dashboard Studente");
        header.add(lblTitolo, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        header.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnLogout = new JButton();
        btnLogout.setText("Logout");
        header.add(btnLogout, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloSteps = new JPanel();
        pannelloSteps.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(12, 20, 12, 20), -1, -1));
        pannelloSteps.setBackground(new Color(-5192482));
        pannelloSteps.setOpaque(true);
        selezionaAreaPane.add(pannelloSteps, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblSteps = new JLabel();
        lblSteps.setForeground(new Color(-6710887));
        lblSteps.setHorizontalAlignment(0);
        lblSteps.setText("1 Data  Sala  ──  2 Fascia Oraria  ──  3 Area  ──  4 Postazione  ──  5 Conferma");
        lblSteps.setDisplayedMnemonic(' ');
        lblSteps.setDisplayedMnemonicIndex(7);
        pannelloSteps.add(lblSteps, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloContenuto = new JPanel();
        pannelloContenuto.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(10, 30, 20, 30), -1, -1));
        pannelloContenuto.setBackground(new Color(-5192482));
        pannelloContenuto.setOpaque(true);
        selezionaAreaPane.add(pannelloContenuto, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        cardArea = new JPanel();
        cardArea.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, 10));
        cardArea.setBackground(new Color(-1));
        cardArea.setOpaque(true);
        pannelloContenuto.add(cardArea, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblTitoloCard = new JLabel();
        Font lblTitoloCardFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 16, lblTitoloCard.getFont());
        if (lblTitoloCardFont != null) lblTitoloCard.setFont(lblTitoloCardFont);
        lblTitoloCard.setText("3. Scegli un'Area");
        cardArea.add(lblTitoloCard, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblSottotitolo = new JLabel();
        lblSottotitolo.setForeground(new Color(-6710887));
        lblSottotitolo.setText("Scegli un'area della sala.");
        cardArea.add(lblSottotitolo, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloAree = new JPanel();
        pannelloAree.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        pannelloAree.setBackground(new Color(-1));
        pannelloAree.setOpaque(true);
        cardArea.add(pannelloAree, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        pannelloAzioni = new JPanel();
        pannelloAzioni.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(10, 0, 0, 0), -1, -1));
        pannelloAzioni.setBackground(new Color(-1));
        pannelloAzioni.setOpaque(true);
        cardArea.add(pannelloAzioni, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblIndietro = new JLabel();
        lblIndietro.setForeground(new Color(-6710887));
        lblIndietro.setText("← Indietro");
        pannelloAzioni.add(lblIndietro, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        pannelloAzioni.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnContinua = new JButton();
        btnContinua.setBackground(new Color(-8621082));
        btnContinua.setForeground(new Color(-1));
        btnContinua.setText("Continua");
        pannelloAzioni.add(btnContinua, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        return selezionaAreaPane;
    }
}
