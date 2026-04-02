// Точка входа в программу
package launcher;

import java.awt.Frame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.util.Locale;

import utils.RussianLocalizer;
import gui.MainApplicationFrame;

public class RobotsProgram {
  public static void main(String[] args) {

    Locale.setDefault(new Locale("ru", "RU"));
    
    RussianLocalizer.initRussianResources();

    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (Exception e) {
      e.printStackTrace();
    }

    SwingUtilities.invokeLater(() -> {
      MainApplicationFrame frame = new MainApplicationFrame();
      frame.pack();
      frame.setVisible(true);
      frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    });
  }  
}
