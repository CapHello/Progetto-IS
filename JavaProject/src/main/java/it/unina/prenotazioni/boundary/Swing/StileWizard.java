package it.unina.prenotazioni.boundary.Swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Costanti di colore e helper grafici condivisi dalle boundary Swing del wizard di
 * prenotazione (stessa palette dei css della GUI web).
 */
final class StileWizard {

    static final Color VIOLA         = new Color(124, 115, 230);
    static final Color VIOLA_CHIARO  = new Color(237, 242, 254); // radio-card selezionata
    static final Color PLACEHOLDER   = new Color(160, 160, 170); // testo segnaposto dei campi
    static final Color VERDE         = new Color(72, 187, 120);  // postazione libera / conferma
    static final Color ROSSO         = new Color(245, 101, 101); // postazione occupata
    static final Color GRIGIO_TESTO  = new Color(120, 120, 130);
    static final Color GRIGIO_BORDO  = new Color(220, 220, 228);
    static final Color GRIGIO_SFONDO = new Color(248, 250, 252); // box riepilogo/dettaglio

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN);

    private StileWizard() {}

    /** Data leggibile per le etichette, es. "10 luglio 2026". */
    static String formattaData(LocalDate data) {
        return data == null ? "" : data.format(FORMATO_DATA);
    }

    /**
     * Barra di avanzamento del wizard con lo step corrente in evidenza e i precedenti
     * in viola (equivalente della wizard-progress web); con corrente &gt; 5 sono tutti completati.
     */
    static String htmlSteps(int corrente) {
        String[] titoli = {"Data & Sala", "Fascia Oraria", "Area", "Postazione", "Conferma"};
        StringBuilder sb = new StringBuilder("<html>");
        for (int i = 1; i <= titoli.length; i++) {
            if (i > 1) sb.append("&nbsp; ── &nbsp;");
            String voce = i + " " + titoli[i - 1];
            if (i < corrente) {
                sb.append("<font color='#7C73E6'>").append(voce).append("</font>");
            } else if (i == corrente) {
                sb.append("<b><font color='#333333'>").append(voce).append("</font></b>");
            } else {
                sb.append("<font color='#999999'>").append(voce).append("</font>");
            }
        }
        return sb.append("</html>").toString();
    }

    /** Bottone d'azione pieno (Continua/Conferma), nello stile del btnLogin. */
    static void stilizzaBottone(JButton bottone, Color sfondo) {
        bottone.setBackground(sfondo);
        bottone.setForeground(Color.WHITE);
        bottone.setFocusPainted(false);
        bottone.setBorderPainted(false);
        bottone.setOpaque(true);
        bottone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Bottone Logout dell'header (bianco su viola). */
    static void stilizzaLogout(JButton btnLogout) {
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(VIOLA);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Etichetta-link "← Indietro" a fondo card. */
    static void stilizzaIndietro(JLabel lblIndietro) {
        lblIndietro.setText("← Indietro");
        lblIndietro.setForeground(GRIGIO_TESTO);
        lblIndietro.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblIndietro.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Campo di testo con bordo e padding, nello stile dei campi del Login. */
    static void stilizzaCampo(JTextField campo) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GRIGIO_BORDO, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campo.setBackground(Color.WHITE);
    }

    /** Segnaposto grigio come nel Login: sparisce al focus e ricompare se il campo resta vuoto. */
    static void installaPlaceholder(JTextField campo, String placeholder) {
        campo.setText(placeholder);
        campo.setForeground(PLACEHOLDER);
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (campo.getText().equals(placeholder)) {
                    campo.setText("");
                    campo.setForeground(Color.DARK_GRAY);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (campo.getText().isEmpty()) {
                    campo.setText(placeholder);
                    campo.setForeground(PLACEHOLDER);
                }
            }
        });
    }

    /** Valore effettivo del campo: stringa vuota se contiene ancora il segnaposto. */
    static String valoreCampo(JTextField campo, String placeholder) {
        String testo = campo.getText().trim();
        return testo.equals(placeholder) ? "" : testo;
    }

    /** Bordo arrotondato con padding per le card (non configurabile nel form designer). */
    static void bordaCard(JPanel card) {
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GRIGIO_BORDO, 1, true),
                new EmptyBorder(20, 24, 20, 24)));
    }

    /** Radio "a card" con bordo e sfondo evidenziati in viola quando selezionata. */
    static JRadioButton creaRadioCard(String html) {
        JRadioButton radio = new JRadioButton(html);
        configuraRadioCard(radio);
        return radio;
    }

    /** Applica lo stile radio-card a una radio già esistente (es. creata dal form designer). */
    static void configuraRadioCard(JRadioButton radio) {
        radio.setBackground(Color.WHITE);
        radio.setFont(new Font("SansSerif", Font.PLAIN, 13));
        radio.setFocusPainted(false);
        radio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        radio.setBorderPainted(true);
        radio.setOpaque(true);
        radio.setBorder(bordoRadioCard(false));
        radio.addItemListener(e -> {
            boolean selezionata = radio.isSelected();
            radio.setBackground(selezionata ? VIOLA_CHIARO : Color.WHITE);
            radio.setBorder(bordoRadioCard(selezionata));
        });
    }

    private static Border bordoRadioCard(boolean selezionata) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(selezionata ? VIOLA : new Color(220, 220, 225), 1, true),
                new EmptyBorder(10, 12, 10, 12));
    }
}
