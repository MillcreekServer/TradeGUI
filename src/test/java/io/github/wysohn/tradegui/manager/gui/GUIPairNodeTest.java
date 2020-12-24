package io.github.wysohn.tradegui.manager.gui;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInvsPlugin;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.SlotPos;
import fr.minuskube.inv.opener.InventoryOpener;
import io.github.wysohn.rapidframework3.core.language.ManagerLanguage;
import io.github.wysohn.rapidframework3.core.main.PluginMain;
import io.github.wysohn.rapidframework3.interfaces.ICommandSender;
import io.github.wysohn.tradegui.manager.gui.trade.GUIPairNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({SmartInvsPlugin.class, Bukkit.class})
public class GUIPairNodeTest {

    private PluginMain mockMain;
    private ItemStack[] content1;
    private ItemStack[] content2;
    private Map<String, Double> currencies1;
    private Map<String, Double> currencies2;
    private GUIPairNode.CloseHandle mockCancel;
    private GUIPairNode.TradeHandle mockTrade;
    private GUIPairNode node;
    private InventoryManager mockInventoryManager;
    private ItemFactory mockItemFactory;
    private ManagerLanguage mockLanguageManager;

    @Before
    public void init() throws Exception {
        mockMain = mock(PluginMain.class);
        mockLanguageManager = mock(ManagerLanguage.class);

        when(mockMain.lang()).thenReturn(mockLanguageManager);
        when(mockLanguageManager.parse(any(), any(), any())).thenReturn(new String[0]);
        when(mockLanguageManager.parseFirst(any(ICommandSender.class), any())).thenReturn("");

        ItemStack head1 = new ItemStack(Material.PLAYER_HEAD);
        ItemStack head2 = new ItemStack(Material.PLAYER_HEAD);
        ItemStack button = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        content1 = new ItemStack[GUIPairNode.CONTENTS_ROW * GUIPairNode.CONTENTS_ROW];
        content2 = new ItemStack[GUIPairNode.CONTENTS_ROW * GUIPairNode.CONTENTS_ROW];
        currencies1 = new HashMap<>();
        currencies2 = new HashMap<>();
        mockCancel = mock(GUIPairNode.CloseHandle.class);
        mockTrade = mock(GUIPairNode.TradeHandle.class);

        mockInventoryManager = mock(InventoryManager.class);

        InventoryOpener mockOpener = mock(InventoryOpener.class);
        when(mockInventoryManager.findOpener(any(InventoryType.class))).thenReturn(Optional.of(mockOpener));

        SlotPos slotPos = new SlotPos(6, 9);
        when(mockOpener.defaultSize(any(InventoryType.class))).thenReturn(slotPos);

        PowerMockito.mockStatic(SmartInvsPlugin.class);
        PowerMockito.when(SmartInvsPlugin.class, "manager").thenReturn(mockInventoryManager);

        node = new GUIPairNode(mockMain,
                head1,
                head2,
                button,
                content1,
                content2,
                currencies1,
                currencies2,
                (curr, value) -> value,
                mockCancel,
                mockTrade);
        GUIPairNode mockOtherNode = mock(GUIPairNode.class);
        node.setOtherGUI(mockOtherNode);

        mockItemFactory = mock(ItemFactory.class);

        Server mockServer = mock(Server.class);
        Field field = Bukkit.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(null, mockServer);

        PowerMockito.mock(Bukkit.class);
        PowerMockito.when(Bukkit.class, "getItemFactory").thenReturn(mockItemFactory);

        ItemMeta mockMeta = mock(ItemMeta.class);
        when(mockItemFactory.getItemMeta(any(Material.class))).thenReturn(mockMeta);
    }

    @Test
    public void testInit() {
        Player mockPlayer = mock(Player.class);
        InventoryContents mockInventoryContents = mock(InventoryContents.class);
        node.init(mockPlayer, mockInventoryContents);
    }

    @Test
    public void testUpdate() {
        Player mockPlayer = mock(Player.class);
        InventoryContents mockInventoryContents = mock(InventoryContents.class);
        node.update(mockPlayer, mockInventoryContents);
    }
}