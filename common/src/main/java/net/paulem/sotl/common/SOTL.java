package net.paulem.sotl.common;

import net.paulem.sotl.common.logger.LoggerWrapper;

public interface SOTL<T> {
    T get();

    LoggerWrapper logger();
}
