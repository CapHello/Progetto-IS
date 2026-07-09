package it.unina.prenotazioni.boundary.Swing;

import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class Login {

    public static void main(String[] args) {
        Login login = new Login();
        login.apriLogin();
    }

    private static final Color VIOLA         = new Color(124, 115, 230);
    private static final Color PLACEHOLDER   = new Color(160, 160, 170);

    // Frame corrente — serve per chiuderlo al momento del login
    private JFrame frameCorrente;

    // Campi legati al .form
    private JPanel         loginPane;
    private JPanel         cardLogin;
    private JLabel         lblTitolo;
    private JTextField     txtEmail;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JLabel         lblRegistrati;

    public Login() {
        // Bordo card arrotondato (non configurabile nel form designer)
        cardLogin.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 228), 1, true),
                new EmptyBorder(40, 40, 40, 40)));

        // Styling campi
        stilizzaCampo(txtEmail);
        stilizzaCampo(txtPassword);

        // Placeholder email
        txtEmail.setForeground(PLACEHOLDER);
        txtEmail.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtEmail.getText().equals("Email Istituzionale")) {
                    txtEmail.setText("");
                    txtEmail.setForeground(Color.DARK_GRAY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtEmail.getText().isEmpty()) {
                    txtEmail.setText("Email Istituzionale");
                    txtEmail.setForeground(PLACEHOLDER);
                }
            }
        });

        // Placeholder password
        txtPassword.setEchoChar((char) 0);
        txtPassword.setForeground(PLACEHOLDER);
        txtPassword.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(txtPassword.getPassword()).equals("Password")) {
                    txtPassword.setText("");
                    txtPassword.setEchoChar('●');
                    txtPassword.setForeground(Color.DARK_GRAY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtPassword.getPassword().length == 0) {
                    txtPassword.setEchoChar((char) 0);
                    txtPassword.setText("Password");
                    txtPassword.setForeground(PLACEHOLDER);
                }
            }
        });

        // Styling btnLogin
        btnLogin.setBackground(VIOLA);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Styling link registrazione
        lblRegistrati.setText("<html>Non hai un account? <font color='#7C73E6'><b>Registrati</b></font></html>");
        lblRegistrati.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = getEmail();
                String password = getPassword();

                if(email.isBlank() || email.equalsIgnoreCase("Email Istituzionale")){
                    mostraErrore("Inserisci la tua email istituzionale");
                    return;
                }

                if(password.isBlank() || password.equalsIgnoreCase("Password")){
                    mostraErrore("Inserisci la password cretino");
                    return;
                }


                UtenteDTO utenteDTO = null;
                try{
                    utenteDTO = BibliotecaFacade.getInstance().autenticazione(email, password);
                    if (utenteDTO == null){
                        mostraErrore("Non sei registrato, registrati!!");
                    }else {
                        if (utenteDTO.getRuolo().equalsIgnoreCase("Studente")) {
                            frameCorrente.dispose();
                            DashboardStudente dashboardStudente = new DashboardStudente();
                            dashboardStudente.apriDashboard(utenteDTO);
                        }else if(utenteDTO.getRuolo().equalsIgnoreCase("Bibliotecario")){
                            //TODO
                        }



                    }
                }catch (SecurityException exception){
                    mostraErrore(exception.getMessage());
                }



            }
        });
    }

    private void stilizzaCampo(JTextField campo) {
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 228), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campo.setBackground(Color.WHITE);
    }

    // ── APRI LOGIN ────────────────────────────────────────────────────────────



    public JFrame apriLogin() {
        frameCorrente = new JFrame("Login");
        frameCorrente.setContentPane(loginPane);
        frameCorrente.setSize(960, 700);
        frameCorrente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    // ── GETTER / LISTENER per il controller ──────────────────────────────────

    public String getEmail()    { return txtEmail.getText(); }
    public String getPassword() { return String.valueOf(txtPassword.getPassword()); }



    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(loginPane, messaggio, "Errore login", JOptionPane.ERROR_MESSAGE);
    }

}
