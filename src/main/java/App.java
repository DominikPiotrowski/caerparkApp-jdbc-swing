import java.awt.*;

public class App {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Login loginForm = new Login();
            loginForm.setVisible(true);
        });
    }
}