package gui;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JInternalFrame;

// класс управления состояниями окна
public class WindowConfigManager { 
    private static final String CONFIG_FILE = 
        System.getProperty("user.home") + File.separator + ".robots_config.dat";
    
    private Map<String, WindowPosition> windowStates = new HashMap<>();
    
    // сохрание состояния окна
    public void saveWindowState(String windowId, JInternalFrame window) {
        windowStates.put(windowId, new WindowPosition(window));
    }
    
    // сохранение файла конфигурации
    public void saveToFile() {
        try (ObjectOutputStream dataToWrite = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE))) {
            dataToWrite.writeObject(windowStates);
            System.out.println("The configuration is saved: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Saving error: " + e.getMessage());
        }
    }
    
    // загрузка конфигурации из файла
    @SuppressWarnings("unchecked")
    public void loadFromFile() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            System.out.println("No configuration file found, using default settings");
            return;
        }
        
        try (ObjectInputStream dataToRead = new ObjectInputStream(new FileInputStream(file))) {
            windowStates = (Map<String, WindowPosition>) dataToRead.readObject();
            System.out.println("The configuration is loaded from: " + CONFIG_FILE);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Download error: " + e.getMessage());
        }
    }
    
    // получение состояния окна по ID
    public WindowPosition getWindowState(String windowId) {
        return windowStates.get(windowId);
    }
}