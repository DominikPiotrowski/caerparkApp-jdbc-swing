import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class NewPark extends JFrame implements WindowListener {

    private JPanel newParkMainPanel;
    private JPanel newParkPanel;
    private JLabel labelCarRegistration;
    private JLabel labelFirstName;
    private JLabel labelLastName;
    private JLabel labelCarType;
    private JLabel labelStartDate;
    private JLabel labelExpiryDate;
    private JLabel labelLapNo;
    private JLabel labelTrackNo;
    private JTextField registrationNoTextField;
    private JTextField textFieldFirstName;
    private JTextField textFieldLastName;
    private JTextField textFieldLapNo;
    private JTextField textFieldTrackNo;
    private JComboBox comboBoxCarSize;
    private JButton BACKButton;
    private JButton PARKButton;
    private JButton CLEARButton;
    private JDateChooser datePickerStartDate;
    private JDateChooser datePickerEndDate;

    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;

    NewPark() {
        try {
            connection = Connector.establishConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initUI();
        getTrackNumber();
        this.pack();
    }

    private final void initUI() {
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.add(newParkMainPanel);
        setupCarSizes();

        addWindowListener(this);

        PARKButton.addActionListener(e -> {
            String query = "INSERT INTO parked (registration_number,first_name,last_name,car_type,start_date,end_date,lap_number,track_number)" +
                    "VALUES(?,?,?,?,?,?,?,?)";

            SimpleDateFormat rentDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = rentDateFormat.format(datePickerStartDate.getDate());

            SimpleDateFormat returnDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String endDate = returnDateFormat.format(datePickerEndDate.getDate());

            if (datePickerStartDate.getDate().before(datePickerEndDate.getDate())) {
                try {
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, registrationNoTextField.getText());
                    preparedStatement.setString(2, textFieldFirstName.getText());
                    preparedStatement.setString(3, textFieldLastName.getText());
                    preparedStatement.setString(4, Objects.requireNonNull(comboBoxCarSize.getSelectedItem()).toString());
                    preparedStatement.setString(5, startDate);
                    preparedStatement.setString(6, endDate);
                    preparedStatement.setString(7, textFieldLapNo.getText());
                    preparedStatement.setString(8, textFieldTrackNo.getText());
                    preparedStatement.executeUpdate();
                    updateStatusInLapsDB();

                    final ImageIcon infoIcon = new ImageIcon(getClass().getResource("icons\\info.png"));
                    JOptionPane.showMessageDialog(null, "Car parked", "Info", JOptionPane.INFORMATION_MESSAGE, infoIcon);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                final ImageIcon infoIcon = new ImageIcon(getClass().getResource("icons\\info.png"));
                JOptionPane.showMessageDialog(null, "Please check the dates", "Info", JOptionPane.INFORMATION_MESSAGE, infoIcon);
            }
        });

        CLEARButton.addActionListener(e -> {
            textFieldLastName.setText("");
            textFieldFirstName.setText("");
            registrationNoTextField.setText("");
            textFieldLapNo.setText("");
            textFieldTrackNo.setText("");
            comboBoxCarSize.setSelectedItem("Saloon/Minivan");
            Date today = new Date();
            datePickerStartDate.getDateEditor().setDate(today);
            datePickerEndDate.getDateEditor().setDate(today);
            getTrackNumber();
        });

        BACKButton.addActionListener(e -> {
            NewPark.this.dispose();
            Login login = new Login();
            login.setVisible(true);
        });
    }

    private void getTrackNumber() {
        String query = "SELECT * FROM laps WHERE status='avaliable' ";

        try {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String addLap = resultSet.getString("lap_number");
                textFieldLapNo.setText(addLap);

                String addTrack = resultSet.getString("track_number");
                textFieldTrackNo.setText(addTrack);
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

    private void updateStatusInLapsDB() {
        String lap = textFieldLapNo.getText();
        String track = textFieldTrackNo.getText();
        String query = "UPDATE laps SET status= 'reserved' WHERE lap_number= " + lap + "  AND track_number= " + track + "  ";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupCarSizes() {
        comboBoxCarSize.removeAllItems();
        comboBoxCarSize.addItem("Saloon/Minivan");
        comboBoxCarSize.addItem("Van");
        comboBoxCarSize.addItem("Truck");
    }

    private void createUIComponents() {
        if (datePickerStartDate == null) {
            datePickerStartDate = new JDateChooser();
            datePickerStartDate.setDateFormatString("yyyy-MM-dd");
        }

        if (datePickerEndDate == null) {
            datePickerEndDate = new JDateChooser();
            datePickerEndDate.setDateFormatString("yyyy-MM-dd");
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new NewPark().setVisible(true));
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.dispose();
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
