package it.unina.prenotazioni.boundary.Swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.PrenotazioneDTO;
import it.unina.prenotazioni.dto.RichiestaPrenotazioneDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

public class RiepilogoPrenotazione {

    // Campi legati al .form
    private JPanel riepilogoPane;
    private JPanel header;
    private JLabel lblTitolo;
    private JButton btnLogout;
    private JPanel pannelloSteps;
    private JLabel lblSteps;
    private JPanel pannelloContenuto;
    private JPanel cardRiepilogo;
    private JLabel lblTitoloCard;
    private JPanel pannelloRiepilogo;
    private JLabel lblVoceSala;
    private JLabel lblValoreSala;
    private JLabel lblVoceData;
    private JLabel lblValoreData;
    private JLabel lblVoceFascia;
    private JLabel lblValoreFascia;
    private JLabel lblVoceArea;
    private JLabel lblValoreArea;
    private JLabel lblVocePostazione;
    private JLabel lblValorePostazione;
    private JPanel pannelloPromemoria;
    private JLabel lblPromemoriaTitolo;
    private JLabel lblPromemoria;
    private JPanel pannelloAzioni;
    private JLabel lblIndietro;
    private JButton btnConferma;

    // Stato interno
    private final StatoWizard stato;
    private JFrame frameCorrente;

    public RiepilogoPrenotazione(StatoWizard stato) {
        this.stato = stato;

        // Styling non configurabile nel form designer
        StileWizard.stilizzaLogout(btnLogout);
        StileWizard.stilizzaBottone(btnConferma, StileWizard.VERDE);
        StileWizard.stilizzaIndietro(lblIndietro);
        StileWizard.bordaCard(cardRiepilogo);
        lblSteps.setText(StileWizard.htmlSteps(5));

        pannelloPromemoria.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, StileWizard.VIOLA),
                BorderFactory.createCompoundBorder(
                        new LineBorder(StileWizard.GRIGIO_BORDO, 1),
                        new EmptyBorder(14, 20, 14, 20))));
        lblPromemoria.setText("<html><ul style='margin:0; padding-left:16px;'>"
                + "<li>Il check-in va effettuato al più 10 minuti dopo l'orario di inizio della prenotazione.</li>"
                + "<li>L'annullamento è possibile fino ad almeno 6 ore prima dell'inizio della fascia oraria.</li>"
                + "<li>Alla conferma riceverai una notifica sulla tua casella di posta istituzionale.</li>"
                + "</ul></html>");

        pannelloRiepilogo.setBorder(new LineBorder(StileWizard.GRIGIO_BORDO, 1, true));
        lblValoreSala.setText(stato.getNomeSala());
        lblValoreData.setText(StileWizard.formattaData(stato.getData()));
        lblValoreFascia.setText(stato.getEtichettaFascia());
        lblValoreArea.setText(stato.getTipologiaArea());
        lblValorePostazione.setText(stato.getEtichettaPostazione());

        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
        lblIndietro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameCorrente.dispose();
                new SelezionaPostazione(stato).apriForm();
            }
        });
        btnConferma.addActionListener(e -> conferma());
    }

    // --- CONFERMA ---

    private void conferma() {
        try {
            PrenotazioneDTO prenotazione = BibliotecaFacade.getInstance().effettuaPrenotazione(
                    new RichiestaPrenotazioneDTO(
                            stato.getIdSala(), stato.getIdArea(), stato.getIdPostazione(),
                            stato.getData(), stato.getIdFascia(), stato.getStudente().getId()));
            frameCorrente.dispose();
            new PrenotazioneConfermata(stato, prenotazione).apriForm();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
    }

    // --- APRI FORM ---

    public JFrame apriForm() {
        frameCorrente = new JFrame("Nuova Prenotazione");
        frameCorrente.setContentPane(riepilogoPane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(riepilogoPane, messaggio, "Errore prenotazione", JOptionPane.ERROR_MESSAGE);
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
        riepilogoPane = new JPanel();
        riepilogoPane.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        riepilogoPane.setBackground(new Color(-5192482));
        header = new JPanel();
        header.setLayout(new GridLayoutManager(1, 3, new Insets(10, 15, 10, 15), -1, -1));
        header.setBackground(new Color(-8621082));
        header.setOpaque(true);
        riepilogoPane.add(header, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        riepilogoPane.add(pannelloSteps, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblSteps = new JLabel();
        lblSteps.setForeground(new Color(-6710887));
        lblSteps.setHorizontalAlignment(0);
        lblSteps.setText("1 Data  Sala  ──  2 Fascia Oraria  ──  3 Area  ──  4 Postazione  ──  5 Conferma");
        lblSteps.setDisplayedMnemonic(' ');
        lblSteps.setDisplayedMnemonicIndex(7);
        pannelloSteps.add(lblSteps, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloContenuto = new JPanel();
        pannelloContenuto.setLayout(new GridLayoutManager(1, 3, new Insets(10, 30, 20, 30), -1, -1));
        pannelloContenuto.setBackground(new Color(-5192482));
        pannelloContenuto.setOpaque(true);
        riepilogoPane.add(pannelloContenuto, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        pannelloContenuto.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        cardRiepilogo = new JPanel();
        cardRiepilogo.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, 15));
        cardRiepilogo.setBackground(new Color(-1));
        cardRiepilogo.setOpaque(true);
        pannelloContenuto.add(cardRiepilogo, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(760, -1), null, 0, false));
        lblTitoloCard = new JLabel();
        Font lblTitoloCardFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 18, lblTitoloCard.getFont());
        if (lblTitoloCardFont != null) lblTitoloCard.setFont(lblTitoloCardFont);
        lblTitoloCard.setText("5. Riepilogo e Conferma");
        cardRiepilogo.add(lblTitoloCard, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloRiepilogo = new JPanel();
        pannelloRiepilogo.setLayout(new GridLayoutManager(5, 2, new Insets(24, 28, 24, 28), 10, 14));
        pannelloRiepilogo.setBackground(new Color(-460036));
        pannelloRiepilogo.setOpaque(true);
        cardRiepilogo.add(pannelloRiepilogo, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        lblVoceSala = new JLabel();
        lblVoceSala.setForeground(new Color(-8882046));
        lblVoceSala.setText("Sala Studio:");
        pannelloRiepilogo.add(lblVoceSala, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(160, -1), null, 0, false));
        lblValoreSala = new JLabel();
        Font lblValoreSalaFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblValoreSala.getFont());
        if (lblValoreSalaFont != null) lblValoreSala.setFont(lblValoreSalaFont);
        lblValoreSala.setText("—");
        pannelloRiepilogo.add(lblValoreSala, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblVoceData = new JLabel();
        lblVoceData.setForeground(new Color(-8882046));
        lblVoceData.setText("Data:");
        pannelloRiepilogo.add(lblVoceData, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblValoreData = new JLabel();
        Font lblValoreDataFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblValoreData.getFont());
        if (lblValoreDataFont != null) lblValoreData.setFont(lblValoreDataFont);
        lblValoreData.setText("—");
        pannelloRiepilogo.add(lblValoreData, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblVoceFascia = new JLabel();
        lblVoceFascia.setForeground(new Color(-8882046));
        lblVoceFascia.setText("Fascia Oraria:");
        pannelloRiepilogo.add(lblVoceFascia, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblValoreFascia = new JLabel();
        Font lblValoreFasciaFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblValoreFascia.getFont());
        if (lblValoreFasciaFont != null) lblValoreFascia.setFont(lblValoreFasciaFont);
        lblValoreFascia.setText("—");
        pannelloRiepilogo.add(lblValoreFascia, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblVoceArea = new JLabel();
        lblVoceArea.setForeground(new Color(-8882046));
        lblVoceArea.setText("Area:");
        pannelloRiepilogo.add(lblVoceArea, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblValoreArea = new JLabel();
        Font lblValoreAreaFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblValoreArea.getFont());
        if (lblValoreAreaFont != null) lblValoreArea.setFont(lblValoreAreaFont);
        lblValoreArea.setText("—");
        pannelloRiepilogo.add(lblValoreArea, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblVocePostazione = new JLabel();
        lblVocePostazione.setForeground(new Color(-8882046));
        lblVocePostazione.setText("Postazione:");
        pannelloRiepilogo.add(lblVocePostazione, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblValorePostazione = new JLabel();
        Font lblValorePostazioneFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblValorePostazione.getFont());
        if (lblValorePostazioneFont != null) lblValorePostazione.setFont(lblValorePostazioneFont);
        lblValorePostazione.setText("—");
        pannelloRiepilogo.add(lblValorePostazione, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloPromemoria = new JPanel();
        pannelloPromemoria.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, 8));
        pannelloPromemoria.setBackground(new Color(-1));
        pannelloPromemoria.setOpaque(true);
        cardRiepilogo.add(pannelloPromemoria, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblPromemoriaTitolo = new JLabel();
        Font lblPromemoriaTitoloFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblPromemoriaTitolo.getFont());
        if (lblPromemoriaTitoloFont != null) lblPromemoriaTitolo.setFont(lblPromemoriaTitoloFont);
        lblPromemoriaTitolo.setText("Promemoria importante:");
        pannelloPromemoria.add(lblPromemoriaTitolo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblPromemoria = new JLabel();
        lblPromemoria.setForeground(new Color(-6710887));
        lblPromemoria.setText("promemoria");
        pannelloPromemoria.add(lblPromemoria, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloAzioni = new JPanel();
        pannelloAzioni.setLayout(new GridLayoutManager(1, 3, new Insets(10, 0, 0, 0), -1, -1));
        pannelloAzioni.setBackground(new Color(-1));
        pannelloAzioni.setOpaque(true);
        cardRiepilogo.add(pannelloAzioni, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblIndietro = new JLabel();
        lblIndietro.setForeground(new Color(-6710887));
        lblIndietro.setText("← Indietro");
        pannelloAzioni.add(lblIndietro, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        pannelloAzioni.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnConferma = new JButton();
        btnConferma.setBackground(new Color(-12010632));
        btnConferma.setForeground(new Color(-1));
        btnConferma.setText("Conferma Prenotazione");
        pannelloAzioni.add(btnConferma, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        pannelloContenuto.add(spacer4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
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
        return riepilogoPane;
    }

}
