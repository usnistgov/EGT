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



function EGT_Segmentation_GUI()

if ~isdeployed
  addpath([pwd filesep 'sub_functions']);
  addpath([pwd filesep 'imgs']);
end

%  Global Parameters
%-----------------------------------------------------------------------------------------
%-----------------------------------------------------------------------------------------

raw_images_path = [pwd filesep 'test' filesep];
raw_images_common_name = '';
raw_image_files = [];
nb_frames = 0;
current_frame_nb = 1;

morphological_operations = {'None','Dilate','Erode','Close','Open'};
greedy_range = 50;

grayscale_image = [];
foreground_mask = [];
I1 = [];

colormap_options = {'gray','jet','hsv','hot','cool'};
colormap_selected_option = colormap_options{1};

contour_color_options = {'Red', 'Green', 'Blue', 'Black', 'White'};
countour_color_selected_opt = contour_color_options{1};

% Figure setup
%-----------------------------------------------------------------------------------------
%-----------------------------------------------------------------------------------------
GUI_Name = 'EGT';

% if the GUI is already open, don't open another copy, bring the current copy to the front
open_fig_handle = findobj('type','figure','name',GUI_Name);
if ~isempty(open_fig_handle)
      figure(open_fig_handle);
      return;
%   close(open_fig_handle);
end

% Define General colors
lt_gray = [0.86,0.86,0.86];
dark_gray = [0.7,0.7,0.7];
green_blue = [0.0,0.3,0.4];


%   Get user screen size
SC = get(0, 'ScreenSize');
MaxMonitorX = SC(3);
MaxMonitorY = SC(4);

%   Set the figure window size values
main_tabFigScale = 0.5;          % Change this value to adjust the figure size
gui_ratio = 0.6;
gui_width = round(MaxMonitorX*main_tabFigScale);
gui_height = gui_width*gui_ratio;
% MaxWindowY = round(MaxMonitorY*main_tabFigScale);
% if MaxWindowX <= MaxWindowY, MaxWindowX = round(1.6*MaxWindowY); end
offset = 0;
if (SC(2) ~= 1)
  offset = abs(SC(2));
end
XPos = (MaxMonitorX-gui_width)/2 - offset;
YPos = (MaxMonitorY-gui_height)/2 + offset;


hctfig = figure(...
  'units', 'pixels',...
  'Position',[ XPos, YPos, gui_width, gui_height ],...
  'Name',GUI_Name,...
  'NumberTitle','off',...
  'CloseRequestFcn', @closeAllGuis);

% other guis (mitotic, border, seed
  function closeAllGuis(varargin)
    
    fh = findobj('type','figure','name','Save Images');
    close(fh);
    closereq();
    
  end


% Create main menu tab, 'Foreground Segmentation'
main_panel = uipanel('Units', 'normalized', 'Parent', hctfig,'Visible', 'on', 'Backgroundcolor', lt_gray,'BorderWidth',0,'Position', [0,0,1,1]);


%-----------------------------------------------------------------------------------------
%-----------------------------------------------------------------------------------------
% Empirical Gradient Threshold segmentation
%-----------------------------------------------------------------------------------------
%-----------------------------------------------------------------------------------------
foreground_min_object_size = 250;
foreground_display_contour = false;
foreground_display_raw_image = true;
foreground_display_labeled_image = false;
foreground_display_labeled_text = false;
foreground_strel_disk_radius = 2;
foreground_morph_operation = morphological_operations{2};
adjust_contrast_raw_image = 0;

fg_min_hole_size = 2*foreground_min_object_size;
fg_max_hole_size = Inf;
fg_hole_min_perct_intensity = 0;
fg_hole_max_perct_intensity = 100;


% options_panel = sub_panel(main_panel, [0.72,0.02,.27,.9], 'Options', 'lefttop', green_blue, lt_gray, 14, 'serif');
display_panel = sub_panel(main_panel, [0.01,0.02,.7,.955], ['Image: <' '>'], 'lefttop', green_blue, lt_gray, 0.04, 'serif');

TabLabels = {'Image Folders'; 'Params';};

% Number of tabs to be generated
NumberOfTabs = length(TabLabels);

h_tabpanel = zeros(NumberOfTabs,1);
h_tabpb = zeros(NumberOfTabs,1);

%-----------------------------------------------------------------------------------------
% Option Tabs
%-----------------------------------------------------------------------------------------
tab_label_text_size = 0.5;

% Create options menu tab, 'Image Folder'
h_tabpanel(1) = sub_panel(main_panel, [0.72,0.02,.27,.93], '', 'lefttop', green_blue, lt_gray, 14, 'serif');
h_tabpb(1) = push_button(main_panel, [.72 0.95 0.135 0.05], TabLabels(1), 'center', 'k', lt_gray, tab_label_text_size, 'serif', 'bold', 'on', {@first_tab_callback} );

% Create options menu tab, 'Params'
h_tabpanel(2) = sub_panel(main_panel, [0.72,0.02,.27,.93], '', 'lefttop', green_blue, lt_gray, 14, 'serif');
h_tabpb(2) = push_button(main_panel, [.855 0.95 0.135 0.05], TabLabels(2), 'center', 'k', dark_gray, tab_label_text_size, 'serif', 'bold', 'off', {@second_tab_callback} );
set(h_tabpanel(2), 'Visible', 'off');

% Create main menu tab, 'Help'
push_button(main_panel, [.575 0.95 0.135 0.05], 'Help', 'center', 'k', 'default', tab_label_text_size, 'serif', 'bold', 'on', {@Open_Help_callback} );

axes('Parent', h_tabpanel(1), 'Units', 'normalized', 'Position', [.05 0 .9 .25]);
axis image;
axis off

try
  imshow('NIST_Logo.tif');
catch err
  warning('unable to load and show NIST logo');
end


  function first_tab_callback(varargin)
    set(h_tabpb(1), 'Backgroundcolor', dark_gray);
    set(h_tabpb(2), 'Backgroundcolor', lt_gray);
    
    set(h_tabpanel(1), 'Visible', 'on');
    set(h_tabpanel(2), 'Visible', 'off');
    
  end

  function second_tab_callback(varargin)
    set(h_tabpb(1), 'Backgroundcolor', lt_gray);
    set(h_tabpb(2), 'Backgroundcolor', dark_gray);
    
    set(h_tabpanel(1), 'Visible', 'off');
    set(h_tabpanel(2), 'Visible', 'on');
    
  end

%-----------------------------------------------------------------------------------------
% Data Panel
%-----------------------------------------------------------------------------------------

component_height = .05;

label(h_tabpanel(1), [.01 .91 .99 component_height], 'Raw Images Path:', 'left', 'k', lt_gray, .6, 'sans serif', 'normal');
input_dir_editbox = editbox(h_tabpanel(1), [.01 .87 .98 component_height], raw_images_path, 'left', 'k', 'w', .6, 'normal');
push_button(h_tabpanel(1), [.5 .815 .485 component_height], 'Browse', 'center', 'k', 'default', 0.5, 'sans serif', 'bold', 'on',  {@choose_raw_images_callback} );

label(h_tabpanel(1), [.01 .73 .95 .05], 'Raw Common Name:', 'left', 'k', lt_gray, .6, 'sans serif', 'normal');
common_name_editbox = editbox(h_tabpanel(1), [.01 .69 .98 component_height], '', 'left', 'k', 'w', .6, 'normal');

  function choose_raw_images_callback(varargin)
    % get directory
    sdir = uigetdir(pwd,'Select Image(s)');
    if sdir ~= 0
      try
        raw_images_path = validate_filepath(sdir);
      catch err
        if (strcmp(err.identifier,'validate_filepath:notFoundInPath')) || ...
            (strcmp(err.identifier,'validate_filepath:argChk'))
          errordlg('Invalid directory selected');
          return;
        else
          rethrow(err);
        end
      end
      set(input_dir_editbox, 'String', raw_images_path);
    end
  end


push_button(h_tabpanel(1), [.1 .5 .8 1.5*component_height], 'Load Images', 'center', 'k', dark_gray, 0.6, 'sans serif', 'bold', 'on',  {@initImages} );



%-----------------------------------------------------------------------------------------
% Params Panel
%-----------------------------------------------------------------------------------------

fg_options_panel = h_tabpanel(2);
y = .94;
label(fg_options_panel, [.05 y .45 component_height], 'Min Object Area', 'left', 'k', lt_gray, .6, 'sans serif', 'normal');
fg_min_object_size_edit = editbox_check(fg_options_panel, [.5 y .3 component_height], num2str(foreground_min_object_size), 'left', 'k', 'w', .6, 'normal', @fg_min_object_size_Callback);
label(fg_options_panel, [.82 y .1 component_height], 'px', 'left', 'k', lt_gray, .6, 'sans serif', 'normal');

  function bool = fg_min_object_size_Callback(varargin)
    bool = false;
    temp = str2double(get(fg_min_object_size_edit, 'String'));
    if isnan(temp) || temp < 0
      errordlg('Invalid Min Object Size');
      return;
    end
    foreground_min_object_size = temp;
    bool = true;
  end


% Fill holes sub-panel
y = 0.72;
fg_fill_holes_panel = sub_panel(fg_options_panel, [.02 y .96 .22], 'Keep Holes with', 'lefttop', green_blue, lt_gray, 0.16, 'serif');
% set(display_panel, 'Title', 'Keep Holes with');
fill_hole_subpanel_height = 0.25;
y = .7;
fg_min_hole_size_edit = editbox_check(fg_fill_holes_panel, [.03 y .19 fill_hole_subpanel_height], num2str(fg_min_hole_size), 'left', 'k', 'w', .6, 'normal', @fg_min_hole_size_Callback);
label(fg_fill_holes_panel, [.22 y .54 fill_hole_subpanel_height], '< size (pixels) <', 'center', 'k', lt_gray, .7, 'sans serif', 'normal');
fg_max_hole_size_edit = editbox_check(fg_fill_holes_panel, [.78 y .19 fill_hole_subpanel_height], num2str(fg_max_hole_size), 'left', 'k', 'w', .6, 'normal', @fg_max_hole_size_Callback);

y = .4;
fill_holes_options = {'AND','OR'};
fg_hole_fill_pop = popupmenu(fg_fill_holes_panel, [0.39 y 0.22 0.22], fill_holes_options, 'k', 'w', .6, 'normal',[]);

y = .05;
fg_hole_min_perct_intensity_edit = editbox_check(fg_fill_holes_panel, [.03 y .19 fill_hole_subpanel_height], num2str(fg_hole_min_perct_intensity), 'left', 'k', 'w', .6, 'normal', @fg_hole_min_perct_intensity_Callback);
label(fg_fill_holes_panel, [.22 y .54 fill_hole_subpanel_height], '< intensity (%) <', 'center', 'k', lt_gray, .7, 'sans serif', 'normal');
fg_hole_max_perct_intensity_edit = editbox_check(fg_fill_holes_panel, [.78 y .19 fill_hole_subpanel_height], num2str(fg_hole_max_perct_intensity), 'left', 'k', 'w', .6, 'normal', @fg_hole_max_perct_intensity_Callback);

  function bool = fg_min_hole_size_Callback(varargin)
    bool = false;
    temp = str2double(get(fg_min_hole_size_edit, 'String'));
    if isnan(temp) || temp < 0
      errordlg('Invalid Min Hole Size');
      return;
    end
    fg_min_hole_size = temp;
    bool = true;
  end

  function bool = fg_max_hole_size_Callback(varargin)
    bool = false;
    temp = str2double(get(fg_max_hole_size_edit, 'String'));
    if isnan(temp) || temp < 0
      errordlg('Invalid Max Hole Size');
      return;
    end
    fg_max_hole_size = temp;
    bool = true;
  end

  function bool = fg_hole_min_perct_intensity_Callback(varargin)
    bool = false;
    temp = str2double(get(fg_hole_min_perct_intensity_edit, 'String'));
    if isnan(temp) || temp < 0 || temp > 100
      errordlg('Invalid Percentile Intensity threshold');
      return;
    end
    fg_hole_min_perct_intensity = temp;
    bool = true;
  end

  function bool = fg_hole_max_perct_intensity_Callback(varargin)
    bool = false;
    temp = str2double(get(fg_hole_max_perct_intensity_edit, 'String'));
    if isnan(temp) || temp < 0 || temp > 100
      errordlg('Invalid Min Hole Size');
      return;
    end
    fg_hole_max_perct_intensity = temp;
    bool = true;
  end

y = .65;
label(fg_options_panel, [.05 y .95 component_height], 'Morphological Operation', 'left', 'k', lt_gray, .6, 'sans serif', 'normal');
fg_morph_dropdown = popupmenu(fg_options_panel, [.05 y-.04 .38 component_height], morphological_operations, 'k', 'w', .6, 'normal', @fg_morph_Callback);
label(fg_options_panel, [.43 y-.045 .34 component_height], 'with radius:', 'center', 'k', lt_gray, .6, 'sans serif', 'normal');
fg_strel_radius_edit = editbox_check(fg_options_panel, [.77 y-.04 .18 component_height], num2str(foreground_strel_disk_radius), 'right', 'k', 'w', .6, 'normal', @fg_strel_radius_Callback);

  function fg_morph_Callback(varargin)
    temp = get(fg_morph_dropdown, 'value');
    foreground_morph_operation = morphological_operations{temp};
  end

  function bool = fg_strel_radius_Callback(varargin)
    bool = false;
    temp = round(str2double(get(fg_strel_radius_edit, 'string')));
    if temp < 0
      errordlg('Invalid strel radius');
      return;
    end
    foreground_strel_disk_radius = temp;
    bool = true;
  end


y = .54;
label(fg_options_panel, [.05 y .95 component_height], 'Greedy', 'left', 'k', lt_gray, .6, 'sans serif', 'normal');

% Create Slider for img display
fg_greedy_slider_num = 0;
fg_greedy_edit = uicontrol('style','slider',...
  'Parent',fg_options_panel,...
  'unit','normalized',...
  'Min',-greedy_range,'Max',greedy_range,'Value',fg_greedy_slider_num, ...
  'position',[.05 y-.04 .8 component_height],...
  'SliderStep', [1, 1]/(greedy_range - -greedy_range), ...  % Map SliderStep to whole number, Actual step = SliderStep * (Max slider value - Min slider value)
  'callback',{@fgGreedySliderCallback});

fg_slider_num_label = label(fg_options_panel, [.875 y-.04 .1 component_height], fg_greedy_slider_num, 'center', 'k', lt_gray, .6, 'sans serif', 'normal');

  function fgGreedySliderCallback(varargin)
    fg_greedy_slider_num = ceil(get(fg_greedy_edit, 'value'));
    set(fg_slider_num_label, 'String', num2str(fg_greedy_slider_num));
  end



push_button(h_tabpanel(2), [.1 .41 .78 1.2*component_height], 'Update Preview', 'center', 'k', dark_gray, 0.5, 'sans serif', 'bold', 'on', {@Foreground_Display_update_image});


label(h_tabpanel(2), [.05 .34 .95 component_height], 'ColorMap:', 'left', 'k', lt_gray, .6, 'sans serif', 'normal');
OS_Options_colormap_dropdown = popupmenu(h_tabpanel(2), [.05 .3 .91 component_height], colormap_options, 'k', 'w', .6, 'normal', @OS_Options_colormap_Callback);
  function OS_Options_colormap_Callback(varargin)
    temp = get(OS_Options_colormap_dropdown, 'value');
    colormap_selected_option = colormap_options(temp);
    colormap_selected_option = colormap_selected_option{1};
    update_display_image();
  end



Foreground_Display_labeled_image_checkbox = checkbox(h_tabpanel(2), [.05 .24 .45 component_height], 'Label Image', 'center', 'k', lt_gray, .6, 'sans serif', 'normal', {@Foreground_Display_labeled_image_checkbox_Callback});
  function Foreground_Display_labeled_image_checkbox_Callback(varargin)
    foreground_display_labeled_image = logical(get(Foreground_Display_labeled_image_checkbox, 'value'));
    if foreground_display_labeled_image
      set(Foreground_Display_labeled_text_checkbox, 'enable', 'on');
      set(contour_color_dropdown, 'enable', 'off');
    else
      set(Foreground_Display_labeled_text_checkbox, 'enable', 'off');
      foreground_display_labeled_text = false;
      set(Foreground_Display_labeled_text_checkbox, 'value', foreground_display_labeled_text);
      set(contour_color_dropdown, 'enable', 'on');
    end
    update_label_image();
  end

Foreground_Display_labeled_text_checkbox = checkbox(h_tabpanel(2), [.53 .24 .45 component_height], 'Show Labels', 'center', 'k', lt_gray, .6, 'sans serif', 'normal', {@Foreground_Display_labeled_text_checkbox_Callback});
  function Foreground_Display_labeled_text_checkbox_Callback(varargin)
    foreground_display_labeled_text = logical(get(Foreground_Display_labeled_text_checkbox, 'value'));
    update_display_image();
  end
set(Foreground_Display_labeled_text_checkbox, 'enable', 'off');




Foreground_Display_contour_checkbox = checkbox(h_tabpanel(2), [.05 .19 .54 component_height], 'Display Contour', 'center', 'k', lt_gray, .6, 'sans serif', 'normal', {@Foreground_Display_contour_checkbox_Callback});
  function Foreground_Display_contour_checkbox_Callback(varargin)
    foreground_display_contour = logical(get(Foreground_Display_contour_checkbox, 'value'));
    if(nb_frames <= 0)
      return;
    else
      update_display_image
    end
    
  end

contour_color_dropdown = popupmenu(h_tabpanel(2), [.659 .19 .3 component_height], contour_color_options, 'k', 'w', .6, 'normal', @contour_color_callback);
  function contour_color_callback(varargin)
    temp1 = get(contour_color_dropdown, 'value');
    countour_color_selected_opt = contour_color_options(temp1);
    update_display_image
  end

Foreground_Display_raw_image_checkbox = checkbox(h_tabpanel(2), [.05 .14 .65 component_height], 'Display Raw Image', 'center', 'k', lt_gray, .6, 'sans serif', 'normal', {@Foreground_Display_raw_image_checkbox_Callback});
set(Foreground_Display_raw_image_checkbox, 'value',foreground_display_raw_image);
  function Foreground_Display_raw_image_checkbox_Callback(varargin)
    foreground_display_raw_image = logical(get(Foreground_Display_raw_image_checkbox, 'value'));
    if(nb_frames <= 0)
      return;
    else
      update_display_image
    end    
  end


Adjust_Contrast_raw_image_checkbox = checkbox(h_tabpanel(2), [.05 .09 .65 component_height], 'Adjust Contrast', 'center', 'k', lt_gray, .6, 'sans serif', 'normal', {@Adjust_Contrast_raw_image_checkbox_Callback});
  function Adjust_Contrast_raw_image_checkbox_Callback(varargin)
    adjust_contrast_raw_image = logical(get(Adjust_Contrast_raw_image_checkbox, 'value'));
    if(nb_frames <= 0)
      return;
    else
      update_display_image
    end
    
  end

push_button(h_tabpanel(2), [.1 .01 .78 1.2*component_height], 'Save Segmented Images', 'center', 'k', dark_gray, 0.5, 'sans serif', 'bold', 'on', {@save_images_GUI_callback});

% Save images popup GUI
% -------------------------------------------------------------------------------------
  function save_images_GUI_callback(varargin)
    % Create figure, if found return, this prevents opening multiples of the same figure
    % Create figure in case not found
    save_fig = findobj('type','figure','name','Save Images');
    if ~isempty(save_fig)
      figure(save_fig)
    else
      save_fig = figure(...
        'units', 'pixels',...
        'Position', [ (MaxMonitorX-gui_width*0.3)/2 - offset, (MaxMonitorY-gui_height*0.5)/2 + offset, gui_width*0.3, gui_height*0.5 ], ...
        'Name','Save Images',...
        'NumberTitle','off',...
        'Menubar','none',...
        'Toolbar','none',...
        'Resize', 'on');
    end
    
    save_image_format_opts = {'Tiff','PNG','JPG'};
    save_image_format = save_image_format_opts{1};
    save_common_name = 'segmented_';
    save_range = 'All';
    
    type_format_opts = {'Binary Mask', 'Labeled Mask', 'As Shown in Preview'};
    type_format = type_format_opts{1};
    
    
    
    content_panel = sub_panel(save_fig, [0 0 1 1], '', 'lefttop', green_blue, lt_gray, 14, 'serif');
    
    label(content_panel, [.03 .86 .2 .09], 'Format:', 'right', 'k', lt_gray, .6, 'sans serif', 'normal');
    format_edit_dropdown = popupmenu(content_panel, [.27 .87 .7 .09], save_image_format_opts, 'k', 'w', .6, 'normal', {@format_callback});
    label(content_panel, [.27 .77 .7 .09], 'Binary mask saved as Tiff only', 'left', 'k', lt_gray, .45, 'sans serif', 'normal');
    set(format_edit_dropdown, 'value',1);
    
    function format_callback(varargin)
      temp = get(format_edit_dropdown, 'value');
      save_image_format = save_image_format_opts{temp};
    end
    
    label(content_panel, [.03 .68 .2 .09], 'Name:', 'right', 'k', lt_gray, .6, 'sans serif', 'normal');
    save_common_name_edit = editbox(content_panel, [.27 .69 .7 .09], save_common_name, 'left', 'k', 'w', .6, 'normal');
    
    label(content_panel, [.03 .57 .2 .09], 'Range:', 'right', 'k', lt_gray, .6, 'sans serif', 'normal');
    save_range_edit = editbox(content_panel, [.27 .58 .7 .09], save_range, 'left', 'k', 'w', .6, 'normal');
    label(content_panel, [.27 .49 .7 .09], 'i.e. - All or subset 1,2,3:7,12', 'left', 'k', lt_gray, .45, 'sans serif', 'normal');
    
    label(content_panel, [.03 .35 .2 .09], 'Type:', 'right', 'k', lt_gray, .6, 'sans serif', 'normal');
    type_edit_dropdown = popupmenu(content_panel, [.27 .36 .7 .09], type_format_opts, 'k', 'w', .6, 'normal', {@type_callback});
    set(type_edit_dropdown, 'value', 1);
    
    function type_callback(varargin)
      temp1 = get(type_edit_dropdown, 'value');
      type_format = type_format_opts{temp1};
    end
    
    
    push_button(content_panel, [.01 .01 .49 .09], 'Save', 'center', 'k', 'default', 0.5, 'sans serif', 'bold', 'on', {@save_callback});
    push_button(content_panel, [.5 .01 .49 .09], 'Cancel', 'center', 'k', 'default', 0.5, 'sans serif', 'bold', 'on', {@cancel_save_callback});
    
    function save_callback(varargin)
      save_common_name = get(save_common_name_edit, 'String');
      save_range = get(save_range_edit, 'String');
      
      save_images(save_image_format, save_common_name, save_range, type_format);
      if ishandle(save_fig), close(save_fig); end
    end
    
    
    function cancel_save_callback(varargin)
      if ishandle(save_fig), close(save_fig); end
    end
    
    
  end


% Format = tif, png, or jpg
% Name = common name of images
% Range = All or 1,2,3:7,9
% Type = Binary mask or As shown in Preview

  function save_images(format, name, range, type, varargin)
    % directory
    sdir = uigetdir(pwd,'Select Saved Path');
    if sdir ~= 0
      try
        h = msgbox('Working...');
        nb_frames_temp = nb_frames;
        
        save_images_path = validate_filepath(sdir);
        
        print_to_command(['Saving Images to: ' save_images_path]);
        
        switch(format)
          case 'PNG'
            formatted_format = '.png';
          case 'JPG'
            formatted_format = '.jpg';
          otherwise
            formatted_format = '.tif';
        end
        
        if(strcmp(range, 'All'))
          range = '0';
        end
        
        frames_to_save = str2num(range); %#ok<ST2NM>
        % truncate frames_to_save
        frames_to_save( frames_to_save > nb_frames_temp) = [];
        if ~isempty(frames_to_save) && any(frames_to_save > 0)
          nb_frames_temp = numel(frames_to_save);
        else
          frames_to_save = 1:nb_frames_temp;
          nb_frames_temp = numel(frames_to_save);
        end
        
        zero_pad = num2str(length(num2str(nb_frames_temp)));
        
        % log parameters
        fh = fopen([save_images_path filesep 'parameters.log'],'w');
        fprintf(fh,'Empirical Gradient Thresholding GUI Run on %s\n\n', datestr(clock));
        fprintf(fh,'Raw Images Path:\n');
        fprintf(fh,'%s\n',raw_images_path);
        fprintf(fh,'Common Image Name:\n');
        fprintf(fh,'%s\n',raw_images_common_name);
        fprintf(fh,'Min Cell Area: %d\n',foreground_min_object_size);
        fprintf(fh,'Keep Holes with: \n');
        fprintf(fh,'    %g < size(pixels) < %g\n',fg_min_hole_size, fg_max_hole_size);
        fill_holes_bool_oper = fill_holes_options{get(fg_hole_fill_pop, 'value')};
        fprintf(fh,'    %s\n', fill_holes_bool_oper);
        percent_sign = '%';
        fprintf(fh,'    %g < intensity(%s) < %g\n',fg_hole_min_perct_intensity, percent_sign, fg_hole_max_perct_intensity);
        fprintf(fh,'Morphological Operation: %s with radius %d\n',foreground_morph_operation, foreground_strel_disk_radius);
        fprintf(fh,'Greedy: %d\n',fg_greedy_slider_num);
        fclose(fh);
        
        
        print_update(1, 1, nb_frames_temp);
        for i = 1:nb_frames_temp
          
          print_update(2,i,nb_frames_temp);
          
          raw_image = imread([raw_images_path raw_image_files(i).name]);
          
          I1 = double(raw_image);
          fill_holes_bool_oper = fill_holes_options{get(fg_hole_fill_pop, 'value')};
          BW = EGT_Segmentation(I1,foreground_min_object_size,fg_min_hole_size, fg_max_hole_size, ...
            fg_hole_min_perct_intensity, fg_hole_max_perct_intensity, fill_holes_bool_oper, fg_greedy_slider_num);
          
          
          foreground_morph_operation = lower(regexprep(foreground_morph_operation, '\W', ''));
          BW = morphOp(I1, BW, foreground_morph_operation, foreground_strel_disk_radius);
          
          BW = fill_holes(BW, I1, fg_min_hole_size, fg_max_hole_size, fg_hole_min_perct_intensity, fg_hole_max_perct_intensity, fill_holes_bool_oper);
          BW = bwareaopen(BW ,foreground_min_object_size,8);
          
          
          foreground_mask = BW;
          if foreground_display_labeled_image
            [foreground_mask, nb_objects] = bwlabel(foreground_mask);
          else
            foreground_mask = foreground_mask>0;
            nb_objects = 1;
          end
          
          switch type
            case  'Binary Mask'
              imwrite(uint8(logical(foreground_mask)), [save_images_path filesep name sprintf(['%0' zero_pad 'd'],i) '.tif']);
            case 'Labeled Mask'
              imwrite(uint16(bwlabel(foreground_mask)), [save_images_path filesep name sprintf(['%0' zero_pad 'd'],i) '.tif']);
            otherwise % as shown in preview
              image = superimpose_colormap_contour(I1, foreground_mask, colormap([colormap_selected_option '(65000)']), countour_color_selected_opt, foreground_display_raw_image, foreground_display_contour, adjust_contrast_raw_image);
              imwrite(image, [save_images_path filesep name sprintf(['%0' zero_pad 'd'],i) formatted_format]);
          end
        end
        
        
        %             upper_hole_size_bound = foreground_min_hole_size;
        %             zero_pad = num2str(length(num2str(nb_frames)));
        
        %             disp(save_images_path);
        %             disp(format);
        %             disp(name);
        %             disp(range);
        %             disp(type);
        
        
        if ishandle(h), close(h); end
        
      catch err
        if (strcmp(err.identifier,'validate_filepath:notFoundInPath')) || ...
            (strcmp(err.identifier,'validate_filepath:argChk'))
          errordlg('Invalid directory selected');
          return;
        else
          rethrow(err);
        end
      end
    end
    
  end



% ---------------------------------------------------------------------------------------
% Display Panel

% setup the display panel for the foreground tab

  function bool = Foreground_Options_validate(varargin)
    bool = false;
    fg_morph_Callback();
    fg_strel_radius_Callback();
    
    if ~fg_min_hole_size_Callback(), return, end
    if ~fg_max_hole_size_Callback(), return, end
    if ~fg_hole_min_perct_intensity_Callback(), return, end
    if ~fg_hole_max_perct_intensity_Callback(), return, end
    
    bool = true;
    
  end

num_objects_label = [];
  function initImagePanel(varargin)
    
    
    if nb_frames == 1
      slider_step = [0, 0];
    else
      slider_step = [1, 1]/(nb_frames - 1);
    end
    % Create Slider for image display
    image_slider_edit = uicontrol('style','slider',...
      'Parent',display_panel,...
      'unit','normalized',...
      'Min',1,'Max',nb_frames,'Value',1, ...
      'position',[.01 0.01 0.6 0.05],...
      'SliderStep', slider_step, ...  % Map SliderStep to whole number, Actual step = SliderStep * (Max slider value - Min slider value)
      'callback',{@imgSliderCallback});
    
    % Edit: Cell Numbers to show
    goto_user_frame_edit = uicontrol('style','Edit',...
      'Parent',display_panel,...
      'unit','normalized',...
      'position',[.63 0.01 0.1 0.05],...
      'HorizontalAlignment','center',...
      'String','1',...
      'FontUnits', 'normalized',...
      'fontsize',.5,...
      'fontweight','normal',...
      'backgroundcolor', 'w',...
      'callback',{@gotoFrameCallback});
    
    % # of frames label
    uicontrol('style','text',...
      'Parent',display_panel,...
      'unit','normalized',...
      'position',[.74 .005 .09 .05],...
      'HorizontalAlignment','left',...
      'String',['of ' num2str(nb_frames)],...
      'FontUnits', 'normalized',...
      'fontsize',.6,...
      'backgroundcolor', lt_gray,...
      'fontweight','normal');
    
    num_objects_label = label(display_panel, [.85 .005 .14 .05], [num2str(nb_objects) ' objects'], 'center', 'k', lt_gray, .6, 'sans serif', 'normal');
    
    %         % Pushbutton: Goto Frame
    %         uicontrol('style','push',...
    %             'Parent',display_panel,...
    %             'unit','normalized',...
    %             'position',[.89 0.01 0.1 0.05],...
    %             'HorizontalAlignment','right',...
    %             'String','Go',...
    %             'FontUnits', 'normalized',...
    %             'fontsize',.5,...
    %             'fontweight','normal',...
    %             'callback',{@gotoFrameCallback});
    
    function imgSliderCallback(varargin)
      if nb_frames > 1
        current_frame_nb = ceil(get(image_slider_edit, 'value'));
        set(goto_user_frame_edit, 'String', num2str(current_frame_nb));
        Foreground_Display_update_image();
      end
    end
    
    function gotoFrameCallback(varargin)
      new_frame_nb = str2double(get(goto_user_frame_edit, 'String'));
      if isnan(new_frame_nb)
        errordlg('Invalid frame, please input a valid number.');
        set(goto_user_frame_edit, 'String', num2str(current_frame_nb));
        return;
      end
      
      % constrain the new frame number to the existing frame numbers
      new_frame_nb = min(new_frame_nb, nb_frames);
      new_frame_nb = max(1, new_frame_nb);
      
      current_frame_nb = new_frame_nb;
      set(goto_user_frame_edit, 'string', num2str(current_frame_nb));
      set(image_slider_edit, 'value', current_frame_nb);
      update_display_image();
    end
    
    
  end


Foreground_Display_Superimpose_Axis = axes('Parent', display_panel, 'Units','normalized', 'Position', [.001 .1 .999 .90]);
%set(Foreground_Display_Superimpose_Axis,'nextplot','replacechildren');
axis off; axis image;
colors_vector = 0; nb_objects = 1; text_location = 0;

  function Foreground_Display_update_image(varargin)
    if ~Foreground_Options_validate(), return, end
    
    % Read corresponding images
    grayscale_image = imread([raw_images_path raw_image_files(current_frame_nb).name]);
    I1 = grayscale_image;
    set(display_panel, 'Title', ['Image: <' raw_image_files(current_frame_nb).name '>']);
    
    
    % TODO update this
    fill_holes_bool_oper = fill_holes_options{get(fg_hole_fill_pop, 'value')};
    BW = EGT_Segmentation(grayscale_image,foreground_min_object_size,fg_min_hole_size, fg_max_hole_size, ...
      fg_hole_min_perct_intensity, fg_hole_max_perct_intensity, fill_holes_bool_oper, fg_greedy_slider_num);
    
    
    foreground_morph_operation = lower(regexprep(foreground_morph_operation, '\W', ''));
    BW = morphOp(grayscale_image, BW, foreground_morph_operation, foreground_strel_disk_radius);
    
    BW = fill_holes(BW, grayscale_image, fg_min_hole_size, fg_max_hole_size, fg_hole_min_perct_intensity, fg_hole_max_perct_intensity, fill_holes_bool_oper);
    BW = bwareaopen(BW ,foreground_min_object_size,8);
    
    foreground_mask = BW;
    %
    %         foreground_morph_operation = lower(regexprep(foreground_morph_operation, '\W', ''));
    %         foreground_mask = morphOp(grayscale_image, foreground_mask, foreground_morph_operation, foreground_strel_disk_radius);
    
    delete(get(Foreground_Display_Superimpose_Axis, 'Children'));
    [disp_I, colors_vector] = superimpose_colormap_contour(grayscale_image, foreground_mask, colormap([colormap_selected_option '(65000)']), countour_color_selected_opt, foreground_display_raw_image, foreground_display_contour, adjust_contrast_raw_image, colors_vector);
    imshow(disp_I, 'Parent', Foreground_Display_Superimpose_Axis);
    
    if foreground_display_labeled_image
      update_label_image();
    end
    
  end

  function update_label_image(varargin)
    
    if foreground_display_labeled_image
      [foreground_mask, nb_objects] = bwlabel(foreground_mask);
    else
      foreground_mask = foreground_mask>0;
      nb_objects = 1;
    end
    delete(get(Foreground_Display_Superimpose_Axis, 'Children'));
    [disp_I, colors_vector] = superimpose_colormap_contour(I1, foreground_mask, colormap([colormap_selected_option '(65000)']), countour_color_selected_opt, foreground_display_raw_image, foreground_display_contour, adjust_contrast_raw_image);
    imshow(disp_I, 'Parent', Foreground_Display_Superimpose_Axis);
    
    [~, text_location] = find_edges_labeled(foreground_mask, nb_objects);
    % Place the number of the cell in the image
    if foreground_display_labeled_image && foreground_display_labeled_text
      hold on,
      for i = 1:nb_objects
        cell_number = foreground_mask(text_location(i,2), text_location(i,1));
        
        text(text_location(i,1), text_location(i,2), num2str(cell_number), 'fontsize', 8, ...
          'FontWeight', 'bold', 'Margin', .1, 'color', 'k', 'BackgroundColor', 'w')
      end
    end
    
    set(num_objects_label, 'String', [num2str(nb_objects) ' objects']);
    
    set(Foreground_Display_Superimpose_Axis,'nextplot','replacechildren'); % maintains zoom when clicking through slider
    
  end

  function update_display_image(varargin)
    delete(get(Foreground_Display_Superimpose_Axis, 'Children'));
    [disp_I, colors_vector] = superimpose_colormap_contour(I1, foreground_mask, colormap([colormap_selected_option '(65000)']), countour_color_selected_opt, foreground_display_raw_image, foreground_display_contour, adjust_contrast_raw_image, colors_vector);
    imshow(disp_I, 'Parent', Foreground_Display_Superimpose_Axis);
    if foreground_display_labeled_image && foreground_display_labeled_text
      hold on,
      for i = 1:nb_objects
        cell_number = foreground_mask(text_location(i,2), text_location(i,1));
        
        text(text_location(i,1), text_location(i,2), num2str(cell_number), 'fontsize', 8, ...
          'FontWeight', 'bold', 'Margin', .1, 'color', 'k', 'BackgroundColor', 'w')
      end
    end
    
    set(Foreground_Display_Superimpose_Axis,'nextplot','replacechildren'); % maintains zoom when clicking through slider
  end

  function initImages(varargin)
    
    % get path and common name info from gui
    raw_images_path = get(input_dir_editbox, 'string');
    if raw_images_path(end) ~= filesep
      raw_images_path = [raw_images_path filesep];
    end
    raw_images_common_name = get(common_name_editbox, 'string');
    
    raw_image_files = dir([raw_images_path '*' raw_images_common_name '*.tif']);
    nb_frames = length(raw_image_files);
    if nb_frames <= 0
      errordlg('Chosen image folder doesn''t contain any .tif images.');
      return;
    end
    
    
    
    % Get first image to check its size
    image = imread([raw_images_path raw_image_files(1).name]);
    
    % if image is very large, send the user a warning
    if numel(image) > 10^7
      
      response = questdlg('Images are large! Visualization might be slow, Continue?', ...
        'Notice','Yes','Cancel','Cancel');
      % Handle response
      switch response
        case 'Yes'
          %                     continue displaying images
        case 'Cancel'
          % if the user did not select yes for continue, abort visualization
          return;
      end
      
    end
    
    
    initImagePanel
    Foreground_Display_update_image
    
    
    set(h_tabpb(2), 'enable', 'on');
    second_tab_callback
  end

end



% Other Functions
%-----------------------------------------------------------------------------------------
%-----------------------------------------------------------------------------------------

function Open_Help_callback(varargin)
winopen('Edge_Detection_Readme.txt');
end




% % UI Control Wrappers
function edit_return = editbox(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontweight, varargin)
edit_return = uicontrol('style','edit',...
  'parent',parent_handle,...
  'unit','normalized',...
  'fontunits', 'normalized',...
  'position',position,...
  'horizontalalignment',horz_align,...
  'string',string,...
  'foregroundcolor',color,...
  'backgroundcolor',bgcolor,...
  'fontsize',fontsize,...
  'fontweight',fontweight);
end

function edit_return = editbox_check(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontweight, callback, varargin)
edit_return = uicontrol('style','edit',...
  'parent',parent_handle,...
  'unit','normalized',...
  'fontunits', 'normalized',...
  'position',position,...
  'horizontalalignment',horz_align,...
  'string',string,...
  'foregroundcolor',color,...
  'backgroundcolor',bgcolor,...
  'fontsize',fontsize,...
  'fontweight',fontweight,...
  'callback', callback);
end

function label_return = label(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontname, fontweight, varargin)
label_return = uicontrol('style','text',...
  'parent',parent_handle,...
  'unit','normalized',...
  'fontunits','normalized',...
  'position',position,...
  'horizontalalignment',horz_align,...
  'string',string,...
  'foregroundcolor',color,...
  'backgroundcolor',bgcolor,...
  'fontsize',fontsize,...
  'fontname', fontname,...
  'fontweight',fontweight);
end

function pop_return = popupmenu(parent_handle, position, string_array, color, bgcolor, fontsize, fontweight, callback, varargin)
pop_return = uicontrol('style','popupmenu',...
  'parent',parent_handle,...
  'unit','normalized',...
  'fontunits', 'normalized',...
  'position',position,...
  'string',string_array,...
  'foregroundcolor',color,...
  'backgroundcolor',bgcolor,...
  'fontsize',fontsize,...
  'fontweight',fontweight,...
  'callback',callback);
end

function button_return = push_button(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontname, fontweight, on_off, callback, varargin)
button_return = uicontrol('style','pushbutton',...
  'parent',parent_handle,...
  'unit','normalized',...
  'fontunits','normalized',...
  'position',position,...
  'horizontalalignment',horz_align,...
  'foregroundcolor',color,...
  'backgroundcolor',bgcolor,...
  'string',string,...
  'fontsize',fontsize,...
  'fontname', fontname,...
  'fontweight',fontweight,...
  'enable', on_off,...
  'callback',callback);
end


function check_return = checkbox(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontname, fontweight, callback, varargin)
check_return = uicontrol('style','checkbox',...
  'Parent',parent_handle,...
  'unit','normalized',...
  'fontunits', 'normalized',...
  'position',position,...
  'horizontalalignment',horz_align,...
  'string',string,...
  'foregroundcolor',color,...
  'backgroundcolor',bgcolor,...
  'fontsize', fontsize,...
  'fontname', fontname,...
  'fontweight',fontweight,...
  'callback', callback);
end

% UI Panels
function panel_return = sub_panel(parent_handle, position, title, title_align, color, bgcolor, fontsize, fontname, varargin)
panel_return = uipanel('parent', parent_handle,...
  'units', 'normalized',...
  'position',position,...
  'title',title,...
  'titleposition',title_align,...
  'foregroundcolor',color,...
  'backgroundcolor',bgcolor,...
  'fontname', fontname,...
  'fontunits','normalized',...
  'fontsize',fontsize,...
  'fontweight', 'bold',...
  'visible', 'on',...
  'borderwidth',1);
end


function BW = morphOp(I, BW, op_str, radius, border_mask)
if nargin == 5
  use_border_flag = true;
else
  use_border_flag = false;
  border_mask = [];
end
border_mask = logical(border_mask);


op_str = lower(regexprep(op_str, '\W', ''));
switch op_str
  case 'dilate'
    if use_border_flag
      BW = geodesic_imdilate(BW, ~border_mask, strel('disk', radius), radius);
    else
      BW = imdilate(BW, strel('disk', radius));
    end
  case 'erode'
    BW = imerode(BW, strel('disk', radius));
  case 'close'
    if use_border_flag
      BW = geodesic_imclose(BW, ~border_mask, strel('disk', radius), radius);
    else
      BW = imclose(BW, strel('disk', radius));
    end
  case 'open'
    if use_border_flag
      BW = geodesic_imopen(BW, ~border_mask, strel('disk', radius), radius);
    else
      BW = imopen(BW, strel('disk', radius));
    end
    
  case 'iterativegraydilate'
    factorNb = 0.5;
    se = strel('disk', 1);
    
    if ~use_border_flag
      border_mask = true(size(BW));
    end
    BW = logical(BW);
    
    for i = 1:floor((1/factorNb)*radius)
      BWd = imdilate(BW>0, se);
      if use_border_flag
        BWd = BWd & ~border_mask;
      end
      [BWd, nb_obj] = bwlabel(BWd);
      for k = 1:nb_obj
        idx = find(BWd == k);
        % remove non border pixels
        old_idx = BW(idx);
        idx(old_idx) = [];
        
        % decide the pixels to keep
        vals = I(idx);
        [~, locs] = sort(vals, 'ascend');
        locs2 = locs(1:round(numel(locs)*factorNb));
        idx2 = idx(locs2);
        BW(idx2) = 1;
        
      end
    end
    BW = logical(BW);
end

end



function BW = geodesic_imdilate(BW, mask, se, radius)

BW = logical(BW);
BW = BW & mask;
BWmask = imdilate(BW, se);
BWmask = BWmask & mask;

BW1 = bwdistgeodesic(BWmask, BW);

BW = BW1 <= radius;

end

function BW = geodesic_imopen(BW, mask, se, radius)

BW = imerode(BW, se);
BW = geodesic_imdilate(BW,mask,se,radius);

end

function BW = geodesic_imclose(BW, mask, se, radius)

BW = geodesic_imdilate(BW,mask,se,radius);
BW = imerode(BW, se);

end




