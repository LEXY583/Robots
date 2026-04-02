package model;

import java.util.Observable;

public class RobotModel extends Observable {
    // координаты и направление робота
    private double x = 100;
    private double y = 100;
    private double direction = 0;
    
    // целевая точка
    private int targetX = 150;
    private int targetY = 100;
    
    // константы движения
    private static final double MAX_VELOCITY = 0.1;
    private static final double MAX_ANGULAR_VELOCITY = 0.01;
    
    // флаг для остановки движения
    private boolean moving = true;
    
    // геттеры
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDirection() { return direction; }
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public boolean isMoving() { return moving; }
    
    // сеттеры с уведомлением наблюдателей
    public void setTargetPosition(int x, int y) {
        this.targetX = x;
        this.targetY = y;
        setChanged();
        notifyObservers("target_changed");
    }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        setChanged();
        notifyObservers("position_changed");
    }
    
    public void setDirection(double direction) {
        this.direction = normalizeAngle(direction);
        setChanged();
        notifyObservers("direction_changed");
    }
    
    // основная логика движения
    public void updateMovement(double duration) {
        if (!moving) return;
        
        double distance = distanceToTarget();
        if (distance < 2.0) {
            moving = false;
            setChanged();
            notifyObservers("stopped");
            return;
        }
        
        double angleToTarget = angleToTarget();
        double angularVelocity = calculateAngularVelocity(angleToTarget);
        double velocity = calculateVelocity(angleToTarget);;
        
        moveRobot(velocity, angularVelocity, duration);
    }
    
    private double calculateAngularVelocity(double angleToTarget) {
        double angleDifference = angleToTarget - direction;
        
        // нормализуем разницу углов в диапазон [-п, п]
        while (angleDifference > Math.PI) angleDifference -= 2 * Math.PI;
        while (angleDifference < -Math.PI) angleDifference += 2 * Math.PI;
        
        if (Math.abs(angleDifference) < 0.05) {
            return 0;
        }

        double angularSpeed = MAX_ANGULAR_VELOCITY;
        if (Math.abs(angleDifference) < 0.3) {
            angularSpeed = MAX_ANGULAR_VELOCITY * 0.5;
        }
        
        if (angleDifference > 0) {
            return angularSpeed;
        } else {
            return -angularSpeed;
        }
    }

    private double calculateVelocity(double angleToTarget) {
        double angleDifference = Math.abs(normalizeAngle(angleToTarget - direction));
        
        if (angleDifference < 0.2) {
            return MAX_VELOCITY;
        } else if (angleDifference < 0.5) {
            return MAX_VELOCITY * 0.8;
        } else {
            return MAX_VELOCITY * 0.5;
        }
    }
    
    private void moveRobot(double velocity, double angularVelocity, double duration) {
        velocity = applyLimits(velocity, 0, MAX_VELOCITY);
        angularVelocity = applyLimits(angularVelocity, -MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);
        
        double newX = x;
        double newY = y;
        
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
    
    // вычисление расстояния до цели
    private double distanceToTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    // вычисление угла до цели
    private double angleToTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        return normalizeAngle(Math.atan2(dy, dx));
    }
    
    // нормализация угла в диапазон
    private double normalizeAngle(double angle) {
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        return angle;
    }
    
    // ограничение значений
    private double applyLimits(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
    
    // сброс движения при новой цели
    public void setTargetAndMove(int x, int y) {
        setTargetPosition(x, y);
        moving = true;
        setChanged();
        notifyObservers("target_set");
    }
}