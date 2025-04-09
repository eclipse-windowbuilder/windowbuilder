/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfosetObjectBefore;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.awt.LayoutManager;

import javax.swing.JDesktopPane;

/**
 * Model for {@link JDesktopPane}.
 *
 * @author mitin_aa
 * @coverage swing.model
 */
public final class JDesktopPaneInfo extends JLayeredPaneInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JDesktopPaneInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// A workaround for http://www.eclipse.org/forums/index.php/t/264347/
		// Under OS X JDesktopPane has an internal layout manager installed, see
		// http://hg.openjdk.java.net/macosx-port/macosx-port/jdk/file/2a8a869b2a60/src/macosx/classes/com/apple/laf/AquaInternalFramePaneUI.java
		// The workaround is to set layout to null.
		if (EnvironmentUtils.IS_MAC) {
			addBroadcastListener(new JavaInfosetObjectBefore() {
				@Override
				public void invoke(JavaInfo target, Object[] objectRef) throws Exception {
					if (target == JDesktopPaneInfo.this) {
						JDesktopPane pane = (JDesktopPane) objectRef[0];
						LayoutManager layout = pane.getLayout();
						if (layout != null) {
							String layoutClassName = layout.getClass().getName();
							if ("com.apple.laf.AquaInternalFramePaneUI$DockLayoutManager".equals(layoutClassName)) {
								pane.setLayout(null);
							}
						}
					}
				}
			});
		}
	}
}
