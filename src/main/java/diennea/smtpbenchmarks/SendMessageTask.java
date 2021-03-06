
package diennea.smtpbenchmarks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;

import diennea.smtpbenchmarks.SendMessageTask.Result;

public class SendMessageTask implements Callable<Result> {
	public static final class Result {
		private final Map<Integer, Long> messageIDBeforeSendTimes;
		private final Map<Integer, Long> messageIDAfterSendTimes;

		private Result(int size) {
			messageIDBeforeSendTimes = new HashMap<>(size);
			messageIDAfterSendTimes = new HashMap<>(size);
		}

		public Map<Integer, Long> getMessageIDBeforeSendTimes() {
			return messageIDBeforeSendTimes;
		}

		public Map<Integer, Long> getMessageIDAfterSendTimes() {
			return messageIDAfterSendTimes;
		}
	}

	private final ResultCollector collector;

	private final String host;
	private final int port;
	private final String username;
	private final String password;

	private final Session session;
	private final MimeMessage message;

	private final int messageCount;

	private final int connectionID;
	private final AtomicInteger messageIDGenerator;
	private final String messageIDHeader;

	public SendMessageTask(ResultCollector collector, String host, int port, String username, String password, Session session, MimeMessage message, int messageCount, AtomicInteger connectionIDGenerator, AtomicInteger messageIDGenerator, String messageIDHeader) {
		super();

		this.collector = collector;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.session = session;

		try {
			/* We need a copy because its headers will be modified */
			this.message = new MimeMessage(message);

		} catch (MessagingException e) {
			/* Should never occur */
			throw new RuntimeException("Cannot copy message");
		}

		this.messageCount = messageCount;
		connectionID = connectionIDGenerator.getAndIncrement();
		this.messageIDGenerator = messageIDGenerator;
		this.messageIDHeader = messageIDHeader;
	}

	@Override
	public Result call() throws Exception {
		final Result result = new Result(messageCount);

		long mtime = 0;
		long stime = 0;
		long cstart = System.nanoTime();

		try {

			CustomSMTPTransport transport = new CustomSMTPTransport(session, new URLName("smtps", host, port, null, null, null));
			try {
				transport.connect(host, port, username, password);

				for (int i = 0; i < messageCount; i++) {

					long mstart = -1, mend = -1;

					mstart = System.nanoTime();

					/*
					 * Generate a message id and add it to the message, it will
					 * be needed to recognize received messages.
					 */
					int messageID = messageIDGenerator.getAndIncrement();

					message.setHeader(messageIDHeader, Integer.toString(messageID));
					message.saveChanges();

					mend = System.nanoTime();

					mtime += mend - mstart;

					long before = mend;
					long after;
					try {
						transport.sendMessage(message, message.getAllRecipients());

						/*
						 * On multithread environment this thread could be
						 * descheduled for a long time. It happens even that a
						 * message is received before current thread finish to
						 * read end data aknowledge. To avoid strange negative
						 * timings we evaluate time on byte array send finish
						 * and before send data terminator '.'
						 */
						after = transport.time;

						long cstime = after - before;
						stime += cstime;

						collector.messageSent(connectionID, i, cstime, transport.getLastServerResponse(), null);

					} catch (Exception err) {
						after = transport.time;

						long cstime = after - before;
						stime += cstime;

						collector.messageSent(connectionID, i, cstime, transport.getLastServerResponse(), err);
						break;
					}

					/*
					 * Adds message id start time after message send to not
					 * account map time into message send time
					 */
					result.messageIDBeforeSendTimes.put(messageID, before);
					result.messageIDAfterSendTimes.put(messageID, after);
				}

			} finally {
				transport.close();
			}

			long cend = System.nanoTime();

			collector.connectionHandled(connectionID, cend - cstart - mtime - stime, null);

		} catch (Throwable error) {
			long cend = System.nanoTime();

			collector.connectionHandled(connectionID, cend - cstart - mtime - stime, error);
		}

		return result;
	}

	private final class CustomSMTPTransport extends SMTPTransport {
		long time;

		public CustomSMTPTransport(Session session, URLName urlname) {
			super(session, urlname, "smtps", true);
		}

		@Override
		protected void finishData() throws IOException, MessagingException {
			time = System.nanoTime();

			super.finishData();
		}
	}

}
