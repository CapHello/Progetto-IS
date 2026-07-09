package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.dto.AreaDettaglioDTO;
import it.unina.prenotazioni.dto.DettaglioSalaDTO;
import it.unina.prenotazioni.dto.PostazioneDTO;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

/**
 * Step 4 del wizard di prenotazione: scelta della postazione nell'area (UC7).
 * Non interroga il controller: usa il DettaglioSalaDTO già caricato allo step 3
 * (come la GUI web); idPostazione = 0 significa "assegnazione automatica".
 */
public class SelezionaPostazione {

    public static void main(String[] args) {
        // test sulla singola interfaccia con un dettaglio fittizio (nessun accesso al DB)
        UtenteDTO utente = new UtenteDTO();
        utente.setId(1L);
        StatoWizard stato = new StatoWizard(utente);
        stato.setData(LocalDate.now());
        stato.setIdSala(1L);
        stato.setNomeSala("Sala1");
        stato.setIdFascia(1L);
        stato.setEtichettaFascia("08:30-10:30");
        stato.setIdArea(1L);
        stato.setTipologiaArea("comune");

        DettaglioSalaDTO dettaglio = new DettaglioSalaDTO();
        AreaDettaglioDTO area = new AreaDettaglioDTO(1L, "comune");
        int liberi = 0;
        for (int i = 1; i <= 20; i++) {
            PostazioneDTO posto = new PostazioneDTO();
            posto.setId((long) i);
            posto.setNumero(i);
            posto.setTipologiaArea("comune");
            posto.setDisponibile(i % 7 != 0);
            if (posto.isDisponibile()) liberi++;
            area.getPostazioni().add(posto);
        }
        area.setPostiDisponibili(liberi);
        dettaglio.getAree().add(area);
        stato.setDettaglio(dettaglio);

        new SelezionaPostazione(stato).apriForm();
    }

    // Campi legati al .form
    private JPanel  selezionaPostazionePane;
    private JPanel  header;
    private JLabel  lblTitolo;
    private JButton btnLogout;
    private JPanel  pannelloSteps;
    private JLabel  lblSteps;
    private JPanel  pannelloContenuto;
    private JPanel  pannelloSplit;
    private JPanel  cardPosti;
    private JLabel  lblTitoloCard;
    private JLabel  lblSottotitolo;
    private JPanel  pannelloPosti;
    private JPanel  colonnaDestra;
    private JPanel  cardDettaglio;
    private JLabel  lblTitoloDettaglio;
    private JLabel  lblDettaglio;
    private JPanel  cardAuto;
    private JLabel  lblOppure;
    private JRadioButton rbAutomatica;
    private JButton btnContinua;
    private JPanel  pannelloAzioni;
    private JLabel  lblIndietro;

    // Stato interno
    private final StatoWizard stato;
    private JFrame frameCorrente;
    private JButton bottoneSelezionato; // posto evidenziato in viola nella griglia

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

    // ── GRIGLIA POSTI ────────────────────────────────────────────────────────

    /** Griglia dei posti dell'area scelta: verdi i liberi, rossi gli occupati (non cliccabili). */
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

    // ── APRI FORM ────────────────────────────────────────────────────────────

    public JFrame apriForm() {
        frameCorrente = new JFrame("Nuova Prenotazione");
        frameCorrente.setContentPane(selezionaPostazionePane);
        frameCorrente.setSize(960, 700);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(true);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(selezionaPostazionePane, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
}
