# 鱼人博客移动端实现
移动端检测逻辑已经实现，你只需关注页面page的渲染即可。
`mobile`下都可以操作，你可以添加你需要的辅助页面的组件或者index一把梭
要求：
使用vue + ts，尽量兼容市面上常见的移动设备。
没有强调颜色的统一采用浅蓝白色调(注意不要纯白色或者很亮的颜色，会感觉太刺眼)，保证页面颜色统一而不单调

## topbar顶部栏

位置：topbar占一定高度且(随下滑)紧贴顶部，底色为：rgb(227, 227, 227), 字体为：rgb(46, 46, 46)
内容：**左右**分布
左侧是一个字体较大的`oldmerman`
右侧是一个GitHub的小标，对应项目的`src\static\github.svg`，你自己放大或者缩放到合适大小，链接到我的github页面:"https://github.com/oldMerm"

## 主渲染栏

### 描述

渲染文章类型和文章item
文章类型：栏位的顶部(但不粘贴)，渲染条目为：公告(进入该页面就默认渲染)，技术分享，日常生活，对应下方type为0,1,2，点击这三个item可以切换下方文章的渲染
文章条目渲染：你先不需要关注文章如何渲染，渲染条目即可，当用户点击类型的item后，请求后端：
**后端代码:**
```java
@Data
public class ArticlePageDetailVO {

    private String id;

    private String articleName;

    private String articleDecr;

    private String groupName;

    private Byte articleType;

}

@GetMapping("/public/page") // GET /article/public/page
public Result<PageResult<ArticlePageDetailVO>> page(@RequestParam(name = "current", defaultValue = "1") Long current,
                                                    @RequestParam(name = "size", defaultValue = "6") Long size,
                                                    @RequestParam(name = "type", defaultValue = "1") Byte articleType){
    return Result.success(articleService.page(current, size, articleType));
}

```

**响应案例:**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "current": 1,
        "size": 10,
        "total": 2,
        "pages": 1,
        "records": [
            {
                "id": "2047882638207094784",
                "articleName": "ai记忆功能简单实现",
                "articleDecr": "在langchain中实现ai的记忆功能",
                "groupName": "LangChain合集",
                "articleType": 1
            },
            {
                "id": "2054441594522046464",
                "articleName": "agent模块之流式输出",
                "articleDecr": "Langchian实现流式输出的方式",
                "groupName": "LangChain合集",
                "articleType": 1
            }
        ]
    },
    "timestamp": "2026-06-04 13:28:00",
    "path": "/article/public/page",
    "success": true
}
```

获取响应后，就可以渲染文章item了，item不要太小个，视口内渲染2~3个item。
一次请求6个item，一个视口渲染不全可以下滑(不要渲染下拉条)，栏目底部渲染....来提示用户可以左(或右)滑动进行分页请求并渲染
**文章item渲染**
顶部渲染文章名，中部渲染文章介绍，底部(右)渲染文章所属分组，宽度尽量宽，高度见上方，尽量做到饱满
请求成功获取响应后，渲染文章item的时候要有缓慢微微向上弹出的渲染动画效果，根据条目切换渲染文章时也要有这个效果

## footer底部栏
底部用没那么深的黑色渲染网站版本信息，备案号等，具体参考文件`src\views\Home\components\bcontent.vue`(参考业务逻辑)
渲染从上往下依次为版本号，icp备案号，公安备案号，用户再往下滑，有黑色背景变大(竖直方向变长，黑色背景占视口约三分之一就停止不允许下滑了，松开又弹回去)

## 总结
主页面实现如上，请求后端的逻辑只有分页渲染文章item条目，获取版本信息
请求使用axios的公共实例，使用方式参考：

```ts
const res = await httpInstance.get<any, Response>('/article/public/info', {
    params: {
      id: articleType.TECNO,
      size: 11
    }
  });
```
