/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author dan
 */
import model.Notification;
import dao.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Date;
public class FenetreNotification extends JFrame {
    private DefaultListModel<Notification> model;
    private JList<Notification>liste;
    private JButton marquerCommeLue, supprimer;
    private int idEmployeConnecte;
public FenetreNotification(int idEmployeConnecte){
       this.idEmployeConnecte = idEmployeConnecte;
       setTitle ("Mes notifications");
       setSize(600,400);
       setLocationRelativeTo(null);
       setDefaultCloseOperation(DISPOSE_ON_CLOSE);
       
       model = new DefaultListModel<>();
       liste = new JList<>(model);
       chargerNotifications();
       
       marquerCommeLue = new JButton ("Marque comme lue");
       supprimer = new JButton ("supprimer");
       marquerCommeLue.addActionListener(e-> marquerCommeLue());
       supprimer.addActionListener(e-> supprimerNotification());
       JPanel boutons = new JPanel();
       boutons.add(marquerCommeLue);
       boutons.add(supprimer);
       
       add(new JScrollPane(liste),BorderLayout.CENTER);
       add(boutons, BorderLayout.SOUTH);
       
}
private void chargerNotifications(){
    model.clear();
    try(Connection conn = DatabaseConnection.getConnection()) {
        PreparedStatement ps = conn.prepareStatement 
        ("SELECT * FROM Notification WHERE id_employe = ? ORDER BY date DESC" );
                ps.setInt(1,idEmployeConnecte);
                ResultSet rs = ps.executeQuery();
                while (rs.next()){
                    model.addElement(new Notification(
                    rs.getInt("id"),
                    rs.getString("message"),
                    rs.getTimestamp("date"),
                    rs.getInt("id_employe"),
                    rs.getBoolean("lue")
                    ));
                }   
    }catch (Exception e){
        e.printStackTrace();
        
    
    }

}
   private void marquerCommeLue(){
        Notification notif = liste.getSelectedValue();
        if(notif != null && !notif.isLue()){
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE Notification SET lue = TRUE WHERE  id=?");
                ps.setInt(1, notif.getId());
                ps.executeUpdate();
                notif.setLue(true);
                liste.repaint();//rafraichir l'affichage
            } catch (Exception e){
            e.printStackTrace();
            }
        }
   }
   private void supprimerNotification(){
       Notification notif = liste.getSelectedValue();
       if (notif != null && JOptionPane.showConfirmDialog(this, "supprimer cette"
               + " notification ?")== JOptionPane.YES_OPTION){
                try (Connection conn = DatabaseConnection.getConnection())
                {   
                    PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM Notification WHERE id= ?");
                    ps.setInt(1, notif.getId());
                    ps.executeUpdate();
                    model.removeElement(notif);
                } catch (Exception e){
                    e.printStackTrace();
                }
           
       }
   }
}
