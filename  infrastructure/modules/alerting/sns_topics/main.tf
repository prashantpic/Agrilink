# Purpose: Establish centralized AWS SNS topics as notification channels for different alert severities and target teams/services.
# LogicDescription: Creates SNS topics based on input variables (e.g., a map of topic names to configurations).
# Configures subscriptions for these topics (e.g., email endpoints, SMS, Lambda functions).
# Manages access policies for who can publish to these topics (e.g., CloudWatch, Alertmanager).
# ImplementedFeatures: SNS Topic Creation, Subscription Management (Email, SMS, Lambda), Access Policy Configuration.
# RequirementIds: REQ-17-006, REQ-17-009

locals {
  tags = var.tags
}

resource "aws_sns_topic" "main" {
  for_each = var.sns_topics

  name              = "${var.project_name}-${var.environment}-${each.key}"
  display_name      = each.value.display_name != "" ? each.value.display_name : null
  kms_master_key_id = each.value.kms_master_key_id # For server-side encryption

  # Default policy allows account owner full access.
  # More restrictive policies can be defined here or via aws_sns_topic_policy.
  # REQ-17-009: Centralized notification channels
  # REQ-17-006: Route alerts (Alertmanager will publish here, CloudWatch will publish here)

  tags = merge(local.tags, {
    Name        = "${var.project_name}-${var.environment}-${each.key}"
    Purpose     = "Alerting and Notifications"
    TopicKeyRef = each.key
  })
}

resource "aws_sns_topic_subscription" "main" {
  for_each = {
    for topic_key, topic_config in var.sns_topics :
    for sub_idx, sub_config in topic_config.subscriptions :
    "${topic_key}-${sub_idx}" => {
      topic_arn = aws_sns_topic.main[topic_key].arn
      protocol  = sub_config.protocol
      endpoint  = sub_config.endpoint
      # raw_message_delivery, filter_policy, etc. can be added here from sub_config
      raw_message_delivery          = lookup(sub_config, "raw_message_delivery", false)
      filter_policy                 = lookup(sub_config, "filter_policy", null) == null ? null : jsonencode(sub_config.filter_policy)
      redrive_policy                = lookup(sub_config, "redrive_policy", null) == null ? null : jsonencode(sub_config.redrive_policy)
      delivery_policy               = lookup(sub_config, "delivery_policy", null) == null ? null : jsonencode(sub_config.delivery_policy)
      confirmation_timeout_in_minutes = lookup(sub_config, "confirmation_timeout_in_minutes", null)
    }
  }

  topic_arn                       = each.value.topic_arn
  protocol                        = each.value.protocol
  endpoint                        = each.value.endpoint
  raw_message_delivery            = each.value.raw_message_delivery
  filter_policy                   = each.value.filter_policy
  redrive_policy                  = each.value.redrive_policy
  delivery_policy                 = each.value.delivery_policy
  confirmation_timeout_in_minutes = each.value.confirmation_timeout_in_minutes
  # endpoint_auto_confirms is useful for http/https but requires the endpoint to be ready.
  # For email, manual confirmation is needed unless using a pre-confirmed endpoint.
}

data "aws_iam_policy_document" "sns_topic_publish_policy_doc" {
  for_each = { for k, v in var.sns_topics : k => v if length(v.allowed_publish_principals) > 0 || length(v.allowed_publish_services) > 0 }
  # REQ-17-006: Allow Alertmanager (via some AWS service like Lambda or API GW), CloudWatch to publish

  statement {
    sid    = "AllowPublishToTopic"
    effect = "Allow"
    actions = [
      "SNS:Publish",
    ]
    resources = [
      aws_sns_topic.main[each.key].arn,
    ]

    dynamic "principals" {
      for_each = length(each.value.allowed_publish_principals) > 0 ? [1] : [] # Check if list is not empty
      content {
        type        = "AWS"
        identifiers = each.value.allowed_publish_principals
      }
    }
    
    dynamic "principals" {
      for_each = length(each.value.allowed_publish_services) > 0 ? [1] : [] # Check if list is not empty
      content {
        type        = "Service"
        identifiers = each.value.allowed_publish_services # e.g., "cloudwatch.amazonaws.com", "events.amazonaws.com"
      }
    }
  }
}

resource "aws_sns_topic_policy" "publish_policy" {
  for_each = data.aws_iam_policy_document.sns_topic_publish_policy_doc

  arn    = aws_sns_topic.main[each.key].arn
  policy = each.value.json
}