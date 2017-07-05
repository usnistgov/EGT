// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.


package gov.nist.isg.egt;

import gov.nist.isg.egt.display.Display;
import gov.nist.isg.egt.log.Log;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Undo;
import ij.WindowManager;
import ij.gui.GUI;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;


/**
 * `
 *
 * @author Michael Majurski
 */
public class Empirical_Gradient_Threshold implements PlugIn {


  public void run(String args) {

    // prevents the recorder from printing the call to launch the macro to the recorder window
    // this is done because when using a JFrame macro recording is taken care of explicitly as
    // opposed to allowing the generic dialog to handle it
    Recorder.setCommand(null);

    // Create the Gui for the user
    EGT_GUI gui = new EGT_GUI();

    GUI.center(gui);
    gui.setVisible(true);
  }


  /**
   * Segment an Image using the EGT technique
   *
   * @param minimumObjectSize              the minimum object size (in pixels) to be considered
   *                                       foreground
   * @param minimumHoleSizeInPixels        the minimum hole size to be considered background
   * @param maximumHoleSizeInPixels        the maximum hole size to be considered background
   * @param keepHolesWithJoinOperator      the operator used to join the holes filling conditions,
   *                                       Options ("AND","OR").
   * @param minimumHoleIntensityPercentile the minimum intensity percentile to be considered
   *                                       background
   * @param maximumHoleIntensityPercentile the maximum intensity percentile to be considered
   *                                       background
   * @param greedy                         the greedy parameter to control adjustments to the
   *                                       automatically selected percentile threshold
   * @param superimpose                    flag controlling whether the resulting image is a
   *                                       superimposed version of the input and the segmented
   *                                       image.
   * @param contour                        flag controlling whether the superimposed image colors
   *                                       all foreground pixels, or just ones that are 8 connected
   *                                       to the background.
   * @param newWindow                      flag controlling whether the resulting segmentation is
   *                                       displayed in a new window.
   * @param color                          string containing the color to use in displaying the
   *                                       superimposed image. Options {"red","green","blue","black","white"}.
   */
  public static void macro(String minimumObjectSize, String minimumHoleSizeInPixels, String
      maximumHoleSizeInPixels, String keepHolesWithJoinOperator, String minimumHoleIntensityPercentile,
                           String maximumHoleIntensityPercentile, String greedy, String superimpose, String contour,
                           String newWindow, String color) {


    segment(Double.parseDouble(minimumObjectSize), Double.parseDouble(minimumHoleSizeInPixels),
        Double.parseDouble(maximumHoleSizeInPixels),
        keepHolesWithJoinOperator, Double.parseDouble(minimumHoleIntensityPercentile),
        Double.parseDouble(maximumHoleIntensityPercentile), Double.parseDouble(greedy),
        Boolean.parseBoolean(superimpose), Boolean.parseBoolean(contour), Boolean.parseBoolean(newWindow), color);
  }


  /**
   * Segment an Image using the EGT technique
   *
   * @param minimumObjectSize              the minimum object size (in pixels) to be considered
   *                                       foreground
   * @param minimumHoleSizeInPixels        the minimum hole size to be considered background
   * @param maximumHoleSizeInPixels        the maximum hole size to be considered background
   * @param keepHolesWithJoinOperator      the operator used to join the holes filling conditions,
   *                                       Options ("AND","OR").
   * @param minimumHoleIntensityPercentile the minimum intensity percentile to be considered
   *                                       background
   * @param maximumHoleIntensityPercentile the maximum intensity percentile to be considered
   *                                       background
   * @param greedy                         the greedy parameter to control adjustments to the
   *                                       automatically selected percentile threshold
   */
  public static void macro(String minimumObjectSize, String minimumHoleSizeInPixels, String
      maximumHoleSizeInPixels, String keepHolesWithJoinOperator, String minimumHoleIntensityPercentile,
                           String maximumHoleIntensityPercentile, String greedy) {


    segment(Double.parseDouble(minimumObjectSize), Double.parseDouble(minimumHoleSizeInPixels),
        Double.parseDouble(maximumHoleSizeInPixels),
        keepHolesWithJoinOperator, Double.parseDouble(minimumHoleIntensityPercentile),
        Double.parseDouble(maximumHoleIntensityPercentile), Double.parseDouble(greedy),
        false, false, false, "green");
  }


  /**
   * Segment an Image using the EGT technique
   *
   * @param minimumObjectSize              the minimum object size (in pixels) to be considered
   *                                       foreground
   * @param minimumHoleSizeInPixels        the minimum hole size to be considered background
   * @param maximumHoleSizeInPixels        the maximum hole size to be considered background
   * @param keepHolesWithJoinOperator      the operator used to join the holes filling conditions,
   *                                       possible values are ('AND','OR').
   * @param minimumHoleIntensityPercentile the minimum intensity percentile to be considered
   *                                       background
   * @param maximumHoleIntensityPercentile the maximum intensity percentile to be considered
   *                                       background
   * @param greedy                         the greedy parameter to control adjustments to the
   *                                       automatically selected percentile threshold
   * @param superimpose                    flag controlling whether the resulting image is a
   *                                       superimposed version of the input and the segmented
   *                                       image.
   * @param contour                        flag controlling whether the superimposed image colors
   *                                       all foreground pixels, or just ones that are 8 connected
   *                                       to the background.
   * @param newWindow                      flag controlling whether the resulting segmentation is
   *                                       displayed in a new window.
   * @param color                          string containing the color to use in displaying the
   *                                       superimposed image. Options {"red","green","blue","black","white"}.
   */
  public static void segment(double minimumObjectSize, double minimumHoleSizeInPixels, double maximumHoleSizeInPixels,
                             String keepHolesWithJoinOperator, double minimumHoleIntensityPercentile, double
                                 maximumHoleIntensityPercentile, double greedy, boolean
                                 superimpose, boolean contour, boolean newWindow, String color) {

    ImagePlus grayImp = IJ.getImage();
    if (grayImp == null) {
      return;
    }

    // Validate that the input image stack in non virtual
    ImageStack grayStack = grayImp.getStack();
    if (grayStack.isVirtual()) {
      IJ.error("Empirical_Gradient_Threshold",
          "This command does not work with virtual stacks.\n"
              + "Use Image>Duplicate to convert to a normal stack.");
      return;
    }

    // Validate the input image stack is a single channel
    if (grayImp.getNChannels() != 1) {
      IJ.error("Empirical_Gradient_Threshold",
          "This command does not work with multichannel image stacks.\n"
              + "Segment one channel at a time.");
      return;
    }

    // Validate the input image stack is grayscale
    int type = grayImp.getType();
    if (type == ImagePlus.GRAY8 || type == ImagePlus.GRAY16 || type == ImagePlus.GRAY32) {
      // do nothing
    } else {
      // the image is non grayscale
      IJ.error("Empirical_Gradient_Threshold",
          "This command does not work with non grayscale images.\n"
              + "Convert the image to grayscale (8, 16, or 32 bit)");
      return;
    }


    Log.debug("Running EGT segmentation");
    ImagePlus resultImp = WindowManager.getImage("result");

    // setup and launch the segmentation in a background thread
    EGTSegmentation seg = new EGTSegmentation(grayImp, resultImp, minimumObjectSize,
        minimumHoleSizeInPixels, maximumHoleSizeInPixels,
        keepHolesWithJoinOperator, minimumHoleIntensityPercentile,
        maximumHoleIntensityPercentile, greedy);

    try {
      seg.run();

      // get the resulting ImagePlus
      resultImp = seg.getResult();
      resultImp.setTitle("result");

      if (superimpose) {
        // make resultImp into the superimposed version
        resultImp = Display.superimposeMask(resultImp, grayImp, contour, color);
        resultImp.setTitle("result");
      }

      if (newWindow) {
        // gov.nist.isg.egt.display the resultImp image to the user
        resultImp.show();
      } else {
        // overwrite the input image with resultImp
        ImagePlus dispImg = WindowManager.getImage("result");
        if (dispImg != null) {
          dispImg.setImage(resultImp);
          dispImg.updateImage();
        } else {
          Undo.setup(Undo.COMPOUND_FILTER, grayImp);
          grayImp.setImage(resultImp);
          grayImp.updateImage();
          Undo.setup(Undo.COMPOUND_FILTER_DONE, grayImp);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      StackTraceElement[] st = e.getStackTrace();
      Log.mandatory("Exception in Empirical_Gradient_Threshold.run: " + e.getMessage());
      for (int i = 0; i < st.length; i++) {
        Log.mandatory(st[i].toString());
      }
      IJ.error("Exception in Empirical_Gradient_Threshold.run", e.getMessage());
    }
  }


  public static void main(String[] args) {
    new ImageJ();

//    IJ.run("Record...");
    IJ.open("C:\\majurski\\image-data\\Misc\\phase_image_001.tif");
//    IJ.open("/Users/mmajurski/Workspace/NIST/I.tif");

    Empirical_Gradient_Threshold egt = new Empirical_Gradient_Threshold();
    egt.run("");
  }

}

