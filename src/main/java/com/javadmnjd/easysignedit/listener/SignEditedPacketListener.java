package com.javadmnjd.easysignedit.listener;

import eu.crushedpixel.sponge.packetgate.api.event.PacketEvent;
import eu.crushedpixel.sponge.packetgate.api.listener.PacketListener;
import eu.crushedpixel.sponge.packetgate.api.listener.PacketListenerAdapter;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketConnection;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketGate;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.text.Text;

public class SignEditedPacketListener extends PacketListenerAdapter {

    public SignEditedPacketListener(){
        Sponge.getServiceManager().provide(PacketGate.class).ifPresent(packetGate -> packetGate.registerListener(
                this,
                ListenerPriority.DEFAULT,
                CPacketUpdateSign.class));
    }

    @Override
    public void onPacketRead(PacketEvent event, PacketConnection connection) {
        if (!(event.getPacket() instanceof CPacketUpdateSign)) return;
        CPacketUpdateSign packet = (CPacketUpdateSign)event.getPacket();
        Sponge.getServer().getPlayer(connection.getPlayerUUID()).ifPresent(player -> {
            if (player.hasPermission("easysignedit.use")){
                BlockPos bpos = packet.getPosition();
                player.getWorld()
                        .getTileEntity(bpos.getX(), bpos.getY() ,bpos.getZ())
                        .ifPresent(signTileEntity -> {
                            if (signTileEntity instanceof Sign){
                                SignData signData = ((Sign) signTileEntity).getSignData();
                                String[] lines = packet.getLines();
                                for (int i = 0; i < 4; i++){
                                    signData.setElement(i, Text.of(replaceColor(lines[i])));
                                }
                                signTileEntity.offer(signData);
                            }
                            event.setCancelled(true);
                        });

            }
        });
    }

    // credit: FabioZumbi12
    private static String replaceColor(String str){
        return str.replaceAll("(&([a-fk-or0-9]))", "\u00A7$2");
    }

}
