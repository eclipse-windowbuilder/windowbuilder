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
package org.eclipse.wb.tests.designer.editor.validator;

import org.eclipse.wb.core.gef.policy.validator.BorderOfChildLayoutRequestValidator;
import org.eclipse.wb.core.gef.policy.validator.CachingLayoutRequestValidator;
import org.eclipse.wb.core.gef.policy.validator.CompatibleLayoutRequestValidator;
import org.eclipse.wb.core.gef.policy.validator.ComponentClassLayoutRequestValidator;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.gef.policy.validator.ModelClassLayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * Tests for {@link LayoutRequestValidators}.
 *
 * @author scheglov_ke
 */
public class LayoutRequestValidatorsTest extends AbstractLayoutRequestValidatorTest {
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
	// Literals
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ILayoutRequestValidator#TRUE}.
	 */
	@Test
	public void test_TRUE() throws Exception {
		ILayoutRequestValidator validator = ILayoutRequestValidator.TRUE;
		assertTrue(validator);
	}

	/**
	 * Test for {@link ILayoutRequestValidator#FALSE}.
	 */
	@Test
	public void test_FALSE() throws Exception {
		ILayoutRequestValidator validator = ILayoutRequestValidator.FALSE;
		assertFalse(validator);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Factories
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link LayoutRequestValidators#finalize(ILayoutRequestValidator)}.
	 */
	@Test
	public void test_finalize() throws Exception {
		ILayoutRequestValidator specificValidator = ILayoutRequestValidator.TRUE;
		ILayoutRequestValidator validator = LayoutRequestValidators.finalize(specificValidator);
		Assertions.assertThat(getValidators(validator)).contains(specificValidator);
		assertHas_Compatible(validator);
		assertHas_BorderOfChild(validator);
	}

	/**
	 * Test for {@link LayoutRequestValidators#modelType(Class)}.
	 */
	@Test
	public void test_modelType() throws Exception {
		ILayoutRequestValidator validator = LayoutRequestValidators.modelType(ContainerInfo.class);
		assertHas_ModelType(validator, ContainerInfo.class);
		assertHas_Compatible(validator);
		assertHas_BorderOfChild(validator);
	}

	/**
	 * Test for {@link LayoutRequestValidators#componentType(String)}.
	 */
	@Test
	public void test_componentType() throws Exception {
		ILayoutRequestValidator validator =
				LayoutRequestValidators.componentType("javax.swing.JButton");
		assertHas_ComponentType(validator, "javax.swing.JButton");
		assertHas_Compatible(validator);
		assertHas_BorderOfChild(validator);
	}

	private static void assertHas_Compatible(ILayoutRequestValidator compoundValidator) {
		ILayoutRequestValidator[] validators = getValidators(compoundValidator);
		for (ILayoutRequestValidator validator : validators) {
			if (validator instanceof CompatibleLayoutRequestValidator) {
				return;
			}
		}
		fail("Can not find Compatible_LayoutRequestValidator");
	}

	private static void assertHas_BorderOfChild(ILayoutRequestValidator compoundValidator) {
		ILayoutRequestValidator[] validators = getValidators(compoundValidator);
		for (ILayoutRequestValidator validator : validators) {
			if (validator instanceof BorderOfChildLayoutRequestValidator) {
				return;
			}
		}
		fail("Can not find Compatible_LayoutRequestValidator");
	}

	private static void assertHas_ModelType(ILayoutRequestValidator compoundValidator,
			Class<?> requiredModelClass) {
		ILayoutRequestValidator[] validators = getValidators(compoundValidator);
		for (ILayoutRequestValidator validator : validators) {
			if (validator instanceof ModelClassLayoutRequestValidator) {
				if (Objects.equals(
						ReflectionUtils.getFieldObject(validator, "m_requiredModelClass"),
						requiredModelClass)) {
					return;
				}
			}
		}
		fail("Can not find ModelClass_LayoutRequestValidator " + requiredModelClass);
	}

	private static void assertHas_ComponentType(ILayoutRequestValidator compoundValidator,
			String requiredComponentClass) {
		ILayoutRequestValidator[] validators = getValidators(compoundValidator);
		for (ILayoutRequestValidator validator : validators) {
			if (validator instanceof ComponentClassLayoutRequestValidator) {
				if (Objects.equals(
						ReflectionUtils.getFieldObject(validator, "m_requiredClass"),
						requiredComponentClass)) {
					return;
				}
			}
		}
		fail("Can not find ComponentClass_LayoutRequestValidator " + requiredComponentClass);
	}

	private static ILayoutRequestValidator[] getValidators(ILayoutRequestValidator compoundValidator) {
		ILayoutRequestValidator[] validators =
				(ILayoutRequestValidator[]) ReflectionUtils.getFieldObject(
						compoundValidator,
						"val$validators");
		stripCaching(validators);
		return validators;
	}

	/**
	 * We wrap {@link ILayoutRequestValidator}s into {@link CachingLayoutRequestValidator} for
	 * performance.
	 */
	private static void stripCaching(ILayoutRequestValidator[] validators) {
		for (int i = 0; i < validators.length; i++) {
			ILayoutRequestValidator validator = validators[i];
			if (validator instanceof CachingLayoutRequestValidator) {
				validators[i] =
						(ILayoutRequestValidator) ReflectionUtils.getFieldObject(validator, "m_validator");
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Compound
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link LayoutRequestValidators#and(ILayoutRequestValidator...)}.
	 */
	@Test
	public void test_Compound_and_true() throws Exception {
		ILayoutRequestValidator validator =
				LayoutRequestValidators.and(ILayoutRequestValidator.TRUE, ILayoutRequestValidator.TRUE);
		assertTrue(validator);
	}

	/**
	 * Test for {@link LayoutRequestValidators#and(ILayoutRequestValidator...)}.
	 */
	@Test
	public void test_Compound_and_false() throws Exception {
		ILayoutRequestValidator validator =
				LayoutRequestValidators.and(ILayoutRequestValidator.TRUE, ILayoutRequestValidator.FALSE);
		assertFalse(validator);
	}

	/**
	 * Test for {@link LayoutRequestValidators#or(ILayoutRequestValidator...)}.
	 */
	@Test
	public void test_Compound_or_true() throws Exception {
		ILayoutRequestValidator validator =
				LayoutRequestValidators.or(ILayoutRequestValidator.TRUE, ILayoutRequestValidator.FALSE);
		assertTrue(validator);
	}

	/**
	 * Test for {@link LayoutRequestValidators#or(ILayoutRequestValidator...)}.
	 */
	@Test
	public void test_Compound_or_false() throws Exception {
		ILayoutRequestValidator validator =
				LayoutRequestValidators.or(ILayoutRequestValidator.FALSE, ILayoutRequestValidator.FALSE);
		assertFalse(validator);
	}
}
