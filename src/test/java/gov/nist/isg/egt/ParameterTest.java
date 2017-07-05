// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.egt;

import org.junit.Test;

import java.io.File;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import static gov.nist.isg.egt.EGTSegmentation.segmentImageProcessor;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ParameterTest {

  @Test
  public void validateAgainstMatlab() {
    String sourceImagePath = null;
    try {
      sourceImagePath = new File(".").getCanonicalPath() + File.separator + "src" + File
          .separator + "test" + File.separator + "imgs" + File.separator;
    } catch (Exception e) {
    }

    // check that the test image exists
    assertNotNull("Source Image filepath must not be null", sourceImagePath);


    ImagePlus sourceImage = new ImagePlus(sourceImagePath + "phase_image_001.tif");
    ImageProcessor sourceIP = sourceImage.getProcessor();

    String[] fillOperators = {"AND", "OR"};


    // loop over the parameter configurations that matlab generated (see "generate_test_results.m")
    for (int minCellSize = 100; minCellSize <= 200; minCellSize += 100) {
      for (int minHoleSize = 10; minHoleSize <= 20; minHoleSize += 10) {
        for (int maxHoleSize = 50; maxHoleSize <= 60; maxHoleSize += 10) {
          for (int minHolePercentileIntensity = 0; minHolePercentileIntensity <= 10;
               minHolePercentileIntensity += 5) {
            for (int maxHolePercentileIntensity = 90; maxHolePercentileIntensity <= 100;
                 maxHolePercentileIntensity += 5) {
              for (int fillIdx = 0; fillIdx <= 1; fillIdx += 1) {
                for (int manualFineTune = -10; manualFineTune <= 10; manualFineTune += 5) {

                  ImageProcessor resultsIP = segmentImageProcessor(sourceIP, minCellSize,
                      minHoleSize, maxHoleSize,
                      fillOperators[fillIdx], minHolePercentileIntensity,
                      maxHolePercentileIntensity, manualFineTune);


                  String fn = String.format("%d_%d_%d_%d_%d_%s_%d.tif", minCellSize,
                      minHoleSize, maxHoleSize, minHolePercentileIntensity,
                      maxHolePercentileIntensity, fillOperators[fillIdx], manualFineTune);

                  // validate that the result ImageProcessor is the same as the one saved on disk
                  ImagePlus comp = new ImagePlus(sourceImagePath + "eval" + File.separator + fn);

                  int nbDiffPixels = Utils.nbDifferentPixels(resultsIP, comp.getProcessor());
                  assertTrue("Matlab and Java segmentation are not pixelwise identical under parameters: " +
                      "minCellSize=" +
                      minCellSize + " minHoleSize=" + minHoleSize + " maxHoleSize="
                      + maxHoleSize + " minHolePercentileIntensity=" +
                      minHolePercentileIntensity + " maxHolePercentileIntensity=" +
                      maxHolePercentileIntensity + " operator=" + fillOperators[fillIdx] + " " +
                      "manualFineTune=" + manualFineTune, nbDiffPixels == 0);


                }
              }
            }
          }
        }
      }
    }


  }


}