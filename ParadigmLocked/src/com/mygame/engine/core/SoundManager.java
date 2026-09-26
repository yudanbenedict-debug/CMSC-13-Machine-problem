package com.mygame.engine.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

public class SoundManager {
    private static final Map<String, Clip> sounds = new HashMap<>();
    private static Clip activeMusic;
    private static float masterVolume = 0.6f;  // 0.0 to 1.0

    //load a sound into the cache
    public static void load(String name, String path) {
        try {
            String resourcePath = path.startsWith("/") ? path : "/" + path;
            
            //JAR
            InputStream is = SoundManager.class.getResourceAsStream(resourcePath);
            if (is == null) {
                is = SoundManager.class.getClassLoader().getResourceAsStream(resourcePath.substring(1));
            }
            
            //Folder
            if (is == null) {
                String rawPath = resourcePath.substring(1);
                File rawFile = new File("assets/" + rawPath);
                
                if (!rawFile.exists()) {
                    rawFile = new File(rawPath);
                }

                if (rawFile.exists()) {
                    is = Files.newInputStream(rawFile.toPath());
                } else {
                    System.err.println("Could not find sound resource: " + resourcePath);
                    return;
                }
            }

            //wrap in a BufferedInputStream to prevent the "mark/reset not supported" error
            InputStream bufferedIn = new BufferedInputStream(is);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bufferedIn);
            
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            sounds.put(name, clip);

        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.err.println("Error loading sound: " + path + " - " + e.getMessage());
        }
    }

    //[lay short SFX
    public static void playSFX(String name) {
        Clip clip = sounds.get(name);
        if (clip != null) {
            clip.setFramePosition(0); //rewind to start
            clip.start();
        }
    }

    //play looping BGM
    public static void playMusic(String name) {
        //stop current music if playing
        if (activeMusic != null && activeMusic.isRunning()) {
            activeMusic.stop();
        }

        //get the new clip from cache
        activeMusic = sounds.get(name);

        //play the new clip
        if (activeMusic != null) {
            activeMusic.setFramePosition(0);
            activeMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
    
    public static void stopAllMusic() {
        if (activeMusic != null) {
            activeMusic.stop();
        }
    }
    
    //set master volume for all sounds and music (0.0 to 1.0)
    public static void setVolumeMaster(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        
        //apply to all cached sounds
        for (Clip clip : sounds.values()) {
            applyVolumeToClip(clip, masterVolume);
        }
        
        //apply to active music
        if (activeMusic != null) {
            applyVolumeToClip(activeMusic, masterVolume);
        }
    }
    
    private static void applyVolumeToClip(Clip clip, float volume) {
        if (clip == null) return;
        
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            //convert volume (0-1) to dB range (-80 to 6)
            float dB = 20f * (float) Math.log10(volume);
            gainControl.setValue(dB);
        } catch (IllegalArgumentException e) {
            //some clips may not support gain control
            System.err.println("Volume control not available for this clip");
        }
    }
}