package com.example.streamq.domain.chat

import jakarta.persistence.*

@Entity
@DiscriminatorValue("ASSISTANT")
class AiChat(
    thread: Thread,
    content: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ChatStatus = ChatStatus.PENDING,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_chat_id")
    val parentChat: UserChat
) : Chat(thread, content)
