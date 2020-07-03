package planner.menu.configuration.overhaul.fissionmsr;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.lwjgl.opengl.Display;
import planner.Core;
import planner.configuration.overhaul.fissionmsr.Block;
import planner.menu.component.MenuComponentMinimalistButton;
import planner.menu.component.MenuComponentMinimalistOptionButton;
import planner.menu.component.MenuComponentMinimalistTextBox;
import simplelibrary.opengl.gui.GUI;
import simplelibrary.opengl.gui.Menu;
public class MenuBlockConfiguration extends Menu{
    private final MenuComponentMinimalistTextBox name = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "Name", true));
    private final MenuComponentMinimalistButton texture = add(new MenuComponentMinimalistButton(0, 0, 0, 0, "Select Texture", true, true));
    private final MenuComponentMinimalistTextBox cooling = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "", true).setIntFilter());
    private final MenuComponentMinimalistTextBox input = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "", true));
    private final MenuComponentMinimalistTextBox output = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "", true));
    private final MenuComponentMinimalistOptionButton cluster = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Can Cluster", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistOptionButton createCluster = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Creates Cluster", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistOptionButton conductor = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Conductor", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistOptionButton fuelVessel = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Fuel Vessel", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistOptionButton reflector = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Reflector", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistOptionButton irradiator = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Irradiator", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistOptionButton moderator = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Moderator", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistOptionButton activeModerator = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Active Moderator", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistOptionButton shield = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Neutron Shield", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistTextBox flux = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "", true).setIntFilter());
    private final MenuComponentMinimalistTextBox efficiency = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "", true).setFloatFilter());
    private final MenuComponentMinimalistTextBox reflectivity = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "", true).setFloatFilter());
    private final MenuComponentMinimalistTextBox heatMult = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "", true).setIntFilter());
    private final MenuComponentMinimalistOptionButton blocksLOS = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Block Line of Sight", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistOptionButton functional = add(new MenuComponentMinimalistOptionButton(0, 0, 0, 0, "Functional", true, true, 0, "FALSE", "TRUE"));
    private final MenuComponentMinimalistButton rules = add(new MenuComponentMinimalistButton(0, 0, 0, 0, "Placement Rules", true, true));
    private final MenuComponentMinimalistButton back = add(new MenuComponentMinimalistButton(0, 0, 0, 0, "Back", true, true));
    private final Block block;
    private final int numComps = 22;
    public MenuBlockConfiguration(GUI gui, Menu parent, Block block){
        super(gui, parent);
        texture.addActionListener((e) -> {
            new Thread(() -> {
                JFileChooser chooser = new JFileChooser(new File("file").getAbsoluteFile().getParentFile());
                chooser.setFileFilter(new FileNameExtensionFilter("PNG (.png)", "png"));
                chooser.addActionListener((event) -> {
                    if(event.getActionCommand().equals("ApproveSelection")){
                        File file = chooser.getSelectedFile();
                        try{
                            BufferedImage img = ImageIO.read(file);
                            if(img==null)return;
                            if(img.getWidth()!=img.getHeight()){
                                JOptionPane.showMessageDialog(null, "Image is not square!", "Error loading image", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            block.setTexture(img);
                        }catch(IOException ex){}
                    }
                });
                chooser.showOpenDialog(null);
            }).start();
        });
        rules.addActionListener((e) -> {
            gui.open(new MenuPlacementRulesConfiguration(gui, this, block));
        });
        back.addActionListener((e) -> {
            gui.open(parent);
        });
        this.block = block;
    }
    @Override
    public void onGUIOpened(){
        name.text = block.name;
        cooling.text = block.cooling+"";
        input.text = block.input==null?"":block.input;
        output.text = block.output==null?"":block.output;
        cluster.setIndex(block.cluster?1:0);
        createCluster.setIndex(block.createCluster?1:0);
        conductor.setIndex(block.conductor?1:0);
        fuelVessel.setIndex(block.fuelVessel?1:0);
        reflector.setIndex(block.reflector?1:0);
        irradiator.setIndex(block.irradiator?1:0);
        moderator.setIndex(block.moderator?1:0);
        activeModerator.setIndex(block.activeModerator?1:0);
        shield.setIndex(block.shield?1:0);
        flux.text = block.flux+"";
        efficiency.text = block.efficiency+"";
        reflectivity.text = block.reflectivity+"";
        heatMult.text = block.heatMult+"";
        blocksLOS.setIndex(block.blocksLOS?1:0);
        functional.setIndex(block.functional?1:0);
    }
    @Override
    public void onGUIClosed(){
        block.name = name.text;
        block.cooling = Integer.parseInt(cooling.text);
        block.input = input.text.trim().isEmpty()?null:input.text;
        block.output = output.text.trim().isEmpty()?null:output.text;
        block.cluster = cluster.getIndex()==1;
        block.createCluster = createCluster.getIndex()==1;
        block.conductor = conductor.getIndex()==1;
        block.fuelVessel = fuelVessel.getIndex()==1;
        block.reflector = reflector.getIndex()==1;
        block.irradiator = irradiator.getIndex()==1;
        block.moderator = moderator.getIndex()==1;
        block.activeModerator = activeModerator.getIndex()==1;
        block.shield = shield.getIndex()==1;
        block.flux = Integer.parseInt(flux.text);
        block.efficiency = Float.parseFloat(efficiency.text);
        block.reflectivity = Float.parseFloat(reflectivity.text);
        block.heatMult = Integer.parseInt(heatMult.text);
        block.blocksLOS = blocksLOS.getIndex()==1;
        block.functional = functional.getIndex()==1;
    }
    @Override
    public void render(int millisSinceLastTick){
        drawRect(0, Display.getHeight()/numComps, Display.getHeight()/numComps, Display.getHeight()/numComps*2, Core.getTexture(block.texture));
        cluster.enabled = conductor.getIndex()==0;
        conductor.enabled = cluster.getIndex()==0;
        createCluster.enabled = cluster.getIndex()==1;
        activeModerator.enabled = moderator.getIndex()==1;
        flux.editable = moderator.getIndex()==1||shield.getIndex()==1;
        efficiency.editable = moderator.getIndex()==1||shield.getIndex()==1||reflector.getIndex()==1;
        reflectivity.editable = reflector.getIndex()==1;
        heatMult.editable = shield.getIndex()==1;
        fuelVessel.enabled = reflector.getIndex()==0;
        reflector.enabled = fuelVessel.getIndex()==0;
        input.width = output.width = cooling.width = flux.width = efficiency.width = reflectivity.width = heatMult.width = Display.getWidth()*.75;
        input.x = output.x = cooling.x = flux.x = efficiency.x = reflectivity.x = heatMult.x = Display.getWidth()-cooling.width;
        functional.width = blocksLOS.width = shield.width = activeModerator.width = moderator.width = irradiator.width = reflector.width = fuelVessel.width = conductor.width = createCluster.width = cluster.width = name.width = rules.width = back.width = Display.getWidth();
        texture.x = texture.height = functional.height = blocksLOS.height = heatMult.height = reflectivity.height = efficiency.height = flux.height = shield.height = activeModerator.height = moderator.height = irradiator.height = reflector.height = fuelVessel.height = conductor.height = createCluster.height = cluster.height = input.height = output.height = cooling.height = name.height = rules.height = back.height = Display.getHeight()/numComps;
        texture.width = Display.getWidth()-texture.x;
        texture.y = name.height;
        cooling.y = texture.y+texture.height;
        input.y = cooling.y+cooling.height;
        output.y = input.y+input.height;
        cluster.y = output.y+output.height;
        createCluster.y = cluster.y+cluster.height;
        conductor.y = createCluster.y+createCluster.height;
        fuelVessel.y = conductor.y+conductor.height;
        reflector.y = fuelVessel.y+fuelVessel.height;
        irradiator.y = reflector.y+reflector.height;
        moderator.y = irradiator.y+irradiator.height;
        activeModerator.y = moderator.y+moderator.height;
        shield.y = activeModerator.y+activeModerator.height;
        flux.y = shield.y+shield.height;
        efficiency.y = flux.y+flux.height;
        reflectivity.y = efficiency.y+efficiency.height;
        heatMult.y = reflectivity.y+reflectivity.height;
        blocksLOS.y = heatMult.y+heatMult.height;
        functional.y = blocksLOS.y+blocksLOS.height;
        rules.y = functional.y+functional.height;
        back.y = Display.getHeight()-back.height;
        Core.applyColor(Core.theme.getTextColor());
        drawText(0, Display.getHeight()/numComps*2, Display.getWidth()*.25, Display.getHeight()/numComps*3, "Cooling");
        drawText(0, Display.getHeight()/numComps*3, Display.getWidth()*.25, Display.getHeight()/numComps*4, "Input Fluid");
        drawText(0, Display.getHeight()/numComps*4, Display.getWidth()*.25, Display.getHeight()/numComps*5, "Output Fluid");
        drawText(0, Display.getHeight()/numComps*14, Display.getWidth()*.25, Display.getHeight()/numComps*15, "Neutron Flux");
        drawText(0, Display.getHeight()/numComps*15, Display.getWidth()*.25, Display.getHeight()/numComps*16, "Efficiency");
        drawText(0, Display.getHeight()/numComps*16, Display.getWidth()*.25, Display.getHeight()/numComps*17, "Reflectivity");
        drawText(0, Display.getHeight()/numComps*17, Display.getWidth()*.25, Display.getHeight()/numComps*18, "Heat Multiplier");
        Core.applyWhite();
        super.render(millisSinceLastTick);
    }
}