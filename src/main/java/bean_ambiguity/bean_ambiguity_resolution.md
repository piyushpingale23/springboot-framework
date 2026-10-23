# Spring Bean Ambiguity Resolution

## What is Bean Ambiguity?

Bean ambiguity occurs when multiple Spring beans have the same type and
Spring does not know which bean to inject.

Example:

    public interface NotificationService {
        void send(String message);
    }

    @Service
    public class EmailNotificationService implements NotificationService {
        ...
    }

    @Service
    public class SmsNotificationService implements NotificationService {
        ...
    }

Now:

    @Autowired
    private NotificationService notificationService;

Spring finds:

    EmailNotificationService
    SmsNotificationService

Both are `NotificationService`, so Spring throws:

    NoUniqueBeanDefinitionException

Spring provides several ways to resolve this.

---

# 1. @Qualifier

`@Qualifier` explicitly tells Spring which specific bean should be injected.

Think:

    @Qualifier = "I specifically want this bean."

Example:

    @Autowired
    @Qualifier("emailNotificationService")
    private NotificationService notificationService;

Spring selects:

    EmailNotificationService

The default bean name is usually the class name with the first letter
converted to lowercase:

    EmailNotificationService
            ↓
    emailNotificationService

## Remember

    @Qualifier → Specific bean

---

# 2. @Primary

`@Primary` marks one bean as the default choice when multiple beans of
the same type exist.

Think:

    @Primary = "Use this bean by default."

Example:

    @Service
    @Primary
    public class EmailNotificationService
            implements NotificationService {
        ...
    }

    @Service
    public class SmsNotificationService
            implements NotificationService {
        ...
    }

Now:

    @Autowired
    private NotificationService notificationService;

Spring selects:

    EmailNotificationService

because it is marked with `@Primary`.

## Remember

    @Primary → Default bean

If `@Qualifier` is also specified, the specific `@Qualifier` selection
takes precedence over `@Primary`.

---

# 3. @Resource

`@Resource` can select a Spring bean primarily by its name.

Think:

    @Resource = "Give me the bean with this name."

Example:

    @Resource(name = "emailNotificationService")
    private NotificationService notificationService;

Spring looks for the bean named:

    emailNotificationService

and injects:

    EmailNotificationService

It is similar to:

    @Autowired
    @Qualifier("emailNotificationService")

## Remember

    @Resource(name = "...") → Select bean by name

---

# 4. @Profile

`@Profile` controls whether a bean is created based on the active
Spring profile.

It is useful when different environments should use different
implementations.

Example:

    @Service
    @Profile("dev")
    public class EmailNotificationService
            implements NotificationService {
        ...
    }

    @Service
    @Profile("prod")
    public class SmsNotificationService
            implements NotificationService {
        ...
    }

If:

    spring.profiles.active=dev

Spring creates:

    EmailNotificationService

If:

    spring.profiles.active=prod

Spring creates:

    SmsNotificationService

Therefore:

    @Autowired
    private NotificationService notificationService;

has no ambiguity because only one matching bean is active.

## Remember

    @Profile → Controls which bean is created for an environment

---

# Quick Comparison

    @Qualifier
        → Select a specific bean

    @Primary
        → Select a default bean

    @Resource
        → Select a bean by name

    @Profile
        → Activate different beans for different environments

---

# Easy Way to Remember

    @Qualifier → "I want THIS specific bean."

    @Primary   → "Use THIS bean by default."

    @Resource  → "Give me the bean with THIS name."

    @Profile   → "Use THIS bean in THIS environment."

---

# Example Scenario

Suppose we have:

    NotificationService
        |
        +-- EmailNotificationService
        |
        +-- SmsNotificationService

Possible solutions:

    @Qualifier("emailNotificationService")
        → Explicitly choose EmailNotificationService

    @Primary
        → Make EmailNotificationService the default

    @Resource(name = "emailNotificationService")
        → Choose EmailNotificationService by bean name

    @Profile("dev") / @Profile("prod")
        → Use different implementations depending on environment