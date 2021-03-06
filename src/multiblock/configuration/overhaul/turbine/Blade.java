package multiblock.configuration.overhaul.turbine;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Objects;
import multiblock.configuration.TextureManager;
import simplelibrary.config2.Config;
import simplelibrary.config2.ConfigNumberList;
public class Blade{
    public static Blade blade(String name, float efficiency, float expansion, String texture){
        Blade blade = new Blade(name);
        blade.efficiency = efficiency;
        blade.expansion = expansion;
        blade.setTexture(TextureManager.getImage(texture));
        return blade;
    }
    public static Blade stator(String name, float expansion, String texture){
        Blade blade = new Blade(name);
        blade.efficiency = 0;
        blade.expansion = expansion;
        blade.stator = true;
        blade.setTexture(TextureManager.getImage(texture));
        return blade;
    }
    public String name;
    public float efficiency;
    public float expansion;
    public boolean stator = false;
    public BufferedImage texture;
    public BufferedImage displayTexture;
    public Blade(String name){
        this.name = name;
    }
    public Config save(boolean partial){
        Config config = Config.newConfig();
        config.set("name", name);
        config.set("efficiency", efficiency);
        config.set("expansion", expansion);
        config.set("stator", stator);
        if(texture!=null&&!partial){
            ConfigNumberList tex = new ConfigNumberList();
            tex.add(texture.getWidth());
            for(int x = 0; x<texture.getWidth(); x++){
                for(int y = 0; y<texture.getHeight(); y++){
                    tex.add(texture.getRGB(x, y));
                }
            }
            config.set("texture", tex);
        }
        return config;
    }
    public void setTexture(BufferedImage image){
        texture = image;
        BufferedImage displayImg = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for(int x = 0; x<image.getWidth(); x++){
            for(int y = 0; y<image.getHeight(); y++){
                Color col = new Color(image.getRGB(x, y));
                displayImg.setRGB(x, y, new Color(TextureManager.convert(col.getRed()), TextureManager.convert(col.getGreen()), TextureManager.convert(col.getBlue()), col.getAlpha()).getRGB());
            }
        }
        displayTexture = displayImg;
    }
    @Override
    public String toString(){
        return name;
    }
    @Override
    public boolean equals(Object obj){
        if(obj!=null&&obj instanceof Blade){
            Blade b = (Blade)obj;
            return Objects.equals(name, b.name)
                    &&efficiency==b.efficiency
                    &&b.expansion==expansion
                    &&b.stator==stator
                    &&compareImages(b.texture, texture);
        }
        return false;
    }
    /**
    * Compares two images pixel by pixel.
    * 
    * from https://stackoverflow.com/questions/11006394/is-there-a-simple-way-to-compare-bufferedimage-instances/11006474#11006474
    *
    * @param imgA the first image.
    * @param imgB the second image.
    * @return whether the images are both the same or not.
    */
   public static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
       if(imgA==null&&imgB==null)return true;
       if(imgA==null&&imgB!=null)return false;
       if(imgA!=null&&imgB==null)return false;
     // The images must be the same size.
     if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
       return false;
     }

     int width  = imgA.getWidth();
     int height = imgA.getHeight();

     // Loop over every pixel.
     for (int y = 0; y < height; y++) {
       for (int x = 0; x < width; x++) {
         // Compare the pixels for equality.
         if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
           return false;
         }
       }
     }

     return true;
   }
}