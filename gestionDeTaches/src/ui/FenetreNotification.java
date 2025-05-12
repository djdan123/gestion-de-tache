package ui;

import model.Notification;
import model.Tache;
import dao.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FenetreNotification extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(FenetreNotification.class.getName());
    
    private final DefaultListModel<Notification> model;
    private final JList<Notification> liste;
    private DefaultListModel< Tache > Tachemodel;
    private JList< Tache > Tacheliste;
    private final JButton marquerCommeLue, supprimerToutes;
    private final int idEmployeConnecte;
    private JButton actualiser;

    public FenetreNotification(int idEmployeConnecte) {
        this.idEmployeConnecte = idEmployeConnecte;
        
        setTitle("Mes Notifications");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Configuration du modèle et de la liste
        model = new DefaultListModel<>();
        liste = new JList<>(model);
        Tachemodel = new DefaultListModel<>();
        Tacheliste = new JList<>(Tachemodel);

        liste.setCellRenderer(new NotificationRenderer());
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Configuration des boutons
        marquerCommeLue = new JButton("Marquer comme lue");
        supprimerToutes = new JButton("Supprimer toutes");
        actualiser = new JButton("Actualiser");
        
        // Panel des boutons
        JPanel boutonsPanel = new JPanel(new FlowLayout());
        boutonsPanel.add(marquerCommeLue);
        boutonsPanel.add(supprimerToutes);
        boutonsPanel.add(actualiser);
        
        // Gestionnaires d'événements
        marquerCommeLue.addActionListener(e -> marquerCommeLue());
        supprimerToutes.addActionListener(e -> supprimerToutesNotifications());
        actualiser.addActionListener(e -> chargerNotifications());
        
        // Configuration du layout
        setLayout(new BorderLayout());
        add(new JScrollPane(liste), BorderLayout.CENTER);
        add(boutonsPanel, BorderLayout.SOUTH);
        
        // Chargement initial et timer d'actualisation
        chargerNotifications();
        demarrerAutoActualisation();
    }

    private void chargerNotifications() {
        model.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Notification WHERE id_employe = ? ORDER BY date DESC");
            ps.setInt(1, idEmployeConnecte);
            
            ResultSet rs = ps.executeQuery();
            boolean hasUnread = false;
            
            while (rs.next()) {
                Notification notif = new Notification(
                    rs.getInt("id"),
                    rs.getString("message"),
                    rs.getTimestamp("date"),
                    rs.getInt("id_employe"),
                    rs.getBoolean("lue")
                );
                model.addElement(notif);
                if (!notif.isLue()) hasUnread = true;
            }
            
            if (model.isEmpty()) {
                model.addElement(new Notification(-1, "Aucune notification", null, idEmployeConnecte, true));
            }
            
            marquerCommeLue.setEnabled(hasUnread);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de chargement des notifications", e);
            model.addElement(new Notification(-1, "Erreur de chargement", null, -1, true));
        }
    }

    private void marquerCommeLue() {
        Notification notif = liste.getSelectedValue();
        if (notif != null && !notif.isLue()) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Notification SET lue = TRUE WHERE id = ?");
                ps.setInt(1, notif.getId());
                ps.executeUpdate();
                
                notif.setLue(true);
                liste.repaint();
                conn.commit();
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du marquage comme lue", e);
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la mise à jour",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void supprimerToutesNotifications() {
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Supprimer toutes les notifications ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Notification WHERE id_employe = ?");
                ps.setInt(1, idEmployeConnecte);
                ps.executeUpdate();
                chargerNotifications();
                
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur de suppression", e);
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void demarrerAutoActualisation() {
        Timer timer = new Timer(10000, e -> chargerNotifications()); // Actualise toutes les 10s
        timer.setRepeats(true);
        timer.start();
    }

    // Renderer personnalisé pour les notifications
    private static class NotificationRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Notification) {
                Notification notif = (Notification) value;
                setText(formatNotificationText(notif));
                
                if (!notif.isLue()) {
                    setFont(getFont().deriveFont(Font.BOLD));
                    setBackground(new Color(230, 240, 255));
                } else {
                    setForeground(Color.GRAY);
                }
            }
            return this;
        }

        private String formatNotificationText(Notification notif) {
            return String.format("<html><b>%s</b><br><small>%s</small></html>",
                notif.getMessage(),
                notif.getDate() != null ? notif.getDate().toString() : "Date inconnue");
        }
        
}
    private void chargerTaches() {
        Tachemodel.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.id, t.libelle, t.id_projet, t.id_employe, " +
                         "t.statut, t.date_creation, p.nom AS projet_nom, " +
                         "CONCAT(e.nom, ' ', e.prenom) AS employe_nom " +
                         "FROM Tache t " +
                         "LEFT JOIN Projet p ON t.id_projet = p.id " +
                         "LEFT JOIN Employe e ON t.id_employe = e.id " +
                         "ORDER BY t.date_creation DESC";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Tache tache = new Tache (
                    rs.getInt("id"),
                    rs.getString("libelle"),
                    rs.getInt("id_projet"),
                    rs.getInt("id_employe"),
                    rs.getString("projet_nom"),
                    rs.getString("employe_nom"),
                    rs.getTimestamp("date_creation"),
                    rs.getString("statut")
                );
                Tachemodel.addElement(tache);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur de chargement des tâches: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        
    }
}