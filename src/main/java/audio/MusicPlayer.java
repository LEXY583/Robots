package audio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;

public class MusicPlayer {
    private Clip clip; // объект для хранения аудио
    private FloatControl volumeControl; // громкость в децибелах
    private boolean isPlaying = false; // флаг, играет музыка или нет

    // музыка играет?
    public boolean isPlaying() {
        return isPlaying;
    }

    // основной метод воспроизведения треков
    public void play(File musicFile) {
        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile)) { // try-with-resources, т.к. AudioInputStream - autocloseable
            stop();
            
            // получаем формат исходного аудиофайла
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
            try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, audioStream)) {
                // открываем Clip с PCM-потоком
                clip = AudioSystem.getClip();
                clip.open(pcmStream);
            }
            
            setupVolumeControl();

            // начинаем воспроизведение
            clip.start();
            isPlaying = true;

            // Обработка окончания трека
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    isPlaying = false;
                    if (clip != null && clip.getFramePosition() >= clip.getFrameLength() - 1000) {
                        if (onCompleteListener != null) {
                            onCompleteListener.onComplete();
                        }
                    }
                }
            });
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Playback error: " + e.getMessage());
        }
    }
    
    // метод проверки доступных типов контроля громкости и установка громкости 70
    private void setupVolumeControl() {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(70);
        } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
            setVolume(70);
        }
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

    // пауза
    public void pause() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            isPlaying = false;
        }
    }
    
    // воспроизведение с того места, где остановились
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

    // получить текущую позицию воспроизведения в секундах
    public long getCurrentPosition() {
        return clip != null ? clip.getMicrosecondPosition() / 1000000 : 0;
    }  

    // интерфейс для автоматического переключения на следующий трек
    public interface OnCompleteListener {
        void onComplete();
    }
    
    private OnCompleteListener onCompleteListener;
    
    public void setOnCompleteListener(OnCompleteListener listener) {
        this.onCompleteListener = listener;
    }
}