// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.

package gov.nist.isg.egt.display;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class Display {


  private static void validateImages(ImagePlus labeled, ImagePlus grayscale) {

    // get the width and height of the labeled image
    int height = labeled.getHeight();
    int width = labeled.getWidth();

    // check that the two images are the same size (width and height)
    if (height != grayscale.getHeight() || width != grayscale.getWidth()) {
      // the two images were not the same size
      throw new IllegalArgumentException("Input images were different sizes.");
    }

//        // check that the two image stacks are the same size
//        if(labeled.getImageStack().getSize() != grayscale.getImageStack().getSize()) {
//            throw new IllegalArgumentException("Input Labeled image stack must have the same number of slices as the grayscale image stack");
//        }

    // get ImageStacks that are behind the ImagePlus objects
    ImageStack labelStack = labeled.getImageStack();

    // get the image processor for the labeled image
    ImageProcessor ipL = labelStack.getProcessor(1); // slices are 1 based
    if (ipL.getBitDepth() != 8 && ipL.getBitDepth() != 16) {
      // the edges in the labeled image cannot be used to generate a superimposed image
      throw new IllegalArgumentException("Input Labeled image was not 8 or 16 bit. It must be a labeled mask, not RGB or 32bit floating point.");
    }
  }

  private static int convertColorString(String maskColor) {
    // create the color to use for the labeled image
    String[] validColors = {"red", "green", "blue", "black", "white"};
    int[] color = {0, 255, 0}; // green default
    for (int i = 0; i < validColors.length; i++) {
      if (maskColor.equalsIgnoreCase(validColors[i])) {
        switch (i) {
          case 0:
            color[0] = 255;
            color[1] = 0;
            color[2] = 0;
            break;
          case 1:
            color[0] = 0;
            color[1] = 255;
            color[2] = 0;
            break;
          case 2:
            color[0] = 0;
            color[1] = 0;
            color[2] = 255;
            break;
          case 3:
            color[0] = 0;
            color[1] = 0;
            color[2] = 0;
            break;
          case 4:
            color[0] = 255;
            color[1] = 255;
            color[2] = 255;
            break;
        }
        break;
      }
    }
    int colorVal = ((color[0] << 16) & 0xff0000)
        | ((color[1] << 8) & 0xff00)
        | (color[2] & 0xff);

    return colorVal;
  }


  // Superimposes the Images applying the color map from the grayscale image
  public static ImagePlus superimposeMask(ImagePlus labeled, ImagePlus grayscale, boolean contour, String maskColor) throws InterruptedException {

    validateImages(labeled, grayscale); // this will throw exception if there is a problem

    // get the width and height of the labeled image
    int height = labeled.getHeight();
    int width = labeled.getWidth();

    // get ImageStacks that are behind the ImagePlus objects
    ImageStack labelStack = labeled.getImageStack();
    ImageStack grayStack = grayscale.getImageStack();

    ImageStack outputStack = new ImageStack(width, height);

    int colorVal = convertColorString(maskColor);
    int endIndex = Math.max(labelStack.getSize(), grayStack.getSize());
    for (int i = 0; i < endIndex; i++) {

      if (Thread.interrupted())
        throw new InterruptedException("Thread Interrupted");

      IJ.showProgress(i + 1, endIndex);

      int sliceNb = i + 1; // slices are 1 based
      if (labelStack.getSize() < sliceNb) sliceNb = labelStack.getSize();
      IJ.showStatus("Superimposing(" + i + "/" + endIndex + ") " + labelStack.getSliceLabel(sliceNb));
      ImageProcessor ipL = labelStack.getProcessor(sliceNb);


      sliceNb = i + 1; // slices are 1 based
      if (grayStack.getSize() < sliceNb) sliceNb = grayStack.getSize();
      ImageProcessor ipG = grayStack.getProcessor(sliceNb);

      // get the Image Processor for the current output slice
      ImageProcessor ipO = ipG.convertToRGB();

      if (contour) {
        // color just the edges of teh objects in labeled image
        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            // if this is foreground pixel
            if (ipL.get(x, y) > 0) {
              int pix = ipL.get(x, y);
              if (x == 0 || y == 0 || x == (width - 1) || y == (height - 1)) {
                // edge pixel
                ipO.set(x, y, colorVal);
              } else {
                // if touching a different label
                if (ipL.get(x - 1, y) != pix || ipL.get(x + 1, y) != pix || ipL.get(x, y - 1) != pix || ipL.get(x, y + 1) != pix ||
                    ipL.get(x - 1, y - 1) != pix || ipL.get(x - 1, y + 1) != pix || ipL.get(x + 1, y - 1) != pix || ipL.get(x + 1, y + 1) != pix) {
                  // border pixel
                  ipO.set(x, y, colorVal);
                }
              }
            }
          }
        }
      } else {
        // Color foreground pixels without regard to edges of objects

        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            // if this is foreground pixel
            if (ipL.get(x, y) > 0) {
              ipO.set(x, y, colorVal);
            }
          }
        }
      }

      String imgName = String.format("superimpose_%08d.tif", i);

      // add the current slice to the output stack
      outputStack.addSlice(imgName, ipO);

    }
    IJ.showProgress(endIndex, endIndex);


    return new ImagePlus("Superimposed", outputStack);
  }


  public static ImagePlus contour(ImagePlus labeled) {

    ImageStack labelStack = labeled.getImageStack();
    // get the image processor for the labeled image
    ImageProcessor ipL = labelStack.getProcessor(1); // slices are 1 based
    if (ipL.getBitDepth() != 8 && ipL.getBitDepth() != 16) {
      // the edges in the labeled image cannot be used to generate a superimposed image
      throw new IllegalArgumentException("Input Labeled image was not 8 or 16 bit. It must be a labeled mask, not RGB or 32bit floating point.");
    }

    int width = labeled.getWidth();
    int height = labeled.getHeight();
    ImageStack outputStack = new ImageStack(width, height);
    for (int i = 0; i < labelStack.getSize(); i++) {
      ipL = labelStack.getProcessor(i + 1); // slices are 1 based

      // add the slice to the output image as an RGB image
      outputStack.addSlice(labelStack.getSliceLabel(i + 1), ipL.createProcessor(width, height));

      // get the Image Processor for the current output slice
      ImageProcessor ipO = outputStack.getProcessor(i + 1);

      // color just the edges of teh objects in labeled image
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          // if this is foreground pixel
          if (ipL.get(x, y) > 0) {
            int pix = ipL.get(x, y);
            if (x == 0 || y == 0 || x == (width - 1) || y == (height - 1)) {
              // edge pixel
              ipO.set(x, y, pix);
            } else {
              // if touching a different label
              if (ipL.get(x - 1, y) != pix || ipL.get(x + 1, y) != pix || ipL.get(x, y - 1) != pix || ipL.get(x, y + 1) != pix ||
                  ipL.get(x - 1, y - 1) != pix || ipL.get(x - 1, y + 1) != pix || ipL.get(x + 1, y - 1) != pix || ipL.get(x + 1, y + 1) != pix) {
                // border pixel
                ipO.set(x, y, pix);
              }
            }
          }
        }
      }
    }

    return new ImagePlus(labeled.getTitle(), outputStack);
  }


}
