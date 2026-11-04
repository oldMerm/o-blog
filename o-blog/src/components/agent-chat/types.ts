export type AgentTurnStatus = 'pending' | 'streaming' | 'done' | 'aborted' | 'error'

export type AgentChatStatus = 'idle' | 'connecting' | 'streaming' | 'done' | 'aborted' | 'error'

export interface AgentTurn {
  id: string
  question: string
  content: string
  reasoning: string
  tools: string[]
  status: AgentTurnStatus
  error: string
  expanded: boolean
  finishReason: string
}
