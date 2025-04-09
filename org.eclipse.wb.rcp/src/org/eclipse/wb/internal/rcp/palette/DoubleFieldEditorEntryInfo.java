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
package org.eclipse.wb.internal.rcp.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.BundleResourceProvider;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.Activator;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * {@link EntryInfo} for adding <code>DoubleFieldEditor</code>.
 *
 * @author scheglov_ke
 * @coverage rcp.editor.palette
 */
public final class DoubleFieldEditorEntryInfo extends ToolEntryInfo {
	private static final String TYPE_NAME = "org.eclipse.wb.swt.DoubleFieldEditor";
	private static final ImageDescriptor ICON = BundleResourceProvider.get(Activator.PLUGIN_ID)
			.getImageDescriptor("wbp-meta/org/eclipse/wb/swt/DoubleFieldEditor.png");

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DoubleFieldEditorEntryInfo() throws Exception {
		setId(getClass().getName());
		setName(PaletteMessages.DoubleFieldEditorEntryInfo_name);
		setDescription(PaletteMessages.DoubleFieldEditorEntryInfo_description);
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

	@Override
	public Tool createTool() throws Exception {
		ProjectUtils.ensureResourceType(m_javaProject, Activator.getDefault().getBundle(), TYPE_NAME);
		// create tool
		ICreationFactory factory = new ICreationFactory() {
			private JavaInfo m_javaInfo;

			@Override
			public void activate() throws Exception {
				CreationSupport creationSupport = new ConstructorCreationSupport();
				m_javaInfo = JavaInfoUtils.createJavaInfo(m_editor, TYPE_NAME, creationSupport);
				m_javaInfo.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
			}

			@Override
			public Object getNewObject() {
				return m_javaInfo;
			}
		};
		return new CreationTool(factory);
	}
}
