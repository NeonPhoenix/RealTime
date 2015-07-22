package net.petercashel.RealTime;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.petercashel.RealTime.RealWeather.ClientWeatherPacketHandler;
import cpw.mods.fml.common.network.IGuiHandler;

public class ClientProxy extends CommonProxy{

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}
	
	public void load(){
		mod_RealTime.Channel.register(new ClientPacketHandler());
		mod_RealTime.ChannelLogin.register(new ClientPacketHandlerLogin());
		mod_RealTime.ChannelWeather.register(new ClientWeatherPacketHandler());
		
		//ExampleMod.Channel.register(new ServerPacketHandler());
	}
}
