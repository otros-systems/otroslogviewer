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
import java.util.ArrayList;
import java.util.List;

public class ListUncaughtExceptionHandlers implements UncaughtExceptionHandler {

	private List<UncaughtExceptionHandler> list;
	

	public ListUncaughtExceptionHandlers(UncaughtExceptionHandler...handlers){
		list = new ArrayList<Thread.UncaughtExceptionHandler>();
		for (UncaughtExceptionHandler uncaughtExceptionHandler : handlers) {
			list.add(uncaughtExceptionHandler);
		}
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		synchronized (list) {
			for (UncaughtExceptionHandler handler : list) {
				handler.uncaughtException(thread, throwable);
			}			
		}
	}

	public boolean add(UncaughtExceptionHandler arg0) {
		synchronized(list){
			return list.add(arg0);			
		}
	}
	
	public boolean remove(UncaughtExceptionHandler arg0) {
		synchronized (list) {
			return list.remove(arg0);			
		}
	}
}
