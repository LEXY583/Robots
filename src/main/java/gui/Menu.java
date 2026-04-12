package gui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

public class Menu {
    private final MainApplicationFrame frame;
    
    public Menu(MainApplicationFrame frame) {
        this.frame = frame;
    }

    public JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = createMenu("Режим отображения", KeyEvent.VK_V, "Управление режимом отображения приложения");
        
        addMenuItem(lookAndFeelMenu, "Системная схема", KeyEvent.VK_S, (event) -> {
            frame.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            frame.invalidate();
        });
        
        addMenuItem(lookAndFeelMenu, "Универсальная схема", KeyEvent.VK_S, (event) -> {
            frame.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            frame.invalidate();
        });
        
        return lookAndFeelMenu;
    }

    public JMenu createTestMenu() {
        JMenu testMenu = createMenu("Тесты", KeyEvent.VK_T, "естовые команды");
        
        addMenuItem(testMenu, "Сообщение в лог", KeyEvent.VK_S, (event) -> {
            Logger.debug("Новая строка");
        });
        
        return testMenu;
    }

    public JMenu createFileMenu() {
        JMenu fileMenu = createMenu("Файл", KeyEvent.VK_F, "Действия с файлами");
        
        addMenuItem(fileMenu, "Выход", KeyEvent.VK_X, (event) -> {
            frame.exitApplication();
        });
        
        return fileMenu;
    }

    private JMenu createMenu(String title, int mnemonic, String description) {
        JMenu menu = new JMenu(title);
        menu.setMnemonic(mnemonic);
        menu.getAccessibleContext().setAccessibleDescription(description);
        return menu;
    }

    private void addMenuItem(JMenu menu, String text, int mnemonic, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(text, mnemonic);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }
}