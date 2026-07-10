package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.dto.PrenotazioneDTO;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;

public class PrenotazioneConfermata {

    public static void main(String[] args) {
        // test sulla singola interfaccia
        UtenteDTO utente = new UtenteDTO();
        utente.setId(1L);
        utente.setNome("Giovanni");
        utente.setCognome("Rossi");
        utente.setIdentificativo("N46001234");

        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setIdPrenotazione(9L);
        prenotazione.setStato("ATTIVA");
        prenotazione.setNomeSala("Sala1");
        prenotazione.setTipologiaArea("comune");
        prenotazione.setNumeroPostazione(1);
        prenotazione.setData(LocalDate.now());
        prenotazione.setFasciaOraria("08:30-10:30");

        new PrenotazioneConfermata(new StatoWizard(utente), prenotazione).apriForm();
    }

    // Campi legati al .form
    private JPanel  confermataPane;
    private JPanel  header;
    private JLabel  lblTitolo;
    private JButton btnLogout;
    private JPanel  pannelloSteps;
    private JLabel  lblSteps;
    private JPanel  pannelloContenuto;
    private JPanel  cardConfermata;
    private JPanel  pannelloIcona;
    private JLabel  lblTitolone;
    private JLabel  lblMessaggio;
    private JPanel  pannelloBox;
    private JLabel  lblBoxRiga1;
    private JLabel  lblBoxRiga2;
    private JButton btnDashboard;

    // Stato interno
    private final StatoWizard stato;
    private JFrame frameCorrente;

    public PrenotazioneConfermata(StatoWizard stato, PrenotazioneDTO prenotazione) {
        this.stato = stato;

        // Styling non configurabile nel form designer
        StileWizard.stilizzaLogout(btnLogout);
        StileWizard.stilizzaBottone(btnDashboard, StileWizard.VIOLA);
        StileWizard.bordaCard(cardConfermata);
        lblSteps.setText(StileWizard.htmlSteps(6));
        pannelloBox.setBorder(new LineBorder(StileWizard.GRIGIO_BORDO, 1, true));

        pannelloIcona.setLayout(new GridBagLayout());
        pannelloIcona.setBackground(Color.WHITE);
        pannelloIcona.add(new CerchioSpunta());

        lblMessaggio.setText("<html><div style='text-align:center;'>La prenotazione <b>#"
                + prenotazione.getIdPrenotazione() + "</b> è stata registrata in stato <b><font color='#3182CE'>"
                + prenotazione.getStato() + "</font></b>.</div></html>");
        lblBoxRiga1.setText(prenotazione.getNomeSala() + " · " + prenotazione.getTipologiaArea()
                + " · Postazione #" + prenotazione.getNumeroPostazione());
        lblBoxRiga2.setText("<html>" + StileWizard.formattaData(prenotazione.getData())
                + " &nbsp;|&nbsp; " + prenotazione.getFasciaOraria() + "</html>");

        btnLogout.addActionListener(e -> {
            frameCorrente.dispose();
            new Login().apriLogin();
        });
        btnDashboard.addActionListener(e -> {
            frameCorrente.dispose();
            new DashboardStudente().apriDashboard(stato.getStudente());
        });
    }

    // ── APRI FORM ────────────────────────────────────────────────────────────

    public JFrame apriForm() {
        frameCorrente = new JFrame("Prenotazione Confermata");
        frameCorrente.setContentPane(confermataPane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    private static class CerchioSpunta extends JPanel {

        CerchioSpunta() {
            setPreferredSize(new Dimension(90, 90));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(198, 246, 213));
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(56, 161, 105));
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int w = getWidth();
            int h = getHeight();
            g2.drawPolyline(
                    new int[]{Math.round(w * 0.30f), Math.round(w * 0.45f), Math.round(w * 0.72f)},
                    new int[]{Math.round(h * 0.52f), Math.round(h * 0.66f), Math.round(h * 0.36f)}, 3);
            g2.dispose();
        }
    }
}
