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

public class GuildDissolveCommand implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		} else {
			Player p = (Player) src;
			boolean IsInGuild = false;
			for (int i = 0; i < Main.guilds.size(); i++) {
				if (Main.guilds.get(i).IsInGuild(p.getName())) {
					IsInGuild = true;
				}
			}
			
			if (!IsInGuild) {
				p.sendMessage(Text.of(TextColors.RED, "[Guild] You're not in guild."));
			}
			
			for (int i = 0; i < Main.guilds.size(); i++) {
				if (Main.guilds.get(i).IsInGuild(p.getName())) {
					if (Main.guilds.get(i).IsLeader(p.getName())) {
						Main.guilds.get(i).DeleteGuild();
						Main.guilds.get(i).SayToChat("Guild has been dissolved by the guild leader.");
						Main.guilds.remove(i);
						p.sendMessage(Text.of(TextColors.GREEN, "[Guild] You have been dissolve the guild."));						
					} else {
						p.sendMessage(Text.of(TextColors.RED, "[Guild] You don't have permission to do that."));
					}
				}
			}
		}
		return CommandResult.success();
	}

}
