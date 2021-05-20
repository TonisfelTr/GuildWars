package guildunits;

public class PlayerInvitation {
	private String Nickname;
	private int toGuildID;
	
	public PlayerInvitation(String nickName, int GuildID) {
		this.Nickname = nickName;
		this.toGuildID = GuildID;
	}
	
	public String getNickname() {
		return this.Nickname;
	}
	
	public int GetGuildId() {
		return this.toGuildID;
	}
}
