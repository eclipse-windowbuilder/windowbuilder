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
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import phonebook.model.Person;
import phonebook.model.PhoneGroup;
import phonebook.model.PhoneGroups;

public class PhoneBookUsingDetailComposite {

	private PhoneBookDetailComposite phoneBookDetail;
	private Button deleteGroupButton;
	private Button newGroupButton;
	private Table table_1;
	private Button editGroupButton;
	private TableViewer m_personViewer;
	private TableViewer m_groupViewer;
	private PhoneGroups m_groups = new PhoneGroups();
	private Table table;
	protected Shell shell;
	private Button newPersonButton;
	private Button deletePersonButton;
	private DataBindingContext m_bindingContext;

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
					PhoneBookUsingDetailComposite window = new PhoneBookUsingDetailComposite();
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
		setDefaultValues();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void setDefaultValues() {
		PhoneGroup group1 = new PhoneGroup("Developer Team");
		m_groups.addGroup(group1);
		group1.addPerson(new Person("Konstantin Scheglov", "kosta@nospam.com", "1234567890", "", ""));
		group1.addPerson(new Person("Alexander Mitin", "mitin@nospam.com", "", "0987654321", ""));
		group1.addPerson(new Person("Alexander Lobas", "lobas@nospam.com", "", "", "111-222-333-00"));
		//
		PhoneGroup group2 = new PhoneGroup("Management Team");
		m_groups.addGroup(group2);
		group2.addPerson(new Person("Mike Taylor", "taylor@instantiations.com", "503-598-4900", "", ""));
		group2.addPerson(new Person("Eric Clayberg", "clayberg@instantiations.com", "+1 (503) 598-4900", "", ""));
		group2.addPerson(new Person("Dan Rubel", "dan@instantiations.com", "503-598-4900", "", ""));
		//
		PhoneGroup group3 = new PhoneGroup("Support Team");
		m_groups.addGroup(group3);
		group3.addPerson(new Person("Gina Nebling", "support@instantiations.com", "800-808-3737", "", ""));
	}

	/**
	 * Create contents of the window
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setSize(789, 517);
		shell.setText("Phone Book");

		final SashForm sashForm = new SashForm(shell, SWT.NONE);

		final Composite groupComposite = new Composite(sashForm, SWT.BORDER);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		groupComposite.setLayout(gridLayout);

		final Composite groupToolBarComposite = new Composite(groupComposite, SWT.NONE);
		final GridLayout gridLayout_3 = new GridLayout(3, false);
		groupToolBarComposite.setLayout(gridLayout_3);
		groupToolBarComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		newGroupButton = new Button(groupToolBarComposite, SWT.NONE);
		newGroupButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PhoneGroup group = new PhoneGroup();
				GroupDialog dialog = new GroupDialog(shell, group, true);
				if (dialog.open() == Window.OK) {
					m_groups.addGroup(group);
					m_groupViewer.setSelection(new StructuredSelection(group), true);
					m_bindingContext.updateModels();
				}
			}
		});
		newGroupButton.setText("New...");

		editGroupButton = new Button(groupToolBarComposite, SWT.NONE);
		editGroupButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) m_groupViewer.getSelection();
				PhoneGroup group = (PhoneGroup) selection.getFirstElement();
				//
				GroupDialog dialog = new GroupDialog(shell, group, false);
				dialog.open();
			}
		});
		editGroupButton.setEnabled(false);
		editGroupButton.setText("Edit");

		deleteGroupButton = new Button(groupToolBarComposite, SWT.NONE);
		deleteGroupButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) m_groupViewer.getSelection();
				PhoneGroup group = (PhoneGroup) selection.getFirstElement();
				boolean confirm = MessageDialog.openConfirm(shell, "Confirm Delete",
						"Are you sure you want to delete group '" + group.getName() + "'?");
				if (confirm) {
					m_groups.removeGroup(group);
					m_bindingContext.updateModels();
				}
			}
		});
		deleteGroupButton.setEnabled(false);
		deleteGroupButton.setText("Delete");

		m_groupViewer = new TableViewer(groupComposite, SWT.NONE);
		table_1 = m_groupViewer.getTable();
		table_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final SashForm personSashForm = new SashForm(sashForm, SWT.VERTICAL);

		final Composite personComposite = new Composite(personSashForm, SWT.BORDER);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.horizontalSpacing = 0;
		gridLayout_1.marginWidth = 0;
		gridLayout_1.verticalSpacing = 0;
		gridLayout_1.marginHeight = 0;
		personComposite.setLayout(gridLayout_1);

		final Composite personToolBar = new Composite(personComposite, SWT.NONE);
		personToolBar.setLayout(new GridLayout(2, false));
		personToolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		newPersonButton = new Button(personToolBar, SWT.NONE);
		newPersonButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection groupSelection = (IStructuredSelection) m_groupViewer.getSelection();
				PhoneGroup group = (PhoneGroup) groupSelection.getFirstElement();
				if (group != null) {
					Person person = new Person();
					group.addPerson(person);
					m_personViewer.setSelection(new StructuredSelection(person), true);
					m_bindingContext.updateModels();
				}
			}
		});
		newPersonButton.setText("New...");

		deletePersonButton = new Button(personToolBar, SWT.NONE);
		deletePersonButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection groupSelection = (IStructuredSelection) m_groupViewer.getSelection();
				IStructuredSelection personSelection = (IStructuredSelection) m_personViewer.getSelection();
				PhoneGroup group = (PhoneGroup) groupSelection.getFirstElement();
				Person person = (Person) personSelection.getFirstElement();
				boolean confirm = MessageDialog.openConfirm(shell, "Confirm Delete",
						"Are you sure you want to delete person '" + person.getName() + "'?");
				if (confirm) {
					group.removePerson(person);
					m_bindingContext.updateModels();
				}
			}
		});
		deletePersonButton.setEnabled(false);
		deletePersonButton.setText("Delete");

		m_personViewer = new TableViewer(personComposite, SWT.FULL_SELECTION);
		table = m_personViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
		newColumnTableColumn.setWidth(123);
		newColumnTableColumn.setText("Name");

		final TableColumn newColumnTableColumn_1 = new TableColumn(table, SWT.NONE);
		newColumnTableColumn_1.setWidth(168);
		newColumnTableColumn_1.setText("E-mail");

		final TableColumn newColumnTableColumn_2 = new TableColumn(table, SWT.NONE);
		newColumnTableColumn_2.setWidth(119);
		newColumnTableColumn_2.setText("Phone");

		final TableColumn newColumnTableColumn_3 = new TableColumn(table, SWT.NONE);
		newColumnTableColumn_3.setWidth(100);
		newColumnTableColumn_3.setText("Mobile Phone 1");

		final TableColumn newColumnTableColumn_4 = new TableColumn(table, SWT.NONE);
		newColumnTableColumn_4.setWidth(100);
		newColumnTableColumn_4.setText("Mobile Phone 2");

		phoneBookDetail = new PhoneBookDetailComposite(personSashForm, SWT.BORDER);

		sashForm.setWeights(new int[] { 161, 617 });
		personSashForm.setWeights(new int[] { 1, 1 });
		//
		m_bindingContext = initDataBindings();
	}

	protected DataBindingContext initDataBindings() {
		// @formatter:off
		IObservableValue<Person> phoneBookDetailPersonObserveValue = BeanProperties.value("person", Person.class).observe(phoneBookDetail);
		IObservableValue<Boolean> newGroupButtonEnabledObserveWidget = WidgetProperties.enabled().observe(editGroupButton);
		IObservableValue<PhoneGroup> m_groupViewerSelectionObserveSelection = ViewerProperties.singleSelection(PhoneGroup.class).observe(m_groupViewer);
		IObservableValue<Integer> table_1SelectionIndexObserveWidget = WidgetProperties.singleSelectionIndex().observe(table_1);
		IObservableValue<Person> m_personViewerSelectionObserveSelection = ViewerProperties.singleSelection(Person.class).observe(m_personViewer);
		IObservableValue<Integer> tableSelectionIndexObserveWidget = WidgetProperties.singleSelectionIndex().observe(table);
		IObservableValue<Boolean> deletePersonButtonEnabledObserveWidget = WidgetProperties.enabled().observe(deletePersonButton);
		IObservableValue<Integer> table_1SelectionIndexObserveWidget_1 = WidgetProperties.singleSelectionIndex().observe(table_1);
		IObservableValue<Boolean> deleteGroupButtonEnabledObserveWidget = WidgetProperties.enabled().observe(deleteGroupButton);
		//
		ObservableListContentProvider<PhoneGroup> m_groupViewerContentProviderList = new ObservableListContentProvider<>();
		m_groupViewer.setContentProvider(m_groupViewerContentProviderList);
		//
		IObservableMap<PhoneGroup, String> m_groupViewerLabelProviderMaps = BeanProperties.value(PhoneGroup.class, "name", String.class).observeDetail(m_groupViewerContentProviderList.getKnownElements());
		m_groupViewer.setLabelProvider(new ObservableMapLabelProvider(m_groupViewerLabelProviderMaps));
		//
		IObservableList<PhoneGroup> m_groupsGroupsObserveList = BeanProperties.list("groups", PhoneGroup.class).observe(m_groups);
		m_groupViewer.setInput(m_groupsGroupsObserveList);
		//
		ObservableListContentProvider<Person> m_personViewerContentProviderList = new ObservableListContentProvider<>();
		m_personViewer.setContentProvider(m_personViewerContentProviderList);
		//
		List<IObservableMap<Person, Object>> m_personViewerLabelProviderMaps = new ArrayList<>();
		for (IBeanValueProperty<Person, Object> personValue : BeanProperties.values(Person.class, "name", "email", "phone", "mobilePhone1", "mobilePhone2")) {
			m_personViewerLabelProviderMaps.add(personValue.observeDetail(m_personViewerContentProviderList.getKnownElements()));
		}
		m_personViewer.setLabelProvider(
				new ObservableMapLabelProvider(m_personViewerLabelProviderMaps.toArray(IObservableMap[]::new)));
		//
		IObservableList<Person> m_groupViewerPersonsObserveDetailList = BeanProperties.list("persons", Person.class).observeDetail(m_groupViewerSelectionObserveSelection);
		m_personViewer.setInput(m_groupViewerPersonsObserveDetailList);
		//
		DataBindingContext bindingContext = new DataBindingContext();
		//
		bindingContext.bindValue(table_1SelectionIndexObserveWidget_1, newGroupButtonEnabledObserveWidget,
				new phonebook.SelectionUpdateValueStrategy(),
				new UpdateValueStrategy<>(UpdateValueStrategy.POLICY_NEVER));
		bindingContext.bindValue(table_1SelectionIndexObserveWidget, deleteGroupButtonEnabledObserveWidget,
				new phonebook.SelectionUpdateValueStrategy(),
				new UpdateValueStrategy<>(UpdateValueStrategy.POLICY_NEVER));
		bindingContext.bindValue(tableSelectionIndexObserveWidget, deletePersonButtonEnabledObserveWidget,
				new phonebook.SelectionUpdateValueStrategy(),
				new UpdateValueStrategy<>(UpdateValueStrategy.POLICY_NEVER));
		bindingContext.bindValue(phoneBookDetailPersonObserveValue, m_personViewerSelectionObserveSelection,
				new UpdateValueStrategy<>(UpdateValueStrategy.POLICY_NEVER), null);
		// @formatter:on
		//
		return bindingContext;
	}
}
