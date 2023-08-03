/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.XML.palette;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.xml.editor.palette.model.CategoryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.ComponentEntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.EntryInfo;
import org.eclipse.wb.internal.core.xml.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;
import org.junit.After;

/**
 * Abstract superclass for {@link PaletteInfo} tests.
 *
 * @author scheglov_ke
 */
public abstract class AbstractPaletteTest extends AbstractCoreTest {
	private static final String POINT_ID = "org.eclipse.wb.core.toolkits";
	protected static final String PALETTE_EXTENSION_ID = "testPaletteId";
	protected static final String TOOLKIT_ID = "test.toolkit";

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		removeToolkitExtension();
		if (m_lastManager != null) {
			m_lastManager.commands_clear();
			m_lastManager.commands_write();
			m_lastManager = null;
		}
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Toolkit/palette extension operations
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds dynamic "palette" extension.
	 */
	protected static void addPaletteExtension(String... paletteLines) {
		addPaletteExtension(PALETTE_EXTENSION_ID, paletteLines);
	}

	/**
	 * Adds dynamic "palette" extension.
	 */
	protected static void addPaletteExtension(String extensionId, String[] paletteLines) {
		String[] toolkitLines = new String[1 + paletteLines.length + 1];
		toolkitLines[0] = "    <palette>";
		System.arraycopy(paletteLines, 0, toolkitLines, 1, paletteLines.length);
		toolkitLines[toolkitLines.length - 1] = "    </palette>";
		addToolkitExtension(extensionId, toolkitLines);
	}

	/**
	 * Adds dynamic "toolkit" extension (including "palette" extension).
	 */
	protected static void addToolkitExtension(String extensionId, String[] lines) {
		String contribution =
				getSource3(
						new String[]{"  <toolkit id='" + TOOLKIT_ID + "'>"},
						lines,
						new String[]{"  </toolkit>"});
		TestUtils.addDynamicExtension(POINT_ID, extensionId, contribution);
	}

	/**
	 * Removes dynamic "toolkit" extension.
	 */
	protected static void removeToolkitExtension() {
		removeToolkitExtension(PALETTE_EXTENSION_ID);
	}

	/**
	 * Removes dynamic "toolkit" extension.
	 */
	protected static void removeToolkitExtension(String extensionId) {
		TestUtils.removeDynamicExtension(POINT_ID, extensionId);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private PaletteManager m_lastManager;

	/**
	 * Parses empty {@link Shell}.
	 */
	protected final XmlObjectInfo parseEmptyPanel() throws Exception {
		return parse("<Shell/>");
	}

	/**
	 * @return the {@link PaletteInfo} loaded for empty {@link Shell}.
	 */
	protected final PaletteInfo loadPalette() throws Exception {
		XmlObjectInfo panel = parseEmptyPanel();
		return loadPalette(panel);
	}

	/**
	 * @return the {@link PaletteManager} loaded for empty {@link Shell}.
	 */
	protected final PaletteManager loadManager() throws Exception {
		XmlObjectInfo panel = parseEmptyPanel();
		m_lastManager = new PaletteManager(panel, TOOLKIT_ID);
		m_lastManager.reloadPalette();
		return m_lastManager;
	}

	/**
	 * @return the {@link PaletteInfo} loaded for given {@link XmlObjectInfo}.
	 */
	protected final PaletteInfo loadPalette(XmlObjectInfo javaInfo) {
		m_lastManager = new PaletteManager((XmlObjectInfo) javaInfo.getRoot(), TOOLKIT_ID);
		m_lastManager.reloadPalette();
		return m_lastManager.getPalette();
	}

	/**
	 * @return the {@link ComponentEntryInfo} with given id.
	 */
	protected final ComponentEntryInfo loadSingleComponent(String id) throws Exception {
		{
			EntryInfo entry = loadSingleEntry(id);
			if (entry instanceof ComponentEntryInfo) {
				return (ComponentEntryInfo) entry;
			}
		}
		throw new IllegalArgumentException("No component: " + id);
	}

	/**
	 * @return the {@link EntryInfo} with given id.
	 */
	protected final EntryInfo loadSingleEntry(String id) throws Exception {
		PaletteInfo palette = loadPalette();
		for (CategoryInfo category : palette.getCategories()) {
			for (EntryInfo entry : category.getEntries()) {
				if (StringUtils.equals(entry.getId(), id)) {
					return entry;
				}
			}
		}
		throw new IllegalArgumentException("No entry: " + id);
	}

	/**
	 * @return the {@link DocumentElement} which will be added by {@link CreationSupport} of given
	 *         {@link XmlObjectInfo}.
	 */
	protected static DocumentElement getNewCreationElement(XmlObjectInfo object) throws Exception {
		DocumentElement parentElement = new DocumentElement();
		object.getCreationSupport().addElement(parentElement, 0);
		return parentElement.getChildAt(0);
	}
}
