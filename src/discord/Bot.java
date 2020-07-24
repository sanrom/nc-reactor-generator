package discord;
import discord.keyword.KeywordBlockRange;
import discord.keyword.KeywordConfiguration;
import discord.keyword.KeywordCube;
import discord.keyword.KeywordCuboid;
import discord.keyword.KeywordFormat;
import discord.keyword.KeywordFuel;
import discord.keyword.KeywordMultiblock;
import discord.keyword.KeywordOverhaul;
import discord.keyword.KeywordPriority;
import discord.keyword.KeywordSymmetry;
import discord.keyword.KeywordUnderhaul;
import discord.play.PlayBot;
import discord.play.action.SmoreAction;
import discord.play.action.SmoreLordAction;
import generator.MultiblockGenerator;
import generator.Priority;
import generator.StandardGenerator;
import generator.StandardGeneratorSettings;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.security.auth.login.LoginException;
import multiblock.Block;
import multiblock.Multiblock;
import multiblock.Range;
import multiblock.configuration.Configuration;
import multiblock.configuration.PartialConfiguration;
import multiblock.configuration.underhaul.fissionsfr.Fuel;
import multiblock.overhaul.fissionmsr.OverhaulMSR;
import multiblock.overhaul.fissionsfr.OverhaulSFR;
import multiblock.ppe.ClearInvalid;
import multiblock.ppe.PostProcessingEffect;
import multiblock.symmetry.AxialSymmetry;
import multiblock.symmetry.Symmetry;
import multiblock.underhaul.fissionsfr.UnderhaulSFR;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GatewayPingEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import planner.Core;
import planner.Core.BufferRenderer;
import planner.file.FileReader;
import planner.file.FileWriter;
import planner.file.FormatWriter;
import planner.file.NCPFFile;
import simplelibrary.CircularStream;
import simplelibrary.Stack;
import simplelibrary.Sys;
import simplelibrary.config2.Config;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
public class Bot extends ListenerAdapter{
    public static boolean debug = false;
    private static ArrayList<String> prefixes = new ArrayList<>();
    private static ArrayList<Long> botChannels = new ArrayList<>();
    private static ArrayList<Long> playChannels = new ArrayList<>();
    private static ArrayList<Long> dataChannels = new ArrayList<>();
    private static Config config;
    private static int cookies;
    private static JDA jda;
    private static final ArrayList<Command> botCommands = new ArrayList<>();
    private static final ArrayList<Command> playCommands = new ArrayList<>();
    private static MultiblockGenerator generator;
    private static Message generatorMessage;
    private static final int batchSize = 100;
    public static final HashMap<NCPFFile, String> storedMultiblocks = new HashMap();
    static{
//        commands.add(new Command("debug"){
//            @Override
//            public String getHelpText(){
//                return "Toggles Debug Mode";
//            }
//            @Override
//            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
//                Bot.debug = !Bot.debug;
//                event.getChannel().sendMessage("Debug mode **"+(Bot.debug?"Enabled":"Disabled")+"**").queue();
//            }
//        });
        botCommands.add(new Command("help", "smelp", "s'melp", "s’melp"){
            @Override
            public String getHelpText(){
                return "Shows this help window";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                EmbedBuilder builder = createEmbed(jda.getSelfUser().getName()+" Help");
                String prefx = "";
                for(String s : Bot.prefixes){
                    prefx+="`"+s+"`\t";
                }
                builder.addField("Prefixes", prefx.trim(), false);
                for(Command c : botCommands){
                    if(c.isSecret())continue;
                    builder.addField(prefixes.get(0)+c.command, c.getHelpText(), false);
                }
                event.getChannel().sendMessage(builder.build()).queue();
            }
        });
        botCommands.add(new Command("stop","abort","halt","cancel","finish"){
            @Override
            public String getHelpText(){
                return "Stops current generation";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                if(generator!=null)generator.stopAllThreads();
                else{
                    event.getChannel().sendMessage("Generator is not running!").queue();
                }
            }
        });
        botCommands.add(new KeywordCommand("generate"){
            @Override
            public String getHelpText(){
                return "**Common generation settings**\n" +
"overhaul - generates an overhaul reactor (Default: underhaul)\n" +
"XxYxZ - generates a reactor of size XxYxZ (Default: 3x3x3)\n" +
"<fuel> - generates a reactor using the specified fuel (Default: TBU)\n" +
"efficiency or efficient - sets efficiency as the main proiority (default)\n" +
"output - sets output as the main priority\n" +
"breeder or cell count or fuel usage - sets fuel usage as the main priority (Underhaul only)\n" +
"irradiator - sets irradiation as the main priority (Overhaul only)\n" +
"symmetry or symmetrical - applies symmetry to generated reactors\n" +
"no <block> - blacklists a certain block from being used in generation\n" +
"e2e - Uses the Enigmatica 2 Expert config\n" +
"po3 - Uses the Project: Ozone 3 config\n" +
"**Special Fuels**\n" +
"Yellorium (Extreme Reactors)\n" +
"IC2-MOX (IC2)\n" +
"Enriched Uranium (IC2)\n" +
"Uranium Ingot (E2E Only)\n" +
"**Examples of valid commands**\n" +
"-generate a 3x3x3 PO3 LEU-235 Oxide breeder with symmetry\n" +
"-generate an efficient 3x8x3 overhaul reactor using [NI] TBU fuel no cryotheum";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, ArrayList<Keyword> keywords, boolean debug){
                if(generator!=null){
                    event.getChannel().sendMessage("Generator is already running!\nUse `"+prefixes.get(0)+"stop` to stop generation").queue();
                    return;
                }
                boolean underhaul = false;
                boolean overhaul = false;
                String multiblockName = null;
                Configuration configuration = null;
                ArrayList<Range<String>> stringRanges = new ArrayList<>();
                ArrayList<String> fuelStrings = new ArrayList<>();
                ArrayList<String> priorityStrings = new ArrayList<>();
                ArrayList<String> symmetryStrings = new ArrayList<>();
                ArrayList<String> formatStrings = new ArrayList<>();
                int x = 0, y = 0, z = 0;
                for(Keyword keyword : keywords){
                    if(keyword instanceof KeywordOverhaul){
                        overhaul = true;
                    }else if(keyword instanceof KeywordUnderhaul){
                        underhaul = true;
                    }else if(keyword instanceof KeywordConfiguration){
                        if(configuration!=null){
                            event.getChannel().sendMessage("Please choose no more than one configuration!").queue();
                            return;
                        }
                        configuration = ((KeywordConfiguration)keyword).config;
                    }else if(keyword instanceof KeywordMultiblock){
                        if(multiblockName!=null){
                            event.getChannel().sendMessage("Please choose no more than one multiblock type!").queue();
                            return;
                        }
                        multiblockName = ((KeywordMultiblock)keyword).text;
                    }else if(keyword instanceof KeywordCube){
                        KeywordCube cube = (KeywordCube)keyword;
                        if(x==0&&y==0&&z==0){
                            x = y = z = cube.size;
                        }else{
                            event.getChannel().sendMessage("You may only choose one size!").queue();
                            return;
                        }
                    }else if(keyword instanceof KeywordCuboid){
                        KeywordCuboid cuboid = (KeywordCuboid)keyword;
                        if(x==0&&y==0&&z==0){
                            x = cuboid.x;
                            y = cuboid.y;
                            z = cuboid.z;
                        }else{
                            event.getChannel().sendMessage("You may only choose one size!").queue();
                            return;
                        }
                    }else if(keyword instanceof KeywordBlockRange){
                        KeywordBlockRange range = (KeywordBlockRange)keyword;
                        stringRanges.add(new Range(range.block, range.min, range.max));
                    }else if(keyword instanceof KeywordFuel){
                        fuelStrings.add(((KeywordFuel)keyword).fuel);
                    }else if(keyword instanceof KeywordPriority){
                        priorityStrings.add(((KeywordPriority)keyword).input);
                    }else if(keyword instanceof KeywordSymmetry){
                        symmetryStrings.add(((KeywordSymmetry)keyword).input);
                    }else if(keyword instanceof KeywordFormat){
                        formatStrings.add(((KeywordFormat)keyword).input);
                    }
                }
                if(x==0||y==0||z==0){
                    x = y = z = 3;
                }
                if(!(underhaul||overhaul))underhaul = true;
                if(configuration==null)configuration = Configuration.configurations.get(0);
                if(underhaul&&overhaul){
                    event.getChannel().sendMessage("Please choose either `underhaul` or `overhaul`, not both!").queue();
                    return;
                }
                if(configuration.underhaul==null&&underhaul){
                    event.getChannel().sendMessage("`"+configuration.name+" doesn't have an Underhaul configuration!").queue();
                    return;
                }
                if(configuration.overhaul==null&&overhaul){
                    event.getChannel().sendMessage("`"+configuration.name+" doesn't have an Overhaul configuration!").queue();
                    return;
                }
                if(multiblockName==null||multiblockName.isEmpty())multiblockName = "SFR";
                String fullMultiblockName = (overhaul?"Overhaul ":"Underhaul ")+multiblockName.toUpperCase();
                Multiblock multiblock = null;
                ArrayList<Range<Block>> blockRanges = new ArrayList<>();
                for(Multiblock m : Core.multiblockTypes){
                    if(m.getDefinitionName().equalsIgnoreCase(fullMultiblockName))multiblock = m;
                }
                if(multiblock==null){
                    event.getChannel().sendMessage("Unknown multiblock: `"+fullMultiblockName+"`!").queue();
                    return;
                }
                ArrayList<Block> availableBlocks = new ArrayList<>();
                multiblock.getAvailableBlocks(availableBlocks);
                FOR:for(Range<String> range : stringRanges){
                    String blockNam = range.obj.toLowerCase();
                    if(blockNam.endsWith("s"))blockNam = blockNam.substring(0, blockNam.length()-1);
                    blockNam = blockNam.replace("_", " ").replace("liquid ", "").replace(" cooler", "").replace(" heat sink", "").replace(" heatsink", "").replace(" sink", "").replace(" neutron shield", "").replace(" shield", "").replace(" moderator", "").replace(" heater", "").replace("fuel ", "").replace(" reflector", "");
                    for(Block block : availableBlocks){
                        if(block.getName().toLowerCase().replace("_", " ").replace("liquid ", "").replace(" cooler", "").replace(" heat sink", "").replace(" heatsink", "").replace(" sink", "").replace(" neutron shield", "").replace(" shield", "").replace(" moderator", "").replace(" heater", "").replace("fuel ", "").replace(" reflector", "").equalsIgnoreCase(blockNam)){
                            blockRanges.add(new Range(block, range.min, range.max));
                            continue FOR;
                        }
                    }
                    event.getChannel().sendMessage("Unknown block: `"+blockNam+"`!").queue();
                    return;
                }
                Object fuels = null;
                switch(multiblock.getMultiblockID()){
                    case 0://underhaul SFR
                        multiblock.configuration.underhaul.fissionsfr.Fuel fuel = null;
                        FUEL:for(String str : fuelStrings){
                            for(multiblock.configuration.underhaul.fissionsfr.Fuel f : configuration.underhaul.fissionSFR.fuels){
                                if(f.name.equalsIgnoreCase(str)){
                                    if(fuel!=null){
                                        event.getChannel().sendMessage("Underhaul SFRs can only have one fuel!").queue();
                                        return;
                                    }
                                    fuel = f;
                                    continue FUEL;
                                }
                            }
                            event.getChannel().sendMessage("Unknown fuel: "+str).queue();
                            return;
                        }
                        if(fuel==null)fuel = configuration.underhaul.fissionSFR.fuels.get(0);
                        fuels = fuel;
                        break;
                    case 1://overhaul SFR
                        ArrayList<multiblock.configuration.overhaul.fissionsfr.Fuel> sfrFuels = new ArrayList<>();
                        FUEL:for(String str : fuelStrings){
                            for(multiblock.configuration.overhaul.fissionsfr.Fuel f : configuration.overhaul.fissionSFR.fuels){
                                if(f.name.equalsIgnoreCase(str)){
                                    sfrFuels.add(f);
                                    continue FUEL;
                                }
                            }
                            event.getChannel().sendMessage("Unknown fuel: "+str).queue();
                            return;
                        }
                        if(sfrFuels.isEmpty())sfrFuels.add(configuration.overhaul.fissionSFR.fuels.get(0));
                        fuels = sfrFuels;
                        break;
                    case 2://overhaul MSR
                        ArrayList<multiblock.configuration.overhaul.fissionmsr.Fuel> msrFuels = new ArrayList<>();
                        FUEL:for(String str : fuelStrings){
                            for(multiblock.configuration.overhaul.fissionmsr.Fuel f : configuration.overhaul.fissionMSR.fuels){
                                if(f.name.equalsIgnoreCase(str)){
                                    msrFuels.add(f);
                                    continue FUEL;
                                }
                            }
                            event.getChannel().sendMessage("Unknown fuel: "+str).queue();
                            return;
                        }
                        if(msrFuels.isEmpty())msrFuels.add(configuration.overhaul.fissionMSR.fuels.get(0));
                        fuels = msrFuels;
                        break;
                }
                Priority.Preset priority = null;
                ArrayList<Priority> priorities = multiblock.getGenerationPriorities();
                ArrayList<Priority.Preset> presets = multiblock.getGenerationPriorityPresets(priorities);
                for(Priority.Preset preset : presets){
                    for(String str : priorityStrings){
                        for(String alternative : preset.alternatives){
                            if(str.equalsIgnoreCase(alternative)){
                                if(priority!=null){
                                    event.getChannel().sendMessage("You can only target one priority at a time!\nDownload the generator for more control over generation priorities (see footnote)").queue();
                                    return;
                                }
                                priority = preset;
                            }
                        }
                    }
                }
                if(priority==null)priority = presets.get(0);
                ArrayList<Symmetry> symmetries = new ArrayList<>();
                ArrayList<Symmetry> availableSymmetries = multiblock.getSymmetries();
                for(Symmetry symmetry : availableSymmetries){
                    for(String sym : symmetryStrings){
                        if(symmetry instanceof AxialSymmetry){
                            if(((AxialSymmetry)symmetry).matches(sym)){
                                symmetries.add(symmetry);
                            }
                        }else{
                            if(symmetry.name.equalsIgnoreCase(sym))symmetries.add(symmetry);
                        }
                    }
                }
                ArrayList<FormatWriter> formats = new ArrayList<>();
                for(String format : formatStrings){
                    for(FormatWriter writer : FileWriter.formats){
                        for(String extention : writer.getExtensions()){
                            if(format.toLowerCase().contains(extention)){
                                formats.add(writer);
                                break;
                            }
                        }
                    }
                }
                if(formats.isEmpty()){
                    formats.add(FileWriter.formats.get(0));//hellrage json
                    formats.add(FileWriter.formats.get(2));//NCPF
                }
                formats.add(FileWriter.formats.get(1));//png
                Multiblock multiblockInstance = multiblock.newInstance(x,y,z);
                if(multiblockInstance instanceof UnderhaulSFR){
                    ((UnderhaulSFR)multiblockInstance).fuel = (Fuel)fuels;
                }
                if(multiblockInstance instanceof OverhaulSFR){
                    ArrayList<Range<multiblock.configuration.overhaul.fissionsfr.Source>> validSources = new ArrayList<>();
                    for(multiblock.configuration.overhaul.fissionsfr.Source s : configuration.overhaul.fissionSFR.sources){
                        validSources.add(new Range(s, 0));
                    }
                    ((OverhaulSFR)multiblockInstance).setValidSources(validSources);
                    ArrayList<Range<multiblock.configuration.overhaul.fissionsfr.Fuel>> validFuels = new ArrayList<>();
                    for(multiblock.configuration.overhaul.fissionsfr.Fuel f : (ArrayList<multiblock.configuration.overhaul.fissionsfr.Fuel>)fuels){
                        validFuels.add(new Range(f, 0));
                    }
                    ((OverhaulSFR)multiblockInstance).setValidFuels(validFuels);
                }
                if(multiblockInstance instanceof OverhaulMSR){
                    ArrayList<Range<multiblock.configuration.overhaul.fissionmsr.Source>> validSources = new ArrayList<>();
                    for(multiblock.configuration.overhaul.fissionmsr.Source s : configuration.overhaul.fissionMSR.sources){
                        validSources.add(new Range(s, 0));
                    }
                    ((OverhaulMSR)multiblockInstance).setValidSources(validSources);
                    ArrayList<Range<multiblock.configuration.overhaul.fissionmsr.Fuel>> validFuels = new ArrayList<>();
                    for(multiblock.configuration.overhaul.fissionmsr.Fuel f : (ArrayList<multiblock.configuration.overhaul.fissionmsr.Fuel>)fuels){
                        validFuels.add(new Range(f, 0));
                    }
                    ((OverhaulMSR)multiblockInstance).setValidFuels(validFuels);
                }
                try{
                    generator = MultiblockGenerator.getGenerators(multiblock).get(0).newInstance(multiblockInstance);
                }catch(IndexOutOfBoundsException ex){
                    throw new IllegalArgumentException("No generators available for multiblock!", ex);
                }
                if(generator instanceof StandardGenerator){
                    StandardGeneratorSettings settings = new StandardGeneratorSettings((StandardGenerator)generator);
                    settings.finalMultiblocks = 1;
                    settings.workingMultiblocks = 1;
                    settings.timeout = 10;
                    priority.set(priorities);
                    settings.priorities.addAll(priorities);
                    settings.symmetries.addAll(symmetries);
                    ArrayList<PostProcessingEffect> ppes = multiblock.getPostProcessingEffects();
                    for(PostProcessingEffect ppe : ppes){
                        if(ppe instanceof ClearInvalid){
                            settings.postProcessingEffects.add(ppe);
                        }
                    }
                    for(Range<Block> range : blockRanges){
                        if(range.min==0&&range.max==0)continue;
                        settings.allowedBlocks.add(range);
                    }
                    FOR:for(Block b : availableBlocks){
                        for(Range<Block> range : blockRanges){
                            if(range.obj==b)continue FOR;
                        }
                        if(b.defaultEnabled())settings.allowedBlocks.add(new Range(b, 0));
                    }
                    settings.changeChancePercent = 1;
                    settings.variableRate = true;
                    settings.lockCore = false;
                    settings.fillAir = true;
                    generator.refreshSettings(settings);
                    Core.configuration = configuration;
                    Thread t = new Thread(() -> {
                        generator.startThread();
                        for(NCPFFile file : storedMultiblocks.keySet()){
                            for(Multiblock m : file.multiblocks){
                                if(m.getMultiblockID()==generator.multiblock.getMultiblockID())generator.importMultiblock(m);
                            }
                        }
                        String configName = Core.configuration.getShortName();
                        generatorMessage = event.getChannel().sendMessage(createEmbed("Generating "+(configName==null?"":configName+" ")+generator.multiblock.getGeneralName()+"s...").addField("Reactor Details", generator.getMainMultiblockBotTooltip(), false).build()).complete();
                        int time = 0;
                        int interval = 1000;//1 sec
                        int maxTime = 60000;//60 sec
                        int timeout = 10000;//10 sec
                        while(time<maxTime){
                            try{
                                Thread.sleep(interval);
                            }catch(InterruptedException ex){
                                printErrorMessage(event.getChannel(), "Generation Interrupted!", ex);
                                break;
                            }
                            time+=interval;
                            if(!generator.isRunning())break;
                            Multiblock main = generator.getMainMultiblock();
                            generatorMessage.editMessage(createEmbed("Generating "+(configName==null?"":configName+" ")+generator.multiblock.getGeneralName()+"s...").addField("Reactor Details", generator.getMainMultiblockBotTooltip(), false).build()).queue();
                            if(main!=null&&main.millisSinceLastChange()<maxTime&&main.millisSinceLastChange()>timeout)break;
                        }
                        generator.stopAllThreads();
                        Multiblock finalMultiblock = generator.getMainMultiblock();
                        if(finalMultiblock==null||finalMultiblock.isEmpty()){
                            generatorMessage.editMessage(createEmbed("No "+generator.multiblock.getGeneralName().toLowerCase()+" was generated. :(").build()).queue();
                        }else{
                            generatorMessage.editMessage(createEmbed("Generated "+(configName==null?"":configName+" ")+generator.multiblock.getGeneralName()).addField("Reactor Details", finalMultiblock.getBotTooltip(), false).build()).queue();
                            NCPFFile ncpf = new NCPFFile();
                            String name = UUID.randomUUID().toString();
                            ncpf.metadata.put("Author", "S'plodo-Bot");
                            finalMultiblock.metadata.put("Author", "S'plodo-Bot");
                            ncpf.metadata.put("Name", name);
                            finalMultiblock.metadata.put("Name", name);
                            GregorianCalendar calendar = new GregorianCalendar();
                            String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                            ncpf.metadata.put("Generation Date", months[calendar.get(Calendar.MONTH)]+" "+calendar.get(Calendar.DAY_OF_MONTH)+", "+calendar.get(Calendar.YEAR));
                            ncpf.metadata.put("Generation Time", calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+"."+calendar.get(Calendar.MILLISECOND));
                            ncpf.multiblocks.add(finalMultiblock);
                            ncpf.configuration = PartialConfiguration.generate(Core.configuration, ncpf.multiblocks);
                            for(FormatWriter writer : formats){
                                CircularStream stream = new CircularStream(1024*1024);//1MB
                                CompletableFuture<Message> submit = event.getChannel().sendFile(stream.getInput(), (configName==null?"":configName+" ")+generator.multiblock.getX()+"x"+generator.multiblock.getY()+"x"+generator.multiblock.getZ()+" "+generator.multiblock.getGeneralName()+"."+writer.getExtensions()[0]).submit();
                                try{
                                    writer.write(ncpf, stream);
                                }catch(Exception ex){
                                    printErrorMessage(event.getChannel(), "Failed to write file", ex);
                                    submit.cancel(true);
                                    stream.close();
                                }
                            }
                        }
                        generator = null;
                        generatorMessage = null;
                    });
                    t.setDaemon(true);
                    t.setName("Discord Bot Generation Thread");
                    t.start();
                }else{
                    throw new IllegalArgumentException("I don't know how to use the non-standard generators!");
                }
            }
        });
        //fun commands
        playCommands.add(new Command("help"){
            @Override
            public String getHelpText(){
                return "Shows this help window";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                EmbedBuilder builder = createEmbed(jda.getSelfUser().getName()+" Help");
                String prefx = "";
                for(String s : Bot.prefixes){
                    prefx+="`"+s+"`\t";
                }
                builder.addField("Prefixes", prefx.trim(), false);
                for(Command c : playCommands){
                    if(c.isSecret())continue;
                    builder.addField(prefixes.get(0)+c.command, c.getHelpText(), false);
                }
                event.getChannel().sendMessage(builder.build()).queue();
            }
        });
        playCommands.add(new Command("stop","abort","halt","cancel","finish"){
            @Override
            public String getHelpText(){
                return "Cancels your current action";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                if(PlayBot.actions.containsKey(event.getAuthor().getIdLong())){
                    PlayBot.actions.get(event.getAuthor().getIdLong()).cancel(event.getChannel());
                    PlayBot.actions.remove(event.getAuthor().getIdLong());
                }
            }
        });
        playCommands.add(new Command("smore", "s'more", "s’more"){
            @Override
            public String getHelpText(){
                return "Mmmm, s'mores";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                PlayBot.action(event, new SmoreAction());
            }
        });
        playCommands.add(new Command("give", "send", "pay"){
            @Override
            public String getHelpText(){
                return "Give someone your s'mores!";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                args = args.trim();
                if(args.isEmpty()){
                    event.getChannel().sendMessage("You give nobody nothing.").queue();
                    return;
                }
                long have = PlayBot.getSmoreCount(event.getAuthor().getIdLong());
                if(have<=0){
                    event.getChannel().sendMessage("You don't have any s'mores!").queue();
                    return;
                }
                String[] argses = args.split(" ");
                if(argses.length==1){
                    try{
                        long amt = Long.parseLong(argses[0]);
                        if(have<amt)event.getChannel().sendMessage("You don't have "+amt+" s'more"+(amt==1?"":"s")+"!").queue();
                        else event.getChannel().sendMessage("You try to give nobody "+amt+" s'more"+(amt==1?"":"s")+", but nobody doesn't respond.").queue();
                    }catch(Exception ex){
                        event.getChannel().sendMessage("You try to give `"+argses[0].replace("`", "\\`")+"` nothing, but nothing happens.").queue();
                    }
                    return;
                }
                String target = argses[0];
                long targetID = 0;
                if(target.startsWith("<@")&&target.endsWith(">")){
                    if(target.contains("!"))target = target.substring(1);
                    try{
                        targetID = Long.parseLong(target.substring(2, target.length()-1));
                    }catch(Exception ex){
                        event.getChannel().sendMessage("Who?").queue();
                        return;
                    }
                }else{
                    event.getChannel().sendMessage("Who?").queue();
                    return;
                }
                User targetUser;
                try{
                    targetUser = jda.getUserById(targetID);
                    if(targetUser==null){
                        event.getChannel().sendMessage("Who?").queue();
                        return;
                    }
                }catch(Exception ex){
                    event.getChannel().sendMessage("Who?").queue();
                    return;
                }
                long amt = 0;
                try{
                    amt = Long.parseLong(argses[1]);
                }catch(Exception ex){
                    event.getChannel().sendMessage("How many?").queue();
                    return;
                }
                if(have<amt){
                    event.getChannel().sendMessage("You don't have "+amt+" s'more"+(amt==1?"":"s")+"!").queue();
                    return;
                }
                if(amt<0){
                    event.getChannel().sendMessage("You can't give negative s'mores!").queue();
                    return;
                }
                if(amt==0){
                    event.getChannel().sendMessage("Nothing happens.").queue();
                    return;
                }
                event.getChannel().sendMessage("You gave "+nick(event.getGuild().getMember(targetUser))+" "+amt+" smore"+(amt==1?"":"s")+".").queue();
                PlayBot.removeSmores(event.getAuthor(), amt);
                PlayBot.addSmores(targetUser, amt);
            }
        });
        playCommands.add(new Command("eat", "nom"){
            @Override
            public String getHelpText(){
                return "Yummy!";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                long have = PlayBot.getSmoreCount(event.getAuthor().getIdLong());
                if(have<=0){
                    event.getChannel().sendMessage("You don't have any s'mores!").queue();
                    return;
                }
                long eat = 1;
                try{
                    eat = Long.parseLong(args.trim());
                }catch(NumberFormatException __){}
                if(have<eat){
                    event.getChannel().sendMessage("You don't have "+eat+" s'more"+(eat==1?"":"s")+"!").queue();
                    return;
                }
                if(eat<0){
                    event.getChannel().sendMessage("You can't eat negative s'mores!").queue();
                    return;
                }
                if(eat==0){
                    event.getChannel().sendMessage("You eat nothing. Nothing happens.").queue();
                    return;
                }
                if(eat>255){
                    event.getChannel().sendMessage("You can't eat more than 255 s'mores in a single *byte*!").queue();
                    return;
                }
                String[] noms = {" Nom nom nom.", " Tasty!", " Yum!"};
                event.getChannel().sendMessage("You eat "+eat+" smore"+(eat==1?"":"s")+"."+noms[new Random().nextInt(noms.length)]).queue();
                PlayBot.removeSmores(event.getAuthor(), eat);
            }
        });
        playCommands.add(new SecretCommand("moresmore", "mores'more", "mores’more", "doublesmore", "doubles'more", "doubles’more"){
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                event.getChannel().sendMessage("If you tried making two s'mores at once, your arms would get tired and you'd drop them! You wouldn't want that, would you?").queue();
            }
        });
        playCommands.add(new SecretCommand("foursmore", "fours'more", "fours’more", "quadsmore", "quads'more", "quads’more"){
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                event.getChannel().sendMessage("To make four s'mores at once, you'd need four arms. You don't have four arms.").queue();
            }
        });
        playCommands.add(new SecretCommand("smorelord", "s'morelord", "s’morelord"){
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                for(Role role : event.getGuild().getMember(event.getAuthor()).getRoles()){
                    if(role.getIdLong()==563124574032756746L){
                        if(PlayBot.getSmoreCount(event.getAuthor().getIdLong())<0){
                            event.getChannel().sendMessage("You try to make a s'morelord, but you are stopped by the S'more bank. They want the S'mores you owe them.").queue();
                            return;
                        }
                        PlayBot.action(event, new SmoreLordAction());
                        return;
                    }
                }
                event.getChannel().sendMessage("You need a special S'mengineering degree to even comprehend smorelordship").queue();
            }
        });
        playCommands.add(new SecretCommand("glowshroom"){
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                event.getChannel().sendMessage("You don't see a glowshroom").queue();
            }
        });
        playCommands.add(new SecretCommand("actions"){
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                String actions = "";
                for(Long key : PlayBot.actions.keySet()){
                    actions+=nick(event.getGuild().getMemberById(key))+" is "+PlayBot.actions.get(key).getAction()+"\n";
                }
                event.getChannel().sendMessage(actions).queue();
            }
        });
        playCommands.add(new Command("smores", "s'mores", "s’mores", "bal","balance","money"){
            @Override
            public String getHelpText(){
                return "Displays the amount of s'mores currently in your possession";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                event.getChannel().sendMessage(PlayBot.getSmoreCountS(event.getAuthor().getIdLong())).queue();
            }
        });
        playCommands.add(new Command("leaderboard", "smoreboard", "s'moreboard", "s’moreboard"){
            @Override
            public String getHelpText(){
                return "Displays the top 5 s'more stockpilers";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                ArrayList<Long> smorepilers = new ArrayList<>(PlayBot.smores.keySet());
                Collections.sort(smorepilers, (Long o1, Long o2) -> (int)(PlayBot.smores.get(o2)-PlayBot.smores.get(o1)));
                EmbedBuilder builder = createEmbed("S'moreboard");
                String mess = "";
                for(int i = 0; i<Math.min(5, smorepilers.size()); i++){
                    mess+=nick(event.getGuild().getMemberById(smorepilers.get(i)))+": "+PlayBot.getSmoreCountS(smorepilers.get(i))+"\n";
                }
                event.getChannel().sendMessage(builder.addField("Top S'more Stockpilers", mess, false).build()).queue();
            }
        });
        playCommands.add(new SecretCommand("snoreboard", "s'noreboard", "s’noreboard"){
            @Override
            public String getHelpText(){
                return "Displays the bottom 5 s'more stockpilers";
            }
            @Override
            public void run(GuildMessageReceivedEvent event, String args, boolean debug){
                ArrayList<Long> smorepilers = new ArrayList<>(PlayBot.smores.keySet());
                Collections.sort(smorepilers, (Long o1, Long o2) -> (int)(PlayBot.smores.get(o1)-PlayBot.smores.get(o2)));
                EmbedBuilder builder = createEmbed("Snoreboard");
                String mess = "";
                for(int i = 0; i<Math.min(5, smorepilers.size()); i++){
                    mess+=nick(event.getGuild().getMemberById(smorepilers.get(i)))+": "+PlayBot.getSmoreCountS(smorepilers.get(i))+"\n";
                }
                event.getChannel().sendMessage(builder.addField("Top S'more Debtors", mess, false).build()).queue();
            }
        });
    }
    private static String nick(Member member){
        String name = member.getNickname();
        if(name==null)name = member.getUser().getName();
        return "`"+name.replace("`", "")+"`";
    }
    @Override
    public void onGatewayPing(GatewayPingEvent event){
        super.onGatewayPing(event);
        PlayBot.save();
    }
    @Override
    public void onReady(ReadyEvent event){
        Thread channelRead = new Thread(() -> {
            int bytes = 0;
            int totalCount = 0;
            for(Long id : dataChannels){
                TextChannel channel = jda.getTextChannelById(id);
                if(channel==null){
                    System.err.println("Invalid channel: "+id);
                    continue;
                }
                System.out.println("Reading channel: "+channel.getName());
                MessageHistory history = channel.getHistoryFromBeginning(batchSize).complete();
                int count = 0;
                while(true){
                    Message last = null;
                    ArrayList<Message> messages = new ArrayList<>(history.getRetrievedHistory());
                    Stack<Message> stak = new Stack<>();
                    for(Message m : messages){
                        stak.push(m);
                    }
                    messages.clear();
                    while(!stak.isEmpty())messages.add(stak.pop());
                    for(Message message : messages){
                        last = message;
                        storeReactors(message);
                        for(Attachment att : message.getAttachments()){
                            if(att==null||att.getFileExtension()==null)continue;
                            if(att.getFileExtension().equalsIgnoreCase("json")){
                                count++;
                                System.out.println("Found "+att.getFileName()+" ("+count+")");
                                bytes+=att.getSize();
                            }
                        }
                    }
                    if(last==null)break;
                    if(history.size()<batchSize){
                        break;
                    }else{
                        history = channel.getHistoryAfter(last, batchSize).complete();
                    }
                }
                System.out.println("Finished Reading channel: "+channel.getName()+". Reactors: "+count);
                totalCount+=count;
            }
            System.out.println("Total Reactors: "+totalCount);
            System.out.println("Total Size: "+bytes);
        });
        channelRead.setDaemon(true);
        channelRead.start();
    }
    public void storeReactors(Message message){
        for(Attachment att : message.getAttachments()){
            if(att!=null&&att.getFileExtension()!=null&&att.getFileExtension().toLowerCase().contains("png"))continue;
            try{
                NCPFFile ncpf = FileReader.read(() -> {
                    try{
                        return att.retrieveInputStream().get();
                    }catch(InterruptedException|ExecutionException ex){
                        throw new RuntimeException(ex);
                    }
                });
                if(ncpf!=null)storedMultiblocks.put(ncpf, message.getJumpUrl());
            }catch(Exception ex){
                System.err.println("Failed to read file: "+att.getFileName());
            }
        }
    }
    private static EmbedBuilder createEmbed(String title){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(title);
        builder.setColor(new Color(255, 200, 0));
        builder.setFooter("Powered by https://github.com/ThizThizzyDizzy/nc-reactor-generator/releases");
        return builder;
    }
    public static void start(String[] args){
        PlayBot.load();
        for(int i = 2; i<args.length; i++){
            String arg = args[i];
            if(arg.startsWith("bot"))botChannels.add(Long.parseLong(arg.substring(3)));
            else if(arg.startsWith("play"))playChannels.add(Long.parseLong(arg.substring(4)));
            else if(arg.startsWith("data"))dataChannels.add(Long.parseLong(arg.substring(4)));
            else prefixes.add(arg);
        }
        if(prefixes.isEmpty())prefixes.add("-");
        config = Config.newConfig(new File(new File("special.dat").getAbsolutePath()));
        config.load();
        cookies = config.get("cookies", 0);
        JDABuilder b = new JDABuilder(AccountType.BOT);
        b.setToken(args[1]);
        b.setActivity(Activity.watching("cookies accumulate ("+cookies+")"));
        b.addEventListeners(new Bot());
        try{
            jda = b.build();
        }catch(LoginException ex){
            Sys.error(ErrorLevel.critical, "Failed to log in!", ex, ErrorCategory.InternetIO);
        }
    }
    public static void stop(){
        PlayBot.save();
        if(jda!=null)jda.shutdownNow();
    }
    public static void render2D(){
        if(pendingImage!=null){
            image = Core.makeImage(imgWidth, imgHeight, pendingImage);
            pendingImage = null;
        }
    }
    public static boolean isRunning(){
        return jda!=null;
    }
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        if(event.getAuthor().isBot())return;
        storeReactors(event.getMessage());
        if(botChannels.contains(event.getChannel().getIdLong())){
            String command = event.getMessage().getContentRaw();
            boolean hasPrefix = false;
            for(String prefix : prefixes){
                if(command.startsWith(prefix)){
                    command = command.substring(prefix.length());
                    hasPrefix = true;
                    break;
                }
            }
            if(!hasPrefix)return;
            try{
                for(Command cmd : botCommands){
                    for(String alt : cmd.alternates){
                        if(command.equalsIgnoreCase(alt)||command.startsWith(alt+" ")){
                            String args = command.substring(alt.length()).trim();
                            try{
                                cmd.run(event, args, debug);
                            }catch(Exception ex){
                                printErrorMessage(event.getChannel(), "Caught exception running command `"+alt+"`!", ex);
                            }
                        }
                    }
                }
            }catch(Exception ex){
                printErrorMessage(event.getChannel(), "Caught exception loading command!", ex);
            }
        }
        if(playChannels.contains(event.getChannel().getIdLong())){
            String command = event.getMessage().getContentRaw();
            boolean hasPrefix = false;
            for(String prefix : prefixes){
                if(command.startsWith(prefix)){
                    command = command.substring(prefix.length());
                    hasPrefix = true;
                    break;
                }
            }
            if(!hasPrefix)return;
            try{
                for(Command cmd : playCommands){
                    for(String alt : cmd.alternates){
                        if(command.equalsIgnoreCase(alt)||command.startsWith(alt+" ")){
                            String args = command.substring(alt.length()).trim();
                            try{
                                cmd.run(event, args, debug);
                            }catch(Exception ex){
                                printErrorMessage(event.getChannel(), "Caught exception running command `"+alt+"`!", ex);
                            }
                        }
                    }
                }
            }catch(Exception ex){
                printErrorMessage(event.getChannel(), "Caught exception loading command!", ex);
            }
        }
    }
    private static void printErrorMessage(TextChannel channel, String message, Exception ex){
        String trace = "";
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for(StackTraceElement e : stackTrace){
            if(e.getClassName().startsWith("net."))continue;
            if(e.getClassName().startsWith("com."))continue;
            String[] splitClassName = e.getClassName().split("\\Q.");
            String filename = splitClassName[splitClassName.length-1]+".java";
            String nextLine = "\nat "+e.getClassName()+"."+e.getMethodName()+"("+filename+":"+e.getLineNumber()+")";
            if((trace+nextLine).length()+4>1024){
                trace+="\n...";
                break;
            }else trace+=nextLine;
        }
        channel.sendMessage(createEmbed(message).addField(ex.getClass().getName()+": "+ex.getMessage(), trace.trim(), false).build()).queue();
    }
    private static int imgWidth, imgHeight;
    private static BufferRenderer pendingImage = null;
    private static BufferedImage image = null;
    public static BufferedImage makeImage(int width, int height, BufferRenderer r){
        imgWidth = width;
        imgHeight = height;
        pendingImage = r;
        image = null;
        while(image==null)
            try{
                Thread.sleep(10);
            }catch(InterruptedException ex){}
        return image;
    }
}