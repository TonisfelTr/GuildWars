package guildunits;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class GuildHall {
	
	private int MasterGuildID;
	private int GuildHallPrice;
	private String HallName;
	private Location<World> FirstPos = null;
	private Location<World> SecondPos = null;
	private Location<World> TeleportPos = null;
	
	public boolean CheckExistance(double x1, double x2, double point) {
		if (x2 < x1) {
			double t = x2;
			x2 =  x1;
			x1 = t;
		}
		return point >= x1 && point <= x2;
	}
	
	public GuildHall(Integer MasterGuildId, String HallName,Integer Price, Location<World> fPos, Location<World> sPos, Location<World> tPos) {
		this.HallName = HallName;
		this.MasterGuildID = MasterGuildId;
		this.GuildHallPrice = Price;
		this.FirstPos = fPos;
		this.SecondPos = sPos;
		this.TeleportPos = tPos;
	}
	
	public Location<World> getFirstPos(){
		return this.FirstPos;
	}
	
	public Location<World> getSecondPos(){
		return this.SecondPos;
	}
	
	public Location<World> getTeleportPos(){
		return this.TeleportPos;
	}
	
	public int getMasterGuild() {
		return this.MasterGuildID;
	}
	
	public int getPrice() {
		return this.GuildHallPrice;
	}
	
	public String getName() {
		return this.HallName;
	}
	
	public void setGuildMaster(int id) {
		this.MasterGuildID = id;
	}
}
