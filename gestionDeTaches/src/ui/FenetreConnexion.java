package ui;

import dao.DatabaseConnection;
import model.Employe;
import org.apache.commons.codec.digest.DigestUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class FenetreConnexion extends JFrame {
    private JTextField tfNom;
    private JPasswordField pfPassword;

    public FenetreConnexion() {
        setTitle("Connexion");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Nom:"));
        tfNom = new JTextField();
        panel.add(tfNom);

        panel.add(new JLabel("Mot de passe:"));
        pfPassword = new JPasswordField();
        panel.add(pfPassword);

        JButton btnConnexion = new JButton("Se connecter");
        btnConnexion.addActionListener(e -> connecter());
        panel.add(btnConnexion);

        add(panel);
    }

    private void connecter() {
        String nom = tfNom.getText().trim();
        String mdp = new String(pfPassword.getPassword());

        if (nom.isEmpty() || mdp.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez remplir tous les champs", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, nom, prenom, role FROM Employe WHERE nom = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            // Hashage du mot de passe saisi
            String mdpHash = DigestUtils.sha256Hex(mdp);
            
            ps.setString(1, nom);
            ps.setString(2, mdpHash);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                
                new FenetrePrincipale(id, role).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Identifiants incorrects",
                    "Échec de connexion",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Erreur de base de données: " + ex.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}