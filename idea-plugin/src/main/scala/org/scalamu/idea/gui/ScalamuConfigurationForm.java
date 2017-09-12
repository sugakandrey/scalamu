package org.scalamu.idea.gui;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.scalamu.idea.configuration.ScalamuRunConfiguration;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;

@SuppressWarnings({"unused", "FieldCanBeLocal", "unchecked"})
public class ScalamuConfigurationForm {
  private JTabbedPane mainPanel;
  private JPanel generalPane;
  private JPanel advancedPane;
  private JTextField targetClasses;
  private JTextField targetTests;
  private TextFieldWithBrowseButton scalamuJarPath;
  private JFormattedTextField parallelism;
  private ModulesComboBox modulesComboBox;
  private TextFieldWithBrowseButton reportDIr;
  private RawCommandLineEditor jvmParams;
  private JCheckBox enableVerboseLoggingCheckBox;
  private JFormattedTextField timeoutFactor;
  private JFormattedTextField timeoutConst;
  private JCheckBox openReportInBrowserCheckBox;
  private JComboBox<BrowserFamily> browserComboBox;
  private JBList activeMutators;
  private ConfigurationModuleSelector moduleSelector;

  public ScalamuConfigurationForm(Project project, ScalamuRunConfiguration configuration) {
    $$$setupUI$$$();
    setupTimeouts();
    setupParallelism();
    setupBrowserComboBox();
    setupActiveMutators(project);
    setupModuleSelector(project, configuration);
  }

  public JTabbedPane getMainPanel() {
    return mainPanel;
  }

  public void apply(ScalamuRunConfiguration configuration) {

  }

  private <N extends Number & Comparable<? super N>> DefaultFormatterFactory makeNumberFormatterFactory(N min, N max) {
    NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format) {
      @Override
      public Object stringToValue(String text) throws ParseException {
        return text.isEmpty()
                ? null
                : super.stringToValue(text);
      }
    };

    formatter.setValueClass(min.getClass());
    formatter.setMinimum(min);
    formatter.setMaximum(max);
    formatter.setAllowsInvalid(false);
    return new DefaultFormatterFactory(formatter);
  }

  private void setupActiveMutators(Project project) {
    CollectionListModel listModel = new CollectionListModel();
    activeMutators.setModel(listModel);

    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(activeMutators);

    JPanel panel = decorator.setAddAction(anActionButton -> {
      String result = Messages.showInputDialog(project, "Enter mutator name:", "Add Mutator", null);
      if (result != null) {
        listModel.add(result);
      }
    }).setRemoveAction(anActionButton -> {
      ListUtil.removeSelectedItems(activeMutators);
    }).createPanel();
    
    mainPanel.insertTab("Mutators", null, panel, null, 1);
  }

  private void setupParallelism() {
    DefaultFormatterFactory factory = makeNumberFormatterFactory(1, Integer.MAX_VALUE);
    parallelism.setFormatterFactory(factory);
    parallelism.setText("1");
  }

  private void setupTimeouts() {
    DefaultFormatterFactory constFactory = makeNumberFormatterFactory(0, Integer.MAX_VALUE);
    timeoutConst.setFormatterFactory(constFactory);
    timeoutConst.setText("2000");

    DefaultFormatterFactory factorFactory = makeNumberFormatterFactory(1, Integer.MAX_VALUE);
    timeoutFactor.setFormatterFactory(factorFactory);
    timeoutFactor.setText("1.5");
  }

  private void setupBrowserComboBox() {
    for (WebBrowser browser : WebBrowserManager.getInstance().getActiveBrowsers()) {
      browserComboBox.addItem(browser.getFamily());
    }
    browserComboBox.setRenderer(new ListCellRendererWrapper<BrowserFamily>() {
      @Override
      public void customize(JList list, BrowserFamily value, int index, boolean selected, boolean hasFocus) {
        if (value != null) {
          setText(value.getName());
          setIcon(value.getIcon());
        }
      }
    });
    if (browserComboBox.getItemCount() < 2) {
      browserComboBox.setVisible(false);
    } else {
      browserComboBox.setSelectedItem(0);
    }
  }

  private void setupModuleSelector(Project project, ScalamuRunConfiguration configuration) {
    moduleSelector = new ConfigurationModuleSelector(project, modulesComboBox);
    moduleSelector.reset(configuration);
    modulesComboBox.fillModules(project);
    modulesComboBox.setEnabled(true);
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    mainPanel = new JTabbedPane();
    panel1.add(mainPanel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
    generalPane = new JPanel();
    generalPane.setLayout(new GridLayoutManager(10, 2, new Insets(0, 0, 0, 0), -1, -1));
    mainPanel.addTab("General", generalPane);
    targetClasses = new JTextField();
    targetClasses.setText("*");
    generalPane.add(targetClasses, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label1 = new JLabel();
    label1.setText("Target tests:");
    generalPane.add(label1, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    targetTests = new JTextField();
    targetTests.setText("*");
    generalPane.add(targetTests, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setText("Path to Scalamu jar:");
    generalPane.add(label2, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    scalamuJarPath = new TextFieldWithBrowseButton();
    scalamuJarPath.setText("");
    generalPane.add(scalamuJarPath, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label3 = new JLabel();
    label3.setText("Target classes:");
    generalPane.add(label3, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label4 = new JLabel();
    label4.setText("Number of simultaneously running analysers:");
    generalPane.add(label4, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label5 = new JLabel();
    label5.setText("Use classpath of module:");
    generalPane.add(label5, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    modulesComboBox = new ModulesComboBox();
    generalPane.add(modulesComboBox, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    parallelism = new JFormattedTextField();
    parallelism.setText("1");
    generalPane.add(parallelism, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    activeMutators = new JBList();
    final DefaultListModel defaultListModel1 = new DefaultListModel();
    defaultListModel1.addElement("1");
    defaultListModel1.addElement("2");
    defaultListModel1.addElement("3");
    defaultListModel1.addElement("4");
    activeMutators.setModel(defaultListModel1);
    generalPane.add(activeMutators, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    advancedPane = new JPanel();
    advancedPane.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), -1, -1));
    mainPanel.addTab("Advanced", advancedPane);
    final JLabel label6 = new JLabel();
    label6.setText("Report directory:");
    advancedPane.add(label6, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    reportDIr = new TextFieldWithBrowseButton();
    reportDIr.setText("");
    advancedPane.add(reportDIr, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label7 = new JLabel();
    label7.setText("Analyser JVM parameters:");
    advancedPane.add(label7, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    jvmParams = new RawCommandLineEditor();
    jvmParams.setText("");
    advancedPane.add(jvmParams, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label8 = new JLabel();
    label8.setText("Timeout factor:");
    advancedPane.add(label8, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(198, 30), null, 0, false));
    final JLabel label9 = new JLabel();
    label9.setText("Timeout const, ms:");
    advancedPane.add(label9, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    timeoutConst = new JFormattedTextField();
    timeoutConst.setText("2000");
    advancedPane.add(timeoutConst, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    timeoutFactor = new JFormattedTextField();
    timeoutFactor.setText("1.5");
    advancedPane.add(timeoutFactor, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    openReportInBrowserCheckBox = new JCheckBox();
    openReportInBrowserCheckBox.setText("Open report in browser:");
    advancedPane.add(openReportInBrowserCheckBox, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    browserComboBox = new JComboBox();
    advancedPane.add(browserComboBox, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    enableVerboseLoggingCheckBox = new JCheckBox();
    enableVerboseLoggingCheckBox.setText("Enable verbose logging");
    advancedPane.add(enableVerboseLoggingCheckBox, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
  }
}
