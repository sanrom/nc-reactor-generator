package planner.menu.component.generator;
import multiblock.ppe.PostProcessingEffect;
import org.lwjgl.glfw.GLFW;
import planner.Core;
import simplelibrary.font.FontManager;
import simplelibrary.opengl.gui.components.MenuComponent;
public class MenuComponentPostProcessingEffect extends MenuComponent{
    public final PostProcessingEffect postProcessingEffect;
    public boolean enabled = false;
    public MenuComponentPostProcessingEffect(PostProcessingEffect postProcessingEffect){
        super(0, 0, 0, 32);
        this.postProcessingEffect = postProcessingEffect;
        enabled = postProcessingEffect.defaultEnabled();
    }
    @Override
    public void render(){
        if(isMouseOver&&!enabled)Core.applyAverageColor(Core.theme.getButtonColor(), Core.theme.getSelectedMultiblockColor());
        else Core.applyColor(enabled?Core.theme.getSelectedMultiblockColor():Core.theme.getButtonColor());
        drawRect(x, y, x+width, y+height, 0);
        Core.applyColor(Core.theme.getTextColor());
        drawText();
    }
    public void drawText(){
        double textLength = FontManager.getLengthForStringWithHeight(postProcessingEffect.name, height);
        double scale = Math.min(1, width/textLength);
        double textHeight = (int)(height*scale)-1;
        drawCenteredText(x, y+height/2-textHeight/2, x+width, y+height/2+textHeight/2, postProcessingEffect.name);
    }
    @Override
    public void onMouseButton(double x, double y, int button, boolean pressed, int mods){
        super.onMouseButton(x, y, button, pressed, mods);
        if(button==GLFW.GLFW_MOUSE_BUTTON_LEFT&&pressed){
            enabled = !enabled;
        }
    }
}