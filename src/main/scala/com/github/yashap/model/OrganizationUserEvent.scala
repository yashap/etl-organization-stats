package com.github.yashap.model

import com.github.yashap.model.SocialNetworkType._

import java.time.LocalDateTime

case class OrganizationUserEvent(
  timestamp: LocalDateTime,
  userId: Long,
  event: String,
  socialNetworkType: Option[SocialNetworkType],
  organizationId: Long
) extends BaseOrgEvent
