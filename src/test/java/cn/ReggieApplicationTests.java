package cn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootTest
@ServletComponentScan           //扫描整个servlet(filter)
class ReggieApplicationTests {

    @Test
    void contextLoads() {
    }

}
