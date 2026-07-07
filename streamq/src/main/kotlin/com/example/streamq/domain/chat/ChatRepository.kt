package com.example.streamq.domain.chat

import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findTop20ByThreadIdAndIdNotOrderByCreatedAtDesc(threadId: Long, excludeChatId: Long): List<Chat>
    fun findByThreadIdInOrderByCreatedAtAsc(threadIds: List<Long>): List<Chat>
    fun deleteAllByThreadId(threadId: Long)
}
