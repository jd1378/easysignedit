package com.javadmnjd.easysignedit;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.javadmnjd.easysignedit.listener.InteractListener;
import com.javadmnjd.easysignedit.listener.SignEditedPacketListener;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Plugin(id = "easysignedit", name = "Easy Sign Edit", version = "1.0",
        dependencies = {
                @Dependency(
                        id = "packetgate",
                        version = "0.1.1"
                )
        })
public class EasySignEditPlugin {

    @Inject
    private Logger logger;

    @Inject
    private PluginContainer pluginContainer;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private Path configFile;

    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    private ItemType itemType;
    private boolean sneakRequired;

    private InteractListener interactListener;
    private SignEditedPacketListener signEditedPacketListener;

    private static EasySignEditPlugin plugin;

    public EasySignEditPlugin(){
        if(plugin == null){
            plugin = this;
        } else{
            getLogger().warn("There is another instance of this plugin running, something is wrong !");
        }
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent e){
        configFile = Paths.get(configDir + "/config.conf");
        configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
        //Creates config directory, your could also create a method for this.
        if(!Files.exists(configDir)){
            try{
                Files.createDirectories(configDir);
            }catch(IOException io){
                io.printStackTrace();
            }
        }
        setup();
    }

    private void setup(){
        if(Files.notExists(configFile)){
            pluginContainer.getAsset("default.conf").ifPresent(asset -> {
                try{
                    asset.copyToFile(configFile);
                }catch(IOException e){
                    e.printStackTrace();
                }
            });
        }
        load();
    }

    private void load(){
        try{
            configNode = configLoader.load();
            try {
                sneakRequired = configNode.getNode("general", "sneak-required").getBoolean(true);
                itemType = configNode.getNode("general", "tool").getValue(TypeToken.of(ItemType.class));
            } catch (ObjectMappingException omex) {
                getLogger().error("Could not detect tool, using AIR instead.");
                itemType = ItemTypes.AIR;
            }
        }catch(IOException e){
            e.printStackTrace();
            getLogger().warn("Could not load config file, using AIR as edit tool (requires sneaking)");
            // cannot load conf
            itemType = ItemTypes.AIR;
            sneakRequired = true;
        }
    }

    public void save(){
        try{
            configLoader.save(configNode);
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    @Listener
    public void reload(GameReloadEvent event) {
        load();
        if(interactListener != null){
            Sponge.getEventManager().unregisterListeners(interactListener);
        }
        interactListener = new InteractListener(this);
        Sponge.getEventManager().registerListeners(this, interactListener);
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        if(interactListener != null){
            Sponge.getEventManager().unregisterListeners(interactListener);
        }
        interactListener = new InteractListener(this);
        if(signEditedPacketListener == null) {
            signEditedPacketListener = new SignEditedPacketListener();
        }

        Sponge.getEventManager().registerListeners(this, interactListener);
        getLogger().info("Easy Sign Edit loaded.");
    }

    public static EasySignEditPlugin getPlugin(){
        return plugin;
    }

    private Logger getLogger() {
        return logger;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public boolean isSneakRequired() {
        return sneakRequired;
    }

}
