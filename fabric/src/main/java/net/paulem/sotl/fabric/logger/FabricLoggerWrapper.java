package net.paulem.sotl.fabric.logger;

import net.paulem.sotl.common.wrapper.LoggerWrapper;
import org.slf4j.Logger;

public record FabricLoggerWrapper(Logger logger) implements LoggerWrapper {
    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }
}
