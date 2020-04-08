package com.backbase.oss.blade.logging;

import com.backbase.oss.blade.tomcat.BladeTomcat;
import org.apache.juli.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S2629")
public class BladeLogger implements Log {

    private final Logger logger;

    public BladeLogger() {
        logger = LoggerFactory.getLogger(BladeTomcat.class);
    }

    public BladeLogger(final String name) {
        logger = LoggerFactory.getLogger(name);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void trace(Object message) {
        logger.trace(message.toString());
    }

    @Override
    public void trace(Object message, Throwable t) {
        logger.trace(message.toString(), t);
    }

    @Override
    public void debug(Object message) {
        logger.debug(message.toString());
    }

    @Override
    public void debug(Object message, Throwable t) {
        logger.debug(message.toString(), t);
    }

    @Override
    public void info(Object message) {
        logger.info(message.toString());
    }

    @Override
    public void info(Object message, Throwable t) {
        logger.info(message.toString(), t);
    }

    @Override
    public void warn(Object message) {
        logger.warn(message.toString());
    }

    @Override
    public void warn(Object message, Throwable t) {
        logger.warn(message.toString(), t);
    }

    @Override
    public void error(Object message) {
        logger.error(message.toString());
    }

    @Override
    public void error(Object message, Throwable t) {
        logger.error(message.toString(), t);
    }

    @Override
    public void fatal(Object message) {
        logger.error(message.toString());
    }

    @Override
    public void fatal(Object message, Throwable t) {
        logger.error(message.toString(), t);
    }
}
