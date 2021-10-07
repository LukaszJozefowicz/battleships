package com.ljozefowicz.battleships.config;

import com.ljozefowicz.battleships.dto.mapper.DtoMapper;
import com.ljozefowicz.battleships.model.beans.ActiveUsersList;
import com.ljozefowicz.battleships.model.beans.ActiveGamesList;
import com.ljozefowicz.battleships.model.beans.PcShipsToAddToDB;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public PcShipsToAddToDB pcShipsToAddToDB() { return new PcShipsToAddToDB(); }

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
