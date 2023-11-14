import matplotlib.pyplot as plt
import numpy as np
from scipy.spatial import ConvexHull
from shapely.geometry import LineString, Polygon, box
import cv2

# 已知一个不规则的轮廓,
# 先画一个不规则的外接多边形,
# 再对这个多边形画一个最小外接矩形框,最小外接矩形框的左侧短边与多边形的交点分别记为p点,
# 找到多边形上与p点相邻的两个点b1和b2,连接p点与b1,连接p点与b2, 形成l1和l2两条直线.
# 取l1和l2两条直线中角度较小的一条直线, 与外接矩形相交, 形成新的多边形.
# 用代码实现以上过程


import numpy as np
from scipy.spatial import ConvexHull
import matplotlib.pyplot as plt

# 假设我们有一个不规则形状的点集
points = np.random.rand(30, 2)   # 30个2维的随机点

# 计算这些点的凸包，这将给我们一个不规则的外接多边形
hull = ConvexHull(points)

# 画出这个不规则的外接多边形
plt.plot(points[:,0], points[:,1], 'o')
for simplex in hull.simplices:
    plt.plot(points[simplex, 0], points[simplex, 1], 'k-')

# 计算最长的两条平行边
longest_edges = [(0, 0), (0, 0)]  # (长度, 边索引)的元组列表
for i, edge in enumerate(hull.simplices):
    length = np.linalg.norm(points[edge[1]] - points[edge[0]])
    if length > longest_edges[0][0]:
        longest_edges[1] = longest_edges[0]
        longest_edges[0] = (length, i)
    elif length > longest_edges[1][0]:
        longest_edges[1] = (length, i)

# 画出最长的两条平行边
for edge_index in longest_edges:
    edge = hull.simplices[edge_index[1]]
    plt.plot(points[edge, 0], points[edge, 1], 'r-', linewidth=2)

plt.show()





# # 用minAreaRect()画出hull.simplices的最小外接矩形框
# rect = cv2.minAreaRect(points[hull.vertices])
# #绘制rect
# box = cv2.boxPoints(rect)
# box = np.int0(box)
# print("box:",box)


# # 找到外接矩形框左侧短边与多边形的交点，记为p点
# # 这里我们假设左侧是短边
# poly = Polygon(points[hull.vertices])
# line = LineString([(min_x, min_y), (min_x, max_y)])
# intersection = poly.intersection(line)
# p = np.array([intersection.x, intersection.y])

# # 画出p点
# plt.plot(p[0], p[1], 'go')


# # 找到外接多边形hull.simplices所有的顶点
# vertices = points[hull.vertices] 

# #vertices从p点开始顺时针排序
# vertices = np.roll(vertices, -np.argmin(np.linalg.norm(vertices - p, axis=1)), axis=0)
 

# # vertices中p点下一个点为b1,最后一个点为b2
# b1 = vertices[1]
# b2 = vertices[-1]
# print("b1,b2:",b1,b2)

# print("p点为:",p)
# print("vertices为:",vertices)


# # # # 找到外接多边形hull.simplices上与p点相邻的两个点b1和b2
# # b1 = points[hull.vertices[np.argmin(np.linalg.norm(points[hull.vertices] - p, axis=1))]]
# # b2 = points[hull.vertices[np.argmin(np.linalg.norm(points[hull.vertices] - b1, axis=1))]]



# # 连接p点与b1和b2，形成l1和l2两条直线
# plt.plot([p[0], b1[0]], [p[1], b1[1]], 'b-')
# plt.plot([p[0], b2[0]], [p[1], b2[1]], 'b-')

# # 定义一个函数，计算与x轴的角度
# def angle_with_x_axis(p1, p2):
#     diff = p2 - p1
#     return np.arctan2(diff[1], diff[0])

# # 计算两个角度，选择较小的一个
# angle1 = angle_with_x_axis(p, b1)
# angle2 = angle_with_x_axis(p, b2)
# if abs(angle1) < abs(angle2):
#     selected_line = LineString([p, b1])
# else:
#     selected_line = LineString([p, b2])

# print([p, b1])
# print([p, b2])

# # # 画出selected_line
# # plt.plot([selected_line.coords[0][0], selected_line.coords[1][0]], [selected_line.coords[0][1], selected_line.coords[1][1]], 'r-')


# # 用绿色画出selected_line
# plt.plot([selected_line.coords[0][0], selected_line.coords[1][0]], [selected_line.coords[0][1], selected_line.coords[1][1]], 'g-')

# plt.show()
