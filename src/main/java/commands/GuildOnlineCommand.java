package commands;

import java.util.ArrayList;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

public class GuildOnlineCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		} else {
			Player p = (Player) src;
			ArrayList<String> players;
			for (int i = 0; i < Main.guilds.size(); i++) {
				if (Main.guilds.get(i).IsInGuild(p.getName())) {
					players = Main.guilds.get(i).GetMembersOfGuild();
					String OnlinePlayers = "";
					for (int j = 0; j < players.size(); j++)
						if (Sponge.getServer().getPlayer(players.get(j)).isPresent()) {
							OnlinePlayers += players.get(j) + ", ";
						}
					OnlinePlayers = OnlinePlayers.substring(0, OnlinePlayers.length()-2);
					p.sendMessage(Text.builder("[Guild] Online: ").color(TextColors.GREEN)
							.append(Text.builder(OnlinePlayers).color(TextColors.WHITE).build()).build());
					return CommandResult.success();
				}
			}
			p.sendMessage(Text.of(TextColors.RED, "[Guild] You are not member of a guild."));
			return CommandResult.success();
		}
	}	

	
}
