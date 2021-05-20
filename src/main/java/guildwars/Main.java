package guildwars;

import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.inject.Inject;

import commands.GuildMemberCommand;
import commands.GuildMemberListCommand;
import commands.GuildOnlineCommand;
import commands.GuildWarCommand;
import commands.selection.SelectorBuyHallCommand;
import commands.selection.SelectorClaimCommand;
import commands.selection.SelectorDeleteHallCommand;
import commands.selection.SelectorInfoHallCommand;
import commands.selection.SelectorSellHallCommand;
import commands.selection.SelectorSpawnItemCommand;
import commands.GuildChatCommand;
import commands.GuildDissolveCommand;
import commands.GuildInfoCommand;
import commands.GuildInviteOkeyCommand;
import commands.GuildLeaveCommand;
import guildunits.Guild;
import guildunits.GuildHall;
import guildunits.PlayerInvitation;
import guildunits.SelectedPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

@Plugin(id = "guildwars", name="Guild Wars", version = "0.9.0", description="Plugin for guild wars and trade.")
public class Main {
	private SqlService sql;
	@Inject
	private Logger logger;
	
	public static ArrayList<Guild> guilds;
	public static ArrayList<PlayerInvitation> invitations = new ArrayList<PlayerInvitation>();
	public static Map<String, SelectedPoint> points = new HashMap<String, SelectedPoint>();
	public static ArrayList<GuildHall> halls;
	
	public DataSource getDataSource(String jdbcUrl) throws SQLException {
		if (this.sql == null)
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
		return sql.getDataSource(jdbcUrl);
	}
	
	public void LoadGuildsAndMembers(World world) throws SQLException {
		String url = "jdbc:sqlite:config/guildwars/guilds.db";
	
		Connection conn = getDataSource(url).getConnection();
		PreparedStatement stmtGetter = conn.prepareStatement("SELECT * FROM guilds");
		ResultSet guilds = stmtGetter.executeQuery();				
		Main.guilds = new ArrayList<Guild>();
		Main.halls = new ArrayList<GuildHall>();
		while (guilds.next()) {
			Guild guild = new Guild(guilds.getString("name"), Integer.valueOf(guilds.getString("id")), Integer.valueOf(guilds.getString("coins")));
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM members WHERE guildId = " + guilds.getString("id"));
			ResultSet guildsMembers = stmt.executeQuery();
			while (guildsMembers.next()) {
				boolean isLeader = (guildsMembers.getString("master").equals("1")) ? true : false;
				guild.AddMemberToGuild(guildsMembers.getString("nickname").trim(), Integer.valueOf(guildsMembers.getString("guildId")), isLeader);
			}
			stmt = conn.prepareStatement("SELECT * FROM wars");
			ResultSet guildWars = stmt.executeQuery();
			while(guildWars.next()) {
				if (guildWars.getString("fromID").equals(String.valueOf(guild.GetId()))){
					logger.info("War is declared to:" + guildWars.getString("toID"));
					guild.WarMode(Integer.valueOf(guildWars.getString("toID")));
				}
			}
			Main.guilds.add(guild);
			logger.info("Guild \"" + guilds.getString("name") + "\" has been registred! In guild: " + guild.GetGuildMembersCount() + " members.");
		}
		stmtGetter = conn.prepareStatement("SELECT * FROM `halls`");
		ResultSet hallsResult = stmtGetter.executeQuery();
		while (hallsResult.next()) {
			halls.add(new GuildHall(Integer.valueOf(hallsResult.getString("guildmaster")), 
									hallsResult.getString("name"),
									Integer.valueOf(hallsResult.getString("price")),
									new Location<World>(world, Integer.valueOf(hallsResult.getString("flocx")),
															  Integer.valueOf(hallsResult.getString("flocy")),
															  Integer.valueOf(hallsResult.getString("flocz"))),
									new Location<World>(world, Integer.valueOf(hallsResult.getString("slocx")),
											 				  Integer.valueOf(hallsResult.getString("slocy")),
											 				  Integer.valueOf(hallsResult.getString("slocz"))),
									new Location<World>(world, Integer.valueOf(hallsResult.getString("tp_locx")),
															   Integer.valueOf(hallsResult.getString("tp_locy")),
															   Integer.valueOf(hallsResult.getString("tp_locz")))));
		}
		conn.close(); 
		
	}
	
	public void SayToGuild(Text text, int number) {
		Guild guild = Main.guilds.get(number);
		
		ArrayList<String> guildMembers = guild.GetMembersOfGuild();
		for (int k = 0; k < guildMembers.size(); k++) {
			if (Sponge.getServer().getPlayer(guildMembers.get(k)).isPresent()){
				Optional<Player> p = Sponge.getServer().getPlayer(guildMembers.get(k));
				Player player = p.get();
				player.sendMessage(text);
			}
		}
	}
	
	@Listener
	public void onServerStart(GameStartedServerEvent event) throws SQLException {
		this.LoadGuildsAndMembers(Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get());
		
		CommandSpec guildAddCommand = CommandSpec.builder()
				.description(Text.of("Guild Wars main command."))
				.permission("guildwars.main")
				.arguments(
						GenericArguments.firstParsing(GenericArguments.string(Text.of("invite")),
								GenericArguments.string(Text.of("kick")), 
								GenericArguments.string(Text.of("create")),
								GenericArguments.string(Text.of("leader"))),
						GenericArguments.firstParsing(GenericArguments.string(Text.of("name")),
								GenericArguments.string(Text.of("nickname"))))
				.executor(new GuildMemberCommand())
				.build();
		
		Sponge.getCommandManager().register(this, guildAddCommand, "guild", "g");
		
		CommandSpec guildChatCommand = CommandSpec.builder()
				.description(Text.of("Chat with guild members"))
				.permission("guildwars.chat")
				.arguments(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of("message"))))
				.executor(new GuildChatCommand())
				.build();
		
		Sponge.getCommandManager().register(this, guildChatCommand, "guildchat", "gc");
		
		CommandSpec guildInviteOkeyCommand = CommandSpec.builder()
				.description(Text.of("Agreement for invite in guild."))
				.permission("guildwars.agree")
				.executor(new GuildInviteOkeyCommand())
				.build();
		
		Sponge.getCommandManager().register(this, guildInviteOkeyCommand, "gaccept");
		
		CommandSpec guildOnlineCommand = CommandSpec.builder()
				.description(Text.of("Get online player list in guild."))
				.permission("guildwars.who")
				.executor(new GuildOnlineCommand())
				.build();
		
		Sponge.getCommandManager().register(this, guildOnlineCommand, "gwho", "gonline");
		
		CommandSpec guildMemberListCommand = CommandSpec.builder()
				.description(Text.of("Get member list of guild."))
				.permission("guildwars.who")
				.executor(new GuildMemberListCommand())
				.build();
		
		Sponge.getCommandManager().register(this, guildMemberListCommand, "glist", "gmembers", "gl");
		
		CommandSpec guildLeaveCommand = CommandSpec.builder()
				.description(Text.of("Leave the guild."))
				.permission("guildwars.leave")
				.executor(new GuildLeaveCommand())
				.build();
		
		Sponge.getCommandManager().register(this, guildLeaveCommand, "gleave");
		
		CommandSpec guildDissolveCommand = CommandSpec.builder()
				.description(Text.of("Dissolve the guild."))
				.permission("guildwars.dissolve")
				.executor(new GuildDissolveCommand())
				.build();
		
		Sponge.getCommandManager().register(this, guildDissolveCommand, "gdissolve");
		
		CommandSpec guildWarCommand = CommandSpec.builder()
				.description(Text.of("Declare or cancel war between guilds."))
				.permission("guildwars.war")
				.arguments(GenericArguments.firstParsing(GenericArguments.string(Text.of("war")),
														  GenericArguments.string(Text.of("cancel"))),
						GenericArguments.onlyOne(GenericArguments.string(Text.of("guildName"))))
				.executor(new GuildWarCommand())
				.build();
		
		Sponge.getCommandManager().register(this, guildWarCommand, "gw", "guildwar");
		
		CommandSpec guildInfoCommand = CommandSpec.builder()
				.description(Text.of("Get information about guild."))
				.permission("guildwars.war")
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("guild_name"))))
				.executor(new GuildInfoCommand())
				.build();
		
		Sponge.getCommandManager().register(this, guildInfoCommand, "ginfo", "guildinfo");
		
		/***************************************************************
		 * SELECTOR HANDLER
		 ***************************************************************/
		
		CommandSpec selectionAxeCreateCommand = CommandSpec.builder()
				.description(Text.of("Spawn the selection axe."))
				.permission("guildwars.selection.spawntool")
				.executor(new SelectorSpawnItemCommand())
				.build();
		
		Sponge.getCommandManager().register(this, selectionAxeCreateCommand, "gwand");
		
		CommandSpec selectionClaimCommand = CommandSpec.builder()
				.description(Text.of("Claim zone as guild hall."))
				.permission("guildwars.selection.claim")
				.arguments(
						GenericArguments.firstParsing(GenericArguments.string(Text.of("hall_name"))),
						GenericArguments.firstParsing(GenericArguments.string(Text.of("price"))))
				.executor(new SelectorClaimCommand())
				.build();
		
		Sponge.getCommandManager().register(this, selectionClaimCommand, "ghclaim");
		
		CommandSpec selectionDeleteCommand = CommandSpec.builder()
				.description(Text.of("Delete guild hall."))
				.permission("guildwars.selection.delete")
				.arguments(GenericArguments.firstParsing(GenericArguments.string(Text.of("hall_name"))))
				.executor(new SelectorDeleteHallCommand())
				.build();
		
		Sponge.getCommandManager().register(this, selectionDeleteCommand, "ghdelete");
		
		CommandSpec selectionBuyCommand = CommandSpec.builder()
				.description(Text.of("Buy the guild hall by it's name."))
				.permission("guildwars.selection.buy")
				.arguments(GenericArguments.firstParsing(GenericArguments.string(Text.of("hall_name"))))
				.executor(new SelectorBuyHallCommand())
				.build();
		
		Sponge.getCommandManager().register(this, selectionBuyCommand, "ghbuy");
		
		CommandSpec selectionSellCommand = CommandSpec.builder()
				.description(Text.of("Sell guild hall."))
				.permission("guildwars.selection.sell")
				.executor(new SelectorSellHallCommand())
				.build();
		
		Sponge.getCommandManager().register(this, selectionSellCommand, "ghsell");
		
		CommandSpec selectionInfoCommand = CommandSpec.builder()
				.description(Text.of("Get information of guild hall."))
				.permission("guildwars.selection.info")
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("hall_name"))))
				.executor(new SelectorInfoHallCommand())
				.build();
		
		Sponge.getCommandManager().register(this, selectionInfoCommand, "ghinfo");
	}
	
	@Listener
    public void onUserConnect(ClientConnectionEvent.Join event) {
		Text guildMasterNotify = Text.builder("Guild Master joined to the server!")
				.color(TextColors.DARK_GREEN)
				.build();
		for (int j = 0; j < Main.guilds.size(); j++ ) {
			Player p = (Player) event.getTargetEntity();
			Optional<Text> opNick = p.get(Keys.DISPLAY_NAME);
			if (opNick.isPresent()) {
				Text nickName = opNick.get();
				Text guildNotify = Text.builder("Guilder ")
						.append(Text.builder(nickName.toPlain())
								.color(TextColors.BLUE)
								.build())
						.append(Text.builder(" joined to the server!")
								.build())
						.build();		
				if (Main.guilds.get(j).IsInGuild(nickName.toPlain().trim())) {
					if (Main.guilds.get(j).IsLeader(nickName.toPlain().trim())) {
						this.SayToGuild(guildMasterNotify, j);
						//event.getTargetEntity().sendMessage(guildMasterNotify);
					} else {
						//event.getTargetEntity().sendMessage(guildNotify);
						this.SayToGuild(guildNotify, j);
					}
				} else {
					continue;
				}
			}
						
		}
	}
	
	@Listener
	public void onPlayerDeath(DestructEntityEvent.Death event) {
		int killerGuildId = 0;
		int killedGuildId = 0;
		Player killer = null;
		Player killed = null;
		Guild killerGuild = null;
		Guild killedGuild = null;
		killed = (Player) event.getCause().last(Player.class).get();
		if (event.getTargetEntity() instanceof Player)
		    killer = (Player) event.getTargetEntity();
		
		if (killer != null && killed != null) {
			//Get killer guild.
			for (int i = 0; i < guilds.size(); i++) {
				if (guilds.get(i).IsInGuild(killer.getName())) {
					killerGuild = guilds.get(i);
					killerGuildId = i;
				}
				if (guilds.get(i).IsInGuild(killed.getName())) {
					killedGuild = guilds.get(i);
					killedGuildId = i;
				}
			}
			
			if (killerGuild != killedGuild) {
				if (killedGuild.IsInWarMode(killerGuild.GetId())) {
					/*logger.info("-----------------------------------------");
					logger.info("Killer from: " + killerGuild.GetName());
					logger.info("Killed from: " + killedGuild.GetName());
					logger.info("-----------------------------------------");*/
					int reward = guilds.get(killerGuildId).TakeCoins();
					guilds.get(killedGuildId).PayToGuild(reward);
					killer.sendMessage(Text.of(TextColors.RED, "[Guild] Your guild has lose " + Integer.valueOf(reward) + " bronze coins!"));
					killed.sendMessage(Text.of(TextColors.GREEN, "[Guild] Your guild has receive " + Integer.valueOf(reward) + " bronze coins!"));
				}
			}
			
		}
	}
	
	@SuppressWarnings("deprecation")
	@Listener
	public void onFirstClickSelector(InteractBlockEvent.Primary event, @First Player player) {
		if (event.getTargetBlock().getState().getId().equals("minecraft:air"))
			return;
		
		if (!points.containsKey(player.getName())) {
			points.put(player.getName(), new SelectedPoint());
		}
		
		if (player.hasPermission("guildwars.selection.select")) {
			ItemType selector = ItemTypes.NONE;
			if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
				selector = player.getItemInHand(HandTypes.MAIN_HAND).get().getItem();
				if (selector.getId().equals("minecraft:golden_axe")) {
					Location<World> loc = event.getTargetBlock().getLocation().get();
					points.get(player.getName()).setFirstPoint(loc);
					player.sendMessage(Text.builder("First point for selecting at ")
							.color(TextColors.GREEN)
							.append(Text.of(TextColors.WHITE, loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()))
							.build());
					event.setCancelled(true);
				} else if (selector.getId().equals("minecraft:golden_sword")) {
					Location<World> loc = event.getTargetBlock().getLocation().get();
					points.get(player.getName()).setTeleportPoint(loc);
					player.sendMessage(Text.builder("Teleport point at ")
							.color(TextColors.GREEN)
							.append(Text.of(TextColors.WHITE, loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()))
							.build());
				}
			}
		}
		if (!player.hasPermission("guildwars.selection.build"))
			event.setCancelled(true);
		
	}
	
	@SuppressWarnings("deprecation")
	@Listener
	public void onSecondClickSelector(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
		if (event.getTargetBlock().getState().getId().equals("minecraft:air"))
			return;
		
		if (event.getTargetBlock().getState().getId().contains("door")){
			return;
		}
		
		if (event.getTargetBlock().getState().getType().equals(BlockTypes.ANVIL) || event.getTargetBlock().getState().getId().contains("weapon_rack") || event.getTargetBlock().getState().getId().contains("toolrack") || 
				event.getTargetBlock().getState().getId().contains("armorstand")) {
			if (!player.hasPermission("guildwars.selection.build")) {
				event.setCancelled(true);
				return;
			}
		}
		
		if (!points.containsKey(player.getName())) {
			points.put(player.getName(), new SelectedPoint());
		}
		
		if (player.hasPermission("guildwars.selection.select")) {
			ItemType selector = ItemTypes.NONE;
			if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
				selector = player.getItemInHand(HandTypes.MAIN_HAND).get().getItem();
				if (selector.getId().equals("minecraft:golden_axe")) {
					Location<World> loc = event.getTargetBlock().getLocation().get();
					points.get(player.getName()).setSecondPoint(loc);
					player.sendMessage(Text.builder("Second point for selecting at ")
							.color(TextColors.GREEN)
							.append(Text.of(TextColors.WHITE, loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()))
							.build());
					event.setCancelled(true);
					return;
				}
			}
		}
		//1. Если блок на территории гильдхолла
		//2. Если это дверь, зачаровальня, кровать или зельеварка, то разрешить.
		int guildId = -1;
		for(int i = 0; i < Main.guilds.size(); i++) {
			if (Main.guilds.get(i).IsInGuild(player.getName())) {
				guildId = Main.guilds.get(i).GetId();
				break;
			}
		}
		for(int i = 0; i < Main.halls.size(); i++) {
			GuildHall hall = Main.halls.get(i);
			boolean IsInX = hall.CheckExistance(hall.getFirstPos().getX(), hall.getSecondPos().getX(), event.getTargetBlock().getLocation().get().getX());
			boolean IsInY = hall.CheckExistance(hall.getFirstPos().getY(), hall.getSecondPos().getY() , event.getTargetBlock().getLocation().get().getY());
			boolean IsInZ = hall.CheckExistance(hall.getFirstPos().getZ(), hall.getSecondPos().getZ(), event.getTargetBlock().getLocation().get().getZ());
			
			if (hall.getMasterGuild() != 0 && guildId != hall.getMasterGuild() && IsInX && IsInY && IsInZ) {
				if (!player.hasPermission("guildwars.selection.foreign_visit") && guildId != -1) {
					player.sendMessage(Text.of(TextColors.RED, "[Guild] You don't have permission to use blocks in this guild hall."));
					event.setCancelled(true);
					return;
				}
			} else if (IsInX && IsInY && IsInZ){
				String blockId = event.getTargetBlock().getState().getId();
				if (!blockId.contains("pedestal")) {
					event.setCancelled(true);
					return;
				}
				event.setCancelled(false);
				return;
			}
			
			Location<World> blockLoc = new Location<World>(Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get(),
														  event.getTargetBlock().getLocation().get().getX(),
														  event.getTargetBlock().getLocation().get().getY(),
														  event.getTargetBlock().getLocation().get().getZ());
			BlockType type = blockLoc.getBlock().getType();
			ItemType picture = player.getItemInHand(HandTypes.MAIN_HAND).get().getType();
			if (!type.equals(BlockTypes.CHEST) && !type.equals(BlockTypes.BED) && !type.equals(BlockTypes.ENCHANTING_TABLE) && !type.equals(BlockTypes.BREWING_STAND) && !picture.equals(ItemTypes.PAINTING)) {
				if (!player.hasPermission("guildwars.selection.build"))
					event.setCancelled(true);
			}
			
		}
		
	}
	
	@Listener
	public void onPrimaryInteract(InteractEntityEvent.Primary event, @First Player player) {
		/*if (event.getTargetEntity().getType().equals(EntityTypes.PAINTING)) {
			if (!player.hasPermission("guildwars.selection.build")) {
				event.setCancelled(true);
				return;
			}
		}*/
		if (event.getTargetEntity().getType().equals(EntityTypes.PAINTING) || event.getTargetEntity().getType().equals(EntityTypes.ITEM_FRAME)) {
			int guildId = -1;
			for(int i = 0; i < Main.guilds.size(); i++) {
				if (Main.guilds.get(i).IsInGuild(player.getName())) {
					guildId = Main.guilds.get(i).GetId();
					break;
				}
			}
			boolean IsInX = false;
			boolean IsInY = false;
			boolean IsInZ = false;
			for(int i = 0; i < Main.halls.size(); i++) {
				GuildHall hall = Main.halls.get(i);
				IsInX = hall.CheckExistance(hall.getFirstPos().getX(), hall.getSecondPos().getX(), Math.floor(event.getTargetEntity().getLocation().getX()));
				IsInY = hall.CheckExistance(hall.getFirstPos().getY(), hall.getSecondPos().getY() , Math.floor(event.getTargetEntity().getLocation().getY()));
				IsInZ = hall.CheckExistance(hall.getFirstPos().getZ(), hall.getSecondPos().getZ(), Math.floor(event.getTargetEntity().getLocation().getZ()));
				
				if (hall.getMasterGuild() != 0 && guildId != hall.getMasterGuild() && IsInX && IsInY && IsInZ) {
					if (!player.hasPermission("guildwars.selection.foreign_visit") && guildId != -1) {
						player.sendMessage(Text.of(TextColors.RED, "[Guild] You don't have permission to use blocks in this guild hall."));
						event.setCancelled(true);
						return;
					} 
				} else if (IsInX != false && IsInY != false && IsInZ != false){
					return;
				} else {
					if (!player.hasPermission("guildwars.selection.build")) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}
	
	@Listener
	public void onSecondaryInteract(InteractEntityEvent.Secondary event, @First Player player) {
		if (event.getTargetEntity().getType().equals(EntityTypes.ARMOR_STAND)) {
			int guildId = -1;
			for(int i = 0; i < Main.guilds.size(); i++) {
				if (Main.guilds.get(i).IsInGuild(player.getName())) {
					guildId = Main.guilds.get(i).GetId();
					break;
				}
			}
			boolean IsInX = false;
			boolean IsInY = false;
			boolean IsInZ = false;
			for(int i = 0; i < Main.halls.size(); i++) {
				GuildHall hall = Main.halls.get(i);
				IsInX = hall.CheckExistance(hall.getFirstPos().getX(), hall.getSecondPos().getX(), Math.floor(event.getTargetEntity().getLocation().getX()));
				IsInY = hall.CheckExistance(hall.getFirstPos().getY(), hall.getSecondPos().getY() , Math.floor(event.getTargetEntity().getLocation().getY()));
				IsInZ = hall.CheckExistance(hall.getFirstPos().getZ(), hall.getSecondPos().getZ(), Math.floor(event.getTargetEntity().getLocation().getZ()));
				if (hall.getMasterGuild() != 0 && guildId != hall.getMasterGuild() && IsInX && IsInY && IsInZ) {
					if (!player.hasPermission("guildwars.selection.foreign_visit") && guildId != -1) {
						player.sendMessage(Text.of(TextColors.RED, "[Guild] You don't have permission to use blocks in this guild hall."));
						event.setCancelled(true);
						return;
					} 
				} else if (IsInX != false && IsInY != false && IsInZ != false){
					return;
				} else {
					if (!player.hasPermission("guildwars.selection.build")) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}
	
	@Listener
	public void OnZoneEnter(MoveEntityEvent event, @First Player player) {
		int guildId = -1;
		for(int i = 0; i < Main.guilds.size(); i++) {
			if (Main.guilds.get(i).IsInGuild(player.getName())) {
				guildId = Main.guilds.get(i).GetId();
				break;
			}
		}
		
		for(int i = 0; i < Main.halls.size(); i++) {
			GuildHall hall = Main.halls.get(i);
			boolean IsInX = hall.CheckExistance(hall.getFirstPos().getX(), hall.getSecondPos().getX(), player.getLocation().getX());
			boolean IsInY = hall.CheckExistance(hall.getFirstPos().getY(), hall.getSecondPos().getY() , player.getLocation().getY());
			boolean IsInZ = hall.CheckExistance(hall.getFirstPos().getZ(), hall.getSecondPos().getZ(), player.getLocation().getZ());
			
			if (hall.getMasterGuild() != 0 && guildId != hall.getMasterGuild() && IsInX && IsInY && IsInZ) {
				if (!player.hasPermission("guildwars.selection.foreign_visit") && guildId != -1) {
					Location<World> loc = hall.getTeleportPos();
					player.sendMessage(Text.of(TextColors.RED, "[Guild] You don't have permission to visit this guild hall."));
					event.setToTransform(new Transform<World>(loc));
					return;
				}
			}
		}
	}
}