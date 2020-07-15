package planner.menu.component;
import generator.MultiblockGenerator;
import planner.Core;
import simplelibrary.font.FontManager;
import simplelibrary.opengl.gui.components.MenuComponent;
public class MenuComponentMultiblockGenerator extends MenuComponent{
    private final MultiblockGenerator generator;
    public MenuComponentMultiblockGenerator(MultiblockGenerator generator){
        super(0, 0, 0, 96);
        this.generator = generator;
    }
    @Override
    public void render(){
        if(isMouseOver&&!isSelected)Core.applyAverageColor(Core.theme.getDarkButtonColor(), Core.theme.getSelectedMultiblockColor());
        else Core.applyColor(isSelected?Core.theme.getSelectedMultiblockColor():Core.theme.getDarkButtonColor());
        drawRect(x, y, x+width, y+height, 0);
        Core.applyColor(Core.theme.getTextColor());
        drawText();
    }
    public void drawText(){
        double height = this.height/2;
        double textLength = FontManager.getLengthForStringWithHeight(generator.getName(), height)+height;
        double scale = Math.min(1, width/textLength);
        double textHeight = (int)(height*scale)-1;
        drawCenteredText(x, y+this.height/2-textHeight/2, x+width, y+this.height/2+textHeight/2, generator.getName());
    }
    @Override
    public boolean mouseWheelChange(int wheelChange){
        return parent.mouseWheelChange(wheelChange);
    }
}