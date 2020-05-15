package uk.co.ramp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class Framework {

    public static void main(String[] args) {
        SpringApplication.run(Framework.class, args);
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        ctx.register(ContactRunner.class, Outbreak.class);
        ContactRunner runner = (ContactRunner) ctx.getBean("contactRunner");
        runner.run();
    }

}
