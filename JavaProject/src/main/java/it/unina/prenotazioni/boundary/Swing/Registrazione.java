package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Registrazione {

    public static void main(String[] args) {
        // test sulla singola interfaccia
        new Registrazione().apriForm();
    }

    private static final String PLACEHOLDER_NOME     = "Nome";
    private static final String PLACEHOLDER_COGNOME  = "Cognome";
    private static final String PLACEHOLDER_EMAIL    = "Email Istituzionale";
    private static final String PLACEHOLDER_PASSWORD = "Password";
    private static final String PLACEHOLDER_CODICE   = "Matricola/Codice";

    // Campi legati al .form
    private JPanel         registrazionePane;
    private JPanel         cardRegistrazione;
    private JLabel         lblTitolo;
    private JTextField     txtNome;
    private JTextField     txtCognome;
    private JTextField     txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> comboRuolo;
    private JTextField     txtIdentificativo;
    private JButton        btnRegistrati;
    private JLabel         lblAccedi;

    // Frame corrente — serve per chiuderlo quando si torna al Login
    private JFrame frameCorrente;

    public Registrazione() {
        // Bordo card arrotondato (non configurabile nel form designer)
        cardRegistrazione.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 228), 1, true),
                new EmptyBorder(35, 40, 35, 40)));

        // Styling e placeholder dei campi
        for (JTextField campo : new JTextField[]{txtNome, txtCognome, txtEmail, txtPassword, txtIdentificativo}) {
            StileWizard.stilizzaCampo(campo);
        }
        StileWizard.installaPlaceholder(txtNome, PLACEHOLDER_NOME);
        StileWizard.installaPlaceholder(txtCognome, PLACEHOLDER_COGNOME);
        StileWizard.installaPlaceholder(txtEmail, PLACEHOLDER_EMAIL);
        StileWizard.installaPlaceholder(txtIdentificativo, PLACEHOLDER_CODICE);

        // Placeholder password
        txtPassword.setEchoChar((char) 0);
        txtPassword.setForeground(StileWizard.PLACEHOLDER);
        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(txtPassword.getPassword()).equals(PLACEHOLDER_PASSWORD)) {
                    txtPassword.setText("");
                    txtPassword.setEchoChar('●');
                    txtPassword.setForeground(Color.DARK_GRAY);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtPassword.getPassword().length == 0) {
                    txtPassword.setEchoChar((char) 0);
                    txtPassword.setText(PLACEHOLDER_PASSWORD);
                    txtPassword.setForeground(StileWizard.PLACEHOLDER);
                }
            }
        });

        comboRuolo.setModel(new DefaultComboBoxModel<>(new String[]{"Studente", "Bibliotecario"}));
        comboRuolo.setBackground(Color.WHITE);

        StileWizard.stilizzaBottone(btnRegistrati, StileWizard.VIOLA);

        lblAccedi.setText("<html>Hai già un account? <font color='#7C73E6'><b>Accedi</b></font></html>");
        lblAccedi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblAccedi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameCorrente.dispose();
                new Login().apriLogin();
            }
        });

        btnRegistrati.addActionListener(e -> registra());
    }

    // ── REGISTRAZIONE ────────────────────────────────────────────────────────

    private void registra() {
        String nome           = StileWizard.valoreCampo(txtNome, PLACEHOLDER_NOME);
        String cognome        = StileWizard.valoreCampo(txtCognome, PLACEHOLDER_COGNOME);
        String email          = StileWizard.valoreCampo(txtEmail, PLACEHOLDER_EMAIL);
        String password       = String.valueOf(txtPassword.getPassword());
        String ruolo          = (String) comboRuolo.getSelectedItem();
        String identificativo = StileWizard.valoreCampo(txtIdentificativo, PLACEHOLDER_CODICE);

        if (password.equals(PLACEHOLDER_PASSWORD)) {
            password = "";
        }
        if (nome.isBlank() || cognome.isBlank() || email.isBlank()
                || password.isBlank() || identificativo.isBlank()) {
            mostraErrore("Compila tutti i campi");
            return;
        }

        try {
            BibliotecaFacade.getInstance().registrazione(ruolo, nome, cognome, email, password, identificativo);
            JOptionPane.showMessageDialog(registrazionePane,
                    "Registrazione completata! Ora puoi accedere.",
                    "Registrazione", JOptionPane.INFORMATION_MESSAGE);
            frameCorrente.dispose();
            new Login().apriLogin();
        } catch (RuntimeException ex) {
            mostraErrore(ex.getMessage());
        }
    }

    // ── APRI FORM ────────────────────────────────────────────────────────────

    public JFrame apriForm() {
        frameCorrente = new JFrame("Crea Account");
        frameCorrente.setContentPane(registrazionePane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(registrazionePane, messaggio, "Errore registrazione", JOptionPane.ERROR_MESSAGE);
    }
}
