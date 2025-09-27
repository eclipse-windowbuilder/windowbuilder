/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.execution;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.widgets.Display;

import java.beans.Beans;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Utilities for executing actions, such as {@link RunnableEx}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public class ExecutionUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private ExecutionUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Sleep
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sleeps given number of milliseconds, ignoring exceptions.
	 */
	public static void sleep(int millis) {
		long end = System.currentTimeMillis() + millis;
		do {
			final long leftMillis = end - System.currentTimeMillis();
			if (leftMillis >= 0) {
				runIgnore(() -> Thread.sleep(leftMillis));
			}
		} while (System.currentTimeMillis() < end);
	}

	/**
	 * Waits given number of milliseconds and runs events loop every 1 millisecond. At least one
	 * events loop will be executed. If current thread is not UI thread, then this method works just
	 * as {@link #sleep(int)}.
	 */
	public static void waitEventLoop(int millis) {
		Display display = Display.getCurrent();
		if (display != null) {
			long nanos = millis * 1000000L;
			long start = System.nanoTime();
			do {
				sleep(0);
				while (display.readAndDispatch()) {
					// do nothing
				}
			} while (System.nanoTime() - start < nanos);
		} else {
			sleep(millis);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// void
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Runs given {@link RunnableEx} and ignores exceptions.
	 *
	 * @return <code>true</code> if execution was finished without exception.
	 */
	public static boolean runIgnore(RunnableEx runnable) {
		try {
			runnable.run();
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	/**
	 * Runs given {@link RunnableEx} and logs exceptions using {@link DesignerPlugin#log(Throwable)}.
	 *
	 * @return <code>true</code> if execution was finished without exception.
	 */
	public static boolean runLog(RunnableEx runnable) {
		try {
			runnable.run();
			return true;
		} catch (Throwable e) {
			DesignerPlugin.log(e);
			return false;
		}
	}

	/**
	 * Runs given {@link RunnableEx} and re-throws exceptions using {@link RuntimeException}.
	 */
	public static void runRethrow(RunnableEx runnable) {
		try {
			runnable.run();
		} catch (Throwable e) {
			throw ReflectionUtils.propagate(e);
		}
	}

	/**
	 * Runs given {@link RunnableEx} and re-throws exceptions using {@link RuntimeException}.
	 */
	public static void runRethrow(RunnableEx runnable, String format, Object... args) {
		try {
			runnable.run();
		} catch (Throwable e) {
			String message = String.format(format, args);
			throw new RuntimeException(message, e);
		}
	}

	/**
	 * Ensures that {@link Beans#isDesignTime()} returns <code>true</code> and runs given
	 * {@link RunnableEx}.
	 */
	public static void runDesignTime(RunnableEx runnable) throws Exception {
		boolean old_designTime = Beans.isDesignTime();
		try {
			Beans.setDesignTime(true);
			runnable.run();
		} finally {
			Beans.setDesignTime(old_designTime);
		}
	}

	/**
	 * Ensures that {@link Beans#isDesignTime()} returns <code>true</code> and runs given
	 * {@link Callable}.
	 */
	public static <T> T runDesignTime(Callable<T> runnable) throws Exception {
		boolean old_designTime = Beans.isDesignTime();
		try {
			Beans.setDesignTime(true);
			return runnable.call();
		} finally {
			Beans.setDesignTime(old_designTime);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Runs given {@link RunnableEx} inside of UI thread, using {@link Display#syncExec(Runnable)}.
	 *
	 * @return <code>true</code> if {@link RunnableEx} was executed without any {@link Exception}.
	 */
	public static boolean runLogUI(final RunnableEx runnable) {
		final boolean[] success = new boolean[1];
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				success[0] = ExecutionUtils.runLog(runnable);
			}
		});
		return success[0];
	}

	/**
	 * Runs given {@link RunnableEx} inside of UI thread, using {@link Display#syncExec(Runnable)}.
	 */
	public static void runRethrowUI(final RunnableEx runnable) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ExecutionUtils.runRethrow(runnable);
			}
		});
	}

	/**
	 * Runs given {@link Callable} inside of UI thread, using
	 * {@link Display#syncExec(Runnable)}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T runObjectUI(final Callable<T> runnable) {
		final Object[] result = new Object[1];
		runRethrowUI(() -> result[0] = runObject(runnable));
		return (T) result[0];
	}

	/**
	 * Runs given {@link RunnableEx} as {@link #runLog(RunnableEx)}, but using
	 * {@link CompletableFuture#runAsync(Runnable)} and
	 * {@link Display#asyncExec(Runnable)}.
	 * 
	 * @return the new CompletableFuture
	 */
	public static CompletableFuture<Void> runLogLater(final RunnableEx runnable) {
		return CompletableFuture.runAsync(() -> runLog(runnable), Display.getDefault()::asyncExec);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Runs given {@link Callable} and re-throws exceptions using {@link RuntimeException}.
	 *
	 * @return the {@link Object} returned by {@link Callable#call()}.
	 */
	public static <T> T runObject(Callable<T> runnable) {
		try {
			return runnable.call();
		} catch (Throwable e) {
			throw ReflectionUtils.propagate(e);
		}
	}

	/**
	 * Runs given {@link RunnableEx} and re-throws exceptions using {@link RuntimeException}.
	 *
	 * @return the {@link Object} returned by {@link Callable#run()}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T runObject(ObjectInfo object, final Callable<T> runnable) {
		final Object[] result = {null};
		run(object, () -> result[0] = runnable.call());
		return (T) result[0];
	}

	/**
	 * Runs given {@link Callable} and re-throws exceptions using {@link RuntimeException}.
	 *
	 * @return the {@link Object} returned by {@link Callable#call()}.
	 */
	public static <T> T runObject(Callable<T> runnable, String format, Object... args) {
		try {
			return runnable.call();
		} catch (Throwable e) {
			String message = String.format(format, args);
			throw new Error(message, e);
		}
	}

	/**
	 * Runs given {@link Callable} and ignores exceptions.
	 *
	 * @return the {@link Object} returned by {@link Callable#call()} or <code>defaultValue</code> if
	 *         exception happened.
	 */
	public static <T> T runObjectIgnore(Callable<T> runnable, T defaultValue) {
		try {
			return runnable.call();
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * Runs given {@link Callable} and logs exceptions using {@link DesignerPlugin#log(Throwable)}.
	 *
	 * @return the {@link Object} returned by {@link Callable#call()} or <code>defaultValue</code> if
	 *         exception was logged.
	 */
	public static <T> T runObjectLog(Callable<T> runnable, T defaultValue) {
		try {
			return runnable.call();
		} catch (Throwable e) {
			DesignerPlugin.log(e);
			return defaultValue;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObjectInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Runs given {@link RunnableEx} inside of edit operation.
	 *
	 * @return <code>true</code> if execution was finished without exception.
	 */
	public static boolean run(ObjectInfo objectInfo, RunnableEx runnable) {
		try {
			objectInfo.startEdit();
			runnable.run();
			objectInfo.endEdit();
			return true;
		} catch (Throwable e) {
			IDesignPageSite site = IDesignPageSite.Helper.getSite(objectInfo);
			if (site != null) {
				site.handleException(e);
				return false;
			} else {
				throw ReflectionUtils.propagate(e);
			}
		}
	}

	/**
	 * Performs NOOP edit operation, so just {@link ObjectInfo#refresh()} if there are no enclosing
	 * edit operation.
	 */
	public static void refresh(ObjectInfo objectInfo) {
		run(objectInfo, () -> {
			// do nothing, we need just refresh
		});
	}

	/**
	 * Runs given {@link RunnableEx} inside of edit operation, using
	 * {@link Display#asyncExec(Runnable)}.
	 */
	public static void runLater(final ObjectInfo objectInfo, final RunnableEx runnable) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				ExecutionUtils.run(objectInfo, runnable);
			}
		});
	}
}
