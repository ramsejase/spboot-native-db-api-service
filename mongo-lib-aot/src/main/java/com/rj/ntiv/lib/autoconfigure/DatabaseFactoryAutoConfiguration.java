package com.rj.ntiv.lib.autoconfigure;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.util.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import com.rj.ntiv.lib.config.ConnectionUrl;
import com.rj.ntiv.lib.config.DatabaseProperties;
import com.rj.ntiv.lib.config.MongoConfig;
import com.rj.ntiv.lib.config.MongoQualifier;

@AutoConfigureOrder(value = Ordered.HIGHEST_PRECEDENCE)
@AutoConfiguration
public class DatabaseFactoryAutoConfiguration implements EnvironmentAware, BeanFactoryAware, BeanFactoryPostProcessor {

    static final Logger log = LoggerFactory.getLogger(DatabaseFactoryAutoConfiguration.class);

    ConfigurableEnvironment environment;
    ConfigurableListableBeanFactory beanFactory;

    @Bean
    static FactoryAotInitializer factoryAotInitializer() {
        return new FactoryAotInitializer();
    }
    
    @Bean
    BeanRegistry beanRegistry() {
        return new BeanRegistry();
    }

    @Override
    public void setEnvironment(Environment environment) {
    
        log.info("-----------> setEnvironment");
        this.environment = (ConfigurableEnvironment) environment;
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        
        log.info("-----------> setBeanFactory");
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        log.info("-----------> postProcessBeanFactory");
        this.beanFactory = beanFactory;
    }

    public static class FactoryAotInitializer implements BeanFactoryInitializationAotProcessor {
        @Override
        @Nullable
        public BeanFactoryInitializationAotContribution processAheadOfTime(
                ConfigurableListableBeanFactory beanFactory) {
            
            log.info("-----------> FactoryAotInitializer");
            return (generationContext, beanFactoryInitializationCode) -> {

                RuntimeHints hints = generationContext.getRuntimeHints();;
                hints.reflection()
                    .registerType(ConnectionUrl.class, MemberCategory.values())
                    .registerType(DatabaseProperties.class, MemberCategory.values())
                    .registerType(MongoConfig.class, MemberCategory.values());             

                hints.resources()
                    .registerPattern("application.*");
            };
        }
    }

    public class BeanRegistry implements BeanDefinitionRegistryPostProcessor {
        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        
            log.info("-----------> postProcessBeanDefinitionRegistry");
            registerBeanDefinitions(beanFactory, registry);
        }
    }

    public void registerBeanDefinitions(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry) {

        log.info("-----------> registerBeanDefinition");
        DefaultListableBeanFactory dfactory = (DefaultListableBeanFactory) beanFactory;
        
        AtomicInteger counter = new AtomicInteger(1);
        
        List<DatabaseProperties> databasePropertiesList = Binder
                .get(environment)
                .bind("mongo", MongoConfig.class)
                .get()
                .getConfig();
                log.info("-----------> Properties Configured Count: {}", databasePropertiesList.size());

        databasePropertiesList
            .forEach(props -> {
                
                Integer countpos = counter.getAndIncrement();
                ConnectionUrl curl = mongoConnectionUrl(props);
                log.info("-----------> props - " + curl.val());

                AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(ConnectionUrl.class);
                definition.getConstructorArgumentValues().addIndexedArgumentValue(0, curl.val());
                definition.setNonPublicAccessAllowed(true);
                definition.setLazyInit(true);
                definition.setAutowireCandidate(true);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                definition.setScope(AbstractBeanDefinition.SCOPE_PROTOTYPE);
                definition.setPrimary(countpos == 1);
                
                AutowireCandidateQualifier mqualifier = new AutowireCandidateQualifier(MongoQualifier.class);
                mqualifier.setAttribute("name", props.getName());
                
                definition.addQualifier(mqualifier);

                dfactory.setAutowireCandidateResolver(new MongoQualifierResolver(beanFactory));

                registry.registerBeanDefinition(props.getName(), definition);

            });
    }

    public ConnectionUrl mongoConnectionUrl(DatabaseProperties properties) {

        String url = "mongodb+srv://"+ properties.getUser()+":"+properties.getSecret()
            +"@"+properties.getHost()+":"+properties.getPort()+"/"+properties.getDb()+"?retryWrites=true&w=majority";
        return new ConnectionUrl(url);
    }

    public class MongoQualifierResolver extends QualifierAnnotationAutowireCandidateResolver {

        ConfigurableListableBeanFactory cFactory;

        public MongoQualifierResolver(ConfigurableListableBeanFactory cFactory) {
            this.cFactory = cFactory;
        }

        @Override
        protected boolean checkQualifier(BeanDefinitionHolder bdHolder, Annotation annotation,
                TypeConverter typeConverter) {
                                
                    if(annotation instanceof MongoQualifier) {
                        MongoQualifier mannotation = (MongoQualifier) annotation;
                        annotation = new MQualifierResolver(getQualifierPlaceHolder(mannotation.name()));
                    }

                    if(annotation instanceof Qualifier) {
                        Qualifier qannotation = (Qualifier) annotation;
                        annotation = new DefaultQualifierResolver(getQualifierPlaceHolder(qannotation.value()));
                    }
            return super.checkQualifier(bdHolder, annotation, typeConverter);
        }

        String getQualifierPlaceHolder(String qualifierFieldValue) {
            if(qualifierFieldValue.startsWith("${") && qualifierFieldValue.endsWith("}")) {
                return cFactory.resolveEmbeddedValue(qualifierFieldValue);
            }
            return qualifierFieldValue;
        }

        public class DefaultQualifierResolver implements Qualifier {

            String value;

            public DefaultQualifierResolver(String value) {
                this.value = value;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Qualifier.class;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public String toString() {
                return "MongoQualifier(name="+value()+")";
            }
        }

        public class MQualifierResolver implements MongoQualifier {

            String name;

            public MQualifierResolver(String name) {
                this.name = name;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return MongoQualifier.class;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String toString() {
                return "MongoQualifier(name="+name()+")";
            }
        }
    }
}
