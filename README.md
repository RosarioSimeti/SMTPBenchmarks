# smtpbenchmarks
SMTP Benchmarks

## Getting started

  All you need to get started is a Java8 JRE, the smtpbenchmarks-1.0.jar file and an SMTP server to test.

  Just run this command and follow the Usage notes

```

  $JAVA_HOME/java -jar smtpbenchmarks-1.0.jar

```
If you want, you can pass the arguments to use this software in command line.
Otherwise, it will ask you for them later.

##  Building from source

  In order to build SMTPBenchmarks from source you need to have Maven installed. 
  Just clone this repository and issue a mvn clean install command. 
  You will find smtpbenchmarks-1.0.jar in your target directory


##  Reference

```
usage: smtpsampler [-a] [-d] [-f <arg>] [-h <arg>] [-l] [-lh <arg>] [-lp
       <arg>] [-mf <arg>] [-ms <arg>] [-n <arg>] [-nc <arg>] [-p <arg>]
       [-pwd <arg>] [-s <arg>] [-stls] [-t <arg>] [-tt <arg>] [-tx <arg>]
       [-u <arg>] [-v]
 -a,--auth                              Use authentication
 -d,--javamaildebug                     Enable JavaMail Debug
 -f,--from <arg>                        Value for the From header of the
                                        test message
 -h,--host <arg>                        SMTP Server hostname or IP
                                        Address, default to localhost
 -l,--listen                            Listen on a generated inbound SMTP
                                        Server for message delivery
 -lh,--listenhost <arg>                 SMTP Server hostname or IP
                                        Address, default to localhost
 -lp,--listenport <arg>                 SMTP Server port, default to 25
 -mf,--file <arg>                       Use file as message and do not
                                        generate a test message
 -ms,--messagesize <arg>                Size of the body of the generated
                                        message, defaults to 10 bytes
 -n,--nummessages <arg>                 Number of messages, defaults to 1
 -nc,--nummessagesperconnection <arg>   Number of messages per connection,
                                        defaults to 1
 -p,--port <arg>                        SMTP Server port, default to 25
 -pwd,--password <arg>                  Password
 -s,--subject <arg>                     Subject of the generated email
 -stls,--starttls                       Use STARTTLS
 -t,--to <arg>                          Value for the To header of the
                                        test message
 -tt,--timeout <arg>                    Max time for execution of the
                                        test, in seconds, defaults to 0,
                                        which means 'forever'
 -tx,--numthreads <arg>                 Number of concurrent
                                        threads/connections
 -u,--username <arg>                    Username
 -v,--verbose                           Verbose output

```
 
