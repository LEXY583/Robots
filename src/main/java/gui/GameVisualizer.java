package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import model.RobotModel;

public class GameVisualizer extends JPanel implements Observer {
    private final Timer m_timer = initTimer();
    private final RobotModel model;

    private static Timer initTimer() {
        Timer timer = new Timer("events generator", true);
        return timer;
    }
    
    public GameVisualizer(RobotModel model) {
        this.model = model;
        
        // подписываемся на обновления модели
        model.addObserver(this);

        // таймер для перерисовки
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onRedrawEvent();
            }
        }, 0, 50);

        // таймер для обновления модели
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onModelUpdateEvent();
            }
        }, 0, 10);

        // обработчик кликов мыши
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setTargetPosition(e.getPoint());
            }
        });
        setDoubleBuffered(true);
    }

    protected void setTargetPosition(Point p) {
        model.setTargetAndMove(p.x, p.y);
        repaint();
    }
    
    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    protected void onModelUpdateEvent() {
        model.updateMovement(10);
    }
    
    @Override
    public void update(Observable o, Object arg) {
        repaint();
    }

    private static int round(double value) {
        return (int)(value + 0.5);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        drawRobot(g2d, round(model.getX()), round(model.getY()), model.getDirection());
        drawTarget(g2d, model.getTargetX(), model.getTargetY());
    }
    
    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }
    
    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2) {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction) { 
        AffineTransform old = g.getTransform();
        g.rotate(direction, x, y);

        g.setColor(Color.MAGENTA); 
        fillOval(g, x, y, 30, 10); 
        g.setColor(Color.BLACK); 
        drawOval(g, x, y, 30, 10); 
        g.setColor(Color.WHITE); 
        fillOval(g, x + 10, y, 5, 5); 
        g.setColor(Color.BLACK); 
        drawOval(g, x + 10, y, 5, 5); 
        
        g.setTransform(old); 
    } 

    private void drawTarget(Graphics2D g, int x, int y) { 
        g.setColor(Color.GREEN); 
        fillOval(g, x, y, 5, 5); 
        g.setColor(Color.BLACK); 
        drawOval(g, x, y, 5, 5); 
    }
}
