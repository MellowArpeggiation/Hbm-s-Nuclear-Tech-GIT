package com.hbm.main;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;

public class OrthoHandler {
	
	private static final String KEY_CATEGORY = "key.categories.mineshot";
	private static final float ZOOM_STEP = 0.5f;
	private static final float ROTATE_STEP = 15;
	private static final float SECONDS_PER_TICK = 1f/20f;
	
	private final KeyBinding keyToggle = new KeyBinding("key.mineshot.ortho.toggle", Keyboard.KEY_NUMPAD5, KEY_CATEGORY);
	private final KeyBinding keyZoomIn = new KeyBinding("key.mineshot.ortho.zoom_in", Keyboard.KEY_ADD, KEY_CATEGORY);
	private final KeyBinding keyZoomOut = new KeyBinding("key.mineshot.ortho.zoom_out", Keyboard.KEY_SUBTRACT, KEY_CATEGORY);
	private final KeyBinding keyRotateL = new KeyBinding("key.mineshot.ortho.rotate_l", Keyboard.KEY_NUMPAD4, KEY_CATEGORY);
	private final KeyBinding keyRotateR = new KeyBinding("key.mineshot.ortho.rotate_r", Keyboard.KEY_NUMPAD6, KEY_CATEGORY);
	private final KeyBinding keyRotateU = new KeyBinding("key.mineshot.ortho.rotate_u", Keyboard.KEY_NUMPAD8, KEY_CATEGORY);
	private final KeyBinding keyRotateD = new KeyBinding("key.mineshot.ortho.rotate_d", Keyboard.KEY_NUMPAD2, KEY_CATEGORY);
	private final KeyBinding keyRotateT = new KeyBinding("key.mineshot.ortho.rotate_t", Keyboard.KEY_NUMPAD7, KEY_CATEGORY);
	private final KeyBinding keyRotateF = new KeyBinding("key.mineshot.ortho.rotate_f", Keyboard.KEY_NUMPAD1, KEY_CATEGORY);
	private final KeyBinding keyRotateS = new KeyBinding("key.mineshot.ortho.rotate_s", Keyboard.KEY_NUMPAD3, KEY_CATEGORY);
	private final KeyBinding keyClip = new KeyBinding("key.mineshot.ortho.clip", Keyboard.KEY_MULTIPLY, KEY_CATEGORY);
	
	private boolean enabled;
	private boolean freeCam;
	private boolean clip;
	
	private float zoom;
	private float xRot;
	private float yRot;
	
	private int tick;
	private int tickPrevious;
	private double partialPrevious;

	public OrthoHandler() {
		ClientRegistry.registerKeyBinding(keyToggle);
		ClientRegistry.registerKeyBinding(keyZoomIn);
		ClientRegistry.registerKeyBinding(keyZoomOut);
		ClientRegistry.registerKeyBinding(keyRotateL);
		ClientRegistry.registerKeyBinding(keyRotateR);
		ClientRegistry.registerKeyBinding(keyRotateU);
		ClientRegistry.registerKeyBinding(keyRotateD);
		ClientRegistry.registerKeyBinding(keyRotateT);
		ClientRegistry.registerKeyBinding(keyRotateF);
		ClientRegistry.registerKeyBinding(keyRotateS);
		ClientRegistry.registerKeyBinding(keyClip);
		
		reset();
	}
 
	private void reset() {
		freeCam = false;
		clip = false;
		
		zoom = 8;
		xRot = 30;
		yRot = -45;
		tick = 0;
		tickPrevious = 0;
		partialPrevious = 0;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void enable() {
		if(!enabled) {
			reset();
		}
		
		enabled = true;
	}
	
	public void disable() {
		Minecraft mc = Minecraft.getMinecraft();
		mc.mouseHelper.grabMouseCursor();

		enabled = false;
	}
	
	public void toggle() {
		if(isEnabled()) {
			disable();
		} else {
			enable();
		}
	}
	
	private boolean modifierKeyPressed() {
		return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
	}
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent evt) {
		boolean mod = modifierKeyPressed();
		
		if(keyToggle.getIsKeyPressed()) {
			if(mod) {
				freeCam = !freeCam;
			} else {
				toggle();
			} 
		} else if(keyClip.getIsKeyPressed()) {
			clip = !clip;
		} else if(keyRotateT.getIsKeyPressed()) {
			xRot = mod ? -90 : 90;
			yRot = 0;
		} else if(keyRotateF.getIsKeyPressed()) {
			xRot = 0;
			yRot = mod ? -90 : 90;
		} else if(keyRotateS.getIsKeyPressed()) {
			xRot = 0;
			yRot = mod ? 180 : 0;
		}

		if(mod) {
			// snap values to step units
			xRot -= xRot % ROTATE_STEP;
			yRot -= yRot % ROTATE_STEP;
			zoom -= zoom % ZOOM_STEP;
			
			updateZoomAndRotation(1);
		}
	}
	
	private void updateZoomAndRotation(double multi) {
		if(keyZoomIn.getIsKeyPressed()) {
			zoom *= 1 - ZOOM_STEP * multi;
		} else if(keyZoomOut.getIsKeyPressed()) {
			zoom *= 1 + ZOOM_STEP * multi;
		}
		
		if(keyRotateL.getIsKeyPressed()) {
			yRot += ROTATE_STEP * multi;
		} else if(keyRotateR.getIsKeyPressed()) {
			yRot -= ROTATE_STEP * multi;
		}

		if(keyRotateU.getIsKeyPressed()) {
			xRot += ROTATE_STEP * multi;
		} else if(keyRotateD.getIsKeyPressed()) {
			xRot -= ROTATE_STEP * multi;
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent evt) {
		if(!enabled) {
			return;
		}
		
		if(evt.phase != Phase.START) {
			return;
		}
		
		tick++;
	}
	
	@SubscribeEvent
	public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
		if(!enabled) {
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();

		// leggo of the cursor
		if(Mouse.isGrabbed()) {
			mc.mouseHelper.ungrabMouseCursor();
		}
		
		// update zoom and rotation
		if(!modifierKeyPressed()) {
			int ticksElapsed = tick - tickPrevious;
			double elapsed = ticksElapsed + (event.renderPartialTicks - partialPrevious);
			elapsed *= SECONDS_PER_TICK;
			updateZoomAndRotation(elapsed);
			
			tickPrevious = tick;
			partialPrevious = event.renderPartialTicks;
		}

		float width = zoom * (mc.displayWidth / (float) mc.displayHeight);
		float height = zoom;

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		double cameraZoom = mc.entityRenderer.cameraZoom;
		double cameraOfsX = mc.entityRenderer.cameraYaw;
		double cameraOfsY = mc.entityRenderer.cameraPitch;

		if(cameraZoom != 1) {
			GL11.glTranslated(cameraOfsX, -cameraOfsY, 0);
			GL11.glScaled(cameraZoom, cameraZoom, 1);
		}

		GL11.glOrtho(-width, width, -height, height, clip ? 0 : -9999, 9999);

		if(freeCam) {
			// rotate the orthographic camera with the player view
			xRot = mc.thePlayer.rotationPitch;
			yRot = mc.thePlayer.rotationYaw;
		}

		// set camera rotation
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glRotatef(xRot, 1, 0, 0);
		GL11.glRotatef(yRot + 180.0F, 0, 1, 0);

		// fix particle rotation
		if(!freeCam) {
			float pitch = xRot;
			float yaw = yRot;
			ActiveRenderInfo.rotationX = MathHelper.cos(yaw * (float) Math.PI / 180f);
			ActiveRenderInfo.rotationZ = MathHelper.sin(yaw * (float) Math.PI / 180f);
			ActiveRenderInfo.rotationYZ = -ActiveRenderInfo.rotationZ * MathHelper.sin(pitch * (float) Math.PI / 180f);
			ActiveRenderInfo.rotationXY = ActiveRenderInfo.rotationX * MathHelper.sin(pitch * (float) Math.PI / 180f);
			ActiveRenderInfo.rotationXZ = MathHelper.cos(pitch * (float) Math.PI / 180f);
		}
		
		getOrthoMouseOver(mc, (float)event.renderPartialTicks);
	}

	private void getOrthoMouseOver(Minecraft mc, float partialTicks) {
		if(mc.renderViewEntity == null) return;
		if(mc.theWorld == null) return;

		float width = zoom * (mc.displayWidth / (float) mc.displayHeight);
		float height = zoom;

		// normalise mouse to -1..1
		float mx = ((float)Mouse.getX() / mc.displayWidth - 0.5F) * 2.0F;
		float my = ((float)Mouse.getY() / mc.displayHeight - 0.5F) * 2.0F;
		
		float rz = MathHelper.cos(-yRot * 0.017453292F - (float)Math.PI);
		float rx = MathHelper.sin(-yRot * 0.017453292F - (float)Math.PI);
		float rxz = -MathHelper.cos(-xRot * 0.017453292F);
		float ry = MathHelper.sin(-xRot * 0.017453292F);

		Vec3 look = Vec3.createVectorHelper((double)(rx * rxz), (double)ry, (double)(rz * rxz));
		Vec3 pos = mc.renderViewEntity.getPosition(partialTicks);

		// Move to mouse position
		Vec3 from = pos.addVector((double)(-rz * rxz) * mx * width, 0, (double)(rx * rxz) * mx * width);
		Vec3 to = from.addVector(look.xCoord * 16, look.yCoord * 16, look.zCoord * 16);

		mc.pointedEntity = null;
		mc.objectMouseOver = mc.theWorld.rayTraceBlocks(from, to);
	}

	@SubscribeEvent
	public void onMouseEvent(MouseEvent event) {
		if(!enabled) {
			return;
		}
		
		getOrthoMouseOver(Minecraft.getMinecraft(), 1.0F);
	}

}
