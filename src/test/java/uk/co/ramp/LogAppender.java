package uk.co.ramp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.rules.ExternalResource;

import java.io.CharArrayWriter;

public class LogAppender extends ExternalResource {

    private static final String APPENDER_NAME = "log4jRuleAppender";

    /**
     * Logged messages contains level and message only.
     * This allows us to test that level and message are set.
     */
    private static final String PATTERN = "%-5level %msg\n";
    private final CharArrayWriter outContent = new CharArrayWriter();
    private final Logger logger;
    private Appender appender;

    public LogAppender(Class clazz) {
        this.logger = (Logger) LogManager.getLogger(clazz.getName());
    }

    @Override
    protected void before() {
        StringLayout layout = PatternLayout.newBuilder().withPattern(PATTERN).build();
        appender = WriterAppender.newBuilder()
                .setTarget(outContent)
                .setLayout(layout)
                .setName(APPENDER_NAME).build();
        appender.start();
        logger.addAppender(appender);
    }

    @Override
    protected void after() {
        logger.removeAppender(appender);
    }

    public String getOutput() {
        return outContent.toString();
    }
}