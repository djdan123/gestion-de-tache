package ui;

import model.Tache;
import model.Employe;
import model.Projet;
import dao.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class FenetreTache extends JFrame {
    private DefaultListModel<Tache> model;
    private JList<Tache> liste;
    private JButton ajouter, modifier, supprimer;
    private int currentUserId; // Pour suivre l'utilisateur connecté

    public FenetreTache(int userId) {
        this.currentUserId = userId;
        setTitle("Gestion des Tâches");
        setSize(600, 400); // Taille augmentée pour meilleure visibilité
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        chargerTaches();
    }

    private void initComponents() {
        model = new DefaultListModel<>();
        liste = new JList<>(model);
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        liste.setCellRenderer(new TacheRenderer());

        ajouter = new JButton("Ajouter", new ImageIcon("icons/add.png"));
        modifier = new JButton("Modifier", new ImageIcon("icons/edit.png"));
        supprimer = new JButton("Supprimer", new ImageIcon("icons/delete.png"));

        // Tooltips
        ajouter.setToolTipText("Ajouter une nouvelle tâche");
        modifier.setToolTipText("Modifier la tâche sélectionnée");
        supprimer.setToolTipText("Supprimer la tâche sélectionnée");

        // Gestionnaires d'événements
        ajouter.addActionListener(e -> ajouterTache());
        modifier.addActionListener(e -> modifierTache());
        supprimer.addActionListener(e -> supprimerTache());

        // Panel des boutons
        JPanel boutonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        boutonsPanel.add(ajouter);
        boutonsPanel.add(modifier);
        boutonsPanel.add(supprimer);
        ajouter.setBackground(Color.BLUE);
        ajouter.setPreferredSize(new Dimension(100,100));
        boutonsPanel.setBackground(Color.red);
        // Configuration principale
        add(new JScrollPane(liste), BorderLayout.CENTER);
        add(boutonsPanel, BorderLayout.SOUTH);
    }

    private void chargerTaches() {
        model.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.*, p.nom as projet_nom, e.nom as employe_nom, e.prenom " +
                         "FROM Tache t " +
                         "JOIN Projet p ON t.id_projet = p.id " +
                         "JOIN Employe e ON t.id_employe = e.id";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                model.addElement(new Tache(
                    rs.getInt("id"),
                    rs.getString("libelle"),
                    rs.getInt("id_projet"),
                    rs.getInt("id_employe"),
                    rs.getString("projet_nom"),
                    rs.getString("employe_nom") + " " + rs.getString("prenom")
                ));
            }
        } catch (SQLException e) {
            showError("Erreur de chargement", e);
        }
    }

    private void ajouterTache() {
        Vector<Projet> projets = chargerProjets();
        Vector<Employe> employes = chargerEmployes();
        
        if (projets.isEmpty() || employes.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Aucun projet ou employé disponible", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Création du formulaire
        JComboBox<Projet> cbProjet = new JComboBox<>(projets);
        JComboBox<Employe> cbEmploye = new JComboBox<>(employes);
        JTextField tfLibelle = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Libellé*:"));
        panel.add(tfLibelle);
        panel.add(new JLabel("Projet*:"));
        panel.add(cbProjet);
        panel.add(new JLabel("Assigné à*:"));
        panel.add(cbEmploye);
        panel.add(new JLabel("* Champs obligatoires"));

        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Nouvelle Tâche", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String libelle = tfLibelle.getText().trim();
            if (libelle.isEmpty()) {
                showError("Le libellé est obligatoire", null);
                return;
            }
            
            Projet projet = (Projet) cbProjet.getSelectedItem();
            Employe employe = (Employe) cbEmploye.getSelectedItem();
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                // 1. Insertion de la tâche
                String sqlTache = "INSERT INTO Tache (libelle, id_projet, id_employe) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlTache, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, libelle);
                    ps.setInt(2, projet.getId());
                    ps.setInt(3, employe.getId());
                    ps.executeUpdate();
                    
                    // 2. Création de la notification
                    String message = String.format(
                        "Nouvelle tâche assignée: %s\nProjet: %s\nAssigné par: Vous",
                        libelle, projet.getNom());
                    
                    createNotification(conn, employe.getId(), message, currentUserId);
                }
                
                conn.commit();
                chargerTaches();
                
            } catch (SQLException e) {
                showError("Erreur lors de la création", e);
            }
        }
    }

    private void modifierTache() {
        Tache tache = liste.getSelectedValue();
        if (tache == null) {
            showError("Aucune tâche sélectionnée", null);
            return;
        }
        
        Vector<Projet> projets = chargerProjets();
        Vector<Employe> employes = chargerEmployes();
        
        JComboBox<Projet> cbProjet = new JComboBox<>(projets);
        JComboBox<Employe> cbEmploye = new JComboBox<>(employes);
        JTextField tfLibelle = new JTextField(tache.getLibelle());
        
        // Sélectionner les valeurs actuelles
        cbProjet.setSelectedItem(findProjetById(projets, tache.getIdProjet()));
        cbEmploye.setSelectedItem(findEmployeById(employes, tache.getIdEmploye()));
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Libellé:"));
        panel.add(tfLibelle);
        panel.add(new JLabel("Projet:"));
        panel.add(cbProjet);
        panel.add(new JLabel("Assigné à:"));
        panel.add(cbEmploye);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Modifier Tâche", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String libelle = tfLibelle.getText().trim();
            if (libelle.isEmpty()) {
                showError("Le libellé ne peut être vide", null);
                return;
            }
            
            Projet newProjet = (Projet) cbProjet.getSelectedItem();
            Employe newEmploye = (Employe) cbEmploye.getSelectedItem();
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                // 1. Mise à jour de la tâche
                String sql = "UPDATE Tache SET libelle=?, id_projet=?, id_employe=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, libelle);
                    ps.setInt(2, newProjet.getId());
                    ps.setInt(3, newEmploye.getId());
                    ps.setInt(4, tache.getId());
                    ps.executeUpdate();
                    
                    // 2. Notification si changement d'assignation
                    if (tache.getIdEmploye() != newEmploye.getId()) {
                        String message = String.format(
                            "Tâche réassignée: %s\nNouveau responsable: %s %s\nProjet: %s",
                            libelle, newEmploye.getNom(), newEmploye.getPrenom(), newProjet.getNom());
                        
                        createNotification(conn, newEmploye.getId(), message, currentUserId);
                        
                        // Notification à l'ancien responsable
                        String oldMessage = String.format(
                            "Tâche retirée: %s\nNouveau responsable: %s %s",
                            libelle, newEmploye.getNom(), newEmploye.getPrenom());
                        
                        createNotification(conn, tache.getIdEmploye(), oldMessage, currentUserId);
                    }
                }
                
                conn.commit();
                chargerTaches();
                
            } catch (SQLException e) {
                showError("Erreur lors de la modification", e);
            }
        }
    }

    private void supprimerTache() {
        Tache tache = liste.getSelectedValue();
        if (tache == null) {
            showError("Aucune tâche sélectionnée", null);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment supprimer cette tâche?\n" + tache.getLibelle(),
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Notification avant suppression
                String message = String.format(
                    "Tâche supprimée: %s\nProjet: %s",
                    tache.getLibelle(), tache.getProjetNom());
                
                createNotification(conn, tache.getIdEmploye(), message, currentUserId);
                
                // Suppression de la tâche
                String sql = "DELETE FROM Tache WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, tache.getId());
                    ps.executeUpdate();
                }
                
                chargerTaches();
                
            } catch (SQLException e) {
                showError("Erreur lors de la suppression", e);
            }
        }
    }

    // Méthodes utilitaires
    private void createNotification(Connection conn, int employeId, String message, int senderId) throws SQLException {
        String sql = "INSERT INTO Notification (message, id_employe, id_sender, date) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, message);
            ps.setInt(2, employeId);
            ps.setInt(3, senderId);
            ps.executeUpdate();
        }
    }

    private Projet findProjetById(Vector<Projet> projets, int id) {
        for (Projet p : projets) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private Employe findEmployeById(Vector<Employe> employes, int id) {
        for (Employe e : employes) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    private void showError(String message, Exception e) {
        if (e != null) {
            message += "\n" + e.getMessage();
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    // Classes internes
    private static class TacheRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                    boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Tache) {
                Tache t = (Tache) value;
                setText(String.format("<html><b>%s</b> (Projet: %s)<br>Assigné à: %s</html>",
                    t.getLibelle(), t.getProjetNom(), t.getEmployeNom()));
            }
            return this;
        }
    }
    private Vector<Projet> chargerProjets() {
    Vector<Projet> projets = new Vector<>();
    try (Connection conn = DatabaseConnection.getConnection()) {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM Projet");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            projets.add(new Projet(
                rs.getInt("id"),
                rs.getString("nom")
            ));
        }
    } catch (SQLException e) {
        showError("Erreur de chargement des projets", e);
    }
    return projets;
}

private Vector<Employe> chargerEmployes() {
    Vector<Employe> employes = new Vector<>();
    try (Connection conn = DatabaseConnection.getConnection()) {
        PreparedStatement ps = conn.prepareStatement("SELECT id, nom, prenom FROM Employe");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            employes.add(new Employe(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom")
            ));
        }
    } catch (SQLException e) {
        showError("Erreur de chargement des employés", e);
    }
    return employes;
}
}