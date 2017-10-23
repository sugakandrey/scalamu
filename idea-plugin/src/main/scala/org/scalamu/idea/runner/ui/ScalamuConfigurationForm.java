package org.scalamu.idea.runner.ui;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.execution.ui.ConfigurationModuleSelector;
import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.scalamu.idea.runner.ScalamuDefaultSettings;
import org.scalamu.idea.runner.ScalamuJarFetcher;
import org.scalamu.idea.runner.ScalamuRunConfiguration;
import scala.Option;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

@SuppressWarnings({"unused", "FieldCanBeLocal", "unchecked"})
public class ScalamuConfigurationForm {
  private JPanel mainPanel;
  private ScalamuFilterTextFieldWithBrowseButton targetClasses;
  private ScalamuFilterTextFieldWithBrowseButton targetTests;
  private TextFieldWithBrowseButton scalamuJarPath;
  private JFormattedTextField parallelism;
  private ModulesComboBox modulesComboBox;
  private TextFieldWithBrowseButton reportDir;
  private RawCommandLineEditor vmParameters;
  private JComboBox<BrowserFamily> browserComboBox;
  private JCheckBox openReportInBrowserCheckBox;
  private JButton downloadScalamuJarButton;
  private ConfigurationModuleSelector moduleSelector;

  public ScalamuConfigurationForm(Project project) {
    $$$setupUI$$$();
    setupParallelism();
    setupBrowserComboBox();
    setupReportDir(project);
    setupModuleSelector(project);
    setupPathToJar(project);
    setupDownloadButton(project);
  }

  public JPanel getMainPanel() {
    return mainPanel;
  }

  public Module getModule() {
    return moduleSelector.getModule();
  }

  public int getParallelism() {
    String text = parallelism.getText();
    return StringUtil.isEmpty(text) ? ScalamuDefaultSettings.parallelism() : Integer.parseInt(text);
  }

  public boolean getOpenInBrowser() {
    return openReportInBrowserCheckBox.isSelected();
  }

  public String getVMParameters() {
    return vmParameters.getText();
  }

  public BrowserFamily getBrowserFamily() {
    return ((BrowserFamily) browserComboBox.getSelectedItem());
  }

  public String getTargetTests() {
    return targetTests.getText();
  }

  public String getTargetClasses() {
    return targetClasses.getText();
  }

  public String getJarPath() {
    return scalamuJarPath.getText();
  }

  public String getReportDir() {
    return reportDir.getText();
  }


  public void apply(ScalamuRunConfiguration configuration) {
    Module selectedModule = configuration.getConfigurationModule().getModule();
    modulesComboBox.setSelectedModule(selectedModule);

    Option<WebBrowser> browser = configuration.browser();
    if (browser.isDefined()) {
      browserComboBox.setSelectedItem(browser.get().getFamily());
    }

    parallelism.setText(Integer.toString(configuration.parallelism()));
    reportDir.setText(configuration.reportDir());
    openReportInBrowserCheckBox.setSelected(configuration.openInBrowser());
    scalamuJarPath.setText(configuration.pathToJar());
    targetClasses.setData(configuration.getTargetClassesAsJava());
    targetTests.setData(configuration.getTargetTestsAsJava());
    vmParameters.setText(configuration.vmParameters());
  }

  private void setupDownloadButton(Project project) {
    downloadScalamuJarButton.addActionListener(e -> {
      Module selectedModule = getModule();
      if (selectedModule == null) {
        Messages.showErrorDialog(project, "No module selected, unable to determine jar version.", "No Module Selected");
      } else {
        try {
          Path path = ScalamuJarFetcher.downloadScalamuJar(selectedModule).get();
          scalamuJarPath.setText(path.toString());
        } catch (Throwable t) {
          Messages.showErrorDialog(project, t.getMessage(), "Error Downloading Scalamu Jar");
        }
      }
    });
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

  private void setupReportDir(Project project) {
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    reportDir.addBrowseFolderListener("Choose Report Directory", null, project, descriptor);
  }

  private void setupPathToJar(Project project) {
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    scalamuJarPath.addBrowseFolderListener("Choose Scalamu Jar", null, project, descriptor);
  }

  private void setupParallelism() {
    parallelism.setInputVerifier(new InputVerifier() {
      @Override
      public boolean verify(JComponent input) {
        JTextField tf = (JTextField) input;
        String text = tf.getText();
        boolean isNumber = NumberUtils.isParsable(text);
        return isNumber && Integer.parseInt(text) >= 1;
      }

      @Override
      public boolean shouldYieldFocus(JComponent input) {
        boolean isValid = verify(input);
        if (!isValid) Messages.showErrorDialog("Please, enter a valid integer >= 1.", "Invalid Input.");
        return isValid;
      }
    });
    parallelism.setText("1");
  }

  private void setupModuleSelector(Project project) {
    moduleSelector = new ConfigurationModuleSelector(project, modulesComboBox);
    modulesComboBox.fillModules(project, JavaModuleType.getModuleType());
    modulesComboBox.setEnabled(true);
    modulesComboBox.addActionListener(e -> {
      Module selectedModule = modulesComboBox.getSelectedModule();
      if (selectedModule != null) {
        reportDir.setText(CompilerPaths.getModuleOutputPath(selectedModule, false));
        Option<Path> cachedScalamuJar = ScalamuJarFetcher.getCachedScalamuJar(selectedModule);
        if (cachedScalamuJar.isDefined()) {
          scalamuJarPath.setText(cachedScalamuJar.get().toString());
        }
      }
    });
  }

  private void createUIComponents() {
    targetClasses = new ScalamuFilterTextFieldWithBrowseButton("Target classes", "All classes");
    targetTests = new ScalamuFilterTextFieldWithBrowseButton("Target tests", "All tests");
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    createUIComponents();
    mainPanel = new JPanel();
    mainPanel.setLayout(new GridLayoutManager(14, 3, new Insets(0, 0, 0, 0), -1, -1));
    final JLabel label1 = new JLabel();
    label1.setText("Target classes:");
    mainPanel.add(label1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    targetClasses.setText("");
    mainPanel.add(targetClasses, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setText("Target tests:");
    mainPanel.add(label2, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    targetTests.setText("");
    mainPanel.add(targetTests, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label3 = new JLabel();
    label3.setText("VM options:");
    mainPanel.add(label3, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    vmParameters = new RawCommandLineEditor();
    vmParameters.setText("");
    mainPanel.add(vmParameters, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label4 = new JLabel();
    label4.setText("Path to Scalamu jar:");
    mainPanel.add(label4, new GridConstraints(8, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    scalamuJarPath = new TextFieldWithBrowseButton();
    scalamuJarPath.setText("");
    mainPanel.add(scalamuJarPath, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    reportDir = new TextFieldWithBrowseButton();
    mainPanel.add(reportDir, new GridConstraints(11, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JLabel label5 = new JLabel();
    label5.setText("Generate report in:");
    mainPanel.add(label5, new GridConstraints(10, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    browserComboBox = new JComboBox();
    mainPanel.add(browserComboBox, new GridConstraints(12, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    openReportInBrowserCheckBox = new JCheckBox();
    openReportInBrowserCheckBox.setText("Open report in browser");
    mainPanel.add(openReportInBrowserCheckBox, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label6 = new JLabel();
    label6.setText("Number of simultaneously running analysers:");
    mainPanel.add(label6, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    parallelism = new JFormattedTextField();
    mainPanel.add(parallelism, new GridConstraints(13, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    downloadScalamuJarButton = new JButton();
    downloadScalamuJarButton.setText("Download Scalamu jar");
    mainPanel.add(downloadScalamuJarButton, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label7 = new JLabel();
    label7.setText("Use classpath of module:");
    mainPanel.add(label7, new GridConstraints(6, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    modulesComboBox = new ModulesComboBox();
    mainPanel.add(modulesComboBox, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return mainPanel;
  }
}
