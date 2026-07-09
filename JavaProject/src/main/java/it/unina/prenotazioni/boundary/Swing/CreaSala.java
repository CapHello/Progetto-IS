package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.CreazioneSalaDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creazione di una nuova sala studio (UC3), replica Swing di crea-sala.html:
 * orari per i 5 giorni feriali e aree specifiche dinamiche; l'area "comune" di
 * default è sempre presente (V19) e riceve le postazioni non assegnate alle aree.
 * Le validazioni di merito restano nel controller (GestoreSale).
 */
public class CreaSala {

    public static void main(String[] args) {
        // test sulla singola interfaccia: MySQL serve solo alla pressione di "Procedi"
        new CreaSala().apriForm();
    }

    private static final String[] GIORNI = {"Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì"};

    // Campi legati al .form
    private JPanel      creaSalaPane;
    private JPanel      header;
    private JLabel      lblTitolo;
    private JButton     btnLogout;
    private JPanel      pannelloContenuto;
    private JScrollPane scrollCrea;
    private JPanel      cardCrea;
    private JLabel      lblTitoloCard;
    private JLabel      lblNome;
    private JTextField  txtNome;
    private JLabel      lblDescrizione;
    private JTextField  txtDescrizione;
    private JLabel      lblPostazioni;
    private JSpinner    spinPostazioni;
    private JLabel      lblAree;
    private JSpinner    spinAree;
    private JPanel      pannelloAree;
    private JLabel      lblRiepilogoAree;
    private JLabel      lblOrariTitolo;
    private JPanel      pannelloOrari;
    private JLabel      lblGrana;
    private JTextField  txtGrana;
    private JPanel      pannelloAzioni;
    private JLabel      lblIndietro;
    private JButton     btnProcedi;

    // Stato interno: campi delle righe-area e degli orari creati dinamicamente
    private final List<JTextField> nomiAree  = new ArrayList<>();
    private final List<JSpinner>   postiAree = new ArrayList<>();
    private final JTextField[] orariApertura = new JTextField[GIORNI.length];
    private final JTextField[] orariChiusura = new JTextField[GIORNI.length];
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

        costruisciOrari();
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

    // ── AREE DINAMICHE ───────────────────────────────────────────────────────

    /** Rigenera le righe-area in base al numero scelto, preservando i valori già inseriti (come la GUI web). */
    private void rigeneraAree() {
        List<String>  vecchiNomi  = new ArrayList<>();
        List<Integer> vecchiPosti = new ArrayList<>();
        for (JTextField campo : nomiAree)  vecchiNomi.add(campo.getText());
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

    /** L'area "comune" riceve le postazioni non assegnate: il riepilogo mostra il resto in tempo reale. */
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

    // ── ORARI SETTIMANALI ────────────────────────────────────────────────────

    private void costruisciOrari() {
        pannelloOrari.setLayout(new GridLayout(0, 3, 8, 6));
        pannelloOrari.setBackground(Color.WHITE);

        JLabel vuota = new JLabel("");
        JLabel apertura = new JLabel("Apertura");
        JLabel chiusura = new JLabel("Chiusura");
        for (JLabel intestazione : new JLabel[]{vuota, apertura, chiusura}) {
            intestazione.setForeground(StileWizard.GRIGIO_TESTO);
            intestazione.setFont(new Font("SansSerif", Font.BOLD, 11));
            pannelloOrari.add(intestazione);
        }

        for (int i = 0; i < GIORNI.length; i++) {
            JLabel giorno = new JLabel(GIORNI[i]);
            giorno.setForeground(new Color(74, 85, 104));
            pannelloOrari.add(giorno);

            orariApertura[i] = new JTextField("08:30");
            StileWizard.stilizzaCampo(orariApertura[i]);
            pannelloOrari.add(orariApertura[i]);

            orariChiusura[i] = new JTextField("18:30");
            StileWizard.stilizzaCampo(orariChiusura[i]);
            pannelloOrari.add(orariChiusura[i]);
        }
    }

    // ── CREAZIONE (UC3) ──────────────────────────────────────────────────────

    /** Converte una grana testuale ("30m", "1h", "1h30m", "90") in minuti, come la GUI web. */
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
        List<String>  tipologie      = new ArrayList<>();
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

    // ── APRI FORM ────────────────────────────────────────────────────────────

    public JFrame apriForm() {
        frameCorrente = new JFrame("Crea nuova Sala Studio");
        frameCorrente.setContentPane(creaSalaPane);
        frameCorrente.setSize(960, 760);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(true);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(creaSalaPane, messaggio, "Errore creazione sala", JOptionPane.ERROR_MESSAGE);
    }
}
