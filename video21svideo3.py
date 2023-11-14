import os 
import subprocess


# 获取文件夹下所有子文件夹路径
def get_subdirectories(path):
    subdirs = []
    for root, dirs, files in os.walk(path):
        for d in dirs:
            subdir = os.path.join(root, d)
            subdirs.append(subdir)
            subdirs += get_subdirectories(subdir)
    return subdirs


# 返回指定文件夹的指定后缀的文件的完整路径
def get_files_with_suffix(folder_path, suffixes):
    """
    输入一个路径，返回指定后缀的文件的完整路径。

    参数:
    directory (str): 文件夹路径。
    file_extension (str): 指定的文件后缀，例如 ".txt"。

    返回:
    List[str]: 文件完整路径的列表。
    """
    file_paths = []
    for root, dirs, files in os.walk(folder_path):
        for file in files:
            if file.endswith(tuple(suffixes)):
                file_path = os.path.join(root, file)
                file_paths.append(file_path)
    return file_paths


    
# 视频转换为1秒一帧的视频    
def video_to_1svideo(video, vdio_dir):
    """
    将 TS 视频转换为 MP4 格式。
    :param ts_file: TS 视频文件路径
    :param mp4_file: MP4 输出文件路径
    """
    cmd = f"ffmpeg -i {video} -r 1 {vdio_dir} "
    process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output, error = process.communicate()
    print(output)   

import tarfile
def compress_folder(source_dir, output_path):
    """
    将一个目录打包成一个tar.gz的压缩文件，并将压缩文件保存到指定路径
    :param source_dir: 要压缩的源目录的路径
    :param output_path: 压缩文件的输出路径
    """
    
    
    temp = os.path.splitext(os.path.basename(source_dir))[0] + '.tar.gz'
    output_path = os.path.join(output_path, temp)
    # output_path
    # 创建tar.gz压缩文件，并遍历源目录中的所有文件，将它们添加到压缩文件中
    with tarfile.open(output_path, "w:gz") as tar:
        for root, dirs, files in os.walk(source_dir):
            for file in files:
                file_path = os.path.join(root, file)
                # 使用os.path.relpath()获取文件在源目录中的相对路径，并将其作为arcname参数传递给tar.add()方法
                tar.add(file_path, arcname=os.path.relpath(file_path, source_dir))


from datetime import datetime,timedelta


def clip_video_by_datetime(end_time, input_path, output_path, start_time=None):
    # 根据时间戳剪切视频
    if start_time is None:
        start_time = end_time - timedelta(seconds=30)
    
    video_start=input_path.split("/")[-1].split("_",2)[-1].split(".")[0]
    video_start = datetime.strptime(video_start, '%Y_%m_%d_%H_%M_%S_%f')
    # print(video_start)
    loc_start_time  = start_time - video_start
    loc_end_time = loc_start_time + timedelta(seconds=30)
    # print(loc_start_time,loc_end_time)

    print(input_path)
    if loc_start_time.total_seconds()< 0:
        loc_start_time =  timedelta(seconds=0)
    cmd = f"ffmpeg -i {input_path} -ss {loc_start_time} -to {loc_end_time} -c copy {output_path}"
    subprocess.run(cmd, shell=True)
    print("video_during:",(loc_end_time-loc_start_time).total_seconds())
    


test_path=r"D:\data\evaluation_metric\0112-vis-parking-result\gt_video_100\ori_video"
vdio_save_dir =r"D:\data\evaluation_metric\0112-vis-parking-result\gt_video_100\1s_video"
test_file=get_subdirectories(test_path) # 子文件夹读取


for idx,file in enumerate(test_file):
    video_list = get_files_with_suffix(file, '.ts')   
    vdio_dir = os.path.join(vdio_save_dir , os.path.basename(file))
    for video in video_list:
        if "MAIN" in video:
            continue
        file_name = os.path.basename(video)
        vdio_1s = os.path.join(vdio_dir , file_name)
        video_to_1svideo(video, vdio_1s)
        
        
    vdio_list = get_subdirectories(vdio_save_dir) # 子文件夹读取
    [compress_folder(vdio,vdio_save_dir) for vdio in vdio_list] # 压缩文件夹
    
    