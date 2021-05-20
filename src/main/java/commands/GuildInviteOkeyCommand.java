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

public class GuildInviteOkeyCommand implements CommandExecutor{
		
	private boolean DoHaveInvite(String nickname) {
		for (int i = 0; i < Main.invitations.size(); i++) {
			if (Main.invitations.get(i).getNickname().equals(nickname))
				return true;
		}
		return false;
	}
	
	private int GetIdFromInvitation(String nickname) {
		for (int i = 0; i < Main.invitations.size(); i++) {
			if (Main.invitations.get(i).getNickname().equals(nickname))
				return Main.invitations.get(i).GetGuildId();
		}
		return -1;
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		}
		else {
			Player p = (Player)src;
			if (!DoHaveInvite(p.getName())) {
				p.sendMessage(Text.of(TextColors.RED, "[Guild] You don't have any invitation."));
				return CommandResult.success();
			} else {
				int guildIdInArray = this.GetIdFromInvitation(p.getName().toUpperCase());
				Main.guilds.get(guildIdInArray-1)._AddMemberToGuild(p.getName(), Main.guilds.get(guildIdInArray-1).GetId(), false);
				Main.guilds.get(guildIdInArray-1).AddMemberToGuild(p.getName(), Main.guilds.get(guildIdInArray-1).GetId(), false);
				p.sendMessage(Text.of(TextColors.GREEN, "[Guild] You have been joined to guild!"));
				Main.guilds.get(guildIdInArray-1).SayToChat(p.getName() + " has been joined to the guild!");
				return CommandResult.success();
			}
		}

	}
}
