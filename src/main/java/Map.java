import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Map extends JFrame {
    private JPanel avaliablePanel;
    private JButton RESERVEDButton;
    private JButton AVALIABLEButton;
    private JButton BACKButton;
    private JTextField textFieldAvaliable;
    private JTextField textFieldReserved;
    private JLabel backButton;

    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;

    Map() {
        try {
            connection = Connector.establishConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initUI();
        countAvalSpace();
        countReservedSpace();
        this.pack();
    }

    private final void initUI() {
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.add(avaliablePanel);

        BACKButton.addActionListener(e -> {
            this.dispose();
            Home home = new Home();
            home.setVisible(true);
        });
    }

    private void countAvalSpace() {
        String query = "SELECT COUNT(status) FROM laps WHERE status= 'AVALIABLE' ";

        try {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String sumOfAvalSpace = resultSet.getString("COUNT(status)");
                textFieldAvaliable.setText(sumOfAvalSpace);
            } else {
                textFieldAvaliable.setText("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void countReservedSpace() {
        String query = "SELECT COUNT(status) FROM laps WHERE status= 'RESERVED' ";

        try {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String sumOfReservedSpace = resultSet.getString("COUNT(status)");
                textFieldReserved.setText(sumOfReservedSpace);
            } else {
                textFieldReserved.setText("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Map().setVisible(true));
    }
}