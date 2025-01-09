package com.spygstudios.chestshop.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.spygstudios.chestshop.ChestShop;
import com.spygstudios.chestshop.config.Message;
import com.spygstudios.spyglib.color.TranslateColor;
import com.spygstudios.spyglib.components.ComponentUtils;
import com.spygstudios.spyglib.inventory.InventoryUtils;
import com.spygstudios.spyglib.location.LocationUtils;

import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.EconomyResponse;

public class Shop {

    @Getter
    private UUID ownerId;

    @Getter
    private int price;

    @Getter
    private int amount;

    @Getter
    private Material material;

    @Getter
    private Location chestLocation;

    @Getter
    private Location signLocation;

    @Getter
    private String createdAt;

    @Getter
    @Setter
    private String name;

    @Getter
    private boolean isNotify;

    private ShopFile shopFile;

    @Getter
    private List<UUID> addedPlayers;

    private static final List<Shop> SHOPS = new ArrayList<>();
    private static ChestShop plugin = ChestShop.getInstance();

    public Shop(Player owner, String name, ShopFile shopFile) {
        this(owner.getUniqueId(), name, shopFile);
    }

    public Shop(UUID ownerId, String name, ShopFile shopFile) {
        this.ownerId = ownerId;
        this.name = name;
        this.shopFile = shopFile;
        price = shopFile.getInt("shops." + name + ".price");
        amount = shopFile.getInt("shops." + name + ".amount");
        material = Material.getMaterial(shopFile.getString("shops." + name + ".material"));
        chestLocation = LocationUtils.toLocation(shopFile.getString("shops." + name + ".location"));
        createdAt = shopFile.getString("shops." + name + ".created");
        isNotify = shopFile.getBoolean("shops." + name + ".do-notify");
        addedPlayers = shopFile.getAddedUuids(name);

        SHOPS.add(this);
        setShopSign();
    }

    public int getPriceForEach() {
        return (int) Math.round(((double) price / amount));
    }

    public String getMaterialString() {
        if (material == null) {
            return plugin.getConf().getString("shops.unknown-material");
        }
        String materialString = getMaterial().toString();
        return materialString.length() > 14 ? materialString.substring(0, 14) : materialString;
    }

    public int getSoldItems() {
        return shopFile.getInt("shops." + name + ".sold-items");
    }

    public int getMoneyEarned() {
        return shopFile.getInt("shops." + name + ".money-earned");
    }

    public String getChestLocationString() {
        return chestLocation.getWorld().getName() + ", x: " + chestLocation.getBlockX() + " y: " + chestLocation.getBlockY() + " z: " + chestLocation.getBlockZ();
    }

    public void setMaterial(Material material) {
        this.material = material;
        ShopFile.getShopFile(ownerId).setMaterial(name, material);
        setShopSign();
    }

    public void setAmount(int amount) {
        this.amount = amount;
        ShopFile.getShopFile(ownerId).setAmount(name, amount);
        setShopSign();
    }

    public void setPrice(int price) {
        this.price = price;
        ShopFile.getShopFile(ownerId).setPrice(name, price);
        setShopSign();
    }

    public void setNotify(boolean notify) {
        isNotify = notify;
        shopFile.overwriteSet("shops." + name + ".do-notify", notify);
        shopFile.save();
    }

    public void addPlayer(OfflinePlayer player) {
        addPlayer(player.getUniqueId());
    }

    public void addPlayer(UUID uuid) {
        if (addedPlayers.contains(uuid)) {
            Bukkit.getPlayer(ownerId).sendMessage(ComponentUtils.replaceComponent(Message.PLAYER_ALREADY_ADDED.get(), "%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            return;
        }
        addedPlayers.add(uuid);
        shopFile.addPlayer(uuid, name);
        Bukkit.getPlayer(ownerId).sendMessage(ComponentUtils.replaceComponent(Message.PLAYER_ADDED.get(), "%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
    }

    public void removePlayer(OfflinePlayer player) {
        removePlayer(player.getUniqueId());
    }

    public void removePlayer(UUID uuid) {
        if (!addedPlayers.contains(uuid)) {
            Bukkit.getPlayer(ownerId).sendMessage(ComponentUtils.replaceComponent(Message.PLAYER_NOT_ADDED.get(), "%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
            return;
        }
        addedPlayers.remove(uuid);
        shopFile.removePlayer(uuid, name);
        Bukkit.getPlayer(ownerId).sendMessage(ComponentUtils.replaceComponent(Message.PLAYER_REMOVED.get(), "%player-name%", Bukkit.getOfflinePlayer(uuid).getName()));
    }

    public void setShopSign() throws IllegalArgumentException {
        if (chestLocation.getBlock().getType() != Material.CHEST) {
            removeShop(this);
            throw new IllegalArgumentException("Block is not a chest, Shop removed! Location: " + chestLocation);
        }
        BlockFace chestFacing = getChestFace(chestLocation.getBlock());
        signLocation = chestLocation.clone().add(chestFacing.getModX(), chestFacing.getModY(), chestFacing.getModZ());
        Block signBlock = signLocation.getBlock();
        if (!(signBlock.getBlockData() instanceof WallSign) || signBlock.getType() == Material.AIR) {
            Bukkit.getLogger().warning("Shop sign is not a sign or air, removing shop! " + getName() + " at " + getChestLocationString());
            remove();
            return;
        }

        Sign sign = (Sign) signLocation.getBlock().getState();
        SignSide side = sign.getSide(Side.FRONT);
        for (int i = 0; i < 4; i++) {
            side.line(i, TranslateColor.translate(plugin.getConf().getString("shop.sign.line." + (i + 1)).replace("%owner%", Bukkit.getOfflinePlayer(ownerId).getName())
                    .replace("%amount%", String.valueOf(amount)).replace("%price%", String.valueOf(price)).replace("%material%", getMaterialString())));
        }
        sign.update();
    }

    public void remove() {
        Shop.removeShop(this);
    }

    public boolean isDoubleChest() {

        if (!(chestLocation.getBlock() instanceof Chest chest)) {
            return false;
        }
        Inventory inv = chest.getInventory();
        return inv instanceof DoubleChestInventory;
    }

    public Block getAdjacentChest() {
        for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST }) {
            Block relativeBlock = chestLocation.getBlock().getRelative(face);
            if (relativeBlock.getState() instanceof Chest) {
                return relativeBlock;
            }
        }
        return null;
    }

    public void sell(Player buyer) {
        if (getMaterial() == null || getAmount() == 0) {
            Message.SHOP_SETUP_NEEDED.send(buyer);
            return;
        }
        Chest chest = (Chest) getChestLocation().getBlock().getState();

        int itemCount;
        int itemsLeft;
        itemCount = itemsLeft = InventoryUtils.countItems(chest.getInventory(), getMaterial());
        if (itemCount == 0) {
            Message.SHOP_EMPTY.send(buyer);
            return;
        }
        itemCount = getAmount() > itemCount ? itemCount : getAmount();
        itemsLeft -= itemCount;
        if (!InventoryUtils.hasFreeSlot(buyer)) {
            Message.SHOP_INVENTORY_FULL.send(buyer);
            return;
        }
        int itemPrice = getPriceForEach() * itemCount;
        EconomyResponse response = plugin.getEconomy().withdrawPlayer(buyer, (double) getPrice());
        if (response.transactionSuccess()) {
            ChestShop.getInstance().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(getOwnerId()), (double) itemPrice);

            itemCount = extractItems(buyer, chest, itemCount);

            Message.SHOP_BOUGHT.send(buyer, Map.of("%price%", String.valueOf(itemPrice), "%material%", getMaterial().name(), "%items-left%", String.valueOf(itemsLeft)));
            shopFile.overwriteSet("shops." + getName() + ".sold-items", shopFile.getInt("shops." + getName() + ".sold-items") + itemCount);
            shopFile.overwriteSet("shops." + getName() + ".money-earned", shopFile.getDouble("shops." + getName() + ".money-earned") + itemPrice);
            shopFile.save();
            Player owner = Bukkit.getPlayer(getOwnerId());
            if (isNotify() && owner != null) {
                Message.SHOP_SOLD.send(owner,
                        Map.of("%price%", String.valueOf(itemPrice), "%material%", getMaterial().name(), "%player-name%", buyer.getName(), "%items-left%", String.valueOf(itemsLeft)));
            }
            return;
        }
        Message.NOT_ENOUGH_MONEY.send(buyer, Map.of("%price%", String.valueOf(itemPrice)));
    }

    private int extractItems(Player buyer, Chest chest, int itemCount) {
        while (itemCount > 0) {
            int amountToTransfer = Math.min(itemCount, getMaterial().getMaxStackSize());
            ItemStack stackToAdd = new ItemStack(getMaterial(), amountToTransfer);
            buyer.getInventory().addItem(stackToAdd);
            for (ItemStack chestItem : chest.getInventory().getContents()) {
                if (chestItem != null && chestItem.getType() == getMaterial()) {
                    int chestItemAmount = chestItem.getAmount();
                    int removeAmount = Math.min(amountToTransfer, chestItemAmount);
                    chestItem.setAmount(chestItemAmount - removeAmount); // Csökkentjük a ládában lévő mennyiséget
                    amountToTransfer -= removeAmount; // Csökkentjük az áthelyezendő mennyiséget
                    if (amountToTransfer <= 0) {
                        break;
                    }
                }
            }
            itemCount -= Math.min(itemCount, getMaterial().getMaxStackSize());
        }
        return itemCount;
    }

    public static boolean isDisabledWorld(String worldName) {
        return Bukkit.getWorld(worldName) != null && isDisabledWorld(Bukkit.getWorld(worldName));
    }

    public static boolean isDisabledWorld(World world) {
        for (String worldName : plugin.getConf().getStringList("shops.disabled-worlds")) {
            if (world.getName().equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }

    public static Shop getShop(String name) {
        for (Shop shop : SHOPS) {
            if (shop.getName().equalsIgnoreCase(name)) {
                return shop;
            }
        }
        return null;
    }

    public static List<Shop> getShops(Player owner) {
        return getShops(owner.getUniqueId());
    }

    public static List<Shop> getShops(UUID ownerId) {
        List<Shop> shops = new ArrayList<>();
        for (Shop shop : SHOPS) {
            if (shop.getOwnerId().equals(ownerId)) {
                shops.add(shop);
            }
        }
        return shops;
    }

    public static Shop getShop(Location location) {
        for (Shop shop : SHOPS) {
            if (shop.getChestLocation().equals(location) || shop.getSignLocation().equals(location)) {
                return shop;
            }
        }
        return null;
    }

    public static List<Shop> getShops() {
        return new ArrayList<>(SHOPS);
    }

    public static void removeShop(Shop shop) {
        ShopFile.getShopFile(shop.getOwnerId()).removeShop(shop.getName());
        SHOPS.remove(shop);
    }

    public static BlockFace getChestFace(Block chestBlock) throws IllegalArgumentException {
        if (!(chestBlock.getBlockData() instanceof Directional directional)) {
            throw new IllegalArgumentException("Block is not a chest!");
        }
        return directional.getFacing();
    }

    public static boolean isDoubleChest(Block block) {
        if (!(block.getState() instanceof Chest chest)) {
            return false;
        }
        Inventory inv = chest.getInventory();
        return inv instanceof DoubleChestInventory;
    }

    public static Block getAdjacentChest(Block block) {
        for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST }) {
            Block relativeBlock = block.getRelative(face);
            if (relativeBlock.getState() instanceof Chest) {
                return relativeBlock;
            }
        }
        return null;
    }

    public static boolean isChestFaceFree(Block chestBlock) {
        BlockFace facing = getChestFace(chestBlock);
        Location signLocation = chestBlock.getLocation().clone().add(facing.getModX(), facing.getModY(), facing.getModZ());
        return signLocation.getBlock().getType() == Material.AIR;
    }

}
