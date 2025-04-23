package ui;

import javax.swing.*;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class FenetrePrincipale extends JFrame {
    public FenetrePrincipale() {
        setTitle("Gestion des Tâches");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton btnEmploye = new JButton("Gérer les Employés");
        JButton btnProjet = new JButton("Gérer les Projets");
        JButton btnTache = new JButton("Gérer les Tâches");

        btnEmploye.addActionListener(e -> new FenetreEmploye().setVisible(true));
        btnProjet.addActionListener(e -> new FenetreProjet().setVisible(true));
        btnTache.addActionListener(e -> new FenetreTache().setVisible(true));

        JPanel panel = new JPanel();
        panel.add(btnEmploye);
        panel.add(btnProjet);
        panel.add(btnTache);

        add(panel);
    }
}