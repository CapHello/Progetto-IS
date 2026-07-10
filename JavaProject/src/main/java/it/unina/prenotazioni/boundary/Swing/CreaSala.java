package it.unina.prenotazioni.boundary.Swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.CreazioneSalaDTO;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreaSala {

    private static final String[] GIORNI = {"Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì"};

    // Campi legati al .form
    private JPanel creaSalaPane;
    private JPanel header;
    private JLabel lblTitolo;
    private JButton btnLogout;
    private JPanel pannelloContenuto;
    private JScrollPane scrollCrea;
    private JPanel cardCrea;
    private JLabel lblTitoloCard;
    private JLabel lblNome;
    private JTextField txtNome;
    private JLabel lblDescrizione;
    private JTextField txtDescrizione;
    private JLabel lblPostazioni;
    private JSpinner spinPostazioni;
    private JLabel lblAree;
    private JSpinner spinAree;
    private JPanel pannelloAree;
    private JLabel lblRiepilogoAree;
    private JLabel lblOrariTitolo;
    private JPanel pannelloOrari;
    private JTextField txtAperturaLunedi;
    private JTextField txtChiusuraLunedi;
    private JTextField txtAperturaMartedi;
    private JTextField txtChiusuraMartedi;
    private JTextField txtAperturaMercoledi;
    private JTextField txtChiusuraMercoledi;
    private JTextField txtAperturaGiovedi;
    private JTextField txtChiusuraGiovedi;
    private JTextField txtAperturaVenerdi;
    private JTextField txtChiusuraVenerdi;
    private JLabel lblGrana;
    private JTextField txtGrana;
    private JPanel pannelloAzioni;
    private JLabel lblIndietro;
    private JButton btnProcedi;

    // Stato interno
    private final List<JTextField> nomiAree = new ArrayList<>();
    private final List<JSpinner> postiAree = new ArrayList<>();
    private JTextField[] orariApertura;
    private JTextField[] orariChiusura;
    private JFrame frameCorrente;

    public CreaSala() {
        // Styling non configurabile nel form designer
        StileWizard.stilizzaLogout(btnLogout);
        StileWizard.stilizzaBottone(btnProcedi, StileWizard.VIOLA);
        StileWizard.stilizzaIndietro(lblIndietro);
        StileWizard.bordaCard(cardCrea);
        StileWizard.stilizzaCampo(txtNome);
        StileWizard.stilizzaCampo(txtDescrizione);
        StileWizard.stilizzaCampo(txtGrana);

        scrollCrea.setBorder(null);
        scrollCrea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCrea.getVerticalScrollBar().setUnitIncrement(16);

        spinPostazioni.setModel(new SpinnerNumberModel(20, 1, 999, 1));
        spinAree.setModel(new SpinnerNumberModel(1, 0, 10, 1));
        spinPostazioni.addChangeListener(e -> aggiornaRiepilogo());
        spinAree.addChangeListener(e -> rigeneraAree());

        // campi orario del form in ordine Lun-Ven
        orariApertura = new JTextField[]{txtAperturaLunedi, txtAperturaMartedi,
                txtAperturaMercoledi, txtAperturaGiovedi, txtAperturaVenerdi};
        orariChiusura = new JTextField[]{txtChiusuraLunedi, txtChiusuraMartedi,
                txtChiusuraMercoledi, txtChiusuraGiovedi, txtChiusuraVenerdi};
        for (int i = 0; i < GIORNI.length; i++) {
            StileWizard.stilizzaCampo(orariApertura[i]);
            StileWizard.stilizzaCampo(orariChiusura[i]);
        }

        rigeneraAree();

        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
        lblIndietro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameCorrente.dispose();
                new DashboardBibliotecario().apriDashboard();
            }
        });
        btnProcedi.addActionListener(e -> procedi());
    }

    // --- AREE DINAMICHE ---

    private void rigeneraAree() {
        List<String> vecchiNomi = new ArrayList<>();
        List<Integer> vecchiPosti = new ArrayList<>();
        for (JTextField campo : nomiAree) vecchiNomi.add(campo.getText());
        for (JSpinner spinner : postiAree) vecchiPosti.add((Integer) spinner.getValue());
        nomiAree.clear();
        postiAree.clear();

        pannelloAree.removeAll();
        pannelloAree.setLayout(new BoxLayout(pannelloAree, BoxLayout.Y_AXIS));
        pannelloAree.setBackground(Color.WHITE);

        int numero = (Integer) spinAree.getValue();
        for (int i = 0; i < numero; i++) {
            JPanel riga = new JPanel(new BorderLayout(8, 0));
            riga.setBackground(Color.WHITE);
            riga.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

            JLabel etichetta = new JLabel("Area " + (i + 1));
            etichetta.setForeground(StileWizard.GRIGIO_TESTO);
            etichetta.setPreferredSize(new Dimension(60, 34));

            JTextField campoNome = new JTextField(i < vecchiNomi.size() ? vecchiNomi.get(i) : "");
            StileWizard.stilizzaCampo(campoNome);
            campoNome.setToolTipText("Nome area " + (i + 1) + " (es. Silenziosa)");

            JSpinner campoPosti = new JSpinner(new SpinnerNumberModel(
                    i < vecchiPosti.size() ? vecchiPosti.get(i) : 1, 1, 999, 1));
            campoPosti.setPreferredSize(new Dimension(90, 34));
            campoPosti.setToolTipText("Postazioni dell'area");
            campoPosti.addChangeListener(e -> aggiornaRiepilogo());

            riga.add(etichetta, BorderLayout.WEST);
            riga.add(campoNome, BorderLayout.CENTER);
            riga.add(campoPosti, BorderLayout.EAST);

            nomiAree.add(campoNome);
            postiAree.add(campoPosti);
            pannelloAree.add(riga);
            pannelloAree.add(Box.createVerticalStrut(6));
        }
        pannelloAree.revalidate();
        pannelloAree.repaint();
        aggiornaRiepilogo();
    }

    private void aggiornaRiepilogo() {
        int totale = (Integer) spinPostazioni.getValue();
        int somma = 0;
        for (JSpinner spinner : postiAree) {
            somma += (Integer) spinner.getValue();
        }
        int comune = totale - somma;
        if (comune < 0) {
            lblRiepilogoAree.setForeground(new Color(229, 62, 62));
            lblRiepilogoAree.setText("Le aree specifiche occupano " + somma + "/" + totale);
        } else {
            lblRiepilogoAree.setForeground(StileWizard.GRIGIO_TESTO);
            lblRiepilogoAree.setText("Aree specifiche: " + somma + " postazioni. Area comune: " + comune + ".");
        }
    }

    // --- CREAZIONE ---

    private int granaInMinuti(String testo) {
        String s = testo.trim().toLowerCase().replace(" ", "");
        if (s.matches("\\d+")) {
            return Integer.parseInt(s);
        }
        int minuti = 0;
        Matcher ore = Pattern.compile("(\\d+)h").matcher(s);
        if (ore.find()) minuti += Integer.parseInt(ore.group(1)) * 60;
        Matcher min = Pattern.compile("(\\d+)m").matcher(s);
        if (min.find()) minuti += Integer.parseInt(min.group(1));
        return minuti;
    }

    private void procedi() {
        int granaMinuti = granaInMinuti(txtGrana.getText());
        if (granaMinuti < 1) {
            mostraErrore("Grana non valida (usa es. 30m, 1h, 90)");
            return;
        }

        int totale = (Integer) spinPostazioni.getValue();
        List<String> tipologie = new ArrayList<>();
        List<Integer> postazioniAree = new ArrayList<>();
        int somma = 0;
        for (int i = 0; i < nomiAree.size(); i++) {
            String nomeArea = nomiAree.get(i).getText().trim();
            int posti = (Integer) postiAree.get(i).getValue();
            if (nomeArea.isEmpty()) {
                mostraErrore("Inserisci il nome dell'area " + (i + 1));
                return;
            }
            if (nomeArea.equalsIgnoreCase("comune")) {
                mostraErrore("Il nome \"comune\" è riservato all'area di default");
                return;
            }
            tipologie.add(nomeArea);
            postazioniAree.add(posti);
            somma += posti;
        }
        if (totale - somma < 0) {
            mostraErrore("Le aree specifiche occupano più postazioni di quelle effettivamente disponibili");
            return;
        }

        List<String> aperture = new ArrayList<>();
        List<String> chiusure = new ArrayList<>();
        for (int i = 0; i < GIORNI.length; i++) {
            aperture.add(orariApertura[i].getText().trim());
            chiusure.add(orariChiusura[i].getText().trim());
        }

        CreazioneSalaDTO richiesta = new CreazioneSalaDTO();
        richiesta.setNome(txtNome.getText().trim());
        richiesta.setDescrizione(txtDescrizione.getText().trim());
        richiesta.setNumeroPostazioni(totale);
        richiesta.setOrariApertura(aperture);
        richiesta.setOrariChiusura(chiusure);
        richiesta.setGranaMinuti(granaMinuti);
        richiesta.setTipologie(tipologie);
        richiesta.setPostazioniAree(postazioniAree);

        try {
            BibliotecaFacade.getInstance().creaSalaStudio(richiesta);
            JOptionPane.showMessageDialog(creaSalaPane, "Sala creata con successo!",
                    "Crea sala", JOptionPane.INFORMATION_MESSAGE);
            frameCorrente.dispose();
            new DashboardBibliotecario().apriDashboard();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
    }

    // --- APRI FORM ---

    public JFrame apriForm() {
        frameCorrente = new JFrame("Crea nuova Sala Studio");
        frameCorrente.setContentPane(creaSalaPane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(creaSalaPane, messaggio, "Errore creazione sala", JOptionPane.ERROR_MESSAGE);
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
        creaSalaPane = new JPanel();
        creaSalaPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        creaSalaPane.setBackground(new Color(-5192482));
        header = new JPanel();
        header.setLayout(new GridLayoutManager(1, 3, new Insets(10, 15, 10, 15), -1, -1));
        header.setBackground(new Color(-8621082));
        header.setOpaque(true);
        creaSalaPane.add(header, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        pannelloContenuto.setLayout(new GridLayoutManager(1, 3, new Insets(15, 30, 20, 30), -1, -1));
        pannelloContenuto.setBackground(new Color(-5192482));
        pannelloContenuto.setOpaque(true);
        creaSalaPane.add(pannelloContenuto, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        pannelloContenuto.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        scrollCrea = new JScrollPane();
        pannelloContenuto.add(scrollCrea, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(680, 840), null, 0, false));
        cardCrea = new JPanel();
        cardCrea.setLayout(new GridLayoutManager(11, 2, new Insets(0, 0, 0, 0), 10, 12));
        cardCrea.setBackground(new Color(-1));
        cardCrea.setOpaque(true);
        scrollCrea.setViewportView(cardCrea);
        lblTitoloCard = new JLabel();
        Font lblTitoloCardFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 18, lblTitoloCard.getFont());
        if (lblTitoloCardFont != null) lblTitoloCard.setFont(lblTitoloCardFont);
        lblTitoloCard.setText("Crea nuova Sala Studio");
        cardCrea.add(lblTitoloCard, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblNome = new JLabel();
        lblNome.setText("Nome Sala");
        cardCrea.add(lblNome, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(230, -1), null, 0, false));
        txtNome = new JTextField();
        txtNome.setText("");
        cardCrea.add(txtNome, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        lblDescrizione = new JLabel();
        lblDescrizione.setText("Descrizione");
        cardCrea.add(lblDescrizione, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtDescrizione = new JTextField();
        txtDescrizione.setText("");
        cardCrea.add(txtDescrizione, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        lblPostazioni = new JLabel();
        lblPostazioni.setText("Numero postazioni totali");
        cardCrea.add(lblPostazioni, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinPostazioni = new JSpinner();
        cardCrea.add(spinPostazioni, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(110, 34), null, 0, false));
        lblAree = new JLabel();
        lblAree.setText("Aree specifiche (oltre alla comune)");
        cardCrea.add(lblAree, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinAree = new JSpinner();
        cardCrea.add(spinAree, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(110, 34), null, 0, false));
        pannelloAree = new JPanel();
        pannelloAree.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        pannelloAree.setBackground(new Color(-1));
        pannelloAree.setOpaque(true);
        cardCrea.add(pannelloAree, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblRiepilogoAree = new JLabel();
        lblRiepilogoAree.setForeground(new Color(-6710887));
        lblRiepilogoAree.setText("Aree specifiche: 0 postazioni.");
        cardCrea.add(lblRiepilogoAree, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblOrariTitolo = new JLabel();
        Font lblOrariTitoloFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, lblOrariTitolo.getFont());
        if (lblOrariTitoloFont != null) lblOrariTitolo.setFont(lblOrariTitoloFont);
        lblOrariTitolo.setText("Orari della settimana (Lun-Ven)");
        cardCrea.add(lblOrariTitolo, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pannelloOrari = new JPanel();
        pannelloOrari.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), 8, 6));
        pannelloOrari.setBackground(new Color(-1));
        pannelloOrari.setOpaque(true);
        cardCrea.add(pannelloOrari, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("");
        pannelloOrari.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(90, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$("SansSerif", Font.BOLD, 11, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setForeground(new Color(-6710887));
        label2.setText("Apertura");
        pannelloOrari.add(label2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$("SansSerif", Font.BOLD, 11, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setForeground(new Color(-6710887));
        label3.setText("Chiusura");
        pannelloOrari.add(label3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setForeground(new Color(-11905688));
        label4.setText("Lunedì");
        pannelloOrari.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtAperturaLunedi = new JTextField();
        txtAperturaLunedi.setText("08:30");
        pannelloOrari.add(txtAperturaLunedi, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        txtChiusuraLunedi = new JTextField();
        txtChiusuraLunedi.setText("18:30");
        pannelloOrari.add(txtChiusuraLunedi, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setForeground(new Color(-11905688));
        label5.setText("Martedì");
        pannelloOrari.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtAperturaMartedi = new JTextField();
        txtAperturaMartedi.setText("08:30");
        pannelloOrari.add(txtAperturaMartedi, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        txtChiusuraMartedi = new JTextField();
        txtChiusuraMartedi.setText("18:30");
        pannelloOrari.add(txtChiusuraMartedi, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setForeground(new Color(-11905688));
        label6.setText("Mercoledì");
        pannelloOrari.add(label6, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtAperturaMercoledi = new JTextField();
        txtAperturaMercoledi.setText("08:30");
        pannelloOrari.add(txtAperturaMercoledi, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        txtChiusuraMercoledi = new JTextField();
        txtChiusuraMercoledi.setText("18:30");
        pannelloOrari.add(txtChiusuraMercoledi, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setForeground(new Color(-11905688));
        label7.setText("Giovedì");
        pannelloOrari.add(label7, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtAperturaGiovedi = new JTextField();
        txtAperturaGiovedi.setText("08:30");
        pannelloOrari.add(txtAperturaGiovedi, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        txtChiusuraGiovedi = new JTextField();
        txtChiusuraGiovedi.setText("18:30");
        pannelloOrari.add(txtChiusuraGiovedi, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setForeground(new Color(-11905688));
        label8.setText("Venerdì");
        pannelloOrari.add(label8, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtAperturaVenerdi = new JTextField();
        txtAperturaVenerdi.setText("08:30");
        pannelloOrari.add(txtAperturaVenerdi, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        txtChiusuraVenerdi = new JTextField();
        txtChiusuraVenerdi.setText("18:30");
        pannelloOrari.add(txtChiusuraVenerdi, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 38), null, 0, false));
        lblGrana = new JLabel();
        lblGrana.setText("Grana suddivisioni (es. 30m, 1h)");
        cardCrea.add(lblGrana, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtGrana = new JTextField();
        txtGrana.setText("2h");
        cardCrea.add(txtGrana, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(110, 38), null, 0, false));
        pannelloAzioni = new JPanel();
        pannelloAzioni.setLayout(new GridLayoutManager(1, 3, new Insets(10, 0, 0, 0), -1, -1));
        pannelloAzioni.setBackground(new Color(-1));
        pannelloAzioni.setOpaque(true);
        cardCrea.add(pannelloAzioni, new GridConstraints(10, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblIndietro = new JLabel();
        lblIndietro.setForeground(new Color(-6710887));
        lblIndietro.setText("← Indietro");
        pannelloAzioni.add(lblIndietro, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        pannelloAzioni.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnProcedi = new JButton();
        btnProcedi.setBackground(new Color(-8621082));
        btnProcedi.setForeground(new Color(-1));
        btnProcedi.setText("Procedi");
        pannelloAzioni.add(btnProcedi, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        return creaSalaPane;
    }

}
