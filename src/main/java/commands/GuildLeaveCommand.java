package commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

public class GuildLeaveCommand implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		}
		else {
			Player p = (Player) src;
			boolean IsInGuild = false;
			for (int j = 0; j < Main.guilds.size(); j++) {
				if (Main.guilds.get(j).IsInGuild(p.getName()))
					IsInGuild = true;
			}
			
			if (IsInGuild) {
				for (int i = 0; i < Main.guilds.size(); i++) {
					if (Main.guilds.get(i).IsInGuild(p.getName())) {
						if (Main.guilds.get(i).IsLeader(p.getName())) {
							p.sendMessage(Text.of(TextColors.RED, "[Guild] Guild Leader cannot leave. Give leader status to another guild member."));
							return CommandResult.success();
						}
						else {
							if (Main.guilds.get(i).RemoveMemberFromGuild(p.getName()) == 1)
								p.sendMessage(Text.of(TextColors.GREEN, "[Guild] You left the " + Main.guilds.get(i).GetName() + " guild."));
							else 
								p.sendMessage(Text.of(TextColors.RED, "[Guild] You cannot leave the guild."));
						}
						return CommandResult.success();
					}
				}
			} else {
				p.sendMessage(Text.of(TextColors.RED, "[Guild] You are not in guild!"));
			}
			
		}
		return CommandResult.success();
	}

}
