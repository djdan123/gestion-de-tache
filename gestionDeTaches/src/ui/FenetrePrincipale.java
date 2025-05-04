package ui;
import ui.FenetreStatistiques;
import dao.DatabaseConnection;
import model.Notification;
import service.NotificationService;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FenetrePrincipale extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(FenetrePrincipale.class.getName());
    private static String currentUserRole;
    
    private final int idEmploye;
    private final String roleEmploye;
    private Timer notificationTimer;
    private int dernierIdNotif = 0;
    private JLabel badgeNotif;

    public FenetrePrincipale(int idEmploye, String roleEmploye) {
        this.idEmploye = idEmploye;
        this.roleEmploye = roleEmploye;
        currentUserRole = roleEmploye;

        setTitle("Gestion des Tâches - " + (isAdmin() ? "Administrateur" : "Employé"));
        setSize(400, 350); // Augmenté pour le badge
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
        initNotificationSystem();
    }

    private void initNotificationSystem() {
        // Initialiser le dernier ID
        try {
            dernierIdNotif = NotificationService.getDernierIdNotification(idEmploye);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur d'initialisation des notifications", e);
        }

        // Configuration du polling intelligent
        notificationTimer = new Timer(5000, e -> {
            try {
                checkNewNotifications();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Erreur de vérification des notifications", ex);
            }
        });
        notificationTimer.start();
    }

    private void checkNewNotifications() throws SQLException {
        List<Notification> nouvelles = NotificationService.getNouvellesNotifications(idEmploye, dernierIdNotif);
        
        if (!nouvelles.isEmpty()) {
            dernierIdNotif = NotificationService.getDernierIdNotification(idEmploye);
            
            SwingUtilities.invokeLater(() -> {
                // Mettre à jour le badge
                updateNotificationBadge(nouvelles.size());
                
                // Afficher une notification popup
                if (this.isVisible()) {
                    nouvelles.forEach(notif -> {
                        JOptionPane.showMessageDialog(
                            this, 
                            notif.getMessage(), 
                            "Nouvelle Notification", 
                            JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            });
        }
    }

    private void updateNotificationBadge(int count) {
        if (badgeNotif == null) {
            badgeNotif = new JLabel("", SwingConstants.CENTER);
            badgeNotif.setForeground(Color.RED);
            badgeNotif.setFont(new Font("Arial", Font.BOLD, 12));
        }
        
        badgeNotif.setText(count > 0 ? String.valueOf(count) : "");
        badgeNotif.setToolTipText(count + " nouvelles notifications");
    }

    public static boolean estAdminStatic() {
        return "admin".equals(currentUserRole);
    }

    private boolean isAdmin() {
        return "admin".equals(roleEmploye);
    }

    private void initUI() {
        // Configuration du layout principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel des boutons
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Boutons principaux
        JButton btnEmployes = createMenuButton("Gérer les employés", isAdmin());
        JButton btnProjets = createMenuButton("Gérer les projets", true);
        JButton btnTaches = createMenuButton("Gérer les tâches", true);
        JButton btnStats = createMenuButton("Statistiques", isAdmin());
        JButton btnNotifs = createMenuButton("Mes notifications", true);

        // Gestion des événements
        btnEmployes.addActionListener(e -> new FenetreEmploye().setVisible(true));
        btnProjets.addActionListener(e -> new FenetreProjet().setVisible(true));
        btnTaches.addActionListener(e -> new FenetreTache(idEmploye).setVisible(true));
        btnStats.addActionListener(e -> new FenetreStatistiques().setVisible(true));
        btnNotifs.addActionListener(e -> new FenetreNotification(idEmploye).setVisible(true));

        // Ajout des composants
        buttonPanel.add(btnEmployes);
        buttonPanel.add(btnProjets);
        buttonPanel.add(btnTaches);
        buttonPanel.add(btnStats);
        
        // Configuration du badge de notification
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        badgeNotif = new JLabel();
        updateNotificationBadge(0);
        topPanel.add(btnNotifs);
        topPanel.add(badgeNotif);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JButton createMenuButton(String text, boolean enabled) {
        JButton button = new JButton(text);
        button.setEnabled(enabled);
        button.setPreferredSize(new Dimension(200, 40));
        return button;
    }

    @Override
    public void dispose() {
        if (notificationTimer != null) {
            notificationTimer.stop();
        }
        super.dispose();
    }
}