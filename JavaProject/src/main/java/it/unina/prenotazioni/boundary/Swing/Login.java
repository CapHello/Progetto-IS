package it.unina.prenotazioni.boundary.Swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.unina.prenotazioni.boundary.ConfigurazioneNotifiche;
import it.unina.prenotazioni.controller.BibliotecaFacade;
import it.unina.prenotazioni.dto.UtenteDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;

/**
 * commento
 */
public class Login {

    /**
     * commento
     * @param args args
     */
    public static void main(String[] args) {
        // Qui Spring non c'è, quindi il @PostConstruct di ConfigurazioneNotifiche non parte:
        // il cablaggio del servizio notifiche va fatto a mano, altrimenti le notifiche si perdono.
        new ConfigurazioneNotifiche().configura();

        Login login = new Login();
        login.apriLogin();
    }

    private static final Color VIOLA = new Color(124, 115, 230);
    private static final Color PLACEHOLDER = new Color(160, 160, 170);

    // Frame corrente: serve per chiuderlo al momento del login
    private JFrame frameCorrente;

    // Campi legati al .form
    private JPanel loginPane;
    private JPanel cardLogin;
    private JLabel lblTitolo;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblRegistrati;

    /**
     * commento
     */
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
        lblRegistrati.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frameCorrente.dispose();
                new Registrazione().apriForm();
            }
        });
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = getEmail();
                String password = getPassword();

                if (email.isBlank() || email.equalsIgnoreCase("Email Istituzionale")) {
                    mostraErrore("Inserisci la tua email istituzionale");
                    return;
                }

                // Se l'echo char è disattivato il campo sta ancora mostrando il placeholder:
                // così una password che vale davvero "Password" resta accettata.
                if (password.isBlank() || txtPassword.getEchoChar() == (char) 0) {
                    mostraErrore("Inserisci la password");
                    return;
                }

                try {
                    UtenteDTO utenteDTO = BibliotecaFacade.getInstance().autenticazione(email, password);
                    if (utenteDTO.getRuolo().equalsIgnoreCase("Studente")) {
                        frameCorrente.dispose();
                        DashboardStudente dashboardStudente = new DashboardStudente();
                        dashboardStudente.apriDashboard(utenteDTO);
                    } else if (utenteDTO.getRuolo().equalsIgnoreCase("Bibliotecario")) {
                        frameCorrente.dispose();
                        new DashboardBibliotecario().apriDashboard();
                    }
                } catch (RuntimeException exception) {
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

    // --- APRI LOGIN ---


    /**
     * commento
     * @return result
     */
    public JFrame apriLogin() {
        frameCorrente = new JFrame("Login");
        frameCorrente.setContentPane(loginPane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    // --- GETTER / LISTENER per il controller ---

    /**
     * commento
     * @return result
     */
    public String getEmail() {
        return txtEmail.getText();
    }

    /**
     * commento
     * @return result
     */
    public String getPassword() {
        return String.valueOf(txtPassword.getPassword());
    }


    /**
     * commento
     * @param messaggio messaggio
     */
    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(loginPane, messaggio, "Errore login", JOptionPane.ERROR_MESSAGE);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        loginPane = new JPanel();
        loginPane.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        loginPane.setBackground(new Color(-5192482));
        final Spacer spacer1 = new Spacer();
        loginPane.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-5192482));
        panel1.setOpaque(true);
        loginPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        cardLogin = new JPanel();
        cardLogin.setLayout(new GridLayoutManager(5, 1, new Insets(40, 40, 40, 40), -1, 12));
        cardLogin.setBackground(new Color(-1));
        cardLogin.setOpaque(true);
        panel1.add(cardLogin, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(400, 400), null, 0, false));
        lblTitolo = new JLabel();
        Font lblTitoloFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 24, lblTitolo.getFont());
        if (lblTitoloFont != null) lblTitolo.setFont(lblTitoloFont);
        lblTitolo.setHorizontalAlignment(0);
        lblTitolo.setText("Accedi");
        cardLogin.add(lblTitolo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtEmail = new JTextField();
        txtEmail.setForeground(new Color(-6710887));
        txtEmail.setText("");
        txtEmail.setToolTipText("Email Istituzionale");
        cardLogin.add(txtEmail, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 65), null, 0, false));
        txtPassword = new JPasswordField();
        txtPassword.setForeground(new Color(-6710887));
        txtPassword.setText("Password");
        cardLogin.add(txtPassword, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 45), null, 0, false));
        btnLogin = new JButton();
        btnLogin.setBackground(new Color(-8621082));
        Font btnLoginFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, btnLogin.getFont());
        if (btnLoginFont != null) btnLogin.setFont(btnLoginFont);
        btnLogin.setForeground(new Color(-1));
        btnLogin.setText("Login");
        cardLogin.add(btnLogin, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 45), null, 0, false));
        lblRegistrati = new JLabel();
        lblRegistrati.setHorizontalAlignment(0);
        lblRegistrati.setText("Non hai un account? Registrati");
        cardLogin.add(lblRegistrati, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        loginPane.add(spacer4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /*
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /*
     * @noinspection ALL
     */
    /**
     * commento
     * @return result
     */
    public JComponent $$$getRootComponent$$$() {
        return loginPane;
    }

}
