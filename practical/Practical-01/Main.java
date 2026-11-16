//Object
class AccountNumber {
    private final String value;

    public AccountNumber(String value) {
        this.value = value;
    }
}

// Value Object
class Money {
    private final double amount;

    public Money(double amount) {
        this.amount = amount;
    }
}

// Value Object
class OverdraftLimit {
    private final Money limit;

    public OverdraftLimit(Money limit) {
        this.limit = limit;
    }
}

// Entity
class LedgerEntry {
    private String entryId;
    private Money amount;

    public LedgerEntry(String entryId, Money amount) {
        this.entryId = entryId;
        this.amount = amount;
    }
}

// Entity
class BankAccount {
    private AccountNumber accountNumber;
    private Money balance;
    private OverdraftLimit overdraftLimit;

    public BankAccount(AccountNumber accountNumber,
                       Money balance,
                       OverdraftLimit overdraftLimit) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.overdraftLimit = overdraftLimit;
    }
}

// Entity
class FundTransfer {
    private String transferId;
    private BankAccount sourceAccount;
    private BankAccount destinationAccount;
    private Money amount;

    public FundTransfer(String transferId,
                        BankAccount sourceAccount,
                        BankAccount destinationAccount,
                        Money amount) {
        this.transferId = transferId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
    }
}

public class Main {
    public static void main(String[] args) {

        // Value Objects
        AccountNumber accNo1 = new AccountNumber("ACC101");
        AccountNumber accNo2 = new AccountNumber("ACC102");

        Money balance1 = new Money(10000);
        Money balance2 = new Money(5000);

        OverdraftLimit limit1 = new OverdraftLimit(new Money(2000));
        OverdraftLimit limit2 = new OverdraftLimit(new Money(1000));

        // Entities
        BankAccount account1 = new BankAccount(accNo1, balance1, limit1);
        BankAccount account2 = new BankAccount(accNo2, balance2, limit2);

        LedgerEntry entry =
                new LedgerEntry("TXN001", new Money(1500));

        FundTransfer transfer =
                new FundTransfer(
                        "FT001",
                        account1,
                        account2,
                        new Money(1500));

        System.out.println("Objects created successfully.");
    }
}