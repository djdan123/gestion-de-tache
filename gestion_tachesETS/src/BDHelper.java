import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BDHelper {
    private static Connection conn;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                String url = "jdbc:mysql://localhost:3306/gestion_taches";
                String user = "Dan_user";
                String password = "MATONDO"; // ton mot de passe

                conn = DriverManager.getConnection(url, user, password);
                System.out.println("✅ Connexion réussie !");
            } catch (SQLException e) {
                System.out.println("❌ Erreur de connexion : " + e.getMessage());
            }
        }
        return conn;
    }
}
