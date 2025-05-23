/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.swing.FormLayout.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.swing.FormLayout.Activator;
import org.eclipse.wb.internal.swing.FormLayout.parser.DefaultComponentFactoryCreationSupport;

import org.eclipse.jface.resource.ImageDescriptor;

import com.jgoodies.forms.factories.DefaultComponentFactory;

/**
 * {@link EntryInfo} creates {@link DefaultComponentFactory#createTitle(String)}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class DefaultComponentFactoryCreateTitleEntryInfo
extends
DefaultComponentFactoryEntryInfo {
	private static final ImageDescriptor ICON = Activator
			.getImageDescriptor("DefaultComponentFactory/createTitle_java.lang.String_.gif");

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DefaultComponentFactoryCreateTitleEntryInfo() {
		setId(getClass().getName());
		setName("createTitle(String)");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon() {
		return ICON;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ToolEntryInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Tool createTool() throws Exception {
		ICreationFactory factory = new ICreationFactory() {
			private JavaInfo m_javaInfo;

			@Override
			public void activate() throws Exception {
				String source = "createTitle(\"New JGoodies title\")";
				m_javaInfo =
						JavaInfoUtils.createJavaInfo(
								m_editor,
								"javax.swing.JLabel",
								new DefaultComponentFactoryCreationSupport(source));
				m_javaInfo.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
			}

			@Override
			public Object getNewObject() {
				return m_javaInfo;
			}
		};
		// return tool
		ensureLibrary();
		return new CreationTool(factory);
	}
}
