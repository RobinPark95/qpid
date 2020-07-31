package org.apache.qpid.jms.example;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

public class SortedServer {
    public static void main(String[] args) throws Exception {
        try {
            // The configuration for the Qpid InitialContextFactory has been supplied in
            // a jndi.properties file in the classpath, which results in it being picked
            // up automatically by the InitialContext constructor.
            Context context = new InitialContext();

            ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");

            Queue ConsumerQueue = (Queue) context.lookup("mySortedLookup");

            Connection connection = factory.createConnection("guest", "guest");
            connection.setExceptionListener(new SortedServer.MyExceptionListener());
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer messageConsumer = session.createConsumer(ConsumerQueue);
            MessageProducer messageProducer = session.createProducer(null);

            while (true) {
                //Receive messages and return a new uppercase message.
                TextMessage requestMessage = (TextMessage) messageConsumer.receive();

                System.out.println("[SERVER] Received: " + requestMessage.getText());

                TextMessage responseMessage = session.createTextMessage(requestMessage.getText().toUpperCase());

                messageProducer.send(requestMessage.getJMSReplyTo(), responseMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
            }

        } catch (Exception exp) {
            System.out.println("[SERVER] Caught exception, exiting.");
            exp.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static class MyExceptionListener implements ExceptionListener {
        @Override
        public void onException(JMSException exception) {
            System.out.println("[SERVER] Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
