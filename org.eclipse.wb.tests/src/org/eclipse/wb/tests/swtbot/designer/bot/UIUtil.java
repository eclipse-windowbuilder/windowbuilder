/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.swtbot.designer.bot;

import org.eclipse.swt.SwtCallable;
import org.eclipse.swt.SwtRunnable;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.PlatformUI;

/**
 * Inspired by {@link UIThreadRunnable} to support the execution of tasks within
 * the UI thread. Unlike the SWTBot class however, we need to also support the
 * case where those tasks throw an exception.
 */
public final class UIUtil {
	private UIUtil() {
		// This utility class should never be instantiated
	}

	/**
	 * Executes the given {@code runnable} within the UI thread. Any exception
	 * thrown by this task is converted into a {@link RuntimeException}.
	 *
	 * @param <E>      The type of exception thrown by this runnable.
	 * @param runnable The task to be executed in the UI thread.
	 */
	public static <E extends Exception> void syncExec(SwtRunnable<E> runnable) {
		syncCall(() -> {
			runnable.run();
			return null;
		});
	}

	/**
	 * Executes the given {@code callable} within the UI thread and returns the
	 * result. Any exception thrown by this task is converted into a
	 * {@link RuntimeException}.
	 *
	 * @param <E>      The type of exception thrown by this runnable.
	 * @param callable The task to be executed in the UI thread.
	 */
	public static <U, E extends Exception> U syncCall(SwtCallable<U, E> callable) {
		try {
			return PlatformUI.getWorkbench().getDisplay().syncCall(callable);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
