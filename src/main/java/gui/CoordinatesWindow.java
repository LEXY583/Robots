package gui;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import model.RobotModel;

public class CoordinatesWindow extends JInternalFrame implements Observer {
    private final RobotModel model;
    private final JLabel xLabel;
    private final JLabel yLabel;
    private final JLabel directionLabel;
    private final JLabel targetXLabel;
    private final JLabel targetYLabel;
    private final JLabel distanceLabel;
    
    public CoordinatesWindow(RobotModel model) {
        super("Координаты робота", true, true, true, true);
        this.model = model;
        
        // подписываемся на обновления модели
        model.addObserver(this);
        
        // создаем панель с информацией
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // добавляем компоненты
        panel.add(new JLabel("Координата X:"));
        xLabel = new JLabel(String.format("%.2f", model.getX()));
        panel.add(xLabel);
        
        panel.add(new JLabel("Координата Y:"));
        yLabel = new JLabel(String.format("%.2f", model.getY()));
        panel.add(yLabel);
        
        panel.add(new JLabel("Направление (град):"));
        directionLabel = new JLabel(String.format("%.1f", Math.toDegrees(model.getDirection())));
        panel.add(directionLabel);
        
        panel.add(new JLabel("Цель X:"));
        targetXLabel = new JLabel(String.valueOf(model.getTargetX()));
        panel.add(targetXLabel);
        
        panel.add(new JLabel("Цель Y:"));
        targetYLabel = new JLabel(String.valueOf(model.getTargetY()));
        panel.add(targetYLabel);
        
        panel.add(new JLabel("Расстояние до цели:"));
        distanceLabel = new JLabel(String.format("%.2f", calculateDistance()));
        panel.add(distanceLabel);
        
        getContentPane().add(panel);
        setSize(300, 200);
        setLocation(350, 10);
    }
    
    private double calculateDistance() {
        double dx = model.getTargetX() - model.getX();
        double dy = model.getTargetY() - model.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    @Override
    public void update(Observable o, Object arg) {
        // обновляем все значения в потоке Swing
        SwingUtilities.invokeLater(() -> {
            xLabel.setText(String.format("%.2f", model.getX()));
            yLabel.setText(String.format("%.2f", model.getY()));
            directionLabel.setText(String.format("%.1f", Math.toDegrees(model.getDirection())));
            targetXLabel.setText(String.valueOf(model.getTargetX()));
            targetYLabel.setText(String.valueOf(model.getTargetY()));
            distanceLabel.setText(String.format("%.2f", calculateDistance()));
        });
    }
}