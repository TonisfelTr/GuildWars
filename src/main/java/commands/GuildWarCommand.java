package commands;

import java.util.Collection;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

public class GuildWarCommand implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		} else {
			boolean IsExists = false;
			boolean IsInGuild = false;
			boolean IsLeader = false;
			Player p = (Player) src;
			Collection<String> action = args.getAll("war");
			String guildName = args.<String>getOne("guildName").get();
			
			String triggeredAction = (String) action.toArray()[0];
			
			for (int i = 0; i < Main.guilds.size(); i++) {
				if (Main.guilds.get(i).GetName().toUpperCase().equals(guildName.toUpperCase()))
					IsExists = true;
				if (Main.guilds.get(i).IsInGuild(p.getName())) {
					IsInGuild = true;
					if (Main.guilds.get(i).IsLeader(p.getName()))
						IsLeader = true;
				}
			}
			if (!IsExists) {
				p.sendMessage(Text.of(TextColors.RED, "[Guild] Guild with that name does not exist."));
				return CommandResult.success();
			}
			if (!IsInGuild) {
				p.sendMessage(Text.of(TextColors.RED, "[Guild] You're not in guild."));
				return CommandResult.success();
			} else {
				if (!IsLeader) {
					p.sendMessage(Text.of(TextColors.RED, "[Guild] You're not leader of guild."));
					return CommandResult.success();
				}
			}
			
			if (triggeredAction.equals("war")) {
				//1. Does guild exist?
				//2. Is guild in warmode?
				//3. Are you the leader?
				
				/*for (int o = 0; o < Main.guilds.size(); o++) {
					if (Main.guilds.get(o).GetName().toUpperCase().equals(guildName.toUpperCase())) {
						p.sendMessage(Text.of(TextColors.RED, "[Guild] You cannot war with your guild."));
						return CommandResult.success();
					}
						
				}*/
				
				int idFrom = 0;
				int idTo = 0;
				int guildFromId = -1;
				int guildToId = -1;
				for (int i = 0; i < Main.guilds.size(); i++) {
					if (Main.guilds.get(i).IsInGuild(p.getName())) {
						idFrom = Main.guilds.get(i).GetId();
						guildFromId = i;
					}
					if (Main.guilds.get(i).GetName().toUpperCase().equals(guildName.toUpperCase())) {
						idTo = Main.guilds.get(i).GetId();
						guildToId = i;
					}
						
				}
				if (idTo == 0) {
					p.sendMessage(Text.of(TextColors.RED, "[Guild] This guild does not exist."));
					return CommandResult.success();
				}
				if (Main.guilds.get(guildFromId).GetName().toUpperCase().equals(guildName.toUpperCase())) {
					p.sendMessage(Text.of(TextColors.RED, "[Guild] You cannot war with your guild."));
					return CommandResult.success();
				}
					
				
				if (idFrom != idTo) {
					//p.sendMessage(Text.of("It works!"));
					if (!Main.guilds.get(guildFromId).IsInWarMode(idTo)) {
						int result = Main.guilds.get(guildToId).ToDeclareWar(idFrom);
						if (result == 1){
							p.sendMessage(Text.of(TextColors.GREEN, "[Guild] You has been declared war to the " + guildName + " guild."));
						} else 
							p.sendMessage(Text.of(TextColors.RED, "[Guild] Cannot declare war with that guild."));
					} else {
						p.sendMessage(Text.of(TextColors.RED, "[Guild] Your guild already war with that guild."));
					}
				}
			}
			
			if (triggeredAction.equals("cancel")) {
				int idFrom = 0;
				int idTo = 0;
				int guildFromId = -1;
				int guildToId = -1;
				for (int i = 0; i < Main.guilds.size(); i++) {
					if (Main.guilds.get(i).IsInGuild(p.getName())) {
						idFrom = Main.guilds.get(i).GetId();
						guildFromId = i;
					}
					if (Main.guilds.get(i).GetName().toUpperCase().equals(guildName.toUpperCase())) {
						idTo = Main.guilds.get(i).GetId();
						guildToId = i;
					}
						
				}
				if (idTo == 0) {
					p.sendMessage(Text.of(TextColors.RED, "[Guild] This guild does not exist."));
					return CommandResult.success();
				}
				if (Main.guilds.get(guildFromId).GetName().toUpperCase().equals(guildName.toUpperCase())) {
					p.sendMessage(Text.of(TextColors.RED, "[Guild] You cannot declare peace with your guild."));
					return CommandResult.success();
				}
				
				if (idFrom != idTo) {
					//p.sendMessage(Text.of("It works!"));
					if (Main.guilds.get(guildFromId).IsInWarMode(idTo)) {
						int result = Main.guilds.get(guildToId).ToDeclarePeace(idFrom);
						if (result == 1){
							p.sendMessage(Text.of(TextColors.GREEN, "[Guild] You has been made peace to the " + guildName + " guild."));
						} else 
							p.sendMessage(Text.of(TextColors.RED, "[Guild] Cannot made peace with that guild."));
					} else {
						p.sendMessage(Text.of(TextColors.RED, "[Guild] Your do not have war with that guild."));
					}
				}
			}
		}
		return CommandResult.success();
	}

}
