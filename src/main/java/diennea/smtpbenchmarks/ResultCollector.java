package diennea.smtpbenchmarks;

public interface ResultCollector {

	public void start();

	public void finishSend();

	public void finishReceive();

	public void finished();

	public void messageSent(int connectionId, int messageNumber, long time, String lastServerResponse, Throwable error);

	public void connectionHandled(int connectionId, long time, Throwable error);

	public void messageReceived(long receivens, long beforesendns, long aftersendns);

}
