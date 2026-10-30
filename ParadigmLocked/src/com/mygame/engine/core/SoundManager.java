package com.mygame.engine.core;

import com.mygame.engine.helpers.constants.GameConstants;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/*
    Audio cache. Java Sound cannot decode MP3, so the game loads the .wav copies in assets/audios.
    - Every sound owns a small pool of Clips. playSFX picks the next free clip, so quick repeats
      (radial hover clicks, menu hovers) STACK instead of cutting each other off.
    - Names starting with THEME / ending with _THEME are music: they loop and play at
      MUSIC_VOLUME_LEVEL so they sit quietly under everything else.
 */
public class SoundManager {
    private static final Map<String, Clip[]> sounds = new ConcurrentHashMap<>();
    private static final Map<String, Integer> nextClip = new ConcurrentHashMap<>();
    private static volatile Clip activeMusic;
    private static volatile String activeMusicName;
    private static float masterVolume = 0.6f;  // 0.0 to 1.0

    public static void load(String name, String path) {
        load(name, path, poolSizeFor(name));
    }

    // load a sound into the cache with `poolSize` clips that can play at the same time
    public static void load(String name, String path, int poolSize) {
        try (InputStream raw = open(path)) {
            if (raw == null) {
                System.err.println("Could not find sound resource: " + path);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
            AudioFormat format = ais.getFormat();
            byte[] data = readAll(ais);
            Clip[] pool = new Clip[Math.max(1, poolSize)];
            for (int index = 0; index < pool.length; index++) {
                pool[index] = AudioSystem.getClip();
                pool[index].open(format, data, 0, data.length);
                applyVolumeToClip(pool[index], volumeFor(name));
            }
            sounds.put(name, pool);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException
                 | IllegalArgumentException e) {
            System.err.println("Error loading sound: " + path + " - " + e.getMessage());
        }
    }

    private static InputStream open(String path) throws IOException {
        String resourcePath = path.startsWith("/") ? path : "/" + path;
        InputStream is = SoundManager.class.getResourceAsStream(resourcePath);
        if (is != null) {
            return is;
        }
        String rawPath = resourcePath.substring(1);
        File rawFile = new File("assets/" + rawPath);
        if (!rawFile.exists()) {
            rawFile = new File(rawPath);
        }
        return rawFile.exists() ? Files.newInputStream(rawFile.toPath()) : null;
    }

    private static byte[] readAll(AudioInputStream ais) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = ais.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static int poolSizeFor(String name) {
        if (isMusic(name)) {
            return 1;
        }
        if (name.startsWith("RADIAL_CHOICES_HOVER")) {
            return 6;
        }
        if (name.startsWith("UI_MENU_HOVER") || name.equals("DAMAGE_TICK")) {
            return 3;
        }
        return 1;
    }

    private static boolean isMusic(String name) {
        return name.startsWith("THEME") || name.endsWith("_THEME");
    }

    private static float volumeFor(String name) {
        return isMusic(name) ? masterVolume * GameConstants.MUSIC_VOLUME_LEVEL : masterVolume;
    }

    // play a short SFX; overlapping calls use the next clip in the pool so they stack
    public static void playSFX(String name) {
        Clip[] pool = sounds.get(name);
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
            int index = nextClip.merge(name, 1, Integer::sum) % pool.length;
            chosen = pool[index];
            chosen.stop();
        }
        chosen.setFramePosition(0);
        chosen.start();
    }

    // plays one random sound out of PREFIX_00, PREFIX_01, ... (e.g. UI_MENU_HOVER)
    public static void playRandomSFX(String prefix) {
        String[] matches = sounds.keySet().stream().filter(key -> key.startsWith(prefix))
            .sorted().toArray(String[]::new);
        if (matches.length > 0) {
            playSFX(matches[ThreadLocalRandom.current().nextInt(matches.length)]);
        }
    }

    public static void playMenuHover() {
        playRandomSFX("UI_MENU_HOVER");
    }

    // play looping BGM (does nothing when that track is already playing)
    public static void playMusic(String name) {
        if (name.equals(activeMusicName) && activeMusic != null && activeMusic.isRunning()) {
            return;
        }
        stopAllMusic();
        Clip[] pool = sounds.get(name);
        activeMusic = pool == null ? null : pool[0];
        activeMusicName = pool == null ? null : name;
        if (activeMusic != null) {
            applyVolumeToClip(activeMusic, volumeFor(name));
            activeMusic.setFramePosition(0);
            activeMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void stopAllMusic() {
        if (activeMusic != null) {
            activeMusic.stop();
        }
        activeMusic = null;
        activeMusicName = null;
    }

    public static float getVolumeMaster() {
        return masterVolume;
    }

    // set master volume for all sounds and music (0.0 to 1.0)
    public static void setVolumeMaster(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        for (Map.Entry<String, Clip[]> entry : sounds.entrySet()) {
            for (Clip clip : entry.getValue()) {
                applyVolumeToClip(clip, volumeFor(entry.getKey()));
            }
        }
    }

    private static void applyVolumeToClip(Clip clip, float volume) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        // convert volume (0-1) to dB, clamped to what the clip supports (log10(0) would be -infinity)
        float dB = volume <= 0.0001f ? gainControl.getMinimum() : 20f * (float) Math.log10(volume);
        gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
    }
}
