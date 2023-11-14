import os
# from ocr_process import *
from function import *





test_path=r"D:\data\评价指标数据\test\4.27漕河泾OCR\压缩文件"
img_save_dir =r"D:\data\评价指标数据\test\4.27漕河泾OCR\img"


test_file = get_files_with_suffix(test_path, ".tar.gz")  # 获取文件夹下所有tar.gz文件路径




for idx,file in enumerate(test_file):
    # if file == 8:
    #     continue
                    
    video_dir = extract_tar_gz(file,is_gap=True)  # 解压tar.gz文件,得到ts视频文件, 是否跳过已经解压的文件，如果是，就不解压了
    # video_dir = file
    
    
    video_list = get_files_with_suffix(video_dir, '.ts')
    # img_dir = os.path.join(video_dir , "img2")
    img_dir = os.path.join(img_save_dir , os.path.basename(video_dir))
    
    # save_dir = video_dir # os.path.join(video_dir,"save")
    
    
    for video in video_list:
        if "MAIN" in video:
            continue
        
        extract_images(video, img_dir)
    # OCR_process(img_dir,save_dir)
    
    
    
    




    



