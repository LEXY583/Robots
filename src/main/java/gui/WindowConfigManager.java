package gui;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JInternalFrame;

/**
 * Класс для управления состояниями окон приложения.
 * Сохраняет и восстанавливает позиции, размеры и состояние окон
 * с использованием сериализации Java.
 */
public class WindowConfigManager {
    // Формируем путь к файлу конфигурации в домашнем каталоге пользователя
    // File.separator автоматически подставляет \ для Windows или / для Linux
    private static final String CONFIG_FILE =
            System.getProperty("user.home") + File.separator + ".robots_config.dat";

    // Хранилище состояний окон. ключ - уник идентификатор окна, Знач - объект WindowPosition, содержащ все параметры окна
    private Map<String, WindowPosition> windowStates = new HashMap<>();

    // сохрание состояния окна
    public void saveWindowState(String windowId, JInternalFrame window) {
        windowStates.put(windowId, new WindowPosition(window));
    } //windowId уникальный идентификатор окна (обычно frame.getTitle())
      //window само окно, состояние которого нужно сохранить

    // сохранение файла конфигурации
    public void saveToFile() {
        // ObjectOutputStream позволяет записывать объекты Java в поток
        // FileOutputStream создаёт файл для записи
        try (ObjectOutputStream dataToWrite = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE))) {
            dataToWrite.writeObject(windowStates);
            System.out.println("Конфигурация сохранена: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    // загрузка конфигурации (состояния окон) из файла
    @SuppressWarnings("unchecked") // подавляем предупреждение о небезопасном приведении типов
    public void loadFromFile() {
        File file = new File(CONFIG_FILE);

        // Проверяем, существует ли файл конфигурации
        if (!file.exists()) {
            System.out.println("Файл конфигурации не найден, используются настройки по умолчанию");
            return;
        }

        // ObjectInputStream читает сериализованные объекты из файла
        try (ObjectInputStream dataToRead = new ObjectInputStream(new FileInputStream(file))) {
            // Читаем объект из файла и приводим его к нужному типу Map
            // (тип должен совпадать с тем, что записывали)
            windowStates = (Map<String, WindowPosition>) dataToRead.readObject();
            System.out.println("Конфигурация загружена из: " + CONFIG_FILE);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
        }
    }

    // получение состояния окна по ID
    public WindowPosition getWindowState(String windowId) {
        // Просто возвращаем значение из Map по ключу
        return windowStates.get(windowId);
    }
}