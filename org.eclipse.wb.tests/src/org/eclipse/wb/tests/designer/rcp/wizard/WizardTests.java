package org.eclipse.wb.tests.designer.rcp.wizard;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		Eclipse4WizardTest.class,
		JFaceWizardTest.class,
		RcpWizardTest.class,
		SwtWizardTest.class
})
public class WizardTests {
}
