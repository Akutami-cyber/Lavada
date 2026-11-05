package mainpackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class InventoryData {
    private static final double EPSILON = 0.00001;
    private static final ArrayList<Item> ITEMS = new ArrayList<Item>();

    static {
        ITEMS.add(new Item("INV-001", "Laundry detergent", "Cleaning", 50.0, 10.0, "packs", 20));
        ITEMS.add(new Item("INV-002", "Fabric conditioner", "Cleaning", 30.0, 10.0, "packs", 25));
        ITEMS.add(new Item("INV-003", "Plastic laundry bags", "Packaging", 120, 40, "pcs", 2));
        ITEMS.add(new Item("INV-004", "Bleach", "Cleaning", 200, 40, "20ml", 15));
        ITEMS.add(new Item("INV-005", "Bleach color safe", "Cleaning", 100, 20, "20ml", 18));
    }

    /** Prevents the instances of this static inventory-data utility class. */
    private InventoryData() {
    }

    /** Returns the read-only view of the current inventory items. */
    static List<Item> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    /** Counts the items at or below their reorder levels. */
    static int getLowStockCount() {
        int count = 0;
        for (Item item : ITEMS) {
            if (item.isLowStock()) {
                count++;
            }
        }
        return count;
    }

    /** Validates and registers a new inventory item. */
    static Item addItem(String id, String name, String category, double stock,
            double reorderLevel, String unit, double unitCost) {
        if (findItem(id) != null) {
            throw new IllegalArgumentException("That inventory item ID already exists.");
        }
        validateItemDetails(name, category, stock, reorderLevel, unit, unitCost);
        validateWholePackAmounts(id, stock, reorderLevel);
        Item item = new Item(id, name.trim(), category.trim(), stock, reorderLevel,
            unit.trim(), unitCost);
        ITEMS.add(item);
        return item;
    }

    /** Updates the editable details of an existing inventory item. */
    static synchronized Item updateItem(String id, String name, String category, double stock,
            double reorderLevel, String unit, double unitCost) {
        Item item = findItem(id);
        if (item == null) {
            throw new IllegalArgumentException("That inventory item is no longer available.");
        }
        validateItemDetails(name, category, stock, reorderLevel, unit, unitCost);
        validateWholePackAmounts(id, stock, reorderLevel);
        item.name = name.trim();
        item.category = category.trim();
        item.stock = stock;
        item.reorderLevel = reorderLevel;
        item.unit = unit.trim();
        item.unitCost = unitCost;
        return item;
    }

    /** Removes the item by ID, returning false when the item no longer exists. */
    static synchronized boolean removeItem(String id) {
        Item item = findItem(id);
        return item != null && ITEMS.remove(item);
    }

    /** Validates required text and non-negative quantities shared by add and edit. */
    private static void validateItemDetails(String name, String category, double stock,
            double reorderLevel, String unit, double unitCost) {
        if (name.trim().isEmpty() || category.trim().isEmpty() || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name, category, and unit are required.");
        }
        if (!Double.isFinite(stock) || stock < 0
                || !Double.isFinite(reorderLevel) || reorderLevel < 0
                || !Double.isFinite(unitCost) || unitCost < 0) {
            throw new IllegalArgumentException(
                "Stock, reorder level, and unit cost must be valid non-negative numbers.");
        }
    }

    static boolean usesWholeUnits(String itemId) {
        return "INV-001".equals(itemId) || "INV-002".equals(itemId);
    }

    private static void validateWholePackAmounts(String itemId, double stock,
            double reorderLevel) {
        if (usesWholeUnits(itemId)
                && (stock != Math.floor(stock) || reorderLevel != Math.floor(reorderLevel))) {
            throw new IllegalArgumentException(
                "Laundry detergent and fabric conditioner stock and reorder levels must be whole packs.");
        }
    }

    /** Calculates the inventory cost for the supplied order quantities. */
    static double getUsageCost(Map<String, Double> usage) {
        return getUsageCost(usage, Collections.<String, Double>emptyMap());
    }

    /** Overloaded form that calculates usage cost usi the optional historical unit costs. */
    static double getUsageCost(Map<String, Double> usage, Map<String, Double> unitCosts) {
        double total = 0;
        for (Map.Entry<String, Double> entry : usage.entrySet()) {
            Item item = findItem(entry.getKey());
            if (item == null) {
                throw new IllegalArgumentException(
                    "An inventory item used by this laundry is no longer available.");
            }
            double unitCost = unitCosts.containsKey(entry.getKey())
                ? unitCosts.get(entry.getKey()) : item.unitCost;
            if (!Double.isFinite(unitCost) || unitCost < 0) {
                throw new IllegalArgumentException("The inventory unit cost is invalid.");
            }
            total += entry.getValue() * unitCost;
            if (!Double.isFinite(total)) {
                throw new IllegalArgumentException("The inventory usage cost is too large.");
            }
        }
        return total;
    }

    /** Finds the inventory item by its unique ID. */
    static Item findItem(String id) {
        for (Item item : ITEMS) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    /** Checks if all requested quantities and deducts them as one stock operation. */
    static synchronized void consume(Map<String, Double> usage) {
        Map<String, Double> normalized = validateUsage(usage);
        validateAvailableStock(normalized, Collections.<String, Double>emptyMap());
        applyUsage(normalized, Collections.<String, Double>emptyMap());
    }

    static synchronized void restore(Map<String, Double> usage) {
        Map<String, Double> normalized = validateUsage(usage);
        for (Map.Entry<String, Double> entry : normalized.entrySet()) {
            Item item = findItem(entry.getKey());
            if (item == null || !Double.isFinite(item.stock + entry.getValue())) {
                throw new IllegalArgumentException(
                    "Inventory could not be restored because a stock amount is invalid.");
            }
        }
        for (Map.Entry<String, Double> entry : normalized.entrySet()) {
            findItem(entry.getKey()).stock += entry.getValue();
        }
    }

    /** Adjusts the stock by the difference between an order's old and corrected usage. */
    static synchronized void updateUsage(Map<String, Double> oldUsage, Map<String, Double> newUsage) {
        Map<String, Double> oldValues = validateUsage(oldUsage);
        Map<String, Double> newValues = validateUsage(newUsage);
        validateAvailableStock(newValues, oldValues);
        applyUsage(newValues, oldValues);
    }

    /** Rejects the unknown items and invalid quantities, omitti zero-use entries. */
    private static Map<String, Double> validateUsage(Map<String, Double> usage) {
        Map<String, Double> normalized = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : usage.entrySet()) {
            String itemId = entry.getKey();
            Double amount = entry.getValue();
            if (amount == null || !Double.isFinite(amount) || amount < 0) {
                throw new IllegalArgumentException("Supply quantities must be valid non-negative numbers.");
            }
            if (usesWholeUnits(itemId) && amount != Math.floor(amount)) {
                throw new IllegalArgumentException(
                    "Laundry detergent and fabric conditioner quantities must be whole packs.");
            }
            if (amount > EPSILON) {
                if (findItem(itemId) == null) {
                    throw new IllegalArgumentException(
                        "An inventory item used by this laundry no longer exists.");
                }
                normalized.put(itemId, amount);
            }
        }
        return normalized;
    }

    /** Ensures the additional usage can be met without maki any stock negative. */
    private static void validateAvailableStock(Map<String, Double> newUsage,
            Map<String, Double> oldUsage) {
        for (Item item : ITEMS) {
            double additionalUsage = newUsage.getOrDefault(item.id, 0.0)
                - oldUsage.getOrDefault(item.id, 0.0);
            if (additionalUsage > item.stock + EPSILON) {
                throw new IllegalArgumentException("Not enough " + item.name + ". Available: "
                    + format(item.stock) + " " + item.unit + "; requested additional: "
                    + format(additionalUsage) + " " + item.unit + ".");
            }
        }
    }

    /** Applies the validated usage changes and normalizes tiny floating-point remainders. */
    private static void applyUsage(Map<String, Double> newUsage, Map<String, Double> oldUsage) {
        for (Item item : ITEMS) {
            double change = newUsage.getOrDefault(item.id, 0.0)
                - oldUsage.getOrDefault(item.id, 0.0);
            item.stock -= change;
            if (Math.abs(item.stock) < EPSILON) {
                item.stock = 0;
            }
        }
    }

    /** Formats the stock quantities consistently insufficient-stock messages. */
    private static String format(double value) {
        return String.format("%.2f", value);
    }

    static final class Item {
        private final String id;
        private String name;
        private String category;
        private double reorderLevel;
        private String unit;
        private double stock;
        private double unitCost;

        /** Creates  inventory item with its openi quantity and reorder threshold. */
        private Item(String id, String name, String category, double stock,
                double reorderLevel, String unit, double unitCost) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.stock = stock;
            this.reorderLevel = reorderLevel;
            this.unit = unit;
            this.unitCost = unitCost;
        }

        /** Returns the this inventory item's unique ID. */
        String getId() {
            return id;
        }

        /** Returns the display name of this supply. */
        String getName() {
            return name;
        }

        /** Returns the inventory category for this supply. */
        String getCategory() {
            return category;
        }

        /** Returns the current available quantity. */
        double getStock() {
            return stock;
        }

        /** Returns the threshold used to flag this supply as low stock. */
        double getReorderLevel() {
            return reorderLevel;
        }

        /** Returns the unit used when displayi or consumi this supply. */
        String getUnit() {
            return unit;
        }

        /** Returns the cost charged per unit of this inventory item. */
        double getUnitCost() {
            return unitCost;
        }

        /** Checks if current quantity has reached the reorder threshold. */
        boolean isLowStock() {
            return stock <= reorderLevel + EPSILON;
        }
    }
}




