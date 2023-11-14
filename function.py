import os


# 返回该文件夹下所有文件的路径列表
def get_file_paths(folder_path):
    file_paths = []
    for root, dirs, files in os.walk(folder_path):
        for file in files:
            file_path = os.path.join(root, file)
            file_paths.append(file_path)
    return file_paths



# 获取文件夹下所有子文件夹路径
def get_subdirectories(path):
    subdirs = []
    for root, dirs, files in os.walk(path):
        for d in dirs:
            subdir = os.path.join(root, d)
            subdirs.append(subdir)
            subdirs += get_subdirectories(subdir)
    return subdirs


import tarfile

# 将目录下每个子文件夹中的文件压缩成一个tar.gz文件


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



# 解压tar.gz文件,得到ts视频文件
def extract_tar_gz(tar_file, is_gap=False):
    
    output_dir = os.path.splitext(os.path.splitext(tar_file)[0])[0] # 去掉tar.gz后缀

    # if os.path.exists(output_dir): # 如果已经解压过，则跳过
    #     return output_dir
    
    if is_gap==True: # is_gap=True, 则跳过已经解压的文件
        return output_dir
    
    
    # 创建输出文件夹，名称与 tar 文件相同
    directory = output_dir
    if not os.path.exists(directory):
        os.makedirs(directory)

    try:
    # 打开 tar.gz 文件
        with tarfile.open(tar_file, "r:gz") as tar:
            # 遍历 tar.gz 文件中的所有成员（文件或目录）
            for member in tar.getmembers():
                # 如果当前成员是文件，则打印文件名和文件大小
                if member.isfile():
                    print("Filename: {}".format(member.name))
                    print("File size: {} bytes".format(member.size))
                    
                    # 读取文件内容
                    with tar.extractfile(member) as f:
                        content = f.read()
                        # 保存文件到输出文件夹中，名称与原文件名相同
                        ts_file_path = os.path.join(output_dir, member.name)
                        
                        with open(ts_file_path, 'wb') as ts_file:
                            ts_file.write(content)
                        print("Saved {}".format(ts_file_path))
    except:
        print(tar_file)
        
    return output_dir



import datetime
import subprocess

# 将ts视频文件每秒截图一次
def extract_images(input_path, output_path):
    
    # 如果输出目录不存在，则新建
    if not os.path.exists(output_path):
        os.makedirs(output_path)
    # else :
    #     return output_path # 存在就跳过
    

    # 从输入路径中提取视频文件名（不含扩展名）
    video_name = os.path.splitext(os.path.basename(input_path))[0]

    # 提取视频名称前缀
    prefix = "_".join(video_name.split("_")[:2]) + "_"

    # 获取视频开始时间戳
    start_timestamp_str =  "_".join(video_name.split("_")[2:]) 
    start_timestamp = datetime.datetime.strptime(start_timestamp_str, "%Y_%m_%d_%H_%M_%S_%f")

    # 使用ffmpeg获取视频长度（单位为秒）
    cmd = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 {}".format(input_path)
    process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output, error = process.communicate()
    duration = float(output.decode("utf-8").strip())
    
    # 每秒截取一帧图像
    for i in range(int(duration)):
        timestamp = start_timestamp + datetime.timedelta(seconds=i)
        timestamp_str = timestamp.strftime("%Y_%m_%d_%H_%M_%S") + "_{:03d}".format(timestamp.microsecond // 1000)
        output_file = os.path.join(output_path, "{}{}.jpg".format(prefix, timestamp_str))
        cmd = "ffmpeg -i {} -ss {} -vframes 1 {}".format(input_path, i+1, output_file)
        process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        output, error = process.communicate()
        print(output)
    
    return output_path

# 示例用法
# input_path = "/path/to/your/video/FINDCAR_BACK_2023_04_15_14_37_11_526.ts"
# output_path = "/path/to/your/output/directory"
# extract_images(input_path, output_path)




import shutil
def copy_images(source_dir, target_dir): # 复制图片
    os.makedirs(target_dir, exist_ok=True)  # 确保目标文件夹存在，如果不存在则创建
    for filename in os.listdir(source_dir):
        if filename.endswith('.jpg') or filename.endswith('.png'):
            source_path = os.path.join(source_dir, filename)
            target_path = os.path.join(target_dir, filename)
            shutil.copy2(source_path, target_path)
            
         
         
         
            
# 截图，保存到指定文件夹，将图片合成为ts视频文件            
import subprocess

def capture_frame(input_path, output_file, timestamp): 
    # 定义一个函数capture_frame，用于截取视频中的某一帧图像
    cmd = "ffmpeg -i {} -ss {} -vframes 1 -vf fps=1 {}".format(input_path, timestamp, output_file)
    # 拼接出ffmpeg命令行，其中{}是需要传入参数的位置，这里传入的参数分别为：
    # -i 输入文件路径
    # -ss 开始时间戳，即要截取的帧的时间
    # -vframes 1 表示只截取一帧
    # -vf fps=1 表示将视频转换为一帧，以便在指定时间戳处截取一帧
    process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    # subprocess.Popen启动新进程运行ffmpeg命令，通过stdout和stderr获取输出结果
    output, error = process.communicate()
    # 等待进程执行完成，并返回stdout和stderr输出结果

    
   
   
def save_image(temp_file, temp_dir, img_out):
    img_path = os.path.join(img_out, os.path.splitext(os.path.basename(temp_file))[0] + '.jpg')
    cmd = "ffmpeg -i {} -qscale:v 2 -y {}".format(os.path.join(temp_dir, temp_file), img_path)
    process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output, error = process.communicate()
    
    
def capture_frames(input_path, img_out,video_out, fps=1, num_processes=14): # 截取视频中的所有图像
    
    if not os.path.exists(video_out):
        os.makedirs(video_out)
    if not os.path.exists(img_out):
        os.makedirs(img_out)
     
    # 从输入路径中提取视频文件名（不含扩展名）
    video_name = os.path.splitext(os.path.basename(input_path))[0]

    # 提取视频名称前缀
    prefix = "_".join(video_name.split("_")[:2]) + "_"
        
    # 获取视频开始时间戳
    start_timestamp_str =  "_".join(video_name.split("_")[2:]) 
    start_timestamp = datetime.datetime.strptime(start_timestamp_str, "%Y_%m_%d_%H_%M_%S_%f")

    
    
    # 使用ffmpeg获取视频长度（单位为秒）
    cmd = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 {}".format(input_path)
    process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output, error = process.communicate()
    duration = float(output.decode("utf-8").strip())
    
    
# 每秒截取一帧图像
    # temp_dir = img_out
    
    temp_dir = os.path.join(img_out, prefix)
    if not os.path.exists(temp_dir):
        os.makedirs(temp_dir)
    
    timestamps = [start_timestamp + datetime.timedelta(seconds=i / fps) for i in range(int(duration * fps))]
    output_files = [os.path.join(temp_dir, "{}{}.jpg".format(prefix, timestamp.strftime("%Y_%m_%d_%H_%M_%S") + "_{:03d}".format(timestamp.microsecond // 1000))) for timestamp in timestamps]
    
    frame_list = list(range(1, int(duration) + 1))
    
    
    # pool = multiprocessing.Pool(processes=num_processes)
    for i in range(len(timestamps)):
        capture_frame(input_path, output_files[i],  frame_list[i])
    #     pool.apply_async(capture_frame, args=(input_path, output_files[i], frame_list[i]))
    # pool.close()
    # pool.join()
    print("Finished capturing frames.")
    
    
    copy_images(temp_dir, img_out)     # 将采集到的所有图片保存到指定目录
    
    shutil.rmtree(temp_dir)     # 删除临时文件夹及其中的所有文件
    
    
import cv2
import os

def capture_frames_mp4(video_path, output_folder, interval=1):
    """
    从给定的视频中每隔指定秒数截取一张图片。
    :param video_path: 视频文件路径
    :param output_folder: 图片输出文件夹
    :param interval: 截取图片的时间间隔（秒）
    """
    # 确保输出文件夹存在
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)

    # 打开视频文件
    video = cv2.VideoCapture(video_path)
    fps = int(video.get(cv2.CAP_PROP_FPS))
    frame_count = int(video.get(cv2.CAP_PROP_FRAME_COUNT))

    # 遍历视频的每一帧
    for frame_index in range(0, frame_count, fps * interval):
        # 将视频设置为指定帧
        video.set(cv2.CAP_PROP_POS_FRAMES, frame_index)
        # 读取当前帧
        ret, frame = video.read()
        # 如果读取成功，保存图片
        if ret:
            output_path = os.path.join(output_folder, f"frame_{frame_index}.jpg")
            cv2.imwrite(output_path, frame)

    # 释放视频资源
    video.release()


def convert_ts_to_mp4(ts_file, mp4_file):
    """
    将 TS 视频转换为 MP4 格式。
    :param ts_file: TS 视频文件路径
    :param mp4_file: MP4 输出文件路径
    """
    cmd = f"ffmpeg -i {ts_file} -c:v libx264 -c:a aac -strict experimental -b:a 128k {mp4_file}"
    process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output, error = process.communicate()
    print(output)   


    
    

import numpy as np
import cv2
import pathlib

# 把图片生成ts视频
def image_to_video(img_folder, output_dir):
    print("img_folder: ", img_folder)
    # img_folder = img_folder.encode('gbk')
    # resdir = os.path.join(output_dir, os.path.basename(img_folder))
    # os.makedirs(resdir, exist_ok=True)
    resdir = output_dir
    if not os.path.exists(resdir):
        os.makedirs(resdir)
    # 设置视频宽度和高度
    width = 1280
    height = 1000

    # 设置帧率
    fps = 1.0

    # 读取文件夹中的所有图像文件
    images = [img for img in os.listdir(img_folder) if img.endswith(".jpg")]
    front = [img for img in images if "FRONT" in img]
    back = [img for img in images if "BACK" in img]
    left = [img for img in images if "LEFT" in img]
    right = [img for img in images if "RIGHT" in img]
    total = [front, back, left, right]
    
    names = [os.path.splitext(front[0])[0],
             os.path.splitext(back[0])[0],
             os.path.splitext(left[0])[0],
             os.path.splitext(right[0])[0]
             ]
    
    for i in range(len(total)):
        if total[i] is not None:
            # 将图像按照文件名排序
            total[i].sort(key=lambda x: x.split('.')[0])

            # 创建视频编码器
            # fourcc = cv2.VideoWriter_fourcc(*"avc1")
            # out = cv2.VideoWriter(os.path.join(resdir, f"{names[i]}.mp4"), -1, fps, (width, height))
            
            vdo_path = os.path.join(resdir, f"{names[i]}.ts")
            # vdo_path = pathlib.Path(vdo_path)
            out = cv2.VideoWriter(vdo_path, -1, fps, (width, height))

            # 遍历图像列表并将它们添加到视频中
            for image in total[i]:
                img_path = os.path.join(img_folder, image)
                img_path = pathlib.Path(img_path)
                img = cv2.imdecode(np.fromfile(file=img_path, dtype=np.uint8), cv2.IMREAD_COLOR)
                # img = cv2.imread(img_path)
                # img = cv2.resize(img, (width, height))
                out.write(img)

            # 释放资源
            out.release()
            
            
            
import os

def rename_ts_files(folder_path, new_prefix):
    """
    修改文件夹中的所有 TS 视频的开头名字。
    :param folder_path: 文件夹路径
    :param new_prefix: 新的开头名字
    """
    for filename in os.listdir(folder_path):
        # 检查文件是否是 TS 视频
        if filename.endswith('.ts'):
            # 提取文件名的主体部分（不包括扩展名）
            basename, extension = os.path.splitext(filename)
            # 将文件名的主体部分切分成前缀和后缀
            old_prefix, suffix = basename.split('_', 1)
            # 生成新的文件名
            new_filename = f'{new_prefix}_{suffix}{extension}'
            # 获取原始文件和新文件的完整路径
            old_filepath = os.path.join(folder_path, filename)
            new_filepath = os.path.join(folder_path, new_filename)
            # 重命名文件
            os.rename(old_filepath, new_filepath)

# # 示例用法
# folder_path = '/path/to/your/folder'
# new_prefix = 'NewPrefix'
# rename_ts_files(folder_path, new_prefix)


# 统计文件夹中的 JPG 文件数量
def count_jpg_files(folder_path):
    """
    统计文件夹中的 JPG 文件数量。
    :param folder_path: 文件夹路径
    :return: JPG 文件数量
    """
    jpg_count = 0
    for filename in os.listdir(folder_path):
        # 检查文件是否是 JPG 图片（不区分大小写）
        if filename.lower().endswith('.jpg'):
            jpg_count += 1
    return jpg_count



