package com.ncpl.sales.cashflow.config;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
@Configuration
public class CashflowViewConfig implements WebMvcConfigurer {
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext applicationContext;
    // Use Boot's reserved bean name to prevent an unrestricted default resolver.
    @Bean(name = "thymeleafViewResolver") public ThymeleafViewResolver cashflowViewResolver() {
        SpringResourceTemplateResolver templates = new SpringResourceTemplateResolver();
        templates.setApplicationContext(applicationContext);
        templates.setPrefix("classpath:/templates/"); templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8"); templates.setTemplateMode("HTML");
        SpringTemplateEngine engine = new SpringTemplateEngine(); engine.setTemplateResolver(templates);
        ThymeleafViewResolver resolver = new ThymeleafViewResolver(); resolver.setTemplateEngine(engine);
        resolver.setCharacterEncoding("UTF-8"); resolver.setViewNames(new String[]{"cashflow/*"}); resolver.setOrder(-10);
        return resolver;
    }
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/cashflow-analyzer/js/**").addResourceLocations("classpath:/static/cashflow/js/");
        registry.addResourceHandler("/cashflow-analyzer/css/**").addResourceLocations("classpath:/static/cashflow/css/");
        registry.addResourceHandler("/cashflow-analyzer/images/**").addResourceLocations("classpath:/static/cashflow/images/");
    }
    @Override public void extendMessageConverters(java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        // ERP supplies a plain ObjectMapper. Scope Java-time serialization to cashflow requests only.
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        converters.add(0, new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(mapper) {
            private boolean cashflowRequest() {
                org.springframework.web.context.request.RequestAttributes attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (!(attributes instanceof org.springframework.web.context.request.ServletRequestAttributes)) return false;
                javax.servlet.http.HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes) attributes).getRequest();
                return request.getRequestURI().startsWith(request.getContextPath() + "/cashflow-analyzer/");
            }
            @Override public boolean canWrite(Class<?> type, org.springframework.http.MediaType mediaType) {
                return cashflowRequest() && super.canWrite(type, mediaType);
            }
            @Override public boolean canWrite(java.lang.reflect.Type type, Class<?> clazz, org.springframework.http.MediaType mediaType) {
                return cashflowRequest() && super.canWrite(type, clazz, mediaType);
            }
            @Override public boolean canRead(Class<?> type, org.springframework.http.MediaType mediaType) {
                return cashflowRequest() && super.canRead(type, mediaType);
            }
            @Override public boolean canRead(java.lang.reflect.Type type, Class<?> clazz, org.springframework.http.MediaType mediaType) {
                return cashflowRequest() && super.canRead(type, clazz, mediaType);
            }
        });
    }
}
