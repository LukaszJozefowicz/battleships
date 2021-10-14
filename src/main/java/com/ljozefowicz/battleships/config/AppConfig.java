package com.ljozefowicz.battleships.config;

import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.model.beans.ActiveUsersList;
import com.ljozefowicz.battleships.model.beans.ActiveGamesList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class AppConfig {

    @Bean
    public DtoMapper dtoMapper(){
        return new DtoMapper();
    }

    @Bean
    public ActiveUsersList activeUsersList(){
        return new ActiveUsersList();
    }

    @Bean
    public ActiveGamesList gamesList(){
        return new ActiveGamesList();
    }

//    @Bean
//    public ShipsToAddToDB pcShipsToAddToDB() { return new ShipsToAddToDB(); }

    @Bean
    @Description("Spring Message Resolver")
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

//    @Bean
//    public ClassLoaderTemplateResolver secondaryTemplateResolver() {
//        ClassLoaderTemplateResolver secondaryTemplateResolver = new ClassLoaderTemplateResolver();
//        secondaryTemplateResolver.setPrefix("templates/fragments");
//        //secondaryTemplateResolver.setSuffix(".html");
//        secondaryTemplateResolver.setTemplateMode(TemplateMode.HTML);
//        secondaryTemplateResolver.setCharacterEncoding("UTF-8");
//        secondaryTemplateResolver.setOrder(1);
//        secondaryTemplateResolver.setCheckExistence(true);
//
//        return secondaryTemplateResolver;
//    }
}
