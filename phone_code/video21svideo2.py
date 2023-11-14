import os
import multiprocessing
# from ocr_process import *
from function import *


def main():



    # test_path=r"D:\data\评价指标数据\generalparking\generalparking_video"
    # img_save_dir =r"D:\data\评价指标数据\generalparking\generalparking_video\img"

    test_path=r"D:\data\evaluation_metric\generalparking\generalparking_video\ori_video"
    vdio_save_dir =r"D:\data\evaluation_metric\generalparking\generalparking_video\1s_video"
    img_save_dir =r"D:\data\evaluation_metric\generalparking\generalparking_video\1s_img"
    ts_save_dir = r"D:\data\evaluation_metric\generalparking\generalparking_video\1s_gz"

    # test_file=get_file_paths(test_path) # 获取文件夹下所有文件路径
    # test_file=get_subdirectories(test_path) #子文件夹ts

    #  compress_folder(r'D:\data\评价指标数据\test\4.23万象城+漕河泾科技绿洲\source')  # 压缩文件夹

    # test_file = get_files_with_suffix(test_path, ".tar.gz")

    # for file in test_file:
    #     new_name = file.replace("tar.gz",".tar.gz")
    #     os.rename(file, new_name)


   

    num_processes = 4  # 进程数
    pool = multiprocessing.Pool(processes=num_processes)

    test_file=get_subdirectories(test_path) # 子文件夹读取
    for idx,file in enumerate(test_file):
        print(idx,file)
        # if idx < 3 :
        #     continue
        
        video_list = get_files_with_suffix(file, '.ts')
        img_dir = os.path.join(img_save_dir , os.path.basename(file))
        vdio_dir = os.path.join(vdio_save_dir , os.path.basename(file))
        # cut_img = 0
        # if count_jpg_files(img_dir) % 4:     
        #         print(img_dir)
        #         cut_img = 1
        
        # save_dir = video_dir # os.path.join(video_dir,"save")
        
        
        for video in video_list:
            if "MAIN" in video:
                continue
            # 截图

            capture_frames(video, img_dir,vdio_dir)
        #     pool.apply_async(capture_frames, args=(video, img_dir, vdio_dir))
            
        for video in video_list:
            image_to_video(img_dir, vdio_dir)
            # pool.apply_async(image_to_video, args=(img_dir, vdio_dir))

        
    vdio_list = get_subdirectories(vdio_save_dir) # 子文件夹读取
    [compress_folder(vdio,ts_save_dir) for vdio in vdio_list] # 压缩文件夹

            
            
        # OCR_process(img_dir,save_dir) # OCR识别
    
    pool.close()
    pool.join()  
if __name__ == '__main__':
    multiprocessing.freeze_support()
    main()
        
    




    



