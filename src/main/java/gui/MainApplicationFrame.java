package gui;

import javax.swing.JDesktopPane;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JOptionPane;
import java.beans.PropertyVetoException;

import log.Logger;

import model.RobotModel;

public class MainApplicationFrame extends JFrame {

    private final JDesktopPane desktopPane = new JDesktopPane();
    private final WindowConfigManager configManager = new WindowConfigManager();
    private final RobotModel robotModel = new RobotModel(); // создаем модель
    
    private LogWindow logWindow;
    private GameWindow gameWindow;
    private CoordinatesWindow coordinatesWindow; // объявление окна
    
    public MainApplicationFrame() {
        configManager.loadFromFile(); 

        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);

        setContentPane(desktopPane);
        
        
        logWindow = createLogWindow();
        addWindow(logWindow);

        gameWindow = new GameWindow(robotModel); // передали robotModel
        gameWindow.setSize(400,  400);
        addWindow(gameWindow);

        // создаем окно с координатами
        coordinatesWindow = new CoordinatesWindow(robotModel);
        addWindow(coordinatesWindow);

        restoreWindowState(logWindow.getTitle(), logWindow);
        restoreWindowState(gameWindow.getTitle(), gameWindow);
        restoreWindowState(coordinatesWindow.getTitle(), coordinatesWindow);

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }
    
    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    
    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        Menu menu = new Menu(this);

        menuBar.add(menu.createLookAndFeelMenu());
        menuBar.add(menu.createTestMenu());
        menuBar.add(menu.createFileMenu());

        return menuBar;
    }

    public void setLookAndFeel(String className){
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }

    public void exitApplication() { 
        int result = JOptionPane.showConfirmDialog(
            this, // родительское окно
            "Вы хотите выйти из Robots?", 
            "Подтверждение выхода",                 
            JOptionPane.YES_NO_OPTION,             
            JOptionPane.QUESTION_MESSAGE             
        );
        
        if (result == JOptionPane.YES_OPTION) {
            saveWindowsState(); // сохраняем состояние окон перед выходом
            System.exit(0);
        }
    } 
    
    // метод восстановления состояния окон
    private void restoreWindowState(String windowId, JInternalFrame window) {
        WindowPosition savedState = configManager.getWindowState(windowId);
        if (savedState != null) {
            savedState.applyTo(window);
        }
    }

    // метод сохранения состояния окон
    private void saveWindowsState() {
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            configManager.saveWindowState(frame.getTitle(), frame);
        }
        configManager.saveToFile();
    }

}
