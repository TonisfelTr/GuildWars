package commands.selection;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

public class SelectorInfoHallCommand implements CommandExecutor {	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String hallName = args.<String>getOne("hall_name").get();
		for (int i = 0; i < Main.halls.size(); i++) {
			if (Main.halls.get(i).getName().toUpperCase().equals(hallName.toUpperCase())) {
				src.sendMessage(Text.of(TextColors.GREEN, "Information about \"" + hallName + "\" guild hall"));
				src.sendMessage(Text.of(TextColors.DARK_GREEN, "--------------------------------------------"));
				Text hallMaster = Text.of(TextColors.WHITE, "no");
				for (int u = 0; u < Main.guilds.size(); u++) {
					if (Main.halls.get(i).getMasterGuild() == Main.guilds.get(u).GetId()) {
						hallMaster = Text.of(TextColors.WHITE, Main.halls.get(i).getName());
						break;
					}
				}
				src.sendMessage(Text.builder("Guild Hall Owner: ")
						.color(TextColors.GREEN)
						.append(hallMaster)
						.build());
			
			
				int gold = Main.halls.get(i).getPrice() / 3500;
				int goldEnd = Main.halls.get(i).getPrice() % 3500;
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
				src.sendMessage(Text.builder("Price: ")
						.color(TextColors.GREEN)
						.append(goldText)
						.append(silverText)
						.append(bronzeText)
						.build());
				
				return CommandResult.success();
			}
		}
		src.sendMessage(Text.of(TextColors.RED, "[Guild] That guild hall does not exist."));
		return CommandResult.success();
	}
}
