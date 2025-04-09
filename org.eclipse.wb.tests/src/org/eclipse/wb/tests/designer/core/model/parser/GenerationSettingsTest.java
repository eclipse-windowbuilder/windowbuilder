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
package org.eclipse.wb.tests.designer.core.model.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.description.FieldInitializerVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

/**
 * Tests for {@link GenerationSettings}.
 *
 * @author scheglov_ke
 */
public class GenerationSettingsTest extends SwingModelTest {
	private static final GenerationSettings SWING_SETTINGS =
			org.eclipse.wb.internal.swing.ToolkitProvider.DESCRIPTION.getGenerationSettings();

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
	public void test_compatibleStatements() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public final class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		GenerationSettings settings = panel.getDescription().getToolkit().getGenerationSettings();
		// check variables
		VariableSupportDescription[] variables = settings.getVariables();
		assertEquals(4, variables.length);
		assertSame(LocalUniqueVariableDescription.INSTANCE, variables[0]);
		assertSame(FieldUniqueVariableDescription.INSTANCE, variables[1]);
		assertSame(FieldInitializerVariableDescription.INSTANCE, variables[2]);
		assertSame(LazyVariableDescription.INSTANCE, variables[3]);
		// check statements for "local unique" variable
		{
			StatementGeneratorDescription[] statements = settings.getStatements(variables[0]);
			assertEquals(2, statements.length);
			assertSame(FlatStatementGeneratorDescription.INSTANCE, statements[0]);
			assertSame(BlockStatementGeneratorDescription.INSTANCE, statements[1]);
		}
		// check statements for "lazy" variable
		{
			StatementGeneratorDescription[] statements = settings.getStatements(variables[3]);
			assertEquals(1, statements.length);
			assertSame(LazyStatementGeneratorDescription.INSTANCE, statements[0]);
		}
	}

	/**
	 * Test for
	 * {@link GenerationSettings#getPreview(VariableSupportDescription, StatementGeneratorDescription)}
	 * for Swing and SWT.
	 */
	@Test
	public void test_getPreview() throws Exception {
		// test several Swing preview's
		assertInstanceOf(
				org.eclipse.wb.internal.core.model.generation.preview.GenerationPreviewLocalUniqueFlat.class,
				SWING_SETTINGS.getPreview(
						LocalUniqueVariableDescription.INSTANCE,
						FlatStatementGeneratorDescription.INSTANCE));
		assertInstanceOf(
				org.eclipse.wb.internal.core.model.generation.preview.GenerationPreviewLocalUniqueBlock.class,
				SWING_SETTINGS.getPreview(
						LocalUniqueVariableDescription.INSTANCE,
						BlockStatementGeneratorDescription.INSTANCE));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Defaults
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test variable/statement defaults.
	 */
	@Test
	public void test_defaults() throws Exception {
		// variable default/current
		assertSame(LocalUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getDefaultVariable());
		assertSame(LocalUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable());
		// statement default/current
		assertSame(BlockStatementGeneratorDescription.INSTANCE, SWING_SETTINGS.getDefaultStatement());
		assertSame(BlockStatementGeneratorDescription.INSTANCE, SWING_SETTINGS.getStatement());
		// set new variable
		SWING_SETTINGS.setVariable(FieldUniqueVariableDescription.INSTANCE);
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		assertSame(FieldUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
		assertSame(BlockStatementGeneratorDescription.INSTANCE, SWING_SETTINGS.getStatement(panel));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getVariable()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link GenerationSettings#getVariable(JavaInfo)}.
	 * <p>
	 * Test that "variable.validateID" script can be used to change ID of
	 * {@link VariableSupportDescription} to use for given {@link JavaInfo}.
	 */
	@Test
	public void test_getVariable_useScript() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
		// prepare new JavaInfo
		JavaInfo newComponent = createJavaInfo("javax.swing.JButton");
		// by default "local unique"
		assertEquals(LocalUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(newComponent));
		// force "field unique"
		JavaInfoUtils.setParameter(newComponent, "variable.validateID", CodeUtils.getSource(
				"import org.eclipse.wb.internal.core.model.variable.description.*;",
				"return FieldUniqueVariableDescription.ID;"));
		assertEquals(FieldUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(newComponent));
		// remove script, so again "local unique"
		JavaInfoUtils.setParameter(newComponent, "variable.validateID", null);
		assertEquals(LocalUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(newComponent));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// No deduce
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * No deduce because no components.
	 */
	@Test
	public void test_noDeduce_noComponents() throws Exception {
		SWING_SETTINGS.setVariable(FieldUniqueVariableDescription.INSTANCE);
		//
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		assertSame(FieldUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
	}

	/**
	 * No deduce because not enough components.
	 */
	@Test
	public void test_noDeduce_notEnoughComponents() throws Exception {
		SWING_SETTINGS.setVariable(FieldUniqueVariableDescription.INSTANCE);
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton('1');",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton('2');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		assertSame(FieldUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
	}

	/**
	 * No deduce because disabled.
	 */
	@Test
	public void test_noDeduce_disabled() throws Exception {
		SWING_SETTINGS.setVariable(LazyVariableDescription.INSTANCE);
		SWING_SETTINGS.setStatement(LazyStatementGeneratorDescription.INSTANCE);
		SWING_SETTINGS.setDeduceSettings(false);
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button_1 = new JButton('1');",
						"    add(button_1);",
						"    //",
						"    JButton button_2 = new JButton('2');",
						"    add(button_2);",
						"    //",
						"    JButton button_3 = new JButton('3');",
						"    add(button_3);",
						"  }",
						"}");
		assertSame(LazyVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
		assertSame(LazyStatementGeneratorDescription.INSTANCE, SWING_SETTINGS.getStatement(panel));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Deduce
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Deduce as {@link LocalUniqueVariableDescription}.
	 */
	@Test
	public void test_deduce_localUnique() throws Exception {
		SWING_SETTINGS.setDeduceSettings(true);
		SWING_SETTINGS.setVariable(FieldUniqueVariableDescription.INSTANCE);
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton('1');",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton('2');",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton('3');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		assertSame(LocalUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
	}

	/**
	 * Deduce as {@link LocalUniqueVariableDescription}, fix statement.
	 */
	@Test
	public void test_deduce_localUnique_block() throws Exception {
		SWING_SETTINGS.setDeduceSettings(true);
		SWING_SETTINGS.setVariable(LazyVariableDescription.INSTANCE);
		SWING_SETTINGS.setStatement(LazyStatementGeneratorDescription.INSTANCE);
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
						"}");
		assertSame(LocalUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
		assertSame(BlockStatementGeneratorDescription.INSTANCE, SWING_SETTINGS.getStatement(panel));
	}

	/**
	 * More blocks with single component than with multiple, so "block" wins.
	 */
	@Test
	public void test_deduce_mixedBlockFlat_blockWins() throws Exception {
		SWING_SETTINGS.setDeduceSettings(true);
		SWING_SETTINGS.setStatement(LazyStatementGeneratorDescription.INSTANCE);
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button_1 = new JButton();",
						"      JButton button_2 = new JButton();",
						"      add(button_1);",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		assertSame(BlockStatementGeneratorDescription.INSTANCE, SWING_SETTINGS.getStatement(panel));
	}

	/**
	 * More blocks with single component than with multiple, so "flat" wins.
	 */
	@Test
	public void test_deduce_mixedBlockFlat_flatWins() throws Exception {
		SWING_SETTINGS.setDeduceSettings(true);
		SWING_SETTINGS.setStatement(LazyStatementGeneratorDescription.INSTANCE);
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button_1 = new JButton();",
						"      JButton button_2 = new JButton();",
						"      JButton button_3 = new JButton();",
						"      JButton button_4 = new JButton();",
						"      add(button_1);",
						"      add(button_2);",
						"      add(button_3);",
						"      add(button_4);",
						"    }",
						"  }",
						"}");
		assertSame(FlatStatementGeneratorDescription.INSTANCE, SWING_SETTINGS.getStatement(panel));
	}

	@Test
	public void test_deduce_localUnique_flat() throws Exception {
		SWING_SETTINGS.setDeduceSettings(true);
		SWING_SETTINGS.setVariable(LazyVariableDescription.INSTANCE);
		SWING_SETTINGS.setStatement(LazyStatementGeneratorDescription.INSTANCE);
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button_1 = new JButton();",
						"    add(button_1);",
						"    //",
						"    JButton button_2 = new JButton();",
						"    add(button_2);",
						"    //",
						"    JButton button_3 = new JButton();",
						"    add(button_3);",
						"  }",
						"}");
		assertSame(LocalUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
		assertSame(FlatStatementGeneratorDescription.INSTANCE, SWING_SETTINGS.getStatement(panel));
	}

	/**
	 * Deduce as {@link LocalUniqueVariableDescription}, one component is field.
	 */
	@Test
	public void test_deduce_localUnique2() throws Exception {
		SWING_SETTINGS.setDeduceSettings(true);
		SWING_SETTINGS.setVariable(FieldUniqueVariableDescription.INSTANCE);
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton button_1;",
						"  public Test() {",
						"    {",
						"      button_1 = new JButton('1');",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button = new JButton('2');",
						"      add(button);",
						"    }",
						"    {",
						"      JButton button = new JButton('3');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		assertSame(LocalUniqueVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
	}

	/**
	 * Deduce as {@link FieldInitializerVariableDescription}, one component is local.
	 */
	@Test
	public void test_deduce_fieldWithInitializer() throws Exception {
		SWING_SETTINGS.setDeduceSettings(true);
		SWING_SETTINGS.setVariable(FieldUniqueVariableDescription.INSTANCE);
		//
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final JButton button_1 = new JButton('1');",
						"  private final JButton button_2 = new JButton('2');",
						"  private final JButton button_3 = new JButton('3');",
						"  public Test() {",
						"    {",
						"      add(button_1);",
						"    }",
						"    {",
						"      add(button_2);",
						"    }",
						"    {",
						"      add(button_3);",
						"    }",
						"    {",
						"      JButton button = new JButton('4');",
						"      add(button);",
						"    }",
						"  }",
						"}");
		assertSame(FieldInitializerVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
	}

	/**
	 * Deduce as {@link LazyVariableDescription} and {@link LazyStatementGeneratorDescription}.
	 */
	@Test
	public void test_deduce_lazy() throws Exception {
		SWING_SETTINGS.setDeduceSettings(true);
		SWING_SETTINGS.setVariable(LocalUniqueVariableDescription.INSTANCE);
		SWING_SETTINGS.setStatement(FlatStatementGeneratorDescription.INSTANCE);
		String[] lines =
			{
					"public class Test extends JPanel {",
					"  private JButton button_1;",
					"  private JButton button_2;",
					"  private JButton button_3;",
					"  public Test() {",
					"    add(getButton_1());",
					"    add(getButton_2());",
					"    add(getButton_3());",
					"  }",
					"  private JButton getButton_1() {",
					"    if (button_1 == null) {",
					"      button_1 = new JButton();",
					"    }",
					"    return button_1;",
					"  }",
					"  private JButton getButton_2() {",
					"    if (button_2 == null) {",
					"      button_2 = new JButton();",
					"    }",
					"    return button_2;",
					"  }",
					"  private JButton getButton_3() {",
					"    if (button_3 == null) {",
					"      button_3 = new JButton();",
					"    }",
					"    return button_3;",
					"  }",
			"}"};
		//
		ContainerInfo panel = parseContainer(lines);
		assertSame(LazyVariableDescription.INSTANCE, SWING_SETTINGS.getVariable(panel));
		assertSame(LazyStatementGeneratorDescription.INSTANCE, SWING_SETTINGS.getStatement(panel));
	}

	/**
	 * If some {@link JavaInfo} has no component class in {@link ComponentDescription}, this caused
	 * {@link NullPointerException} during deducing settings.
	 */
	@Test
	public void test_deduce_nullComponentClass() throws Exception {
		SWING_SETTINGS.setDeduceSettings(true);
		SWING_SETTINGS.setVariable(LocalUniqueVariableDescription.INSTANCE);
		String[] lines = {"public class Test extends JPanel {", "  public Test() {", "  }", "}"};
		// parse for context
		parseContainer(lines);
		// prepare JavaInfo with "null" component class
		AbsoluteLayoutInfo componentNull = AbsoluteLayoutInfo.createExplicit(m_lastEditor);
		// this should not throw exception
		SWING_SETTINGS.getVariable(componentNull);
	}
}
