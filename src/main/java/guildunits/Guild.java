package guildunits;

import org.spongepowered.api.Sponge;

import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Guild {
	private int ID;
	private String name;
	private ArrayList<GuildUnit> members = new ArrayList<GuildUnit>();
	private ArrayList<GuildWar> wars = new ArrayList<GuildWar>();
	private int CoinsCount = 0;
	
	public Guild(String GuildName, int ID, int CoinsCount) {
		this.name = GuildName;			
		this.ID = ID;
		this.CoinsCount = CoinsCount; 
	}
	
	public int GetId() {
		return this.ID;
	}
	
	public void SayToChat(String strToSay) {
		for (int i = 0; i < this.members.size(); i++) {
			if (Sponge.getServer().getPlayer(this.members.get(i).GetNickname()).isPresent())
				Sponge.getServer().getPlayer(this.members.get(i).GetNickname()).get().sendMessage(Text.of(TextColors.GREEN, "[Guild] ", strToSay));
		}
		
	}
	
	public void SayToChat(String nickname, String message) {
		ArrayList<String> members = this.GetMembersOfGuild();
		for (int i = 0; i < this.members.size(); i++) {
			if (Sponge.getServer().getPlayer(members.get(i)).isPresent())
				Sponge.getServer().getPlayer(members.get(i)).get().sendMessage(Text.builder("[Guild Chat] " + nickname)
																				.color(TextColors.GREEN)
																				.append(Text.builder(": " + message)
																						.color(TextColors.WHITE)
																						.build())
																				.build());
		}
		
	}
	
	public void PayToGuild(int CoinsCount)
	{
		this.CoinsCount += CoinsCount;
		SqlService sql;
		String url = "jdbc:sqlite:config/guildwars/guilds.db";
		String query = "UPDATE guilds SET coins = (SELECT coins FROM guilds WHERE id = " + this.GetId() + ") + " + CoinsCount + " WHERE id = " + this.GetId();
		
		Connection conn;
		try {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
			conn = sql.getDataSource(url).getConnection();
			PreparedStatement stmtGetter = conn.prepareStatement(query);
			stmtGetter.executeUpdate();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public int GetGuildCapital() {
		return this.CoinsCount;
	}
	
	public int TakeCoins() {
		SqlService sql;
		String url = "jdbc:sqlite:config/guildwars/guilds.db";	
		Connection conn;
	
		if (this.CoinsCount < 35) {
			int coins = this.CoinsCount;
			this.CoinsCount = 0;
			try {
				sql = Sponge.getServiceManager().provide(SqlService.class).get();
				conn = sql.getDataSource(url).getConnection();
				PreparedStatement stmtGetter = conn.prepareStatement("UPDATE guilds SET coins = 0 WHERE id = " + this.GetId());
				stmtGetter.executeUpdate();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return coins;
		} else {
			try {
				sql = Sponge.getServiceManager().provide(SqlService.class).get();
				conn = sql.getDataSource(url).getConnection();
				PreparedStatement stmtGetter = conn.prepareStatement("UPDATE guilds SET coins = (SELECT coins FROM guilds WHERE id = " + this.GetId() + ") - 35 WHERE id = " + this.GetId());
				stmtGetter.executeUpdate();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			this.CoinsCount -= 35;
			return 35;
		}		
	}
	
	public void TakeCoins(int count) {
		SqlService sql;
		String url = "jdbc:sqlite:config/guildwars/guilds.db";	
		Connection conn;
		try {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
			conn = sql.getDataSource(url).getConnection();
			PreparedStatement stmtGetter = conn.prepareStatement("UPDATE guilds SET coins = (SELECT coins FROM guilds WHERE id = " + this.GetId() + ") - " + count + " WHERE id = " + this.GetId());
			stmtGetter.executeUpdate();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void WarMode(int ToID) {
		this.wars.add(new GuildWar(this.GetId(), ToID));
	}
	
	public boolean g(int ToID) {
		for (int i = 0; i < this.wars.size(); i++) {
			if (this.wars.get(i).getTo() == ToID)
				return true;
		}
		return false;
	}
	 
	public boolean IsInWarMode(int guildId) {
		for(int i = 0; i < this.wars.size(); i++) {
			if (this.wars.get(i).getFrom() == this.GetId() && this.wars.get(i).getTo() == guildId)
				return true;
		}
		return false;
	}
	
	public ArrayList<GuildWar> getGuildWithWar(){
		return this.wars;
	}
	
	public int ToDeclareWar(int ToID) {
		if (this.GetId() == ToID)
			return 0;
		
		boolean IsExists = false;
		int i;
		for (i = 0; i < Main.guilds.size(); i++) {
			if (Main.guilds.get(i).GetId() == ToID) {
				IsExists = true;
				break;
			}
		}
		if (!IsExists)
			return 0;
		
		this.wars.add(new GuildWar(this.GetId(), ToID));
		SqlService sql;
		String url = "jdbc:sqlite:config/Guildwars/Guilds.db";
		String query = "INSERT INTO wars (fromID, toID) VALUES ("+ ToID + ", " + this.GetId() + ")";
		
		Connection conn;
		try {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
			conn = sql.getDataSource(url).getConnection();
			PreparedStatement stmtGetter = conn.prepareStatement(query);
			stmtGetter.executeUpdate();
			conn.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return 1;
		
	}
	
	public int ToDeclarePeace(int ToID) {
		if (this.GetId() == ToID)
			return 0;
		
		boolean IsExists = false;
		int i;
		for (i = 0; i < Main.guilds.size(); i++) {
			if (Main.guilds.get(i).GetId() == ToID) {
				IsExists = true;
				break;
			}
		}
		if (!IsExists)
			return 0;
		
		for (i = 0; i < this.wars.size(); i++) {
			if (this.wars.get(i).getTo() == ToID)
				this.wars.remove(i);
		}
		SqlService sql;
		String url = "jdbc:sqlite:config/Guildwars/Guilds.db";
		String query = "DELETE FROM `wars` WHERE `fromId` = " + this.GetId() + " AND `toId` = " + ToID;
		
		Connection conn;
		try {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
			conn = sql.getDataSource(url).getConnection();
			PreparedStatement stmtGetter = conn.prepareStatement(query);
			stmtGetter.executeUpdate();
			conn.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return 1;
		
	}
	
	public ArrayList<String> GetMembersOfGuild(){
		ArrayList<String> nicknames = new ArrayList<String>();
		for (int i = 0; i < this.members.size(); i++) {
			nicknames.add(this.members.get(i).GetNickname());
		}
		return nicknames;
	}
	
	public int GetGuildMembersCount() {
		return this.members.size();
	}

	public String GetName() {
		return this.name;
	}
	
	public boolean IsLeader(String nickname) {
		for (int i = 0; i < this.members.size(); i++) {
			if (this.members.get(i).GetNickname().equals(nickname))
				return this.members.get(i).IsLeader();
		}
		return false;
	}
	
	public boolean IsInGuild(String nickname) {
		for (int i = 0; i < this.members.size(); i++) {
			if (this.members.get(i).GetNickname().toUpperCase().equals(nickname.toUpperCase()))
				return true;
		}
		return false;
	}
	
	public int GetUnitOfGuild(String nickname) {
		for (int i = 0; i < this.members.size(); i++) {
				if (this.members.get(i).GetNickname().equals(nickname))
					return i;
		}
		return -1;
	}
	
	public static void CreateGuild(String name, String masterNickname) {
		for (int i = 0; i < Main.guilds.size(); i++) {
			if (Main.guilds.get(i).GetName().toUpperCase().equals(name.toUpperCase())) {
				Sponge.getServer().getPlayer(masterNickname).get().sendMessage(Text.of(TextColors.RED, "[Guild] Guild with that name already exists!"));
				return;
			}
		}
		SqlService sql;
		String url = "jdbc:sqlite:config/guildwars/guilds.db";
		String query = "INSERT INTO guilds (name) VALUES (\""+name+"\")";
		
		Connection conn;
		try {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
			conn = sql.getDataSource(url).getConnection();
			PreparedStatement stmtGetter = conn.prepareStatement(query);
			stmtGetter.executeUpdate();
			
			stmtGetter = conn.prepareStatement("SELECT MAX(id) AS max FROM guilds");
			ResultSet guildMaxId = stmtGetter.executeQuery();
			//guildMaxId.next();
			stmtGetter = conn.prepareStatement("INSERT INTO members (nickname, guildId, master) VALUES (\"" + masterNickname + "\", " + guildMaxId.getString("max") + ", 1)");
			stmtGetter.executeUpdate();	
			Guild newGuild = new Guild(name, Integer.valueOf(guildMaxId.getString("max")), 0);
			int result = newGuild.AddMemberToGuild(masterNickname, Integer.valueOf(guildMaxId.getString("max")), true);
			if (result == 2)
				Sponge.getServer().getPlayer(masterNickname).get().sendMessage(Text.of(TextColors.GREEN, "[Guild] Leader is added to guild!"));
			else if (result == 0)
					Sponge.getServer().getPlayer(masterNickname).get().sendMessage(Text.of(TextColors.RED, "[Guild] Cannot add leader to the guild!"));
				else if (result == 1)
					Sponge.getServer().getPlayer(masterNickname).get().sendMessage(Text.of(TextColors.RED, "[Guild] Guild is overfilled!"));
				
			Main.guilds.add(newGuild);
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void DeleteGuild() {
		SqlService sql;
		String url = "jdbc:sqlite:config/Guildwars/Guilds.db";
		String query = "DELETE FROM guilds WHERE name=\"" + this.name + "\"";
		
		Connection conn;
		try {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
			conn = sql.getDataSource(url).getConnection();
			PreparedStatement stmtGetter = conn.prepareStatement(query);
			stmtGetter.executeUpdate();
			stmtGetter = conn.prepareStatement("DELETE FROM members WHERE guildId = " + this.GetId());
			stmtGetter.executeUpdate();
			stmtGetter = conn.prepareStatement("DELETE FROM wars WHERE fromID = " + this.GetId() + " OR toID = " + this.GetId());
			stmtGetter.executeUpdate();
			stmtGetter = conn.prepareStatement("UPDATE halls SET guildmaster = 0 WHERE id = " + this.GetId());
			stmtGetter.executeUpdate();
			conn.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void _AddMemberToGuild(String nickname, int ParentGI, boolean IsLeader) {
		String url = "jdbc:sqlite:config/guildwars/Guilds.db";
		SqlService sql;
		Connection conn;
		try {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
			conn = sql.getDataSource(url).getConnection();
			PreparedStatement stmtInsert = conn.prepareStatement("INSERT INTO members (nickname, guildId, master) VALUES (\"" + nickname + "\", " + ParentGI +", 0)");
			stmtInsert.executeUpdate();
			conn.close();
			return;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public int AddMemberToGuild(String nickname, int ParentGI, boolean IsLeader) {
		if (this.GetGuildMembersCount() > 30) {
			return 1;
		} else {
			if (this.members.size() <= 30) {
				this.members.add(new GuildUnit(nickname, ParentGI, IsLeader));
				return 2;
					
			}
		}
		return 0;
	}
	
	public int RemoveMemberFromGuild(String nickName) {
		if (this.IsInGuild(nickName)) {
			this.members.remove(this.GetUnitOfGuild(nickName));
			String url = "jdbc:sqlite:config/guildwars/Guilds.db";
			SqlService sql;
			Connection conn;
			try {
				sql = Sponge.getServiceManager().provide(SqlService.class).get();
				conn = sql.getDataSource(url).getConnection();
				PreparedStatement stmtInsert = conn.prepareStatement("DELETE FROM members WHERE nickname = \"" + nickName + "\"");
				stmtInsert.executeUpdate();
				conn.close();
				return 1;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		}
		return 0;
	}
	
	public void SetLeader(String nickname) {
		if (this.IsInGuild(nickname)) {
			if (!this.IsLeader(nickname)) {
				for (int i = 0; i < this.members.size(); i++) {
					this.members.get(i).SetLeader(false);
				}
				for (int i = 0; i < this.members.size(); i++) {
					if (this.members.get(i).GetNickname().toUpperCase().equals(nickname.toUpperCase()))
						this.members.get(i).SetLeader(true);
				}
				String url = "jdbc:sqlite:config/guildwars/Guilds.db";
				SqlService sql;
				Connection conn;
				try {
					sql = Sponge.getServiceManager().provide(SqlService.class).get();
					conn = sql.getDataSource(url).getConnection();
					PreparedStatement stmtInsert = conn.prepareStatement("UPDATE members SET master = 0 WHERE master = 1 AND guildId = " + this.GetId() );
					stmtInsert.executeUpdate();
					stmtInsert = conn.prepareStatement("UPDATE members SET master = 1 WHERE nickname = \"" + nickname + "\" AND guildId = " + this.GetId());
					stmtInsert.executeUpdate();
					conn.close();
					return;
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

}	
