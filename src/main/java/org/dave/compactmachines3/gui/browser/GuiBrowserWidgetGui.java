package org.dave.compactmachines3.gui.browser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.dave.compactmachines3.gui.framework.GUI;
import org.dave.compactmachines3.gui.framework.event.GuiDataUpdatedEvent;
import org.dave.compactmachines3.gui.framework.event.MouseClickEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;
import org.dave.compactmachines3.gui.framework.widgets.*;
import org.dave.compactmachines3.gui.machine.GuiMachineData;
import org.dave.compactmachines3.gui.machine.widgets.WidgetMachinePreview;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.network.*;
import org.dave.compactmachines3.utility.ShrinkingDeviceUtils;

import java.util.List;

/**
 * Layout:
 *
 * ┌──────────────────────────────────────────────────┐
 * │ Favorites │  [<]  Machine Name / #ID  [>]  [★]  │
 * │ sidebar   │  ──────────────────────────────────  │
 * │ (scroll)  │         Preview render               │
 * │           │                                      │
 * │           │  [Rename input field]          [PSD] │
 * └──────────────────────────────────────────────────┘
 *
 * Width: 280, Height: 220
 * Sidebar width: 70
 * Main panel: 210 wide
 */
public class GuiBrowserWidgetGui extends GUI {

    private static final int SIDEBAR_WIDTH = 70;
    private static final int HEADER_HEIGHT = 20;

    private final EntityPlayer player;
    // If opened from a block, we track its pos/dim for toggle-favorite calls
    private final BlockPos browserBlockPos;
    private final int browserBlockDim;
    private final boolean isBlockBrowser;

    private WidgetList favoritesList;
    private WidgetTextBox headerLabel;
    private WidgetButton prevButton;
    private WidgetButton nextButton;
    private WidgetButton starButton;
    private WidgetButton enterButton;
    private WidgetMachinePreview preview;
    private WidgetPanel renamePanel;
    private WidgetButton renameButton;
    private WidgetTextBox machineNameTextBox;

    public GuiBrowserWidgetGui(int width, int height, EntityPlayer player,
            BlockPos browserBlockPos, int browserBlockDim) {
        super(0, 0, width, height);
        this.player = player;
        this.browserBlockPos = browserBlockPos;
        this.browserBlockDim = browserBlockDim;
        this.isBlockBrowser = browserBlockPos != null;

        buildLayout(width, height);
    }

    private void buildLayout(int width, int height) {
        int mainX = SIDEBAR_WIDTH;
        int mainW = width - SIDEBAR_WIDTH;

        // ── Favorites sidebar ──────────────────────────────────────────
        favoritesList = new WidgetList();
        favoritesList.setX(0);
        favoritesList.setY(0);
        favoritesList.setWidth(SIDEBAR_WIDTH);
        favoritesList.setHeight(height);
        populateFavoritesList();
        this.add(favoritesList);

        // ── Header bar ─────────────────────────────────────────────────
        prevButton = new WidgetButton("<");
        prevButton.setX(mainX);
        prevButton.setY(0);
        prevButton.setWidth(16);
        prevButton.setHeight(HEADER_HEIGHT);
        prevButton.addListener(MouseClickEvent.class, (event, widget) -> {
            navigatePrev();
            return WidgetEventResult.HANDLED;
        });
        this.add(prevButton);

        headerLabel = new WidgetTextBox("", 0xFF1f2429) {
            @Override
            public void draw(GuiScreen screen) {
                FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
                String text = getHeaderText();
                int maxW = this.width - 4;
                if (fr.getStringWidth(text) > maxW) {
                    text = fr.trimStringToWidth(text, maxW - fr.getStringWidth("...")) + "...";
                }
                int x = this.width / 2 - fr.getStringWidth(text) / 2;
                fr.drawString(text, x, 6, 0xFF1f2429);
            }
        };
        headerLabel.setX(mainX + 16);
        headerLabel.setY(0);
        headerLabel.setWidth(mainW - 16 - 16 - 20); // leave room for next + star
        headerLabel.setHeight(HEADER_HEIGHT);
        this.add(headerLabel);

        nextButton = new WidgetButton(">");
        nextButton.setX(mainX + mainW - 16 - 20);
        nextButton.setY(0);
        nextButton.setWidth(16);
        nextButton.setHeight(HEADER_HEIGHT);
        nextButton.addListener(MouseClickEvent.class, (event, widget) -> {
            navigateNext();
            return WidgetEventResult.HANDLED;
        });
        this.add(nextButton);

        starButton = new WidgetButton("") {
            @Override
            protected void drawButtonContent(GuiScreen screen, FontRenderer renderer) {
                super.drawButtonContent(screen, renderer);
                // Draw a name tag as the star icon (close enough for now; can replace with texture)
                screen.mc.getRenderItem().renderItemAndEffectIntoGUI(
                        new ItemStack(Items.NAME_TAG), (width - 16) / 2, 2);
            }
        };
        starButton.setX(mainX + mainW - 20);
        starButton.setY(0);
        starButton.setWidth(20);
        starButton.setHeight(HEADER_HEIGHT);
        starButton.setTooltipLines("Toggle Favorite");
        starButton.addListener(MouseClickEvent.class, (event, widget) -> {
            toggleFavorite();
            return WidgetEventResult.HANDLED;
        });
        this.add(starButton);

        // ── Preview ────────────────────────────────────────────────────
        preview = new WidgetMachinePreview();
        preview.setX(mainX);
        preview.setY(HEADER_HEIGHT);
        preview.setWidth(mainW);
        preview.setHeight(height - HEADER_HEIGHT - 25);
        this.add(preview);

        // ── Bottom bar: name display, rename, enter ────────────────────
        int bottomY = height - 24;

        machineNameTextBox = new WidgetTextBox("", 0xFF1f2429);
        machineNameTextBox.setX(mainX + 2);
        machineNameTextBox.setY(bottomY + 4);
        machineNameTextBox.setWidth(mainW - 50);
        this.add(machineNameTextBox);

        // Rename panel (hidden by default)
        renamePanel = new WidgetPanel();
        renamePanel.setX(mainX + 2);
        renamePanel.setY(bottomY);
        renamePanel.setWidth(mainW - 26);
        renamePanel.setHeight(22);
        renamePanel.setVisible(false);

        WidgetInputField renameInput = new WidgetInputField("renameInput");
        renameInput.setWidth(renamePanel.width - 22);
        renameInput.setHeight(20);
        renamePanel.add(renameInput);

        WidgetButton confirmRename = new WidgetButton("OK");
        confirmRename.setX(renamePanel.width - 22);
        confirmRename.setWidth(22);
        confirmRename.setHeight(20);
        confirmRename.addListener(MouseClickEvent.class, (event, widget) -> {
            commitRename(renameInput.getText());
            renamePanel.setVisible(false);
            machineNameTextBox.setVisible(true);
            renameButton.setVisible(true);
            return WidgetEventResult.HANDLED;
        });
        renamePanel.add(confirmRename);
        this.add(renamePanel);

        renameButton = new WidgetButton("") {
            @Override
            protected void drawButtonContent(GuiScreen screen, FontRenderer renderer) {
                super.drawButtonContent(screen, renderer);
                screen.mc.getRenderItem().renderItemAndEffectIntoGUI(
                        new ItemStack(Items.NAME_TAG), (width - 16) / 2, 2);
            }
        };
        renameButton.setX(mainX + mainW - 46);
        renameButton.setY(bottomY);
        renameButton.setWidth(20);
        renameButton.setHeight(22);
        renameButton.setTooltipLines("Rename");
        renameButton.addListener(MouseClickEvent.class, (event, widget) -> {
            MessageMachineList.MachineEntry cur = GuiBrowserData.getCurrentEntry();
            if (cur == null) return WidgetEventResult.CONTINUE_PROCESSING;
            renameInput.setText(cur.name != null ? cur.name : "");
            renamePanel.setVisible(true);
            machineNameTextBox.setVisible(false);
            renameButton.setVisible(false);
            return WidgetEventResult.HANDLED;
        });
        this.add(renameButton);

        enterButton = new WidgetButton("") {
            @Override
            protected void drawButtonContent(GuiScreen screen, FontRenderer renderer) {
                super.drawButtonContent(screen, renderer);
                screen.mc.getRenderItem().renderItemAndEffectIntoGUI(
                        new ItemStack(Itemss.psd), (width - 16) / 2, 2);
            }
        };
        enterButton.setX(mainX + mainW - 24);
        enterButton.setY(bottomY);
        enterButton.setWidth(22);
        enterButton.setHeight(22);
        enterButton.setTooltipLines(I18n.format("gui.compactmachines3.compactsky.enter"));
        enterButton.addListener(MouseClickEvent.class, (event, widget) -> {
            if (!canEnter()) return WidgetEventResult.CONTINUE_PROCESSING;
            MessageMachineList.MachineEntry cur = GuiBrowserData.getCurrentEntry();
            if (cur == null) return WidgetEventResult.CONTINUE_PROCESSING;
            PackageHandler.instance.sendToServer(
                    new MessageRequestMachineAction(cur.id, MessageRequestMachineAction.Action.TRY_TO_ENTER));
            player.closeScreen();
            return WidgetEventResult.HANDLED;
        });
        this.add(enterButton);

        // ── Data update listener ───────────────────────────────────────
        this.addListener(GuiDataUpdatedEvent.class, (event, widget) -> {
            refreshFromGuiBrowserData();
            return WidgetEventResult.CONTINUE_PROCESSING;
        });

        // Initial population if data already arrived
        refreshFromGuiBrowserData();
    }

    // ── Navigation ─────────────────────────────────────────────────────

    private void navigatePrev() {
        if (GuiBrowserData.entries.isEmpty()) return;
        GuiBrowserData.currentIndex = (GuiBrowserData.currentIndex - 1 + GuiBrowserData.entries.size())
                % GuiBrowserData.entries.size();
        loadCurrentMachine();
    }

    private void navigateNext() {
        if (GuiBrowserData.entries.isEmpty()) return;
        GuiBrowserData.currentIndex = (GuiBrowserData.currentIndex + 1) % GuiBrowserData.entries.size();
        loadCurrentMachine();
    }

    private void loadCurrentMachine() {
        MessageMachineList.MachineEntry cur = GuiBrowserData.getCurrentEntry();
        if (cur == null) return;
        // Request preview data for this machine (reuses existing packet)
        PackageHandler.instance.sendToServer(
                new MessageRequestMachineAction(cur.id, MessageRequestMachineAction.Action.REFRESH));
        refreshHeaderAndButtons();
    }

    // ── Favorites ──────────────────────────────────────────────────────

    private void toggleFavorite() {
        MessageMachineList.MachineEntry cur = GuiBrowserData.getCurrentEntry();
        if (cur == null) return;

        MessageToggleFavorite msg = isBlockBrowser
                ? new MessageToggleFavorite(cur.id, browserBlockPos, browserBlockDim)
                : new MessageToggleFavorite(cur.id);
        PackageHandler.instance.sendToServer(msg);
        // Optimistic local update while we wait for server to resend the list
        cur.favorite = !cur.favorite;
        populateFavoritesList();
        refreshHeaderAndButtons();
    }

    private void populateFavoritesList() {
        favoritesList.clear();
        favoritesList.deselect();

        List<MessageMachineList.MachineEntry> favs = GuiBrowserData.getFavorites();
        for (MessageMachineList.MachineEntry e : favs) {
            WidgetFavoriteEntry entry = new WidgetFavoriteEntry(e);
            entry.setWidth(SIDEBAR_WIDTH - 4);
            entry.addListener(MouseClickEvent.class, (event, widget) -> {
                GuiBrowserData.selectById(e.id);
                loadCurrentMachine();
                return WidgetEventResult.HANDLED;
            });
            favoritesList.addListEntry(entry);
        }
    }

    // ── Rename ─────────────────────────────────────────────────────────

    private void commitRename(String newName) {
        MessageMachineList.MachineEntry cur = GuiBrowserData.getCurrentEntry();
        if (cur == null || !GuiMachineData.isOwner(player)) return;
        cur.name = newName;
        machineNameTextBox.setText(newName);
        PackageHandler.instance.sendToServer(new MessageSetMachineName(cur.id, newName));
    }

    // ── UI helpers ─────────────────────────────────────────────────────

    private String getHeaderText() {
        MessageMachineList.MachineEntry cur = GuiBrowserData.getCurrentEntry();
        if (cur == null) return I18n.format("gui.compactmachines3.browser.none");
        return cur.getDisplayName();
    }

    private boolean canEnter() {
        return ShrinkingDeviceUtils.hasShrinkingDeviceInInventory(player)
                && GuiBrowserData.getCurrentEntry() != null
                && GuiMachineData.isAllowedToEnter(player)
                && player.dimension == ConfigurationHandler.Settings.dimensionId;
    }

    private void refreshHeaderAndButtons() {
        enterButton.setVisible(canEnter());
        renameButton.setVisible(GuiMachineData.isOwner(player) && GuiBrowserData.getCurrentEntry() != null);

        MessageMachineList.MachineEntry cur = GuiBrowserData.getCurrentEntry();
        machineNameTextBox.setText(cur != null
                ? (cur.name != null ? cur.name : "") : "");
    }

    private void refreshFromGuiBrowserData() {
        populateFavoritesList();
        refreshHeaderAndButtons();
    }
}
