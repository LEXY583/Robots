package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import model.MusicPlayer;

// графический интерфейс окна плеера
public class MusicWindow extends JInternalFrame {
    
    private MusicPlayer musicPlayer; // ссылка на модель
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
        
        musicPlayer = new MusicPlayer(); // экземпляр плеера
        
        initComponents(); // заполнение окошка при запуске
        setupEventHandlers(); // установка обработчиков событий
        loadMusicFromResources(); // загрузка музыки
        
        // если трек закончился - включаем следующий
        musicPlayer.setOnTrackCompleteListener(() -> {
            SwingUtilities.invokeLater(() -> {
                musicPlayer.nextTrack();
                updateUIFromModel();
            });
        });
        
        setSize(350, 400);
        setLocation(100, 100);
    }
    
    // метод заполнения окошка при  запуске
    private void initComponents() { 
        setLayout(new BorderLayout());
        
        // верхняя панель с элементами управления
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
        
        // панель с кнопками управления и громкостью
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        // создаем кнопки воспроизведения
        prevButton = new JButton(loadIcon("img/previous.png"));
        playPauseButton = new JButton(loadIcon("img/play.png"));
        nextButton = new JButton(loadIcon("img/next.png"));
        
        prevButton.setToolTipText("Предыдущий трек");
        playPauseButton.setToolTipText("Воспроизвести");
        nextButton.setToolTipText("Следующий трек");
        
        // убираем фон и рамку у кнопок
        prevButton.setContentAreaFilled(false);
        prevButton.setBorderPainted(false);
        playPauseButton.setContentAreaFilled(false);
        playPauseButton.setBorderPainted(false);
        nextButton.setContentAreaFilled(false);
        nextButton.setBorderPainted(false);
        
        controlPanel.add(prevButton);
        controlPanel.add(playPauseButton);
        controlPanel.add(nextButton);
        
        // панель с регулятором громкости
        JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JLabel volumeIcon = new JLabel(loadIcon("img/volume.png"));
        volumeIcon.setToolTipText("Громкость");
        volumePanel.add(volumeIcon);
        
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setPreferredSize(new Dimension(100, 25));
        volumePanel.add(volumeSlider);
        
        topPanel.add(controlPanel, BorderLayout.CENTER);
        topPanel.add(volumePanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // список плейлиста
        playlistModel = new DefaultListModel<>();
        playlistList = new JList<>(playlistModel);
        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistList.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(playlistList);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        // нижняя панель с информацией о текущем треке
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        bottomPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        
        // иконка песни слева
        songIconLabel = new JLabel(loadIcon("img/song.png"));
        bottomPanel.add(songIconLabel);
        
        // текст с названием трека
        currentTrackLabel = new JLabel("Нет трека");
        currentTrackLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        bottomPanel.add(currentTrackLabel);
        
        add(bottomPanel, BorderLayout.SOUTH);
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
        playPauseButton.addActionListener(e -> togglePlayPause());
        prevButton.addActionListener(e -> {
            musicPlayer.previousTrack();
            updateUIFromModel();
        });
        nextButton.addActionListener(e -> {
            musicPlayer.nextTrack();
            updateUIFromModel();
        });
        
        playlistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = playlistList.getSelectedIndex();
                if (selectedIndex >= 0 && musicPlayer.hasPlaylist() && selectedIndex < musicPlayer.getPlaylist().size()) {
                    musicPlayer.playTrack(selectedIndex);
                    updateUIFromModel();
                }
            }
        });
        
        volumeSlider.addChangeListener(e -> {
            musicPlayer.setVolume(volumeSlider.getValue());
        });
    }
    
    // метод загрузки музыки
    private void loadMusicFromResources() {
        List<File> musicFiles = new ArrayList<>();
        
        try {
            java.net.URL musicUrl = getClass().getClassLoader().getResource("music");
            if (musicUrl != null) {
                File musicFolder = new File(musicUrl.toURI());
                File[] files = musicFolder.listFiles((dir, name) -> 
                    name.toLowerCase().endsWith(".mp3") || 
                    name.toLowerCase().endsWith(".wav"));
                
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        musicFiles.add(file);
                        String fileName = file.getName();
                        String trackName = fileName.substring(0, fileName.lastIndexOf('.'));
                        playlistModel.addElement("♪ " + trackName);
                    }
                    musicPlayer.setPlaylist(musicFiles);
                } else {
                    playlistModel.addElement("Нет музыкальных файлов");
                }
            } else {
                playlistModel.addElement("Папка music не найдена в resources");
            }
        } catch (Exception e) {
            e.printStackTrace();
            playlistModel.addElement("Ошибка загрузки музыки: " + e.getMessage());
        }
    }
    
    // метод обновления интерфейса
    private void updateUIFromModel() {
        if (musicPlayer.hasPlaylist()) {
            String trackName = musicPlayer.getCurrentTrackName();
            currentTrackLabel.setText("Сейчас играет - " + trackName);
            
            int currentIndex = musicPlayer.getCurrentTrackIndex();
            if (currentIndex >= 0 && currentIndex < playlistModel.size()) {
                playlistList.setSelectedIndex(currentIndex);
            }
            if (musicPlayer.isPlaying()) {
                playPauseButton.setIcon(loadIcon("img/pause.png"));
                playPauseButton.setToolTipText("Пауза");
            } else {
                playPauseButton.setIcon(loadIcon("img/play.png"));
                playPauseButton.setToolTipText("Воспроизвести");
            }
        } else {
            currentTrackLabel.setText("Нет трека");
            playPauseButton.setIcon(loadIcon("img/play.png"));
            playPauseButton.setToolTipText("Воспроизвести");
        }
    }
    
    // метод преключения play/pause
    private void togglePlayPause() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause();
            updateUIFromModel();
        } else {
            if (musicPlayer.hasPlaylist()) {
                if (musicPlayer.getCurrentPosition() > 0) {
                    musicPlayer.resume();
                } else {
                    musicPlayer.playCurrentTrack();
                }
                updateUIFromModel();
            }
        }
    }
    
    // переопределение закрытия окна, останавливаем музыку перед закрытием
    @Override
    public void dispose() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
        super.dispose();
    }
}