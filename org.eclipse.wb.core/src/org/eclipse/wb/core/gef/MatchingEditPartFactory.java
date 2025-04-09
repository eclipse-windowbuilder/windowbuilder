/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.core.gef;

import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.gef.EditPart;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Implementation of {@link IEditPartFactory} using "package matching" strategy for model/part.
 * <p>
 * It allows us just place models and {@link EditPart}'s into same sub-packages of specified
 * "prefix" packages, and then automatically find {@link EditPart}, without writing manually each
 * correspondence.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class MatchingEditPartFactory implements IEditPartFactory {
	private final List<String> m_modelPackages;
	private final List<String> m_partPackages;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param modelPackages
	 *          the {@link List} of packages that can have models.
	 * @param partsPackages
	 *          the {@link List} of package that can have {@link EditPart}'s, corresponding to the
	 *          <code>modelPackages</code>.
	 */
	public MatchingEditPartFactory(List<String> modelPackages, List<String> partPackages) {
		Assert.equals(modelPackages.size(), partPackages.size());
		m_modelPackages = modelPackages;
		m_partPackages = partPackages;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IEditPartFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public org.eclipse.wb.gef.core.EditPart createEditPart(EditPart context, Object model) {
		Class<?> modelClass = model.getClass();
		for (; modelClass != null; modelClass = modelClass.getSuperclass()) {
			org.eclipse.wb.gef.core.EditPart editPart = createEditPart(model, modelClass);
			if (editPart != null) {
				return editPart;
			}
		}
		// not found
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param model
	 *          the model to get {@link EditPart}.
	 * @param modelClass
	 *          the current {@link Class} of model, of its super-class.
	 *
	 * @return the {@link EditPart} corresponding to <code>modelClass</code>, may be <code>null</code>
	 *         , if no match found.
	 */
	private org.eclipse.wb.gef.core.EditPart createEditPart(Object model, Class<?> modelClass) {
		// "Info" suffix
		{
			org.eclipse.wb.gef.core.EditPart editPart = createEditPart(model, modelClass, "Info");
			if (editPart != null) {
				return editPart;
			}
		}
		// no suffix
		{
			org.eclipse.wb.gef.core.EditPart editPart = createEditPart(model, modelClass, "");
			if (editPart != null) {
				return editPart;
			}
		}
		// special support for inner classes
		{
			String modelClassName = modelClass.getName();
			if (modelClassName.contains("$")) {
				modelClassName = StringUtils.remove(modelClassName, "Info");
				modelClassName = StringUtils.remove(modelClassName, "$");
				org.eclipse.wb.gef.core.EditPart editPart = createEditPart(model, modelClass, modelClassName, "");
				if (editPart != null) {
					return editPart;
				}
			}
		}
		// not found
		return null;
	}

	/**
	 * Implementation for {@link #createEditPart(Object, Class)}, with single model suffix.
	 */
	private org.eclipse.wb.gef.core.EditPart createEditPart(Object model, Class<?> modelClass, String modelSuffix) {
		String modelClassName = modelClass.getName();
		return createEditPart(model, modelClass, modelClassName, modelSuffix);
	}

	/**
	 * Implementation for {@link #createEditPart(Object, Class)}, with single model suffix.
	 */
	private org.eclipse.wb.gef.core.EditPart createEditPart(Object model,
			Class<?> modelClass,
			String modelClassName,
			String modelSuffix) {
		// check each model/part packages pair
		for (int i = 0; i < m_modelPackages.size(); i++) {
			String modelPackage = m_modelPackages.get(i);
			String partPackage = m_partPackages.get(i);
			if (modelClassName.startsWith(modelPackage) && modelClassName.endsWith(modelSuffix)) {
				// prepare name of component, strip model package and "Info" suffix
				String componentName = modelClassName;
				componentName = componentName.substring(modelPackage.length());
				componentName = StringUtils.chomp(componentName, modelSuffix);
				// create corresponding EditPart, use "EditPart" prefix
				{
					String partClassName = partPackage + componentName + "EditPart";
					org.eclipse.wb.gef.core.EditPart editPart = createEditPart0(model, modelClass, partClassName);
					if (editPart != null) {
						return editPart;
					}
				}
			}
		}
		// not found
		return null;
	}

	private static org.eclipse.wb.gef.core.EditPart createEditPart0(Object model, Class<?> modelClass, String partClassName) {
		try {
			ClassLoader classLoader = modelClass.getClassLoader();
			Class<?> partClass = classLoader.loadClass(partClassName);
			// try all constructors
			for (Constructor<?> constructor : partClass.getConstructors()) {
				try {
					return (org.eclipse.wb.gef.core.EditPart) constructor.newInstance(model);
				} catch (Throwable e) {
					// ignore
				}
			}
		} catch (Throwable e) {
			// ignore
		}
		return null;
	}
}
