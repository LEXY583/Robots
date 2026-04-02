package model;

import java.io.Serializable;
import javax.swing.JInternalFrame;

// Класс хранения состояния окна
public class WindowPosition implements Serializable { 

    private static final long serialVersionUID = 1L;
    
    // положения окна на экране
    public int x;
    public int y;
    // параметры окна
    public int width;
    public int height;
    public boolean isIcon; // свернуто/развернуто
    public boolean isMaximum; // развернуто на весь экран
    
    public WindowPosition() {}
    
    public WindowPosition(JInternalFrame frame) {
        this.x = frame.getX();
        this.y = frame.getY();
        this.width = frame.getWidth();
        this.height = frame.getHeight();
        this.isIcon = frame.isIcon();
        this.isMaximum = frame.isMaximum();
    }
    
    public void applyTo(JInternalFrame frame) {
        frame.setLocation(x, y);
        frame.setSize(width, height);
        try {
            if (isMaximum) {
                frame.setMaximum(true);
            } else if (isIcon) {
                frame.setIcon(true);
            }
        } catch (Exception e) {}
    }
}