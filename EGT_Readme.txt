Disclaimer:  IMPORTANT:  This software was developed at the National Institute of Standards and Technology by employees 
of the Federal Government in the course of their official duties. Pursuant to title 17 Section 105 of the United States 
Code this software is not subject to copyright protection and is in the public domain. This is an experimental system. 
NIST assumes no responsibility whatsoever for its use by other parties, and makes no guarantees, expressed or implied, 
about its quality, reliability, or any other characteristic. We would appreciate acknowledgment if the software is used. 
This software can be redistributed and/or modified freely provided that any derivative works bear some notice that they 
are derived from it, and any modified versions bear some notice that they have been modified.

****************************************************************
Package Manifest:
imgs/
	NIST_Logo.tif
sub_functions/
	EGT_Segmentation.m
	fill_holes.m
	find_edges.m
	find_edges_labeled.m
	percentile_computation.m
	print_to_command.m
	print_update.m
	superimpose_colormap_contour.m
	validate_filepath.m
test/
    phase_image_001.tif
    phase_image_002.tif
    phase_image_003.tif
EGT_Segmentation_GUI.m
EGT_Readme.txt


****************************************************************
Instructions
-navigate Matlab's current folder menu to 'EGT_Segmentation_GUI' folder
-run 'EGT_Segmentation_GUI.m'


****************************************************************
Image Folder Parameters
Raw Images Path
	The directory path to the input images to be segmented.
Raw Common Name
	The common name for the images to be segmented

Segmentation Parameters
Min Cell Area
	The minimum object (cell) area in pixels to be considered valid. Objects smaller than this will be set to background.
Fill Holes Smaller Than
	Minimum area of a hole in the image, in pixels, for that hole to be kept. All holes smaller than this will be removed by setting them to foreground. A hole in the image is a region of background that is not 4 connected to the edge of the image.
Morphological Operation
	Select the morphological operation to be applied after the initial binary segmented image has been created. The morphological processing can be used to clean up the resulting segmentation.
	Operations:	
		None
		Dilate
		Erode
		Open: erode followed by a dilate
		Close: dilate followed by an erode
with radius
	The radius of the structuring element to be used in the morphological operation. All structuring elements are disks.
	For example a disk of radius 1 would look as follows
	0 1 0
	1 1 1
	0 1 0
Greedy
	Controls how greedy foreground is with respect to background. If the segmentation is missing some background increasing the greedy parameter in the positive direction will result in more image are being considered foreground.
	
Display Parameters
ColorMap
	The colormap to use to display the image being segmented
Label Image
	Select this if you want the output segmentation to be labeled objects instead of binary
Display Contour
	Displays just the exterior contour of the segmented foreground regions
Display Raw Image
	Will display the input raw image behind the segmented results. This is useful, along with Display Contour, to determine the quality of segmentation
Show Labels
	Display the object label numbers on the image
Color Dropdown (Red by default)
	This controls the color used to display foreground in the segmented image; if Label Image is not selected.

Output Parameters
Save Segmented Images (button)
	This will launch the save dialog to allowing the specification of output parameters and specifying where to save the segmented masks
Format
	The file format to save the images in
Name
	The common name to use when saving the images
Range
	the range of image numbers to save
Type
	Save the binary foreground mask or the displayed image (colormap, contour, etc, options preserved)
	
	
	