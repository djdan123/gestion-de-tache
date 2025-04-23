import ui.FenetreConnexion;
        import javax.swing.*;                   

public class Main {
    public static void main(String[] args) {
        // Initialisation Swing thread-safe
        SwingUtilities.invokeLater(() -> {
            FenetreConnexion fenetre = new FenetreConnexion();
            fenetre.setVisible(true);
        });
    }
}