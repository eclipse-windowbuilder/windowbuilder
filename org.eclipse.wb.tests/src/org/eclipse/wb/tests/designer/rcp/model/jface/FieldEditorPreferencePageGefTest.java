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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.rcp.model.jface.FieldEditorPreferencePageInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.eclipse.jface.preference.FieldEditorPreferencePage;

import org.junit.Test;

/**
 * Tests for {@link FieldEditorPreferencePage} in GEF.
 *
 * @author scheglov_ke
 */
public class FieldEditorPreferencePageGefTest extends RcpGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_canvas() throws Exception {
		FieldEditorPreferencePageInfo page =
				(FieldEditorPreferencePageInfo) openJavaInfo(
						"import org.eclipse.jface.preference.*;",
						"public class Test extends FieldEditorPreferencePage {",
						"  public Test() {",
						"    super(FLAT);",
						"  }",
						"  protected void createFieldEditors() {",
						"  }",
						"}");
		// create "BooleanFieldEditor"
		JavaInfo booleanFieldEditor;
		{
			booleanFieldEditor = loadCreationTool("org.eclipse.jface.preference.BooleanFieldEditor");
			canvas.create();
			canvas.target(page).in(300, 100).move();
			canvas.click();
			waitEventLoop(0);
			assertEditor(
					"import org.eclipse.jface.preference.*;",
					"public class Test extends FieldEditorPreferencePage {",
					"  public Test() {",
					"    super(FLAT);",
					"  }",
					"  protected void createFieldEditors() {",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"  }",
					"}");
		}
		// create "StringFieldEditor" before "BooleanFieldEditor"
		JavaInfo stringFieldEditor;
		{
			stringFieldEditor = loadCreationTool("org.eclipse.jface.preference.StringFieldEditor");
			canvas.create();
			canvas.target(booleanFieldEditor).inX(0).outY(-1).move();
			canvas.click();
			waitEventLoop(0);
			assertEditor(
					"import org.eclipse.jface.preference.*;",
					"public class Test extends FieldEditorPreferencePage {",
					"  public Test() {",
					"    super(FLAT);",
					"  }",
					"  protected void createFieldEditors() {",
					"    addField(new StringFieldEditor('id', 'New StringFieldEditor', -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, getFieldEditorParent()));",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"  }",
					"}");
		}
		// move "BooleanFieldEditor" before "StringFieldEditor"
		{
			canvas.beginMove(booleanFieldEditor);
			canvas.target(stringFieldEditor).inX(0).outY(-1).drag();
			canvas.endDrag();
			waitEventLoop(0);
			assertEditor(
					"import org.eclipse.jface.preference.*;",
					"public class Test extends FieldEditorPreferencePage {",
					"  public Test() {",
					"    super(FLAT);",
					"  }",
					"  protected void createFieldEditors() {",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"    addField(new StringFieldEditor('id', 'New StringFieldEditor', -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, getFieldEditorParent()));",
					"  }",
					"}");
		}
		// copy/paste "BooleanFieldEditor"
		{
			doCopyPaste(booleanFieldEditor);
			canvas.create();
			canvas.target(stringFieldEditor).inX(10).outY(1).move();
			canvas.click();
			waitEventLoop(0);
			assertEditor(
					"import org.eclipse.jface.preference.*;",
					"public class Test extends FieldEditorPreferencePage {",
					"  public Test() {",
					"    super(FLAT);",
					"  }",
					"  protected void createFieldEditors() {",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"    addField(new StringFieldEditor('id', 'New StringFieldEditor', -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, getFieldEditorParent()));",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"  }",
					"}");
		}
	}

	@Test
	public void test_tree() throws Exception {
		FieldEditorPreferencePageInfo page =
				(FieldEditorPreferencePageInfo) openJavaInfo(
						"import org.eclipse.jface.preference.*;",
						"public class Test extends FieldEditorPreferencePage {",
						"  public Test() {",
						"    super(FLAT);",
						"  }",
						"  protected void createFieldEditors() {",
						"  }",
						"}");
		// create "BooleanFieldEditor"
		JavaInfo booleanFieldEditor;
		{
			booleanFieldEditor = loadCreationTool("org.eclipse.jface.preference.BooleanFieldEditor");
			tree.moveOn(page).click();
			waitEventLoop(0);
			assertEditor(
					"import org.eclipse.jface.preference.*;",
					"public class Test extends FieldEditorPreferencePage {",
					"  public Test() {",
					"    super(FLAT);",
					"  }",
					"  protected void createFieldEditors() {",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"  }",
					"}");
		}
		// create "StringFieldEditor" before "BooleanFieldEditor"
		JavaInfo stringFieldEditor;
		{
			stringFieldEditor = loadCreationTool("org.eclipse.jface.preference.StringFieldEditor");
			tree.moveBefore(booleanFieldEditor).click();
			waitEventLoop(0);
			assertEditor(
					"import org.eclipse.jface.preference.*;",
					"public class Test extends FieldEditorPreferencePage {",
					"  public Test() {",
					"    super(FLAT);",
					"  }",
					"  protected void createFieldEditors() {",
					"    addField(new StringFieldEditor('id', 'New StringFieldEditor', -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, getFieldEditorParent()));",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"  }",
					"}");
		}
		// move "BooleanFieldEditor" before "StringFieldEditor"
		{
			tree.startDrag(booleanFieldEditor);
			tree.dragBefore(stringFieldEditor);
			tree.endDrag();
			waitEventLoop(0);
			assertEditor(
					"import org.eclipse.jface.preference.*;",
					"public class Test extends FieldEditorPreferencePage {",
					"  public Test() {",
					"    super(FLAT);",
					"  }",
					"  protected void createFieldEditors() {",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"    addField(new StringFieldEditor('id', 'New StringFieldEditor', -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, getFieldEditorParent()));",
					"  }",
					"}");
		}
		// copy/paste "BooleanFieldEditor"
		{
			doCopyPaste(booleanFieldEditor);
			tree.moveOn(page).click();
			waitEventLoop(0);
			assertEditor(
					"import org.eclipse.jface.preference.*;",
					"public class Test extends FieldEditorPreferencePage {",
					"  public Test() {",
					"    super(FLAT);",
					"  }",
					"  protected void createFieldEditors() {",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"    addField(new StringFieldEditor('id', 'New StringFieldEditor', -1, StringFieldEditor.VALIDATE_ON_KEY_STROKE, getFieldEditorParent()));",
					"    addField(new BooleanFieldEditor('id', 'New BooleanFieldEditor', BooleanFieldEditor.DEFAULT, getFieldEditorParent()));",
					"  }",
					"}");
		}
	}
}
