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
package org.eclipse.wb.tests.designer.core.nls.ui;

import org.eclipse.wb.internal.core.nls.bundle.pure.direct.DirectSourceNewComposite;
import org.eclipse.wb.internal.core.nls.ui.NewSourceDialog;
import org.eclipse.wb.internal.core.nls.ui.PropertiesComposite;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import static org.eclipse.swtbot.swt.finder.matchers.WithText.withText;

import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.Test;

/**
 * Tests for {@link NewSourceDialog}.
 *
 * @author scheglov_ke
 */
public class NewSourceDialogTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Open {@link NewSourceDialog} from {@link PropertiesComposite}.
	 */
	@Test
	public void test_openDialog() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JFrame {",
						"  public Test() {",
						"  }",
						"}");
		final NewSourceDialog newSourceDialog = new NewSourceDialog(null, frame);
		//
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() throws Exception {
				newSourceDialog.open();
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("New source").bot();
				assertTrue(shell.radio("Classic Eclipse messages class").isEnabled());
				assertTrue(shell.radio("Modern Eclipse messages class").isEnabled());
				assertTrue(shell.radio("Direct ResourceBundle usage").isEnabled());
				assertTrue(shell.radio("ResourceBundle in field").isEnabled());
				// close dialog
				shell.button("Cancel").click();
			}
		});
	}

	/**
	 * Test for {@link DirectSourceNewComposite}.
	 */
	@Test
	public void test_DirectSource() throws Exception {
		ContainerInfo frame =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JFrame {",
						"  public Test() {",
						"  }",
						"}");
		final NewSourceDialog newSourceDialog = new NewSourceDialog(null, frame);
		//
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() throws Exception {
				newSourceDialog.open();
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) throws Exception {
				SWTBot shell = bot.shell("New source").bot();
				SWTBotButton okButton = shell.button("OK");
				shell.radio("Direct ResourceBundle usage").click();
				{
					SWTBotStyledText styledText = shell.styledText();
					assertEquals(
							"button.setText( ResourceBundle.getBundle(\"full.bundle.name\").getString(\"some.key\") );",
							styledText.getText());
				}
				Widget widget = shell.getFinder().findControls(withText("Property file location and name")).getFirst();
				SWTBot group = new SWTBot(widget);
				// source folder
				{
					SWTBotText sourceFolderText = group.textWithLabel("Source folder: ");
					assertEquals("TestProject/src", sourceFolderText.getText());
					// set bad folder - "OK" button disabled
					sourceFolderText.setText("no-such-folder");
					assertFalse(okButton.isEnabled());
					// restore good folder - "OK" button enabled
					sourceFolderText.setText("TestProject/src");
					assertTrue(okButton.isEnabled());
				}
				// package
				{
					SWTBotText packageText = group.textWithLabel("Package:");
					assertEquals("test", packageText.getText());
					// set bad - "OK" button disabled
					packageText.setText("no-such-package");
					assertFalse(okButton.isEnabled());
					// restore good - "OK" button enabled
					packageText.setText("test");
					assertTrue(okButton.isEnabled());
				}
				// properties file
				{
					SWTBotText fileText = group.textWithLabel("Property file name:");
					assertEquals("messages.properties", fileText.getText());
					// set bad - "OK" button disabled
					fileText.setText("bad-file-name");
					assertFalse(okButton.isEnabled());
					// restore good - "OK" button enabled
					fileText.setText("messages.properties");
					assertTrue(okButton.isEnabled());
				}
				// close dialog
				okButton.click();
			}
		});
		// result
		/*System.out.println(newSourceDialog.getNewSourceDescription());
    System.out.println(newSourceDialog.getNewEditableSource());
    System.out.println(newSourceDialog.getNewSourceParameters());*/
	}
}
