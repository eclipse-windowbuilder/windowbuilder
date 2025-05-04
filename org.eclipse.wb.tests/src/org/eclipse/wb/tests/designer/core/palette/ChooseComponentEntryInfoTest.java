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
import org.eclipse.wb.core.editor.palette.model.IPaletteSite;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ChooseComponentEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for {@link ChooseComponentEntryInfo}.
 *
 * @author scheglov_ke
 */
public class ChooseComponentEntryInfoTest extends AbstractPaletteTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_access() throws Exception {
		ChooseComponentEntryInfo entry = new ChooseComponentEntryInfo();
		assertNotNull(entry.getIcon());
		assertNotNull(entry.getName());
		assertNotNull(entry.getDescription());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parse
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_parse() throws Exception {
		addPaletteExtension(new String[]{
				"<category id='category_1' name='category 1'>",
				"  <entry id='system.chooseComponent' name='my name'"
						+ " class='"
						+ ChooseComponentEntryInfo.class.getName()
						+ "'/>",
		"</category>"});
		PaletteInfo palette = loadPalette();
		// prepare entry
		CategoryInfo category = palette.getCategory("category_1");
		ChooseComponentEntryInfo entry = (ChooseComponentEntryInfo) category.getEntries().get(0);
		// check component
		assertSame(category, entry.getCategory());
		assertEquals("system.chooseComponent", entry.getId());
		assertEquals("my name", entry.getName());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tool
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_createTool_cancel() throws Exception {
		addPaletteExtension(new String[]{
				"<category id='category_1' name='category 1'>",
				"  <entry id='system.chooseComponent' class='"
						+ ChooseComponentEntryInfo.class.getName()
						+ "'/>",
		"</category>"});
		JavaInfo panel = parseEmptyPanel();
		final PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
		manager.reloadPalette();
		// set palette site
		IPaletteSite.Helper.setSite(panel, new IPaletteSite.Empty() {
			@Override
			public Shell getShell() {
				return DesignerPlugin.getShell();
			}
		});
		// prepare entry
		final ChooseComponentEntryInfo entry;
		{
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("category_1");
			entry = (ChooseComponentEntryInfo) category.getEntries().get(0);
		}
		// initialize
		assertTrue(entry.initialize(null, panel));
		// create tool
		CreationTool creationTool;
		{
			final CreationTool[] tools = new CreationTool[1];
			new UiContext().executeAndCheck(new FailableRunnable<>() {
				@Override
				public void run() throws Exception {
					tools[0] = (CreationTool) entry.createTool();
				}
			}, new FailableConsumer<>() {
				@Override
				public void accept(SWTBot bot) {
					SWTBot shell = bot.shell("Open type").bot();
					shell.button("Cancel").click();
				}
			});
			creationTool = tools[0];
		}
		// check tool
		assertNull(creationTool);
	}

	@Test
	public void test_createTool_select() throws Exception {
		addPaletteExtension(new String[]{
				"<category id='category_1' name='category 1'>",
				"  <entry id='system.chooseComponent' class='"
						+ ChooseComponentEntryInfo.class.getName()
						+ "'/>",
		"</category>"});
		JavaInfo panel = parseEmptyPanel();
		final PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
		manager.reloadPalette();
		// set palette site
		IPaletteSite.Helper.setSite(panel, new IPaletteSite.Empty() {
			@Override
			public Shell getShell() {
				return DesignerPlugin.getShell();
			}

			@Override
			public PaletteInfo getPalette() {
				return manager.getPalette();
			}

			@Override
			public void addCommand(Command command) {
				manager.commands_add(command);
			}
		});
		// prepare entry
		final ChooseComponentEntryInfo entry;
		{
			PaletteInfo palette = manager.getPalette();
			CategoryInfo category = palette.getCategory("category_1");
			entry = (ChooseComponentEntryInfo) category.getEntries().get(0);
		}
		// initialize
		assertTrue(entry.initialize(null, panel));
		// create tool
		CreationTool creationTool;
		{
			final CreationTool[] tools = new CreationTool[1];
			new UiContext().executeAndCheck(new FailableRunnable<>() {
				@Override
				public void run() throws Exception {
					tools[0] = (CreationTool) entry.createTool();
				}
			}, new FailableConsumer<>() {
				@Override
				public void accept(SWTBot bot) {
					animateOpenTypeSelection(bot, "JButton", "OK");
				}
			});
			creationTool = tools[0];
		}
		// check tool
		{
			ICreationFactory creationFactory = creationTool.getFactory();
			creationFactory.activate();
			// check new object
			JavaInfo javaInfo = (JavaInfo) creationFactory.getNewObject();
			assertEquals(
					"new javax.swing.JButton(\"New button\")",
					javaInfo.getCreationSupport().add_getSource(null));
		}
	}

	/**
	 * In GWT it is possible following situation: user opens form, so its {@link ClassLoader} created.
	 * Then he creates new <code>Composite</code> and tries to use in already opened form. This does
	 * not work, because new component has {@link IType}, but {@link ClassLoader} is already fixed, we
	 * can not add new {@link Class} into it.
	 * <p>
	 * So, we detect {@link IType} presence and ask about reparse.
	 */
	@DisposeProjectAfter
	// Test may get stuck on the Linux build...
	@Ignore
	@Test
	public void test_createTool_inProject_butNotInClassLoader() throws Exception {
		JavaInfo panel = parseEmptyPanel();
		// set palette site
		{
			final PaletteManager manager = new PaletteManager(panel, TOOLKIT_ID);
			manager.reloadPalette();
			IPaletteSite.Helper.setSite(panel, new IPaletteSite.Empty() {
				@Override
				public Shell getShell() {
					return DesignerPlugin.getShell();
				}

				@Override
				public PaletteInfo getPalette() {
					return manager.getPalette();
				}

				@Override
				public void addCommand(Command command) {
					manager.commands_add(command);
				}
			});
		}
		// set page site
		final AtomicBoolean reparsed = new AtomicBoolean();
		DesignPageSite.Helper.setSite(panel, new DesignPageSite() {
			@Override
			public void reparse() {
				reparsed.set(true);
			}
		});
		// add new project, so its IType is visible, but not in ClassLoader
		TestProject newProject = new TestProject("NewProject");
		try {
			setFileContentSrc(
					newProject.getProject(),
					"my/classes/MyClass.java",
					getSource(
							"// filler filler filler filler filler",
							"package my.classes;",
							"public class MyClass {",
							"}"));
			m_testProject.addRequiredProject(newProject);
			waitForAutoBuild();
			// prepare entry
			final ChooseComponentEntryInfo entry = new ChooseComponentEntryInfo();
			assertTrue(entry.initialize(null, panel));
			// create tool
			{
				final CreationTool[] tools = new CreationTool[1];
				new UiContext().executeAndCheck(new FailableRunnable<>() {
					@Override
					public void run() throws Exception {
						tools[0] = (CreationTool) entry.createTool();
					}
				}, new FailableConsumer<>() {
					@Override
					public void accept(SWTBot bot) {
						animateOpenTypeSelection(bot, "MyClass", "OK");
						SWTBot shell = bot.shell("Unable to load component").bot();
						shell.button("Yes").click();
					}
				});
			}
		} finally {
			newProject.dispose();
		}
		// should be reparsed
		assertTrue(reparsed.get());
	}
}
