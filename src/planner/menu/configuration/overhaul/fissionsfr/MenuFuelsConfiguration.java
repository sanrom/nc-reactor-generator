package planner.menu.configuration.overhaul.fissionsfr;
import multiblock.configuration.Configuration;
import planner.Core;
import multiblock.configuration.overhaul.fissionsfr.Fuel;
import planner.menu.component.MenuComponentMinimaList;
import planner.menu.component.MenuComponentMinimalistButton;
import simplelibrary.opengl.gui.GUI;
import simplelibrary.opengl.gui.Menu;
import simplelibrary.opengl.gui.components.MenuComponentButton;
public class MenuFuelsConfiguration extends Menu{
    private final MenuComponentMinimaList list = add(new MenuComponentMinimaList(0, 0, 0, 0, 50));
    private final MenuComponentMinimalistButton add = add(new MenuComponentMinimalistButton(0, 0, 0, 0, "Add Fuel", true, true));
    private final MenuComponentMinimalistButton back = add(new MenuComponentMinimalistButton(0, 0, 0, 0, "Back", true, true));
    private boolean refreshNeeded = false;
    private final Configuration configuration;
    public MenuFuelsConfiguration(GUI gui, Menu parent, Configuration configuration){
        super(gui, parent);
        add.addActionListener((e) -> {
            Fuel b = new Fuel("New Fuel", 0, 0, 0, 0, false);
            configuration.overhaul.fissionSFR.fuels.add(b);
            Core.configuration.overhaul.fissionSFR.allFuels.add(b);
            gui.open(new MenuFuelConfiguration(gui, this, b));
        });
        back.addActionListener((e) -> {
            gui.open(parent);
        });
        this.configuration = configuration;
    }
    @Override
    public void onGUIOpened(){
        list.components.clear();
        for(Fuel b : configuration.overhaul.fissionSFR.fuels){
            list.add(new MenuComponentFuelConfiguration(b));
        }
    }
    @Override
    public void tick(){
        if(refreshNeeded){
            onGUIOpened();
            refreshNeeded = false;
        }
        super.tick();
    }
    @Override
    public void render(int millisSinceLastTick){
        list.width = gui.helper.displayWidth();
        list.height = gui.helper.displayHeight()-back.height-add.height;
        for(simplelibrary.opengl.gui.components.MenuComponent component : list.components){
            component.width = list.width-(list.hasVertScrollbar()?list.vertScrollbarWidth:0);
        }
        add.width = back.width = gui.helper.displayWidth();
        add.height = back.height = gui.helper.displayHeight()/16;
        back.y = gui.helper.displayHeight()-back.height;
        add.y = back.y-add.height;
        super.render(millisSinceLastTick);
    }
    @Override
    public void buttonClicked(MenuComponentButton button){
        for(simplelibrary.opengl.gui.components.MenuComponent c : list.components){
            if(c instanceof MenuComponentFuelConfiguration){
                if(button==((MenuComponentFuelConfiguration) c).delete){
                    configuration.overhaul.fissionSFR.fuels.remove(((MenuComponentFuelConfiguration) c).fuel);
                    Core.configuration.overhaul.fissionSFR.allFuels.remove(((MenuComponentFuelConfiguration) c).fuel);
                    refreshNeeded = true;
                    return;
                }
                if(button==((MenuComponentFuelConfiguration) c).edit){
                    gui.open(new MenuFuelConfiguration(gui, this, ((MenuComponentFuelConfiguration) c).fuel));
                    return;
                }
            }
        }
        super.buttonClicked(button);
    }
}