// NIST-developed software is provided by NIST as a public service. You may use, copy and distribute copies of the software in any medium, provided that you keep intact this entire notice. You may improve, modify and create derivative works of the software or any portion of the software, and you may copy and distribute such modifications or works. Modified works should carry a notice stating that you changed the software and should note the date and nature of any such change. Please explicitly acknowledge the National Institute of Standards and Technology as the source of the software.

// NIST-developed software is expressly provided "AS IS." NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT OR ARISING BY OPERATION OF LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT AND DATA ACCURACY. NIST NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.

// You are solely responsible for determining the appropriateness of using and distributing the software and you assume all risks associated with its use, including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and the unavailability or interruption of operation. This software is not intended to be used in any situation where a failure could cause risk of injury or damage to property. The software developed by NIST employees is not subject to copyright protection within the United States.


package gov.nist.isg.egt.display;


import java.awt.Color;
import java.util.Arrays;

import ij.process.LUT;


public class ColorMap {
  public byte r[];
  public byte g[];
  public byte b[];
  public LUT lut = null;


  public static ColorMap getMap(String name) {
    if (name.equalsIgnoreCase("gray"))
      return ColorMap.gray();
    if (name.equalsIgnoreCase("jet"))
      return ColorMap.jet();
    if (name.equalsIgnoreCase("fire"))
      return ColorMap.fire();
    if (name.equalsIgnoreCase("ice"))
      return ColorMap.ice();
    if (name.equalsIgnoreCase("hsv"))
      return ColorMap.hsv();
    return ColorMap.gray();
  }


  public LUT getLut() {
    if (lut == null) {
      lut = new LUT(r, g, b);
    }
    return lut;
  }

  // shuffle the color maps colors around
  public void shuffle() {

    for (int i = r.length - 1; i > 1; i--) { // shuffle everything but the zero element
      int rand = (int) (Math.random() * i);
      // if the swap index is zero, find a new swap index
      while (rand == 0) { // ensure that the zero element cannot be swapped with
        rand = (int) (Math.random() * i);
      }
      byte temp = r[i];
      r[i] = r[rand];
      r[rand] = temp;

      temp = g[i];
      g[i] = g[rand];
      g[rand] = temp;

      temp = b[i];
      b[i] = b[rand];
      b[rand] = temp;
    }
    lut = null;
  }

  public static ColorMap jet() {
    int n = 256;
    ColorMap cm = new ColorMap();

    byte r[] = new byte[n];
    byte g[] = new byte[n];
    byte b[] = new byte[n];

    int maxval = 255;
    Arrays.fill(g, 0, n / 8, (byte) 0);
    for (int x = 0; x < n / 4; x++)
      g[x + n / 8] = (byte) (maxval * x * 4 / n);
    Arrays.fill(g, n * 3 / 8, n * 5 / 8, (byte) maxval);
    for (int x = 0; x < n / 4; x++)
      g[x + n * 5 / 8] = (byte) (maxval - (maxval * x * 4 / n));
    Arrays.fill(g, n * 7 / 8, n, (byte) 0);

    for (int x = 0; x < g.length; x++)
      b[x] = g[(x + n / 4) % g.length];
    Arrays.fill(b, n * 7 / 8, n, (byte) 0);
    Arrays.fill(g, 0, n / 8, (byte) 0);
    for (int x = n / 8; x < g.length; x++)
      r[x] = g[(x + n * 6 / 8) % g.length];

    cm.r = r;
    cm.g = g;
    cm.b = b;

    cm.r[0] = 0;
    cm.g[0] = 0;
    cm.b[0] = 0; // add the black in for background color

    return cm;
  }

  public static ColorMap hsv() {

    int n = 256;
    ColorMap cm = new ColorMap();
    cm.r = new byte[n];
    cm.g = new byte[n];
    cm.b = new byte[n];

    Color c;
    cm.r[0] = 0;
    cm.g[0] = 0;
    cm.b[0] = 0; // add the black in for background color
    for (int i = 1; i < 256; i++) {
      c = Color.getHSBColor(i / 255f, 1f, 1f);
      cm.r[i] = (byte) c.getRed();
      cm.g[i] = (byte) c.getGreen();
      cm.b[i] = (byte) c.getBlue();
    }
    return cm;
  }


  public static ColorMap fire() {
    int[] reds = {0, 0, 1, 25, 49, 73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255};
    int[] greens = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161, 175, 190, 205, 219, 234, 248, 255, 255, 255, 255};
    int[] blues = {0, 61, 96, 130, 165, 192, 220, 227, 210, 181, 151, 122, 93, 64, 35, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 98, 160, 223, 255};

    ColorMap cm = new ColorMap();
    int n = 256;
    cm.r = new byte[n];
    cm.g = new byte[n];
    cm.b = new byte[n];

    for (int i = 0; i < reds.length; i++) {
      cm.r[i] = (byte) reds[i];
      cm.g[i] = (byte) greens[i];
      cm.b[i] = (byte) blues[i];
    }
    interpolate(cm.r, cm.g, cm.b, reds.length);

    cm.r[0] = 0;
    cm.g[0] = 0;
    cm.b[0] = 0; // add the black in for background color

    return cm;
  }


  public static ColorMap gray() {
    ColorMap cm = new ColorMap();
    int n = 256;
    cm.r = new byte[n];
    cm.g = new byte[n];
    cm.b = new byte[n];

    for (int i = 0; i < 256; i++) {
      cm.r[i] = (byte) i;
      cm.g[i] = (byte) i;
      cm.b[i] = (byte) i;
    }

    return cm;
  }


  public static ColorMap ice() {
    int[] reds = {0, 0, 0, 0, 0, 0, 19, 29, 50, 48, 79, 112, 134, 158, 186, 201, 217, 229, 242, 250, 250, 250, 250, 251, 250, 250, 250, 250, 251, 251, 243, 230};
    int[] greens = {156, 165, 176, 184, 190, 196, 193, 184, 171, 162, 146, 125, 107, 93, 81, 87, 92, 97, 95, 93, 93, 90, 85, 69, 64, 54, 47, 35, 19, 0, 4, 0};
    int[] blues = {140, 147, 158, 166, 170, 176, 209, 220, 234, 225, 236, 246, 250, 251, 250, 250, 245, 230, 230, 222, 202, 180, 163, 142, 123, 114, 106, 94, 84, 64, 26, 27};

    ColorMap cm = new ColorMap();
    int n = 256;
    cm.r = new byte[n];
    cm.g = new byte[n];
    cm.b = new byte[n];

    for (int i = 0; i < reds.length; i++) {
      cm.r[i] = (byte) reds[i];
      cm.g[i] = (byte) greens[i];
      cm.b[i] = (byte) blues[i];
    }
    interpolate(cm.r, cm.g, cm.b, reds.length);

    cm.r[0] = 0;
    cm.g[0] = 0;
    cm.b[0] = 0; // add the black in for background color

    return cm;
  }


  private static void interpolate(byte[] reds, byte[] greens, byte[] blues, int nColors) {
    byte[] r = new byte[nColors];
    byte[] g = new byte[nColors];
    byte[] b = new byte[nColors];
    System.arraycopy(reds, 0, r, 0, nColors);
    System.arraycopy(greens, 0, g, 0, nColors);
    System.arraycopy(blues, 0, b, 0, nColors);
    double scale = nColors / 256.0;
    int i1, i2;
    double fraction;
    for (int i = 0; i < 256; i++) {
      i1 = (int) (i * scale);
      i2 = i1 + 1;
      if (i2 == nColors) i2 = nColors - 1;
      fraction = i * scale - i1;
      reds[i] = (byte) ((1.0 - fraction) * (r[i1] & 255) + fraction * (r[i2] & 255));
      greens[i] = (byte) ((1.0 - fraction) * (g[i1] & 255) + fraction * (g[i2] & 255));
      blues[i] = (byte) ((1.0 - fraction) * (b[i1] & 255) + fraction * (b[i2] & 255));
    }
  }


  /**
   * Get the RGB value associated with an entry in this ColorMap
   */
  public int getColor(int idx) {
    int pixel;
    if (idx >= r.length) {
      // compose the single 32 bit int from the 3 8bit rgb components
      pixel = ((255 << 16) & 0xff0000)
          | ((255 << 8) & 0xff00)
          | (255 & 0xff);
    } else {
      // compose the single 32 bit int from the 3 8bit rgb components
      pixel = ((r[idx] << 16) & 0xff0000)
          | ((g[idx] << 8) & 0xff00)
          | (b[idx] & 0xff);
    }

    return pixel;
  }

}