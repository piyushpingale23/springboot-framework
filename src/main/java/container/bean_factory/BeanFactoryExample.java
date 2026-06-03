package container.bean_factory;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/*
 * BeanFactory Example
 * -------------------
 * BeanFactory is the basic IoC container of Spring.
 *
 * Key Point:
 * BeanFactory follows Lazy Initialization.
 *
 * Lazy Initialization means:
 * - Spring reads bean definitions from XML.
 * - Bean objects are NOT created immediately.
 * - Bean objects are created only when getBean() is called.
 */
public class BeanFactoryExample {

    public static void main(String[] args) {

        /*
         * Step 1:
         * Locate beans.xml from the classpath.
         */
        Resource resource = new ClassPathResource("beans.xml");

        /*
         * Step 2:
         * Create BeanFactory container.
         *
         * At this point:
         * No bean object is created.
         */
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        /*
         * Step 3:
         * Read bean definitions from beans.xml and register them
         * inside BeanFactory.
         *
         * Spring only knows:
         * "How to create the bean"
         *
         * Bean object is still NOT created.
         */
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);

        reader.loadBeanDefinitions(resource);

        System.out.println("BeanFactory Created");

        /*
         * Step 4:
         * Request the bean from Spring.
         *
         * Since BeanFactory uses Lazy Initialization,
         * Spring creates the Engine object NOW.
         */
        Engine engine = beanFactory.getBean("engine_bean_factory", Engine.class);

        /*
         * Use the bean.
         */
        engine.start();
    }
}

/*
 * Simple Spring Bean
 */
class Engine {

    /*
     * Constructor is called when Spring creates the bean.
     */
    public Engine() {
        System.out.println("BeanFactory Engine Bean Created");
    }

    public void start() {
        System.out.println("BeanFactory Engine Started");
    }
}