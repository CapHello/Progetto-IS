package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.SalaStudioDTO;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/** Step 1 del wizard di prenotazione: scelta della data e della sala disponibile (UC6). */
public class SelezionaDataAndSala {

    public static void main(String[] args) {
        // test sulla singola interfaccia: serve uno studente (id 1) nel DB per completare il flusso
        UtenteDTO utente = new UtenteDTO();
        utente.setId(1L);
        new SelezionaDataAndSala(new StatoWizard(utente)).apriForm();
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
    private final StatoWizard stato;
    private LocalDate meseCorrente;
    private JFrame frameCorrente;
    private ButtonGroup gruppoSale = new ButtonGroup();

    public SelezionaDataAndSala(StatoWizard stato) {
        this.stato = stato;
        // riparte dal mese della data già scelta quando si torna indietro dallo step 2
        meseCorrente = (stato.getData() != null ? stato.getData() : LocalDate.now()).withDayOfMonth(1);

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

        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
        btnContinua.addActionListener(e -> continua());

        // Costruisce il calendario e, al ritorno dallo step 2, ripropone le sale della data scelta
        costruisciCalendario();
        if (stato.getData() != null) {
            aggiornaSale(stato.getData());
        }
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

            boolean weekend = data.getDayOfWeek() == DayOfWeek.SATURDAY
                    || data.getDayOfWeek() == DayOfWeek.SUNDAY;

            if (data.equals(stato.getData())) {
                btnG.setBackground(VIOLA);
                btnG.setForeground(Color.WHITE);
            } else if (data.isBefore(oggi) || weekend) {
                // giorni passati e weekend non prenotabili (le sale aprono nei giorni feriali)
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
                // il cambio data invalida la sala scelta in precedenza (come nella GUI web)
                stato.setIdSala(null);
                stato.setNomeSala(null);
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

    // ── SALE (UC6) ───────────────────────────────────────────────────────────

    /** Sale aperte e con posti liberi nella data scelta, come radio-card selezionabili. */
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
                radio.setSelected(true); // riselezione al ritorno dallo step 2
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

    // ── APRI FORM ────────────────────────────────────────────────────────────

    public JFrame apriForm() {
        frameCorrente = new JFrame("Nuova Prenotazione");
        frameCorrente.setContentPane(selezionaDataSalaPane);
        frameCorrente.setSize(960, 700);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(true);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(selezionaDataSalaPane, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
}
