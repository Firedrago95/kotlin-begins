package com.example.streamq.domain.chat

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue(value = "USER")
class UserChat (
    thread: Thread,
    content: String
) : Chat(thread, content){
}
