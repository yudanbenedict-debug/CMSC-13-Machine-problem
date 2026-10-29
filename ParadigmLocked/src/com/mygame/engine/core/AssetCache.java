package com.mygame.engine.core;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class AssetCache {
    private static final Map<String, BufferedImage> images = new ConcurrentHashMap<>();

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

        image = toCompatibleImage(image);
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

    /*
        ImageIO usually returns images in a pixel format the screen doesn't use (e.g. 3-byte BGR),
        which forces Java2D to convert pixels on EVERY drawImage call. Copying the image once into
        a "compatible" image (same format as the display) makes every draw after that much cheaper.
        Looks identical, only the internal pixel layout changes.
    */
    private static BufferedImage toCompatibleImage(BufferedImage source) {
        if (source == null || GraphicsEnvironment.isHeadless()) {
            return source;
        }
        try {
            GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
            if (source.getColorModel().equals(config.getColorModel(source.getTransparency()))) {
                return source;
            }
            BufferedImage compatible = config.createCompatibleImage(
                source.getWidth(), source.getHeight(), source.getTransparency());
            Graphics2D g = compatible.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(source, 0, 0, null);
            g.dispose();
            return compatible;
        } catch (RuntimeException e) {
            return source; // never fail loading just because the optimization failed
        }
    }
}