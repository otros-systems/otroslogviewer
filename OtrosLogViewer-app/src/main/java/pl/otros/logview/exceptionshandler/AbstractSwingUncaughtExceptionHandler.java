/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.exceptionshandler;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.swing.SwingUtilities;

public abstract class AbstractSwingUncaughtExceptionHandler implements UncaughtExceptionHandler{

	@Override
	public final void uncaughtException(final Thread arg0, final Throwable arg1) {
		if (SwingUtilities.isEventDispatchThread()){
			uncaughtExceptionInSwingEDT(arg0, arg1);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					uncaughtExceptionInSwingEDT(arg0, arg1);
				}
			});
		}
	}
	
	protected abstract void uncaughtExceptionInSwingEDT(Thread arg0, Throwable arg1);

}
