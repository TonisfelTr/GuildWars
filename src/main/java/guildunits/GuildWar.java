package guildunits;

public class GuildWar {
	private int FromID;
	private int ToID;
	
	public GuildWar(int FromID, int ToID) {
		this.FromID = FromID;
		this.ToID = ToID;
	}
	
	public int getFrom() {
		return this.FromID;
	}
	
	public int getTo() {
		return this.ToID;
	}
}
