package mainpackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the service collection and exposes focused operations so callers
 * need not know how services are stored, validated, or matched.
 */
final class ServiceCatalog {
    private static final ArrayList<Service> SERVICES = new ArrayList<Service>();

    static {
        SERVICES.add(new Service("SRV-001", "Wash and Dry", 80.0, "per kg", true));
        SERVICES.add(new Service("SRV-002", "Wash-Dry-Fold", 100.0, "per kg", true));
        SERVICES.add(new Service("SRV-003", "Dry Cleaning", 250.0, "per item", true));
        SERVICES.add(new Service("SRV-004", "Blanket Washing", 300.0, "per item", true));
        SERVICES.add(new Service("SRV-005", "Ironing", 50.0, "per item", true));
    }

    private ServiceCatalog() {
    }

    static List<Service> getServices() {
        return Collections.unmodifiableList(SERVICES);
    }

    static List<String> getActiveServiceNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Service service : SERVICES) {
            if (service.active) {
                names.add(service.name);
            }
        }
        return names;
    }

    static Service findById(String id) {
        for (Service service : SERVICES) {
            if (service.id.equals(id)) {
                return service;
            }
        }
        return null;
    }

    static Service findByName(String name) {
        for (Service service : SERVICES) {
            if (service.name.equals(name) || service.aliases.contains(name)) {
                return service;
            }
        }
        return null;
    }

    static Service add(String id, String name, double rate, String unit, boolean active) {
        if (findById(id) != null) {
            throw new IllegalArgumentException("That service ID already exists.");
        }
        validate(name, rate, unit);
        if (findByName(name.trim()) != null) {
            throw new IllegalArgumentException("A service with that name already exists.");
        }
        Service service = new Service(id, name.trim(), rate, unit.trim(), active);
        SERVICES.add(service);
        return service;
    }

    static Service update(String id, String name, double rate, String unit, boolean active) {
        Service service = findById(id);
        if (service == null) {
            throw new IllegalArgumentException("That service is no longer available.");
        }
        validate(name, rate, unit);
        Service duplicate = findByName(name.trim());
        if (duplicate != null && duplicate != service) {
            throw new IllegalArgumentException("A service with that name already exists.");
        }
        if (!service.name.equals(name.trim())) {
            service.aliases.add(service.name);
        }
        service.name = name.trim();
        service.rate = rate;
        service.unit = unit.trim();
        service.active = active;
        return service;
    }

    static boolean remove(String id) {
        Service service = findById(id);
        return service != null && SERVICES.remove(service);
    }

    static boolean matchesOrderName(String id, String name) {
        Service service = findById(id);
        return service != null
            && (service.name.equals(name) || service.aliases.contains(name));
    }

    private static void validate(String name, double rate, String unit) {
        if (name == null || name.trim().isEmpty() || unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Service name and unit are required.");
        }
        if (!Double.isFinite(rate) || rate < 0) {
            throw new IllegalArgumentException("Service price must be a valid non-negative amount.");
        }
    }

    static final class Service {
        private final String id;
        private String name;
        private double rate;
        private String unit;
        private boolean active;
        private final List<String> aliases = new ArrayList<String>();

        private Service(String id, String name, double rate, String unit, boolean active) {
            this.id = id;
            this.name = name;
            this.rate = rate;
            this.unit = unit;
            this.active = active;
        }

        String getId() {
            return id;
        }

        String getName() {
            return name;
        }

        double getRate() {
            return rate;
        }

        String getUnit() {
            return unit;
        }

        boolean isActive() {
            return active;
        }

    }
}




