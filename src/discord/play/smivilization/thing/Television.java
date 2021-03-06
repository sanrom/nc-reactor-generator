package discord.play.smivilization.thing;
import discord.play.smivilization.Hut;
import discord.play.smivilization.HutThing;
import discord.play.smivilization.Wall;
import java.util.UUID;
public class Television extends HutThing{
    public Television(UUID uuid, Hut hut){
        super(uuid, hut, "Television", "tv", 32);
        mirrorIf = -1;
    }
    @Override
    public HutThing newInstance(UUID uuid, Hut hut){
        return new Television(uuid, hut);
    }
    @Override
    public int[] getDimensions(){
        return new int[]{5,2,4};
    }
    @Override
    public int[] getDefaultLocation(){
        return  new int[]{5,0,2};
    }
    @Override
    public Wall getDefaultWall(){
        return Wall.FLOOR;
    }
    @Override
    public float getRenderWidth(){
        return 496;
    }
    @Override
    public float getRenderHeight(){
        return 529;
    }
    @Override
    public float getRenderOriginX(){
        return 235;
    }
    @Override
    public float getRenderOriginY(){
        return 504;
    }
    @Override
    public float getRenderScale(){
        return 1/1.092132f;
    }
    @Override
    public Wall[] getAllowedWalls(){
        return new Wall[]{Wall.FLOOR};
    }
}