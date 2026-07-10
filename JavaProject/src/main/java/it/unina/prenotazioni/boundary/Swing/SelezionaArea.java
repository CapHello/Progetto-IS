package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.AreaDettaglioDTO;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

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
    private JPanel  selezionaAreaPane;
    private JPanel  header;
    private JLabel  lblTitolo;
    private JButton btnLogout;
    private JPanel  pannelloSteps;
    private JLabel  lblSteps;
    private JPanel  pannelloContenuto;
    private JPanel  cardArea;
    private JLabel  lblTitoloCard;
    private JLabel  lblSottotitolo;
    private JPanel  pannelloAree;
    private JPanel  pannelloAzioni;
    private JLabel  lblIndietro;
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
}
