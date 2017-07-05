% Disclaimer: IMPORTANT: This software was developed at the National
% Institute of Standards and Technology by employees of the Federal
% Government in the course of their official duties. Pursuant to
% title 17 Section 105 of the United States Code this software is not
% subject to copyright protection and is in the public domain. This
% is an experimental system. NIST assumes no responsibility
% whatsoever for its use by other parties, and makes no guarantees,
% expressed or implied, about its quality, reliability, or any other
% characteristic. We would appreciate acknowledgement if the software
% is used. This software can be redistributed and/or modified freely
% provided that any derivative works bear some notice that they are
% derived from it, and any modified versions bear some notice that
% they have been modified.


% Use empty matrix if I doesn't exist or not of interest
function S = fill_holes(S, I, min_hole_size, max_hole_size, hole_min_perct_intensity, hole_max_perct_intensity, fill_holes_bool_oper)

if ~exist('min_hole_size','var') || isempty(min_hole_size)
  min_hole_size = 0;
end
if ~exist('max_hole_size','var') || isempty(max_hole_size)
  max_hole_size = Inf;
end
if ~exist('hole_min_perct_intensity','var') || isempty(hole_min_perct_intensity)
  hole_min_perct_intensity = 0;
end
if ~exist('hole_max_perct_intensity','var') || isempty(hole_max_perct_intensity)
  hole_max_perct_intensity = 100;
end
if ~exist('fill_holes_bool_oper','var') || isempty(fill_holes_bool_oper)
  fill_holes_bool_oper = 'AND';
end

min_hole_size = double(min_hole_size);
max_hole_size = double(max_hole_size);
hole_min_perct_intensity = double(hole_min_perct_intensity);
hole_max_perct_intensity = double(hole_max_perct_intensity);


% Make sure input is binary
S = logical(S);

% Label hole image
CC = bwconncomp(~S, 4);
nb_holes = CC.NumObjects;
B = labelmatrix(CC);


% create a list of which holes (in the labeled B image) which need to be kept. All others will be deleted for being invalid
to_keep_holes_idx = true(nb_holes,1);

if isempty(I)
  % no raw image has been inputed, so intensity filtering of holes cannot happen
  p = regionprops(CC,'Area');
  holes_size = [p.Area]';
  % keep only the holes whose size is between min and max thresholds
  valid_holes_by_area_idx = (holes_size > min_hole_size) & (holes_size < max_hole_size);
  % mark the invalid holes for later deletion
  to_keep_holes_idx(~valid_holes_by_area_idx) = 0;
else
  % the raw image was inputed, so use it to filter based on intensity
  
  % Compute size and mean intensity of each hole
  p = regionprops(CC,I,'Area', 'MeanIntensity');
  holes_size = [p.Area]';
  hole_mean_intensity = [p.MeanIntensity]';
  
  % identify the holes whose size is between the min and max thresholds
  valid_holes_by_area_idx = (holes_size > min_hole_size) & (holes_size < max_hole_size);
  % identify the holes whose intensity is between the min and max thresholds
  min_intensity = percentile_computation(I(S),hole_min_perct_intensity/100);
  max_intensity = percentile_computation(I(S),hole_max_perct_intensity/100);
  valid_holes_by_intensity_idx = (hole_mean_intensity > min_intensity) & (hole_mean_intensity < max_intensity);
  
  % combine the area conditions with the intensity conditions
  % AND or OR the delete operation
  switch lower(fill_holes_bool_oper)
    case 'and'
      % keep holes with AND
      valid_holes = valid_holes_by_area_idx & valid_holes_by_intensity_idx;
      % mark the invalid holes for later deletion
      to_keep_holes_idx(~valid_holes) = 0;
    case 'or'
      % keep holes with OR
      valid_holes = valid_holes_by_area_idx | valid_holes_by_intensity_idx;
      % mark the invalid holes for later deletion
      to_keep_holes_idx(~valid_holes) = 0;
    otherwise
      error('Non-Supported boolean operator');
  end
end

% if a hole touches the edge of the image, we do not know its true size so it needs to be kept
image_edge_holes_idx = unique(nonzeros([B(1,:)'; B(end,:)'; B(:,1); B(:,end)]));

% add the edge holes back into the to keep list
to_keep_holes_idx(image_edge_holes_idx) = 1;

% remove all identified holes from the hole mask (B)
to_delete_holes_idx = ~to_keep_holes_idx;
to_delete_holes_idx = [0; to_delete_holes_idx];
B = to_delete_holes_idx(B+1)>0;
% fill those holes in to the logical segmented mask
S(B) = 1;






