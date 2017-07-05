// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.egt;

import java.awt.Dimension;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;

import gov.nist.isg.egt.log.Log;
import ij.plugin.frame.Recorder;

/**
 * Created by mmajursk on 5/21/2014.
 */
public class EGT_GUI extends JFrame {

  private OptionsPanel optionsPanel;
  private double minimumObjectSize;
  private double minimumHoleSizeInPixels;
  private double maximumHoleSizeInPixels;
  private String keepHolesWithJoinOperator;
  private double minimumHoleIntensityPercentile;
  private double maximumHoleIntensityPercentile;
  private double greedy;
  private boolean superimpose;
  private boolean contour;
  private boolean newWindow;
  private String color;

  private DialogButtonListener segmentActionListener;

  /**
   * Initializes the Gradient Threshold GUI
   */
  public EGT_GUI() {
    // Taken from Generic Dialog for creating a new dialog in ImageJ.
    this("Empirical Gradient Threshold");
  }

  /**
   * Initializes the Gradient Threshold GUI
   *
   * @param title the title of the dialog
   */
  public EGT_GUI(String title) {
    super(title);

    this.setSize(new Dimension(400, 450));

    // setup logging
    Log.setLogLevel(Log.LogType.MANDATORY);
//    Log.enableTiming();

    // add the options panel into the gui
    optionsPanel = new OptionsPanel();
    this.add(optionsPanel);

    // setup the button listeners
    initListeners();
    initToolTip();
    this.setVisible(true);

  }


  /**
   * Initialize the button and window listeners
   */
  private void initListeners() {
    segmentActionListener = new DialogButtonListener();

    optionsPanel.getSegmentButton().addActionListener(segmentActionListener);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        performExit();
      }
    });

    // Adds listener for tab key to select all in a text field.
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(
        new KeyEventPostProcessor() {
          @Override
          public boolean postProcessKeyEvent(final KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
              if (e.getSource() instanceof JTextField) {
                JTextField textField = (JTextField) e.getSource();
                textField.selectAll();
              }
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              if (e.getSource() == optionsPanel.getSegmentButton())
                segmentActionListener.start();
            }
            return false;
          }
        });
  }


  private void initToolTip() {
    ToolTipManager ttm = ToolTipManager.sharedInstance();
    ttm.setInitialDelay(500);
    ttm.setReshowDelay(500);
    ttm.setDismissDelay(60 * 1000);
  }


  public class DialogButtonListener implements ActionListener {
    public void start() {
      optionsPanel.getSegmentButton().setEnabled(false);
      // call the segmentation function
      parseInputs();
      Empirical_Gradient_Threshold.segment(minimumObjectSize,
          minimumHoleSizeInPixels, maximumHoleSizeInPixels,
          keepHolesWithJoinOperator,
          minimumHoleIntensityPercentile, maximumHoleIntensityPercentile,
          greedy, superimpose, contour, newWindow, color);
      optionsPanel.getSegmentButton().setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == optionsPanel.getSegmentButton()) {
        start();
      }
    }
  }

  public void performExit() {
    Log.debug("Performing Exit");
    this.dispose();
  }

  private void parseInputs() {

    // extract the parameters from the GUI
    minimumObjectSize = optionsPanel.getMinimumObjectSizeInPixels();
    minimumHoleSizeInPixels = optionsPanel.getMinimumHoleSizeInPixels();
    maximumHoleSizeInPixels = optionsPanel.getMaximumHoleSizeInPixels();
    keepHolesWithJoinOperator = optionsPanel.getKeepHolesWithJoinOperator();
    minimumHoleIntensityPercentile = optionsPanel.getMinimumHoleIntensityPercentile();
    maximumHoleIntensityPercentile = optionsPanel.getMaximumHoleIntensityPercentile();

    greedy = optionsPanel.getGreedy();
    superimpose = optionsPanel.isSuperimpose();
    contour = optionsPanel.isSuperimposeContour();
    newWindow = optionsPanel.isGenerateNewImage();
    color = optionsPanel.getSuperimposeColor().toLowerCase();

    logInputs();
    record();
  }

  private void logInputs() {
    Log.debug("\n********* NIST Empirical Gradient Threshold FIJI Plugin *********");
    Log.debug("Parameters loaded from GUI");
    Log.debug("minimum object size: " + minimumObjectSize);
    Log.debug("minimum hole size in pixels: " + minimumHoleSizeInPixels);
    Log.debug("maximum hole size in pixels: " + maximumHoleSizeInPixels);
    Log.debug("keep holes with join operator: " + keepHolesWithJoinOperator);
    Log.debug("minimum hole intensity percentile: " + minimumHoleIntensityPercentile);
    Log.debug("maximum hole intensity percentile: " + maximumHoleIntensityPercentile);
    Log.debug("greedy: " + greedy);
    Log.debug("superimpose result: " + superimpose);
    Log.debug("superimpose contour: " + contour);
    Log.debug("superimpose color: " + color);
    Log.debug("result in new window: " + newWindow);
  }


  private void record() {
    Log.debug("Recording Macro Options");

    String command = "call(\"gov.nist.isg.egt.Empirical_Gradient_Threshold.macro";
    command += "\", \"" + Double.toString(minimumObjectSize);
    command += "\", \"" + Double.toString(minimumHoleSizeInPixels);
    command += "\", \"" + Double.toString(maximumHoleSizeInPixels);
    command += "\", \"" + keepHolesWithJoinOperator;
    command += "\", \"" + Double.toString(minimumHoleIntensityPercentile);
    command += "\", \"" + Double.toString(maximumHoleIntensityPercentile);
    command += "\", \"" + Double.toString(greedy);

    // If one of the gov.nist.isg.egt.display parameters has been used, generate them for recording as well
    if (superimpose || contour || newWindow) {
      command += "\", \"" + Boolean.toString(superimpose);
      command += "\", \"" + Boolean.toString(contour);
      command += "\", \"" + Boolean.toString(newWindow);
      command += "\", \"" + color;
    }

    command += "\");\n";
    if (Recorder.record)
      Recorder.recordString(command);
  }


}
