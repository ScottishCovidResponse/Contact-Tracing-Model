package uk.co.ramp;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.rules.ExternalResource;
import org.slf4j.LoggerFactory;

public final class LogSpy extends ExternalResource {

  private Logger logger;
  private ListAppender<ILoggingEvent> appender;

  @Override
  protected void before() {
    appender = new ListAppender<>();
    logger =
        (Logger)
            LoggerFactory.getLogger(
                Logger.ROOT_LOGGER_NAME); // cast from facade (SLF4J) to implementation class
    // (logback)
    logger.addAppender(appender);
    appender.start();
  }

  @Override
  protected void after() {
    logger.detachAppender(appender);
  }

  public String getOutput() {
    StringBuilder sb = new StringBuilder();
    appender.list.forEach(str -> sb.append(str).append("  "));
    return sb.toString();
  }
}
