package commands.selection;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.extent.Extent;

public class SelectorSpawnItemCommand implements CommandExecutor {
	private ItemStack generateSelectionTool() {
		ItemStack tool = ItemStack.builder()
		        .itemType(ItemTypes.GOLDEN_AXE).build();
		tool.offer(Keys.DISPLAY_NAME, Text.of(
				TextColors.YELLOW, "Hall Selector Axe"));
		tool.offer(Keys.UNBREAKABLE, true);
		return tool;
	}
	
	private ItemStack generateTeleportTool() {
		ItemStack tool = ItemStack.builder()
						          .itemType(ItemTypes.GOLDEN_SWORD)
						          .build();
		tool.offer(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Teleport Pointer Tool"));
		tool.offer(Keys.UNBREAKABLE, true);
		return tool;
	}
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		} else {
			Player p = (Player)src;
			Extent extent = p.getLocation().getExtent();
			Entity item = extent.createEntity(EntityTypes.ITEM, p.getLocation().getPosition());
		    item.offer(Keys.REPRESENTED_ITEM, this.generateSelectionTool().createSnapshot());
		    
		    Entity tpitem = extent.createEntity(EntityTypes.ITEM, p.getLocation().getPosition());
		    tpitem.offer(Keys.REPRESENTED_ITEM, this.generateTeleportTool().createSnapshot());
		    
		    try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
		        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
		        extent.spawnEntity(item);
		        extent.spawnEntity(tpitem);
		    }
		}
		return CommandResult.success();
	}
}
