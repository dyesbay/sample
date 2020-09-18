package app.base.configs;

import app.base.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Set;

@RequiredArgsConstructor
@Configuration
public class GApplicationStart implements ApplicationListener<ContextRefreshedEvent> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${info.app.name}")
    protected String system;

    protected final RedisService redisService;

    protected void clearCache() {
        Set<String> keys = redisService.keys(system + ".*");
        logger.info("Clear static cache: {}", keys);
        redisService.del(keys.toArray(new String[0]));
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent arg0) {
        try {
            clearCache();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
