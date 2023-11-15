# Desc: 卡尔曼滤波器的简单实现
# 
import numpy as np

# 时间间隔
dt = 1.0

# 初始化状态（位置和速度）
x = np.array([[0], [0], [0], [0]])  # 初始位置 (0,0)，初始速度 (0,0)

# 初始化状态协方差
P = np.eye(4)

# 状态转移矩阵
F = np.array([[1, dt, 0, 0],
              [0, 1, 0, 0],
              [0, 0, 1, dt],
              [0, 0, 0, 1]])

# 观测矩阵
H = np.array([[0, 1, 0, 0],
              [0, 0, 0, 1]])

# 过程噪声协方差
Q = np.eye(4) * 0.01

# 观测噪声协方差
R = np.eye(2) * 0.01

# 模拟一些观测数据（二维速度）
true_velocity = [2, 5]  # 真实速度
observed_velocities = [np.array(true_velocity) + np.random.randn(0.1) for _ in range(10)]
print(observed_velocities)
# 卡尔曼滤波过程
for v in observed_velocities:
    # print(v)
    # 预测
    x = F @ x
    P = F @ P @ F.T + Q

    # 更新
    y = v - H @ x  # 观测残差
    S = H @ P @ H.T + R  # 残差协方差
    K = P @ H.T @ np.linalg.inv(S)  # 卡尔曼增益
    x = x + K @ y
    P = P - K @ H @ P

    print(f"估计位置: ({x[0, 0]}, {x[2, 0]}), 估计速度: ({x[1, 0]}, {x[3, 0]})")
