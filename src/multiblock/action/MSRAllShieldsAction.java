package multiblock.action;
import java.util.ArrayList;
import java.util.HashMap;
import multiblock.Action;
import multiblock.overhaul.fissionmsr.Block;
import multiblock.overhaul.fissionmsr.OverhaulMSR;
public class MSRAllShieldsAction extends Action<OverhaulMSR>{
    private HashMap<Block, Boolean> was = new HashMap<>();
    private final boolean close;
    public MSRAllShieldsAction(boolean close){
        this.close = close;
    }
    @Override
    public void doApply(OverhaulMSR multiblock, boolean allowUndo){
        for(Block b : multiblock.getBlocks()){
            if(b.template.shield){
                if(allowUndo)was.put(b, b.closed);
                b.closed = close;
            }
        }
    }
    @Override
    public void doUndo(OverhaulMSR multiblock){
        for(Block b : was.keySet()){
            b.closed = was.get(b);
        }
    }
    @Override
    protected void getAffectedBlocks(OverhaulMSR multiblock, ArrayList<multiblock.Block> blocks){
        for(Block block : multiblock.getBlocks()){
            if(block.template.shield)blocks.add(block);
        }
    }
}