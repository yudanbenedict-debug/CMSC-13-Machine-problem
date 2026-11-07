package com.mygame.engine.core;

import com.mygame.engine.helpers.constants.GameConstants;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

// Audio cache (WAV only); each sound has a clip pool so quick repeats overlap instead of cutting off.
public final class SoundManager {
    private static final Sfx[] MENU_HOVERS = {Sfx.UI_MENU_HOVER_00, Sfx.UI_MENU_HOVER_01, Sfx.UI_MENU_HOVER_02};
    private static final Map<Sfx, Clip[]> SOUNDS = new EnumMap<>(Sfx.class);
    private static final Map<Sfx, Float> LEVEL_OVERRIDES = new EnumMap<>(Sfx.class);
    private static final Map<Sfx, Integer> NEXT_CLIP = new EnumMap<>(Sfx.class);
    private static Sfx activeMusic;
    private static float masterVolume = 0.6f;

    private SoundManager() {
    }

    public static void load(Sfx sound) {
        try (InputStream raw = Resources.open(sound.path())) {
            if (raw == null) {
                System.err.println("[SoundManager] Missing sound: " + sound.path());
                return;
            }
            AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
            AudioFormat format = stream.getFormat();
            byte[] data = stream.readAllBytes();
            Clip[] pool = new Clip[poolSize(sound)];
            for (int index = 0; index < pool.length; index++) {
                pool[index] = AudioSystem.getClip();
                pool[index].open(format, data, 0, data.length);
                applyVolume(pool[index], volumeFor(sound));
            }
            SOUNDS.put(sound, pool);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException
                 | IllegalArgumentException e) {
            System.err.println("[SoundManager] Failed to load " + sound.path() + ": " + e.getMessage());
        }
    }

    private static int poolSize(Sfx sound) {
        switch (sound) {
            case RADIAL_CHOICES_HOVER_01:
                return 6;
            case UI_MENU_HOVER_00:
            case UI_MENU_HOVER_01:
            case UI_MENU_HOVER_02:
            case DAMAGE_TICK:
                return 3;
            default:
                return 1;
        }
    }

    private static float volumeFor(Sfx sound) {
        Float override = LEVEL_OVERRIDES.get(sound);
        if (override != null) {
            return masterVolume * override;
        }
        if (sound.isMusic()) {
            return masterVolume * GameConstants.MUSIC_VOLUME_LEVEL;
        }
        if (sound == Sfx.HINT_PROMPT) {
            return masterVolume * GameConstants.HINT_PROMPT_VOLUME_LEVEL;
        }
        if (sound == Sfx.RADIAL_CHOICES_HOVER_01) {
            return masterVolume * GameConstants.RADIAL_HOVER_VOLUME_LEVEL;
        }
        return masterVolume * GameConstants.SFX_VOLUME_LEVEL;
    }

    // Overrides one sound's level (before master volume).
    public static void setLevel(Sfx sound, float level) {
        LEVEL_OVERRIDES.put(sound, level);
        applyVolume(sound);
    }

    // Plays the next free clip of the pool (or restarts the oldest), so repeats stack.
    public static void playSFX(Sfx sound) {
        Clip[] pool = SOUNDS.get(sound);
        if (pool == null) {
            return;
        }
        Clip chosen = null;
        for (Clip clip : pool) {
            if (!clip.isRunning()) {
                chosen = clip;
                break;
            }
        }
        if (chosen == null) {
            chosen = pool[NEXT_CLIP.merge(sound, 1, Integer::sum) % pool.length];
            chosen.stop();
        }
        chosen.setFramePosition(0);
        chosen.start();
    }

    public static void playMenuHover() {
        playSFX(MENU_HOVERS[ThreadLocalRandom.current().nextInt(MENU_HOVERS.length)]);
    }

    // Loops music; does nothing if that track is already playing.
    public static void playMusic(Sfx track) {
        Clip current = activeMusic == null ? null : SOUNDS.get(activeMusic)[0];
        if (track == activeMusic && current != null && current.isRunning()) {
            return;
        }
        stopAllMusic();
        Clip[] pool = SOUNDS.get(track);
        if (pool != null) {
            activeMusic = track;
            applyVolume(pool[0], volumeFor(track));
            pool[0].setFramePosition(0);
            pool[0].loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void stopAllMusic() {
        if (activeMusic != null && SOUNDS.get(activeMusic) != null) {
            SOUNDS.get(activeMusic)[0].stop();
        }
        activeMusic = null;
    }

    public static float getVolumeMaster() {
        return masterVolume;
    }

    public static void setVolumeMaster(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        for (Sfx sound : SOUNDS.keySet()) {
            applyVolume(sound);
        }
    }

    private static void applyVolume(Sfx sound) {
        Clip[] pool = SOUNDS.get(sound);
        if (pool != null) {
            for (Clip clip : pool) {
                applyVolume(clip, volumeFor(sound));
            }
        }
    }

    private static void applyVolume(Clip clip, float volume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        // volume -> dB, clamped (log10(0) is -infinity)
        float dB = volume <= 0.0001f ? gain.getMinimum() : 20f * (float) Math.log10(volume);
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
    }
}
