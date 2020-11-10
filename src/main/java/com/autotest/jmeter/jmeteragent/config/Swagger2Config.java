package com.autotest.jmeter.jmeteragent.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
public class Swagger2Config {
    @Bean
    public Docket createRestApi() {
        //        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.basePackage("com.szmachine.web")).paths(PathSelectors.any()).build();
        //添加header<mToken>
        ParameterBuilder ticketPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<>();
        ticketPar.name("mToken")
                .description("member token")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false).build(); //header中的ticket参数非必填，传空也可以
        pars.add(ticketPar.build());    //根据每个方法名也知道当前方法在设置什么参数


        ParameterBuilder signPar = new ParameterBuilder();
        signPar.name("sign")
                .description("sign")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false).build();
        pars.add(signPar.build());

        ParameterBuilder AuthPar= new ParameterBuilder();
        AuthPar.name("Authorization")
                 .description("Authorization")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false).build();
        pars.add(AuthPar.build());


        ParameterBuilder versionPar= new ParameterBuilder();
        versionPar.name("Version")
                .description("Version")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false).build();
        pars.add(versionPar.build());


        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .pathMapping("/")
                .select() // 选择那些路径和api会生成document
                .apis(RequestHandlerSelectors.any())// 对所有api进行监控
                // 不显示错误的接口地址 
                .paths(Predicates.not(PathSelectors.regex("/error.*")))// 错误路径不监控
                .paths(PathSelectors.regex("/.*"))// 对根下所有路径进行监控
                .build();
       // .globalOperationParameters(pars)

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("自动化测试平台接口")
        		.description("接口API")
//        		.termsOfServiceUrl("https://github.com/")
//        		.contact(new Contact("Java Team", "https://github.com/", "1111@qq.com"))
                .version("1.0.0").build();
    }
}
