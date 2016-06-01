package com.github.yashap.model

import java.time.LocalDateTime

case class OrganizationEvent(
  id: String,
  timestamp: LocalDateTime,
  organizationId: Long,
  event: String
) extends BaseOrgEvent
