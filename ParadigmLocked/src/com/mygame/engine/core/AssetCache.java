package com.mygame.engine.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class AssetCache {
    private static final Map<String, BufferedImage> images = new HashMap<>();

    //load image
    public static BufferedImage loadImage(String path) throws IOException {
        if (images.containsKey(path)) {
            return images.get(path);
        }

        //standardize the path for JAR (needs leading slash)
        String resourcePath = path.startsWith("/") ? path : "/" + path;
        
        //JAR
        URL resource = AssetCache.class.getResource(resourcePath);
        if (resource == null) {
            resource = AssetCache.class.getClassLoader().getResource(resourcePath.substring(1));
        }

        BufferedImage image = null;

        if (resource != null) {
            image = ImageIO.read(resource);
        } else {
            String rawPath = resourcePath.substring(1); 
            
            //check if it's inside an 'assets' folder
            File rawFile = new File("assets/" + rawPath); 
            
            //if it's not in assets/, try the root just in case
            if (!rawFile.exists()) {
                rawFile = new File(rawPath); 
            }

            if (rawFile.exists()) {
                image = ImageIO.read(rawFile);
            } else {
                throw new IOException("Failed to find image in JAR or Folder: " + resourcePath);
            }
        }

        images.put(path, image);
        return image;
    }

    public static void load(String key, String path){
        try{
            BufferedImage img = loadImage(path);
            images.put(key, img);
        } catch (IOException e){
            System.err.println("Failed to load: " + path + " (" + e.getMessage() + ")");
        }
    }

    public static BufferedImage get(String key){
        return images.get(key);
    }
}