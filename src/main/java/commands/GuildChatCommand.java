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

public class GuildChatCommand implements CommandExecutor{
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		}
		else {
			for (int i = 0; i < Main.guilds.size(); i++) {
				if (Main.guilds.get(i).IsInGuild(((Player)src).getName())) {
					Main.guilds.get(i).SayToChat(((Player)src).getName(), args.<String>getOne("message").get());
					return CommandResult.success();
				}
				
			}
			((Player) src).sendMessage(Text.of(TextColors.RED, "[Guild] You are not member of any guild."));
		}
		return CommandResult.success();
	}
}
