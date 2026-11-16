import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/** Java 17+. Demonstrates immutable Money and safe OOP design in one file. */
public class MoneySystemDemo {

    // 1. IMMUTABLE VALUE OBJECT (from the Money PPT)
    static final class Money implements Comparable<Money> {
        private final long minor;                 // paise / cents
        private final Currency currency;

        private Money(long minor, Currency currency) {
            this.minor = minor;
            this.currency = Objects.requireNonNull(currency);
        }

        static Money ofMinor(String currencyCode, long minor) {
            return new Money(minor, Currency.getInstance(currencyCode));
        }

        Money add(Money other) {
            requireSameCurrency(other);
            return new Money(Math.addExact(minor, other.minor), currency);
        }

        Money subtract(Money other) {
            requireSameCurrency(other);
            return new Money(Math.subtractExact(minor, other.minor), currency);
        }

        Money times(long factor) {
            return new Money(Math.multiplyExact(minor, factor), currency);
        }

        Money percentage(int percent, RoundingMode mode) {
            long calculated = BigDecimal.valueOf(minor)
                    .multiply(BigDecimal.valueOf(percent))
                    .divide(BigDecimal.valueOf(100), 0, Objects.requireNonNull(mode))
                    .longValueExact();
            return new Money(calculated, currency);
        }

        List<Money> allocate(int parts) {
            if (parts <= 0) throw new IllegalArgumentException("parts must be positive");
            if (minor < 0) throw new IllegalArgumentException("cannot split negative money");
            long base = minor / parts;
            long remainder = minor % parts;
            List<Money> result = new ArrayList<>();
            for (int i = 0; i < parts; i++) {
                result.add(new Money(base + (i < remainder ? 1 : 0), currency));
            }
            return List.copyOf(result); // immutable list
        }

        private void requireSameCurrency(Money other) {
            Objects.requireNonNull(other, "money must not be null");
            if (!currency.equals(other.currency)) {
                throw new IllegalArgumentException(
                        "Currency mismatch: " + currency + " vs " + other.currency);
            }
        }

        @Override public int compareTo(Money other) {
            requireSameCurrency(other);
            return Long.compare(minor, other.minor);
        }

        @Override public boolean equals(Object other) {
            return other instanceof Money m
                    && minor == m.minor && currency.equals(m.currency);
        }

        @Override public int hashCode() {
            return Objects.hash(minor, currency);
        }

        @Override public String toString() {
            BigDecimal shown = BigDecimal.valueOf(minor, currency.getDefaultFractionDigits());
            return currency.getCurrencyCode() + " " + shown.toPlainString();
        }
    }

    // 2. TYPED IDs (not interchangeable, even though each contains a long)
    record AccountId(long value) {
        AccountId {
            if (value <= 0) throw new IllegalArgumentException("Account ID must be positive");
        }
    }
    record UserId(long value) {}
    record OrderId(long value) {}
    record PaymentId(long value) {}

    // Account is an ENTITY: same ID means same account, balance can change.
    static final class Account {
        private final AccountId id;
        private Money balance;

        Account(AccountId id, Money openingBalance) {
            this.id = Objects.requireNonNull(id);
            this.balance = Objects.requireNonNull(openingBalance);
        }

        void deposit(Money amount) {
            balance = balance.add(amount); // replaces old Money, does not mutate it
        }

        Money balance() { return balance; }

        @Override public boolean equals(Object other) {
            return other instanceof Account a && id.equals(a.id);
        }

        @Override public int hashCode() { return id.hashCode(); }
    }

    // 3. DEFENSIVE COPYING + REJECT ILLEGAL ORDERS
    record Order(OrderId id, UserId owner, List<String> items) {
        Order {
            Objects.requireNonNull(id, "order ID required");
            Objects.requireNonNull(owner, "owner required");
            items = List.copyOf(Objects.requireNonNull(items, "items required"));
            if (items.isEmpty()) throw new IllegalArgumentException("order needs items");
        }
    }

    // 4. LEGAL PAYMENT STATES (Java 17 sealed interface + records)
    sealed interface Payment permits Pending, Paid, Failed {}
    record Pending(PaymentId id, Money amount) implements Payment {
        Pending {
            Objects.requireNonNull(id);
            Objects.requireNonNull(amount);
        }
    }
    record Paid(PaymentId id, Money amount, Instant paidAt) implements Payment {
        Paid {
            Objects.requireNonNull(id);
            Objects.requireNonNull(amount);
            Objects.requireNonNull(paidAt);
        }
    }
    record Failed(PaymentId id, Money amount, String reason) implements Payment {
        Failed {
            Objects.requireNonNull(id);
            Objects.requireNonNull(amount);
            if (reason == null || reason.isBlank())
                throw new IllegalArgumentException("failure reason required");
        }
    }

    static String describe(Payment payment) {
        if (payment instanceof Pending) return "Waiting";
        if (payment instanceof Paid p) return "Paid at " + p.paidAt();
        if (payment instanceof Failed f) return "Failed: " + f.reason();
        throw new IllegalStateException("Unknown payment state");
    }

    public static void main(String[] args) {
        // Money: exact arithmetic, immutability, equality
        Money a = Money.ofMinor("INR", 10050); // INR 100.50
        Money b = Money.ofMinor("INR", 10050);
        Money total = a.add(Money.ofMinor("INR", 2575));
        System.out.println("Original: " + a + ", after adding: " + total);
        System.out.println("a == b: " + (a == b));
        System.out.println("a.equals(b): " + a.equals(b));
        System.out.println("HashSet size: " + new HashSet<>(List.of(a, b)).size());
        System.out.println("GST on INR 9.99: " +
                Money.ofMinor("INR", 999).percentage(18, RoundingMode.HALF_UP));
        System.out.println("Split INR 100 in 3: " + Money.ofMinor("INR", 10000).allocate(3));

        // Entity: Account changes balance but original Money object stays unchanged
        Account account = new Account(new AccountId(101), a);
        account.deposit(Money.ofMinor("INR", 500));
        System.out.println("Account balance: " + account.balance() + ", original a: " + a);

        // Defensive copy: original list can change without affecting the order
        List<String> cart = new ArrayList<>(List.of("Book", "Pen"));
        Order order = new Order(new OrderId(9001), new UserId(7), cart);
        cart.add("Pencil");
        System.out.println("Order items (unchanged): " + order.items());

        // Collections: orders are stored by their typed IDs
        Map<OrderId, Order> orders = new HashMap<>();
        orders.put(order.id(), order);
        System.out.println("Found order: " + orders.get(new OrderId(9001)).items());

        // Payment states: each kind has only the fields it needs
        Payment payment = new Paid(new PaymentId(501), total, Instant.parse("2026-11-16T10:00:00Z"));
        System.out.println("Payment: " + describe(payment));

        // Currency mismatch is rejected
        try {
            a.add(Money.ofMinor("USD", 100));
        } catch (IllegalArgumentException ex) {
            System.out.println("Rejected: " + ex.getMessage());
        }
    }
}
