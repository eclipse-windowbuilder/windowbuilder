/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.core;

import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.preferences.PreferenceToSystemForwarder;
import org.eclipse.wb.internal.core.utils.product.ProductInfo;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import static org.burningwave.core.assembler.StaticComponentContainer.Modules;

import org.osgi.framework.BundleContext;

import java.io.InputStream;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author scheglov_ke
 * @coverage core
 */
public class DesignerPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.wb.core";
	private static DesignerPlugin m_plugin;

	////////////////////////////////////////////////////////////////////////////
	//
	// Bundle operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		m_plugin = this;
		BundleResourceProvider.configureCleanUp(context);
		addLogListener();
		if (EnvironmentUtils.IS_LINUX) {
			installPreferenceForwarder();
		}
		try {
			// https://github.com/eclipse-windowbuilder/windowbuilder/issues/1027
			exportAllModulesToAllModules();
		} catch (Throwable e) {
			log(e);
		}
	}

	private void exportAllModulesToAllModules() {
		try {
			ClassLoader ccl = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				Modules.exportAllToAll();
			} finally {
				Thread.currentThread().setContextClassLoader(ccl);
			}
		} catch (Exception exc) {
			log(exc);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		m_plugin = null;
		super.stop(context);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the instance of {@link DesignerPlugin}
	 */
	public static DesignerPlugin getDefault() {
		return m_plugin;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Preferences
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IPreferenceStore} for Designer.
	 */
	public static IPreferenceStore getPreferences() {
		return getDefault().getPreferenceStore();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Display/Shell
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Display} instance, current (if in GUI thread) or default.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/**
	 * @return the active {@link IWorkbenchWindow}.
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * @return the active {@link IWorkbenchPage}.
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		return window == null ? null : window.getActivePage();
	}

	/**
	 * @return active {@link IEditorPart} on active workbench page.
	 */
	public static IEditorPart getActiveEditor() {
		IWorkbenchPage page = getActivePage();
		return page != null ? page.getActiveEditor() : null;
	}

	/**
	 * @return the {@link Shell} of active {@link IWorkbenchWindow}.
	 */
	public static Shell getShell() {
		if (getActiveWorkbenchWindow() != null) {
			return getActiveWorkbenchWindow().getShell();
		}
		return null;
	}

	/////////////////////////////////////////////////////1.10.1.qualifier///////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean m_ctrlPressed;
	private static boolean m_shiftPressed;

	/**
	 * Adds {@link Display} filter that redirects {@link SWT#MouseWheel} event to {@link Control}
	 * under mouse cursor.
	 */
	private static void addMouseWheelRedirector() {
		// Windows-only code
		if (EnvironmentUtils.IS_WINDOWS) {
			final Listener listener = event -> {
				Control cursorControl = Display.getDefault().getCursorControl();
				if (cursorControl instanceof Scrollable && cursorControl != event.widget) {
					event.doit = false;
					if ((cursorControl.getStyle() & SWT.V_SCROLL) != 0) {
						// prepare count and direction
						OSSupport.get().scroll(cursorControl, event.count);
					}
				}
			};
			final Display display = Display.getDefault();
			display.asyncExec(() -> display.addFilter(SWT.MouseWheel, listener));
		}
	}

	/**
	 * Adds {@link Display} filters for tracking interesting events.
	 */
	private static void setupEventFilters() {
		final Listener listener = event -> {
			// Ctrl tracking
			if (event.keyCode == SWT.CTRL) {
				if (event.type == SWT.KeyDown) {
					m_ctrlPressed = true;
				}
				if (event.type == SWT.KeyUp) {
					m_ctrlPressed = false;
				}
				return;
			}
			// Shift tracking
			if (event.keyCode == SWT.SHIFT) {
				if (event.type == SWT.KeyDown) {
					m_shiftPressed = true;
				}
				if (event.type == SWT.KeyUp) {
					m_shiftPressed = false;
				}
				return;
			}
		};
		//
		final Display display = Display.getDefault();
		display.asyncExec(() -> {
			display.addFilter(SWT.MouseDown, listener);
			display.addFilter(SWT.MouseUp, listener);
			display.addFilter(SWT.KeyDown, listener);
			display.addFilter(SWT.KeyUp, listener);
			display.addFilter(SWT.Traverse, listener);
		});
	}

	/**
	 * @return <code>true</code> if Ctrl key is now pressed.
	 */
	public static boolean isCtrlPressed() {
		return m_ctrlPressed;
	}

	/**
	 * @return <code>true</code> if Shift key is now pressed.
	 */
	public static boolean isShiftPressed() {
		return m_shiftPressed;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Logging
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean m_displayExceptionOnConsole = true;

	/**
	 * Sets flag if {@link #log(Throwable)} should display exception on console.
	 */
	public static void setDisplayExceptionOnConsole(boolean displayExceptionOnConsole) {
		m_displayExceptionOnConsole = displayExceptionOnConsole;
	}

	/**
	 * Logs given {@link IStatus} into Eclipse .log.
	 */
	public static void log(IStatus status) {
		DesignerPlugin pluginInstance = getDefault();
		if (pluginInstance != null) {
			pluginInstance.getLog().log(status);
		}
	}

	/**
	 * Logs {@link IStatus} with given message into Eclipse .log.
	 */
	public static void log(String message) {
		log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.INFO, message, null));
	}

	/**
	 * Logs {@link IStatus} with given exception into Eclipse .log.
	 */
	public static void log(Throwable e) {
		// print on console for easy debugging
		if (m_displayExceptionOnConsole) {
			e.printStackTrace();
		}
		// log into Eclipse .log
		{
			String message = e.getMessage();
			if (message == null) {
				message = e.getClass().getName();
			}
			String versionString = ProductInfo.getProduct().getVersion().toString();
			String buildString = ProductInfo.getProduct().getBuild();
			log(
					"Designer ["
							+ versionString
							+ (versionString.endsWith(buildString) ? "" : "." + buildString)
							+ "]: "
							+ message,
							e);
		}
	}

	/**
	 * Logs {@link IStatus} with given message and exception into Eclipse .log.
	 */
	public static void log(String message, Throwable e) {
		log(createStatus(message, e));
	}

	/**
	 * Creates {@link IStatus} for given message and exception.
	 */
	public static Status createStatus(String message, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e) {
			@Override
			public boolean isMultiStatus() {
				return true;
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Track last log entry
	//
	////////////////////////////////////////////////////////////////////////////
	private IStatus m_lastStatus;

	private void addLogListener() {
		ILog log = getLog();
		log.addLogListener((status, plugin) -> setLastStatus(status));
	}

	private void setLastStatus(IStatus lastStatus) {
		m_lastStatus = lastStatus;
	}

	private IStatus getLastStatus0() {
		return m_lastStatus;
	}

	/**
	 * @return the {@link IStatus} instance which was last logged by the eclipse log.
	 */
	public static IStatus getLastStatus() {
		DesignerPlugin pluginInstance = getDefault();
		if (pluginInstance != null) {
			return pluginInstance.getLastStatus0();
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resources
	//
	////////////////////////////////////////////////////////////////////////////
	private static final BundleResourceProvider m_resourceProvider =
			BundleResourceProvider.get(PLUGIN_ID);

	/**
	 * @return the {@link InputStream} for file from plugin directory.
	 */
	public static InputStream getFile(String path) {
		return m_resourceProvider.getFile(path);
	}

	/**
	 * @return the {@link Image} from "icons" directory, with caching.
	 */
	public static Image getImage(String path) {
		return m_resourceProvider.getImage("icons/" + path);
	}

	/**
	 * @return the {@link ImageDescriptor} from "icons" directory.
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return m_resourceProvider.getImageDescriptor("icons/" + path);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Linux only
	//
	////////////////////////////////////////////////////////////////////////////
	private void installPreferenceForwarder() {
		new PreferenceToSystemForwarder(getPreferenceStore(),
				IPreferenceConstants.P_COMMON_LINUX_DISABLE_SCREENSHOT_WORKAROUNDS,
				"__wbp.linux.disableScreenshotWorkarounds");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Set-up during first editor
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean m_preEditorConfigured = false;

	/**
	 * This is fast method which performs configuration directly before first use of editor.
	 */
	public static synchronized void configurePreEditor() {
		if (m_preEditorConfigured) {
			return;
		}
		m_preEditorConfigured = true;
		// add listeners
		setupEventFilters();
		addMouseWheelRedirector();
	}
}
