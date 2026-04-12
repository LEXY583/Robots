package model;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

public class RobotTrail {
    private static final int MAX_SIZE = 1000; // максимальная длина следа
    private final Queue<Point> trailPoints = new LinkedList<>(); // хранилище точек
    
    // метод добавления точки в след
    public void addPoint(double x, double y) {
        Point p = new Point((int)x, (int)y);
        trailPoints.add(p);
        if (trailPoints.size() > MAX_SIZE) {
            trailPoints.poll();
        }
    }
    
    // метод отрисовки следа
    public void draw(Graphics2D g) {
        g.setColor(Color.GREEN);
        g.setStroke(new BasicStroke(3));
        
        Point prev = null;
        for (Point p : trailPoints) {
            if (prev != null) {
                g.drawLine(prev.x, prev.y, p.x, p.y);
            }
            prev = p;
        }
        
        g.setStroke(new BasicStroke(1));
    }
    
    // очистка следа
    public void clear() {
        trailPoints.clear();
    }
}