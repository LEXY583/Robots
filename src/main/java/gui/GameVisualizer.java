package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout; 
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
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JComboBox;
import javax.swing.ButtonGroup;

import model.DrawMode;
import model.RobotModel;
import model.ShapeType;

public class GameVisualizer extends JPanel implements Observer {
    private final Timer m_timer = initTimer();
    private final RobotModel model;
    private final MainApplicationFrame parentFrame;

    // компоненты для управления режимами робота
    private JToggleButton pointModeButton; // кнопка Точка
    private JToggleButton shapeModeButton; // кнопка Фигура
    private JComboBox<ShapeType> shapeTypeComboBox; // выпадающий список фигур

    private static Timer initTimer() {
        Timer timer = new Timer("events generator", true);
        return timer;
    }
    
    public GameVisualizer(RobotModel model, MainApplicationFrame frame) {
        this.model = model;
        this.parentFrame = frame;
        
        model.addObserver(this);

        initUI(); // создаем интерфейс
        startTimers(); // запускаем таймеры
    }

    // создание окошка игры
    private void initUI() {
        setLayout(new BorderLayout()); // разбиваем окно на области
        add(createControlPanel(), BorderLayout.NORTH); // создаем панель с кнопками
        add(createDrawingPanel(), BorderLayout.CENTER); // создаем поле для рисования
    }
    
    // создание панели режимов
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        createModeButtons(); // создаем кнопки
        createShapeComboBox(); // создаем выпадающий список
        
        controlPanel.add(pointModeButton);
        controlPanel.add(shapeModeButton);
        controlPanel.add(shapeTypeComboBox);
        
        return controlPanel;
    }

    // создание кнопок 
    private void createModeButtons() {
        pointModeButton = new JToggleButton("🎯 Точка", true); // установлена по умолчанию
        shapeModeButton = new JToggleButton("✏️ Фигура", false); 
        
        ButtonGroup modeGroup = new ButtonGroup(); // группа: только одна кнопка активна
        modeGroup.add(pointModeButton);
        modeGroup.add(shapeModeButton);
        
        pointModeButton.addActionListener(e -> switchToPointMode());
        shapeModeButton.addActionListener(e -> switchToShapeMode());
    }
    
    // создание выпадающего списка
    private void createShapeComboBox() {
        shapeTypeComboBox = new JComboBox<>(ShapeType.values());
        shapeTypeComboBox.setEnabled(false); // по умолчанию не активен
        shapeTypeComboBox.addActionListener(e -> {
            if (shapeModeButton.isSelected()) {
                model.setShapeType((ShapeType) shapeTypeComboBox.getSelectedItem());
                model.clearTrailAndShapes(); // очищаем след при смене типа
            }
        });
    }
    
    // создание панели рисования
    private JPanel createDrawingPanel() {
        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);
            }
        };
    
    drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleFieldClick(e.getPoint());
            }
        });
        
        return drawingPanel;
    }
    
    private void startTimers() {
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onRedrawEvent();
            }
        }, 0, 50);

        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onModelUpdateEvent();
            }
        }, 0, 10);
    }

    private void switchToPointMode() {
        model.setMode(DrawMode.POINT);
        shapeTypeComboBox.setEnabled(false);
        model.clearTrailAndShapes();
    }
    
    private void switchToShapeMode() {
        model.setMode(DrawMode.SHAPE);
        shapeTypeComboBox.setEnabled(true);
        model.clearTrailAndShapes();
    }
    
    private void handleFieldClick(Point clickPoint) {
        if (model.getMode() == DrawMode.POINT) {
            setTargetPosition(clickPoint);
        } else {
            startDrawingShape(clickPoint);
        }
    }
    
    private void startDrawingShape(Point clickPoint) {
        ShapeType selectedShape = (ShapeType) shapeTypeComboBox.getSelectedItem();
        model.setShapeType(selectedShape);
        model.startDrawingShape(clickPoint.x, clickPoint.y);
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

    private void drawGame(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        
        g2d.setStroke(new BasicStroke(3));
        
        model.getTrail().draw(g2d);
        
        g2d.setColor(Color.GRAY);
        drawRobot(g2d, round(model.getX()), round(model.getY()), model.getDirection());
        
        if (model.getMode() == DrawMode.POINT) {
            drawTarget(g2d, model.getTargetX(), model.getTargetY());
        }
        
        g2d.setStroke(new BasicStroke(1));
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