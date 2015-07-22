package net.petercashel.RealTime.RealWeather;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

public class ClientWeatherPacketHandler {

	@SubscribeEvent
	public void onClientPacket(ClientCustomPacketEvent event) {
		ByteBufInputStream bbis = new ByteBufInputStream(event.packet.payload());

		int length = 0;
		String json = "";
		byte[] bytes = null;
		try {
			length = bbis.readInt();
			bytes = new byte[length];
			bbis.read(bytes, 0, length);
			json = new String(bytes, StandardCharsets.US_ASCII);
			RealWeather.weatherJSON = json;
			RealWeather.needsUpdateClient = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			bbis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
