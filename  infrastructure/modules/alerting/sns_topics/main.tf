# Purpose: Establish centralized AWS SNS topics as notification channels for different alert severities and target teams/services.
# LogicDescription: Creates SNS topics based on input variables. Configures subscriptions (email, SMS, Lambda).
# Manages access policies for publishing to topics (e.g., CloudWatch, Alertmanager).
# Requirements: REQ-17-006, REQ-17-009

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 4.0"
    }
  }
}

resource "aws_sns_topic" "main" {
  for_each = var.sns_topics

  name              = "${var.project_name}-${var.environment}-${each.key}"
  display_name      = lookup(each.value, "display_name", null)
  kms_master_key_id = lookup(each.value, "kms_master_key_id", null) # For server-side encryption

  # Default policy allowing owner to manage, can be overridden by aws_sns_topic_policy
  # policy = data.aws_iam_policy_document.default_sns_topic_policy[each.key].json

  tags = merge(
    var.common_tags,
    lookup(each.value, "tags", {}),
    {
      Name = "${var.project_name}-${var.environment}-${each.key}"
    }
  )
}

resource "aws_sns_topic_subscription" "main" {
  for_each = {
    for topic_key, topic_config in var.sns_topics :
    for sub_idx, sub_config in topic_config.subscriptions :
    "${topic_key}-${sub_idx}" => {
      topic_arn = aws_sns_topic.main[topic_key].arn
      protocol  = sub_config.protocol
      endpoint  = sub_config.endpoint
      # Optional attributes
      endpoint_auto_confirms      = lookup(sub_config, "endpoint_auto_confirms", false)
      raw_message_delivery        = lookup(sub_config, "raw_message_delivery", false)
      filter_policy               = lookup(sub_config, "filter_policy", null) # JSON string
      redrive_policy              = lookup(sub_config, "redrive_policy", null) # JSON string for DLQ
      delivery_policy             = lookup(sub_config, "delivery_policy", null) # JSON string for HTTP/S retries
      confirmation_timeout_in_minutes = lookup(sub_config, "confirmation_timeout_in_minutes", null)
    }
  }

  topic_arn                       = each.value.topic_arn
  protocol                        = each.value.protocol
  endpoint                        = each.value.endpoint
  endpoint_auto_confirms          = each.value.endpoint_auto_confirms
  raw_message_delivery            = each.value.raw_message_delivery
  filter_policy                   = each.value.filter_policy
  redrive_policy                  = each.value.redrive_policy
  delivery_policy                 = each.value.delivery_policy
  confirmation_timeout_in_minutes = each.value.confirmation_timeout_in_minutes
}

data "aws_iam_policy_document" "sns_topic_access_policy" {
  for_each = { for k, v in var.sns_topics : k => v if lookup(v, "access_policy_statements", null) != null }

  statement = [
    # Default statement allowing owner full access
    {
      sid    = "OwnerAccess"
      effect = "Allow"
      principals = [{
        type        = "AWS"
        identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"] # Account root
      }]
      actions   = ["SNS:*"]
      resources = [aws_sns_topic.main[each.key].arn]
    },
    # Additional statements provided in variables
    # Example: Allow CloudWatch Events to publish
    # {
    #   sid = "AllowCloudWatchEvents"
    #   effect = "Allow"
    #   principals = [{
    #     type        = "Service"
    #     identifiers = ["events.amazonaws.com"]
    #   }]
    #   actions = ["SNS:Publish"]
    #   resources = [aws_sns_topic.main[each.key].arn]
    # }
  ]
  dynamic "statement" {
    for_each = lookup(each.value, "access_policy_statements", [])
    content {
      sid        = statement.value.sid
      effect     = statement.value.effect
      principals = statement.value.principals
      actions    = statement.value.actions
      resources  = [aws_sns_topic.main[each.key].arn] # Policy applies to this specific topic
      condition {
        test     = lookup(statement.value.condition, "test", null)
        variable = lookup(statement.value.condition, "variable", null)
        values   = lookup(statement.value.condition, "values", null)
      }
    }
  }
}

resource "aws_sns_topic_policy" "main" {
  for_each = data.aws_iam_policy_document.sns_topic_access_policy
  arn      = aws_sns_topic.main[each.key].arn
  policy   = each.value.json
}

data "aws_caller_identity" "current" {}

# REQ-17-006: Alerting logic (SNS is part of the notification chain)
# REQ-17-009: Notification Channels (SNS topics are primary channels)