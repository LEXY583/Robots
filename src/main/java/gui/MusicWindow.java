package gui;

import javax.swing.JInternalFrame;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import controller.MusicController;

// графический интерфейс окна плеера
public class MusicWindow extends JInternalFrame implements MusicController.MusicUIListener { // MusicWindow реализует интерфейс MusicUIListener
    
    private MusicController controller; // контроллер, который управляет музыкальной логикой
    private DefaultListModel<String> playlistModel; // модель данных для списка треков
    private JList<String> playlistList; // список для отображения плейлиста

    // кнопки воспрозведения
    private JButton playPauseButton;
    private JButton prevButton;
    private JButton nextButton;

    private JSlider volumeSlider; // ползунок громкости
    private JLabel currentTrackLabel; // текущий трек
    private JLabel songIconLabel; // иконка справа от текущего трека
    
    public MusicWindow() {
        super("Музыкальный плеер", true, true, true, true);

        // созаем контроллер и подписываем текущее окно на его события
        controller = new MusicController();
        controller.setUIListener(this);
        
        initComponents(); // заполнение окошка при запуске
        setupEventHandlers(); // установка обработчиков событий

        // Загружаем музыку
        java.net.URL musicUrl = getClass().getClassLoader().getResource("music");
        controller.loadMusicFromFolder(musicUrl);
        
        setSize(350, 400);
        setLocation(100, 100);
    }
    
    // метод заполнения окошка при  запуске
    private void initComponents() { 
        setLayout(new BorderLayout());
        add(createTopPanel(), BorderLayout.NORTH);
        add(createPlaylistPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    // создание верхней панели
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
        topPanel.add(createControlPanel(), BorderLayout.CENTER);
        topPanel.add(createVolumePanel(), BorderLayout.EAST);
        return topPanel;
    }
    
    // создание панели кнопок управления
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        prevButton = createStyledButton("img/previous.png", "Предыдущий трек");
        playPauseButton = createStyledButton("img/play.png", "Воспроизвести");
        nextButton = createStyledButton("img/next.png", "Следующий трек");
        
        controlPanel.add(prevButton);
        controlPanel.add(playPauseButton);
        controlPanel.add(nextButton);
        
        return controlPanel;
    }

    // создание отдельной кнопки
    private JButton createStyledButton(String iconPath, String toolTip) {
        JButton button = new JButton(loadIcon(iconPath));
        button.setToolTipText(toolTip);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        return button;
    }
    
    // создание панели управления громкостью
    private JPanel createVolumePanel() {
        JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JLabel volumeIcon = new JLabel(loadIcon("img/volume.png"));
        volumeIcon.setToolTipText("Громкость");
        volumePanel.add(volumeIcon);
        
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setPreferredSize(new Dimension(100, 25));
        volumePanel.add(volumeSlider);
        
        return volumePanel;
    }
    
    // создание центральной панели-плейлиста
    private JScrollPane createPlaylistPanel() {
        playlistModel = new DefaultListModel<>();
        playlistList = new JList<>(playlistModel);
        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistList.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(playlistList);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        return scrollPane;
    }
    
    // создание нижней панели инф-и о текущем треке
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        bottomPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        
        songIconLabel = new JLabel(loadIcon("img/song.png"));
        bottomPanel.add(songIconLabel);
        
        currentTrackLabel = new JLabel("Нет трека");
        currentTrackLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bottomPanel.add(currentTrackLabel);
        
        return bottomPanel;
    }
    
    // метод загрузки иконки
    private ImageIcon loadIcon(String path) {
        java.net.URL imgURL = getClass().getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        return null;
    }
    
    // метод настройки обработчиков событий
    private void setupEventHandlers() {
        playPauseButton.addActionListener(e -> controller.playPause());
        prevButton.addActionListener(e -> controller.previousTrack());
        nextButton.addActionListener(e -> controller.nextTrack());
        
        playlistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = playlistList.getSelectedIndex();
                if (index >= 0 && controller.hasPlaylist() && index < controller.getPlaylistSize()) {
                    controller.playSelectedTrack(index);
                }
            }
        });
        
        volumeSlider.addChangeListener(e -> controller.setVolume(volumeSlider.getValue()));
    }

    // реализация интерфейса MusicUIListener
    @Override
    public void onPlaylistLoaded(List<String> trackNames) {
        SwingUtilities.invokeLater(() -> {
            playlistModel.clear();
            if (trackNames.isEmpty()) {
                playlistModel.addElement("Нет музыкальных файлов");
            } else {
                for (String name : trackNames) {
                    playlistModel.addElement("♪ " + name);
                }
            }
        });
    }
    
    @Override
    public void onTrackChanged(String trackName, int index) {
        SwingUtilities.invokeLater(() -> {
            currentTrackLabel.setText("Сейчас играет - " + trackName);
            if (index >= 0 && index < playlistModel.size()) {
                playlistList.setSelectedIndex(index);
            }
        });
    }
    
    @Override
    public void onPlayStateChanged(boolean isPlaying) {
        SwingUtilities.invokeLater(() -> {
            if (isPlaying) {
                playPauseButton.setIcon(loadIcon("img/pause.png"));
                playPauseButton.setToolTipText("Пауза");
            } else {
                playPauseButton.setIcon(loadIcon("img/play.png"));
                playPauseButton.setToolTipText("Воспроизвести");
            }
        });
    }
    
    // переопределение закрытия окна, останавливаем музыку перед закрытием
    public void dispose() {
        if (controller != null) { 
            controller.stop();
        }
        super.dispose();
    }
}