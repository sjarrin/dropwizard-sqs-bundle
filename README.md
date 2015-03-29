Dropwizard SQS Bundle
==================================

[![Build Status](https://travis-ci.org/sjarrin/dropwizard-sqs-bundle.svg)](https://travis-ci.org/sjarrin/dropwizard-sqs-bundle)
 
Dropwizard-SQS bundle is an Amazon SQS-dedicated bundle, designed to be simple to send and receive messages from Amazon SQS service.
It is widely inspired from great [Morten Kjetland's ActiveMQ bundle](https://github.com/mbknor/dropwizard-activemq-bundle), thanks to him !

Maven
----------------

Add it as a dependency:

```xml
    <dependency>
        <groupId>ch.rts</groupId>
        <artifactId>dropwizard-sqs</artifactId>
        <version> INSERT LATEST VERSION HERE </version>
    </dependency>
```

Configuration
------------
This bundle relies on Amazon credential system that goes and look for a credentials file in your `~/.aws/` directory. This system
allows you to avoid committing any password or access key in your source code management system.


To use the bundle, first set up your config class to implement `SqsConfigurationHolder` interface:
```java
public class Config extends Configuration implements SqsConfigurationHolder {

    @JsonProperty
    @NotNull
    @Valid
    private SqsConfiguration sqsConfiguration;

    @Override
    public SqsConfiguration getSqsConfiguration() {
        return sqsConfiguration;
    }

}
```

Then add the needed configuration in your config file:
```yml
SQS:
  queueNames:
    - queue1
    - ...
    - queueN

  # The region as mentioned in [Regions and Endpoints list](http://docs.aws.amazon.com/general/latest/gr/rande.html#sqs_region)
  # US East (N. Virginia): us-east-1
  # US West (Oregon): us-west-2
  # US West (N. California): us-west-1
  # EU (Ireland): eu-west-1
  # EU (Frankfurt): eu-central-1
  # Asia Pacific (Singapore): ap-southeast-1
  # Asia Pacific (Sydney): ap-southeast-2
  # Asia Pacific (Tokyo): ap-northeast-1
  # South America (Sao Paulo): sa-east-1
  region: eu-central-1
```

Usage
------------

The bundle has to be instantiated in your main application `initialize` method, as follows:
```java
public class SampleApp extends Application<Config> {

    private SqsBundle sqsBundle;

    public static void main(String[] args) throws Exception {
        new SampleApp().run(args);
    }

    @Override
    public void initialize(Bootstrap<Config> configBootstrap) {
        this.sqsBundle = new SqsBundle();
        messengerConfigurationBootstrap.addBundle(sqsBundle);
    }
```

After that, you are able to easily create senders:
```java
    SqsSender sender = sqsBundle.createSender("test-queue");
    sender.send("Any text message");
```

And create receivers to get back your messages:
```java
    // this receiver is being passed a lambda to print out messages content
    sqsBundle.registerReceiver(
            "test-queue",
            (m) -> {
                Message message = (Message) m;
                System.out.println("  Message");
                System.out.println("    MessageId:     " + message.getMessageId());
            });

    // you can also pass a custom exception handler to the receiver
    sqsBundle.registerReceiver(
            "test-queue",
            (m) -> processMessage(m),
            // Add your custom exception-handler here
            (message, exception) -> {
                System.out.println("Error with this message: " + message);
                return true;
            });

    private void processMessage(Object m) throws IOException {
        // not yet implemented
    }
```

TODO
------------
Testing to improve:
- check exception is thrown if no credentials found
