#!/bin/bash

#读取各个位置的model,加密后下载


# det模型的路径

det=0228
date=0320

det_model_dir=/data/home/jiajia.sun.o/codes/PaddleOCR-release-2.6/inference/ch_PP-OCR_V3_det_student_$det/model.tflite
det_output_dir=/data/home/bofu.huang.o/data/workdir/2023/train_ocr/jiami/$date/det.model



# cls模型的路径

cls=cls_02231340_95_93.tflite
# cls=cls_02231340_95_93
cls_model_dir=/data/home/dongjing.liu/data/parking_frames/gt/upload/models/$cls
cls_output_dir=/data/home/bofu.huang.o/data/workdir/2023/train_ocr/jiami/$date/classification.model 



# res模型的路径
folder_name=0303_rec_ppocr_v3_distillation

res_model_dir=/data/home/bofu.huang.o/data/workdir/2023/train_ocr/output/$folder_name/export/0207/Student/model.tflite
res_output_dir=/data/home/bofu.huang.o/data/workdir/2023/train_ocr/jiami/$date/recognition.model 

cd /data/home/bofu.huang.o/data/workdir/2023/train_ocr/jiami/

# rm res.model
rm det.model
rm classification.model 
rm recognition.model 

./encrypt $det_model_dir  $det_output_dir
./encrypt $cls_model_dir  $cls_output_dir
./encrypt $res_model_dir  $res_output_dir


