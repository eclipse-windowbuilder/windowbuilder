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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import phonebook.model.Person;

public class SimpleDBTest {

	private Button button;
	private ComboViewer comboViewer;
	private List<Person> m_persons;
	private Text text;
	protected Shell shell;

	/**
	 * Launch the application
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {
			@Override
			public void run() {
				try {
					SimpleDBTest window = new SimpleDBTest();
					window.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Open the window
	 */
	public void open() {
		final Display display = Display.getDefault();
		createData();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void createData() {
		m_persons = new ArrayList<>();
		m_persons.add(new Person("Joe", "", "", "", ""));
		m_persons.add(new Person("Jim", "", "", "", ""));
		m_persons.add(new Person("Joan", "", "", "", ""));
		m_persons.add(new Person("Steve", "", "", "", ""));
	}

	/**
	 * Create contents of the window
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setLayout(new GridLayout());
		shell.setSize(403, 127);
		shell.setText("SWT Application");

		comboViewer = new ComboViewer(shell, SWT.BORDER);
		comboViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		text = new Text(shell, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		button = new Button(shell, SWT.NONE);
		button.setEnabled(false);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		button.setText("button");
		initDataBindings();
		//
	}

	protected DataBindingContext initDataBindings() {
		// @formatter:off
		IObservableValue<Person> comboViewerSelectionObserveSelection = ViewerProperties.singleSelection(Person.class).observe(comboViewer);
		IObservableValue<String> textTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(text);
		IObservableValue<Boolean> buttonEnabledObserveWidget = WidgetProperties.enabled().observe(button);
		IObservableValue<Person> comboViewerSelectionObserveSelection_1 = ViewerProperties.singleSelection(Person.class).observe(comboViewer);
		IObservableValue<String> comboViewerNameObserveDetailValue = BeanProperties.value("name", String.class).observeDetail(comboViewerSelectionObserveSelection);
		// @formatter:on
		//
		DataBindingContext bindingContext = new DataBindingContext();
		//
		bindingContext.bindValue(buttonEnabledObserveWidget, comboViewerSelectionObserveSelection_1, null,
				new phonebook.ListSelectionUpdateValueStrategy());
		bindingContext.bindValue(textTextObserveWidget, comboViewerNameObserveDetailValue, null, null);
		//
		ObservableListContentProvider<Person> comboViewerContentProviderList = new ObservableListContentProvider<>();
		comboViewer.setContentProvider(comboViewerContentProviderList);
		//
		IObservableMap<Person, String> comboViewerLabelProviderMaps = BeanProperties
				.value(Person.class, "name", String.class)
				.observeDetail(comboViewerContentProviderList.getKnownElements());
		comboViewer.setLabelProvider(new ObservableMapLabelProvider(comboViewerLabelProviderMaps));
		//
		WritableList<Person> m_personsWritableList = new WritableList<>(m_persons, Person.class);
		comboViewer.setInput(m_personsWritableList);
		//
		return bindingContext;
	}
}