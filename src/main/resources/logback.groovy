// recommended console status listener
statusListener(OnConsoleStatusListener)
scan("15 minutes")

// todo: first load application.properties which is mandatory file
// todo: laod configuration.properties for default values

appender("FILE", FileAppender) {
    append = true
    immediateFlush = true // false for higher logging throughput, fixme: but figure out how to flush()
    file = "D:\\Harsha\\Downloads\\app.log"
    encoder(PatternLayoutEncoder) {
        pattern = "%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}: %msg%n"
    }
}

def appenderList = ["FILE"]
if (!this.class.getResource("").getProtocol().equalsIgnoreCase("jar")) {
    appender("STDOUT", ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
        }
    }

    // todo: should only be added in diagnose mode
    appenderList.add("STDOUT")
}

root(DEBUG, appenderList)
