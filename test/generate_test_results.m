% Test Results Generation


I = imread('phase_image_001.tif');
fill_operators = {'AND','OR'};

for min_cell_size = 100:100:200
  for min_hole_size = 10:10:20
    for max_hole_size = 50:10:60
      for hole_min_perct_intensity = 0:5:10
        for hole_max_perct_intensity = 90:5:100
          for fill_idx = 1:2
            for manual_finetune = -10:5:10
              S = EGT_Segmentation(I, min_cell_size, min_hole_size, max_hole_size, hole_min_perct_intensity, hole_max_perct_intensity, fill_operators{fill_idx}, manual_finetune);

              imwrite(uint8(255*uint8(S)), sprintf('./eval/%d_%d_%d_%d_%d_%s_%d.tif',min_cell_size, min_hole_size, max_hole_size, hole_min_perct_intensity, hole_max_perct_intensity, fill_value, manual_finetune));
            end
          end
        end
      end
    end
  end
end


