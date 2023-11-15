import numpy as np
from scipy.optimize import minimize

# 生成随机点云数据
np.random.seed(0)  # 为了可重现性
points = np.random.rand(100, 3)  # 100个点在1x1x1立方米内

# 划分格子
def assign_to_bins(points, bin_size=0.5):
    bins = {}
    for point in points:
        bin_key = (int(point[0] / bin_size), int(point[1] / bin_size), int(point[2] / bin_size))
        if bin_key not in bins:
            bins[bin_key] = []
        bins[bin_key].append(point)
    return bins

bins = assign_to_bins(points)

# 计算质心
def compute_centroid(points):
    return np.mean(points, axis=0)

bin_centroids = {bin_key: compute_centroid(bin_points) for bin_key, bin_points in bins.items()}
global_centroid = compute_centroid(points)

# 定义优化目标函数
def objective_function(velocity, bins, bin_centroids, global_centroid):
    total_distance = 0
    for bin_key, bin_points in bins.items(): #遍历字典（bins）中的所有键值
        # 应用速度移动点
        moved_points = bin_points + velocity
        centroid = bin_centroids[bin_key]
        # 计算移动点到其所在箱子中心的距离
        distances_to_bin_centroid = np.linalg.norm(moved_points - centroid, axis=1)
        # 计算移动点到全局中心的距离
        distances_to_global_centroid = np.linalg.norm(moved_points - global_centroid, axis=1)
         # 对目标函数的所有距离进行求和
        total_distance += np.sum(distances_to_bin_centroid) + np.sum(distances_to_global_centroid)
    return total_distance

# 初始速度猜测
initial_velocity = np.array([0, 0, 0])

# 运行最小化过程
result = minimize(objective_function, initial_velocity, args=(bins, bin_centroids, global_centroid))



# 输出结果
estimated_velocity = result.x
print("Estimated Velocity:", estimated_velocity)
