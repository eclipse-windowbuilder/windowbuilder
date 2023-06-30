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
package org.eclipse.wb.internal.core.model.variable.description;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Implementation of {@link IVariableSupportDescription} for {@link FieldInitializerVariableSupport}
 * .
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public class FieldInitializerVariableDescription extends VariableSupportDescription {
	public static final String ID = "org.eclipse.wb.core.model.variable.fieldInitializer";
	public static final VariableSupportDescription INSTANCE =
			new FieldInitializerVariableDescription();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private FieldInitializerVariableDescription() {
		super(ID,
				"Init. Field",
				"declarate unique field with component, initialize directly at field declaration");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// VariableSupportDescription
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Class<? extends VariableSupport> getType() {
		return FieldInitializerVariableSupport.class;
	}

	@Override
	public VariableSupport createSupport(JavaInfo javaInfo) {
		return new FieldInitializerVariableSupport(javaInfo);
	}

	@Override
	public void configureDefaultPreferences(IPreferenceStore store) {
		super.configureDefaultPreferences(store);
		store.setDefault(FieldInitializerVariableSupport.P_PREFIX_THIS, false);
		store.setDefault(
				FieldInitializerVariableSupport.P_FIELD_MODIFIER,
				FieldVariableSupport.V_FIELD_MODIFIER_PRIVATE);
	}

	@Override
	public GenerationPropertiesComposite createPropertiesComposite(Composite parent,
			DataBindManager bindManager,
			IPreferenceStore store) {
		return new PropertiesComposite(parent, bindManager, store);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties composite
	//
	////////////////////////////////////////////////////////////////////////////
	private static class PropertiesComposite extends GenerationPropertiesComposite {
		public PropertiesComposite(Composite parent,
				DataBindManager bindManager,
				IPreferenceStore preferences) {
			super(parent, bindManager, preferences);
			GridLayoutFactory.create(this).columns(2);
			{
				Button button =
						checkButton(
								this,
								"Prefix field access with \"this\"",
								FieldInitializerVariableSupport.P_PREFIX_THIS);
				GridDataFactory.modify(button).spanH(2).fillH();
			}
			{
				new Label(this, SWT.NONE).setText("Field modifier:");
				Combo modifierCombo = new Combo(this, SWT.READ_ONLY);
				GridDataFactory.create(modifierCombo).grabH().fillH();
				modifierCombo.add("private");
				modifierCombo.add("package private");
				modifierCombo.add("protected");
				modifierCombo.add("public");
				bindSelection(modifierCombo, FieldInitializerVariableSupport.P_FIELD_MODIFIER);
			}
		}
	}
}
