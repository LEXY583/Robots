package model;

import java.util.Observable;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;

public class RobotModel extends Observable {
    private double x = 100;
    private double y = 100;
    private double direction = 0;
    private int targetX = 100;
    private int targetY = 100;
    private static final double MAX_VELOCITY = 0.1;
    private static final double MAX_ANGULAR_VELOCITY = 0.01;
    private boolean moving = false;
    private RobotTrail trail = new RobotTrail();
    private List<Point> shapePoints = new ArrayList<>();
    private DrawMode mode = DrawMode.POINT;
    private ShapeType shapeType = ShapeType.CIRCLE;
    
    // для рисования фигур
    private boolean isDrawingShape = false; // идет ли процесс рисования фигуры
    private List<Point> shapePath = new ArrayList<>(); // список точек фигуры
    private int currentShapePointIndex = 0; // индекс текущей целевой точки
    private boolean drawingEnabled = false; // разрешено ли добавлять точки в след (чтобы не рисовать след из середины фигуры)
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDirection() { return direction; }
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public boolean isMoving() { return moving; }
    public RobotTrail getTrail() { return trail; }
    public List<Point> getShapePoints() { return shapePoints; }
    public DrawMode getMode() { return mode; }
    public ShapeType getShapeType() { return shapeType; }
    public boolean isDrawingShape() { return isDrawingShape; }
    public List<Point> getShapePath() { return shapePath; }
    
    // устанавливаем режим
    public void setMode(DrawMode mode) { 
        this.mode = mode;
        moving = false;  // останавливаем движение при смене режима
        isDrawingShape = false;  // сбрасываем рисование фигуры
    }
    public void setShapeType(ShapeType shapeType) { this.shapeType = shapeType; } // устанавливаем тип фигуры
    
    public void setTargetPosition(int x, int y) {
        this.targetX = x;
        this.targetY = y;
        setChanged();
        notifyObservers("target_changed");
    }
    
    // очистка
    public void clearTrailAndShapes() {
        trail.clear();
        shapePoints.clear();
        shapePath.clear();
        isDrawingShape = false;
        moving = false;  // останавливаем движение
        setChanged();
        notifyObservers("cleared");
    }
    
    // начать рисование фигуры
    public void startDrawingShape(int startX, int startY) {
        shapePath.clear(); // очищаем старый маршрут
        trail.clear(); // очищаем след
        drawingEnabled = false; // след пока не рисуем
        currentShapePointIndex = 0; // начинаем с первой точки

        int size = 100; // размер фигуры

        // ставим робота в точку клика
        x = startX;
        y = startY;

        switch (shapeType) {
            case CIRCLE:
                for (int i = 0; i <= 36; i++) {
                    double angle = 2 * Math.PI * i / 36;
                    int px = startX + (int)(size / 2 * Math.cos(angle));
                    int py = startY + (int)(size / 2 * Math.sin(angle));
                    shapePath.add(new Point(px, py));
                }
                break;

            case SQUARE:
                int halfSize = size / 2;
                shapePath.add(new Point(startX - halfSize, startY - halfSize));
                shapePath.add(new Point(startX + halfSize, startY - halfSize));
                shapePath.add(new Point(startX + halfSize, startY + halfSize));
                shapePath.add(new Point(startX - halfSize, startY + halfSize));
                shapePath.add(new Point(startX - halfSize, startY - halfSize));
                break;

            case TRIANGLE:
                int height = (int)(size * Math.sqrt(3) / 2);
                shapePath.add(new Point(startX, startY - height / 2));
                shapePath.add(new Point(startX - size / 2, startY + height / 2));
                shapePath.add(new Point(startX + size / 2, startY + height / 2));
                shapePath.add(new Point(startX, startY - height / 2));
                break;
        }

        if (shapePath.size() >= 1) {
            isDrawingShape = true; // включаем режим рисования

            // сначала едем к 1й точке фигуры
            currentShapePointIndex = 0;

            Point firstTarget = shapePath.get(0);
            targetX = firstTarget.x;
            targetY = firstTarget.y;

            moving = true; // начинаем движение

            setChanged();
            notifyObservers("drawing_started");
        }
    }
    
    public void setTargetAndMove(int x, int y) {
        this.targetX = x;
        this.targetY = y;
        moving = true;
        setChanged();
        notifyObservers("target_set");
    }
    
    // логика движения робота
    public void updateMovement(double duration) {
        if (!moving) return;

        double distance = distanceToTarget();

        // если достигли цели
        if (distance < 5.0) {
            // фиксируем позицию
            x = targetX;
            y = targetY;

            if (isDrawingShape) {

                // включаем рисование только после достижения первой точки
                if (currentShapePointIndex == 0) {
                    drawingEnabled = true;
                }

                currentShapePointIndex++;

                if (currentShapePointIndex < shapePath.size()) {
                    Point nextPoint = shapePath.get(currentShapePointIndex);
                    targetX = nextPoint.x;
                    targetY = nextPoint.y;
                } else {
                    // фигура завершена
                    isDrawingShape = false;
                    moving = false;
                    drawingEnabled = false;

                    setChanged();
                    notifyObservers("shape_completed");
                }
            } else {
                moving = false;

                setChanged();
                notifyObservers("stopped");
            }
            return;
        }

        // движение к цели
        double angleToTarget = angleToTarget();
        double angularVelocity = calculateAngularVelocity(angleToTarget);
        double velocity = calculateVelocity(angleToTarget);

        moveRobot(velocity, angularVelocity, duration);
    }
    
    private double calculateAngularVelocity(double angleToTarget) {
        double angleDifference = angleToTarget - direction;
        while (angleDifference > Math.PI) angleDifference -= 2 * Math.PI;
        while (angleDifference < -Math.PI) angleDifference += 2 * Math.PI;
        
        if (Math.abs(angleDifference) < 0.05) return 0;
        double angularSpeed = MAX_ANGULAR_VELOCITY;
        if (Math.abs(angleDifference) < 0.3) angularSpeed = MAX_ANGULAR_VELOCITY * 0.5;
        return angleDifference > 0 ? angularSpeed : -angularSpeed;
    }
    
    private double calculateVelocity(double angleToTarget) {
        double angleDifference = Math.abs(normalizeAngle(angleToTarget - direction));
        if (angleDifference < 0.2) return MAX_VELOCITY;
        if (angleDifference < 0.5) return MAX_VELOCITY * 0.8;
        return MAX_VELOCITY * 0.5;
    }
    
    private void moveRobot(double velocity, double angularVelocity, double duration) {

        if (drawingEnabled) {
            trail.addPoint(x, y);
        }

        velocity = applyLimits(velocity, 0, MAX_VELOCITY);
        angularVelocity = applyLimits(angularVelocity, -MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);
        
        double newX = x, newY = y;
        
        if (Math.abs(angularVelocity) < 0.0001) {
            newX = x + velocity * duration * Math.cos(direction);
            newY = y + velocity * duration * Math.sin(direction);
        } else {
            newX = x + velocity / angularVelocity * 
                (Math.sin(direction + angularVelocity * duration) - Math.sin(direction));
            newY = y - velocity / angularVelocity * 
                (Math.cos(direction + angularVelocity * duration) - Math.cos(direction));
        }
        
        double newDirection = normalizeAngle(direction + angularVelocity * duration);
        
        this.x = newX;
        this.y = newY;
        this.direction = newDirection;
        
        setChanged();
        notifyObservers("moved");
    }
    
    private double distanceToTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private double angleToTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        return normalizeAngle(Math.atan2(dy, dx));
    }
    
    private double normalizeAngle(double angle) {
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        return angle;
    }
    
    private double applyLimits(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}