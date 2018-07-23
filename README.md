# PullSeparateRecyclerView

## 功能
* 滚动到头部或底部item可分离的RecyclerView
* RecyclerView的item重叠
* RecyclerView的item入场动画
* 共享元素跳转页面

## 效果图
![demo](https://github.com/tianyu704/PullSeparateRecyclerView/blob/master/294a7845-e48d-4bcc-b147-4b466252f007.gif)；

## 说明
* 参考PullSeparateListView，让RecyclerView也可以item分离 https://github.com/chiemy/PullSeparateListView
* 用addItemDecoration()实现item重叠，将outRect.bottom=负值即可重叠
* 用第三方动画库实现item入场动画 （implementation 'jp.wasabeef:recyclerview-animators:2.3.0'）
* 用共享元素实现页面跳转的动画

需要该效果的小伙伴可以参考
