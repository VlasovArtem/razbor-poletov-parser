package org.avlasov.razborpoletov.reader.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.CommandLine;
import org.avlasov.razborpoletov.reader.utils.CLIUtils;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Created by artemvlasov on 25/06/2017.
 */
@Configuration
@ComponentScan(basePackages = "org.avlasov")
@PropertySource("classpath:twitter.properties")
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
                .failOnUnknownProperties(false)
                .autoDetectFields(true)
                .autoDetectGettersSetters(false)
                .build();
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        return mapper;
    }

    @Bean
    @Profile("commandLine")
    public CommandLine commandLine(CLIUtils cliUtils) {
        return cliUtils.createCommandLine();
    }

}
