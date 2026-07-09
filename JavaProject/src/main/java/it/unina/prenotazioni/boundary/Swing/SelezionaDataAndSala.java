package it.unina.prenotazioni.boundary.Swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class SelezionaDataAndSala {

    public static void main(String[] args) {
        SelezionaDataAndSala selezionaDataAndSala = new SelezionaDataAndSala();

        selezionaDataAndSala.apriForm();
    }

    private static final Color VIOLA       = new Color(124, 115, 230);
    private static final Color OGGI_COLOR  = new Color(255, 200, 80);
    private static final Color GRIGIO      = new Color(200, 200, 200);

    // Campi legati al .form
    private JPanel  selezionaDataSalaPane;
    private JPanel  header;
    private JLabel  lblTitolo;
    private JButton btnLogout;
    private JPanel  pannelloSteps;
    private JLabel  lblSteps;
    private JPanel  pannelloContenuto;
    private JPanel  cardData;
    private JLabel  lblTitoloData;
    private JPanel  pannelloCalendario;
    private JLabel  lblLegenda;
    private JPanel  cardSala;
    private JLabel  lblTitoloSala;
    private JPanel  pannelloSale;
    private JPanel  pannelloBottom;
    private JButton btnContinua;

    // Stato interno
    private LocalDate meseCorrente    = LocalDate.now().withDayOfMonth(1);
    private LocalDate dataSelezionata = null;
    private ButtonGroup gruppoSale    = new ButtonGroup();

    public SelezionaDataAndSala() {
        // Styling header
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(VIOLA);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Styling btnContinua
        btnContinua.setBackground(VIOLA);
        btnContinua.setForeground(Color.WHITE);
        btnContinua.setFocusPainted(false);
        btnContinua.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Bordi card (non configurabili nel form designer)
        cardData.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 225), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        cardSala.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 225), 1, true),
                new EmptyBorder(15, 15, 15, 15)));

        pannelloSale.setBackground(Color.WHITE);

        // Costruisce il calendario per il mese corrente
        costruisciCalendario();
    }

    // ── CALENDARIO ───────────────────────────────────────────────────────────

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

        navPanel.add(btnPrev,     BorderLayout.WEST);
        navPanel.add(lblMeseAnno, BorderLayout.CENTER);
        navPanel.add(btnNext,     BorderLayout.EAST);

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

        LocalDate oggi = LocalDate.now();
        for (int g = 1; g <= meseCorrente.lengthOfMonth(); g++) {
            LocalDate data = meseCorrente.withDayOfMonth(g);
            JButton btnG = new JButton(String.valueOf(g));
            btnG.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btnG.setFocusPainted(false);
            btnG.setBorderPainted(false);
            btnG.setOpaque(true);

            if (data.equals(dataSelezionata)) {
                btnG.setBackground(VIOLA);
                btnG.setForeground(Color.WHITE);
            } else if (data.equals(oggi)) {
                btnG.setBackground(OGGI_COLOR);
                btnG.setForeground(Color.BLACK);
            } else if (data.isBefore(oggi)) {
                btnG.setBackground(Color.WHITE);
                btnG.setForeground(GRIGIO);
                btnG.setEnabled(false);
            } else {
                btnG.setBackground(Color.WHITE);
                btnG.setForeground(Color.BLACK);
            }

            final LocalDate dataFinal = data;
            btnG.addActionListener(e -> {
                dataSelezionata = dataFinal;
                costruisciCalendario();
                aggiornaSale(dataFinal);
            });
            gridPanel.add(btnG);
        }

        pannelloCalendario.add(navPanel,  BorderLayout.NORTH);
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

    // ── SALE (STUB) ──────────────────────────────────────────────────────────

    private void aggiornaSale(LocalDate data) {
        pannelloSale.removeAll();
        pannelloSale.setLayout(new BoxLayout(pannelloSale, BoxLayout.Y_AXIS));
        pannelloSale.setBackground(Color.WHITE);
        gruppoSale = new ButtonGroup();

        // STUB: una sala fissa — sostituire con chiamata al controller
        JRadioButton rbSala1 = new JRadioButton(
                "<html><b>Sala1</b><br><font color='gray'><small>20 postazioni totali · nessuna</small></font></html>");
        rbSala1.setBackground(Color.WHITE);
        rbSala1.setFont(new Font("SansSerif", Font.PLAIN, 13));
        rbSala1.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 225), 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        rbSala1.setAlignmentX(Component.LEFT_ALIGNMENT);
        rbSala1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        gruppoSale.add(rbSala1);
        pannelloSale.add(rbSala1);
        pannelloSale.add(Box.createVerticalGlue());

        pannelloSale.revalidate();
        pannelloSale.repaint();
    }

    // ── APRI FORM ────────────────────────────────────────────────────────────

    public JFrame apriForm() {
        JFrame frame = new JFrame("Nuova Prenotazione");
        frame.setContentPane(selezionaDataSalaPane);
        frame.setSize(960, 700);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);
        return frame;
    }

    // ── GETTER / LISTENER per il controller ──────────────────────────────────

    public void addLogoutListener(ActionListener l)   { btnLogout.addActionListener(l); }
    public void addContinuaListener(ActionListener l) { btnContinua.addActionListener(l); }
    public LocalDate getDataSelezionata()             { return dataSelezionata; }
    public ButtonGroup getGruppoSale()                { return gruppoSale; }
}
