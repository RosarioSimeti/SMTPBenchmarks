package diennea.smtpbenchmarks;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.server.SMTPServer;

public class MessageReceiver {
	private final ResultCollector resultCollector;
	private final String messageIDHeader;

	private final CountDownLatch countDown;
	private final SMTPServer server;

	private final ConcurrentMap<Integer, Long> messageIDReceiveTimes;

	public MessageReceiver(ResultCollector resultCollector, int messages, String host, int port, String messageIDHeader) throws UnknownHostException {
		server = new SMTPServer(new MessageHandlerFactory() {
			@Override
			public MessageHandler create(MessageContext ctx) {
				return new CountingHandler();
			}
		});

		//        server.setMaxConnections(1000);

		server.setBindAddress(InetAddress.getByName(host));
		server.setPort(port);

		this.resultCollector = resultCollector;
		this.messageIDHeader = messageIDHeader;

		countDown = new CountDownLatch(messages);
		messageIDReceiveTimes = new ConcurrentHashMap<>(messages);
	}

	public void flushResults(Map<Integer, Long> messageIDBeforeSendTimes, Map<Integer, Long> messageIDAfterSendTimes) {
		for (Map.Entry<Integer, Long> entry : messageIDReceiveTimes.entrySet()) {
			Integer messageID = entry.getKey();

			Long receive = messageIDReceiveTimes.get(messageID);

			Long before = messageIDBeforeSendTimes.get(messageID);
			Long after = messageIDAfterSendTimes.get(messageID);

			if (before != null)
				resultCollector.messageReceived(receive, before, after);
		}

	}

	public void start() {
		server.start();
	}

	public void stop() {
		server.stop();
	}

	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return countDown.await(timeout, unit);
	}

	private final class CountingHandler implements MessageHandler {
		private String messageID;

		public CountingHandler() {
			super();
		}

		@Override
		public void from(String from) throws RejectException {
		}

		@Override
		public void recipient(String recipient) throws RejectException {
		}

		@Override
		public void data(InputStream data) throws RejectException, TooMuchDataException, IOException {
			try {
				MimeMessage message = new MimeMessage(null, data);

				messageID = message.getHeader(messageIDHeader, null);

				/*
				 * Save the time just after receive the message, this avoid
				 * reschedulation timing overhead on massively concurrent
				 * systems.
				 */
				long end = System.nanoTime();

				messageIDReceiveTimes.put(Integer.valueOf(messageID), end);

			} catch (MessagingException e) {
				throw new IOException(e);
			}
		}

		@Override
		public void done() {
			countDown.countDown();
		}
	}

}
