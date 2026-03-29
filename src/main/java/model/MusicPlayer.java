package model;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer {
    private Clip clip; // объект для хранения аудио
    private FloatControl volumeControl; // громкость в децибелах
    private boolean isPlaying = false; // флаг, играет музыка или нет
    private List<File> playlist = new ArrayList<>(); // список файлов с музыкой
    private int currentTrackIndex = 0; // индекс текущего трека
    

    // загрузка Java Sound API
    static {
        try {
            Class.forName("javax.sound.sampled.spi.AudioFileReader");
            Class.forName("javax.sound.sampled.spi.FormatConversionProvider");
        } catch (ClassNotFoundException e) {
            System.err.println("Не удалось загрузить аудио-провайдеры: " + e.getMessage());
        }
    }

    // есть ли треки в плейлисте
    public boolean hasPlaylist() {
        return !playlist.isEmpty();
    }

    // музыка играет?
    public boolean isPlaying() {
        return isPlaying;
    }
    
    //ОБРАБОТЧИКИ СОБЫТИЙ
    // интерфейс для обработки србытия "трек закончился"
    public interface OnTrackCompleteListener {
        void onTrackComplete();
    }
    private OnTrackCompleteListener onTrackComplete; // ссылка на наблюдателя
    
    // метод подписки на событие "трек закончился"
    public void setOnTrackCompleteListener(OnTrackCompleteListener listener) {
        this.onTrackComplete = listener;
    }
    
    // следующий трек
    public void nextTrack() {
        if (playlist.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
        playCurrentTrack();
    }
    
    // предыдущий трек
    public void previousTrack() {
        if (playlist.isEmpty()) return;
        currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
        playCurrentTrack();
    }
    
    // воспроизвести текущий трек
    public void playCurrentTrack() {
        if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
            play(playlist.get(currentTrackIndex));
        }
    }
    
    // воспроизвести трек
    public void playTrack(int index) {
        if (index >= 0 && index < playlist.size()) {
            currentTrackIndex = index;
            playCurrentTrack();
        }
    }

    // пауза
    public void pause() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            isPlaying = false;
        }
    }
    
    // воспроизведение
    public void resume() {
        if (clip != null && !clip.isRunning() && clip.getFramePosition() < clip.getFrameLength()) {
            clip.start();
            isPlaying = true;
        }
    }
    
    // полная остановка и закрытие clip
    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            isPlaying = false;
        }
    }
    
    // ГЕТТЕРЫ
    public String getCurrentTrackName() {
        if (playlist.isEmpty() || currentTrackIndex >= playlist.size()) {
            return "Нет трека";
        }
        String name = playlist.get(currentTrackIndex).getName();
        return name.substring(0, name.lastIndexOf('.'));
    }
    
    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }
    
    public List<File> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    // получить текущую позицию в секундах
    public long getCurrentPosition() {
        if (clip != null) {
            return clip.getMicrosecondPosition() / 1000000;
        }
        return 0;
    }
    
    // получить длительность трека в секундах
    public long getDuration() {
        if (clip != null) {
            return clip.getMicrosecondLength() / 1000000;
        }
        return 0;
    }
    
    //СЕТТЕРЫ
    // метод установки плейлиста
    public void setPlaylist(List<File> files) {
        this.playlist = new ArrayList<>(files);
        this.currentTrackIndex = 0;
    }

    // установить громкость
    public void setVolume(int percent) {
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float range = max - min;
            float value = min + (range * percent / 100);
            volumeControl.setValue(value);
        }
    }


    // основной метод воспроизведения треков
    public void play(File musicFile) {
        try {
            stop();
            
            // получаем аудиопоток и формат исходного аудиофайла
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            AudioFormat baseFormat = audioStream.getFormat();
            
            // конвертируем теекущий формат в PCM формат понятный Clip
            AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
            );
            
            // создаем новый поток в формате PCM
            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, audioStream);
            
            // открываем Clip с PCM-потоком
            clip = AudioSystem.getClip();
            clip.open(pcmStream);
            
            // проверяем доступные типы контроля громкости и устанавливаем громкость 70
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(70);
            } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
                setVolume(70);
            }
            
            // начинаем воспроизведение
            clip.start();
            isPlaying = true;
            
            // обработка окончания трека
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    isPlaying = false;
                    if (clip != null && clip.getFramePosition() >= clip.getFrameLength() - 1000) {
                        if (onTrackComplete != null) {
                            onTrackComplete.onTrackComplete();
                        }
                    }
                }
            });
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported format: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(" File reading error: " + e.getMessage());
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println(" The audio device is unavailable: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(" Unknown error: " + e.getMessage());
            e.printStackTrace();
        }
    }    
}