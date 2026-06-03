package annotations.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

/*
 * LifecycleExample - FULL SPRING BEAN LIFECYCLE (STEP BY STEP FLOW)
 * -------------------------------------------------------------------
 *
 * This class explains COMPLETE Spring Bean lifecycle from:
 * Application start → Container creation → Bean creation → DI → Init → Destroy
 */

@Component
public class LifecycleExample {

    /*
     * ================================
     * STEP 1: APPLICATION START
     * ================================
     * When you run:
     * SpringApplication.run(Main.class, args);
     *
     * OR
     * new AnnotationConfigApplicationContext(...)
     *
     * Spring Boot starts the JVM application.
     */
    public LifecycleExample() {
        System.out.println("STEP 1: Application Start!");
        System.out.println("STEP 2: Spring Container Start!");
        System.out.println("STEP 3: Constructor Called! (Bean Object Created)");
    }

    /*
     * ================================
     * STEP 2: SPRING CONTAINER START
     * ================================
     * Spring IoC container is created.
     * It reads configuration (@ComponentScan, @Configuration, etc.)
     * and prepares bean definitions.
     */

    /*
     * ================================
     * STEP 3: BEAN INSTANTIATION
     * ================================
     * Spring creates object of this class using constructor.
     * (Memory allocation happens here)
     */

    /*
     * ================================
     * STEP 4: DEPENDENCY INJECTION (DI)
     * ================================
     * Spring injects dependencies into this bean.
     *
     * Example:
     * @Autowired
     * private SomeService service;
     *
     * At this stage:
     * - All dependencies are set
     * - Bean is NOT yet initialized for use
     */

    /*
     * ================================
     * STEP 5: @PostConstruct (INIT PHASE)
     * ================================
     * Called AFTER:
     * - Constructor execution
     * - Dependency Injection completion
     *
     * Purpose:
     * - Initialize DB connection
     * - Load cache
     * - Setup resources
     *
     * Now the bean is FULLY READY to use.
     */
    @PostConstruct
    public void init() {
        System.out.println("STEP 4: @PostConstruct Called (Bean Initialized)");
    }

    /*
     * ================================
     * STEP 6: APPLICATION RUNNING
     * ================================
     * Bean is actively used by Spring application.
     * Example:
     * Controller → Service → Repository flow
     */

    /*
     * ================================
     * STEP 7: APPLICATION SHUTDOWN
     * ================================
     * Happens when:
     * - Ctrl + C
     * - Server shutdown
     * - context.close()
     *
     * Spring starts destroying beans.
     */

    /*
     * ================================
     * STEP 8: @PreDestroy (CLEANUP PHASE)
     * ================================
     * Called BEFORE bean destruction.
     *
     * Purpose:
     * - Close DB connections
     * - Stop threads
     * - Release resources
     */
    @PreDestroy
    public void cleanup() {
        System.out.println("STEP 5: @PreDestroy Called (Cleanup Before Bean Destroyed)");
    }

    /*
     * ================================
     * STEP 9: BEAN DESTROYED
     * ================================
     * Object is removed from Spring container memory.
     */
}