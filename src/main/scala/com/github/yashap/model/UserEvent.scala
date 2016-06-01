package com.github.yashap.model

import com.github.yashap.model.SocialNetworkType.SocialNetworkType

import java.time.LocalDateTime

case class UserEvent(
  id: String,
  timestamp: LocalDateTime,
  userId: Long,
  event: String,
  socialNetworkType: Option[SocialNetworkType]
)
