import router from '@/router/index.ts';

export interface Article {
  id: string;
  articleName: string;
  articleStatus: number;
  like: number;
  createdAt: string;
}

// 访问文章功能，根据文章id渲染并跳转
export const goToArticle = async (articleId?:string, open?:boolean) => {
  if(!articleId){
    alert('文章ID不存在，无法跳转');
    return;
  }
  if(open === true){
    const routerUrl = router.resolve({ 
      name: 'markdown',
      params: { 
        id:articleId,
      }
    });
    window.open(routerUrl.href, '_blank');
  }else{
    router.push({
      name: 'markdown',
      params: { 
        id:articleId
      }
    });
  }
  
}

export enum articleType {
  Notice = 0,
  TECNO = 1,
  LIFE = 2
}