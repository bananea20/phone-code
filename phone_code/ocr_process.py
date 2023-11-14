from paddleocr import PaddleOCR, draw_ocr
from function import *
from PIL import Image
import json


# det_dir="/home/jiajia.sun.o/jiajia.sun.o/codes/PaddleOCR-release-2.6/inference/ch_PP-OCR_V3_det_teacher_0404/Student/" # 0404,Student
# # det_dir="/home/jiajia.sun.o/jiajia.sun.o/codes/PaddleOCR-release-2.6/inference/ch_PP-OCR_V3_det_teacher_0404/Teacher/" # 0404,Teacher

det_dir="/data/home/jiajia.sun.o/codes/PaddleOCR-release-2.6/inference/ch_PP-OCR_V3_det_student_0214/"
cls_dir = "/data/home/zewen.piao.o/PaddleOCR-release-2.6/output/cls_0331_inference"  # 0331,best
rec_dir='/data/home/bofu.huang.o/data/workdir/2023/train_ocr/output/0323_rec_ppocr_v3_distillation/export/0207/Student' # 0331,best

# save_dir='./inference_results/e2e/'
# # output='./output

OCR = PaddleOCR(use_angle_cls=True, 
                lang="ch",
                det_model_dir=det_dir,
                cls_model_dir=cls_dir,
                rec_model_dir=rec_dir,
                # crop_res_save_dir=save_dir,
                save_crop_res=False,
                use_gpu=False
                )




def OCR_process(img_folder_dir,save_dir):    
    
    
    image_files = get_files_with_suffix(img_folder_dir, 'png')
    # print(f"子文件夹 {output_path} 下的图片文件列表：{image_files}")

    img_result = []
    img_result2 = []
    img_save_dir = os.path.join(save_dir,"OCR_result_img")
    


    for img_dir in image_files:
        print(img_dir)
        
        result = OCR.ocr(img_dir, cls=True, rec=True, det=True)
        result=result[0]
        img = Image.open(img_dir).convert('RGB')
        boxes = [line[0] for line in result]
        txts = [line[1][0] for line in result]
        scores = [line[1][1] for line in result]
        im_show = draw_ocr(img, boxes, txts, scores)
        im_show = Image.fromarray(im_show)

        if not os.path.exists(img_save_dir):
            os.makedirs(img_save_dir)
        file_name = os.path.basename(img_dir)
        top_dir_name = os.path.basename(os.path.dirname(img_dir))
        file_name =top_dir_name + "_" +file_name
        file_name = os.path.join(img_save_dir, file_name)
        
        im_show.save(file_name) #结果图片保存在代码同级文件夹中。
        
        img_result.append((os.path.split(img_dir)[1],result))
        
        
        for idx, txt in enumerate(txts):
            img_result2.append((txt, boxes[idx], scores[idx],img_dir))
            

        
    # 排序
    sorted_results = sorted(img_result2, key=lambda x: len(x[0]))
    
    results = {}
    for txt, box, score, img_dir in sorted_results:
        if txt not in results:
            results[txt] = [txt, [[box, score, img_dir]]]
        else:
            results[txt][-1].append([box, score, img_dir])
    
    results_list = list(results.values()) # dic2list
    
    result_save_dir = save_dir +  'result.json'
    
    if not os.path.exists(os.path.split(result_save_dir)[0]):
        os.makedirs(os.path.split(result_save_dir)[0])
        
    
    img_json2= json.dumps(results_list, ensure_ascii=False)
    f2 = open(result_save_dir, 'w')
    f2.write(img_json2)
    f2.close()