import router from '@/router/index.ts';

export interface Article {
  id: string;
  articleName: string;
  articleStatus: number;
  like: number;
  createdAt: string;
}

// 访问文章功能，根据文章id渲染并跳转
export const goToArticle = async (articleId?:string, open?:boolean, isPublic:'public' | 'private' = 'public') => {
  if(!articleId){
    alert('文章ID不存在，无法跳转');
    return;
  }
  const routerConfig = { 
      name: 'markdown',
      params: { 
        id:articleId,
      },
      query: {isPublic}
  }
  if(open === true){
    const routerUrl = router.resolve(routerConfig);
    window.open(routerUrl.href, '_blank');
  }else{
    router.push(routerConfig);
  }
  
}

export enum articleType {
  Notice = 0,
  TECNO = 1,
  LIFE = 2
}