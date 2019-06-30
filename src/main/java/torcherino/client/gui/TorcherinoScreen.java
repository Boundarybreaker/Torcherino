package torcherino.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import torcherino.Torcherino;
import torcherino.api.Tier;
import torcherino.api.TorcherinoAPI;
import torcherino.api.blocks.TorcherinoTileEntity;
import torcherino.client.gui.buttons.FixedSliderButton;
import torcherino.client.gui.buttons.StateButton;
import torcherino.network.Networker;
import torcherino.network.OpenScreenMessage;
import torcherino.network.ValueUpdateMessage;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TorcherinoScreen extends GuiScreen
{
	private static final ResourceLocation BACKGROUND_TEXTURE = Torcherino.resloc("textures/gui/container/torcherino.png");
	private static final int xSize = 245;
	private static final int ySize = 123;
	private final TorcherinoTileEntity tileEntity;
	private final Tier tier;
	private final String title;
	private int guiLeft, guiTop, xRange, zRange, yRange, speed, redstoneMode;

	public TorcherinoScreen(TorcherinoTileEntity tileEntity, ITextComponent title, int xRange, int zRange, int yRange, int speed, int redstoneMode)
	{
		this.tileEntity = tileEntity;
		this.tier = TorcherinoAPI.INSTANCE.getTier(tileEntity.getTierName());
		this.title = title.getFormattedText();
		this.xRange = xRange;
		this.zRange = zRange;
		this.yRange = yRange;
		this.speed = speed;
		this.redstoneMode = redstoneMode;
	}

	@Override protected void initGui()
	{
		super.initGui();
		guiLeft = (this.width - xSize) / 2;
		guiTop = (this.height - ySize) / 2;
		int buttonId = 0;
		this.addButton(new FixedSliderButton(buttonId++, guiLeft + 8, guiTop + 20, 205)
		{
			private int speed;
			private int MAX_SPEED;

			@Override protected void initialise()
			{
				speed = TorcherinoScreen.this.speed;
				MAX_SPEED = TorcherinoScreen.this.tier.getMaxSpeed();
				this.progress = (double) speed / MAX_SPEED;
				this.displayString = new TextComponentTranslation("gui.torcherino.speed_slider", 100 * speed).getFormattedText();
			}

			@Override protected void onValueChange()
			{
				speed = (int) Math.round(progress * MAX_SPEED);
				TorcherinoScreen.this.speed = speed;
				this.progress = (double) speed / MAX_SPEED;
				this.displayString = new TextComponentTranslation("gui.torcherino.speed_slider", 100 * speed).getFormattedText();
			}
		});
		this.addButton(new FixedSliderButton(buttonId++, guiLeft + 8, guiTop + 45, 205)
		{
			private int xRange;
			private int XZ_RANGE;

			@Override protected void initialise()
			{
				xRange = TorcherinoScreen.this.xRange;
				XZ_RANGE = TorcherinoScreen.this.tier.getXZRange();
				this.progress = (double) xRange / XZ_RANGE;
				this.displayString = new TextComponentTranslation("gui.torcherino.x_range", 1 + 2 * xRange).getFormattedText();
			}

			@Override protected void onValueChange()
			{
				xRange = (int) Math.round(progress * XZ_RANGE);
				TorcherinoScreen.this.xRange = xRange;
				this.progress = (double) xRange / XZ_RANGE;
				this.displayString = new TextComponentTranslation("gui.torcherino.x_range", 1 + 2 * xRange).getFormattedText();
			}
		});
		this.addButton(new FixedSliderButton(buttonId++, guiLeft + 8, guiTop + 70, 205)
		{
			private int zRange;
			private int XZ_RANGE;

			@Override protected void initialise()
			{
				zRange = TorcherinoScreen.this.zRange;
				XZ_RANGE = TorcherinoScreen.this.tier.getXZRange();
				this.progress = (double) zRange / XZ_RANGE;
				this.displayString = new TextComponentTranslation("gui.torcherino.z_range", 1 + 2 * zRange).getFormattedText();
			}

			@Override protected void onValueChange()
			{
				zRange = (int) Math.round(progress * XZ_RANGE);
				TorcherinoScreen.this.zRange = zRange;
				this.progress = (double) zRange / XZ_RANGE;
				this.displayString = new TextComponentTranslation("gui.torcherino.z_range", 1 + 2 * zRange).getFormattedText();
			}
		});
		this.addButton(new FixedSliderButton(buttonId++, guiLeft + 8, guiTop + 95, 205)
		{
			private int yRange;
			private int Y_RANGE;

			@Override protected void initialise()
			{
				yRange = TorcherinoScreen.this.yRange;
				Y_RANGE = TorcherinoScreen.this.tier.getYRange();
				this.progress = (double) yRange / Y_RANGE;
				this.displayString = new TextComponentTranslation("gui.torcherino.y_range", 1 + 2 * yRange).getFormattedText();
			}

			@Override protected void onValueChange()
			{
				yRange = (int) Math.round(progress * Y_RANGE);
				TorcherinoScreen.this.yRange = yRange;
				this.progress = (double) yRange / Y_RANGE;
				this.displayString = new TextComponentTranslation("gui.torcherino.y_range", 1 + 2 * yRange).getFormattedText();
			}
		});
		this.addButton(new StateButton(buttonId++, guiLeft + 217, guiTop + 20, width, height, this.redstoneMode)
		{
			private ItemStack renderStack;
			private List<ITextComponent> tooltip;

			@Override protected void setState(int state)
			{
				tooltip = new ArrayList<>();
				TextComponentTranslation modeTranslationKey;
				switch (state)
				{
					case 0:
						renderStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "redstone")));
						modeTranslationKey = new TextComponentTranslation("gui.torcherino.mode.normal");
						break;
					case 1:
						renderStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "redstone_torch")));
						modeTranslationKey = new TextComponentTranslation("gui.torcherino.mode.inverted");
						break;
					case 2:
						renderStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "gunpowder")));
						modeTranslationKey = new TextComponentTranslation("gui.torcherino.mode.ignored");
						break;
					case 3:
						renderStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "redstone_lamp")));
						modeTranslationKey = new TextComponentTranslation("gui.torcherino.mode.off");
						break;
					default:
						renderStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "furnace")));
						modeTranslationKey = new TextComponentTranslation("gui.torcherino.mode.error");
						break;
				}
				tooltip.add(0, new TextComponentTranslation("gui.torcherino.redstone_mode", modeTranslationKey));
				TorcherinoScreen.this.redstoneMode = state;
			}

			@Override protected int getMaxStates()
			{
				return 4;
			}

			@Override protected ItemStack getButtonIcon()
			{
				return renderStack;
			}

			@Override protected List<ITextComponent> populateToolTip()
			{
				return tooltip;
			}
		});
	}

	public void render(int mouseX, int mouseY, float partialTicks)
	{
		this.drawDefaultBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		fontRenderer.drawString(title, guiLeft + (xSize - fontRenderer.getStringWidth(title)) / 2, guiTop + 6, 4210752);
		super.render(mouseX, mouseY, partialTicks);
	}

	@Override public void onResize(Minecraft mcIn, int w, int h)
	{
		super.onResize(mcIn, w, h);
	}

	@Override public void onGuiClosed()
	{
		super.onGuiClosed();
		Networker.INSTANCE.torcherinoChannel.sendToServer(new ValueUpdateMessage(this.tileEntity.getPos(), this.xRange, this.zRange, this.yRange, this.speed, this.redstoneMode));
	}

	@Override public boolean doesGuiPauseGame()
	{
		return false;
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (keyCode == 256 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(InputMappings.getInputByCode(keyCode, scanCode))) mc.player.closeScreen();
		else super.keyPressed(keyCode, scanCode, modifiers);
		return true;
	}

	public static void open(OpenScreenMessage msg)
	{
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.addScheduledTask(() ->
		{
			World world = minecraft.player.world;
			TileEntity tileEntity = world.getTileEntity(msg.pos);
			if (tileEntity instanceof TorcherinoTileEntity)
			{
				TorcherinoScreen screen = new TorcherinoScreen((TorcherinoTileEntity) tileEntity, msg.title, msg.xRange, msg.zRange, msg.yRange, msg.speed, msg.redstoneMode);
				Minecraft.getInstance().mouseHelper.ungrabMouse();
				Minecraft.getInstance().displayGuiScreen(screen);
			}
		});
	}
}
