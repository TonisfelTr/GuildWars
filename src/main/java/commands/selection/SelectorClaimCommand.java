package commands.selection;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import guildunits.GuildHall;
import guildwars.Main;

public class SelectorClaimCommand implements CommandExecutor {	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		} else {
			if (Main.points.get(src.getName()).getFirstPoint() != null && Main.points.get(src.getName()).getSecondPoint() != null && Main.points.get(src.getName()).getTeleportPoint() != null) {
				Location<World> selectedLoc1 = Main.points.get(src.getName()).getFirstPoint();
				Location<World> selectedLoc2 = Main.points.get(src.getName()).getSecondPoint();
				Location<World> teleportLoc = Main.points.get(src.getName()).getTeleportPoint();
				Collection<String> priceArg = args.getAll("price");
				Collection<String> name = args.getAll("hall_name");
				String price = (String) priceArg.toArray()[0];
				String hallName = (String) name.toArray()[0];
				for (int i = 0; i < Main.halls.size(); i++) {
					if (Main.halls.get(i).getName().toUpperCase().equals(hallName.toUpperCase())) {
						src.sendMessage(Text.of(TextColors.RED, "[Guild] Guild hall with this name already exists."));
						return CommandResult.success();
					}
					Location<World> loc1 = Main.halls.get(i).getFirstPos();
					Location<World> loc2 = Main.halls.get(i).getSecondPos();
					boolean InXPos = Main.halls.get(i).CheckExistance(loc1.getX(), loc2.getX(), selectedLoc1.getX()) || Main.halls.get(i).CheckExistance(loc1.getX(), loc2.getX(), selectedLoc2.getX());
					boolean InYPos = Main.halls.get(i).CheckExistance(loc1.getY(), loc2.getY(), selectedLoc1.getY()) || Main.halls.get(i).CheckExistance(loc1.getY(), loc2.getY(), selectedLoc2.getY());
					boolean InZPos = Main.halls.get(i).CheckExistance(loc1.getZ(), loc2.getZ(), selectedLoc1.getZ()) || Main.halls.get(i).CheckExistance(loc1.getZ(), loc2.getZ(), selectedLoc2.getZ());
					if (InXPos && InYPos && InZPos) {
						src.sendMessage(Text.of(TextColors.RED, "[Guild] The selected zone overlaps with the existing one."));
						return CommandResult.success();
					}
				}
				Main.halls.add(new GuildHall(0, hallName, Integer.valueOf(price), selectedLoc1, selectedLoc2, teleportLoc));
				String url = "jdbc:sqlite:config/guildwars/Guilds.db";
				SqlService sql;
				Connection conn;
				try {
					sql = Sponge.getServiceManager().provide(SqlService.class).get();
					conn = sql.getDataSource(url).getConnection();
					PreparedStatement stmtInsert = conn.prepareStatement("INSERT INTO halls (name, price, guildmaster,flocx,flocy,flocz,slocx,slocy,slocz,tp_locx,tp_locy,tp_locz) "
																	                + "VALUES (\"" + hallName + "\", " + price +", 0, " + selectedLoc1.getX() + ", " + selectedLoc1.getY() + ", " + selectedLoc1.getZ() + 
																	                		  ", " + selectedLoc2.getX()  + ", " + selectedLoc2.getY() + ", " + selectedLoc2.getZ() + 
																	                	      ", " + teleportLoc.getX() + ", " + teleportLoc.getY() + ", " + teleportLoc.getZ() + ")");
					stmtInsert.executeUpdate();
					conn.close();
					src.sendMessage(Text.of(TextColors.GREEN, "[Guild] Guild hall \"" + hallName + "\" has been created!"));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				src.sendMessage(Text.of(TextColors.RED, "[Guild] You didn't select zone or teleport point for guild hall."));
				return CommandResult.success();
			}
		}
		return CommandResult.success();
	}
}
