package service;

import dao.DatabaseConnection;
import model.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    public static List<Notification> getNouvellesNotifications(int idEmploye, int dernierId) throws SQLException {
        List<Notification> nouvelles = new ArrayList<>();
        String sql = "SELECT id, message, date FROM Notification WHERE id_employe = ? AND id > ? AND lue = FALSE ORDER BY date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEmploye);
            ps.setInt(2, dernierId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                nouvelles.add(new Notification(
                    rs.getInt("id"),
                    rs.getString("message"),
                    rs.getTimestamp("date"),
                    idEmploye,
                    false
                ));
            }
        }
        return nouvelles;
    }

    public static int getDernierIdNotification(int idEmploye) throws SQLException {
        String sql = "SELECT MAX(id) FROM Notification WHERE id_employe = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEmploye);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}