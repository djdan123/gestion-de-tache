
package model;
import java.util.Date;

/**
 *
 * @au
*/
public class Notification {
    private int id;
    private String message;
    private Date date;
    private int idEmploye;
    private boolean lue;
        public Notification(int id, String message, Date date, int idEmploye, boolean lue){
            this.id = id;
            this.message = message;
            this.date = date;
            this.idEmploye = idEmploye;
            this.lue = lue;
        }
        // Getters et Setters 
        public int getId(){ return id; }
        public String getMessage(){ return message; }
        public Date getDate() { return date;}
        public int getIdEmploye(){ return idEmploye;}
        public boolean isLue(){ return lue; }
        public void setLue(boolean lue) { this.lue = lue;}
        
        @Override
        public String toString() {
        return (lue ? "[LUE]" : "[NON LUE]")+ message + "("+ date +")";
        }
        }

    

