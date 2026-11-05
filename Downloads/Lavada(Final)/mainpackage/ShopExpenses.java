package mainpackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ShopExpenses {
    private static double electricityBill;
    private static double waterBill;
    private static double inventoryBill;
    private static List<UtilityBill> additionalUtilityBills = new ArrayList<UtilityBill>();

    private ShopExpenses() {
    }

    static double getElectricityBill() {
        return electricityBill;
    }

    static double getWaterBill() {
        return waterBill;
    }

    static double getInventoryBill() {
        return inventoryBill;
    }

    static double getTotalExpenses() {
        double total = electricityBill + waterBill + inventoryBill;
        for (UtilityBill bill : additionalUtilityBills) {
            total += bill.amount;
        }
        return total;
    }

    static List<UtilityBill> getAdditionalUtilityBills() {
        return Collections.unmodifiableList(
            new ArrayList<UtilityBill>(additionalUtilityBills));
    }

    static void update(double electricity, double water, double inventory,
            List<UtilityBill> otherBills) {
        if (!Double.isFinite(electricity) || electricity < 0
                || !Double.isFinite(water) || water < 0
                || !Double.isFinite(inventory) || inventory < 0
                || !Double.isFinite(electricity + water + inventory)) {
            throw new IllegalArgumentException("Bills must be valid non-negative amounts.");
        }
        List<UtilityBill> validatedBills = new ArrayList<UtilityBill>();
        double total = electricity + water + inventory;
        for (UtilityBill bill : otherBills) {
            if (bill == null || bill.name.trim().isEmpty()) {
                throw new IllegalArgumentException("Each additional utility bill needs a name.");
            }
            if (!Double.isFinite(bill.amount) || bill.amount < 0) {
                throw new IllegalArgumentException(
                    "Additional utility bills must be valid non-negative amounts.");
            }
            total += bill.amount;
            if (!Double.isFinite(total)) {
                throw new IllegalArgumentException("The total bills amount is too large.");
            }
            validatedBills.add(new UtilityBill(bill.name.trim(), bill.amount));
        }
        electricityBill = electricity;
        waterBill = water;
        inventoryBill = inventory;
        additionalUtilityBills = validatedBills;
    }

    static final class UtilityBill {
        private final String name;
        private final double amount;

        UtilityBill(String name, double amount) {
            this.name = name;
            this.amount = amount;
        }

        String getName() {
            return name;
        }

        double getAmount() {
            return amount;
        }
    }
}




