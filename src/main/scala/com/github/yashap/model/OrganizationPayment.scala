package com.github.yashap.model

import com.github.yashap.model.PaymentProcessor.PaymentProcessor

import java.time.LocalDateTime

case class OrganizationPayment(
  id: String,
  timestamp: LocalDateTime,
  organizationId: Long,
  event: String,
  paymentAmount: Double,
  paymentProcessor: PaymentProcessor
) extends BaseOrgEvent
