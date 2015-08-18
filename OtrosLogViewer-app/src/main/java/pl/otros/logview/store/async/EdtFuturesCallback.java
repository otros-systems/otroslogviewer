package pl.otros.logview.store.async;

import javax.swing.SwingUtilities;

import com.google.common.util.concurrent.FutureCallback;

public abstract class EdtFuturesCallback<V> implements FutureCallback<V> {

	@Override
	public void onFailure(final Throwable t) {
		SwingUtilities.invokeLater(() -> EdtFuturesCallback.this.onFailureEdt(t));

	}

	@Override
	public void onSuccess(final V arg) {
		SwingUtilities.invokeLater(() -> EdtFuturesCallback.this.onSuccessEdt(arg));

	}

	public abstract void onFailureEdt(Throwable arg0);

	public abstract void onSuccessEdt(V arg0);

}
