package commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

public class GuildInfoCommand implements CommandExecutor{

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String guildName = args.<String>getOne("guild_name").get();
		for (int y = 0; y < Main.guilds.size(); y++) {
			if (Main.guilds.get(y).GetName().toUpperCase().equals(guildName.toUpperCase())) {
				src.sendMessage(Text.builder("Information of the ")
						.color(TextColors.GREEN)
						.append(Text.builder(guildName)
								.color(TextColors.WHITE)
								.append(Text.builder(" guild")
										.color(TextColors.GREEN)
										.build())
								.build())
						.build());
				src.sendMessage(Text.of(TextColors.DARK_GREEN, "--------------------------------------------"));
				for (int u = 0; u < Main.guilds.get(y).GetMembersOfGuild().size(); u++) {
					if (Main.guilds.get(y).IsLeader(Main.guilds.get(y).GetMembersOfGuild().get(u)))
						src.sendMessage(Text.builder("Leader: ")
								.color(TextColors.GREEN)
								.append(Text.builder(Main.guilds.get(y).GetMembersOfGuild().get(u))
										.color(TextColors.WHITE)
										.build())
								.build());
						
				}
				src.sendMessage(Text.builder("Count of members: ")
						.color(TextColors.GREEN)
						.append(Text.builder(String.valueOf(Main.guilds.get(y).GetGuildMembersCount()))
								.build())
								.build());
				
				Text hallName = Text.builder("no").color(TextColors.WHITE).build();
				for (int u = 0; u < Main.halls.size(); u++) {
					if (Main.halls.get(u).getMasterGuild() == Main.guilds.get(y).GetId()) {
						hallName = Text.builder(Main.halls.get(u).getName()).color(TextColors.WHITE).build();
						break;
					}
				}
				src.sendMessage(Text.builder("Guild Hall: ")
									.color(TextColors.GREEN)
									.append(hallName)
									.build());
									
				
				int coinsAmount = Main.guilds.get(y).GetGuildCapital();
				int gold = coinsAmount / 3500;
				int goldEnd = coinsAmount % 3500;
				int silver = goldEnd / 100;
				int bronze = goldEnd % 100;
				Text goldText = Text.builder(String.valueOf(gold))
											.color(TextColors.WHITE)
											.append(Text.builder(" Gold ")
													.color(TextColors.YELLOW)
													.build())
											.build();
				Text silverText = Text.builder(String.valueOf(silver))
											.color(TextColors.WHITE)
											.append(Text.builder(" Silver ")
													.color(TextColors.DARK_GRAY)
													.build())
											.build();
				Text bronzeText = Text.builder(String.valueOf(bronze))
						.color(TextColors.WHITE)
						.append(Text.builder(" Bronze")
								.color(TextColors.RED)
								.build())
						.build();
				src.sendMessage(Text.builder("Balance: ")
						.color(TextColors.GREEN)
						.append(goldText)
						.append(silverText)
						.append(bronzeText)
						.build());
				
				return CommandResult.success();
			}	
		}
		src.sendMessage(Text.builder("[Guild] That guild does not exist.")
				.color(TextColors.RED)
				.build());
		return CommandResult.success();
		
	}
}
