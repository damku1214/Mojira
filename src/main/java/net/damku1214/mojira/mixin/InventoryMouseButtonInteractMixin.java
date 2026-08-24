package net.damku1214.mojira.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.damku1214.mojira.MojiraConfig;
import net.damku1214.mojira.mixin.accessor.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 *     Fixes MC-577 (Mouse buttons block all inventory controls that are not default)
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     1. {@link AbstractContainerScreen#mouseClicked} deliberately neglects cases where the left and right mouse buttons are clicked. <br>
 *     2. Behavior in {@link AbstractContainerScreen#checkHotbarMouseClicked} neglects the cases where a mouse key is mapped to dropping an item and toggling inventory.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Add case checks for the unsupported actions. <br>
 *     Huge thanks to {@code Anonymous} for part of the solution code;
 *     their code can be found in the comments here: <a href="https://report.bugs.mojang.com/servicedesk/customer/portal/2/MC-577">LINK</a>
 * </p>
 */
@Mixin(AbstractContainerScreen.class)
public abstract class InventoryMouseButtonInteractMixin {
    @Shadow
    protected abstract Slot getHoveredSlot(double x, double y);
    @Shadow
    protected abstract void slotClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput);
    @Shadow
    protected abstract void checkHotbarMouseClicked(MouseButtonEvent event);
    @Shadow
    protected abstract boolean hasClickedOutside(double mx, double my, int xo, int yo);
    @Shadow
    private boolean doubleclick;
    @Shadow
    private Slot lastClickSlot;
    @Shadow
    private boolean skipNextRelease;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Shadow
    protected boolean isQuickCrafting;
    @Final
    @Shadow
    protected AbstractContainerMenu menu;
    @Shadow
    private ItemStack lastQuickMoved;
    @Shadow
    private int quickCraftingButton;
    @Final
    @Shadow
    protected Set<Slot> quickCraftSlots;
    @Shadow
    private int quickCraftingType = 1;
    @Shadow
    protected Slot hoveredSlot;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        @SuppressWarnings("rawtypes")
        AbstractContainerScreen self = (AbstractContainerScreen) (Object) this;
        @SuppressWarnings("resource")
        Minecraft minecraft = ((ScreenAccessor) this).mojira$getMinecraft();
        
        if (mojira$superMouseClicked(self, event, doubleClick)) {
            cir.setReturnValue(true);
        }

        var mouseKey = com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(event.button());
        boolean cloning = minecraft.options.keyPickItem.isActiveAndMatches(mouseKey) && Objects.requireNonNull(minecraft.player).hasInfiniteMaterials();
        Slot slot = getHoveredSlot(event.x(), event.y());
        doubleclick = lastClickSlot == slot && doubleClick;
        skipNextRelease = false;
        if (MojiraConfig.CONFIG.MC_577.get() || (event.button() != InputConstants.MOUSE_BUTTON_LEFT && event.button() != InputConstants.MOUSE_BUTTON_RIGHT && !cloning)) {
            this.checkHotbarMouseClicked(event);
        }
        int xo = this.leftPos;
        int yo = this.topPos;
        boolean clickedOutside = this.hasClickedOutside(event.x(), event.y(), xo, yo);
        if (slot != null) clickedOutside = false;
        int slotId = -1;
        if (slot != null) {
            slotId = slot.index;
        }

        if (clickedOutside) {
            slotId = -999;
        }

        if (slotId != -1 && !this.isQuickCrafting) {
            if (this.menu.getCarried().isEmpty()) {
                if (cloning) {
                    this.slotClicked(slot, slotId, event.button(), ContainerInput.CLONE);
                } else {
                    boolean quickKey = slotId != -999 && event.hasShiftDown();
                    ContainerInput containerInput = ContainerInput.PICKUP;
                    if (quickKey) {
                        this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                        containerInput = ContainerInput.QUICK_MOVE;
                    } else if (slotId == -999) {
                        containerInput = ContainerInput.THROW;
                    }

                    this.slotClicked(slot, slotId, event.button(), containerInput);
                }

                this.skipNextRelease = true;
            } else {
                this.isQuickCrafting = true;
                this.quickCraftingButton = event.button();
                this.quickCraftSlots.clear();
                if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                    this.quickCraftingType = 0;
                } else if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
                    this.quickCraftingType = 1;
                } else if (cloning) {
                    this.quickCraftingType = 2;
                }
            }
        }

        lastClickSlot = slot;
        cir.setReturnValue(true);
    }

    @Unique
    private boolean mojira$superMouseClicked(@SuppressWarnings("rawtypes") AbstractContainerScreen self, MouseButtonEvent event, boolean doubleClick) {
        Optional<GuiEventListener> child = self.getChildAt(event.x(), event.y());
        if (child.isEmpty()) {
            return false;
        }

        GuiEventListener widget = child.get();
        if (widget.mouseClicked(event, doubleClick) && widget.shouldTakeFocusAfterInteraction()) {
            self.setFocused(widget);
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                self.setDragging(true);
            }
        }

        return true;
    }

    @Inject(method = "checkHotbarMouseClicked", at = @At("HEAD"), cancellable = true)
    private void checkHotbarMouseClicked(MouseButtonEvent event, CallbackInfo ci) {
        if (!MojiraConfig.CONFIG.MC_577.get()) return;

        @SuppressWarnings("rawtypes")
        AbstractContainerScreen self = (AbstractContainerScreen) (Object) this;

        Minecraft minecraft = ((ScreenAccessor) this).mojira$getMinecraft();
        if (minecraft.options.keyInventory.matchesMouse(event)) {
            self.onClose();
            ci.cancel();
        }
        if (hoveredSlot != null && menu.getCarried().isEmpty() && minecraft.options.keyDrop.matchesMouse(event)) {
            slotClicked(hoveredSlot, hoveredSlot.index, event.hasControlDown() ? 1 : 0, ContainerInput.THROW);
            ci.cancel();
        }
    }
}
