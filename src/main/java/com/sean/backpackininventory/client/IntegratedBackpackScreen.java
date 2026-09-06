package com.sean.backpackininventory.client;

import com.sean.backpackininventory.menu.BackpackDescriptor;
import com.sean.backpackininventory.menu.IntegratedBackpackMenu;
import com.sean.backpackininventory.menu.RecipeBookAdapter;
import com.sean.backpackininventory.network.SelectBackpackPayload;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.fml.ModList;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.client.KeybindHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.IBackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.SBPTranslationHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackOpenPayload;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;

public final class IntegratedBackpackScreen extends StorageScreenBase<IntegratedBackpackMenu>
        implements IBackpackScreen, RecipeUpdateListener {
    static final int VANILLA_WIDTH = 176;
    static final int VANILLA_HEIGHT = 166;
    // Closed upgrade tabs are 21 pixels wide, while an opened module can expand to
    // roughly a hundred pixels. Treat that area as a shared middle corridor between
    // the two inventories so module controls never render beneath the vanilla panel.
    private static final int MIN_PANEL_GAP = 25;
    private static final int PREFERRED_PANEL_GAP = 116;
    private static final int MIN_EDGE_MARGIN = 8;
    private static final int CURIOS_MAX_ROWS = 8;
    private static final int CURIOS_MAX_COLUMNS = 5;
    private static final ResourceLocation CURIOS_INVENTORY = ResourceLocation.fromNamespaceAndPath(
            "curios", "textures/gui/curios/inventory.png");
    private final RecipeBookComponent recipeBook = new RecipeBookComponent();
    private RecipeBookAdapter recipeBookAdapter;
    private boolean recipeBookInitialized;
    private int playerPanelOffsetX;
    private int playerPanelOffsetY;
    private int storageLayoutWidth;
    private int panelGap;
    private boolean curiosOpen;
    private int curiosPage;
    private Button curiosPreviousPage;
    private Button curiosNextPage;

    public IntegratedBackpackScreen(IntegratedBackpackMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        curiosPreviousPage = null;
        curiosNextPage = null;
        curiosPage = ClientPreferences.rememberedCuriosPage;
        int actualWidth = width;
        int largestFittingGap = actualWidth - imageWidth - VANILLA_WIDTH - 2 * MIN_EDGE_MARGIN;
        panelGap = Math.max(MIN_PANEL_GAP, Math.min(PREFERRED_PANEL_GAP, largestFittingGap));
        curiosPage = drawerLayout().page();
        int combinedWidth = imageWidth + panelGap + VANILLA_WIDTH;
        int storageLeft = (actualWidth - combinedWidth) / 2;
        int vanillaLeft = storageLeft + imageWidth + panelGap;
        int vanillaTop = (height - VANILLA_HEIGHT) / 2;
        int storageTop = (height - imageHeight) / 2;
        playerPanelOffsetX = vanillaLeft - storageLeft;
        playerPanelOffsetY = vanillaTop - storageTop;
        storageLayoutWidth = storageLeft * 2 + imageWidth;
        updatePlayerSlotsPositions();
        updateExtraSlotsPositions();
        width = storageLayoutWidth;
        try {
            super.init();
        } finally {
            width = actualWidth;
        }
        // StorageScreenBase uses this label as the anchor for its transfer buttons. The buttons
        // have already been positioned by super.init(); vanilla's inventory panel does not draw
        // an inventory title, so keep the inherited label out of the player-model area.
        inventoryLabelY = -10_000;
        initRecipeBook();
        int manageX = leftPos + playerPanelOffsetX;
        int manageY = topPos + playerPanelOffsetY - 20;
        addRenderableWidget(Button.builder(Component.translatable("gui.backpackininventory.manage"), b -> manageBackpack())
                .tooltip(Tooltip.create(Component.translatable("gui.backpackininventory.locked")))
                .bounds(manageX, manageY, 104, 18).build());
        if (ModList.get().isLoaded("curios")) {
            CuriosClientCompat.createInventoryButton(this, leftPos + playerPanelOffsetX,
                            topPos + playerPanelOffsetY, this::toggleCuriosDrawer)
                    .ifPresent(this::addRenderableWidget);
            initCuriosPageButtons();
        }
        List<BackpackDescriptor> backpacks = integratedMenu().backpacks();
        if (backpacks.size() < 2) {
            return;
        }
        int right = leftPos + playerPanelOffsetX;
        int playerTop = topPos + playerPanelOffsetY;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> selectRelative(-1))
                .tooltip(Tooltip.create(Component.translatable("gui.backpackininventory.previous_backpack")))
                .bounds(right + 132, playerTop + 3, 18, 14).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> selectRelative(1))
                .tooltip(Tooltip.create(Component.translatable("gui.backpackininventory.next_backpack")))
                .bounds(right + 152, playerTop + 3, 18, 14).build());
    }

    private void initRecipeBook() {
        int right = leftPos + playerPanelOffsetX;
        int playerTop = topPos + playerPanelOffsetY;
        int desiredBookX = right + VANILLA_WIDTH + MIN_PANEL_GAP;
        if (desiredBookX + RecipeBookComponent.IMAGE_WIDTH > width) {
            desiredBookX = right + 14;
        }
        int virtualWidth = 2 * (desiredBookX + 86) + RecipeBookComponent.IMAGE_WIDTH;
        recipeBookAdapter = integratedMenu().recipeBookAdapter();
        recipeBook.init(virtualWidth, height, minecraft, false, recipeBookAdapter);
        minecraft.player.containerMenu = integratedMenu();
        recipeBookInitialized = true;
        addRenderableWidget(new ImageButton(right + 104, playerTop + 61, 20, 18,
                RecipeBookComponent.RECIPE_BUTTON_SPRITES,
                button -> withRecipeBookMenu(recipeBook::toggleVisibility)));
        addWidget(recipeBook);
    }

    @Override
    protected void updatePlayerSlotsPositions() {
        int right = playerPanelOffsetX;
        int firstPlayerSlot = getMenu().getNumberOfStorageInventorySlots();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                Slot slot = getMenu().getSlot(firstPlayerSlot + row * 9 + column);
                slot.x = right + 8 + column * 18;
                slot.y = playerPanelOffsetY + 84 + row * 18;
            }
        }
        for (int column = 0; column < 9; column++) {
            Slot slot = getMenu().getSlot(firstPlayerSlot + 27 + column);
            slot.x = right + 8 + column * 18;
            slot.y = playerPanelOffsetY + 142;
        }
        inventoryLabelX = right + 8;
        inventoryLabelY = playerPanelOffsetY + 74;
    }

    @Override
    protected void updateExtraSlotsPositions() {
        int right = playerPanelOffsetX;
        int start = getMenu().vanillaExtraStart();
        getMenu().getSlot(start).x = right + 154;
        getMenu().getSlot(start).y = playerPanelOffsetY + 28;
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                Slot slot = getMenu().getSlot(start + 1 + row * 2 + column);
                slot.x = right + 98 + column * 18;
                slot.y = playerPanelOffsetY + 18 + row * 18;
            }
        }
        for (int armor = 0; armor < 4; armor++) {
            Slot slot = getMenu().getSlot(start + 5 + armor);
            slot.x = right + 8;
            slot.y = playerPanelOffsetY + 8 + armor * 18;
        }
        getMenu().getSlot(start + 9).x = right + 77;
        getMenu().getSlot(start + 9).y = playerPanelOffsetY + 62;
        updateCuriosSlotsPositions();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int actualWidth = width;
        width = storageLayoutWidth;
        try {
            super.renderBg(graphics, partialTick, mouseX, mouseY);
        } finally {
            width = actualWidth;
        }
        int right = leftPos + playerPanelOffsetX;
        int playerTop = topPos + playerPanelOffsetY;
        renderCuriosPanel(graphics, right, playerTop);
        graphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, right, playerTop, 0, 0, VANILLA_WIDTH, VANILLA_HEIGHT);
        if (minecraft != null && minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, right + 26, playerTop + 8, right + 75, playerTop + 78,
                    30, 0.0625F, mouseX, mouseY, minecraft.player);
        }
        selectedDescriptor().ifPresent(descriptor -> graphics.renderItem(descriptor.displayStack(), right + 110, playerTop + 2));
    }

    @Override
    public void drawInventoryBg(GuiGraphics graphics, int x, int y, ResourceLocation texture) {
        // Sophisticated Core's standard background always appends its own 97-pixel player
        // inventory. Those slots live in the vanilla panel here, so render only the title,
        // storage area and bottom border of the Sophisticated texture.
        final int headerHeight = 17;
        final int storageHeight = imageHeight - HEIGHT_WITHOUT_STORAGE_SLOTS;
        graphics.blit(texture, x, y, 0, 0, imageWidth, headerHeight, 256, 256);

        int drawn = 0;
        while (drawn < storageHeight) {
            int chunk = Math.min(150, storageHeight - drawn);
            graphics.blit(texture, x, y + headerHeight + drawn, 0, headerHeight, imageWidth, chunk, 256, 256);
            drawn += chunk;
        }
        graphics.blit(texture, x, y + headerHeight + storageHeight, 0, 252, imageWidth, 4, 256, 256);
    }

    private IntegratedBackpackMenu integratedMenu() {
        return (IntegratedBackpackMenu) menu;
    }

    private void toggleCuriosDrawer() {
        if (!curiosOpen && getUpgradeSettingsControl() != null
                && getUpgradeSettingsControl().getOpenTab().isPresent()) {
            getUpgradeSettingsControl().getOpenTab().get().close();
        }
        curiosOpen = !curiosOpen;
        updateCuriosSlotsPositions();
        updateCuriosPageButtons();
    }

    private void manageBackpack() {
        for (int i = 0; i < integratedMenu().curiosSlotCount(); i++) {
            Slot slot = getMenu().getSlot(integratedMenu().curiosStart() + i);
            if (integratedMenu().isSelectedBackpack(slot.getItem())) {
                CuriosClientCompat.openManagementScreen(getMenu().getCarried());
                return;
            }
        }
        openVanillaInventoryForBackpackMove();
    }

    private DrawerLayout drawerLayout() {
        return DrawerLayout.calculate(integratedMenu().curiosSlotCount(), panelGap, curiosPage);
    }

    private void initCuriosPageButtons() {
        if (curiosPageCount() <= 1) {
            return;
        }
        curiosPreviousPage = addRenderableWidget(Button.builder(Component.literal("<"), button -> changeCuriosPage(-1))
                .tooltip(Tooltip.create(Component.translatable("gui.backpackininventory.previous_curios")))
                .bounds(0, 0, 16, 14).build());
        curiosNextPage = addRenderableWidget(Button.builder(Component.literal(">"), button -> changeCuriosPage(1))
                .tooltip(Tooltip.create(Component.translatable("gui.backpackininventory.next_curios")))
                .bounds(0, 0, 16, 14).build());
        updateCuriosPageButtons();
    }

    private void changeCuriosPage(int direction) {
        curiosPage = Math.floorMod(curiosPage + direction, curiosPageCount());
        ClientPreferences.rememberedCuriosPage = curiosPage;
        updateCuriosSlotsPositions();
        updateCuriosPageButtons();
    }

    private int curiosColumnsPerPage() {
        return drawerLayout().columns();
    }

    private int curiosPageCapacity() {
        return curiosColumnsPerPage() * CURIOS_MAX_ROWS;
    }

    private int curiosPageCount() {
        int count = integratedMenu().curiosSlotCount();
        return Math.max(1, (count + curiosPageCapacity() - 1) / curiosPageCapacity());
    }

    private int curiosVisibleSlotCount() {
        int first = curiosPage * curiosPageCapacity();
        return Math.max(0, Math.min(curiosPageCapacity(), integratedMenu().curiosSlotCount() - first));
    }

    private int curiosVisibleColumns() {
        int count = curiosVisibleSlotCount();
        return Math.max(1, Math.min(curiosColumnsPerPage(),
                (count + CURIOS_MAX_ROWS - 1) / CURIOS_MAX_ROWS));
    }

    private void updateCuriosPageButtons() {
        if (curiosPreviousPage == null || curiosNextPage == null) {
            return;
        }
        int pages = curiosPageCount();
        curiosPage = Math.min(curiosPage, pages - 1);
        boolean visible = curiosOpen && pages > 1;
        curiosPreviousPage.visible = visible;
        curiosNextPage.visible = visible;
        if (!visible) {
            return;
        }
        int playerLeft = leftPos + playerPanelOffsetX;
        int panelLeft = playerLeft - 7 - curiosVisibleColumns() * 18;
        int y = topPos + playerPanelOffsetY - 16;
        curiosPreviousPage.setPosition(panelLeft, y);
        curiosNextPage.setPosition(playerLeft - 24, y);
    }

    private void updateCuriosSlotsPositions() {
        int count = integratedMenu().curiosSlotCount();
        if (count == 0) {
            return;
        }
        curiosPage = drawerLayout().page();
        int firstVisible = curiosPage * curiosPageCapacity();
        int visibleCount = curiosVisibleSlotCount();
        int columns = curiosVisibleColumns();
        int panelWidth = 14 + columns * 18;
        for (int offset = 0; offset < count; offset++) {
            Slot slot = getMenu().getSlot(integratedMenu().curiosStart() + offset);
            int visibleOffset = offset - firstVisible;
            if (!curiosOpen || visibleOffset < 0 || visibleOffset >= visibleCount) {
                slot.x = -2_000;
                slot.y = -2_000;
                continue;
            }
            int column = visibleOffset % columns;
            int row = visibleOffset / columns;
            slot.x = playerPanelOffsetX + 7 + column * 18 - panelWidth;
            slot.y = playerPanelOffsetY + 8 + row * 18;
        }
    }

    private void renderCuriosPanel(GuiGraphics graphics, int playerLeft, int playerTop) {
        if (!curiosOpen || integratedMenu().curiosSlotCount() == 0) {
            return;
        }
        int count = curiosVisibleSlotCount();
        int columns = curiosVisibleColumns();
        int rows = (count + columns - 1) / columns;
        if (curiosPageCount() > 1) {
            graphics.drawCenteredString(font, (curiosPage + 1) + "/" + curiosPageCount(),
                    playerLeft - 7 - columns * 9, playerTop - 27, 0xffffffff);
        }
        int panelX = playerLeft - 33 - (columns - 1) * 18;
        for (int column = 0; column < columns; column++) {
            int bodyHeight = 7 + rows * 18;
            // Curios stores these vertical panel strips horizontally in its texture.
            // The first strip begins at U=91 and the overlapping continuation strips
            // begin at U=98; V spans from 0 through the bottom cap at 159.
            int textureX = column == 0 ? 91 : 98;
            graphics.blit(CURIOS_INVENTORY, panelX, playerTop, textureX, 0, 25, bodyHeight);
            graphics.blit(CURIOS_INVENTORY, panelX, playerTop + bodyHeight, textureX, 159, 25, 7);
            panelX += column == 0 ? 25 : 18;
        }

        // Curios draws the recessed slot grid as a second texture layer. Keep all
        // columns the same height so the embedded drawer has one continuous bottom
        // edge, even when the final column contains fewer actual slots.
        int gridX = playerLeft + 6 - (14 + columns * 18);
        for (int column = 0; column < columns; column++) {
            graphics.blit(CURIOS_INVENTORY, gridX + column * 18, playerTop + 7,
                    7, 7, 18, rows * 18);
        }
    }

    private void selectRelative(int direction) {
        List<BackpackDescriptor> backpacks = integratedMenu().backpacks();
        var currentUuid = integratedMenu().getStorageWrapper().getContentsUuid();
        int current = 0;
        if (currentUuid.isPresent()) {
            for (int i = 0; i < backpacks.size(); i++) {
                if (backpacks.get(i).uuid().equals(currentUuid.get())) {
                    current = i;
                    break;
                }
            }
        }
        int target = Math.floorMod(current + direction, backpacks.size());
        PacketDistributor.sendToServer(new SelectBackpackPayload(backpacks.get(target).uuid()));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (recipeBookInitialized && withRecipeBookMenuResult(() -> recipeBook.keyPressed(keyCode, scanCode, modifiers))) {
            return true;
        }
        if (isTextBoxFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        boolean backpackKey = KeybindHandler.BACKPACK_OPEN_KEYBIND.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
        if (keyCode == 256 || backpackKey) {
            if (keyCode != 256 && backpackKey && getFocused() != null && !clearFocusedWidget()) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            if (getMenu().isFirstLevelStorage() && (keyCode == 256 || mouseNotOverBackpack())) {
                getMinecraft().player.closeContainer();
                return true;
            } else if (!getMenu().isFirstLevelStorage()) {
                PacketDistributor.sendToServer(new BackpackOpenPayload());
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return recipeBookInitialized && withRecipeBookMenuResult(() -> recipeBook.charTyped(codePoint, modifiers))
                || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (recipeBookInitialized && withRecipeBookMenuResult(() -> recipeBook.mouseClicked(mouseX, mouseY, button))) {
            setFocused(recipeBook);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // Expanded upgrade controls take priority over the shared middle corridor.
        // Collapse the drawer before the two interactive slot areas can overlap.
        if (curiosOpen && getUpgradeSettingsControl() != null
                && getUpgradeSettingsControl().getOpenTab().isPresent()) {
            curiosOpen = false;
            updateCuriosSlotsPositions();
            updateCuriosPageButtons();
        }
        if (recipeBookInitialized) {
            withRecipeBookMenu(recipeBook::tick);
        }
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (shouldReleaseSelectedBackpack(slot, type)) {
            if (isCuriosSlot(slot)) {
                CuriosClientCompat.openManagementScreen(getMenu().getCarried());
            } else {
                openVanillaInventoryForBackpackMove();
            }
            return;
        }
        super.slotClicked(slot, slotId, mouseButton, type);
        if (recipeBookInitialized) {
            withRecipeBookMenu(recipeBook::recipesUpdated);
        }
    }

    private boolean shouldReleaseSelectedBackpack(Slot slot, ClickType type) {
        if (slot == null || (type != ClickType.PICKUP && type != ClickType.QUICK_MOVE)) {
            return false;
        }
        return integratedMenu().isSelectedBackpack(slot.getItem());
    }

    private boolean isCuriosSlot(Slot slot) {
        int index = slot.index;
        return index >= integratedMenu().curiosStart()
                && index < integratedMenu().curiosStart() + integratedMenu().curiosSlotCount();
    }

    private void openVanillaInventoryForBackpackMove() {
        if (getMinecraft().player == null) {
            return;
        }
        // Closing first restores InventoryMenu on both sides. Opening a vanilla screen
        // before that acknowledgement would display slots from a menu the server has
        // already replaced, which is exactly the unsafe state the backing-slot lock
        // exists to prevent.
        getMinecraft().player.closeContainer();
        ClientInventoryInterceptor.openVanilla();
    }

    @Override
    public void recipesUpdated() {
        if (recipeBookInitialized) {
            withRecipeBookMenu(recipeBook::recipesUpdated);
        }
    }

    /**
     * Vanilla stores the adapter directly on RecipeBookComponent, but some
     * recipe-book optimization mixins read and cast LocalPlayer.containerMenu
     * instead. Expose the adapter only for the duration of recipe-book calls so
     * those mixins see the type they require without weakening the real menu.
     */
    private void withRecipeBookMenu(Runnable action) {
        var player = minecraft.player;
        var actual = player.containerMenu;
        player.containerMenu = recipeBookAdapter;
        try {
            action.run();
        } finally {
            if (player.containerMenu == recipeBookAdapter) player.containerMenu = actual;
        }
    }

    private boolean withRecipeBookMenuResult(BooleanSupplier action) {
        final boolean[] result = {false};
        withRecipeBookMenu((Runnable) () -> result[0] = action.getAsBoolean());
        return result[0];
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return recipeBook;
    }

    private boolean mouseNotOverBackpack() {
        Slot selected = getSlotUnderMouse();
        return selected == null || !(selected.getItem().getItem() instanceof BackpackItem);
    }

    @Override
    protected String getStorageSettingsTabTooltip() {
        return SBPTranslationHelper.INSTANCE.translGui("settings.tooltip");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        boolean repairedPlayerSlotHovered = renderSlotsSkippedByStorageScreen(graphics, mouseX, mouseY);
        if (recipeBookInitialized) {
            recipeBook.render(graphics, mouseX, mouseY, partialTick);
            recipeBook.renderGhostRecipe(graphics, leftPos, topPos, false, partialTick);
            recipeBook.renderTooltip(graphics, leftPos, topPos, mouseX, mouseY);
        }
        int selectorX = leftPos + playerPanelOffsetX + 110;
        int selectorY = topPos + playerPanelOffsetY + 2;
        if (mouseX >= selectorX && mouseX < selectorX + 16 && mouseY >= selectorY && mouseY < selectorY + 16) {
            selectedDescriptor().ifPresent(descriptor -> {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(descriptor.displayStack().getHoverName());
                tooltip.add(Component.translatable(descriptor.worn()
                        ? "gui.backpackininventory.worn_backpack"
                        : "gui.backpackininventory.carried_backpack"));
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            });
        }
        if (repairedPlayerSlotHovered) {
            // StorageScreenBase rendered its tooltip pass before the skipped slots were
            // repaired, so repeat that pass only when one of those slots is hovered.
            renderTooltip(graphics, mouseX, mouseY);
        }
        if (getMenu().getNumberOfStorageInventorySlots() == 0 && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.closeContainer();
        }
    }

    private boolean renderSlotsSkippedByStorageScreen(GuiGraphics graphics, int mouseX, int mouseY) {
        // StorageScreenBase assumes the final 36 entries in menu.slots are always the
        // player inventory. Vanilla crafting/equipment and optional Curios slots follow
        // them here, so render the corresponding skipped prefix ourselves.
        int firstPlayerSlot = getMenu().getNumberOfStorageInventorySlots();
        int extraSlotCount = IntegratedBackpackMenu.VANILLA_EXTRA_SLOT_COUNT + integratedMenu().curiosSlotCount();
        int skippedPlayerSlots = Math.min(36, extraSlotCount);
        boolean hovered = false;
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);
        for (int offset = 0; offset < skippedPlayerSlots; offset++) {
            Slot slot = getMenu().getSlot(firstPlayerSlot + offset);
            hovered |= renderRepairedSlot(graphics, slot, mouseX, mouseY);
        }
        int skippedExtraSlots = Math.max(0, extraSlotCount - 36);
        for (int offset = 0; offset < skippedExtraSlots; offset++) {
            Slot slot = getMenu().getSlot(integratedMenu().vanillaExtraStart() + offset);
            hovered |= renderRepairedSlot(graphics, slot, mouseX, mouseY);
        }
        graphics.pose().popPose();
        return hovered;
    }

    private boolean renderRepairedSlot(GuiGraphics graphics, Slot slot, int mouseX, int mouseY) {
        if (!slot.isActive()) {
            return false;
        }
        renderSlot(graphics, slot);
        if (!isHovering(slot, mouseX, mouseY)) {
            return false;
        }
        hoveredSlot = slot;
        renderSlotHighlight(graphics, slot.x, slot.y, 0, getSlotColor(slot.index));
        return true;
    }

    @Override protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredSlot != null && integratedMenu().isSelectedBackpack(hoveredSlot.getItem())) {
            graphics.renderComponentTooltip(font, List.of(hoveredSlot.getItem().getHoverName(),
                    Component.translatable("gui.backpackininventory.locked")), mouseX, mouseY);
        } else super.renderTooltip(graphics, mouseX, mouseY);
    }

    private java.util.Optional<BackpackDescriptor> selectedDescriptor() {
        var selectedUuid = integratedMenu().getStorageWrapper().getContentsUuid();
        return selectedUuid.flatMap(uuid -> integratedMenu().backpacks().stream()
                .filter(descriptor -> descriptor.uuid().equals(uuid)).findFirst());
    }

    int playerPanelLeft() {
        return leftPos + playerPanelOffsetX;
    }

    int playerPanelTop() {
        return topPos + playerPanelOffsetY;
    }

    int trashSlotOffsetX() {
        return playerPanelOffsetX + (VANILLA_WIDTH - imageWidth) / 2;
    }

    int trashSlotOffsetY() {
        return playerPanelOffsetY + (VANILLA_HEIGHT - imageHeight) / 2;
    }

}
