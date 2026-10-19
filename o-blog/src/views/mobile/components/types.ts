export interface ArticlePageDetailVO {
  id: string
  articleName: string
  articleDecr: string
  groupNameList?: string[] | null
  isTop?: boolean
}

export interface ArticleGroupRenderVO {
  id: number
  groupName: string
  createdAt?: string
  number?: number
}

export interface PageResult<T> {
  current: number
  size: number
  total: number
  pages: number
  records: T[]
}
