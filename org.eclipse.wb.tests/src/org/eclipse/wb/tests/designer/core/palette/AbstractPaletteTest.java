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
package org.eclipse.wb.tests.designer.core.palette;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.palette.PaletteManager;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.AfterEach;

import java.util.Objects;

import javax.swing.JPanel;

/**
 * Abstract superclass for {@link PaletteInfo} tests.
 *
 * @author scheglov_ke
 */
public abstract class AbstractPaletteTest extends SwingModelTest {
	private static final String POINT_ID = "org.eclipse.wb.core.toolkits";
	protected static final String PALETTE_EXTENSION_ID = "testPaletteId";
	protected static final String TOOLKIT_ID = "test.toolkit";

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@AfterEach
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
	protected static void addPaletteExtension(String[] paletteLines) {
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
	 * Parses empty {@link JPanel}.
	 */
	protected final JavaInfo parseEmptyPanel() throws Exception {
		return parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * @return the {@link PaletteInfo} loaded for empty {@link JPanel}.
	 */
	protected final PaletteInfo loadPalette() throws Exception {
		JavaInfo panel = parseEmptyPanel();
		return loadPalette(panel);
	}

	/**
	 * @return the {@link PaletteManager} loaded for empty {@link JPanel}.
	 */
	protected final PaletteManager loadManager() throws Exception {
		JavaInfo panel = parseEmptyPanel();
		m_lastManager = new PaletteManager(panel, TOOLKIT_ID);
		m_lastManager.reloadPalette();
		return m_lastManager;
	}

	/**
	 * @return the {@link PaletteInfo} loaded for given {@link JavaInfo}.
	 */
	protected final PaletteInfo loadPalette(JavaInfo javaInfo) {
		m_lastManager = new PaletteManager(javaInfo.getRootJava(), TOOLKIT_ID);
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
				if (Objects.equals(entry.getId(), id)) {
					return entry;
				}
			}
		}
		throw new IllegalArgumentException("No entry: " + id);
	}
}
