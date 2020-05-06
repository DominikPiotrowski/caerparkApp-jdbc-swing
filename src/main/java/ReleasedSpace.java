import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Vector;

public class ReleasedSpace extends JFrame {
    private JPanel mainReleasedPanel;
    private JTable releasedTable;
    private JPanel paymentPanel;
    private JTextField textFieldRegistrationNo;
    private JTextField textFieldDays;
    private JTextField textFieldPaid;
    private JButton backButton;
    private JButton reportButton;
    private JButton logoutButton;
    private JTextField textFieldLastName;
    private JTextField textFieldFirstName;
    private JPanel reportPanel;

    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;

    ReleasedSpace() {
        try {
            connection = Connector.establishConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initUI();
        updateTable();
        this.pack();

    }

    private final void initUI() {
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.add(mainReleasedPanel);

        Object[] columns = {"Registration", "Name", "Last name", "Car type", "Start date", "End date", "Lap", "Track", "Days parked", "Paid"};
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columns);
        releasedTable.setModel(model);

        logoutButton.addActionListener(e -> {
            this.dispose();
            Login login = new Login();
            login.setVisible(true);
        });

        backButton.addActionListener(e -> {
            this.dispose();
            Home home = new Home();
            home.setVisible(true);
        });

        releasedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DefaultTableModel defaultTableModel = (DefaultTableModel) releasedTable.getModel();
                int selectIndex = releasedTable.getSelectedRow();

                textFieldRegistrationNo.setText(defaultTableModel.getValueAt(selectIndex, 0).toString());
                textFieldFirstName.setText(defaultTableModel.getValueAt(selectIndex, 1).toString());
                textFieldLastName.setText(defaultTableModel.getValueAt(selectIndex, 2).toString());
                textFieldDays.setText(defaultTableModel.getValueAt(selectIndex, 8).toString());
                textFieldPaid.setText(defaultTableModel.getValueAt(selectIndex, 9).toString());
            }
        });

        reportButton.addActionListener(e -> {
            MessageFormat header = new MessageFormat("Released report");
            MessageFormat footer = new MessageFormat("...");

            try {
                releasedTable.print(JTable.PrintMode.NORMAL, header, footer);
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updateTable() {

        try {
            String query = "SELECT * FROM released";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int carRecordsCount = resultSetMetaData.getColumnCount();
            DefaultTableModel defaultTableModel = (DefaultTableModel) releasedTable.getModel();
            defaultTableModel.setRowCount(0);

            while (resultSet.next()) {
                Vector vector = new Vector();

                for (int i = 1; i < carRecordsCount; i++) {
                    vector.add(resultSet.getString("registration_number"));
                    vector.add(resultSet.getString("first_name"));
                    vector.add(resultSet.getString("last_name"));
                    vector.add(resultSet.getString("car_type"));
                    vector.add(resultSet.getString("start_date"));
                    vector.add(resultSet.getString("end_date"));
                    vector.add(resultSet.getString("lap_number"));
                    vector.add(resultSet.getString("track_number"));
                    vector.add(resultSet.getString("days_total"));
                    vector.add(resultSet.getString("paid"));
                }
                defaultTableModel.addRow(vector);
            }
            releasedTable.setModel(defaultTableModel);

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
        EventQueue.invokeLater(() -> new ReleasedSpace().setVisible(true));
    }
}