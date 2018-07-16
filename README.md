[![Spigot Link](http://www.livecar.net/random/destinations_logo.png)](https://www.spigotmc.org/resources/nunpcdestinations-create-living-npcs-1-8-3-1-11.13863/) 

## About the plugin:
   This one provides mechanics to give your NPC's some creature comforts. Rather than having them sit out in the cold dark night's you can now let them go home and sit next to a nice pixelated fireplace. Think how excited they will be now!  

## Features
-Automated path finding system that adapts to the environment around your NPC's  
-All configuration is stored on the NPC itself, no need to edit any config files  
-Ability to provide multiple locations for an NPC to walk to, based on time of day.  
-Ability to let them just walk on anything, or define their allowed path surfaces.  
-Random movement based on a location.  
-Per location inventory customization.  
-Ability to let the NPC choose a location based on the current weather.  
-Random walking configurations per location defined  
-Use commands to set an NPC's current location (With duration's)  
-Set a different NPC skin per location  
-Detailed permission's to control each command's access.  
-Ability to allow a user access to edit any or only owned NPC's.  



## Required Plugins:
*Citizens2 (NPC base mechanics)  
*Optional Supported Plugins:  
*Particle API     (Visual path debugging)  
*JobsReborn    (NPC locations can be toggled based on users in jobs)  
*LightAPI          (Equip an NPC with a torch (Normal or redstone) and it will light the area around the NPC at night.  
*BetonQuest     (Provides conditions, and events for controlling NPCs from BetonQuest)  
                              [Example BetonQuest Package]  

## Quick Tutorial:
1. Download the latest version of the plugin and place it into your plugins folder.  
2. Start the server  
3. Create an NPC and assign it the trait 'npcdestinations'  
4. Go to the location you want your NPC to sit at first.  
5. type /npcdest addlocation {time of day / sunset / or sunrise}  
6. Go to the second location you want the NPC to visit  
7. type /npcdest addlocation {time of day / sunset / or sunrise}  
8. type /waypoint provider npcdestinations  
9. Change the time of day, they should change locations.  
