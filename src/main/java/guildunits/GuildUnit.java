package guildunits;

public class GuildUnit {
	private String Name;
	private int ParentGI;
	private boolean IsLeader;
	
	public String GetNickname() {
		 return this.Name;
	}
	
	public int GetParent() {
		return this.ParentGI;
	}
	
	public boolean IsLeader() {
		return this.IsLeader;
	}
	
	public void SetLeader(boolean is) {
		this.IsLeader = is;
	}
	
	public GuildUnit(String nickname, int ParentGI, boolean IsLeader) {
		this.Name = nickname;
		this.ParentGI = ParentGI;
		this.IsLeader = IsLeader;
	}
}
