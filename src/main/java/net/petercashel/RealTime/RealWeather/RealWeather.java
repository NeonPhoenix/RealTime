package net.petercashel.RealTime.RealWeather;

import static io.netty.buffer.Unpooled.buffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import net.petercashel.RealTime.mod_RealTime;

// Holds weather data for RealTime and RealWeather
public class RealWeather {

	//Stores the last received weather data
	public static String weatherJSON = "";
	final static boolean isDebugEnvironment = Boolean.parseBoolean(System.getenv("JavaDebugEnvironment"));
	
	public static boolean needsUpdate = false;
	public static boolean needsUpdateClient = false;

	public static boolean threadStartedServer = false;


	//Weather Values
	public static moonPhase moon = moonPhase.Full_Moon;

	public static boolean raining = false;
	public static boolean thunder = false;

	public static float rainStr = 0.0f;
	public static float rainStrPrev = 0.0f;
	public static float thundStr = 0.0f;
	public static float thundStrPrev = 0.0f;

	public static int rainTime = 0;
	public static int thundTime = 0;	

	//Rains
	public static float norain = 0.0f;
	public static float vrylightrain = 0.2f;
	public static float lightrain = 0.35f;
	public static float mediumrain = 0.8f;
	public static float heavyrain = 1.2f;
	public static float stormrain = 2.0f;


	public static enum moonPhase {
		Full_Moon(0), Waxing_Gibbus(1), First_Quarter(2), Waxing_Cresent(3), New_Moon(4), Waning_Cresent(5), Last_Quarter(6), Waning_Gibbus(7);

		private int value;
		private moonPhase(int value) {
			this.value = value;
		}
	}




	public static void processWeatherJSONServer() {
		String s = weatherJSON;
		processWeatherJSONClient(s);
		if (isDebugEnvironment) System.out.println(s);
		//Send data to client
		ByteBuf bb = buffer(8192);
		bb.writeInt(s.getBytes(StandardCharsets.US_ASCII).length);
		bb.writeBytes(s.getBytes(StandardCharsets.US_ASCII));
		FMLProxyPacket pkt = new FMLProxyPacket(bb, "RealWeather");
		mod_RealTime.ChannelWeather.sendToAll(pkt);
		needsUpdate = true;	
	}

	public static void processWeatherJSONClient(String s) {
		JsonObject gson = new JsonObject();
		gson = new GsonBuilder().create().fromJson(s, JsonObject.class);
		
		String moon = gson.getAsJsonObject().get("MoonPhase").getAsString();
		String weather = gson.getAsJsonObject().get("Weather").getAsString();
		
		// MOON LOGIC!
		if (moon.contains("Full")) {
			RealWeather.moon = moonPhase.Full_Moon;
		} else if (moon.contains("Blue")) {
			RealWeather.moon = moonPhase.Full_Moon;
		} else if (moon.contains("New")) {
			RealWeather.moon = moonPhase.New_Moon;
		} else if (moon.contains("First")) {
			RealWeather.moon = moonPhase.First_Quarter;
		} else if (moon.contains("Last")) {
			RealWeather.moon = moonPhase.Last_Quarter;
		} else {
			if (moon.contains("Waxing")) {
				if (moon.contains("Crescent")) {
					RealWeather.moon = moonPhase.Waxing_Cresent;
				} else if (moon.contains("Gibbus")) {
					RealWeather.moon = moonPhase.Waxing_Gibbus;
				}
			} else if (moon.contains("Waning")) {
				if (moon.contains("Crescent")) {
					RealWeather.moon = moonPhase.Waning_Cresent;
				} else if (moon.contains("Gibbus")) {
					RealWeather.moon = moonPhase.Waning_Gibbus;
				}
			}
		}
		
		//Weather Logic //weather //
		if (weather.contains("Rain") || weather.contains("Showers")) {
			if (weather.contains("Chance")) {
				RealWeather.raining = false;
				RealWeather.rainStr = RealWeather.norain;
			} else {
				if (weather.contains("Light")) {
					RealWeather.raining = true;
					RealWeather.rainStr = RealWeather.lightrain;
				} 
				else if (weather.contains("Medium")) {
					RealWeather.raining = true;
					RealWeather.rainStr = RealWeather.mediumrain;
				} 
				else if (weather.contains("Heavy")) {
					RealWeather.raining = true;
					RealWeather.rainStr = RealWeather.heavyrain;
				} else {
					RealWeather.raining = true;
					RealWeather.rainStr = RealWeather.mediumrain;
				}
			}
		} else {
			RealWeather.raining = false;
			RealWeather.rainStr = RealWeather.norain;
		}
		
		if (weather.contains("Storm") || weather.contains("storm")) {
			if (weather.contains("Chance")) {
			} else {
				if (weather.contains("Light")) {
					RealWeather.raining = true;
					RealWeather.rainStr = RealWeather.lightrain;
				} 
				else if (weather.contains("Medium")) {
					RealWeather.raining = true;
					RealWeather.rainStr = RealWeather.mediumrain;
				} 
				else if (weather.contains("Heavy")) {
					RealWeather.raining = true;
					RealWeather.rainStr = RealWeather.heavyrain;
				} else {
					RealWeather.raining = true;
					RealWeather.rainStr = RealWeather.heavyrain;
				}
			}
		}
		
		if (weather.contains("Thunder") || weather.contains("thunder")) {
			if (weather.contains("Chance")) {
			} else {
				if (weather.contains("Light")) {
					RealWeather.thunder = true;
					RealWeather.thundStr = 0.25f;
				} 
				else if (weather.contains("Medium")) {
					RealWeather.thunder = true;
					RealWeather.thundStr = 0.5f;
				} 
				else if (weather.contains("Heavy")) {
					RealWeather.thunder = true;
					RealWeather.thundStr = 1.0f;
				} else {
					RealWeather.thunder = true;
					RealWeather.thundStr = 0.5f;
				}
			}
		} else {
			RealWeather.thunder = false;
			RealWeather.thundStr = 0f;
		}
		
	}

	//Called to create a new thread to run the processing.
	public static void doClientUpdate() {
		Thread t = new Thread(new ClientWeatherUpdateThread());
		t.setDaemon(true);//success is here now
		t.start();

	}

	public static void createServerThread() {
		Thread t = new Thread(new ServerWeatherThread());
		t.setDaemon(true);//success is here now
		t.start();
		
	}
	
	static class ServerWeatherThread implements Runnable {
		@Override
		public void run() {
			if (isDebugEnvironment) System.out.println("Server is performing weather update");
			//Get weather data
			URL urlWeather = null;
			try {
				urlWeather = new URL(
						"http://api.wunderground.com/api/"
						+ mod_RealTime.WeatherAPIKEY
						+ "/conditions/q/"
						+ (mod_RealTime.WeatherLocationCountry).replace(" ", "%20")
						+ "/"
						+ (mod_RealTime.WeatherLocationCity).replace(" ", "%20")
						+ ".json");
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			String jsonWeather = "";
			try {
				BufferedReader reader = new BufferedReader (new InputStreamReader(urlWeather.openStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					jsonWeather += line;
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			JsonObject weathergson = new JsonObject();
			weathergson = new GsonBuilder().create().fromJson(jsonWeather, JsonObject.class);
			
			//Get Moon Data
			URL urlMoon = null;
			try {
				urlMoon = new URL(
						"http://api.wunderground.com/api/"
						+ mod_RealTime.WeatherAPIKEY
						+ "/astronomy/q/"
						+ (mod_RealTime.WeatherLocationCountry).replace(" ", "%20")
						+ "/"
						+ (mod_RealTime.WeatherLocationCity).replace(" ", "%20")
						+ ".json");
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			String jsonMoon = "";
			try {
				BufferedReader reader = new BufferedReader (new InputStreamReader(urlMoon.openStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					jsonMoon += line;
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			JsonObject moongson = new JsonObject();
			moongson = new GsonBuilder().create().fromJson(jsonMoon, JsonObject.class);
			
			JsonObject datagson = new JsonObject();
			datagson.addProperty("MoonPhase", moongson.getAsJsonObject().getAsJsonObject("moon_phase").get("phaseofMoon").getAsString());
			datagson.addProperty("Weather", weathergson.getAsJsonObject().getAsJsonObject("current_observation").get("weather").getAsString());
			datagson.addProperty("Weathericon", weathergson.getAsJsonObject().getAsJsonObject("current_observation").get("icon").getAsString());
			
			RealWeather.weatherJSON = datagson.toString();
			RealWeather.processWeatherJSONServer();
			try {
				Thread.sleep(1000 * 60 * 15);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}				
	}
	
	static class ClientWeatherUpdateThread implements Runnable {

		@Override
		public void run() {
			if (isDebugEnvironment) System.out.println("Client weather data: " + RealWeather.weatherJSON);
			RealWeather.processWeatherJSONClient(RealWeather.weatherJSON);
		}
		
	}
}
