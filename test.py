import treemap

# 创建一个树形结构数据
data = {
    "name": "root",
    "children": [
        {
            "name": "child1",
            "value": 10
        },
        {
            "name": "child2",
            "value": 20
        },
        {
            "name": "child3",
            "children": [
                {
                    "name": "grandchild1",
                    "value": 5
                },
                {
                    "name": "grandchild2",
                    "value": 15
                }
            ]
        }
    ]
}

# 使用Ordered Treemap of Weight Divided Layout Algorithm创建树状图
treemap.treemap(data, algorithm='ordered_weight')
