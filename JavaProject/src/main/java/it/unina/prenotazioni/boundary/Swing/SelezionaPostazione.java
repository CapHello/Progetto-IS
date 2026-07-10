package it.unina.prenotazioni.boundary.Swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.unina.prenotazioni.dto.AreaDettaglioDTO;
import it.unina.prenotazioni.dto.PostazioneDTO;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

public class SelezionaPostazione {

    // Campi legati al .form
    private JPanel selezionaPostazionePane;
    private JPanel header;
    private JLabel lblTitolo;
    private JButton btnLogout;
    private JPanel pannelloSteps;
    private JLabel lblSteps;
    private JPanel pannelloContenuto;
    private JPanel pannelloSplit;
    private JPanel cardPosti;
    private JLabel lblTitoloCard;
    private JLabel lblSottotitolo;
    private JPanel pannelloPosti;
    private JPanel colonnaDestra;
    private JPanel cardDettaglio;
    private JLabel lblTitoloDettaglio;
    private JLabel lblDettaglio;
    private JPanel cardAuto;
    private JLabel lblOppure;
    private JRadioButton rbAutomatica;
    private JButton btnContinua;
    private JPanel pannelloAzioni;
    private JLabel lblIndietro;

    // Stato interno
    private final StatoWizard stato;
    private JFrame frameCorrente;
    private JButton bottoneSelezionato;

    public SelezionaPostazione(StatoWizard stato) {
        this.stato = stato;

        // Styling non configurabile nel form designer
        StileWizard.stilizzaLogout(btnLogout);
        StileWizard.stilizzaBottone(btnContinua, StileWizard.VIOLA);
        StileWizard.stilizzaIndietro(lblIndietro);
        StileWizard.bordaCard(cardPosti);
        StileWizard.bordaCard(cardDettaglio);
        StileWizard.bordaCard(cardAuto);
        StileWizard.configuraRadioCard(rbAutomatica);
        rbAutomatica.setText("<html><b>Assegnazione automatica</b><br>"
                + "<font color='gray'><small>il sistema sceglie per te</small></font></html>");
        lblSteps.setText(StileWizard.htmlSteps(4));
        lblSottotitolo.setText(stato.getNomeSala() + " · " + stato.getTipologiaArea()
                + " · " + StileWizard.formattaData(stato.getData()) + " · " + stato.getEtichettaFascia());

        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
        lblIndietro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameCorrente.dispose();
                new SelezionaArea(stato).apriForm();
            }
        });
        rbAutomatica.addActionListener(e -> scegliAutomatica());
        btnContinua.addActionListener(e -> continua());

        costruisciGriglia();
    }

    // --- GRIGLIA POSTI ---

    private void costruisciGriglia() {
        pannelloPosti.removeAll();
        pannelloPosti.setLayout(new BorderLayout());
        pannelloPosti.setBackground(Color.WHITE);
        stato.setIdPostazione(null);
        stato.setEtichettaPostazione(null);
        bottoneSelezionato = null;
        rbAutomatica.setSelected(false);

        AreaDettaglioDTO area = areaSelezionata();
        if (area == null || area.getPostazioni().isEmpty()) {
            JLabel lblVuoto = new JLabel("Nessuna postazione presente nell'area.");
            lblVuoto.setForeground(StileWizard.GRIGIO_TESTO);
            pannelloPosti.add(lblVuoto, BorderLayout.NORTH);
        } else {
            JPanel griglia = new JPanel(new GridLayout(0, 6, 10, 10));
            griglia.setBackground(Color.WHITE);
            for (PostazioneDTO posto : area.getPostazioni()) {
                griglia.add(creaBottonePosto(posto));
            }
            pannelloPosti.add(griglia, BorderLayout.NORTH);
        }
        pannelloPosti.revalidate();
        pannelloPosti.repaint();
    }

    private JButton creaBottonePosto(PostazioneDTO posto) {
        JButton bottone = new JButton("P" + posto.getNumero());
        bottone.setPreferredSize(new Dimension(80, 80));
        bottone.setFont(new Font("SansSerif", Font.BOLD, 13));
        bottone.setForeground(Color.WHITE);
        bottone.setFocusPainted(false);
        bottone.setBorderPainted(false);
        bottone.setOpaque(true);
        if (posto.isDisponibile()) {
            bottone.setBackground(StileWizard.VERDE);
            bottone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            bottone.addActionListener(e -> selezionaPosto(posto, bottone));
        } else {
            bottone.setBackground(StileWizard.ROSSO);
            bottone.setEnabled(false);
        }
        return bottone;
    }

    private void selezionaPosto(PostazioneDTO posto, JButton bottone) {
        rbAutomatica.setSelected(false);
        if (bottoneSelezionato != null) {
            bottoneSelezionato.setBackground(StileWizard.VERDE);
        }
        bottoneSelezionato = bottone;
        bottone.setBackground(StileWizard.VIOLA);

        stato.setIdPostazione(posto.getId());
        stato.setEtichettaPostazione("P" + posto.getNumero());
        lblTitoloDettaglio.setText("Postazione P" + posto.getNumero());
        lblDettaglio.setText("<html>"
                + "<font color='gray'>Sala:</font> <b>" + stato.getNomeSala() + "</b><br>"
                + "<font color='gray'>Area:</font> <b>" + stato.getTipologiaArea() + "</b><br>"
                + "<font color='gray'>Fascia:</font> <b>" + stato.getEtichettaFascia() + "</b><br>"
                + "<font color='gray'>Stato:</font> <b><font color='#38A169'>libera</font></b></html>");
    }

    private void scegliAutomatica() {
        if (bottoneSelezionato != null) {
            bottoneSelezionato.setBackground(StileWizard.VERDE);
            bottoneSelezionato = null;
        }
        stato.setIdPostazione(StatoWizard.ASSEGNAZIONE_AUTOMATICA);
        stato.setEtichettaPostazione("Assegnazione automatica");
    }

    private AreaDettaglioDTO areaSelezionata() {
        if (stato.getDettaglio() == null) {
            return null;
        }
        for (AreaDettaglioDTO area : stato.getDettaglio().getAree()) {
            if (area.getIdArea().equals(stato.getIdArea())) {
                return area;
            }
        }
        return null;
    }

    private void continua() {
        if (stato.getIdPostazione() == null) {
            mostraErrore("Seleziona una postazione o l'assegnazione automatica");
            return;
        }
        frameCorrente.dispose();
        new RiepilogoPrenotazione(stato).apriForm();
    }

    // --- APRI FORM ---

    public JFrame apriForm() {
        frameCorrente = new JFrame("Nuova Prenotazione");
        frameCorrente.setContentPane(selezionaPostazionePane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(selezionaPostazionePane, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
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
        selezionaPostazionePane = new JPanel();
        selezionaPostazionePane.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        selezionaPostazionePane.setBackground(new Color(-5192482));
        header = new JPanel();
        header.setLayout(new GridLayoutManager(1, 3, new Insets(10, 15, 10, 15), -1, -1));
        header.setBackground(new Color(-8621082));
        header.setOpaque(true);
        selezionaPostazionePane.add(header, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        pannelloSteps = new JPanel();
        pannelloSteps.setLayout(new GridLayoutManager(1, 1, new Insets(12, 20, 12, 20), -1, -1));
        pannelloSteps.setBackground(new Color(-5192482));
        pannelloSteps.setOpaque(true);
        selezionaPostazionePane.add(pannelloSteps, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblSteps = new JLabel();
        lblSteps.setForeground(new Color(-6710887));
        lblSteps.setHorizontalAlignment(0);
        lblSteps.setText("1 Data  Sala  ──  2 Fascia Oraria  ──  3 Area  ──  4 Postazione  ──  5 Conferma");
        lblSteps.setDisplayedMnemonic(' ');
        lblSteps.setDisplayedMnemonicIndex(7);
        pannelloSteps.add(lblSteps, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloContenuto = new JPanel();
        pannelloContenuto.setLayout(new GridLayoutManager(2, 1, new Insets(10, 30, 20, 30), -1, 8));
        pannelloContenuto.setBackground(new Color(-5192482));
        pannelloContenuto.setOpaque(true);
        selezionaPostazionePane.add(pannelloContenuto, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        pannelloSplit = new JPanel();
        pannelloSplit.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 15, -1));
        pannelloSplit.setBackground(new Color(-5192482));
        pannelloSplit.setOpaque(true);
        pannelloContenuto.add(pannelloSplit, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        cardPosti = new JPanel();
        cardPosti.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, 10));
        cardPosti.setBackground(new Color(-1));
        cardPosti.setOpaque(true);
        pannelloSplit.add(cardPosti, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblTitoloCard = new JLabel();
        Font lblTitoloCardFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 16, lblTitoloCard.getFont());
        if (lblTitoloCardFont != null) lblTitoloCard.setFont(lblTitoloCardFont);
        lblTitoloCard.setText("4. Scegli la postazione");
        cardPosti.add(lblTitoloCard, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblSottotitolo = new JLabel();
        lblSottotitolo.setForeground(new Color(-6710887));
        lblSottotitolo.setText("sala · area · data · fascia");
        cardPosti.add(lblSottotitolo, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloPosti = new JPanel();
        pannelloPosti.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        pannelloPosti.setBackground(new Color(-1));
        pannelloPosti.setOpaque(true);
        cardPosti.add(pannelloPosti, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        colonnaDestra = new JPanel();
        colonnaDestra.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, 15));
        colonnaDestra.setBackground(new Color(-5192482));
        colonnaDestra.setOpaque(true);
        pannelloSplit.add(colonnaDestra, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(300, -1), null, 0, false));
        cardDettaglio = new JPanel();
        cardDettaglio.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, 10));
        cardDettaglio.setBackground(new Color(-460036));
        cardDettaglio.setOpaque(true);
        colonnaDestra.add(cardDettaglio, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        lblTitoloDettaglio = new JLabel();
        Font lblTitoloDettaglioFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 15, lblTitoloDettaglio.getFont());
        if (lblTitoloDettaglioFont != null) lblTitoloDettaglio.setFont(lblTitoloDettaglioFont);
        lblTitoloDettaglio.setText("Postazione");
        cardDettaglio.add(lblTitoloDettaglio, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblDettaglio = new JLabel();
        lblDettaglio.setForeground(new Color(-6710887));
        lblDettaglio.setText("Seleziona una postazione libera.");
        cardDettaglio.add(lblDettaglio, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        cardAuto = new JPanel();
        cardAuto.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, 10));
        cardAuto.setBackground(new Color(-1));
        cardAuto.setOpaque(true);
        colonnaDestra.add(cardAuto, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblOppure = new JLabel();
        Font lblOppureFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 15, lblOppure.getFont());
        if (lblOppureFont != null) lblOppure.setFont(lblOppureFont);
        lblOppure.setText("Oppure");
        cardAuto.add(lblOppure, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rbAutomatica = new JRadioButton();
        rbAutomatica.setText("Assegnazione automatica");
        cardAuto.add(rbAutomatica, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnContinua = new JButton();
        btnContinua.setBackground(new Color(-8621082));
        btnContinua.setForeground(new Color(-1));
        btnContinua.setText("Continua");
        colonnaDestra.add(btnContinua, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloAzioni = new JPanel();
        pannelloAzioni.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        pannelloAzioni.setBackground(new Color(-5192482));
        pannelloAzioni.setOpaque(true);
        pannelloContenuto.add(pannelloAzioni, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblIndietro = new JLabel();
        lblIndietro.setForeground(new Color(-6710887));
        lblIndietro.setText("← Indietro");
        pannelloAzioni.add(lblIndietro, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        pannelloAzioni.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
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
        return selezionaPostazionePane;
    }

}
