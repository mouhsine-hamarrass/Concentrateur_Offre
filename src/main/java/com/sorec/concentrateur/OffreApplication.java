package com.sorec.concentrateur;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.sorec.concentrateur.offre.config.ApplicationProperties;
import com.sorec.concentrateur.framework.config.DefaultProfileUtil;
import com.sorec.concentrateur.framework.config.ConcentrateurConstants;

@SpringBootApplication
@EntityScan({ "com.sorec.concentrateur.offre.domain","com.sorec.concentrateur.framework"})
@EnableJpaRepositories("com.sorec.concentrateur")
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class })
@EnableDiscoveryClient
public class OffreApplication implements InitializingBean{
	
	private static final Logger log = LoggerFactory.getLogger(OffreApplication.class);
	private final Environment env;

	    public OffreApplication(Environment env) {
	        this.env = env;
	    }

	@Override
	public void afterPropertiesSet() throws Exception {
		   Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
	        if (activeProfiles.contains(ConcentrateurConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(ConcentrateurConstants.SPRING_PROFILE_PRODUCTION)) {
	            log.error("You have misconfigured your application! It should not run " +
	                "with both the 'dev' and 'prod' profiles at the same time.");
	        }
	        log.info("activate profiles : {}", activeProfiles);
		
	}
	
	public static void main(String[] args) {
		  SpringApplication app = new SpringApplication(OffreApplication.class);
	        DefaultProfileUtil.addDefaultProfile(app);
	        Environment env = app.run(args).getEnvironment();
	        logApplicationStartup(env);
	}
	
	
    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (StringUtils.isBlank(contextPath)) {
            contextPath = "/";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "Application '{}' is running! Access URLs:\n\t" +
                "Local: \t\t{}://localhost:{}{}\n\t" +
                "External: \t{}://{}:{}{}\n\t" +
                "Profile(s): \t{}\n----------------------------------------------------------",
            env.getProperty("spring.application.name"),
            protocol,
            serverPort,
            contextPath,
            protocol,
            hostAddress,
            serverPort,
            contextPath,
            env.getActiveProfiles());

        String configServerStatus = env.getProperty("configserver.status");
        if (configServerStatus == null) {
            configServerStatus = "Not found or not setup for this application";
        }
        log.info("\n----------------------------------------------------------\n\t" +
                "Config Server: \t{}\n----------------------------------------------------------", configServerStatus);
    }
	
	

}
