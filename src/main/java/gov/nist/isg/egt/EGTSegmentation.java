// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.


package gov.nist.isg.egt;


import java.util.Arrays;

import gov.nist.isg.egt.log.Log;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class EGTSegmentation implements Runnable {

  private static final int NUM_HISTOGRAM_MODES = 3;
  private static final int NUM_HISTOGRAM_BINS = 1000;
  private static final byte OUTPUT_BINARY_PIXEL_VALUE = (byte) 255;

  private ImagePlus grayImp;
  private ImagePlus result;
  private double minimumObjectSize;
  private double minimumHoleSizeInPixels;
  private double maximumHoleSizeInPixels;
  private String keepHolesWithJoinOperator;
  private double minimumHoleIntensityPercentile;
  private double maximumHoleIntensityPercentile;
  private double greedy;


  public EGTSegmentation(ImagePlus grayImp, ImagePlus result, double minimumObjectSize,
                         double minimumHoleSizeInPixels, double maximumHoleSizeInPixels,
                         String keepHolesWithJoinOperator, double minimumHoleIntensityPercentile,
                         double maximumHoleIntensityPercentile, double greedy) {
    this.grayImp = grayImp;
    this.result = result;
    this.minimumObjectSize = minimumObjectSize;
    this.minimumHoleSizeInPixels = minimumHoleSizeInPixels;
    this.maximumHoleSizeInPixels = maximumHoleSizeInPixels;
    this.keepHolesWithJoinOperator = keepHolesWithJoinOperator;
    this.minimumHoleIntensityPercentile = minimumHoleIntensityPercentile;
    this.maximumHoleIntensityPercentile = maximumHoleIntensityPercentile;
    this.greedy = greedy;

  }

  public void run() {
    long startTime = System.currentTimeMillis();
    ImageStack segStack = segmentImagePlus(grayImp, minimumObjectSize,
            minimumHoleSizeInPixels, maximumHoleSizeInPixels,
            keepHolesWithJoinOperator, minimumHoleIntensityPercentile,
            maximumHoleIntensityPercentile, greedy);
    Log.debug("Segmentation took: " + (System.currentTimeMillis() - startTime) + "ms");


    if (result != null) {
      result.setStack(segStack);
      result.updateImage();
    } else {
      result = new ImagePlus(grayImp.getTitle(), segStack);
    }

  }

  public ImagePlus getResult() {
    return result;
  }


  /**
   * Segment an ImagePlus using the EGT technique
   *
   * @param grayImp                        the grayscale ImagePlus (with 1 or more slices in the
   *                                       stack) to segment independently
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
   */
  private static ImageStack segmentImagePlus(ImagePlus grayImp, double minimumObjectSize,
                                             double minimumHoleSizeInPixels, double maximumHoleSizeInPixels,
                                             String keepHolesWithJoinOperator, double minimumHoleIntensityPercentile,
                                             double maximumHoleIntensityPercentile, double greedy) {

    ImageStack grayStack = grayImp.getStack();
    int nbSlices = grayStack.getSize();

    ImageStack outputStack = new ImageStack(grayImp.getWidth(), grayImp.getHeight());

    try {
      for (int i = 1; i <= nbSlices; i++) {

        if (Thread.interrupted()) {
          IJ.error("Interrupted", "Segmentation Interrupted");
          return null;
        }

        String sliceLabel = grayStack.getSliceLabel(i);
        if (sliceLabel == null) {
          sliceLabel = grayImp.getTitle();
        }
        IJ.showProgress(i, nbSlices);
        IJ.showStatus("Segmenting (" + i + "/" + nbSlices + ") " + sliceLabel);
        ImageProcessor ip = grayStack.getProcessor(i);
        ip = segmentImageProcessor(ip, minimumObjectSize,
                minimumHoleSizeInPixels, maximumHoleSizeInPixels,
                keepHolesWithJoinOperator, minimumHoleIntensityPercentile,
                maximumHoleIntensityPercentile, greedy);

        // copy the new segmented data into the image stack
        String imgName = grayStack.getSliceLabel(i);
        if (imgName == null) {
          imgName = grayImp.getTitle();
        }
        outputStack.addSlice(imgName, ip);
      }
    } catch (Exception err) {
      IJ.error(err.getMessage());
    }

    IJ.showProgress(1.0);
    return outputStack;
  }


  /**
   * Segment an ImageProcessor using the EGT technique
   *
   * @param ip                             the ImageProcessor to segment
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
   */
  public static ImageProcessor segmentImageProcessor(ImageProcessor ip, double minimumObjectSize,
                                                     double minimumHoleSizeInPixels, double maximumHoleSizeInPixels,
                                                     String keepHolesWithJoinOperator, double minimumHoleIntensityPercentile,
                                                     double maximumHoleIntensityPercentile, double greedy) {

    int width = ip.getWidth();
    int height = ip.getHeight();

    float[] gradientPixels = getGradientPixels(ip);

    // get the EGT percentile threshold based on the gradient histogram
    double percentileThreshold = getPercentileThreshold(getHistogram(gradientPixels));


    // account for the greedy value
    percentileThreshold -= Math.round(greedy);
    // bound threshold to valid value [0-100]
    percentileThreshold = (percentileThreshold > 100) ? 100 : percentileThreshold;
    percentileThreshold = (percentileThreshold < 1) ? 1 : percentileThreshold;

    Log.debug("Percentile Threshold Value: " + percentileThreshold);

    // convert the percentileThreshold into a pixelThreshold
    // Warning: This function modifies the order of the nonZeroPixels array
    double[] queryPercentile = {percentileThreshold / 100};
    double[] pixelThreshold = prctile(queryPercentile, getNonZerosPixels(gradientPixels));
    Log.debug("Pixel Threshold Value: " + pixelThreshold);


    // apply the threshold to the gradient pixels (not just the nonzero ones)
    for (int k = 0; k < gradientPixels.length; k++) {
      gradientPixels[k] = (gradientPixels[k] > pixelThreshold[0]) ? 1 : 0;
    }

    // Fill holes between min and mix size, and/or min and max intensity percentiles
    // this function modifies gradientPixels which contains the mask
    fillHoles(ip, gradientPixels, minimumHoleSizeInPixels,
            maximumHoleSizeInPixels, keepHolesWithJoinOperator, minimumHoleIntensityPercentile / 100,
            maximumHoleIntensityPercentile / 100);

    // erode segmented image with a disk radius 1
    ConnectedComponents.erodeDisk1(gradientPixels, width, height);

    // remove objects below minObjectSize
    removeObjectsSmallerThan(gradientPixels, minimumObjectSize, width, height);

    ip = createByteProcessorFromPixels(gradientPixels, width, height);

    return ip;
  }


  private static float[] getGradientPixels(ImageProcessor ip) {

    // allocate memory for the gradient pixels
    float[] gradientPixels = new float[ip.getHeight()*ip.getWidth()];

    // extract a deep copy of the pixels as floats
    for(int i = 0; i < ip.getHeight()*ip.getWidth(); i++) {
      gradientPixels[i] = ip.getf(i);
    }

    // wrap the float pixel array into an image processor without duplicating the pixel vector
    ImageProcessor fp = new FloatProcessor(ip.getWidth(), ip.getHeight(), gradientPixels);

    // compute the image gradient
    // findEdges applies the Sobol operator to the pixel data insitu
    fp.findEdges(); // modifies gradientPixels

    // return the pixel gradient vector
    return gradientPixels;
  }

  private static float[] getNonZerosPixels(float[] pixels) {
    // determine the number of nonzero pixels
    int nnzPixels = 0;
    for (int k = 0; k < pixels.length; k++) {
      if (pixels[k] > 0) {
        nnzPixels++;
      }
    }

    // copy the nonzero pixels to a new array
    float[] nonZeroPixels = new float[nnzPixels];
    int k2 = 0;
    for (int k = 0; k < pixels.length; k++) {
      if (pixels[k] > 0) {
        nonZeroPixels[k2++] = pixels[k];
      }
    }

    return nonZeroPixels;
  }

  private static float[] getForegroundPixels(ImageProcessor ip, float[] mask) {
    // determine the number of nonzero pixels
    int nnzPixels = 0;
    for (int k = 0; k < mask.length; k++) {
      if (mask[k] != 0) {
        nnzPixels++;
      }
    }

    // copy the nonzero pixels to a new array
    float[] nonZeroPixels = new float[nnzPixels];
    int k2 = 0;
    for (int k = 0; k < mask.length; k++) {
      if (mask[k] != 0) {
        nonZeroPixels[k2++] = ip.getf(k);
      }
    }

    return nonZeroPixels;
  }

  private static double[] getHistogram(float[] pixels) {
    double[] histData = new double[NUM_HISTOGRAM_BINS + 1];

    // extract min and max of the non zero pixels
    float minValue = Float.MAX_VALUE;
    float maxValue = Float.MIN_VALUE;
    for (int k = 0; k < pixels.length; k++) {
      if(pixels[k] != 0) {
        minValue = (pixels[k] < minValue) ? pixels[k] : minValue;
        maxValue = (pixels[k] > maxValue) ? pixels[k] : maxValue;
      }
    }
    // check that min and max are valid
    if (minValue == Float.MAX_VALUE || maxValue == Float.MIN_VALUE) {
      throw new IllegalArgumentException("Input Image has no nonzero gradient pixels");
    }

    // populate the histogram bins
    double rescale = NUM_HISTOGRAM_BINS / (maxValue - minValue);
    for (int k = 0; k < pixels.length; k++) {
      if(pixels[k] != 0) {
        // + 0.5 is to center the bins at [0 1] instead of [-0.5 0.5]
        histData[(int) ((pixels[k] - minValue) * rescale + 0.5)]++;
      }
    }

    return histData;
  }

  private static double getPercentileThreshold(double[] histData) {

    // compute the averaged mode for the histogram
    double[] modes = new double[NUM_HISTOGRAM_MODES];
    int[] modeIdxs = new int[NUM_HISTOGRAM_MODES];
    for (int k = 0; k < modes.length; k++) {
      modes[k] = 0;
      modeIdxs[k] = 0;
    }

    // compute the top nbHistogramModes modes
    for (int k = 0; k < histData.length; k++) {
      for (int l = 0; l < modes.length; l++) {
        if (histData[k] > modes[l]) {
          // slide all modes down one, to make room for the new one
          for (int m = modes.length - 1; m > l; m--) {
            modes[m] = modes[m - 1];
            modeIdxs[m] = modeIdxs[m - 1];
          }
          modes[l] = histData[k];
          modeIdxs[l] = k;
          break;
        }
      }
    }

    // compute the average of the nbHistogramModes mode locations
    double sum = 0;
    for (int k = 0; k < modes.length; k++) {
      sum += modeIdxs[k];
    }
    int histModeLoc = (int) Math.round(sum / NUM_HISTOGRAM_MODES);

    // normalize the hist between 0-1
    sum = 0;
    for (int k = 0; k < histData.length; k++) {
      sum += histData[k];
    }
    sum /= 100;
    for (int k = 0; k < histData.length; k++) {
      histData[k] /= sum;
    }

    // compute the bounds for generating the density metric
    int lowerBound = 3 * (histModeLoc + 1); // +1 is to convert to 1 based indexing for the math
    if (lowerBound >= histData.length) {
      lowerBound = histData.length - 1;
    }
    lowerBound--; // to convert from 1 based index math to 0 based

    // find the maximum value in the histogram
    double maxHistVal = 0;
    for (int k = 0; k < histData.length; k++) {
      maxHistVal = (histData[k] > maxHistVal) ? histData[k] : maxHistVal;
    }

    // find one of the alternate upper bounds
    int idx = 0;
    for (int k = histModeLoc; k < histData.length; k++) {
      if (histData[k] / maxHistVal < 0.05) {
        idx = k;
        break;
      }
    }

    // find the upper bound
    int upperBound =
            Math.max(idx, 18 * (histModeLoc + 1)); // +1 is to convert to 1 based indexing for the math
    if (upperBound >= histData.length) {
      upperBound = histData.length - 1;
    }
    upperBound--; // to convert from 1 based index math to 0 based

    // compute the density metric
    double densityMetric = 0;
    for (int k = lowerBound; k <= upperBound; k++) {
      densityMetric += histData[k];
    }

    // fit a line between the 80th and the 40th percentiles
    double saturation1 = 3;
    double saturation2 = 42;
    double a = (95.0 - 40.0) / (saturation1 - saturation2);
    double b = 95.0 - a * saturation1;

    double percentileValue = Math.round(a * densityMetric + b);
    percentileValue = (percentileValue > 98) ? 98 : percentileValue;
    percentileValue = (percentileValue < 25) ? 25 : percentileValue;

    return percentileValue;
  }

  private static double[] prctile(double[] percentile, float[] data) {
    // This function modifies the data array

    double[] percentileValues = new double[percentile.length];
    for(int i = 0; i < percentileValues.length; i++) {
      percentileValues[i] = Double.NaN;
    }

    if (data.length == 0)
      return percentileValues;

    for(double d : percentile) {
      if (d < 0.0 || d > 1.0)
        throw new IllegalArgumentException("Invalid Percentile Value. " +
            "percentile must be between 0 and 1 inclusive");
    }


    // sort the pixel values
    Arrays.sort(data);

    for(int i = 0; i < percentileValues.length; i++) {

      // percentileThreshold is [0-100] and needs to be converted to an index into pixels
      int pixelThresholdIndex = (int) Math.round((data.length - 1) * percentile[i]);
      // constrain to valid values
      if (pixelThresholdIndex > (data.length - 1))
        pixelThresholdIndex = data.length - 1;
      if (pixelThresholdIndex < 0)
        pixelThresholdIndex = 0;

      percentileValues[i] = data[pixelThresholdIndex];
    }

    return percentileValues;
  }

  private static void fillHoles(ImageProcessor ip, float[] labeledPixels,
                                double minimumHoleSizeInPixels, double maximumHoleSizeInPixels,
                                String keepHolesWithJoinOperator, double minimumHoleIntensityPercentile,
                                double maximumHoleIntensityPercentile) {

    // input ImageProcessor ip contains the grayscale pixel values
    // input labeledPixels array is binary (0s and 1s)

    // get image size
    int width = ip.getWidth();
    int height = ip.getHeight();

    // create array containing the !labeledPixels, pixel is 1 iff the label value is 0
    int[] holesMask = new int[labeledPixels.length]; // equivalent to matrix B in matlab code
    for(int i = 0; i < labeledPixels.length; i++) {
      // invert the values, 1 -> 0, 0 -> 1
      holesMask[i] = labeledPixels[i] > 0 ? 0 : 1;
    }

    // label the holesMask based on 4 connectivity
    int nbObjects = ConnectedComponents.bwlabel4(holesMask, width, height);
    // after this function array holesMask has been modified to contain the object labels

    // allocate a list of holes which are to be kept
    boolean[] toKeepHolesIdx = new boolean[nbObjects + 1];
    // init to true
    for (int k = 0; k < toKeepHolesIdx.length; k++)
      toKeepHolesIdx[k] = true;


    // compute the area of each hole
    int[] objectAreas = new int[nbObjects + 1];
    for (int k = 0; k < holesMask.length; k++) {
      if (holesMask[k] > 0)
        objectAreas[holesMask[k]]++;
    }


    boolean[] validHolesByArea = new boolean[toKeepHolesIdx.length];

    // Filter the holes based on area
    for (int k = 0; k < validHolesByArea.length; k++) {
      // if the hole is within acceptable size ranges
      int holeArea = objectAreas[k];
      if (holeArea > minimumHoleSizeInPixels && holeArea < maximumHoleSizeInPixels)
        validHolesByArea[k] = true;
    }



    // the grayscale pixel array is not null so compute the hole filtering based on
    // intensity and area

    boolean[] validHolesByIntensity = new boolean[toKeepHolesIdx.length];

    // compute the mean intensity per hole
    double[] objectMeanIntensity = new double[nbObjects + 1];
    double[] counts = new double[nbObjects + 1];

    for (int k = 0; k < labeledPixels.length; k++) {
      if (holesMask[k] > 0) {
        objectMeanIntensity[holesMask[k]] += ip.getf(k);
        counts[holesMask[k]] += 1;
      }
    }
    // compute the mean from the sum
    for (int k = 0; k < objectMeanIntensity.length; k++) {
      objectMeanIntensity[k] = objectMeanIntensity[k] / counts[k];
    }


    // convert the intensity percentiles into pixel value thresholds
    // compute the min and max intensity percentile values
    double[] queryPercentiles = {minimumHoleIntensityPercentile,maximumHoleIntensityPercentile};
    double[] percentileValues = prctile(queryPercentiles, getForegroundPixels(ip, labeledPixels));
    double minPixelIntensityValue = percentileValues[0];
    double maxPixelIntensityValue = percentileValues[1];

    // Filter the holes based on area
    for (int k = 0; k < validHolesByIntensity.length; k++) {
      // if the hole is within acceptable size ranges
      double holeMeanIntensity = objectMeanIntensity[k];
      if (holeMeanIntensity > minPixelIntensityValue && holeMeanIntensity < maxPixelIntensityValue)
        validHolesByIntensity[k] = true;
    }


    // combine the area and intensity conditions
    keepHolesWithJoinOperator = keepHolesWithJoinOperator.toLowerCase();
    if (keepHolesWithJoinOperator.equals("and")) {
      // filter the holes based on size and intensity
      for (int k = 0; k < toKeepHolesIdx.length; k++) {

        // if the && of the conditions is not true, mark the hole for later deletion
        if (!(validHolesByArea[k] && validHolesByIntensity[k]))
          toKeepHolesIdx[k] = false;
      }

    } else {
      if (keepHolesWithJoinOperator.equals("or")) {
        // filter the holes based on size and intensity
        for (int k = 0; k < toKeepHolesIdx.length; k++) {

          // if the || of the conditions is not true, mark the hole for later deletion
          if (!(validHolesByArea[k] || validHolesByIntensity[k]))
            toKeepHolesIdx[k] = false;
        }

      } else {
        throw new IllegalArgumentException("Invalid keepHolesWithJoinOperator. Must be \"AND\" or" +
                " \"OR\".");
      }
    }



    // if a hole touches the edge of the image, we do not know its true size so it needs to be kept
    // image is stored row-wise
    for (int j = 0; j < width; j++) {
      // check the first row
      if (holesMask[j] > 0)
        toKeepHolesIdx[holesMask[j]] = true;

      // check the last row
      int i = height - 1;
      int k = i * width + j;
      if (holesMask[k] > 0)
        toKeepHolesIdx[holesMask[k]] = true;
    }
    for (int i = 0; i < height; i++) {
      // check the first column
      int j = 0;
      int k = i * width + j;
      if (holesMask[k] > 0)
        toKeepHolesIdx[holesMask[i]] = true;

      // check the last column
      j = width - 1;
      k = i * width + j;
      if (holesMask[k] > 0)
        toKeepHolesIdx[holesMask[k]] = true;
    }


    // remove all identified holes from the hole mask
    for (int k = 0; k < labeledPixels.length; k++) {
      if (holesMask[k] > 0) {
        int holeIdx = holesMask[k];
        // if the hole in question is not being kept, fill in the binary segmented mask
        if (!toKeepHolesIdx[holeIdx])
          labeledPixels[k] = 1;

      }
    }
  }

  private static void removeObjectsSmallerThan(float[] labeledPixels, double minObjSize,
                                               int width,
                                               int height) {

    // remove objects below minObjectSize
    int nbObjects = ConnectedComponents.bwlabel8(labeledPixels, width, height);

    int[] objSizes = new int[nbObjects + 1];
    for (int k = 0; k < labeledPixels.length; k++) {
      if (labeledPixels[k] > 0) {
        objSizes[(int) labeledPixels[k]]++;
      }
    }

    // remove pixels that are smaller than minHoleSize
    for (int k = 0; k < labeledPixels.length; k++) {
      if (labeledPixels[k] > 0 && objSizes[(int) labeledPixels[k]] < minObjSize) {
        labeledPixels[k] = 0;
      }
    }
  }

  private static ImageProcessor createByteProcessorFromPixels(float[] labeledPixels, int width, int height) {
    // create the output image
    byte[] bytePixels = new byte[labeledPixels.length];
    for (int k = 0; k < labeledPixels.length; k++) {
      if (labeledPixels[k] > 0)
        bytePixels[k] = OUTPUT_BINARY_PIXEL_VALUE;
    }

    return new ByteProcessor(width, height, bytePixels);
  }

}
