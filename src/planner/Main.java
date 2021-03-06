package planner;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
public class Main{
    private static String versionListURL = "https://raw.githubusercontent.com/ThizThizzyDizzy/nc-reactor-generator/overhaul/versions.txt";
    public static final String applicationName = "Nuclearcraft Reactor Generator";
    private static HashMap<String[], Integer> requiredLibraries = new HashMap<>();
    private static int downloadSize = 0;
    //Download details
    private static int total;
    private static int current;
    public static boolean isBot = false;
    public static boolean hasAWT = true;
    public static boolean hasAWTAfterStartup = false;
    public static boolean headless = false;
    private static final int OS_UNKNOWN = -1;
    private static final int OS_WINDOWS = 0;
    private static final int OS_MACOS = 1;
    private static final int OS_LINUX = 2;
    private static int os = OS_UNKNOWN;//Should not be directly referenced from other classes, as there are always better ways of handling OS-compatibility
    private static void addRequiredLibrary(String url, String filename, int sizeKB){
        requiredLibraries.put(new String[]{url,filename}, sizeKB);
    }
    public static void main(String[] args){
        try{
            if(args.length>=1&&args[0].equals("headless")||args.length>=2&&args[1].equals("headless")||args.length>=3&&args[2].equals("headless")){
                hasAWT = false;
                headless = true;
            }
            if(args.length>=1&&args[0].equals("noAWT")||args.length>=2&&args[1].equals("noAWT")||args.length>=3&&args[2].equals("noAWT")){
                hasAWT = false;
            }
            if(args.length>=1&&args[0].equals("noAWTDuringStartup")||args.length>=2&&args[1].equals("noAWTDuringStartup")||args.length>=3&&args[2].equals("noAWTDuringStartup")){
                hasAWT = false;
                hasAWTAfterStartup = true;
            }
            if(args.length>=1&&args[0].equals("maybediscord")||args.length>=2&&args[1].equals("maybediscord")||args.length>=3&&args[2].equals("maybediscord")){
                if(hasAWT){
                    if(javax.swing.JOptionPane.showOptionDialog(null, "Bot or Planner?", "Discord?", javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, new String[]{"Bot", "Planner"}, "Planner")==0)args[1] = "discord";
                }else{
                    System.out.println("Bot or Planner? (B|P)\n> ");
                    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                    String s = r.readLine();
                    if(s==null)s = "";
                    s = s.trim();
                    r.close();
                    if(s.equalsIgnoreCase("B")||s.equalsIgnoreCase("Bot")||s.equalsIgnoreCase("Discord"))args[1] = "discord";
                }
            }
            if(args.length>=1&&args[0].equals("discord")||args.length>=2&&args[1].equals("discord")||args.length>=3&&args[2].equals("discord")){
                isBot = true;
            }
            System.out.println("Initializing...");
            args = update(args);
            if(args==null){
                return;
            }
            Core.main(args);
        }catch(Exception ex){
            if(hasAWT){
                String trace = "";
                for(StackTraceElement e : ex.getStackTrace()){
                    trace+="\n"+e.toString();
                }
                trace = trace.isEmpty()?trace:trace.substring(1);
                javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage()+"\n"+trace, "CAUGHT ERROR: "+ex.getClass().getName()+" on main thread!", javax.swing.JOptionPane.ERROR_MESSAGE);
            }else{
                throw new RuntimeException("Exception on main thread!", ex);
            }
        }
    }
    private static String getLibraryRoot(){
        return "libraries";
    }
    private static String[] update(String[] args) throws URISyntaxException, IOException, InterruptedException{
        ArrayList<String> theargs = new ArrayList<>(Arrays.asList(args));
        if(args.length<1||!args[0].equals("Skip Dependencies")){
            setLookAndFeel();
            if(versionListURL.isEmpty()){
                System.err.println("Version list URL is empty! assuming latest version.");
            }else{
                System.out.println("Checking for updates...");
                Updater updater = Updater.read(versionListURL, VersionManager.currentVersion, applicationName);
                if(updater!=null&&updater.getVersionsBehindLatestDownloadable()>0){
                    boolean allowUpdate = false;
                    if(hasAWT){
                        allowUpdate = javax.swing.JOptionPane.showConfirmDialog(null, "Version "+updater.getLatestDownloadableVersion()+" is out!  Would you like to update "+applicationName+" now?", applicationName+" "+VersionManager.currentVersion+"- Update Available", javax.swing.JOptionPane.YES_NO_OPTION)==javax.swing.JOptionPane.YES_OPTION;
                    }else{
                        System.out.println("Version "+updater.getLatestDownloadableVersion()+" is out!  Would you like to update "+applicationName+" now? (Y/N)");
                        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                        String s = r.readLine();
                        if(s==null)s = "";
                        s = s.trim();
                        r.close();
                        allowUpdate = s.equalsIgnoreCase("Y")||s.equalsIgnoreCase("Yes");
                    }
                    if(allowUpdate){
                        System.out.println("Updating...");
                        startJava(new String[0], new String[]{"justUpdated"}, updater.update(updater.getLatestDownloadableVersion()));
                        System.exit(0);
                    }
                }
                System.out.println("Update Check Complete.");
            }
            addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/jar/lwjgl-assimp.jar", "lwjgl-assimp.jar", 214);
            addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/jar/lwjgl-glfw.jar", "lwjgl-glfw.jar", 105);
            addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/jar/lwjgl-openal.jar", "lwjgl-openal.jar", 78);
            addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/jar/lwjgl-opengl.jar", "lwjgl-opengl.jar", 915);
            addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/jar/lwjgl-stb.jar", "lwjgl-stb.jar", 102);
            addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/jar/lwjgl.jar", "lwjgl-3.2.3.jar", 540);
            String osName = System.getProperty("os.name");
            if(osName==null)osName = "null";
            osName = osName.toLowerCase(Locale.ENGLISH);
            if(osName.contains("win"))os = OS_WINDOWS;
            if(osName.contains("mac"))os = OS_MACOS;
            if(osName.contains("nix")||osName.contains("nux")||osName.contains("aix"))os = OS_LINUX;
            if(os==OS_UNKNOWN){
                System.out.println("Unknown OS: "+osName);
                if(hasAWT){
                    os = javax.swing.JOptionPane.showOptionDialog(null, "Unrecognized OS \""+osName+"\"!\nPlease report this problem at https://github.com/ThizThizzyDizzy/nc-reactor-generator/issues.\nIn the meantime, which OS are you using?", "Unrecognized Operating System", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE, null, new String[]{"Windows", "Mac OS", "Linux"}, "Windows");
                }else{
                    System.out.println("Unrecognized OS \""+osName+"\"! Please report this problem at https://github.com/ThizThizzyDizzy/nc-reactor-generator/issues\nWhich OS are you using? (Windows|Mac|Linux)");
                    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                    String s = r.readLine();
                    if(s==null)s = "";
                    s = s.trim();
                    r.close();
                    if(s.equalsIgnoreCase("Windows"))os = OS_WINDOWS;
                    if(s.equalsIgnoreCase("Mac")||s.equalsIgnoreCase("MacOS")||s.equalsIgnoreCase("Mac OS"))os = OS_MACOS;
                    if(s.equalsIgnoreCase("Linux"))os = OS_LINUX;
                }
                if(os<0||os>2){
                    System.exit(0);
                }
            }
            switch(os){
                case OS_WINDOWS:
                    {
                        final int ARCH_UNKNOWN = -1;
                        final int ARCH_X86 = 0;
                        final int ARCH_X64 = 1;
                        int arch = ARCH_UNKNOWN;
                        String osArch = System.getProperty("os.arch");
                        if(osArch==null)osArch = "null";
                        osArch = osArch.toLowerCase(Locale.ENGLISH);
                        if(osArch.equals("amd64"))arch = ARCH_X64;
                        if(osArch.equals("x86"))arch = ARCH_X86;
                        System.out.println("OS: Windows");
                        if(arch==ARCH_UNKNOWN){
                            System.out.println("Unknown Architecture: "+osArch);
                            if(hasAWT){
                                arch = javax.swing.JOptionPane.showOptionDialog(null, "Unrecognized Architecture \""+osArch+"\"!\nPlease report this problem at https://github.com/ThizThizzyDizzy/nc-reactor-generator/issues.\nIn the meantime, what is your OS architecture?", "Unrecognized Operating System", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE, null, new String[]{"x86", "x64"}, "x64");
                            }else{
                                System.out.println("Unrecognized Architecture \""+osArch+"\"! Please report this problem at https://github.com/ThizThizzyDizzy/nc-reactor-generator/issues\nWhat is your OS architecture? (x86|x64)");
                                BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                                String s = r.readLine();
                                if(s==null)s = "";
                                s = s.trim();
                                r.close();
                                if(s.equalsIgnoreCase("x86")||s.equalsIgnoreCase("86")||s.equalsIgnoreCase("x32")||s.equalsIgnoreCase("32"))arch = ARCH_X86;
                                if(s.equalsIgnoreCase("x64")||s.equalsIgnoreCase("64"))arch = ARCH_X64;
                            }
                            if(arch<0||arch>1){
                                System.exit(0);
                            }
                        }
                        switch(arch){
                            case ARCH_X86:
                                System.out.println("OS Architecture: x86");
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-assimp-natives-windows-x86.jar", "lwjgl-assimp-natives-windows-x86.jar", 2106);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-glfw-natives-windows-x86.jar", "lwjgl-glfw-natives-windows-x86.jar", 132);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-natives-windows-x86.jar", "lwjgl-natives-windows-x86.jar", 117);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-openal-natives-windows-x86.jar", "lwjgl-openal-natives-windows-x86.jar", 598);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-opengl-natives-windows-x86.jar", "lwjgl-opengl-natives-windows-x86.jar", 81);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-stb-natives-windows-x86.jar", "lwjgl-stb-natives-windows-x86.jar", 208);
                                break;
                            case ARCH_X64:
                                System.out.println("OS Architecture: x64");
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-assimp-natives-windows.jar", "lwjgl-assimp-natives-windows.jar", 2533);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-glfw-natives-windows.jar", "lwjgl-glfw-natives-windows.jar", 137);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-natives-windows.jar", "lwjgl-natives-windows.jar", 133);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-openal-natives-windows.jar", "lwjgl-openal-natives-windows.jar", 634);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-opengl-natives-windows.jar", "lwjgl-opengl-natives-windows.jar", 89);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-stb-natives-windows.jar", "lwjgl-stb-natives-windows.jar", 233);
                                break;
                        }
                    }
                    break;
                case OS_MACOS:
                    System.out.println("OS: Mac OS");
                    addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-assimp-natives-macos.jar", "lwjgl-assimp-natives-macos.jar", 3068);
                    addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-glfw-natives-macos.jar", "lwjgl-glfw-natives-macos.jar", 65);
                    addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-natives-macos.jar", "lwjgl-natives-macos.jar", 39);
                    addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-openal-natives-macos.jar", "lwjgl-openal-natives-macos.jar", 516);
                    addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-opengl-natives-macos.jar", "lwjgl-opengl-natives-macos.jar", 39);
                    addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-stb-natives-macos.jar", "lwjgl-stb-natives-macos.jar", 191);
                    break;
                case OS_LINUX:
                    {
                        final int ARCH_UNKNOWN = -1;
                        final int ARCH_X64 = 0;
                        final int ARCH_ARM32 = 1;
                        final int ARCH_ARM64 = 2;
                        int arch = ARCH_UNKNOWN;
                        String osArch = System.getProperty("os.arch");
                        if(osArch==null)osArch = "null";
                        osArch = osArch.toLowerCase(Locale.ENGLISH);
                        if(osArch.equals("amd64"))arch = ARCH_X64;
                        if(osArch.equals("x64"))arch = ARCH_X64;
                        if(osArch.equals("arm32"))arch = ARCH_ARM32;
                        if(osArch.equals("arm64"))arch = ARCH_ARM64;
                        System.out.println("OS: Linux");
                        if(arch==ARCH_UNKNOWN){
                            System.out.println("Unknown Architecture: "+osArch);
                            if(hasAWT){
                                arch = javax.swing.JOptionPane.showOptionDialog(null, "Unrecognized Architecture \""+osArch+"\"!\nPlease report this problem at https://github.com/ThizThizzyDizzy/nc-reactor-generator/issues.\nIn the meantime, what is your OS architecture?", "Unrecognized Operating System", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE, null, new String[]{"x64", "arm32", "arm64"}, "x64");
                            }else{
                                System.out.println("Unrecognized Architecture \""+osArch+"\"! Please report this problem at https://github.com/ThizThizzyDizzy/nc-reactor-generator/issues\nWhat is your OS architecture? (x64|arm32|arm64)");
                                BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                                String s = r.readLine();
                                if(s==null)s = "";
                                s = s.trim();
                                r.close();
                                if(s.equalsIgnoreCase("x64")||s.equalsIgnoreCase("64"))arch = ARCH_X64;
                                if(s.equalsIgnoreCase("arm32"))arch = ARCH_ARM32;
                                if(s.equalsIgnoreCase("arm64"))arch = ARCH_ARM64;
                            }
                            if(arch<0||arch>2){
                                System.exit(0);
                            }
                        }
                        switch(arch){
                            case ARCH_X64:
                                System.out.println("OS Architecture: x64");
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-assimp-natives-linux.jar", "lwjgl-assimp-natives-linux.jar", 4097);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-glfw-natives-linux.jar", "lwjgl-glfw-natives-linux.jar", 156);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-natives-linux.jar", "lwjgl-natives-linux.jar", 74);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-openal-natives-linux.jar", "lwjgl-openal-natives-linux.jar", 578);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-opengl-natives-linux.jar", "lwjgl-opengl-natives-linux.jar", 77);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-stb-natives-linux.jar", "lwjgl-stb-natives-linux.jar", 196);
                                break;
                            case ARCH_ARM32:
                                System.out.println("OS Architecture: arm32");
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-assimp-natives-linux-arm32.jar", "lwjgl-assimp-natives-linux-arm32.jar", 3095);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-glfw-natives-linux-arm32.jar", "lwjgl-glfw-natives-linux-arm32.jar", 136);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-natives-linux-arm32.jar", "lwjgl-natives-linux-arm32.jar", 52);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-openal-natives-linux-arm32.jar", "lwjgl-openal-natives-linux-arm32.jar", 540);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-opengl-natives-linux-arm32.jar", "lwjgl-opengl-natives-linux-arm32.jar", 58);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-stb-natives-linux-arm32.jar", "lwjgl-stb-natives-linux-arm32.jar", 144);
                                break;
                            case ARCH_ARM64:
                                System.out.println("OS Architecture: arm64");
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-assimp-natives-linux-arm64.jar", "lwjgl-assimp-natives-linux-arm64.jar", 3726);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-glfw-natives-linux-arm64.jar", "lwjgl-glfw-natives-linux-arm64.jar", 139);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-natives-linux-arm64.jar", "lwjgl-natives-linux-arm64.jar", 50);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-openal-natives-linux-arm64.jar", "lwjgl-openal-natives-linux-arm64.jar", 542);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-opengl-natives-linux-arm64.jar", "lwjgl-opengl-natives-linux-arm64.jar", 57);
                                addRequiredLibrary("https://github.com/ThizThizzyDizzy/nc-reactor-generator/raw/42f710e533a43267dbff3b34651b33224ca7e071/libraries/lwjgl-3.2.3/native/lwjgl-stb-natives-linux-arm64.jar", "lwjgl-stb-natives-linux-arm64.jar", 177);
                                break;
                        }
                    }
                    break;
            }
            addRequiredLibrary("https://github.com/ThizThizzyDizzy/SimpleLibraryPlus/releases/download/v1.2.0/SimpleLibraryPlus-1.2.0.jar", "SimpleLibraryPlus-1.2.0.jar", 560);
            if(isBot){//I'll leave this on dropbox for now. What could possibly go wrong?
                addRequiredLibrary("https://www.dropbox.com/s/zeeu5wgmcisg4ez/JDA-4.1.1_101.jar?dl=1", "JDA-4.1.1_101.jar", 1097);
                addRequiredLibrary("https://www.dropbox.com/s/ljx8in7xona4akl/annotations-16.0.1.jar?dl=1", "annotations-16.0.1.jar", 19);
                addRequiredLibrary("https://www.dropbox.com/s/5fzv4attffxpn67/commons-collections4-4.1.jar?dl=1", "commons-collections4-4.1.jar", 734);
                addRequiredLibrary("https://www.dropbox.com/s/w9ca19hm60az7d6/jackson-annotations-2.10.1.jar?dl=1", "jackson-annotations-2.10.1.jar", 67);
                addRequiredLibrary("https://www.dropbox.com/s/glbpufagd0mpr1c/jackson-core-2.10.1.jar?dl=1", "jackson-core-2.10.1.jar", 341);
                addRequiredLibrary("https://www.dropbox.com/s/djfkcwgily1xqah/jackson-databind-2.10.1.jar?dl=1", "jackson-databind-2.10.1.jar", 1371);
                addRequiredLibrary("https://www.dropbox.com/s/dkg097yp0sm1d6l/jna-4.4.0.jar?dl=1", "jna-4.4.0.jar", 1066);
                addRequiredLibrary("https://www.dropbox.com/s/a9fil1c2z6fkzav/jsr305-3.0.2.jar?dl=1", "jsr305-3.0.2.jar", 20);
                addRequiredLibrary("https://www.dropbox.com/s/1kcxeldni1vr1il/nv-websocket-client-2.9.jar?dl=1", "nv-websocket-client-2.9.jar", 121);
                addRequiredLibrary("https://www.dropbox.com/s/y3oztlbymtx9ldw/okhttp-3.13.0.jar?dl=1", "okhttp-3.13.0.jar", 405);
                addRequiredLibrary("https://www.dropbox.com/s/hom0yvn6htky8nn/okio-1.17.2.jar?dl=1", "okio-1.17.2.jar", 90);
                addRequiredLibrary("https://www.dropbox.com/s/cv7wico9ry711a1/opus-java-api-1.0.4.jar?dl=1", "opus-java-api-1.0.4.jar", 11);
                addRequiredLibrary("https://www.dropbox.com/s/lmlh95nonmfmkx5/opus-java-natives-1.0.4.jar?dl=1", "opus-java-natives-1.0.4.jar", 2228);
                addRequiredLibrary("https://www.dropbox.com/s/1uguzf5hpqzo0qn/slf4j-api-1.7.25.jar?dl=1", "slf4j-api-1.7.25.jar", 41);
                addRequiredLibrary("https://www.dropbox.com/s/ho0vh24y9cizt9x/trove4j-3.0.3.jar?dl=1", "trove4j-3.0.3.jar", 2465);
            }
            for(String[] lib : requiredLibraries.keySet()){
                if(!new File(getLibraryRoot()+"/"+lib[1]).exists()){
                    downloadSize+=requiredLibraries.get(lib);
                }
            }
            total = requiredLibraries.size();
            File[] requiredLibs = new File[requiredLibraries.size()];
            if(hasAWT){
                javax.swing.JFrame frame;
                javax.swing.JProgressBar bar;
                frame = new javax.swing.JFrame("Download Progress");
                bar = new javax.swing.JProgressBar(0, total);
                frame.add(bar);
                frame.setSize(300, 70);
                bar.setBounds(0, 0, 300, 70);
                if(downloadSize>0){
                    frame.setVisible(true);
                }
                int n = 0;
                System.out.println("Downloading libraries...");
                for(String[] lib : requiredLibraries.keySet()){
                    String url = lib[0];
                    String filename = lib[1];
                    requiredLibs[n] = downloadFile(url, new File(getLibraryRoot()+"/"+filename));
                    bar.setValue(current);
                    n++;
                }
                System.out.println("Finished downloading libraries");
                frame.dispose();
            }else{//duplicated code, oh well
                int n = 0;
                System.out.println("Downloading libraries...");
                for(String[] lib : requiredLibraries.keySet()){
                    String url = lib[0];
                    String filename = lib[1];
                    requiredLibs[n] = downloadFile(url, new File(getLibraryRoot()+"/"+filename));
                    System.out.println("Downloading... "+Math.round(100d*current/total)+"% ("+current+"/"+total+")");
                    n++;
                }
                System.out.println("Finished downloading libraries");
            }
            String[] additionalClasspathElements = new String[requiredLibs.length+4];
            for(int i = 0; i<requiredLibs.length; i++){
                if(requiredLibs[i]==null){
                    if(hasAWT){
                        javax.swing.JOptionPane.showMessageDialog(null, "Failed to download dependencies!\n"+applicationName+" will now exit.", "Exit", javax.swing.JOptionPane.OK_OPTION);
                    }else{
                        System.err.println("Failed to download dependencies!");//TODO yes, but WHAT dependencies?
                    }
                    System.exit(0);
                }
                additionalClasspathElements[i] = requiredLibs[i].getAbsolutePath();
            }
            theargs.add(0, "Skip Dependencies");
            final Process p = restart(new String[0], theargs.toArray(new String[theargs.size()]), additionalClasspathElements, Main.class);
            final int[] finished = {0};
            new Thread("System.out transfer"){
                public void run(){
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    try{
                        while((line=in.readLine())!=null){
                            System.out.println(line);
                        }
                    }catch(IOException ex){
                        throw new RuntimeException(ex);
                    }
                    finished[0]++;
                    if(finished[0]>1){
                        System.exit(0);
                    }
                }
            }.start();
            new Thread("System.err transfer"){
                public void run(){
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String line;
                    try{
                        while((line=in.readLine())!=null){
                            System.err.println(line);
                        }
                    }catch(IOException ex){
                        throw new RuntimeException(ex);
                    }
                    finished[0]++;
                    if(finished[0]>1){
                        System.exit(0);
                    }
                }
            }.start();
            Thread inTransfer = new Thread("System.in transfer"){
                public void run(){
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    PrintWriter out = new PrintWriter(p.getOutputStream());
                    String line;
                    try{
                        while((line=in.readLine())!=null){
                            out.println(line);
                            out.flush();
                        }
                    }catch(IOException ex){
                        throw new RuntimeException(ex);
                    }
                }
            };
            inTransfer.setDaemon(true);
            inTransfer.start();
            return null;
        }
        theargs.remove(0);
        return theargs.toArray(new String[theargs.size()]);
    }
    private static File downloadFile(String link, File destinationFile){
        current++;
        if(destinationFile.exists()||link==null){
            return destinationFile;
        }
        System.out.println("Downloading "+destinationFile.getName()+"...");
        if(destinationFile.getParentFile()!=null)destinationFile.getParentFile().mkdirs();
        try {
            URL url = new URL(link);
            int fileSize;
            URLConnection connection = url.openConnection();
            connection.setDefaultUseCaches(false);
            if ((connection instanceof HttpURLConnection)) {
                ((HttpURLConnection)connection).setRequestMethod("HEAD");
                int code = ((HttpURLConnection)connection).getResponseCode();
                if (code / 100 == 3) {
                    return null;
                }
            }
            fileSize = connection.getContentLength();
            byte[] buffer = new byte[65535];
            int unsuccessfulAttempts = 0;
            int maxUnsuccessfulAttempts = 3;
            boolean downloadFile = true;
            while (downloadFile) {
                downloadFile = false;
                URLConnection urlconnection = url.openConnection();
                if ((urlconnection instanceof HttpURLConnection)) {
                    urlconnection.setRequestProperty("Cache-Control", "no-cache");
                    urlconnection.connect();
                }
                String targetFile = destinationFile.getName();
                FileOutputStream fos;
                int downloadedFileSize;
                try (InputStream inputstream=getRemoteInputStream(targetFile, urlconnection)) {
                    fos=new FileOutputStream(destinationFile);
                    downloadedFileSize=0;
                    int read;
                    while ((read = inputstream.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                        downloadedFileSize += read;
                    }
                }
                fos.close();
                if (((urlconnection instanceof HttpURLConnection)) && 
                    ((downloadedFileSize != fileSize) && (fileSize > 0))){
                    unsuccessfulAttempts++;
                    if (unsuccessfulAttempts < maxUnsuccessfulAttempts){
                        downloadFile = true;
                    }else{
                        throw new Exception("failed to download "+targetFile);
                    }
                }
            }
            return destinationFile;
        }catch (Exception ex){
            return null;
        }
    }
    private static File forceDownloadFile(String link, File destinationFile){
        if(destinationFile.exists())destinationFile.delete();
        return downloadFile(link, destinationFile);
    }
    public static InputStream getRemoteInputStream(String currentFile, final URLConnection urlconnection) throws Exception {
        final InputStream[] is = new InputStream[1];
        for (int j = 0; (j < 3) && (is[0] == null); j++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        is[0] = urlconnection.getInputStream();
                    }catch (IOException localIOException){}
                }
            };
            t.setName("FileDownloadStreamThread");
            t.start();
            int iterationCount = 0;
            while ((is[0] == null) && (iterationCount++ < 5)){
                try {
                    t.join(1000L);
                } catch (InterruptedException localInterruptedException) {
                }
            }
            if (is[0] != null){
                continue;
            }
            try {
                t.interrupt();
                t.join();
            } catch (InterruptedException localInterruptedException1) {
            }
        }
        if (is[0] == null) {
            throw new Exception("Unable to download "+currentFile);
        }
        return is[0];
    }
    private static void delete(File file){
        if(!file.exists()){
            return;
        }
        if(file.isDirectory()){
            File[] files = file.listFiles();
            if(files!=null){
                for(File afile : files){
                    delete(afile);
                }
            }
            file.delete();
        }else{
            file.delete();
        }
    }
    /**
     * Restarts the program.  This method will return normally if the program was properly restarted or throw an exception if it could not be restarted.
     * @param vmArgs The VM arguments for the new instance
     * @param applicationArgs The application arguments for the new instance
     * @param additionalFiles Any additional files to include in the classpath
     * @param mainClass The program's main class.  The new instance is started with this as the specified main class.
     * @throws URISyntaxException if a URI cannot be created to obtain the filepath to the jarfile
     * @throws IOException if Java is not in the PATH environment variable
     */
    public static Process restart(String[] vmArgs, String[] applicationArgs, String[] additionalFiles, Class<?> mainClass) throws URISyntaxException, IOException{
        ArrayList<String> params = new ArrayList<>();
        params.add("java");
        if(os==OS_MACOS)params.add("-XstartOnFirstThread");
        params.addAll(Arrays.asList(vmArgs));
        params.add("-classpath");
        String filepath = mainClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        String separator = System.getProperty("path.separator");
        for(String str : additionalFiles){
            filepath+=separator+str;
        }
        params.add(filepath);
        params.add(mainClass.getName());
        params.addAll(Arrays.asList(applicationArgs));
        ProcessBuilder builder = new ProcessBuilder(params);
        return builder.start();
    }
    /**
     * Starts the requested Java application the program.  This method will return normally if the program was properly restarted or throw an exception if it could not be restarted.
     * @param vmArgs The VM arguments for the new instance
     * @param applicationArgs The application arguments for the new instance
     * @param file The program file.  The new instance is started with this as the specified main class.
     * @throws URISyntaxException if a URI cannot be created to obtain the filepath to the jarfile
     * @throws IOException if Java is not in the PATH environment variable
     */
    public static Process startJava(String[] vmArgs, String[] applicationArgs, File file) throws URISyntaxException, IOException{
        ArrayList<String> params = new ArrayList<>();
        params.add("java");
        if(os==OS_MACOS)params.add("-XstartOnFirstThread");
        params.addAll(Arrays.asList(vmArgs));
        params.add("-jar");
        params.add(file.getAbsolutePath());
        params.addAll(Arrays.asList(applicationArgs));
        ProcessBuilder builder = new ProcessBuilder(params);
        return builder.start();
    }
    public static void setLookAndFeel(){
        if(!hasAWT)return;
        System.out.println("Setting Swing look and feel...");
        String lookAndFeel = null;
        for(javax.swing.UIManager.LookAndFeelInfo info:javax.swing.UIManager.getInstalledLookAndFeels()){
            if("Nimbus".equals(info.getName())){
                lookAndFeel = info.getClassName();
                break;
            }
        }
        for(javax.swing.UIManager.LookAndFeelInfo info:javax.swing.UIManager.getInstalledLookAndFeels()){
            if("Windows".equals(info.getName())){
                lookAndFeel = info.getClassName();
                break;
            }
        }
        try{
            javax.swing.UIManager.setLookAndFeel(lookAndFeel);
        }catch(ClassNotFoundException|InstantiationException|IllegalAccessException|javax.swing.UnsupportedLookAndFeelException ex){}
    }
}