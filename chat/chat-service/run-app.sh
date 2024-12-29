export JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom --add-opens=java.base/java.lang=ALL-UNNAMED
                                                          --add-opens=java.base/java.math=ALL-UNNAMED
                                                          --add-opens=java.base/java.util=ALL-UNNAMED
                                                          --add-opens=java.base/java.util.concurrent=ALL-UNNAMED
                                                          --add-opens=java.base/java.net=ALL-UNNAMED
                                                          --add-opens=java.base/java.text=ALL-UNNAMED
                                                          --add-opens=java.sql/java.sql=ALL-UNNAMED
                                                          --add-opens=java.base/java.time=ALL-UNNAMED $JAVA_OPTS"
export JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom $JAVA_OPTS"
if [ "$DEBUG_PORT" ]; then
  export JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$DEBUG_PORT"
fi
if [ "$JMX_PORT" ]; then
  export JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMX_PORT -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
fi
echo "Starting Java with the arguments: $JAVA_OPTS"
java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher
