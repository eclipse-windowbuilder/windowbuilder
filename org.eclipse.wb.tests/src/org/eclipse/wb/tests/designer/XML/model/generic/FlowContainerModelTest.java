/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.XML.model.generic;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generic.ContainerObjectValidators;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerConfigurable;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerConfiguration;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;

import java.text.MessageFormat;
import java.util.List;

/**
 * Test for {@link FlowContainer} and {@link FlowContainerConfigurable} models.
 *
 * @author scheglov_ke
 */
public class FlowContainerModelTest extends AbstractCoreTest {
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
	// FlowContainer_Factory: horizontal
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getConfigurations_horizontal_trueByDefault() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(configurations.get(0), "alwaysTrue", "direct");
	}

	@Test
	public void test_getConfigurations_horizontal_complexExpression() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "isHorizontal()"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(configurations.get(0), "isHorizontal()", "direct");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FlowContainer_Factory: association
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * No special container association, so "direct" is used.
	 */
	@Test
	public void test_getConfigurations_association_implicitDirect() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(configurations.get(0), "true", "direct");
	}

	/**
	 * The "property" association.
	 */
	@Test
	public void test_getConfigurations_association_property() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.x-association", "property myProperty"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(configurations.get(0), "true", "property myProperty");
	}

	/**
	 * The "inter" association.
	 */
	@Test
	@Ignore
	public void test_getConfigurations_association_inter() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.x-association", "inter myName attrA='a a' attrB='b'"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(configurations.get(0), "true", "inter myName {attrA=a a, attrB=b}");
	}

	@Test
	public void test_getConfigurations_association_bad() throws Exception {
		try {
			getConfigurations(true, new String[][]{
				{"flowContainer", "true"},
				{"flowContainer.horizontal", "true"},
				{"flowContainer.x-association", "bad association text"},
				{"flowContainer.component", "java.awt.Component"},
				{"flowContainer.reference", "java.awt.Component"},});
			fail();
		} catch (Throwable e) {
			Throwable rootCause = DesignerExceptionUtils.getRootCause(e);
			Assertions.assertThat(rootCause).isExactlyInstanceOf(AssertionFailedException.class);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FlowContainer_Factory: component
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Exception when no "component" validator.
	 */
	@Test
	public void test_getConfigurations_noComponentValidator() throws Exception {
		try {
			getConfigurations(true, new String[][]{
				{"flowContainer", "true"},
				{"flowContainer.horizontal", "true"},});
			fail();
		} catch (Throwable e) {
		}
	}

	@Test
	public void test_getConfigurations_explicitComponentTypes() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component", "javax.swing.JButton javax.swing.JTextField"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"javax.swing.JButton javax.swing.JTextField",
				"java.awt.Component");
	}

	/**
	 * Use default component validator.
	 */
	@Test
	public void test_getConfigurations_defaultComponent() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer.defaultComponent", "java.awt.Component"},
					{"flowContainer.defaultReference", "java.awt.Component"},
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"java.awt.Component",
				"java.awt.Component");
	}

	@Test
	public void test_getConfigurations_componentValidatorExpression() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component-validator", "isComponentType(java.awt.Component)"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"isComponentType(java.awt.Component)",
				"java.awt.Component");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FlowContainer_Factory: reference
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * No reference expression or string. So, error.
	 */
	@Test
	public void test_getConfigurations_noReference() throws Exception {
		try {
			getConfigurations(true, new String[][]{
				{"flowContainer.defaultComponent", "java.awt.Component"},
				{"flowContainer", "true"},
				{"flowContainer.horizontal", "true"},});
			fail();
		} catch (Throwable e) {
		}
	}

	@Test
	public void test_getConfigurations_explicitReferenceTypes() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "javax.swing.JButton javax.swing.JTextField"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"java.awt.Component",
				"javax.swing.JButton javax.swing.JTextField");
	}

	@Test
	public void test_getConfigurations_referenceValidatorExpression() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference-validator", "isReferenceType(java.awt.Component)"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"java.awt.Component",
				"isReferenceType(java.awt.Component)");
	}

	/**
	 * When no "reference" specified, same predicate as for "component" should be used.
	 */
	@Test
	public void test_getConfigurations_referencesAsComponents() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"java.awt.Component",
				"java.awt.Component");
	}

	/**
	 * Use default reference validator.
	 */
	@Test
	public void test_getConfigurations_defaultReference() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer.defaultComponent", "java.awt.Component"},
					{"flowContainer.defaultReference", "java.awt.Component"},
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"java.awt.Component",
				"java.awt.Component");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FlowContainer_Factory: canvas/tree parsing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Ask "forCanvas", return common configuration for both - canvas/tree.
	 */
	@Test
	public void test_getConfigurations_forCanvas_common() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"java.awt.Component",
				"java.awt.Component");
	}

	/**
	 * Ask "forCanvas", return explicit "forCanvas".
	 */
	@Test
	public void test_getConfigurations_forCanvas_explicit() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer.canvas", "true"},
					{"flowContainer.canvas.horizontal", "true"},
					{"flowContainer.canvas.component", "java.awt.Component"},
					{"flowContainer.canvas.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"java.awt.Component",
				"java.awt.Component");
	}

	/**
	 * Ask "forCanvas", but only "forTree" exist.
	 */
	@Test
	public void test_getConfigurations_forCanvas_onlyForTree() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer.tree", "true"},
					{"flowContainer.tree.horizontal", "true"},
					{"flowContainer.tree.component", "java.awt.Component"},
					{"flowContainer.tree.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).isEmpty();
	}

	/**
	 * Ask "forTree", return common configuration for both - canvas/tree.
	 */
	@Test
	public void test_getConfigurations_forTree_common() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(false, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
	}

	/**
	 * Ask "forTree", return explicit "forTree".
	 */
	@Test
	public void test_getConfigurations_forTree_explicit() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(false, new String[][]{
					{"flowContainer.tree", "true"},
					{"flowContainer.tree.horizontal", "true"},
					{"flowContainer.tree.component", "java.awt.Component"},
					{"flowContainer.tree.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(1);
	}

	/**
	 * Several different configurations.
	 */
	@Test
	public void test_getConfigurations_3_count() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "true"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "java.awt.Component"},
					{"flowContainer.1", "true"},
					{"flowContainer.1.horizontal", "false"},
					{"flowContainer.1.component", "javax.swing.JButton"},
					{"flowContainer.1.reference", "java.awt.Component"},
					{"flowContainer.5", "true"},
					{"flowContainer.5.horizontal", "true"},
					{"flowContainer.5.component", "javax.swing.JTextField"},
					{"flowContainer.5.reference", "javax.swing.JTextField"},});
		Assertions.assertThat(configurations).hasSize(3);
		assertConfiguration(
				configurations.get(0),
				"true",
				"direct",
				"java.awt.Component",
				"java.awt.Component");
		assertConfiguration(
				configurations.get(1),
				"false",
				"direct",
				"javax.swing.JButton",
				"java.awt.Component");
		assertConfiguration(
				configurations.get(2),
				"true",
				"direct",
				"javax.swing.JTextField",
				"javax.swing.JTextField");
	}

	/**
	 * Ignore if <code>flowContainer</code> value is not <code>true</code>.
	 */
	@Test
	public void test_getConfigurations_ignoreFalse() throws Exception {
		List<FlowContainerConfiguration> configurations =
				getConfigurations(true, new String[][]{
					{"flowContainer", "false"},
					{"flowContainer.horizontal", "true"},
					{"flowContainer.component", "java.awt.Component"},
					{"flowContainer.reference", "java.awt.Component"},});
		Assertions.assertThat(configurations).hasSize(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FlowContainer_Factory: assertions
	//
	////////////////////////////////////////////////////////////////////////////
	private static void assertConfiguration(FlowContainerConfiguration configuration,
			String horizontal,
			String association) throws Exception {
		assertConfiguration(configuration, horizontal, association, null, null);
	}

	private static void assertConfiguration(FlowContainerConfiguration configuration,
			String horizontal,
			String association,
			String expectedComponentValidator,
			String expectedReferenceValidator) throws Exception {
		assertEquals(horizontal, getHorizontalPredicateString(configuration));
		assertEquals(association, getAssociationString(configuration));
		if (expectedComponentValidator != null) {
			assertEquals(
					expectedComponentValidator,
					getValidatorString(configuration.getComponentValidator()));
		}
		if (expectedReferenceValidator != null) {
			assertEquals(
					expectedReferenceValidator,
					getValidatorString(configuration.getReferenceValidator()));
		}
	}

	private static String getValidatorString(Object validator) {
		return validator.toString();
	}

	private static String getHorizontalPredicateString(FlowContainerConfiguration configuration) {
		Predicate<Object> predicate = configuration.getHorizontalPredicate();
		if (predicate == Predicates.alwaysTrue()) {
			return "alwaysTrue";
		}
		return predicate.toString();
	}

	private static String getAssociationString(FlowContainerConfiguration configuration)
			throws Exception {
		return configuration.getAssociation().toString();
	}

	private List<FlowContainerConfiguration> getConfigurations(boolean forCanvas,
			String[][] parameters) throws Exception {
		String[] parameterLines = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			String[] parameterPair = parameters[i];
			assertEquals(2, parameterPair.length);
			parameterLines[i] =
					MessageFormat.format(
							"    <parameter name=''{0}''>{1}</parameter>",
							parameterPair[0],
							parameterPair[1]);
		}
		// prepare description
		prepareMyComponent();
		setFileContentSrc(
				"test/MyComponent.wbp-component.xml",
				getSource3(new String[]{
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
				"  <parameters>"}, parameterLines, new String[]{"  </parameters>", "</component>"}));
		waitForAutoBuild();
		// parse
		XmlObjectInfo panel = parse("<t:MyComponent/>");
		// get configurations
		return new FlowContainerFactory(panel, forCanvas).getConfigurations();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Duck typing
	//
	////////////////////////////////////////////////////////////////////////////
	public static class MyFlowContainer extends XmlObjectInfo {
		public MyFlowContainer(EditorContext context,
				ComponentDescription description,
				CreationSupport creationSupport) throws Exception {
			super(context, description, creationSupport);
		}

		public void command_CREATE(Object component, Object nextComponent) {
		}

		public void command_CREATE_after(Object component, Object nextComponent) {
		}

		public void command_MOVE(Object component, Object nextComponent) {
		}

		public void command_MOVE_after(Object component, Object nextComponent) {
		}

		public void command_APPEND_after(Object component, Object nextComponent) {
		}
	}
	public static class MyFlowContainerForAdd extends XmlObjectInfo {
		public MyFlowContainerForAdd(EditorContext context,
				ComponentDescription description,
				CreationSupport creationSupport) throws Exception {
			super(context, description, creationSupport);
		}

		public void command_MOVE_after(Object component, Object nextComponent) {
		}

		public void command_ADD_after(Object component, Object nextComponent) {
		}

		public void command_APPEND_after(Object component, Object nextComponent) {
		}
	}
	public static class MyFlowContainer_useMostSpecific extends XmlObjectInfo {
		public MyFlowContainer_useMostSpecific(EditorContext context,
				ComponentDescription description,
				CreationSupport creationSupport) throws Exception {
			super(context, description, creationSupport);
		}

		@SuppressWarnings("unused")
		public void command_CREATE(Object component, Object nextComponent) {
		}

		public void command_CREATE(XmlObjectInfo component, Object nextComponent) {
		}
	}

	/**
	 * Create/Move operations of {@link FlowContainerConfigurable} should try to find corresponding
	 * CREATE/MOVE methods in container {@link JavaInfo} and use them instead of generic
	 * implementation.
	 */
	@Test
	public void test_duckTyping() throws Exception {
		final XmlObjectInfo component = mock(XmlObjectInfo.class);
		final XmlObjectInfo nextComponent = mock(XmlObjectInfo.class);
		final MyFlowContainer container = mock(MyFlowContainer.class);
		final InOrder inOrder = inOrder(component, nextComponent, container);
		final FlowContainer flowContainer =
				new FlowContainerConfigurable(container,
						new FlowContainerConfiguration(Predicates.alwaysTrue(),
								Predicates.alwaysFalse(),
								Associations.direct(),
								ContainerObjectValidators.alwaysTrue(),
								ContainerObjectValidators.alwaysTrue()));
		// CREATE
		flowContainer.command_CREATE(component, nextComponent);
		//
		inOrder.verify(container).command_CREATE(component, nextComponent);
		inOrder.verify(container).command_CREATE_after(component, nextComponent);
		inOrder.verify(container).command_APPEND_after(component, nextComponent);
		inOrder.verifyNoMoreInteractions();
		// MOVE
		clearInvocations(container);
		//
		flowContainer.command_MOVE(component, nextComponent);
		//
		inOrder.verify(container).command_MOVE(component, nextComponent);
		inOrder.verify(container).command_MOVE_after(component, nextComponent);
		inOrder.verifyNoMoreInteractions();
	}

	/**
	 * After ADD operation method "command_ADD_after" should be called.
	 */
	@Test
	public void test_duckTyping_ADD() throws Exception {
		final XmlObjectInfo component = mock(XmlObjectInfo.class);
		final XmlObjectInfo oldContainer = mock(XmlObjectInfo.class);
		final XmlObjectInfo nextComponent = mock(XmlObjectInfo.class);
		final MyFlowContainerForAdd container = mock(MyFlowContainerForAdd.class);
		final InOrder inOrder = inOrder(component, oldContainer, nextComponent, container);
		final FlowContainer flowContainer =
				new FlowContainerConfigurable(container,
						new FlowContainerConfiguration(Predicates.alwaysTrue(),
								Predicates.alwaysFalse(),
								Associations.direct(),
								ContainerObjectValidators.alwaysTrue(),
								ContainerObjectValidators.alwaysTrue()));
		// MOVE (as ADD)
		ReflectionUtils.setField(component, "m_parent", oldContainer);
		System.setProperty("flowContainer.simulateMove", "true");
		try {
			flowContainer.command_MOVE(component, nextComponent);
		} finally {
			System.clearProperty("flowContainer.simulateMove");
		}
		//
		inOrder.verify(container).command_ADD_after(component, nextComponent);
		inOrder.verify(container).command_APPEND_after(component, nextComponent);
		inOrder.verify(container).command_MOVE_after(component, nextComponent);
		inOrder.verifyNoMoreInteractions();
	}

	/**
	 * Test that most specific version "command_CREATE" method is used.
	 */
	@Test
	public void test_duckTyping_useMostSpecific() throws Exception {
		final XmlObjectInfo component = mock(XmlObjectInfo.class);
		final XmlObjectInfo nextComponent = mock(XmlObjectInfo.class);
		final MyFlowContainer_useMostSpecific container = mock(MyFlowContainer_useMostSpecific.class);
		final InOrder inOrder = inOrder(component, nextComponent, container);
		final FlowContainer flowContainer =
				new FlowContainerConfigurable(container,
						new FlowContainerConfiguration(Predicates.alwaysTrue(),
								Predicates.alwaysFalse(),
								Associations.direct(),
								ContainerObjectValidators.alwaysTrue(),
								ContainerObjectValidators.alwaysTrue()));
		// CREATE
		flowContainer.command_CREATE(component, nextComponent);
		//
		inOrder.verify(container).command_CREATE(component, nextComponent);
		inOrder.verifyNoMoreInteractions();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_validateMethods() throws Exception {
		XmlObjectInfo container = mock(XmlObjectInfo.class);
		final XmlObjectInfo component = mock(XmlObjectInfo.class);
		final XmlObjectInfo reference = mock(XmlObjectInfo.class);
		final FlowContainerConfiguration configuration = mock(FlowContainerConfiguration.class);
		final InOrder inOrder = inOrder(container, component, reference, configuration);
		final FlowContainer flowContainer = new FlowContainerConfigurable(container, configuration);
		// isHorizontal()
		when(configuration.getHorizontalPredicate()).thenReturn(Predicates.alwaysTrue());
		//
		assertTrue(flowContainer.isHorizontal());
		//
		inOrder.verify(configuration).getHorizontalPredicate();
		inOrder.verifyNoMoreInteractions();
		// validateComponent() = true
		clearInvocations(configuration);
		//
		when(configuration.getComponentValidator()).thenReturn(ContainerObjectValidators.alwaysTrue());
		//
		assertTrue(flowContainer.validateComponent(component));
		//
		inOrder.verify(configuration).getComponentValidator();
		inOrder.verifyNoMoreInteractions();
		// validateReference() = false
		clearInvocations(configuration);
		//
		when(configuration.getReferenceValidator()).thenReturn(ContainerObjectValidators.alwaysTrue());
		//
		assertTrue(flowContainer.validateReference(reference));
		//
		inOrder.verify(configuration).getReferenceValidator();
		inOrder.verifyNoMoreInteractions();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Models
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link FlowContainerConfigurable#command_CREATE(Object, Object)}.
	 */
	@Test
	public void test_CREATE() throws Exception {
		prepareFlowPanel();
		XmlObjectInfo panel =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<t:FlowPanel>",
						"  <Button wbp:name='button'/>",
						"</t:FlowPanel>");
		XmlObjectInfo button = getObjectByName("button");
		// prepare FlowContainer
		FlowContainer flowContainer = new FlowContainerFactory(panel, true).get().get(0);
		// do CREATE
		XmlObjectInfo newButton = createButton();
		{
			assertTrue(flowContainer.isHorizontal());
			assertTrue(flowContainer.validateComponent(newButton));
			assertTrue(flowContainer.validateReference(button));
		}
		flowContainer.command_CREATE(newButton, button);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<t:FlowPanel>",
				"  <Button/>",
				"  <Button wbp:name='button'/>",
				"</t:FlowPanel>");
	}

	/**
	 * Ensure that we can: create, delete and create new component again.
	 */
	@Test
	public void test_CREATE_twoTimes() throws Exception {
		prepareFlowPanel();
		XmlObjectInfo panel =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<t:FlowPanel/>");
		// prepare FlowContainer
		FlowContainer flowContainer = new FlowContainerFactory(panel, true).get().get(0);
		// create/delete
		{
			XmlObjectInfo newButton = createButton();
			flowContainer.command_CREATE(newButton, null);
			newButton.delete();
		}
		// create again
		{
			XmlObjectInfo newButton = createButton();
			flowContainer.command_CREATE(newButton, null);
		}
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<t:FlowPanel>",
				"  <Button/>",
				"</t:FlowPanel>");
	}

	/**
	 * Test for {@link FlowContainerConfigurable#command_MOVE(Object, Object)}.
	 */
	@Test
	public void test_MOVE() throws Exception {
		prepareFlowPanel();
		XmlObjectInfo panel =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<t:FlowPanel>",
						"  <Button wbp:name='button_1'/>",
						"  <Button wbp:name='button_2'/>",
						"</t:FlowPanel>");
		XmlObjectInfo button_1 = getObjectByName("button_1");
		XmlObjectInfo button_2 = getObjectByName("button_2");
		// prepare FlowContainer
		FlowContainer flowContainer =
				new FlowContainerConfigurable(panel,
						new FlowContainerConfiguration(Predicates.alwaysTrue(),
								Predicates.alwaysFalse(),
								Associations.direct(),
								ContainerObjectValidators.alwaysTrue(),
								ContainerObjectValidators.alwaysTrue()));
		// do MOVE
		flowContainer.command_MOVE(button_2, button_1);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<t:FlowPanel>",
				"  <Button wbp:name='button_2'/>",
				"  <Button wbp:name='button_1'/>",
				"</t:FlowPanel>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	static void prepareFlowPanel() throws Exception {
		prepareFlowPanel_classes();
		setFileContentSrc(
				"test/FlowPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='flowContainer'>true</parameter>",
						"    <parameter name='flowContainer.horizontal'>true</parameter>",
						"    <parameter name='flowContainer.component'>org.eclipse.swt.widgets.Control</parameter>",
						"    <parameter name='flowContainer.reference'>org.eclipse.swt.widgets.Control</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
	}

	static void prepareFlowPanel_classes() throws Exception {
		setFileContentSrc(
				"test/MyLayout.java",
				getSource(
						"package test;",
						"import org.eclipse.swt.graphics.Point;",
						"import org.eclipse.swt.widgets.*;",
						"public class MyLayout extends Layout {",
						"  protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {",
						"    int count = composite.getChildren().length;",
						"    return new Point(15 + 95 * count + 5 * (count - 1) + 15, 10 + 50 + 10);",
						"  }",
						"  protected void layout(Composite composite, boolean flushCache) {",
						"    Control[] children = composite.getChildren();",
						"    for (int i = 0; i < children.length; i++) {",
						"      Control child = children[i];",
						"      child.setBounds(15 + 100 * i, 10, 95, 50);",
						"    }",
						"  }",
						"}"));
		setFileContentSrc(
				"test/FlowPanel.java",
				getSource(
						"package test;",
						"import org.eclipse.swt.widgets.*;",
						"public class FlowPanel extends Composite {",
						"  public FlowPanel(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new MyLayout());",
						"  }",
						"}"));
	}
}
