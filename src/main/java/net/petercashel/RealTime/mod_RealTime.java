package net.petercashel.RealTime;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.petercashel.RealTime.RealWeather.RealWeatherWorld;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;


@Mod(modid = "mod_realtime", name = "RealTime")
public class mod_RealTime {
	
	@Instance("mod_RealTime")
	public static mod_RealTime instance;

	public static FMLEventChannel Channel;
	public static FMLEventChannel ChannelConnect;
	public static FMLEventChannel ChannelLogin;
	public static FMLEventChannel ChannelWeather;


	@SidedProxy(clientSide = "net.petercashel.RealTime.ClientProxy", serverSide = "net.petercashel.RealTime.CommonProxy")
	public static CommonProxy proxy;

	public static final String CATEGORY_GENERAL = "general";

	//RealTime Stuff

	public static boolean RealTimeEnabled;
	public static boolean RealWeatherEnabled = false;
	public static String tzName;
	public static int RealTimeZone = 0;
	public static int RealTimeZoneOriginal;
	public static String WeatherAPIKEY = "";
	public static String WeatherLocationCity = "";
	public static String WeatherLocationCountry = "";
	public static boolean EnforceSync = false;
	
	public static float ServerTime = 0F;
	public static float ClientTime = 3000F;
	public static boolean ClientTimeEnabled = false;

	// Used server side to not spam the client with packets from this mod.
	public static int ServerNoSpamCounter = 0;
	
	private TimeSyncCommand TimeSyncCMD;

	private MinecraftServer server;
	
	@EventHandler
	public void load(FMLInitializationEvent event) 
	{
		System.out.println("[RealTime+RealWeather] Loaded.");
		FMLLog.log("RealTime+RealWeather", Level.INFO, "Mod Has Loaded [RealTime]");
	}


	@EventHandler
	public void ServerStopped(FMLServerStoppingEvent event) 
	{
		mod_RealTime.ClientTimeEnabled = false;
		mod_RealTime.RealTimeZone = mod_RealTime.RealTimeZoneOriginal;
	}


	@EventHandler
	public void ServerStarting(FMLServerStartingEvent event) 
	{
		mod_RealTime.ChannelConnect.register(new ServerPacketHandlerConnect());
		
		server = MinecraftServer.getServer();
		ServerCommandManager commands = (ServerCommandManager) server.getCommandManager();
		TimeSyncCMD = new TimeSyncCommand(this);
		commands.registerCommand(TimeSyncCMD);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
		try {
			cfg.load();
			RealTimeEnabled = cfg.get(CATEGORY_GENERAL,"RealTimeEnabled", false).getBoolean(false);
			EnforceSync = cfg.get(CATEGORY_GENERAL,"EnforceSync", false).getBoolean(false);
			
			Calendar cal = Calendar.getInstance();
			TimeZone tz = TimeZone.getDefault();
			
			tzName = cfg.get(CATEGORY_GENERAL,"RealTime_TimeZone_Name", displayTimeZone(cal.getTimeZone())).getString();;
			
			tz = TimeZone.getTimeZone(tzName);
			cal.setTimeZone(tz);
			
			RealTimeZone = cal.getTimeZone().getRawOffset();
			
			RealTimeZoneOriginal = RealTimeZone;
			
			
			RealWeatherEnabled = cfg.get(CATEGORY_GENERAL,"RealWeatherEnabled", false).getBoolean(false);
			WeatherAPIKEY = cfg.get(CATEGORY_GENERAL,"RealWeather_APIKEY", "").getString();
			WeatherLocationCity = cfg.get(CATEGORY_GENERAL,"RealWeather_City", "").getString();
			WeatherLocationCountry = cfg.get(CATEGORY_GENERAL,"RealWeather_Country", "").getString();
			
		} catch (Exception e) {
			System.out.println("[RealTime] Error Loading Config");
		} finally {
			cfg.save();
		}
		FMLCommonHandler.instance().bus().register(new RealTimeEvents());
		RealWeatherWorld.SelfCallForLoading();

	}

	@EventHandler
	public void init(FMLInitializationEvent event){
		Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("RealTime");
		ChannelConnect = NetworkRegistry.INSTANCE.newEventDrivenChannel("RealTimeConnect");
		ChannelLogin = NetworkRegistry.INSTANCE.newEventDrivenChannel("RealTimeLogin");
		ChannelWeather = NetworkRegistry.INSTANCE.newEventDrivenChannel("RealWeather");
		proxy.load();
		RealTimeInit();

		//Channel.sendToServer(FMLProxyPacket);
		//Channel.sendTo(FMLProxyPacket, EntityPlayerMP);
	}

	public void RealTimeInit() {
		//Not Needing to do anything yet
	}

	private static String displayTimeZone(TimeZone tz) {

		long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
	    long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset()) 
	                              - TimeUnit.HOURS.toMinutes(hours);
	    // avoid -4:-30 issue
	    minutes = Math.abs(minutes);

	    String result = "";
	    if (hours > 0) {
	        result = String.format("GMT+%d:%02d", hours, minutes);
	    } else {
	        result = String.format("GMT%d:%02d", hours, minutes);
	    }

	    return result;

	}
}