package nova.wrapper.mc1710.backward.gui;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import nova.core.gui.Gui;
import nova.core.gui.GuiConstraints;
import nova.core.gui.factory.GuiFactory;
import nova.core.util.transform.Vector3i;
import nova.wrapper.mc1710.backward.entity.BWEntityPlayer;
import nova.wrapper.mc1710.launcher.NovaMinecraft;
import cpw.mods.fml.common.network.IGuiHandler;

public class MCGuiFactory extends GuiFactory {

	private static Optional<Gui> guiToOpen = Optional.empty();
	
	@Override
	public void bind(Gui gui, GuiConstraints constraints) {
		BWEntityPlayer player = (BWEntityPlayer) constraints.player;
		Vector3i pos = constraints.position;
		guiToOpen = Optional.of(gui);
		player.entity.openGui(NovaMinecraft.id, 0, player.entity.getEntityWorld(), pos.x, pos.y, pos.z);
	}

	@Override
	protected void unbind(Gui gui) {
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	
	public static class GuiHandler implements IGuiHandler {

		@Override
		public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {	
			if(guiToOpen.isPresent()) {
				// TODO Container implementation
				guiToOpen = Optional.empty();
			}
			return null;
		}

		@Override
		public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
			if(guiToOpen.isPresent()) {
				guiToOpen = Optional.empty();
				return guiToOpen.get().getNative();
			}
			return null;
		}	
	}
}
