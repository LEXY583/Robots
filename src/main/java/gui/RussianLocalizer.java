package gui;

import javax.swing.UIManager;

public class RussianLocalizer {
    public static void initRussianResources() {
        // Ключи для основных диалогов
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.okButtonText", "OK");

        // Ключи для заголовков диалогов 
        UIManager.put("OptionPane.titleText", "Подтверждение");
        UIManager.put("OptionPane.messageDialogTitle", "Сообщение");
        UIManager.put("OptionPane.inputDialogTitle", "Ввод");

        // Ключи для файлового диалога
        UIManager.put("FileChooser.openDialogTitleText", "Открыть файл");
        UIManager.put("FileChooser.saveDialogTitleText", "Сохранить файл");
        UIManager.put("FileChooser.lookInLabelText", "Папка:");
        UIManager.put("FileChooser.fileNameLabelText", "Имя файла:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Тип файлов:");
        UIManager.put("FileChooser.openButtonText", "Открыть");
        UIManager.put("FileChooser.saveButtonText", "Сохранить");
        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.fileNameHeaderText", "Имя");
        UIManager.put("FileChooser.fileSizeHeaderText", "Размер");
        UIManager.put("FileChooser.fileTypeHeaderText", "Тип");
        UIManager.put("FileChooser.fileDateHeaderText", "Дата изменения");

        // Ключи для внутренних окон
        UIManager.put("InternalFrame.closeButtonText", "Закрыть");
        UIManager.put("InternalFrame.iconButtonText", "Свернуть");
        UIManager.put("InternalFrame.maximizeButtonText", "Развернуть");
        UIManager.put("InternalFrame.restoreButtonText", "Восстановить");
        UIManager.put("InternalFrameTitlePane.restoreButtonText", "Восстановить");
        UIManager.put("InternalFrameTitlePane.moveButtonText", "Переместить");
        UIManager.put("InternalFrameTitlePane.sizeButtonText", "Размер");
        UIManager.put("InternalFrameTitlePane.minimizeButtonText", "Свернуть");
        UIManager.put("InternalFrameTitlePane.maximizeButtonText", "Развернуть");
        UIManager.put("InternalFrameTitlePane.closeButtonText", "Закрыть");
        
        // И их подсказки
        UIManager.put("InternalFrame.closeButtonToolTip", "Закрыть окно");
        UIManager.put("InternalFrame.iconButtonToolTip", "Свернуть окно");
        UIManager.put("InternalFrame.maxButtonToolTip", "Развернуть окно"); 
        UIManager.put("InternalFrame.restoreButtonToolTip", "Восстановить окно");
    }
}