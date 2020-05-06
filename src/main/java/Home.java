import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.sql.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class Home extends JFrame {
    private JPanel mainHomePanel;
    private JPanel newParkPanel;
    private JLabel trackNoLabel;
    private JLabel carRegistrationLabel;
    private JLabel firstNameLabel;
    private JLabel lastNameLabel;
    private JLabel carTypelLabel;
    private JLabel stardDateLabel;
    private JLabel expiryDateLabel;
    private JLabel lapNoLabel;
    private JTextField textFieldRegNumber;
    private JTextField textFieldFirstName;
    private JTextField textFieldLastName;
    private JTextField textFieldLapNo;
    private JTextField textFieldTrackNo;
    private JComboBox comboBoxCarSize;
    private JTextField textFieldStatus;
    private JButton AVALButton;
    private JButton PARKButton;
    private JButton RELEASEButton;
    private JButton CLEARButton;
    private JButton reportButton;
    private JTable carsParkedTable;
    private JPanel tablePanel;
    private JButton LOGOUTButton;
    private JButton RELEASEDButton;
    private JPanel logoutPanel;
    private JPanel releasedPanel;
    private JPanel searchPanel;
    private JTextField textFieldCarsParked;
    private JPanel reportPanel;
    private JTextField textFieldCountParked;
    private JButton countParkedButton;
    private JPanel countParkedPanel;
    private JDateChooser datePickerStartDate;
    private JDateChooser datePickerEndDate;

    private Connection connection;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;

    Home() {
        try {
            connection = Connector.establishConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initUI();
        getTrackNumber();
        updateTable();
        this.pack();
    }

    private final void initUI() {
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.add(mainHomePanel);

        setupCarSizes();

        Object[] columns = {"Registration", "Name", "Last name", "Car type", "Start date", "End date", "Lap", "Track"};
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columns);
        carsParkedTable.setModel(model);

        LOGOUTButton.addActionListener(e -> {
            this.dispose();
            Login login = new Login();
            login.setVisible(true);
        });

        RELEASEDButton.addActionListener(e -> {
            this.dispose();
            ReleasedSpace releasedSpace = new ReleasedSpace();
            releasedSpace.setVisible(true);
        });

        AVALButton.addActionListener(e -> {
            this.dispose();
            Map map = new Map();
            map.setVisible(true);
        });

        reportButton.addActionListener(e -> {
            MessageFormat header = new MessageFormat("Cars parked report");
            MessageFormat footer = new MessageFormat("...");

            try {
                carsParkedTable.print(JTable.PrintMode.NORMAL, header, footer);
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        });

        RELEASEButton.addActionListener(e -> {
            int confirmRelease = JOptionPane.showConfirmDialog(null, "Relase?", "Question", JOptionPane.YES_NO_OPTION);

            if (confirmRelease == 0) {
                String query = "DELETE FROM parked WHERE registration_number=?";
                try {
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, textFieldRegNumber.getText());
                    preparedStatement.execute();
                    updateReleasedDB();
                    amountPaid();
                    updateStatusInLapsDBAfterReleasedPressed();
                    updateTable();
                    clearFields();

                    final ImageIcon infoIcon = new ImageIcon(getClass().getResource("icons\\info.png"));
                    JOptionPane.showMessageDialog(null, "Released", "Information", JOptionPane.INFORMATION_MESSAGE, infoIcon);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        countParkedButton.addActionListener(e -> {
            String query = "SELECT COUNT(idparked) FROM parked";
            try {
                preparedStatement = connection.prepareStatement(query);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    String result = resultSet.getString("COUNT(idparked)");
                    textFieldCountParked.setText(result);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        });

        textFieldCarsParked.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = "SELECT * FROM parked WHERE last_name=?";

                try {
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, textFieldCarsParked.getText());
                    resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        String registrationNo = resultSet.getString("registration_number");
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");
                        String carType = resultSet.getString("car_type");
                        String startDate = resultSet.getString("start_date");
                        String endDate = resultSet.getString("end_date");
                        String lapNo = resultSet.getString("lap_number");
                        String trackNo = resultSet.getString("track_number");

                        textFieldRegNumber.setText(registrationNo);
                        textFieldFirstName.setText(firstName);
                        textFieldLastName.setText(lastName);
                        comboBoxCarSize.setSelectedItem(carType);
                        textFieldLapNo.setText(lapNo);
                        textFieldTrackNo.setText(trackNo);

                        SimpleDateFormat parkDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        Date startD = parkDateFormat.parse(startDate);
                        datePickerStartDate.setDate(startD);

                        Date endD = parkDateFormat.parse(endDate);
                        datePickerEndDate.setDate(endD);

                        checkIfExpired();
                    }
                } catch (SQLException | ParseException ex) {
                    ex.printStackTrace();
                }
            }
        });

        CLEARButton.addActionListener(e -> {
            textFieldRegNumber.setText("");
            textFieldFirstName.setText("");
            textFieldLastName.setText("");
            textFieldLapNo.setText("");
            textFieldTrackNo.setText("");
            comboBoxCarSize.setSelectedItem("Saloon/Minivan");
            Date today = new Date();
            datePickerStartDate.getDateEditor().setDate(today);
            datePickerEndDate.getDateEditor().setDate(today);
            getTrackNumber();
        });

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
                    preparedStatement.setString(1, textFieldRegNumber.getText());
                    preparedStatement.setString(2, textFieldFirstName.getText());
                    preparedStatement.setString(3, textFieldLastName.getText());
                    preparedStatement.setString(4, Objects.requireNonNull(comboBoxCarSize.getSelectedItem()).toString());
                    preparedStatement.setString(5, startDate);
                    preparedStatement.setString(6, endDate);
                    preparedStatement.setString(7, textFieldLapNo.getText());
                    preparedStatement.setString(8, textFieldTrackNo.getText());
                    preparedStatement.executeUpdate();
                    updateStatusInLapsDB();
                    updateTable();
                    getTrackNumber();

                    final ImageIcon infoIcon = new ImageIcon(getClass().getResource("icons\\info.png"));
                    JOptionPane.showMessageDialog(null, "Car parked", "Information", JOptionPane.INFORMATION_MESSAGE, infoIcon);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                final ImageIcon infoIcon = new ImageIcon(getClass().getResource("icons\\info.png"));
                JOptionPane.showMessageDialog(null, "Please check the dates", "Information", JOptionPane.INFORMATION_MESSAGE, infoIcon);
            }
        });

        carsParkedTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DefaultTableModel defaultTableModel = (DefaultTableModel) carsParkedTable.getModel();
                int selectIndex = carsParkedTable.getSelectedRow();

                textFieldRegNumber.setText(defaultTableModel.getValueAt(selectIndex, 0).toString());
                textFieldFirstName.setText(defaultTableModel.getValueAt(selectIndex, 1).toString());
                textFieldLastName.setText(defaultTableModel.getValueAt(selectIndex, 2).toString());
                comboBoxCarSize.setSelectedItem(defaultTableModel.getValueAt(selectIndex, 3).toString());

                SimpleDateFormat parkDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date startDate = parkDateFormat.parse(defaultTableModel.getValueAt(selectIndex, 4).toString());
                    datePickerStartDate.setDate(startDate);
                    Date endDate = parkDateFormat.parse(defaultTableModel.getValueAt(selectIndex, 5).toString());
                    datePickerEndDate.setDate(endDate);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                textFieldLapNo.setText(defaultTableModel.getValueAt(selectIndex, 6).toString());
                textFieldTrackNo.setText(defaultTableModel.getValueAt(selectIndex, 7).toString());

                checkIfExpired();
            }
        });
    }

    private void amountPaid() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String startDate = dateFormat.format(datePickerStartDate.getDate());
        String endDate = dateFormat.format(datePickerEndDate.getDate());
        String carType = Objects.requireNonNull(comboBoxCarSize.getSelectedItem()).toString();

        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            long timeDifference = start.getTime() - end.getTime();

            String daysToPayFor = String.valueOf(-TimeUnit.DAYS.convert(timeDifference, TimeUnit.MILLISECONDS));
            int totalInt=0;

            if (carType.equals("Saloon/Minivan")) {
                 totalInt = Integer.parseInt(daysToPayFor) * 9;
            } else if(carType.equals("Van")) {
                 totalInt = Integer.parseInt(daysToPayFor) * 19;
            } else if(carType.equals("Truck")) {
                 totalInt = Integer.parseInt(daysToPayFor) * 29;
            }

            String totalPriceToPay = Integer.toString(totalInt);
            String reg = textFieldRegNumber.getText();

            try {
                String query = "UPDATE released SET days_total= " + daysToPayFor + ", paid= " + totalPriceToPay + " WHERE registration_number= " + reg + "  ";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void checkIfExpired() {

        Date today = new Date();
        long calculateIfExpired = datePickerEndDate.getDate().getTime() - today.getTime();

        if (calculateIfExpired < 1) {
            textFieldStatus.setText("EXPIRED!");
            textFieldStatus.setBackground(Color.RED);
        } else if (calculateIfExpired == 1) {
            textFieldStatus.setText("EXPIRES TODAY");
            textFieldStatus.setBackground(Color.YELLOW);
        } else {
            textFieldStatus.setText(TimeUnit.DAYS.convert(calculateIfExpired, TimeUnit.MILLISECONDS) + " DAYS TO GO");
            textFieldStatus.setBackground(Color.GREEN);
        }

        try {
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void updateReleasedDB() {
        String query = "INSERT INTO released (registration_number,first_name,last_name,car_type,start_date,end_date,lap_number,track_number)" +
                "VALUES(?,?,?,?,?,?,?,?)";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = dateFormat.format(datePickerStartDate.getDate());
        String endDate = dateFormat.format(datePickerEndDate.getDate());

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, textFieldRegNumber.getText());
            preparedStatement.setString(2, textFieldFirstName.getText());
            preparedStatement.setString(3, textFieldLastName.getText());
            preparedStatement.setString(4, Objects.requireNonNull(comboBoxCarSize.getSelectedItem()).toString());
            preparedStatement.setString(5, startDate);
            preparedStatement.setString(6, endDate);
            preparedStatement.setString(7, textFieldLapNo.getText());
            preparedStatement.setString(8, textFieldTrackNo.getText());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStatusInLapsDBAfterReleasedPressed() {
        String lap = textFieldLapNo.getText();
        String track = textFieldTrackNo.getText();
        String query = "UPDATE laps SET status= 'AVALIABLE' WHERE lap_number= " + lap + "  AND track_number= " + track + "  ";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStatusInLapsDB() {
        String lap = textFieldLapNo.getText();
        String track = textFieldTrackNo.getText();
        String query = "UPDATE laps SET status= 'RESERVED' WHERE lap_number= " + lap + "  AND track_number= " + track + "  ";

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTable() {

        try {
            String query = "SELECT * FROM parked";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int carRecordsCount = resultSetMetaData.getColumnCount();
            DefaultTableModel defaultTableModel = (DefaultTableModel) carsParkedTable.getModel();
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
                }
                defaultTableModel.addRow(vector);
            }
            carsParkedTable.setModel(defaultTableModel);

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

    private void setupCarSizes() {
        comboBoxCarSize.removeAllItems();
        comboBoxCarSize.addItem("Saloon/Minivan");
        comboBoxCarSize.addItem("Van");
        comboBoxCarSize.addItem("Truck");
    }

    private void clearFields() {
        textFieldRegNumber.setText("");
        textFieldFirstName.setText("");
        textFieldLastName.setText("");
        textFieldLapNo.setText("");
        textFieldTrackNo.setText("");
        comboBoxCarSize.setSelectedItem("Saloon/Minivan");
        Date today = new Date();
        datePickerStartDate.getDateEditor().setDate(today);
        datePickerEndDate.getDateEditor().setDate(today);
        getTrackNumber();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Home().setVisible(true));
    }
}
