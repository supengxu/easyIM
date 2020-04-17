package top.xxpblog.easyChat.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.bind.annotation.CrossOrigin;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ServletComponentScan
@CrossOrigin
@EnableSwagger2
public class EasyIMApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyIMApiApplication.class, args);
    }

}

