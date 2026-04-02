package controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import audio.MusicPlayer;

public class MusicController {
    
    private MusicPlayer player; // ссылка плеер
    private List<File> playlist = new ArrayList<>(); // список аудиофайлов в плейлисте
    private int currentIndex = 0; // индекс текущего трека
    private MusicUIListener uiListener; // слушатель для обновления интерфейса
    
    // интерфейс для обновления окна
    public interface MusicUIListener {
        void onPlaylistLoaded(List<String> trackNames);
        void onTrackChanged(String trackName, int index);
        void onPlayStateChanged(boolean isPlaying);
    }
    
    // создание экземпляра MusicPlayer
    public MusicController() {
        this.player = new MusicPlayer();

        // подписка на окончание трека
        player.setOnCompleteListener(() -> {
        SwingUtilities.invokeLater(() -> nextTrack());
    });
    }
    
    // устанавливаем слушателя (MusicWindow)
    public void setUIListener(MusicUIListener listener) {
        this.uiListener = listener;
    }
    
    // загрузка музыки
    public void loadMusicFromFolder(URL musicFolderUrl) {
        List<File> musicFiles = new ArrayList<>();
        List<String> trackNames = new ArrayList<>();
        
        try {
            if (musicFolderUrl != null) {
                File musicFolder = new File(musicFolderUrl.toURI());
                File[] files = musicFolder.listFiles((dir, name) -> 
                    name.toLowerCase().endsWith(".mp3") || name.toLowerCase().endsWith(".wav"));
                
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        musicFiles.add(file);
                        String name = file.getName();
                        trackNames.add(name.substring(0, name.lastIndexOf('.')));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading music: " + e.getMessage());
        }
        
        setPlaylist(musicFiles, trackNames);
    }
    
    public void setPlaylist(List<File> files, List<String> trackNames) {
        this.playlist = new ArrayList<>(files);
        this.currentIndex = 0;
        
        if (uiListener != null) {
            uiListener.onPlaylistLoaded(trackNames);
        }
    }
    

    // управление плейлистом
    // есть ли треки в плейлисте?
    public boolean hasPlaylist() {
        return !playlist.isEmpty();
    }
    
    // геттеры
    public File getCurrentTrack() {
        if (playlist.isEmpty() || currentIndex >= playlist.size()) return null;
        return playlist.get(currentIndex);
    }
    
    public String getCurrentTrackName() {
        File track = getCurrentTrack();
        if (track == null) return "Нет трека";
        String name = track.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public int getPlaylistSize() {
        return playlist.size();
    }
    
    public void nextTrack() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playlist.size();
        playCurrentTrack();
    }
    
    public void previousTrack() {
        if (playlist.isEmpty()) return;
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        playCurrentTrack();
    }
    
    public void playSelectedTrack(int index) {
        if (index >= 0 && index < playlist.size()) {
            currentIndex = index;
            playCurrentTrack();
        }
    }
    

    // управление воспроизведением
    public void playCurrentTrack() {
        File track = getCurrentTrack();
        if (track != null) {
            player.play(track);
            notifyUI();
        }
    }
    
    public void playPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            if (player.getCurrentPosition() > 0) {
                player.resume();
            } else {
                playCurrentTrack();
            }
        }
        if (uiListener != null) {
            uiListener.onPlayStateChanged(player.isPlaying());
        }
    }
    
    // сеттеры
    public void setVolume(int percent) {
        player.setVolume(percent);
    }
    
    public boolean isPlaying() {
        return player.isPlaying();
    }
    
    public void stop() {
        player.stop();
    }
    
    private void notifyUI() {
        if (uiListener != null) {
            uiListener.onTrackChanged(getCurrentTrackName(), currentIndex);
            uiListener.onPlayStateChanged(player.isPlaying());
        }
    }
}