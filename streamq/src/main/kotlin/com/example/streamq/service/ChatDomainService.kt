package com.example.streamq.service

import com.example.streamq.domain.chat.*
import com.example.streamq.domain.chat.Thread
import com.example.streamq.domain.user.UserRepository
import com.example.streamq.global.exception.BusinessException
import com.example.streamq.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

import com.example.streamq.domain.feedback.FeedbackRepository

@Service
@Transactional
class ChatDomainService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val feedbackRepository: FeedbackRepository
) {
    fun prepareChats(userId: Long, content: String): Pair<UserChat, AiChat> {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val now = ZonedDateTime.now()
        
        var thread = threadRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
        if (thread == null || thread.isExpired(now)) {
            thread = threadRepository.save(Thread(user = user))
        } else {
            thread.updatedAt = now
        }

        val userChat = chatRepository.save(UserChat(thread = thread, content = content))
        val aiChat = chatRepository.save(AiChat(thread = thread, content = "", status = ChatStatus.PENDING, parentChat = userChat))

        return Pair(userChat, aiChat)
    }

    fun updateAiChatSuccess(aiChatId: Long, fullContent: String) {
        chatRepository.findById(aiChatId).ifPresent { chat ->
            if (chat is AiChat) {
                chat.content = fullContent
                chat.status = ChatStatus.COMPLETED
            }
        }
    }

    fun updateAiChatFailed(aiChatId: Long) {
        chatRepository.findById(aiChatId).ifPresent { chat ->
            if (chat is AiChat) {
                chat.status = ChatStatus.FAILED
            }
        }
    }

    fun updateAiChatPartial(aiChatId: Long, partialContent: String) {
        chatRepository.findById(aiChatId).ifPresent { chat ->
            if (chat is AiChat) {
                chat.content = partialContent
                chat.status = ChatStatus.PARTIAL
            }
        }
    }

    fun updateAiChatFiltered(aiChatId: Long, partialContent: String) {
        chatRepository.findById(aiChatId).ifPresent { chat ->
            if (chat is AiChat) {
                chat.content = partialContent
                chat.status = ChatStatus.FILTERED
            }
        }
    }

    @Transactional(readOnly = true)
    fun getChatHistory(threadId: Long, excludeChatId: Long): List<Chat> {
        return chatRepository.findTop20ByThreadIdAndIdNotOrderByCreatedAtDesc(threadId, excludeChatId).reversed()
    }

    @Transactional
    fun deleteThread(userId: Long, threadId: Long) {
        val thread = threadRepository.findById(threadId)
            .orElseThrow { BusinessException(ErrorCode.THREAD_NOT_FOUND) }
        if (thread.user.id != userId) {
            throw BusinessException(ErrorCode.THREAD_ACCESS_DENIED)
        }
        feedbackRepository.deleteAllByThreadId(thread.id)
        chatRepository.deleteAllByThreadId(thread.id)
        threadRepository.delete(thread)
    }
}
