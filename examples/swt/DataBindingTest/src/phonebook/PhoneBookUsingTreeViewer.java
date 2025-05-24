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
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import phonebook.model.Person;
import phonebook.model.PhoneGroup;
import phonebook.model.PhoneGroups;

public class PhoneBookUsingTreeViewer {

	class TreeLabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0) {
				if (element instanceof PhoneGroup phoneGroup) {
					return phoneGroup.getName();
				} else if (element instanceof Person person) {
					return person.getName();
				}
			} else if (element instanceof Person person) {
				switch (columnIndex) {
				case 1:
					return person.getEmail();
				case 2:
					return person.getPhone();
				case 3:
					return person.getMobilePhone1();
				case 4:
					return person.getMobilePhone2();
				default:
					return "";
				}
			}
			return "";
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}

	class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return m_groups.getGroups().toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof PhoneGroup) {
				PhoneGroup group = (PhoneGroup) parentElement;
				return group.getPersons().toArray();
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof Person) {
				for (PhoneGroup group : m_groups.getGroups()) {
					if (group.getPersons().contains(element)) {
						return group;
					}
				}
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}

	private TreeViewer m_personViewer;
	private PhoneGroups m_groups = new PhoneGroups();
	private Text m_mobile2Text;
	private Text m_mobile1Text;
	private Text m_phoneText;
	private Text m_emailText;
	private Text m_nameText;
	private Tree tree;
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
					PhoneBookUsingTreeViewer window = new PhoneBookUsingTreeViewer();
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

		final SashForm sashForm = new SashForm(shell, SWT.VERTICAL);

		m_personViewer = new TreeViewer(sashForm, SWT.FULL_SELECTION);
		tree = m_personViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TreeColumn newColumnTableColumn = new TreeColumn(tree, SWT.NONE);
		newColumnTableColumn.setWidth(123);
		newColumnTableColumn.setText("Name");

		final TreeColumn newColumnTableColumn_1 = new TreeColumn(tree, SWT.NONE);
		newColumnTableColumn_1.setWidth(168);
		newColumnTableColumn_1.setText("E-mail");

		final TreeColumn newColumnTableColumn_2 = new TreeColumn(tree, SWT.NONE);
		newColumnTableColumn_2.setWidth(119);
		newColumnTableColumn_2.setText("Phone");

		final TreeColumn newColumnTableColumn_3 = new TreeColumn(tree, SWT.NONE);
		newColumnTableColumn_3.setWidth(100);
		newColumnTableColumn_3.setText("Mobile Phone 1");

		final TreeColumn newColumnTableColumn_4 = new TreeColumn(tree, SWT.NONE);
		newColumnTableColumn_4.setWidth(100);
		newColumnTableColumn_4.setText("Mobile Phone 2");

		m_personViewer.setContentProvider(new TreeContentProvider());
		m_personViewer.setLabelProvider(new TreeLabelProvider());
		m_personViewer.setInput(m_groups);

		final Composite detailComposite = new Composite(sashForm, SWT.BORDER);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		detailComposite.setLayout(gridLayout_2);

		final Label descriptionLabel = new Label(detailComposite, SWT.NONE);
		descriptionLabel.setText("Description:");
		new Label(detailComposite, SWT.NONE);

		final Label label = new Label(detailComposite, SWT.NONE);
		label.setText("Name:");

		m_nameText = new Text(detailComposite, SWT.BORDER);
		final GridData gd_m_nameText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_nameText.setLayoutData(gd_m_nameText);

		final Label emailLabel = new Label(detailComposite, SWT.NONE);
		emailLabel.setText("E-mail:");

		m_emailText = new Text(detailComposite, SWT.BORDER);
		final GridData gd_m_emailText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_emailText.setLayoutData(gd_m_emailText);

		final Label phoneLabel = new Label(detailComposite, SWT.NONE);
		phoneLabel.setText("Phone:");

		m_phoneText = new Text(detailComposite, SWT.BORDER);
		final GridData gd_m_phoneText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_phoneText.setLayoutData(gd_m_phoneText);

		final Label mobilePhone1Label = new Label(detailComposite, SWT.NONE);
		mobilePhone1Label.setText("Mobile Phone 1:");

		m_mobile1Text = new Text(detailComposite, SWT.BORDER);
		final GridData gd_m_mobile1Text = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_mobile1Text.setLayoutData(gd_m_mobile1Text);

		final Label mobilePhone2Label = new Label(detailComposite, SWT.NONE);
		mobilePhone2Label.setText("Mobile Phone 2:");

		m_mobile2Text = new Text(detailComposite, SWT.BORDER);
		final GridData gd_m_mobile2Text = new GridData(SWT.FILL, SWT.CENTER, true, false);
		m_mobile2Text.setLayoutData(gd_m_mobile2Text);
		sashForm.setWeights(new int[] { 273, 214 });
		//
		initDataBindings();
	}

	class PersonViewerUpdateValueStrategy extends UpdateValueStrategy<String, String> {
		@Override
		protected IStatus doSet(IObservableValue<? super String> observableValue, String value) {
			Realm.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					m_personViewer.refresh();
				}
			});
			return super.doSet(observableValue, value);
		}
	}

	protected DataBindingContext initDataBindings() {
		// @formatter:off
		IObservableValue<String> m_nameTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_nameText);
		IObservableValue<Person> m_personViewerSelectionObserveSelection_4 = ViewerProperties.singleSelection(Person.class).observe(m_personViewer);
		IObservableValue<String> m_mobile1TextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_mobile1Text);
		IObservableValue<Person> m_personViewerSelectionObserveSelection = ViewerProperties.singleSelection(Person.class).observe(m_personViewer);
		IObservableValue<Person> m_personViewerSelectionObserveSelection_1 = ViewerProperties.singleSelection(Person.class).observe(m_personViewer);
		IObservableValue<String> m_phoneTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_phoneText);
		IObservableValue<String> m_emailTextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_emailText);
		IObservableValue<Person> m_personViewerSelectionObserveSelection_3 = ViewerProperties.singleSelection(Person.class).observe(m_personViewer);
		IObservableValue<Person> m_personViewerSelectionObserveSelection_2 = ViewerProperties.singleSelection(Person.class).observe(m_personViewer);
		IObservableValue<String> m_mobile2TextTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(m_mobile2Text);
		IObservableValue<String> m_personViewerNameObserveDetailValue = BeanProperties.value("name", String.class).observeDetail(m_personViewerSelectionObserveSelection_4);
		IObservableValue<String> m_personViewerMobilePhone2ObserveDetailValue = BeanProperties.value("mobilePhone2", String.class).observeDetail(m_personViewerSelectionObserveSelection);
		IObservableValue<String> m_personViewerEmailObserveDetailValue = BeanProperties.value("email", String.class).observeDetail(m_personViewerSelectionObserveSelection_2);
		IObservableValue<String> m_personViewerMobilePhone1ObserveDetailValue = BeanProperties.value("mobilePhone1", String.class).observeDetail(m_personViewerSelectionObserveSelection_1);
		IObservableValue<String> m_personViewerPhoneObserveDetailValue = BeanProperties.value("phone", String.class).observeDetail(m_personViewerSelectionObserveSelection_3);
		// @formatter:on
		//
		DataBindingContext bindingContext = new DataBindingContext();
		//
		bindingContext.bindValue(m_personViewerNameObserveDetailValue, m_nameTextTextObserveWidget, null,
				new PersonViewerUpdateValueStrategy());
		bindingContext.bindValue(m_personViewerEmailObserveDetailValue, m_emailTextTextObserveWidget, null,
				new PersonViewerUpdateValueStrategy());
		bindingContext.bindValue(m_personViewerPhoneObserveDetailValue, m_phoneTextTextObserveWidget, null,
				new PersonViewerUpdateValueStrategy());
		bindingContext.bindValue(m_personViewerMobilePhone1ObserveDetailValue, m_mobile1TextTextObserveWidget, null,
				new PersonViewerUpdateValueStrategy());
		bindingContext.bindValue(m_personViewerMobilePhone2ObserveDetailValue, m_mobile2TextTextObserveWidget, null,
				new PersonViewerUpdateValueStrategy());
		//
		return bindingContext;
	}
}
