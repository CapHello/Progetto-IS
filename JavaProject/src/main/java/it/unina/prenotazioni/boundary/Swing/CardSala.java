package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.dto.SalaMonitoraggioDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class CardSala {

    private static final Color VERDE_BADGE  = new Color(72, 187, 120);
    private static final Color GIALLO_BADGE = new Color(236, 201, 75);
    private static final Color ROSSO_BADGE  = new Color(245, 101, 101);
    private static final Color ROSSO_AZIONE = new Color(229, 62, 62);

    // Campi legati al .form
    private JPanel  cardPane;
    private JLabel  lblNome;
    private JPanel  pannelloBadge;
    private JLabel  lblBadgeLiberi;
    private JLabel  lblBadgeAttivi;
    private JLabel  lblBadgeConfermati;
    private JPanel  pannelloAree;
    private JButton btnElimina;
    private JLabel  lblRimossa;

    public CardSala(SalaMonitoraggioDTO sala) {
        boolean attiva = sala.isAttiva();

        // Styling non configurabile nel form designer
        cardPane.setBorder(attiva
                ? new LineBorder(StileWizard.GRIGIO_BORDO, 1, true)
                : BorderFactory.createDashedBorder(new Color(204, 204, 204)));
        btnElimina.setForeground(ROSSO_AZIONE);
        btnElimina.setBackground(Color.WHITE);
        btnElimina.setFocusPainted(false);
        btnElimina.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnElimina.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ROSSO_AZIONE, 1, true),
                new EmptyBorder(6, 14, 6, 14)));

        lblNome.setText(attiva
                ? "<html><b>" + sala.getNomeSala() + "</b></html>"
                : "<html><b><font color='#999999'>" + sala.getNomeSala()
                        + "</font></b> <font color='#E53E3E' size='2'><b>(Eliminata)</b></font></html>");

        // badge grigi se la sala è stata eliminata
        stilizzaBadge(lblBadgeLiberi, sala.getPostiLiberi(), attiva ? VERDE_BADGE : Color.GRAY, "Posti liberi");
        stilizzaBadge(lblBadgeAttivi, sala.getPostiAttivi(), attiva ? GIALLO_BADGE : Color.GRAY, "Attive (non confermate)");
        stilizzaBadge(lblBadgeConfermati, sala.getPostiConfermati(), attiva ? ROSSO_BADGE : Color.GRAY, "Confermate (presenti)");

        pannelloAree.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 2));
        if (sala.getAree().isEmpty()) {
            pannelloAree.add(StileWizard.creaPill("nessuna area"));
        } else {
            for (String area : sala.getAree()) {
                pannelloAree.add(StileWizard.creaPill(area));
            }
        }

        btnElimina.setVisible(attiva);
        lblRimossa.setVisible(!attiva);
    }

    private void stilizzaBadge(JLabel badge, int valore, Color sfondo, String descrizione) {
        badge.setText(String.valueOf(valore));
        badge.setOpaque(true);
        badge.setBackground(sfondo);
        badge.setBorder(new EmptyBorder(4, 12, 4, 12));
        badge.setToolTipText(descrizione);
    }

    public JPanel getRoot() {
        return cardPane;
    }

    public void addEliminaListener(ActionListener listener) {
        btnElimina.addActionListener(listener);
    }
}
