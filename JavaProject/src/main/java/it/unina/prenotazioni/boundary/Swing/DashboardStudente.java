package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.PrenotazioneDTO;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DashboardStudente {

    public static void main(String[] args){
        // test sulla singola interfaccia
        UtenteDTO utente = new UtenteDTO();
        utente.setId(2L);
        utente.setNome("Giovanni");
        utente.setCognome("Rossi");
        utente.setIdentificativo("N46001234");

        new DashboardStudente().apriDashboard(utente);
    }

    private static final Color VIOLA        = new Color(124, 115, 230);
    private static final Color TESTO_GRIGIO = new Color(120, 120, 130);

    // Campi legati al .form (i nomi devono corrispondere agli attributi binding="...")
    private JPanel      dashboardStudente;
    private JPanel      header;
    private JLabel      lblTitolo;
    private JButton     btnLogout;
    private JPanel      contentPanel;
    private JLabel      lblNome;
    private JLabel      lblMatricola;
    private JLabel      lblAccessi;
    private JSeparator  separatore;
    private JLabel      lblPrenotazioni;
    private JComboBox   comboOrdine;
    private JScrollPane scrollPrenotazioni;
    private JPanel      pannelloPrenotazioni;
    private JButton     btnNuovaPrenotazione;

    // Stato interno
    private UtenteDTO utente;
    private JFrame frameCorrente;
    private List<PrenotazioneDTO> prenotazioni = new ArrayList<>();

    public DashboardStudente() {
        // Styling aggiuntivo non configurabile nel form designer
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(VIOLA);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnNuovaPrenotazione.setFocusPainted(false);
        btnNuovaPrenotazione.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        scrollPrenotazioni.setBorder(null);
        scrollPrenotazioni.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPrenotazioni.getVerticalScrollBar().setUnitIncrement(16);

        comboOrdine.setModel(new DefaultComboBoxModel<>(new String[]{"data", "stato"}));
        comboOrdine.addActionListener(e -> render());

        btnNuovaPrenotazione.addActionListener(e -> {
            frameCorrente.dispose();
            new SelezionaDataAndSala(new StatoWizard(utente)).apriForm();
        });
        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
    }

    // ── PROFILO E STORICO ────────────────────────────────────────────────────

    private void carica() {
        try {
            UtenteDTO profilo = BibliotecaFacade.getInstance().visualizzaProfiloPersonale(utente.getId());
            lblNome.setText("Profilo Personale di " + profilo.getNome() + " " + profilo.getCognome());
            lblMatricola.setText("Matricola: " + profilo.getIdentificativo());
            lblAccessi.setText("Totale accessi: " + profilo.getNumeroTotaleAccessi());
            prenotazioni = BibliotecaFacade.getInstance().consultaStoricoPrenotazioni(utente.getId());
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
        render();
    }

    private void render() {
        String criterio = (String) comboOrdine.getSelectedItem();
        List<PrenotazioneDTO> lista = new ArrayList<>(prenotazioni);
        if ("stato".equals(criterio)) {
            lista.sort(Comparator.comparing(PrenotazioneDTO::getStato));
        } else {
            lista.sort(Comparator.comparing(
                    (PrenotazioneDTO p) -> p.getData() == null ? LocalDate.MIN : p.getData()).reversed());
        }

        pannelloPrenotazioni.removeAll();
        pannelloPrenotazioni.setLayout(new BorderLayout());
        pannelloPrenotazioni.setBackground(contentPanel.getBackground());

        if (lista.isEmpty()) {
            JLabel lblVuoto = new JLabel("Non hai ancora prenotazioni.", JLabel.CENTER);
            lblVuoto.setForeground(TESTO_GRIGIO);
            pannelloPrenotazioni.add(lblVuoto, BorderLayout.NORTH);
        } else {
            JPanel griglia = new JPanel(new GridLayout(0, 2, 15, 15));
            griglia.setBackground(contentPanel.getBackground());
            for (PrenotazioneDTO prenotazione : lista) {
                CardPrenotazione card = new CardPrenotazione(prenotazione);
                card.addCheckInListener(e -> checkin(prenotazione.getIdPrenotazione()));
                card.addAnnullaListener(e -> annulla(prenotazione.getIdPrenotazione()));
                griglia.add(card.getRoot());
            }
            pannelloPrenotazioni.add(griglia, BorderLayout.NORTH);
        }
        pannelloPrenotazioni.revalidate();
        pannelloPrenotazioni.repaint();
    }

    // ── AZIONI ───────────────────────────────────────────────────────────────

    private void checkin(Long idPrenotazione) {
        try {
            BibliotecaFacade.getInstance().effettuaCheckin(idPrenotazione);
            carica();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
    }

    private void annulla(Long idPrenotazione) {
        int scelta = JOptionPane.showConfirmDialog(dashboardStudente,
                "Confermi l'annullamento della prenotazione?",
                "Annulla prenotazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (scelta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            BibliotecaFacade.getInstance().annullaPrenotazione(idPrenotazione);
            carica();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
    }

    // ── APRI DASHBOARD ───────────────────────────────────────────────────────

    public JFrame apriDashboard(UtenteDTO utente) {
        this.utente = utente;
        frameCorrente = new JFrame("Dashboard Studente");
        frameCorrente.setContentPane(dashboardStudente);

        lblNome.setText("Profilo Personale di " + utente.getNome() + " " + utente.getCognome());
        lblMatricola.setText("Matricola: " + utente.getIdentificativo());
        lblAccessi.setText("Totale accessi: " + utente.getNumeroTotaleAccessi());
        carica();

        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);

        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        frameCorrente.pack();
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(dashboardStudente, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
}
