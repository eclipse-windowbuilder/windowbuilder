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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.ExposedPropertyVariableSupport;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import org.junit.Test;

/**
 * Tests for {@link WidgetInfo}.
 *
 * @author lobas_av
 * @author mitin_aa
 * @author scheglov_ke
 */
public class WidgetTest extends RcpModelTest {
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
	// Style
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getStyle() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    new Text(this, SWT.BORDER);",
						"  }",
						"}");
		shell.refresh();
		// bits for existing widget object
		{
			WidgetInfo text = shell.getChildrenControls().get(0);
			int actualStyle = text.getStyle();
			assertTrue(
					"SWT.BORDER bit expected, but " + Integer.toHexString(actualStyle) + " found.",
					(actualStyle & SWT.BORDER) == SWT.BORDER);
		}
		// Button: CHECK
		{
			ControlInfo checkButton = createJavaInfo("org.eclipse.swt.widgets.Button", "check");
			int actualStyle = checkButton.getStyle();
			assertTrue(
					"SWT.CHECK bit expected, but " + Integer.toHexString(actualStyle) + " found.",
					(actualStyle & SWT.CHECK) == SWT.CHECK);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Description
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that {@link Widget#setData(Object)} and {@link Widget#setData(String, Object)} are
	 * described as executable.
	 */
	@Test
	public void test_description_setData() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setData('1');",
						"    setData('key', '2');",
						"  }",
						"}");
		shell.refresh();
		// check setData()
		Object shellObject = shell.getObject();
		assertEquals("1", ReflectionUtils.invokeMethod(shellObject, "getData()"));
		assertEquals("2", ReflectionUtils.invokeMethod(shellObject, "getData(java.lang.String)", "key"));
		assertNull(ReflectionUtils.invokeMethod(shellObject, "getData(java.lang.String)", "no-such-key"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exposed
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_exposedControls() throws Exception {
		setFileContentSrc(
				"test/ExposedComposite.java",
				getTestSource(
						"public class ExposedComposite extends Composite {",
						"  private Button m_button;",
						"  //",
						"  public ExposedComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout(2, false));",
						"    m_button = new Button(this, SWT.NONE);",
						"    m_button.setText('button');",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
						"  }",
						"}"));
		waitForAutoBuild();
		CompositeInfo mainComposite =
				parseComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new FillLayout());",
						"    Label label = new Label(this, SWT.NONE);",
						"    label.setText('label');",
						"    ExposedComposite composite = new ExposedComposite(this, SWT.NONE);",
						"  }",
						"}");
		//
		assertEquals(2, mainComposite.getChildrenControls().size());
		assertInstanceOf(ControlInfo.class, mainComposite.getChildrenControls().get(0));
		//
		CompositeInfo composite = (CompositeInfo) mainComposite.getChildrenControls().get(1);
		assertEquals(1, composite.getChildrenControls().size());
		//
		ControlInfo exposedButton = composite.getChildrenControls().get(0);
		assertNotNull(exposedButton);
		assertInstanceOf(ExposedPropertyCreationSupport.class, exposedButton.getCreationSupport());
		assertInstanceOf(ExposedPropertyVariableSupport.class, exposedButton.getVariableSupport());
		//
		Property property = exposedButton.getPropertyByTitle("text");
		assertNotNull(property);
		assertEquals("button", property.getValue());
	}

	/**
	 * Test for bounds of exposed {@link Control} from deep hierarchy.
	 */
	@Test
	public void test_exposedControl_bounds() throws Exception {
		setFileContentSrc(
				"test/ExposedComposite.java",
				getTestSource(
						"public class ExposedComposite extends Composite {",
						"  private Button m_button;",
						"  public ExposedComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    //",
						"    Composite innerComposite = new Composite(this, SWT.NONE);",
						"    innerComposite.setBounds(10, 20, 100, 100);",
						"    //",
						"    m_button = new Button(innerComposite, SWT.NONE);",
						"    m_button.setBounds(30, 40, 100, 20);",
						"  }",
						"  public Button getButton() {",
						"    return m_button;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo mainComposite =
				parseComposite(
						"public class Test extends Composite {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new FillLayout());",
						"    ExposedComposite exposedComposite = new ExposedComposite(this, SWT.NONE);",
						"  }",
						"}");
		mainComposite.refresh();
		// prepare ExposedComposite
		assertEquals(1, mainComposite.getChildrenControls().size());
		CompositeInfo exposedComposite = (CompositeInfo) mainComposite.getChildrenControls().get(0);
		// prepare exposed Button
		assertEquals(1, exposedComposite.getChildrenControls().size());
		ControlInfo exposedButton = exposedComposite.getChildrenControls().get(0);
		assertInstanceOf(ExposedPropertyCreationSupport.class, exposedButton.getCreationSupport());
		assertInstanceOf(ExposedPropertyVariableSupport.class, exposedButton.getVariableSupport());
		// check bounds
		assertEquals(new Rectangle(30, 40, 100, 20), exposedButton.getModelBounds());
		assertEquals(new Rectangle(40, 60, 100, 20), exposedButton.getBounds());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Variable name in component"
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that name of component is set in component using {@link Widget#setData(String, Object)}
	 * with key "name".
	 */
	@Test
	public void test_variableName_setData() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Button button = new Button(this, SWT.NONE);",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(0);
		// no "variable name in component" configured, just name modification expected
		{
			button.getVariableSupport().setName("button2");
			shell.refresh();
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    Button button2 = new Button(this, SWT.NONE);",
					"  }",
					"}");
		}
		// do with "variable name in component"
		PreferencesRepairer preferences =
				new PreferencesRepairer(shell.getDescription().getToolkit().getPreferences());
		try {
			preferences.setValue(IPreferenceConstants.P_VARIABLE_IN_COMPONENT, true);
			// no setData() for "button", new should be added
			{
				button.getVariableSupport().setName("button3");
				shell.refresh();
				assertEditor(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Button button3 = new Button(this, SWT.NONE);",
						"    button3.setData('name', 'button3');",
						"  }",
						"}");
			}
			// setData() for "button" exists, should be updated
			{
				button.getVariableSupport().setName("button4");
				shell.refresh();
				assertEditor(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Button button4 = new Button(this, SWT.NONE);",
						"    button4.setData('name', 'button4');",
						"  }",
						"}");
			}
		} finally {
			preferences.restore();
		}
	}

	/**
	 * Test that name of component is set in component using {@link Widget#setData(String, Object)}
	 * with key "name", even when we just add new component.
	 */
	@Test
	public void test_variableName_setData_onCreate() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"  }",
						"}");
		RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
		//
		ControlInfo button = BTestUtils.createButton();
		// do with "variable name in component"
		PreferencesRepairer preferences =
				new PreferencesRepairer(shell.getDescription().getToolkit().getPreferences());
		try {
			preferences.setValue(IPreferenceConstants.P_VARIABLE_IN_COMPONENT, true);
			// do add
			rowLayout.command_CREATE(button, null);
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    setLayout(new RowLayout());",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"      button.setData('name', 'button');",
					"    }",
					"  }",
					"}");
		} finally {
			preferences.restore();
		}
	}
}