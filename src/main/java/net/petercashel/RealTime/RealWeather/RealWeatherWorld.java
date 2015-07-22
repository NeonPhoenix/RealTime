
package net.petercashel.RealTime.RealWeather;

import java.util.Random;

import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;
import net.petercashel.RealTime.mod_RealTime;

public class RealWeatherWorld {

	final static boolean isDebugEnvironment = Boolean.parseBoolean(System.getenv("JavaDebugEnvironment"));
	
	public static void updateWeatherBody(WorldProvider provider, WorldInfo worldInfo, Random rand, float thunderingStrength, float rainingStrength, float prevThunderingStrength, float prevRainingStrength, boolean isRemote) {

		if (!mod_RealTime.RealWeatherEnabled) {
			VanillaupdateWeatherBody(provider, worldInfo, rand, thunderingStrength, rainingStrength, prevThunderingStrength, prevRainingStrength, isRemote);
		} else {
			// WorldProvider provider, WorldInfo worldInfo,
			// Random rand, float thunderingStrength, float rainingStrength,
			// float prevThunderingStrength, float prevRainingStrength, boolean isRemote


			if (!provider.hasNoSky)
			{
				if (provider.dimensionId == 0) {

					if (!isRemote) //Server
					{
						if (RealWeather.threadStartedServer == false) {
							RealWeather.threadStartedServer = true;
							RealWeather.createServerThread();
						}
						
						if (RealWeather.needsUpdate == true) {
							worldInfo.setRaining(RealWeather.raining);
							provider.worldObj.rainingStrength = RealWeather.rainStr;
							provider.worldObj.prevRainingStrength = RealWeather.rainStrPrev;
							worldInfo.setRainTime(RealWeather.rainTime);
							worldInfo.setThundering(RealWeather.thunder);
							provider.worldObj.thunderingStrength = RealWeather.thundStr;
							provider.worldObj.prevThunderingStrength = RealWeather.thundStrPrev;
							worldInfo.setThunderTime(RealWeather.thundTime);

							RealWeather.needsUpdate = false;
						}


					} else {
						//Client
						worldInfo.setRaining(RealWeather.raining);
						provider.worldObj.rainingStrength = RealWeather.rainStr;
						provider.worldObj.prevRainingStrength = RealWeather.rainStrPrev;
						worldInfo.setRainTime(RealWeather.rainTime);
						worldInfo.setThundering(RealWeather.thunder);
						provider.worldObj.thunderingStrength = RealWeather.thundStr;
						provider.worldObj.prevThunderingStrength = RealWeather.thundStrPrev;
						worldInfo.setThunderTime(RealWeather.thundTime);
						
						//Check if data was recived and call the updater
						if (RealWeather.needsUpdateClient) {
							RealWeather.needsUpdateClient = false;
							RealWeather.doClientUpdate();
							if (isDebugEnvironment) System.out.println("Client is performing weather update");
						}

					}
				} else {
					VanillaupdateWeatherBody(provider, worldInfo, rand, thunderingStrength, rainingStrength, prevThunderingStrength, prevRainingStrength, isRemote);
				}
			}
		}
	}

	public static void VanillaupdateWeatherBody(WorldProvider provider, WorldInfo worldInfo, Random rand, float thunderingStrength, float rainingStrength, float prevThunderingStrength, float prevRainingStrength, boolean isRemote) {
		//Vanilla Weather Code
		if (!provider.hasNoSky)
		{
			if (!isRemote)
			{
				int i = worldInfo.getThunderTime();

				if (i <= 0)
				{
					if (worldInfo.isThundering())
					{
						worldInfo.setThunderTime(rand.nextInt(12000) + 3600);
					}
					else
					{
						worldInfo.setThunderTime(rand.nextInt(168000) + 12000);
					}
				}
				else
				{
					--i;
					worldInfo.setThunderTime(i);

					if (i <= 0)
					{
						worldInfo.setThundering(!worldInfo.isThundering());
					}
				}

				prevThunderingStrength = thunderingStrength;

				if (worldInfo.isThundering())
				{
					thunderingStrength = (float)((double)thunderingStrength + 0.01D);
				}
				else
				{
					thunderingStrength = (float)((double)thunderingStrength - 0.01D);
				}

				thunderingStrength = MathHelper.clamp_float(thunderingStrength, 0.0F, 1.0F);
				int j = worldInfo.getRainTime();

				if (j <= 0)
				{
					if (worldInfo.isRaining())
					{
						worldInfo.setRainTime(rand.nextInt(12000) + 12000);
					}
					else
					{
						worldInfo.setRainTime(rand.nextInt(168000) + 12000);
					}
				}
				else
				{
					--j;
					worldInfo.setRainTime(j);

					if (j <= 0)
					{
						worldInfo.setRaining(!worldInfo.isRaining());
					}
				}

				prevRainingStrength = rainingStrength;

				if (worldInfo.isRaining())
				{
					rainingStrength = (float)((double)rainingStrength + 0.01D);
				}
				else
				{
					rainingStrength = (float)((double)rainingStrength - 0.01D);
				}

				rainingStrength = MathHelper.clamp_float(rainingStrength, 0.0F, 1.0F);
			}
		}
	}

	public static void SelfCallForLoading() {
		//Makes sure this class exists before world loads.
	}
}
