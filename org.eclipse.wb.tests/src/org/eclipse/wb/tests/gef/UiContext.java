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
package org.eclipse.wb.tests.gef;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;

import java.util.Set;

/**
 * Helper for testing SWT UI.
 *
 * @author scheglov_ke
 */
public class UiContext {
	private final Display m_display;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public UiContext() {
		m_display = Display.getCurrent();
	}

	public UiContext(Display display) {
		m_display = display;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Executes one {@link FailableRunnable} (can block UI) and uses other
	 * {@link FailableConsumer} to check result.
	 */
	public void executeAndCheck(final FailableRunnable<Exception> uiRunnable,
			final FailableConsumer<SWTBot, Exception> checkRunnable) throws Exception {
		final Set<Shell> originalShells = Set.of(m_display.getShells());
		final Throwable[] checkException = new Throwable[1];
		final boolean[] ready = new boolean[1];


		Thread thread = Thread.ofVirtual().name("UIContext_checkThread").start(() -> {
			while (!ready[0]) {
				Thread.yield();
			}
			try {
				Shell activeShell = UIThreadRunnable.syncExec(m_display::getActiveShell);
				checkRunnable.accept(new SWTBot(activeShell));
			} catch (Throwable e) {
				e.printStackTrace();
				checkException[0] = e;
			} finally {
				dispose(originalShells);
			}
		});
		// Start
		m_display.asyncExec(() -> ready[0] = true);
		// Open dialog
		uiRunnable.run();
		// wait for check to finish
		while (thread.isAlive()) {
			m_display.readAndDispatch();
		}
		// check for exception
		if (checkException[0] != null) {
			if (checkException[0] instanceof AssertionError error) {
				throw error;
			}
			if (checkException[0] instanceof WidgetNotFoundException error) {
				throw error;
			}
			throw new Exception("Exception during running 'check' UIRunnable.", checkException[0]);
		}
	}

	/**
	 * Executes one {@link FailableRunnable} (can block UI).
	 */
	public void execute(final FailableRunnable<Exception> uiRunnable) {
		m_display.asyncExec(() -> {
			try {
				uiRunnable.run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Disposes all shells that are not in {@code originalShells}. Must be executed
	 * from the UI thread.
	 */
	private void dispose(Set<Shell> originalShells) {
		execute(() -> {
			for (Shell shell : m_display.getShells()) {
				if (!originalShells.contains(shell)) {
					shell.dispose();
				}
			}
		});
	}
}
