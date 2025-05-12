package ui;

import model.Notification;
import javax.swing.*;
import java.awt.*;

public class NotificationRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                 boolean isSelected, boolean cellHasFocus) {
        Notification notif = (Notification) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        // Style différent pour les notifications non lues
        if (!notif.isLue()) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setBackground(new Color(230, 240, 255));
        }
        
        return label;
    }
}