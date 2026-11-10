// Value Object
class UserIdentifier {
    private final String id;

    public UserIdentifier(String id) {
        this.id = id;
    }
}

// Value Object
class EmailAddress {
    private final String email;

    public EmailAddress(String email) {
        this.email = email;
    }
}

// Value Object
class PhoneNumber {
    private final String number;

    public PhoneNumber(String number) {
        this.number = number;
    }
}

// Value Object
class PostalAddress {
    private String street;
    private String city;
    private String postalCode;

    public PostalAddress(String street, String city, String postalCode) {
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
    }
}

// Entity
class UserAccount {
    private UserIdentifier userId;
    private EmailAddress email;
    private PhoneNumber phone;
    private PostalAddress address;

    public UserAccount(UserIdentifier userId,
                       EmailAddress email,
                       PhoneNumber phone,
                       PostalAddress address) {
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}

// Entity
class VerificationToken {
    private String tokenId;
    private String tokenValue;

    public VerificationToken(String tokenId, String tokenValue) {
        this.tokenId = tokenId;
        this.tokenValue = tokenValue;
    }
}

public class Email {
    public static void main(String[] args) {

        UserIdentifier userId =
                new UserIdentifier("U101");

        EmailAddress email =
                new EmailAddress("user@gmail.com");

        PhoneNumber phone =
                new PhoneNumber("9876543210");

        PostalAddress address =
                new PostalAddress(
                        "Main Road",
                        "Hyderabad",
                        "500001");

        UserAccount user =
                new UserAccount(
                        userId,
                        email,
                        phone,
                        address);

        VerificationToken token =
                new VerificationToken(
                        "T001",
                        "ABC123XYZ");

        System.out.println("User account created successfully.");
    }
}