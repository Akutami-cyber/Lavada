# Lavada Laundry Management System

Lavada is a Java Swing desktop application for managing a laundry shop. It has
separate admin and staff screens for viewing customers, laundry orders,
services, inventory, employee information, payments, payroll, and expenses.

## Requirements

- JDK 16 or later
- The `Images` folder, including `Lavada Laundry App Logo.png`, for the window
  and login-screen logo

## Build and run

Open a terminal in the project folder and run:

```powershell
javac *.java
java Main
```

The application opens at the login screen. Shop records, admin and staff
credentials, recovery settings, and lockout state remain in memory and reset
when the program exits. Any previously saved account settings are removed when
the application starts.

The initial sign-in credentials are `admin` / `admin123` and `staff` /
`staff123`. Admin account settings and failed sign-in lockouts reset to these
defaults each time the application starts. During a run, configure the admin
recovery code and all three security questions from the prompted
account-settings dialog. After three failed sign-in attempts, staff accounts
remain locked until an admin reactivates them in Employee Management. Admin
recovery allows three recovery code attempts followed by verification of all
three security answers; if verification fails, contact IT support.

Admins and staff can delete an individual laundry or all laundries directly
from their laundry screens after confirming. Deletion removes linked payment
records without recording a cancellation or refund. Supplies for `In Queue`
laundries are returned to inventory; supplies already used remain deducted.

## Project structure

- `Main.java` starts the application by showing `Login`.
- `Login.java` handles authentication and opens the admin or staff dashboard.
- `AdminAccounts.java` manages in-memory admin credentials, lockout, and
  recovery.
- `AdminDashboard.java` builds the administrator's dashboard and management
  dialogs.
- `DashboardPage.java` defines the lifecycle contract for pages when they
  become visible.
- `AbstractDashboardPage.java` provides shared layout and activation behavior
  for admin module and finance pages.
- `StaffDashboard.java` builds the staff interface and manages customer,
  laundry-order, and payment workflows.
- `ServiceCatalog.java` manages available laundry services.
- `InventoryData.java` manages inventory items and supply usage.
- `StaffAccounts.java` manages in-memory staff account credentials.
- `AccountStorage.java` keeps account settings in memory for the current run.
- `CredentialHash.java` derives and verifies salted credential hashes.
- `StaffPayroll.java` stores and calculates employee payroll.
- `ShopExpenses.java` stores utility and other shop expenses.
- `InputValidation.java` provides reusable input filters and validation.

## Data structures and algorithms (DSA)

The staff pickup workflow demonstrates the required DSA components in
`StaffDashboard.java`:

- **Queue:** `refreshPickup()` puts ready laundries into a FIFO `Queue`
  (`ArrayDeque`) and removes them in queue order for the pickup table.
- **Searching algorithm:** `LaundryData.findOrder()` uses linear search to
  find an order by its ID.
- **Sorting algorithm:** `LaundryData.sortPickupOrdersByReceivedDate()` uses
  insertion sort to list ready laundries by received date, oldest first,
  before they enter the pickup queue.

Each implementation is marked with a `DSA:` comment in the source code.
For a detailed guide to the application's data structures, business-data
workflows, calculations, validation, and algorithms, see
[DATA_STRUCTURES_AND_ALGORITHMS.md](./DATA_STRUCTURES_AND_ALGORITHMS.md).
The guide covers the data structures and data logic across the application,
including inventory, orders, payments, services, payroll, expenses, accounts,
validation, and dashboard summaries.
For the terminal-assessment requirement checklist, rubric, source map, and
specific DSA code locations, see
[docs/Terminal-Requirement-Response.md](../docs/Terminal-Requirement-Response.md).

## Object-oriented programming in this project

This section explains the four main object-oriented programming (OOP)
principles using real code from this project. Each example names the class,
shows the relevant line or method, and explains what the code does.

### Encapsulation

**Class:** `InventoryData.Item` in `InventoryData.java`

**Code:**

```java
static final class Item {
    private final String id;
    private String name;
    private double stock;

    String getId() {
        return id;
    }

    String getName() {
        return name;
    }

    double getStock() {
        return stock;
    }
}
```

**Explanation:** `Item` keeps information about one inventory item. The
`private` keyword prevents other classes from changing `id`, `name`, or `stock`
directly. Other code can read those values through methods such as `getName()`
and `getStock()`. The `final` keyword means the ID is assigned when the item
is created and cannot later be replaced. This protects the item's data and
provides controlled access to it.

**Method:** `isLowStock()` in `InventoryData.Item`

```java
boolean isLowStock() {
    return stock <= reorderLevel + EPSILON;
}
```

**Explanation:** Instead of requiring other classes to calculate whether an
item is low on stock, the item provides a method that checks its own stock
against its reorder level. The details of the check stay with the item data.

**Class and calculation:** `LaundryOrder` in `StaffDashboard.java`

```java
private double getTotalAmount() {
    return Math.round(originalAmount * (100.0 - discountPercentage)) / 100.0;
}
```

**Explanation:** This method calculates the order total after applying its
discount and rounds the result to two decimal places. The order keeps
`originalAmount` and `discountPercentage` private, so callers ask the order for
its total instead of calculating it from or changing those fields directly.
This is another example of encapsulation: the order owns both the pricing data
and the rule for calculating its total.

### Abstraction

**Class:** `DashboardPage` in `DashboardPage.java`

**Code:**

```java
public interface DashboardPage {
    void onPageShown();
}
```

**Explanation:** The interface says that every dashboard page must provide an
`onPageShown()` method. It describes what a page can do without saying how each
page must do it. The dashboard can use this simple contract without needing to
know all the details of each page's layout or refresh work.

**Class and method:** `AbstractDashboardPage` in
`AbstractDashboardPage.java`

```java
public abstract class AbstractDashboardPage extends JPanel
        implements DashboardPage {
    protected AbstractDashboardPage(Color background, int top, int left,
            int bottom, int right) {
        setBackground(background);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
    }

    @Override
    public void onPageShown() {
        revalidate();
        repaint();
    }
}
```

**Explanation:** This abstract class gathers common setup in one place. Its
constructor sets the page background, vertical layout, and padding. Its
`onPageShown()` method refreshes the Swing layout and drawing. A page that
extends this class gets the shared behavior without repeating these setup
steps. The class hides those setup details behind a reusable base class.

**Class and calculation methods:** `InventoryData` in `InventoryData.java`

```java
static double getUsageCost(Map<String, Double> usage) {
    return getUsageCost(usage, Collections.<String, Double>emptyMap());
}

static double getUsageCost(Map<String, Double> usage,
        Map<String, Double> unitCosts) {
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
```

**Explanation:** The first method is a convenient option when there are no
saved historical prices. It delegates the work to the second method, passing
an empty map. The second method multiplies each used quantity by its unit cost
and adds the results. If a historical unit cost is provided, it uses that;
otherwise it uses the item's current cost. Callers can request the calculation
without needing to know those storage and pricing details. The two methods
also demonstrate **overloading**, because they share a name but accept
different parameter lists.

### Inheritance

**Class:** `Login` in `Login.java`

**Code:**

```java
public class Login extends JFrame implements ActionListener {
```

**Explanation:** `extends JFrame` means `Login` inherits window behavior from
Swing's `JFrame`, such as the ability to display a desktop window. `Login` can
then add its own username field, password field, and sign-in controls.

The same project also uses inheritance for its admin pages.

**Classes:** `ModulePage` and `FinancePage` in `AdminDashboard.java`

```java
private static final class ModulePage extends AbstractDashboardPage {
    private ModulePage() {
        super(CANVAS, 26, 30, 30, 30);
    }
}
```

**Explanation:** `ModulePage` extends `AbstractDashboardPage`, so it inherits
the common page layout and `onPageShown()` behavior. Its constructor passes the
background and padding values to the parent constructor using `super(...)`.
The finance page uses the same parent class and can add finance-specific
behavior. (`implements ActionListener` in the `Login` declaration means that
Login promises to handle button events; implementing an interface is not the
same as extending a class.)

### Polymorphism

**Class and method:** `FinancePage.onPageShown()` in `AdminDashboard.java`

```java
@Override
public void onPageShown() {
    super.onPageShown();
    refreshFinanceMetrics();
}
```

**Explanation:** `FinancePage` overrides the page method. It first runs the
shared behavior with `super.onPageShown()`, then refreshes the finance totals.
This gives the finance page behavior specific to its role while keeping the
same method name used by other dashboard pages.

**Calling code:** `AdminDashboard.java`

```java
DashboardPage page = dashboardPages.get(section);
if (page != null) {
    page.onPageShown();
}
```

**Explanation:** The variable is declared as the general `DashboardPage`
interface, but it refers to a real page object. When Java runs
`page.onPageShown()`, it selects the method for that actual object. A finance
page runs its override and refreshes its totals; a module page uses the shared
implementation from `AbstractDashboardPage`. One call works with different
page types, which is runtime polymorphism.

**Another related example: calculation method overloading in
`InventoryData.java`**

```java
static double getUsageCost(Map<String, Double> usage)
static double getUsageCost(Map<String, Double> usage,
        Map<String, Double> unitCosts)
```

**Explanation:** Both calculation methods are named `getUsageCost`, but one
takes a usage map and the other takes both a usage map and a price map. Java
chooses the matching method from the arguments. This is **overloading**, a
form of compile-time polymorphism. Unlike `createMetricCard`, this example
shows overloading in real business logic.

**Important distinction:** The project overloads a calculation method, but it
does not override an inherited calculation method. The `FinancePage` override
is a page-activation callback; it calls `refreshFinanceMetrics()`, which
calculates and displays sales, expenses, profit, and payroll. In other words,
the calculation examples are encapsulation (`LaundryOrder.getTotalAmount()`)
and overloading (`InventoryData.getUsageCost(...)`), while overriding is
demonstrated by the dashboard page behavior.

### Summary

- **Encapsulation:** `InventoryData.Item` protects its fields and provides
  methods to read or use its data.
- **Abstraction:** `DashboardPage` and `AbstractDashboardPage` provide a simple
  page contract and reusable behavior.
- **Inheritance:** `Login` extends `JFrame`, and the admin page classes extend
  `AbstractDashboardPage` to reuse behavior.
- **Polymorphism:** dashboard pages respond to `onPageShown()` according to
  their actual class, and the two `getUsageCost(...)` calculation methods are
  overloaded for different inputs. The calculation methods are overloaded;
  the finance page callback is overridden.