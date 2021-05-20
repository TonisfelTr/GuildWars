package guildunits;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SelectedPoint {
	private Location<World> firstPoint = null;
	private Location<World> secondPoint = null;
	private Location<World> teleportPoint = null;
	
	public Location<World> getFirstPoint(){
		return firstPoint;
	}
	
	public Location<World> getSecondPoint(){
		return secondPoint;
	}

	public Location<World> getTeleportPoint(){
		return teleportPoint;
	}
	
	public void setFirstPoint(Location<World> loc) {
		firstPoint = loc;
	}
	
	public void setSecondPoint(Location<World> loc) {
		secondPoint = loc;
	}
	
	public void setTeleportPoint(Location<World> loc) {
		teleportPoint = loc;
	}
}
