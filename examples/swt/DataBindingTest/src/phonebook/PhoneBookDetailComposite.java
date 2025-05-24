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
package phonebook;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import phonebook.model.Person;

public class PhoneBookDetailComposite extends Composite {

	private Person m_person = new Person();

	private Text m_mobile2Text;
	private Text m_mobile1Text;
	private Text m_phoneText;
	private Text m_emailText;
	private Text m_nameText;

	private DataBindingContext m_bindingContext;

	public PhoneBookDetailComposite(Composite parent, int style) {
		super(parent, style);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		final Label descriptionLabel = new Label(this, SWT.NONE);
		descriptionLabel.setText("Description:");
		new Label(this, SWT.NONE);

		final Label label = new Label(this, SWT.NONE);
		label.setText("Name:");

		m_nameText = new Text(this, SWT.BORDER);
		final GridData gd_m_nameText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_nameText.setLayoutData(gd_m_nameText);

		final Label emailLabel = new Label(this, SWT.NONE);
		emailLabel.setText("E-mail:");

		m_emailText = new Text(this, SWT.BORDER);
		final GridData gd_m_emailText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_emailText.setLayoutData(gd_m_emailText);

		final Label phoneLabel = new Label(this, SWT.NONE);
		phoneLabel.setText("Phone:");

		m_phoneText = new Text(this, SWT.BORDER);
		final GridData gd_m_phoneText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_phoneText.setLayoutData(gd_m_phoneText);

		final Label mobilePhone1Label = new Label(this, SWT.NONE);
		mobilePhone1Label.setText("Mobile Phone 1:");

		m_mobile1Text = new Text(this, SWT.BORDER);
		final GridData gd_m_mobile1Text = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_mobile1Text.setLayoutData(gd_m_mobile1Text);

		final Label mobilePhone2Label = new Label(this, SWT.NONE);
		mobilePhone2Label.setText("Mobile Phone 2:");

		m_mobile2Text = new Text(this, SWT.BORDER);
		final GridData gd_m_mobile2Text = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_mobile2Text.setLayoutData(gd_m_mobile2Text);

		m_bindingContext = initDataBindings();
	}

	protected DataBindingContext initDataBindings() {
		// @formatter:off
		IObservableValue<String> m_emailTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_emailText);
		IObservableValue<String> personPhoneObserveValue = BeanProperties.value("phone", String.class).observe(m_person);
		IObservableValue<String> m_phoneTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_phoneText);
		IObservableValue<String> m_nameTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_nameText);
		IObservableValue<String> personEmailObserveValue = BeanProperties.value("email", String.class).observe(m_person);
		IObservableValue<String> personNameObserveValue = BeanProperties.value("name", String.class).observe(m_person);
		IObservableValue<String> m_mobile2TextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_mobile2Text);
		IObservableValue<String> personMobilePhone2ObserveValue = BeanProperties.value("mobilePhone2", String.class).observe(m_person);
		IObservableValue<String> personMobilePhone1ObserveValue = BeanProperties.value("mobilePhone1", String.class).observe(m_person);
		IObservableValue<String> m_mobile1TextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_mobile1Text);
		// @formatter:on
		//
		DataBindingContext bindingContext = new DataBindingContext();
		//
		bindingContext.bindValue(m_nameTextTextObserveWidget, personNameObserveValue, null, null);
		bindingContext.bindValue(m_emailTextTextObserveWidget, personEmailObserveValue, null, null);
		bindingContext.bindValue(m_phoneTextTextObserveWidget, personPhoneObserveValue, null, null);
		bindingContext.bindValue(m_mobile1TextTextObserveWidget, personMobilePhone1ObserveValue, null, null);
		bindingContext.bindValue(m_mobile2TextTextObserveWidget, personMobilePhone2ObserveValue, null, null);
		//
		return bindingContext;
	}

	public Person getPerson() {
		return m_person;
	}

	public void setPerson(Person person) {
		if (m_bindingContext != null) {
			m_bindingContext.dispose();
		}
		this.m_person = person;
		m_bindingContext = initDataBindings();
	}

}
