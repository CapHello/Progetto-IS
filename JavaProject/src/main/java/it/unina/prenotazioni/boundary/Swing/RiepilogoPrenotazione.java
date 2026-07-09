package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.PrenotazioneDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Step 5 del wizard di prenotazione: riepilogo delle scelte e conferma (UC7).
 * Alla conferma invoca effettuaPrenotazione sulla Facade e, in caso di successo,
 * apre la schermata di prenotazione confermata.
 */
public class RiepilogoPrenotazione {

    // Campi legati al .form
    private JPanel  riepilogoPane;
    private JPanel  header;
    private JLabel  lblTitolo;
    private JButton btnLogout;
    private JPanel  pannelloSteps;
    private JLabel  lblSteps;
    private JPanel  pannelloContenuto;
    private JPanel  cardRiepilogo;
    private JLabel  lblTitoloCard;
    private JPanel  pannelloRiepilogo;
    private JPanel  pannelloPromemoria;
    private JLabel  lblPromemoriaTitolo;
    private JLabel  lblPromemoria;
    private JPanel  pannelloAzioni;
    private JLabel  lblIndietro;
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

        // Box promemoria con la barra viola a sinistra (come la GUI web)
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

        costruisciRiepilogo();

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

    // ── RIEPILOGO ────────────────────────────────────────────────────────────

    private void costruisciRiepilogo() {
        pannelloRiepilogo.setLayout(new BorderLayout());
        pannelloRiepilogo.setBackground(Color.WHITE);

        JPanel box = new JPanel(new GridLayout(5, 2, 10, 18));
        box.setBackground(StileWizard.GRIGIO_SFONDO);
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(StileWizard.GRIGIO_BORDO, 1, true),
                new EmptyBorder(24, 28, 24, 28)));

        aggiungiVoce(box, "Sala Studio:", stato.getNomeSala());
        aggiungiVoce(box, "Data:", StileWizard.formattaData(stato.getData()));
        aggiungiVoce(box, "Fascia Oraria:", stato.getEtichettaFascia());
        aggiungiVoce(box, "Area:", stato.getTipologiaArea());
        aggiungiVoce(box, "Postazione:", stato.getEtichettaPostazione());

        pannelloRiepilogo.add(box, BorderLayout.NORTH);
    }

    private void aggiungiVoce(JPanel box, String nome, String valore) {
        JLabel lblNome = new JLabel(nome);
        lblNome.setForeground(StileWizard.GRIGIO_TESTO);
        JLabel lblValore = new JLabel(valore);
        lblValore.setFont(new Font("SansSerif", Font.BOLD, 14));
        box.add(lblNome);
        box.add(lblValore);
    }

    // ── CONFERMA (UC7) ───────────────────────────────────────────────────────

    private void conferma() {
        try {
            PrenotazioneDTO prenotazione = BibliotecaFacade.getInstance().effettuaPrenotazione(
                    stato.getIdSala(), stato.getIdArea(), stato.getIdPostazione(),
                    stato.getData(), stato.getIdFascia(), stato.getStudente().getId());
            frameCorrente.dispose();
            new PrenotazioneConfermata(stato, prenotazione).apriForm();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
    }

    // ── APRI FORM ────────────────────────────────────────────────────────────

    public JFrame apriForm() {
        frameCorrente = new JFrame("Nuova Prenotazione");
        frameCorrente.setContentPane(riepilogoPane);
        frameCorrente.setSize(960, 700);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(true);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(riepilogoPane, messaggio, "Errore prenotazione", JOptionPane.ERROR_MESSAGE);
    }
}
