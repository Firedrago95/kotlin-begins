package com.example.streamq.domain.chat

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
@DiscriminatorValue("AI")
class AiChat (
    thread: Thread,
    content: String,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_chat_id")
    val parentChat: UserChat
) :Chat(thread, content){

    @Enumerated(EnumType.STRING)
    var status: ChatStatus = ChatStatus.PENDING
}
