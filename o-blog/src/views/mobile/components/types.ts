export interface ArticlePageDetailVO {
  id: string
  articleName: string
  articleDecr: string
  groupName: string
  articleType: number
}

export interface PageResult<T> {
  current: number
  size: number
  total: number
  pages: number
  records: T[]
}
