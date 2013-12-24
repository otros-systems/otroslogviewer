package pl.otros.logview.exceptionshandler.errrorreport;

import pl.otros.logview.gui.OtrosApplication;

public class ErrorReportCollectingContext {

	private OtrosApplication otrosApplication;
	private Throwable throwable;
	private Thread thread;

	public OtrosApplication getOtrosApplication() {
		return otrosApplication;
	}

	public void setOtrosApplication(OtrosApplication otrosApplication) {
		this.otrosApplication = otrosApplication;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

}
