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
package org.eclipse.wb.tests.designer.core.model.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.editor.AbstractTextPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.ITextValuePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodeInformation;
import org.eclipse.wb.internal.core.utils.state.EditorState.BadNodesCollection;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract test that works with {@link JavaInfo}, but does not parses them directly, as opposed to
 * {@link AbstractJavaInfoTest}.
 *
 * @author scheglov_ke
 */
public abstract class AbstractJavaInfoRelatedTest extends AbstractJavaTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures given {@link ToolkitDescription} for tests.
	 */
	public static void configureDefaults(ToolkitDescriptionJava toolkit) {
		IPreferenceStore preferences = toolkit.getPreferences();
		// variable name
		preferences.setValue(
				IPreferenceConstants.P_VARIABLE_TEXT_MODE,
				IPreferenceConstants.V_VARIABLE_TEXT_MODE_NEVER);
		preferences.setToDefault(IPreferenceConstants.P_VARIABLE_TEXT_TEMPLATE);
		preferences.setToDefault(IPreferenceConstants.P_VARIABLE_TEXT_WORDS_LIMIT);
		preferences.setToDefault(IPreferenceConstants.P_VARIABLE_IN_COMPONENT);
		NamesManager.setNameDescriptions(toolkit, Collections.emptyList());
		// please, always use in tests default settings
		{
			GenerationSettings generationSettings = toolkit.getGenerationSettings();
			generationSettings.setDefaultDeduceSettings(false);
			generationSettings.setDeduceSettings(false);
			generationSettings.setDefaultStatement(BlockStatementGeneratorDescription.INSTANCE);
			generationSettings.setDefaultVariable(LocalUniqueVariableDescription.INSTANCE);
			generationSettings.setStatement(generationSettings.getDefaultStatement());
			generationSettings.setVariable(generationSettings.getDefaultVariable());
		}
		preferences.setToDefault(GenerationSettings.P_FORCED_METHOD);
		preferences.setToDefault(FieldUniqueVariableSupport.P_PREFIX_THIS);
		// layouts
		preferences.setToDefault(IPreferenceConstants.P_LAYOUT_OF_PARENT);
		preferences.setToDefault(IPreferenceConstants.P_LAYOUT_DEFAULT);
		// direct edit
		preferences.setValue(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Statement} with given index's in {@link Block} that contains given
	 *         {@link JavaInfo}.
	 */
	protected static Statement getStatement(JavaInfo javaInfo, int... indexes) {
		ASTNode node = javaInfo.getCreationSupport().getNode();
		Block block;
		if (node instanceof MethodDeclaration) {
			assertInstanceOf(ThisCreationSupport.class, javaInfo.getCreationSupport());
			block = ((MethodDeclaration) node).getBody();
		} else {
			block = AstNodeUtils.getEnclosingBlock(node);
		}
		return getStatement(block, indexes);
	}

	/**
	 * Asserts that given {@link StatementTarget} has same state as expected.
	 */
	protected static void assertTarget(StatementTarget target,
			Block expectedBlock,
			Statement expectedStatement,
			boolean expectedBefore) {
		StatementTarget expectedTarget =
				new StatementTarget(expectedBlock, expectedStatement, expectedBefore);
		assertEquals(expectedTarget.toString(), target.toString());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hierarchy
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prints the active {@link AstEditor} hierarchy, ready to paste as arguments for
	 * {@link #assertHierarchy(String...)}.
	 */
	public static void printHierarchySource() {
		JavaInfo javaInfo = EditorState.getActiveJavaInfo();
		printHierarchySource(javaInfo);
	}

	/**
	 * Asserts that active {@link AstEditor} has expected hierarchy.
	 */
	public static void assertHierarchy(String... lines) {
		JavaInfo javaInfo = EditorState.getActiveJavaInfo();
		assertEquals(getSourceDQ(lines), printHierarchy(javaInfo));
	}

	/**
	 * Creates string for hierarchy of {@link JavaInfo}'s starting from given root.
	 */
	protected static String printHierarchy(JavaInfo root) {
		final StringBuffer buffer = new StringBuffer();
		root.accept(new ObjectInfoVisitor() {
			private int m_level;

			@Override
			public boolean visit(ObjectInfo objectInfo) throws Exception {
				buffer.append(StringUtils.repeat("\t", m_level));
				buffer.append(objectInfo.toString());
				buffer.append("\n");
				m_level++;
				return true;
			}

			@Override
			public void endVisit(ObjectInfo objectInfo) throws Exception {
				m_level--;
			}
		});
		return buffer.toString();
	}

	/**
	 * Creates source for {@link String}'s array for hierarchy of {@link JavaInfo}'s starting from
	 * given root.
	 */
	private static void printHierarchySource(JavaInfo root) {
		final StringBuffer buffer = new StringBuffer();
		root.accept(new ObjectInfoVisitor() {
			private int m_level;

			@Override
			public boolean visit(ObjectInfo objectInfo) throws Exception {
				buffer.append('"');
				buffer.append(StringUtils.repeat("  ", m_level));
				{
					String line = objectInfo.toString();
					line = line.replace('"', '\'');
					buffer.append(StringUtilities.escapeJava(line));
				}
				buffer.append('"');
				buffer.append(",\n");
				m_level++;
				return true;
			}

			@Override
			public void endVisit(ObjectInfo objectInfo) throws Exception {
				m_level--;
			}
		});
		String result = buffer.toString();
		result = StringUtils.removeEnd(result, ",\n");
		System.out.println(result);
	}

	/**
	 * @return the {@link JavaInfo} which has variable with given name.
	 */
	public static <T extends JavaInfo> T getJavaInfoByName(final String name) throws Exception {
		final AtomicReference<T> result = new AtomicReference<>();
		EditorState.getActiveJavaInfo().accept0(new ObjectInfoVisitor() {
			@Override
			@SuppressWarnings("unchecked")
			public void endVisit(ObjectInfo objectInfo) throws Exception {
				if (result.get() != null) {
					return;
				}
				if (objectInfo instanceof JavaInfo javaInfo) {
					if (isRequestedJavaInfo(javaInfo)) {
						result.set((T) javaInfo);
					}
				}
			}

			private boolean isRequestedJavaInfo(JavaInfo javaInfo) throws Exception {
				VariableSupport variable = javaInfo.getVariableSupport();
				// check name
				if (variable.hasName() && name.equals(variable.getName())) {
					return true;
				}
				// check title
				try {
					if (name.equals(variable.getTitle())) {
						return true;
					}
				} catch (Throwable e) {
				}
				// no
				return false;
			}
		});
		return result.get();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Prints result of {@link #getEditorLinesSource(AstEditor)} .
	 */
	protected static void printEditorLinesSource() {
		AstEditor editor = EditorState.getActiveJavaInfo().getEditor();
		String[] lines = StringUtils.split(editor.getSource(), "\r\n");
		System.out.println(getLinesForSourceDQ(lines));
	}

	/**
	 * Asserts that {@link EditorState} does not have any parse/refresh error or warnings.
	 */
	protected static void assertNoErrors(JavaInfo javaInfo) {
		AstEditor editor = javaInfo.getEditor();
		EditorState editorState = EditorState.get(editor);
		// bad parser nodes
		{
			BadNodesCollection nodes = editorState.getBadParserNodes();
			if (!nodes.isEmpty()) {
				for (BadNodeInformation node : nodes.nodes()) {
					System.out.println("------------------ bad parser node ------------------");
					System.out.println(editor.getSource(node.getNode()));
					node.getException().printStackTrace();
				}
				fail("No parser errors expected.");
			}
		}
		// bad refresh nodes
		{
			BadNodesCollection nodes = editorState.getBadRefreshNodes();
			if (!nodes.isEmpty()) {
				System.out.println("------------------ bad refresh node ------------------");
				for (BadNodeInformation node : nodes.nodes()) {
					System.out.println(editor.getSource(node.getNode()));
					node.getException().printStackTrace();
				}
				fail("No refresh errors expected.");
			}
		}
		// warnings
		if (!editorState.getWarnings().isEmpty()) {
			for (EditorWarning warning : editorState.getWarnings()) {
				System.out.println("------------------ warning ------------------");
				System.out.println(warning.getMessage());
				if (warning.getException() != null) {
					warning.getException().printStackTrace();
				}
			}
			fail("No warnings expected.");
		}
	}

	/**
	 * Removes liens which start with "// filler".
	 */
	protected static String[] removeFillerLines(String... lines) {
		while (lines.length != 0 && lines[0].startsWith("// filler")) {
			lines = ArrayUtils.remove(lines, 0);
		}
		return lines;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the new instance of {@link JavaInfo} for given fully qualified class name.
	 */
	public static <T extends JavaInfo> T createJavaInfo(String componentClassName) throws Exception {
		return createJavaInfo(componentClassName, null);
	}

	/**
	 * @return the new instance of {@link JavaInfo} for given fully qualified class name.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JavaInfo> T createJavaInfo(String componentClassName, String creationId)
			throws Exception {
		AstEditor editor = EditorState.getActiveJavaInfo().getEditor();
		JavaInfo javaInfo =
				JavaInfoUtils.createJavaInfo(
						editor,
						componentClassName,
						new ConstructorCreationSupport(creationId, true));
		javaInfo.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
		return (T) javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the array of {@link ExpressionAccessor}'s for given {@link GenericProperty}.
	 */
	protected static List<ExpressionAccessor> getGenericPropertyAccessors(GenericProperty property)
			throws Exception {
		return ((GenericPropertyImpl) property).getAccessors();
	}

	/**
	 * @return sub {@link Property}'s of given Property with {@link IComplexPropertyEditor}.
	 */
	protected static Property[] getSubProperties(Property property) throws Exception {
		return ((IComplexPropertyEditor) property.getEditor()).getProperties(property);
	}

	/**
	 * @return {@link Property} with given name from array of {@link Property}'s.
	 */
	protected static Property getPropertyByTitle(Property[] properties, String title) {
		for (int i = 0; i < properties.length; i++) {
			Property property = properties[i];
			if (title.equals(property.getTitle())) {
				return property;
			}
		}
		// not found
		return null;
	}

	/**
	 * @return the text from {@link TextDisplayPropertyEditor} for given {@link Property}.
	 */
	protected static String getPropertyText(Property property) throws Exception {
		return (String) ReflectionUtils.invokeMethod2(
				property.getEditor(),
				"getText",
				Property.class,
				property);
	}

	/**
	 * Set value of {@link ITextValuePropertyEditor} using text.
	 */
	protected static void setPropertyText(Property property, String text) throws Exception {
		ReflectionUtils.invokeMethod2(
				property.getEditor(),
				"setText",
				Property.class,
				String.class,
				property,
				text);
	}

	/**
	 * @return the text of {@link PropertyTooltipTextProvider} that should implement given
	 *         {@link IAdaptable}.
	 */
	protected static String getPropertyTooltipText(IAdaptable adaptable, Property property)
			throws Exception {
		PropertyTooltipProvider provider = adaptable.getAdapter(PropertyTooltipProvider.class);
		assertInstanceOf(PropertyTooltipTextProvider.class, provider);
		PropertyTooltipTextProvider textProvider = (PropertyTooltipTextProvider) provider;
		return (String) ReflectionUtils.invokeMethod(
				textProvider,
				"getText(org.eclipse.wb.internal.core.model.property.Property)",
				property);
	}

	/**
	 * @return the text for {@link AbstractTextPropertyEditor}.
	 */
	public static String getTextEditorText(Property property) throws Exception {
		PropertyEditor editor = property.getEditor();
		return (String) ReflectionUtils.invokeMethod(
				editor,
				"getEditorText(org.eclipse.wb.internal.core.model.property.Property)",
				property);
	}

	/**
	 * Updates {@link Property} by setting text for {@link AbstractTextPropertyEditor}.
	 */
	public static void setTextEditorText(Property property, String text) throws Exception {
		PropertyEditor editor = property.getEditor();
		ReflectionUtils.invokeMethod(
				editor,
				"setEditorText(org.eclipse.wb.internal.core.model.property.Property,java.lang.String)",
				property,
				text);
	}

	/**
	 * Opens dialog of {@link TextDialogPropertyEditor}.
	 */
	protected static void openPropertyDialog(Property property) throws Exception {
		ReflectionUtils.invokeMethod(
				property.getEditor(),
				"openDialog(org.eclipse.wb.internal.core.model.property.Property)",
				property);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Combo property editor
	//
	////////////////////////////////////////////////////////////////////////////
	private static Shell TEST_COMBO_SHELL;
	private static CCombo TEST_COMBO;

	@BeforeClass
	public static void setUpAll() {
		TEST_COMBO_SHELL = new Shell();
		TEST_COMBO = new CCombo(TEST_COMBO_SHELL, SWT.NONE);
	}

	@AfterClass
	public static void tearDownAll() {
		TEST_COMBO_SHELL.dispose();
	}

	/**
	 * Fill combo with items.
	 */
	protected static void addComboPropertyItems(Property property) {
		PropertyEditor propertyEditor = property.getEditor();
		String signature =
				"addItems("
						+ "org.eclipse.wb.internal.core.model.property.Property,"
						+ "org.eclipse.swt.custom.CCombo)";
		TEST_COMBO.removeAll();
		ReflectionUtils.invokeMethodEx(propertyEditor, signature, property, TEST_COMBO);
	}

	/**
	 * @return items from combo.
	 */
	protected static List<String> getComboPropertyItems() {
		List<String> items = new ArrayList<>();
		int itemCount = TEST_COMBO.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			items.add(TEST_COMBO.getItem(i));
		}
		return items;
	}

	/**
	 * @return the selection index in combo.
	 */
	protected static int getComboPropertySelection() {
		return TEST_COMBO.getSelectionIndex();
	}

	/**
	 * Sets the selection index in combo, usually to use then
	 * {@link #setComboPropertySelection(Property)} and validate result using
	 * {@link #getComboPropertySelection()}.
	 */
	protected static void setComboPropertySelection(int index) {
		TEST_COMBO.select(index);
	}

	/**
	 * Sets selection which corresponds to the value of {@link Property}.
	 */
	protected static void setComboPropertySelection(Property property) {
		PropertyEditor propertyEditor = property.getEditor();
		String signature =
				"selectItem("
						+ "org.eclipse.wb.internal.core.model.property.Property,"
						+ "org.eclipse.swt.custom.CCombo)";
		ReflectionUtils.invokeMethodEx(propertyEditor, signature, property, TEST_COMBO);
	}

	/**
	 * Simulates user selection of item with given index, updates {@link Property}.
	 */
	protected static void setComboPropertyValue(Property property, int index) {
		PropertyEditor propertyEditor = property.getEditor();
		String signature =
				"toPropertyEx("
						+ "org.eclipse.wb.internal.core.model.property.Property,"
						+ "org.eclipse.swt.custom.CCombo,"
						+ "int)";
		ReflectionUtils.invokeMethodEx(propertyEditor, signature, property, TEST_COMBO, index);
	}
}
