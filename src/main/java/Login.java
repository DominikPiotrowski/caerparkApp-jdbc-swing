import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends JFrame implements WindowListener {
    private JPanel loginPanel;
    private JPanel login;
    private JTextField loginTextfield;
    private JPasswordField passwordTextfield;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JButton NEWButton;
    private JButton ENTERButton;

    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;

    public Login() {
        initUI();
        try {
            connection = Connector.establishConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.pack();
    }

    private final void initUI() {
        JPanel lp = new JPanel();
        getContentPane().add(lp);
        lp.add(loginPanel);
        addWindowListener(this);

        ENTERButton.addActionListener(e -> {
            String query = "SELECT * FROM login WHERE user_id=? AND user_password=?";

            try {
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, loginTextfield.getText());
                preparedStatement.setString(2, passwordTextfield.getText());
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    resultSet.close();
                    preparedStatement.close();

                    Login.this.dispose();
                    Home home = new Home();
                    home.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(login, "Username or password incorrect.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        NEWButton.addActionListener(e -> {
            Login.this.dispose();
            NewPark newPark = new NewPark();
            newPark.setVisible(true);
        });
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Login().setVisible(true));
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}