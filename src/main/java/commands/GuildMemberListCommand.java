package commands;

import java.util.ArrayList;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

public class GuildMemberListCommand implements CommandExecutor{
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		} else {
			Player p = (Player) src;
			for (int j = 0; j < Main.guilds.size(); j++) {
				if (Main.guilds.get(j).IsInGuild(p.getName())) {
					ArrayList<String> members = Main.guilds.get(j).GetMembersOfGuild();
					Text msgHeader = Text.builder("[Guild " + Main.guilds.get(j).GetName() + "] List of members:")
							.color(TextColors.GREEN)
							.build();
					Builder msgBody = null;
					for (int i = 0; i < members.size(); i++) {
						//Is leader?
						//Is null
						if (msgBody == null) {
							if (Main.guilds.get(j).IsLeader(members.get(i))) {
								msgBody = Text.builder(members.get(i))
										.color(TextColors.AQUA)
										.append(Text.of((i != members.size()-1) ? ", " : ""));
							} else {
								msgBody = Text.builder(members.get(i) + ", ")
										.color(TextColors.WHITE);
							}	
						} else {
							if (Main.guilds.get(j).IsLeader(members.get(i))) {
							   msgBody.append(Text.builder(members.get(i))
									.color(TextColors.AQUA)
									.build())
							   		.append(Text.of((i != members.size()-1) ? ", " : ""));
							} else {
								msgBody.append(Text.builder(members.get(i) + ((i != members.size()-1) ? ", " : ""))
										.color(TextColors.WHITE)
										.build());
							}
						}
					}
					
					p.sendMessage(msgHeader);
					p.sendMessage(msgBody.build());
				}
					
			}
		}
		return CommandResult.success();
	}
}
