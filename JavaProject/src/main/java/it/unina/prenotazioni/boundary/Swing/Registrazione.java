package it.unina.prenotazioni.boundary.Swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.unina.prenotazioni.controller.BibliotecaFacade;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

public class Registrazione {

    public static void main(String[] args) {
        // test sulla singola interfaccia
        new Registrazione().apriForm();
    }

    private static final String PLACEHOLDER_NOME = "Nome";
    private static final String PLACEHOLDER_COGNOME = "Cognome";
    private static final String PLACEHOLDER_EMAIL = "Email Istituzionale";
    private static final String PLACEHOLDER_PASSWORD = "Password";
    private static final String PLACEHOLDER_CODICE = "Matricola/Codice";

    // Campi legati al .form
    private JPanel registrazionePane;
    private JPanel cardRegistrazione;
    private JLabel lblTitolo;
    private JTextField txtNome;
    private JTextField txtCognome;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JComboBox<String> comboRuolo;
    private JTextField txtIdentificativo;
    private JButton btnRegistrati;
    private JLabel lblAccedi;

    // Frame corrente: serve per chiuderlo quando si torna al Login
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

    // --- REGISTRAZIONE ---

    private void registra() {
        String nome = StileWizard.valoreCampo(txtNome, PLACEHOLDER_NOME);
        String cognome = StileWizard.valoreCampo(txtCognome, PLACEHOLDER_COGNOME);
        String email = StileWizard.valoreCampo(txtEmail, PLACEHOLDER_EMAIL);
        String password = String.valueOf(txtPassword.getPassword());
        String ruolo = (String) comboRuolo.getSelectedItem();
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

    // --- APRI FORM ---

    public JFrame apriForm() {
        frameCorrente = new JFrame("Crea Account");
        frameCorrente.setContentPane(registrazionePane);
        frameCorrente.setSize(1000, 1000);
        frameCorrente.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameCorrente.setLocationRelativeTo(null);
        frameCorrente.setResizable(false);
        frameCorrente.setVisible(true);
        return frameCorrente;
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(registrazionePane, messaggio, "Errore registrazione", JOptionPane.ERROR_MESSAGE);
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
        registrazionePane = new JPanel();
        registrazionePane.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        registrazionePane.setBackground(new Color(-5192482));
        final Spacer spacer1 = new Spacer();
        registrazionePane.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-5192482));
        panel1.setOpaque(true);
        registrazionePane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        cardRegistrazione = new JPanel();
        cardRegistrazione.setLayout(new GridLayoutManager(9, 1, new Insets(35, 40, 35, 40), -1, 10));
        cardRegistrazione.setBackground(new Color(-1));
        cardRegistrazione.setOpaque(true);
        panel1.add(cardRegistrazione, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(400, 560), null, 0, false));
        lblTitolo = new JLabel();
        Font lblTitoloFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 24, lblTitolo.getFont());
        if (lblTitoloFont != null) lblTitolo.setFont(lblTitoloFont);
        lblTitolo.setHorizontalAlignment(0);
        lblTitolo.setText("Crea Account");
        cardRegistrazione.add(lblTitolo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtNome = new JTextField();
        txtNome.setForeground(new Color(-6710887));
        txtNome.setText("");
        txtNome.setToolTipText("Nome");
        cardRegistrazione.add(txtNome, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 42), null, 0, false));
        txtCognome = new JTextField();
        txtCognome.setForeground(new Color(-6710887));
        txtCognome.setText("");
        txtCognome.setToolTipText("Cognome");
        cardRegistrazione.add(txtCognome, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 42), null, 0, false));
        txtEmail = new JTextField();
        txtEmail.setForeground(new Color(-6710887));
        txtEmail.setText("");
        txtEmail.setToolTipText("Email Istituzionale");
        cardRegistrazione.add(txtEmail, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 42), null, 0, false));
        txtPassword = new JPasswordField();
        txtPassword.setForeground(new Color(-6710887));
        txtPassword.setText("Password");
        cardRegistrazione.add(txtPassword, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 42), null, 0, false));
        comboRuolo = new JComboBox();
        cardRegistrazione.add(comboRuolo, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 42), null, 0, false));
        txtIdentificativo = new JTextField();
        txtIdentificativo.setForeground(new Color(-6710887));
        txtIdentificativo.setText("");
        txtIdentificativo.setToolTipText("Matricola/Codice");
        cardRegistrazione.add(txtIdentificativo, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 42), null, 0, false));
        btnRegistrati = new JButton();
        btnRegistrati.setBackground(new Color(-8621082));
        Font btnRegistratiFont = this.$$$getFont$$$("SansSerif", Font.BOLD, 14, btnRegistrati.getFont());
        if (btnRegistratiFont != null) btnRegistrati.setFont(btnRegistratiFont);
        btnRegistrati.setForeground(new Color(-1));
        btnRegistrati.setText("Registrati");
        cardRegistrazione.add(btnRegistrati, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(320, 45), null, 0, false));
        lblAccedi = new JLabel();
        lblAccedi.setHorizontalAlignment(0);
        lblAccedi.setText("Hai già un account? Accedi");
        cardRegistrazione.add(lblAccedi, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        registrazionePane.add(spacer4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
    public JComponent $$$getRootComponent$$$() {
        return registrazionePane;
    }

}
