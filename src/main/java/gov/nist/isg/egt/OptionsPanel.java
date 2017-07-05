// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.egt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gov.nist.isg.egt.textfield.TextFieldInputPanel;
import gov.nist.isg.egt.textfield.validator.ValidatorDbl;

/**
 * @author Michael Majurski
 */
public class OptionsPanel extends JPanel {

  public enum KeepHolesWithJoinOperator {AND, OR}

  private static final String[] colors = {"red", "green", "blue", "black", "white"};

  private static final double defaultMinimumObjectSizeInPixels = 100;
  private static final double defaultMinimumHoleSizeInPixels = 100;
  private static final double defaultMaximumHoleSizeInPixels = Double.POSITIVE_INFINITY;
  private static final KeepHolesWithJoinOperator defaulKeepHolesWithJoinOperator =
      KeepHolesWithJoinOperator.AND;
  private static final double defaultMinimumHoleIntensityPercentile = 0;
  private static final double defaultMaximumHoleIntensityPercentile = 100;
  private static final int defaultGreedy = 0;
  private static final boolean default_superimpose = false;
  private static final boolean default_contour = false;
  private static final boolean default_newWindow = false;
  private static final int default_color = 1; // index in the colors array

  private static final int greedyRange = 50;

  JFrame guiHelpFrame;
  String helpString = "NIST Empirical Gradient Threshold (EGT) Help\n"
      + "\n"
      + "Minimum Object Size:\n"
      + "\t-Any foreground object smaller in area (pixels) than this value is deleted (set to background). \n"
      + "\t-Valid values are positive integers. ( >= 0 )\n"
      + "\n"
      + "Keep Holes with:\n"
      + "\t\t-A hole is defined as any group of background pixels that is not "
      + "connected to the edge of the image.\n"
      + "Holes within a size range AND/OR intensity range are kept\n"
      + "\t-Minimum Hole Size In Pixels\n"
      + "\t-Maximum Hole Size In Pixels\n"
      + "\t-Operator joining the two keep hole conditions\n"
      + "\t-Minimum Hole Intensity Percentile\n"
      + "\t-Maximum Hole Intensity Percentile\n"
      + "\n"
      + "Greedy:\n"
      + "\t-The higher the greedy value the more area will be assigned to foreground.\n"
      + "\t-Valid values are integers [-50,50].\n"
      + "\t-The greedy slider can be adjusted using the mouse or arrow keys.\n"
      + "\n"
      + "Superimpose Result:\n"
      + "\t-This will superimpose the resulting segmentation mask over the input image using a basic color.\n"
      + "\t-This allows visualization of the segmentation for evaluating its quality.\n"
      + "\n"
      + "\tContour:\n"
      + "\t\t-If selected the superimposed result will only use the edge contour of the segmented mask.\n"
      + "\tColor:\n"
      + "\t\t-This selects the color to superimpose onto the input image.\n"
      + "\n"
      + "New Window:\n"
      + "\t-If selected this will generate the resulting image in a new window.\n"
      + "\t-If not selected the input image will be overwritten by the result.";


  private JButton segmentButton;
  private JButton helpButton;

  private TextFieldInputPanel<Double> minimumObjectSizeInPixels;
  private TextFieldInputPanel<Double> minimumHoleSizeInPixels;
  private TextFieldInputPanel<Double> maximumHoleSizeInPixels;
  private JComboBox keepHolesWithJoinOperator;
  private TextFieldInputPanel<Double> minimumHoleIntensityPercentile;
  private TextFieldInputPanel<Double> maximumHoleIntensityPercentile;
  private JSlider greedySlider;
  private JLabel greedyLabel;
  private int greedy = defaultGreedy;

  private JCheckBox generateNewImage;
  private JCheckBox superimposeSegmentation;
  private JPanel superimposePanel;
  private JCheckBox superimposeContour;
  private JComboBox superimposeColors;


  public OptionsPanel() {
    super();

    initElements();
    initPanel();
  }


  private void initPanel() {

    GridBagLayout layout = new GridBagLayout();
    this.setLayout(layout);
    GridBagConstraints c = new GridBagConstraints();


    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(2, 4, 2, 4);
    c.gridwidth = 3;

    c.gridx = 0;
    c.gridy = 0;
    this.add(minimumObjectSizeInPixels, c);


    JPanel keepHolesWith = new JPanel(layout);
    keepHolesWith.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Keep Holes With"));

    JPanel sizeConstraints = new JPanel();
    sizeConstraints.add(minimumHoleSizeInPixels);
    sizeConstraints.add(new JLabel(" < size(pixels) < "));
    sizeConstraints.add(maximumHoleSizeInPixels);

    c.anchor = GridBagConstraints.CENTER;
    c.gridy = 0;
    keepHolesWith.add(sizeConstraints, c);

    c.gridy = 1;
    keepHolesWith.add(keepHolesWithJoinOperator, c);

    JPanel intensityConstraints = new JPanel();
    intensityConstraints.add(minimumHoleIntensityPercentile);
    intensityConstraints.add(new JLabel(" < intensity(%) < "));
    intensityConstraints.add(maximumHoleIntensityPercentile);

    c.gridy = 2;
    keepHolesWith.add(intensityConstraints, c);


    c.anchor = GridBagConstraints.CENTER;
    c.gridy = 1;
    this.add(keepHolesWith, c);

    c.gridy = c.gridy + 1;
    this.add(greedyLabel, c);

    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridy = c.gridy + 1;
    this.add(greedySlider, c);

    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_START;
    c.gridy = c.gridy + 1;
    this.add(superimposeSegmentation, c);

    c.gridy = c.gridy + 1;
    c.insets = new Insets(0, 40, 0, 8);
    this.add(superimposePanel, c);
    c.insets = new Insets(2, 8, 2, 8);

    c.gridy = c.gridy + 1;
    this.add(generateNewImage, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridwidth = 1;
    c.gridy = c.gridy + 1;
    this.add(segmentButton, c);

    c.gridx = 2;
    this.add(helpButton, c);
  }


  private void initElements() {

    minimumObjectSizeInPixels =
        new TextFieldInputPanel<Double>("Minimum Object Size: ", Double.toString
            (defaultMinimumObjectSizeInPixels), "pixels",
            new ValidatorDbl(0, Double.MAX_VALUE));

    minimumHoleSizeInPixels =
        new TextFieldInputPanel<Double>("", Double.toString
            (defaultMinimumHoleSizeInPixels), "",
            new ValidatorDbl(0, Double.MAX_VALUE));

    maximumHoleSizeInPixels =
        new TextFieldInputPanel<Double>("", Double.toString
            (defaultMaximumHoleSizeInPixels), "",
            new ValidatorDbl(0, Double.POSITIVE_INFINITY));

    keepHolesWithJoinOperator = new JComboBox(KeepHolesWithJoinOperator.values());
    keepHolesWithJoinOperator.setToolTipText("<html>Controls how the hole size " +
        "and the intensity percentile conditions are combined</html>");

    minimumHoleIntensityPercentile =
        new TextFieldInputPanel<Double>("", Double.toString
            (defaultMinimumHoleIntensityPercentile), "",
            new ValidatorDbl(0, 100));

    maximumHoleIntensityPercentile =
        new TextFieldInputPanel<Double>("", Double.toString
            (defaultMaximumHoleIntensityPercentile), "",
            new ValidatorDbl(0, 100));


    superimposeSegmentation = new JCheckBox("Superimpose Result", default_superimpose);
    superimposeSegmentation.setToolTipText("<html>Superimpose the segmentation result over the original image.</html>");
    superimposeSegmentation.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean state = superimposeSegmentation.isSelected();
        superimposeContour.setEnabled(state);
        superimposeColors.setEnabled(state);
      }
    });

    superimposeContour = new JCheckBox("contour", default_contour);
    superimposeContour.setToolTipText("<html>Controls whether the resulting mask is superimposed, or just its contour.</html>");
    superimposeContour.setEnabled(default_superimpose);

    superimposeColors = new JComboBox(colors);
    superimposeColors.setSelectedIndex(default_color);
    superimposeColors.setEnabled(default_superimpose);

    superimposePanel = new JPanel(new FlowLayout());
    superimposePanel.add(superimposeContour);
    superimposePanel.add(new JLabel("in"));
    superimposePanel.add(superimposeColors);


    generateNewImage = new JCheckBox("New Window", default_newWindow);
    generateNewImage.setToolTipText("<html>Segmentation Result will be displayed in a new image window.</html>");

    greedyLabel = new JLabel("Greedy: " + Integer.toString(greedy));

    greedySlider = new JSlider(JSlider.HORIZONTAL, -greedyRange, greedyRange, 0);
    greedySlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
          greedy = source.getValue();
          greedyLabel.setText("Greedy: " + Integer.toString(greedy));
        }
      }
    });
    greedySlider.setMajorTickSpacing(10);
    greedySlider.setMinorTickSpacing(1);
    greedySlider.setPaintTicks(true);
    greedySlider.setPaintLabels(true);
    greedySlider.setSnapToTicks(true);
    greedySlider.setToolTipText("<html>Controls how greedy foreground objects are.<br>"
        + "Adjust with arrow keys, or the mouse.<br>"
        + "The higher the greedy value the more area will be assigned to foreground.<br>"
        + "A greedy of 5 lowers the gradient threshold by 5 percentile points.</html>");

    segmentButton = new JButton("Segment");
    segmentButton.setToolTipText("<html>Segment Image(s) using current settings.</html>");

    helpButton = new JButton("Help");
    helpButton.setToolTipText("<html>Display Help</html>");
    helpButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == helpButton) {
          // open a new window to gov.nist.isg.egt.display the help in
          if (guiHelpFrame == null) {
            guiHelpFrame = new JFrame("EGT Help");
            guiHelpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            String temp = helpString.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;").replace("\n", "<br>");
            temp = "<html><body>" + temp + "</body></html>";
            panel.add(new JLabel(temp), BorderLayout.LINE_START);

            guiHelpFrame.add(panel);
            guiHelpFrame.pack();
            guiHelpFrame.setLocationRelativeTo(null);
          }

          guiHelpFrame.setVisible(true);

        }
      }
    });


  }


  public boolean isGenerateNewImage() {
    return generateNewImage != null && generateNewImage.isSelected();
  }

  public boolean isSuperimpose() {
    return superimposeSegmentation != null && superimposeSegmentation.isSelected();
  }

  public boolean isSuperimposeContour() {
    return superimposeContour != null && superimposeContour.isSelected();
  }

  public String getSuperimposeColor() {
    if (superimposeColors != null) {
      int i = superimposeColors.getSelectedIndex();
      return colors[i];
    } else
      return "green";
  }

  public JButton getSegmentButton() {
    return segmentButton;
  }

  public JButton getHelpButton() {
    return helpButton;
  }

  public int getGreedy() {
    return greedy;
  }


  public double getMinimumObjectSizeInPixels() {
    return minimumObjectSizeInPixels.getValue();
  }

  public double getMinimumHoleSizeInPixels() {
    return minimumHoleSizeInPixels.getValue();
  }

  public double getMaximumHoleSizeInPixels() {
    return maximumHoleSizeInPixels.getValue();
  }

  public double getMinimumHoleIntensityPercentile() {
    return minimumHoleIntensityPercentile.getValue();
  }

  public double getMaximumHoleIntensityPercentile() {
    return maximumHoleIntensityPercentile.getValue();
  }

  public String getKeepHolesWithJoinOperator() {
    KeepHolesWithJoinOperator val = (KeepHolesWithJoinOperator) keepHolesWithJoinOperator
        .getSelectedItem();
    return val.name();
  }


  public String[] getColors() {
    return colors;
  }

}
