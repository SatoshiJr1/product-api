package sn.isi.l3gl.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import sn.isi.l3gl.core.CoreConfig;

@SpringBootApplication
@Import(CoreConfig.class)
public class ProductApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApiApplication.class, args);
    }
}
