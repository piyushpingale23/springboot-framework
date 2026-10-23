# @Primary vs @Qualifier

## Why do we need them?

When Spring finds multiple beans implementing the same interface, it does not know which bean to inject.

Example:

    PaymentService
        |
        +-- CreditCardPaymentService
        |
        +-- UpiPaymentService

Both classes are Spring beans:

    @Service
    class CreditCardPaymentService implements PaymentService

    @Service
    class UpiPaymentService implements PaymentService

Now if we write:

    @Autowired
    private PaymentService paymentService;

Spring sees two possible beans:

    CreditCardPaymentService
    UpiPaymentService

Therefore, Spring gets bean ambiguity and throws:

    NoUniqueBeanDefinitionException

--------------------------------------------------

## @Primary

`@Primary` tells Spring:

    "If you have multiple beans of this type and no specific bean
     is requested, prefer this bean."

Example:

    @Service
    @Primary
    public class CreditCardPaymentService implements PaymentService {
        ...
    }

    @Service
    public class UpiPaymentService implements PaymentService {
        ...
    }

Now:

    @Autowired
    private PaymentService paymentService;

Spring chooses:

    CreditCardPaymentService

because it is marked with `@Primary`.

Think:

    @Primary = DEFAULT CHOICE

--------------------------------------------------

## @Qualifier

`@Qualifier` tells Spring:

    "I specifically want this bean."

Example:

    @Autowired
    @Qualifier("upiPaymentService")
    private PaymentService paymentService;

Spring specifically chooses:

    UpiPaymentService

Even though `CreditCardPaymentService` is marked `@Primary`.

Think:

    @Qualifier = SPECIFIC CHOICE

--------------------------------------------------

## @Primary + @Qualifier together

Suppose:

    CreditCardPaymentService -> @Primary
    UpiPaymentService

And:

    @Autowired
    @Qualifier("upiPaymentService")
    private PaymentService paymentService;

Spring chooses:

    UpiPaymentService

Why?

Because `@Qualifier` explicitly identifies the required bean.

`@Primary` is only used when Spring needs a default choice.

--------------------------------------------------

## Decision Flow

    @Autowired PaymentService
              |
              v
       Multiple beans?
              |
          YES |
              v
       @Qualifier present?
          /          \
        YES          NO
         |            |
         v            v
    Use specified   @Primary
       bean         present?
                    /      \
                  YES       NO
                   |         |
                   v         v
             Use primary   Bean ambiguity
                             exception

--------------------------------------------------

## Bean Name and @Qualifier

By default, Spring creates the bean name using the class name
with the first letter converted to lowercase.

Example:

    Class name:
    UpiPaymentService

    Default bean name:
    upiPaymentService

Therefore:

    @Qualifier("upiPaymentService")

refers to the Spring bean named `upiPaymentService`.

It is NOT the Java class name.

--------------------------------------------------

## Example

    public interface PaymentService {
        void pay();
    }

    @Service
    @Primary
    public class CreditCardPaymentService implements PaymentService {

        public void pay() {
            System.out.println("Paid using Credit Card");
        }
    }

    @Service
    public class UpiPaymentService implements PaymentService {

        public void pay() {
            System.out.println("Paid using UPI");
        }
    }

Without @Qualifier:

    @Autowired
    private PaymentService paymentService;

Output:

    Paid using Credit Card

because CreditCardPaymentService is @Primary.

With @Qualifier:

    @Autowired
    @Qualifier("upiPaymentService")
    private PaymentService paymentService;

Output:

    Paid using UPI

because @Qualifier specifically selects UpiPaymentService.

--------------------------------------------------

## Simple Difference

    @Primary
        ↓
    "Which bean should Spring use by default?"

    @Qualifier
        ↓
    "Which exact bean do I want?"

--------------------------------------------------

## Important Rule

    @Primary   -> Default bean
    @Qualifier -> Specific bean

If both are applicable:

    @Qualifier takes precedence over @Primary.

--------------------------------------------------