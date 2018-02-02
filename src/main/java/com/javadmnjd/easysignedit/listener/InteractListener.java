package com.javadmnjd.easysignedit.listener;

import com.flowpowered.math.vector.Vector3i;
import com.javadmnjd.easysignedit.EasySignEditPlugin;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketConnection;
import eu.crushedpixel.sponge.packetgate.api.registry.PacketGate;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import net.minecraft.network.play.server.SPacketSignEditorOpen;

import java.util.Optional;

public class InteractListener {

    private EasySignEditPlugin plugin;

    public InteractListener(EasySignEditPlugin plugin){
        this.plugin = plugin;
    }

    @Listener
    public void onRightClick(InteractBlockEvent event){
        if (event.getTargetBlock().getState().getType().equals(BlockTypes.WALL_SIGN) || event.getTargetBlock().getState().getType().equals(BlockTypes.STANDING_SIGN )){
            Optional<Player> player = event.getCause().first(Player.class);
            if (player.isPresent()){
                if(player.get().hasPermission("easysignedit.use")){
                    if(plugin.isSneakRequired() && !player.get().get(Keys.IS_SNEAKING).orElse(false)
                                                        .equals(plugin.isSneakRequired())){
                        return;
                    }else if (player.get().getItemInHand(HandTypes.MAIN_HAND)
                            .orElse(ItemStack.of(ItemTypes.AIR,1))
                            .equalTo(ItemStack.of(plugin.getItemType(),1))){
                        Sponge.getServiceManager().provide(PacketGate.class).ifPresent(packetGate ->
                                    packetGate.connectionByPlayer(player.get()).ifPresent(connection ->{
                                        Vector3i vec = event.getTargetBlock().getPosition();
                                        connection.sendPacket(new SPacketSignEditorOpen(new BlockPos(vec.getX(),vec.getY(),vec.getZ())));
                                    }));
                    }
                }
            }
        }
    }
}
