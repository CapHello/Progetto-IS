package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.FasciaDisponibileDTO;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

/** Step 2 del wizard di prenotazione: scelta della fascia oraria con i posti liberi (UC7). */
public class SelezionaFascia {

    public static void main(String[] args) {
        // test sulla singola interfaccia: servono nel DB una sala (id 1) aperta nella data odierna
        UtenteDTO utente = new UtenteDTO();
        utente.setId(1L);
        StatoWizard stato = new StatoWizard(utente);
        stato.setData(LocalDate.now());
        stato.setIdSala(1L);
        stato.setNomeSala("Sala1");
        new SelezionaFascia(stato).apriForm();
    }

    // Campi legati al .form
    private JPanel  selezionaFasciaPane;
    private JPanel  header;
    private JLabel  lblTitolo;
    private JButton btnLogout;
    private JPanel  pannelloSteps;
    private JLabel  lblSteps;
    private JPanel  pannelloContenuto;
    private JPanel  cardFascia;
    private JLabel  lblTitoloCard;
    private JLabel  lblSottotitolo;
    private JPanel  pannelloFasce;
    private JPanel  pannelloAzioni;
    private JLabel  lblIndietro;
    private JButton btnContinua;

    // Stato interno
    private final StatoWizard stato;
    private JFrame frameCorrente;
    private ButtonGroup gruppoFasce = new ButtonGroup();

    public SelezionaFascia(StatoWizard stato) {
        this.stato = stato;

        // Styling non configurabile nel form designer
        StileWizard.stilizzaLogout(btnLogout);
        StileWizard.stilizzaBottone(btnContinua, StileWizard.VIOLA);
        StileWizard.stilizzaIndietro(lblIndietro);
        StileWizard.bordaCard(cardFascia);
        lblSteps.setText(StileWizard.htmlSteps(2));
        lblSottotitolo.setText("<html>Sala selezionata: <b>" + stato.getNomeSala()
                + "</b> &nbsp;|&nbsp; data: <b>" + StileWizard.formattaData(stato.getData()) + "</b></html>");

        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
        lblIndietro.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameCorrente.dispose();
                new SelezionaDataAndSala(stato).apriForm();
            }
        });
        btnContinua.addActionListener(e -> continua());

        caricaFasce();
    }

    // ── FASCE ────────────────────────────────────────────────────────────────

    /** UC7: fasce prenotabili della sala nella data scelta; azzera la selezione a ogni ingresso (come la GUI web). */
    private void caricaFasce() {
        pannelloFasce.removeAll();
        pannelloFasce.setLayout(new BorderLayout());
        pannelloFasce.setBackground(Color.WHITE);
        gruppoFasce = new ButtonGroup();
        stato.setIdFascia(null);
        stato.setEtichettaFascia(null);

        List<FasciaDisponibileDTO> fasce;
        try {
            fasce = BibliotecaFacade.getInstance().getFasceDisponibili(stato.getIdSala(), stato.getData());
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
            fasce = List.of();
        }

        if (fasce.isEmpty()) {
            JLabel lblVuoto = new JLabel("Nessuna fascia disponibile.");
            lblVuoto.setForeground(StileWizard.GRIGIO_TESTO);
            pannelloFasce.add(lblVuoto, BorderLayout.NORTH);
        } else {
            JPanel griglia = new JPanel(new GridLayout(0, 4, 12, 12));
            griglia.setBackground(Color.WHITE);
            for (FasciaDisponibileDTO fascia : fasce) {
                JRadioButton radio = StileWizard.creaRadioCard("<html><b>" + fascia.getEtichetta()
                        + "</b><br><font color='gray'><small>" + fascia.getPostiDisponibili()
                        + " postazioni disponibili</small></font></html>");
                radio.addActionListener(e -> {
                    stato.setIdFascia(fascia.getIdFascia());
                    stato.setEtichettaFascia(fascia.getEtichetta());
                });
                gruppoFasce.add(radio);
                griglia.add(radio);
            }
            pannelloFasce.add(griglia, BorderLayout.NORTH);
        }
        pannelloFasce.revalidate();
        pannelloFasce.repaint();
    }

    private void continua() {
        if (stato.getIdFascia() == null) {
            mostraErrore("Seleziona una fascia oraria");
            return;
        }
        frameCorrente.dispose();
        new SelezionaArea(stato).apriForm();
    }

    // ── APRI FORM ────────────────────────────────────────────────────────────

    public JFrame apriForm() {
        frameCorrente = new JFrame("Nuova Prenotazione");
        frameCorrente.setContentPane(selezionaFasciaPane);
        frameCorrente.setSize(960, 700);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(true);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(selezionaFasciaPane, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
}
