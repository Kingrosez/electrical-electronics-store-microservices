package com.eande.store.auth_service.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static ch.qos.logback.core.spi.FilterReply.ACCEPT;
import static ch.qos.logback.core.spi.FilterReply.DENY;

@Configuration
public class LoggingConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${logging.file.path:logs}")
    private String logPath;

    @Bean
    @Profile("!test")
    public void configureLogging() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Get root logger
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders();

        // Console Appender
        ConsoleAppender consoleAppender = createConsoleAppender(loggerContext);
        rootLogger.addAppender(consoleAppender);

        // File Appender
        RollingFileAppender fileAppender = createFileAppender(loggerContext);
        rootLogger.addAppender(fileAppender);

        // Error File Appender
        RollingFileAppender errorFileAppender = createErrorFileAppender(loggerContext);
        rootLogger.addAppender(errorFileAppender);

        // Set root log level
        rootLogger.setLevel(Level.INFO);

        // Set application log level
        Logger appLogger = loggerContext.getLogger("com.eande.store.auth_service");
        appLogger.setLevel(Level.DEBUG);

        // Print configuration
        StatusPrinter.print(loggerContext);
    }

    private ConsoleAppender createConsoleAppender(LoggerContext loggerContext) {
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName("CONSOLE");

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{ISO8601} | %highlight(%-5level) | [%thread] | %cyan(%logger{36}) | %msg%n");
        encoder.start();

        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        return consoleAppender;
    }

    private RollingFileAppender createFileAppender(LoggerContext loggerContext) {
        RollingFileAppender fileAppender = new RollingFileAppender();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        fileAppender.setFile(String.format("%s/%s.log", logPath, appName));

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{ISO8601} | %-5level | [%thread] | %logger{36} | %msg%n");
        encoder.start();
        fileAppender.setEncoder(encoder);

        // Rolling policy
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(String.format("%s/%s.%%d{yyyy-MM-dd}.log", logPath, appName));
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.start();

        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();

        return fileAppender;
    }

    private RollingFileAppender createErrorFileAppender(LoggerContext loggerContext) {
        RollingFileAppender errorFileAppender = new RollingFileAppender();
        errorFileAppender.setContext(loggerContext);
        errorFileAppender.setName("ERROR_FILE");
        errorFileAppender.setFile(String.format("%s/%s-error.log", logPath, appName));

        // Level filter - only ERROR level
        LevelFilter levelFilter = new LevelFilter();
        levelFilter.setContext(loggerContext);
        levelFilter.setLevel(Level.ERROR);
        levelFilter.setOnMatch(ACCEPT);
        levelFilter.setOnMismatch(DENY);
        levelFilter.start();
        errorFileAppender.addFilter(levelFilter);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{ISO8601} | %-5level | [%thread] | %logger{36} | %msg%n");
        encoder.start();
        errorFileAppender.setEncoder(encoder);

        // Rolling policy
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(errorFileAppender);
        rollingPolicy.setFileNamePattern(String.format("%s/%s-error.%%d{yyyy-MM-dd}.log", logPath, appName));
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.start();

        errorFileAppender.setRollingPolicy(rollingPolicy);
        errorFileAppender.start();

        return errorFileAppender;
    }
}