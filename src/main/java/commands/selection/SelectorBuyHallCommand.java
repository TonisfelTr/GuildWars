package commands.selection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

public class SelectorBuyHallCommand implements CommandExecutor {	
	@SuppressWarnings("unused")
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		} else {
			Collection<String> hallNameCollection = args.getAll("hall_name");
			String hallName = (String) hallNameCollection.toArray()[0];
			//1. Узнать, существует ли нужный холл.
			//1.1 Проверить есть ли хозяин у данной гильдии.
			//1.2 Проверить наличие лидера у игрока.
			//2. Сравнить бюджет гильдии и стоимость холла.
			for (int i = 0; i < Main.halls.size(); i++) {
				if (Main.halls.get(i).getName().toUpperCase().equals(hallName.toUpperCase())) {
					if (Main.halls.get(i).getMasterGuild() > 0) {
						src.sendMessage(Text.of(TextColors.RED, "[Guild] That guildhall is sold already."));
						return CommandResult.success();
					}
					for (int u = 0; u < Main.guilds.size(); u++) {
						if (Main.guilds.get(u).IsInGuild(src.getName())) {
							if (Main.guilds.get(u).IsLeader(src.getName())) {
								if (Main.guilds.get(u).GetGuildCapital() >= Main.halls.get(i).getPrice()) {
									Main.halls.get(i).setGuildMaster(Main.guilds.get(u).GetId());
									String url = "jdbc:sqlite:config/guildwars/Guilds.db";
									SqlService sql;
									Connection conn;
									try {
										sql = Sponge.getServiceManager().provide(SqlService.class).get();
										conn = sql.getDataSource(url).getConnection();
										PreparedStatement stmt = conn.prepareStatement("SELECT guildmaster FROM halls WHERE name = \"" + hallName + "\"");
										ResultSet result = stmt.executeQuery();
										while (result.next()) {
											if (result.getInt("guildmaster") > 0) {
												src.sendMessage(Text.of(TextColors.GREEN, "[Guild] Guild hall \"" + hallName + "\" is sold already!"));
												conn.close();
												return CommandResult.success();
											}
												
										}
										PreparedStatement stmtUpdate = conn.prepareStatement("UPDATE halls SET guildmaster = " + Main.guilds.get(u).GetId() + " WHERE name = \"" + hallName + "\"");
										stmtUpdate.executeUpdate();
										conn.close();
										src.sendMessage(Text.of(TextColors.GREEN, "[Guild] Guild hall \"" + hallName + "\" has been bought by you!"));
										Main.guilds.get(u).TakeCoins(Main.halls.get(i).getPrice());
									} catch (SQLException e) {
										e.printStackTrace();
									}
									return CommandResult.success();
								}
								
								src.sendMessage(Text.of(TextColors.RED, "[Guild] Your guild does not have enough funds to buy this guild hall!"));
								return CommandResult.success();
							} else {
								src.sendMessage(Text.of(TextColors.RED, "[Guild] Your are not guild leader."));
								return CommandResult.success();
							}
						}
					}
					
					src.sendMessage(Text.of(TextColors.RED, "[Guild] You are not in guild."));
					return CommandResult.success();
				}
			}
			return CommandResult.success();
		}
	}
}
