package org.junit.rules;

import java.lang.management.ManagementFactory;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DisableOnDebug implements TestRule {
    private final boolean debugging;
    private final TestRule rule;

    public DisableOnDebug(TestRule rule) {
        this(rule, ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    DisableOnDebug(TestRule rule, List<String> inputArguments) {
        this.rule = rule;
        this.debugging = isDebugging(inputArguments);
    }

    public Statement apply(Statement base, Description description) {
        return this.debugging ? base : this.rule.apply(base, description);
    }

    private static boolean isDebugging(List<String> arguments) {
        for (String argument : arguments) {
            if ("-Xdebug".equals(argument)) {
                return true;
            }
            if (argument.startsWith("-agentlib:jdwp")) {
                return true;
            }
        }
        return false;
    }

    public boolean isDebugging() {
        return this.debugging;
    }
}
