package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.dto.PrenotazioneDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

public class CardPrenotazione {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Color ROSSO_AZIONE = new Color(229, 62, 62);

    // Campi legati al .form
    private JPanel  cardPane;
    private JPanel  intestazione;
    private JLabel  lblId;
    private JLabel  lblStato;
    private JLabel  lblData;
    private JLabel  lblOrario;
    private JLabel  lblPosizione;
    private JPanel  pannelloAzioni;
    private JButton btnCheckIn;
    private JButton btnAnnulla;

    public CardPrenotazione(PrenotazioneDTO prenotazione) {
        // Styling non configurabile nel form designer
        cardPane.setBorder(new LineBorder(StileWizard.GRIGIO_BORDO, 1, true));
        StileWizard.stilizzaBottone(btnCheckIn, StileWizard.VERDE);
        btnCheckIn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnAnnulla.setForeground(ROSSO_AZIONE);
        btnAnnulla.setBackground(Color.WHITE);
        btnAnnulla.setFocusPainted(false);
        btnAnnulla.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAnnulla.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ROSSO_AZIONE, 1, true),
                new EmptyBorder(5, 12, 5, 12)));

        lblId.setText("Prenotazione #" + prenotazione.getIdPrenotazione());
        String dataFormattata = prenotazione.getData() == null ? "" : prenotazione.getData().format(FORMATO_DATA);
        lblData.setText("Data: " + dataFormattata);
        lblOrario.setText("Orario: " + prenotazione.getFasciaOraria());
        lblPosizione.setText(prenotazione.getNomeSala() + " - " + prenotazione.getTipologiaArea()
                + " - Postazione #" + prenotazione.getNumeroPostazione());
        stilizzaBadge(prenotazione.getStato());

        // azioni disponibili solo sulle prenotazioni ATTIVE
        pannelloAzioni.setVisible("ATTIVA".equals(prenotazione.getStato()));
    }

    private void stilizzaBadge(String stato) {
        Color sfondo;
        Color testo;
        switch (stato == null ? "" : stato) {
            case "ATTIVA"     -> { sfondo = new Color(235, 244, 255); testo = new Color(49, 130, 206); }
            case "CONFERMATA" -> { sfondo = new Color(198, 246, 213); testo = new Color(56, 161, 105); }
            case "ANNULLATA"  -> { sfondo = new Color(254, 215, 215); testo = new Color(229, 62, 62); }
            default           -> { sfondo = new Color(226, 232, 240); testo = new Color(74, 85, 104); }
        }
        lblStato.setText(stato);
        lblStato.setOpaque(true);
        lblStato.setBackground(sfondo);
        lblStato.setForeground(testo);
        lblStato.setBorder(new EmptyBorder(3, 10, 3, 10));
    }

    public JPanel getRoot() {
        return cardPane;
    }

    public void addCheckInListener(ActionListener listener) {
        btnCheckIn.addActionListener(listener);
    }

    public void addAnnullaListener(ActionListener listener) {
        btnAnnulla.addActionListener(listener);
    }
}
