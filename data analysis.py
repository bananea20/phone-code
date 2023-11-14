import os
from function import *
import re


txt_folder = r"D:\data\evaluation_metric\test\data analysis"

txt_list= get_files_with_suffix(txt_folder, '.txt')



class TextDetectionResult:
    def __init__(self, timestamp, image_path, text_value, confidence, direction, height, width):
        self.timestamp = timestamp
        self.image_path = image_path
        self.text_value = text_value
        self.confidence = confidence
        self.direction = direction
        self.height = height
        self.width = width

    def __repr__(self):
        return f"{self.timestamp} {self.image_path} {self.text_value} {self.confidence} {self.direction} {self.height} {self.width}"


import ast


for txt in txt_list:

    # 读取txt文件内容
    with open(txt, "r") as file:
        context = file.readlines()

    result = context[0]
    txt = context[1]
    dic_txt = ast.literal_eval(txt)
    lst = context[2]
    line = lst.split('\n')
        
        

    # 使用正则表达式提取所需数据
    pattern = re.compile(r'(".*?"):(\{"data":\[\[(.*?)\]\]\})')
    matches = pattern.findall(content)

    results = []

    # 遍历匹配结果并解析数据
    for match in matches:
        image_path = match[0].strip('"')
        data = match[2]

        sub_pattern = re.compile(r'\{(.*?)\}')
        sub_matches = sub_pattern.findall(data)

        for sub_match in sub_matches:
            fields = sub_match.split(',')

            timestamp = fields[4].split(':')[-1].strip('"')
            text_value = fields[5].split(':')[-1].strip('"')
            confidence = float(fields[1].split(':')[-1])
            direction = fields[2].split(':')[-1].strip('"')
            height = int(fields[3].split(':')[-1])
            width = int(fields[6].split(':')[-1])

            result = TextDetectionResult(timestamp, image_path, text_value, confidence, direction, height, width)
            results.append(result)

    # 输出解析后的数据
    for result in results:
        print(result)