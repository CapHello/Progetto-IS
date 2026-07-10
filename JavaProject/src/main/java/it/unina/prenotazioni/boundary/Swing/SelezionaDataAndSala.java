package it.unina.prenotazioni.boundary.Swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.SalaStudioDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class SelezionaDataAndSala {

    private static final Color VIOLA = new Color(124, 115, 230);
    private static final Color OGGI_COLOR = new Color(255, 200, 80);
    private static final Color GRIGIO = new Color(200, 200, 200);

    // Campi legati al .form
    private JPanel selezionaDataSalaPane;
    private JPanel header;
    private JLabel lblTitolo;
    private JButton btnLogout;
    private JPanel pannelloSteps;
    private JLabel lblSteps;
    private JPanel pannelloContenuto;
    private JPanel cardData;
    private JLabel lblTitoloData;
    private JPanel pannelloCalendario;
    private JLabel lblLegenda;
    private JPanel cardSala;
    private JLabel lblTitoloSala;
    private JScrollPane scrollSale;
    private JPanel pannelloSale;
    private JPanel pannelloBottom;
    private JLabel lblIndietro;
    private JButton btnContinua;

    // Stato interno
    private final StatoWizard stato;
    private LocalDate meseCorrente;
    private JFrame frameCorrente;
    private ButtonGroup gruppoSale = new ButtonGroup();

    public SelezionaDataAndSala(StatoWizard stato) {
        this.stato = stato;
        meseCorrente = (stato.getData() != null ? stato.getData() : LocalDate.now(ZoneId.of("Europe/Rome"))).withDayOfMonth(1);

        // Styling non configurabile nel form designer
        StileWizard.stilizzaLogout(btnLogout);
        StileWizard.stilizzaBottone(btnContinua, VIOLA);
        lblSteps.setText(StileWizard.htmlSteps(1));

        cardData.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 225), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        cardSala.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 225), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        pannelloSale.setBackground(Color.WHITE);

        // Con molte sale la lista supera l'altezza della card: si scorre in verticale.
        scrollSale.setBorder(null);
        scrollSale.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollSale.getVerticalScrollBar().setUnitIncrement(16);

        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
        btnContinua.addActionListener(e -> continua());

        // Torna alla dashboard studente senza perdere l'utente loggato.
        StileWizard.stilizzaIndietro(lblIndietro);
        lblIndietro.setText("← Dashboard");
        lblIndietro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameCorrente.dispose();
                new DashboardStudente().apriDashboard(stato.getStudente());
            }
        });

        costruisciCalendario();
        if (stato.getData() != null) {
            aggiornaSale(stato.getData());
        }
    }

    // --- CALENDARIO ---

    private void costruisciCalendario() {
        pannelloCalendario.removeAll();
        pannelloCalendario.setLayout(new BorderLayout(0, 8));
        pannelloCalendario.setBackground(Color.WHITE);

        // Riga di navigazione: << Mese Anno >>
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(Color.WHITE);

        JButton btnPrev = creaBtnNav("<<");
        btnPrev.addActionListener(e -> {
            meseCorrente = meseCorrente.minusMonths(1);
            costruisciCalendario();
        });

        JButton btnNext = creaBtnNav(">>");
        btnNext.addActionListener(e -> {
            meseCorrente = meseCorrente.plusMonths(1);
            costruisciCalendario();
        });

        String nomeMese = meseCorrente.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
        nomeMese = Character.toUpperCase(nomeMese.charAt(0)) + nomeMese.substring(1);
        JLabel lblMeseAnno = new JLabel(nomeMese + " " + meseCorrente.getYear(), JLabel.CENTER);
        lblMeseAnno.setFont(new Font("SansSerif", Font.BOLD, 13));

        navPanel.add(btnPrev, BorderLayout.WEST);
        navPanel.add(lblMeseAnno, BorderLayout.CENTER);
        navPanel.add(btnNext, BorderLayout.EAST);

        // Griglia giorni: riga nomi + righe date
        JPanel gridPanel = new JPanel(new GridLayout(0, 7, 3, 3));
        gridPanel.setBackground(Color.WHITE);

        for (String g : new String[]{"L", "M", "M", "G", "V", "S", "D"}) {
            JLabel lbl = new JLabel(g, JLabel.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            lbl.setForeground(Color.GRAY);
            gridPanel.add(lbl);
        }

        // Celle vuote prima del primo giorno del mese
        int offset = meseCorrente.withDayOfMonth(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < offset; i++) gridPanel.add(new JLabel(""));

        LocalDate oggi = LocalDate.now(ZoneId.of("Europe/Rome"));
        for (int g = 1; g <= meseCorrente.lengthOfMonth(); g++) {
            LocalDate data = meseCorrente.withDayOfMonth(g);
            JButton btnG = new JButton(String.valueOf(g));
            btnG.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btnG.setFocusPainted(false);
            btnG.setBorderPainted(false);
            btnG.setOpaque(true);

            boolean weekend = data.getDayOfWeek() == DayOfWeek.SATURDAY
                    || data.getDayOfWeek() == DayOfWeek.SUNDAY;

            if (data.equals(stato.getData())) {
                btnG.setBackground(VIOLA);
                btnG.setForeground(Color.WHITE);
            } else if (data.isBefore(oggi) || weekend) {
                btnG.setBackground(Color.WHITE);
                btnG.setForeground(GRIGIO);
                btnG.setEnabled(false);
            } else if (data.equals(oggi)) {
                btnG.setBackground(OGGI_COLOR);
                btnG.setForeground(Color.BLACK);
            } else {
                btnG.setBackground(Color.WHITE);
                btnG.setForeground(Color.BLACK);
            }

            final LocalDate dataFinal = data;
            btnG.addActionListener(e -> {
                stato.setData(dataFinal);
                stato.setIdSala(null);
                stato.setNomeSala(null);
                costruisciCalendario();
                aggiornaSale(dataFinal);
            });
            gridPanel.add(btnG);
        }

        pannelloCalendario.add(navPanel, BorderLayout.NORTH);
        pannelloCalendario.add(gridPanel, BorderLayout.CENTER);
        pannelloCalendario.revalidate();
        pannelloCalendario.repaint();
    }

    private JButton creaBtnNav(String testo) {
        JButton btn = new JButton(testo);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- SALE ---

    private void aggiornaSale(LocalDate data) {
        pannelloSale.removeAll();
        pannelloSale.setLayout(new BoxLayout(pannelloSale, BoxLayout.Y_AXIS));
        pannelloSale.setBackground(Color.WHITE);
        gruppoSale = new ButtonGroup();

        List<SalaStudioDTO> sale;
        try {
            sale = BibliotecaFacade.getInstance().consultaSaleDisponibili(data);
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
            sale = List.of();
        }

        if (sale.isEmpty()) {
            JLabel lblVuoto = new JLabel("Nessuna sala disponibile per questa data.");
            lblVuoto.setForeground(StileWizard.GRIGIO_TESTO);
            lblVuoto.setAlignmentX(Component.LEFT_ALIGNMENT);
            pannelloSale.add(lblVuoto);
        }

        for (SalaStudioDTO sala : sale) {
            String descrizione = sala.getDescrizione() == null ? "" : sala.getDescrizione();
            JRadioButton radio = StileWizard.creaRadioCard("<html><b>" + sala.getNome()
                    + "</b><br><font color='gray'><small>" + sala.getNumeroPostazioniTotali()
                    + " postazioni totali · " + descrizione + "</small></font></html>");
            radio.setAlignmentX(Component.LEFT_ALIGNMENT);
            radio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            radio.addActionListener(e -> {
                stato.setIdSala(sala.getId());
                stato.setNomeSala(sala.getNome());
            });
            if (sala.getId().equals(stato.getIdSala())) {
                radio.setSelected(true);
            }
            gruppoSale.add(radio);
            pannelloSale.add(radio);
            pannelloSale.add(Box.createVerticalStrut(8));
        }
        pannelloSale.add(Box.createVerticalGlue());

        pannelloSale.revalidate();
        pannelloSale.repaint();
    }

    private void continua() {
        if (stato.getData() == null) {
            mostraErrore("Seleziona una data");
            return;
        }
        if (stato.getIdSala() == null) {
            mostraErrore("Seleziona una sala");
            return;
        }
        frameCorrente.dispose();
        new SelezionaFascia(stato).apriForm();
    }

    // --- APRI FORM ---

    public JFrame apriForm() {
        frameCorrente = new JFrame("Nuova Prenotazione");
        frameCorrente.setContentPane(selezionaDataSalaPane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(selezionaDataSalaPane, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
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
        selezionaDataSalaPane = new JPanel();
        selezionaDataSalaPane.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        selezionaDataSalaPane.setBackground(new Color(-5192482));
        header = new JPanel();
        header.setLayout(new GridLayoutManager(1, 3, new Insets(10, 15, 10, 15), -1, -1));
        header.setBackground(new Color(-8621082));
        header.setOpaque(true);
        selezionaDataSalaPane.add(header, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        selezionaDataSalaPane.add(pannelloSteps, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblSteps = new JLabel();
        lblSteps.setForeground(new Color(-6710887));
        lblSteps.setHorizontalAlignment(0);
        lblSteps.setText("1 Data  Sala  ──  2 Fascia Oraria  ──  3 Area  ──  4 Postazione  ──  5 Conferma");
        lblSteps.setDisplayedMnemonic(' ');
        lblSteps.setDisplayedMnemonicIndex(7);
        pannelloSteps.add(lblSteps, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloContenuto = new JPanel();
        pannelloContenuto.setLayout(new GridLayoutManager(1, 2, new Insets(10, 30, 10, 30), 15, -1));
        pannelloContenuto.setBackground(new Color(-5192482));
        pannelloContenuto.setOpaque(true);
        selezionaDataSalaPane.add(pannelloContenuto, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        cardData = new JPanel();
        cardData.setLayout(new GridLayoutManager(3, 1, new Insets(15, 15, 15, 15), -1, 8));
        cardData.setBackground(new Color(-1));
        cardData.setOpaque(true);
        pannelloContenuto.add(cardData, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblTitoloData = new JLabel();
        Font lblTitoloDataFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblTitoloData.getFont());
        if (lblTitoloDataFont != null) lblTitoloData.setFont(lblTitoloDataFont);
        lblTitoloData.setText("1.1 Scegli la data");
        cardData.add(lblTitoloData, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloCalendario = new JPanel();
        pannelloCalendario.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        pannelloCalendario.setBackground(new Color(-1));
        pannelloCalendario.setOpaque(true);
        cardData.add(pannelloCalendario, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        lblLegenda = new JLabel();
        Font lblLegendaFont = this.$$$getFont$$$(null, -1, 11, lblLegenda.getFont());
        if (lblLegendaFont != null) lblLegenda.setFont(lblLegendaFont);
        lblLegenda.setForeground(new Color(-6710887));
        lblLegenda.setText("● oggi   □ chiuso   ■ selezionato");
        cardData.add(lblLegenda, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cardSala = new JPanel();
        cardSala.setLayout(new GridLayoutManager(2, 1, new Insets(15, 15, 15, 15), -1, 8));
        cardSala.setBackground(new Color(-1));
        cardSala.setOpaque(true);
        pannelloContenuto.add(cardSala, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lblTitoloSala = new JLabel();
        Font lblTitoloSalaFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblTitoloSala.getFont());
        if (lblTitoloSalaFont != null) lblTitoloSala.setFont(lblTitoloSalaFont);
        lblTitoloSala.setText("1.2 Scegli la Sala Studio");
        cardSala.add(lblTitoloSala, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scrollSale = new JScrollPane();
        cardSala.add(scrollSale, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        pannelloSale = new JPanel();
        pannelloSale.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        pannelloSale.setBackground(new Color(-1));
        pannelloSale.setOpaque(true);
        scrollSale.setViewportView(pannelloSale);
        pannelloBottom = new JPanel();
        pannelloBottom.setLayout(new GridLayoutManager(1, 3, new Insets(10, 30, 20, 30), -1, -1));
        pannelloBottom.setBackground(new Color(-5192482));
        pannelloBottom.setOpaque(true);
        selezionaDataSalaPane.add(pannelloBottom, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblIndietro = new JLabel();
        lblIndietro.setForeground(new Color(-6710887));
        lblIndietro.setText("← Dashboard");
        pannelloBottom.add(lblIndietro, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        pannelloBottom.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnContinua = new JButton();
        btnContinua.setBackground(new Color(-8621082));
        btnContinua.setForeground(new Color(-1));
        btnContinua.setText("Continua");
        pannelloBottom.add(btnContinua, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        return selezionaDataSalaPane;
    }

}
